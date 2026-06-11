package com.abhik.paisatrack.data.repository

import com.abhik.paisatrack.data.database.CollectionDao
import com.abhik.paisatrack.data.database.TransactionDao
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncResult {
    SUCCESS,
    NETWORK_ERROR,
    USER_DELETED
}

class FinanceRepository(
    private val collectionDao: CollectionDao,
    private val transactionDao: TransactionDao
) {
    val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByCollection(collectionId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCollection(collectionId)
    }

    suspend fun insertCollection(collection: CollectionEntity): Long {
        return collectionDao.insertCollection(collection)
    }

    suspend fun deleteCollection(collection: CollectionEntity) {
        // First delete all transactions belonging to this collection
        transactionDao.deleteTransactionsByCollection(collection.id)
        collectionDao.deleteCollection(collection)
    }

    suspend fun deleteCollectionById(id: String) {
        transactionDao.deleteTransactionsByCollection(id)
        collectionDao.deleteCollectionById(id)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun ensureDefaultCollectionsPreseeded() {
        val collections = collectionDao.getAllCollections().first()
        if (collections.isEmpty()) {
            val defaults = listOf(
                CollectionEntity(name = "General", hexColor = "#3F51B5", iconName = "category", monthlyBudget = null, isPrebuilt = true),
                CollectionEntity(name = "Food & Dining", hexColor = "#FF9800", iconName = "restaurant", monthlyBudget = 400.0, isPrebuilt = true),
                CollectionEntity(name = "Transport & Logistics", hexColor = "#03A9F4", iconName = "directions_car", monthlyBudget = 150.0, isPrebuilt = true),
                CollectionEntity(name = "Entertainment & Leisure", hexColor = "#9C27B0", iconName = "movie", monthlyBudget = 200.0, isPrebuilt = true),
                CollectionEntity(name = "Salary & Income", hexColor = "#4CAF50", iconName = "account_balance_wallet", monthlyBudget = null, isPrebuilt = true),
                CollectionEntity(name = "Heaths & Care", hexColor = "#E91E63", iconName = "local_hospital", monthlyBudget = 100.0, isPrebuilt = true)
            )
            for (col in defaults) {
                collectionDao.insertCollection(col)
            }
        }
    }

    // ─── Backend Sync Logic ──────────────────────────────────────────

    suspend fun signupUser(googleId: String, email: String, name: String, image: String): SignupResponse? {
        return try {
            val response = ApiClient.api.signupUser(SignupRequest(googleId, email, name, image))
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if the user still exists on the backend.
     * Returns true if user exists, false if deleted or API unreachable.
     */
    suspend fun checkUserExists(userId: String, noCache: Boolean = false): Boolean {
        return try {
            val response = ApiClient.api.getCurrentUser(userId, noCache = if (noCache) "true" else "false")
            if (response.isSuccessful) {
                true
            } else if (response.code() == 404) {
                // User explicitly does not exist in DB anymore
                false
            } else {
                // On rate limit (429), server error (500), etc. assume user exists to avoid false logouts
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // On network error, assume user exists to avoid false logouts
            true
        }
    }

    /**
     * Clear all local data (collections + transactions) from Room.
     */
    suspend fun clearAllLocalData() {
        val localCols = collectionDao.getAllCollections().first()
        for (col in localCols) {
            collectionDao.deleteCollection(col)
        }
        val localTxns = transactionDao.getAllTransactions().first()
        for (tx in localTxns) {
            transactionDao.deleteTransaction(tx)
        }
    }

    suspend fun syncFromBackend(userId: String, noCache: Boolean = false): SyncResult {
        try {
            // First, verify user still exists on the backend
            val userExists = checkUserExists(userId, noCache)
            if (!userExists) {
                // User was deleted from DB — clear local data
                clearAllLocalData()
                return SyncResult.USER_DELETED
            }

            val collectionsResponse = ApiClient.api.getCollections(userId, noCache = if (noCache) "true" else "false")
            val transactionsResponse = ApiClient.api.getTransactions(userId, noCache = if (noCache) "true" else "false")
            
            if (collectionsResponse.isSuccessful && transactionsResponse.isSuccessful) {
                val remoteCollections = collectionsResponse.body() ?: emptyList()
                val remoteTransactions = transactionsResponse.body() ?: emptyList()

                // Clear current local tables first to fetch fresh database data
                val localCols = collectionDao.getAllCollections().first()
                for (col in localCols) {
                    collectionDao.deleteCollection(col)
                }
                val localTxns = transactionDao.getAllTransactions().first()
                for (tx in localTxns) {
                    transactionDao.deleteTransaction(tx)
                }

                // Insert remote collections
                remoteCollections.forEach { rc ->
                    collectionDao.insertCollection(
                        CollectionEntity(
                            id = rc.id,
                            name = rc.title,
                            hexColor = rc.color ?: "#3F51B5",
                            iconName = rc.icon ?: "category",
                            monthlyBudget = if (rc.amount > 0.0) rc.amount else null,
                            createdTimestamp = rc.createdAt,
                            isPrebuilt = rc.isPrebuilt ?: false
                        )
                    )
                }

                // Insert remote transactions
                remoteTransactions.forEach { rt ->
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            id = rt.id,
                            description = rt.title,
                            amount = if (rt.amount < 0.0) -rt.amount else rt.amount,
                            type = if (rt.amount < 0.0) "EXPENSE" else "INCOME",
                            collectionId = rt.collectionId ?: "",
                            notes = rt.category ?: "General",
                            timestamp = rt.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
                return SyncResult.SUCCESS
            }
            return SyncResult.NETWORK_ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            return SyncResult.NETWORK_ERROR
        }
    }

    suspend fun addCollectionRemote(userId: String, collection: CollectionEntity) {
        // Prebuilt collections are always seeded server-side on user creation — never upload them
        if (collection.isPrebuilt) return
        try {
            ApiClient.api.createCollection(
                CreateCollectionRequest(
                    id = collection.id,
                    userId = userId,
                    title = collection.name,
                    color = collection.hexColor,
                    icon = collection.iconName
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteCollectionRemote(userId: String, collectionId: String) {
        try {
            ApiClient.api.deleteCollection(collectionId, userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addTransactionRemote(userId: String, transaction: TransactionEntity) {
        try {
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateStr = sdfDate.format(Date(transaction.timestamp))
            val timeStr = sdfTime.format(Date(transaction.timestamp))
            
            ApiClient.api.createTransaction(
                CreateTransactionRequest(
                    id = transaction.id,
                    userId = userId,
                    collectionId = transaction.collectionId,
                    title = transaction.description,
                    amount = if (transaction.type == "EXPENSE") -transaction.amount else transaction.amount,
                    category = transaction.notes.ifEmpty { "General" },
                    date = dateStr,
                    time = timeStr
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateTransactionRemote(userId: String, transaction: TransactionEntity) {
        try {
            val localAmount = if (transaction.type == "EXPENSE") -transaction.amount else transaction.amount
            ApiClient.api.updateTransaction(
                transaction.id,
                UpdateTransactionRequest(
                    title = transaction.description,
                    amount = localAmount,
                    category = transaction.notes.ifEmpty { "General" },
                    collectionId = transaction.collectionId
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTransactionRemote(userId: String, transactionId: String) {
        try {
            ApiClient.api.deleteTransaction(transactionId, userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updatePushTokenRemote(userId: String, token: String, unregister: Boolean = false): Boolean {
        return try {
            val response = ApiClient.api.updatePushToken(UpdatePushTokenRequest(userId, token, unregister))
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun migrateLocalDataToBackend(userId: String) {
        try {
            // 1. Get all local collections and transactions
            val localCollections = collectionDao.getAllCollections().first()
            val localTransactions = transactionDao.getAllTransactions().first()

            // If there's no local data to migrate, skip
            if (localCollections.isEmpty()) return

            // 2. Fetch existing remote collections to check for matches/duplication
            val remoteCollectionsResponse = ApiClient.api.getCollections(userId)
            val remoteCollections = if (remoteCollectionsResponse.isSuccessful) {
                remoteCollectionsResponse.body() ?: emptyList()
            } else {
                emptyList()
            }

            // Map to store Local Collection ID -> Remote Collection ID
            val collectionIdMap = mutableMapOf<String, String>()

            // Map prebuilt collections by title to their remote IDs (backend always seeds them for new users)
            for (localCol in localCollections) {
                if (localCol.isPrebuilt) {
                    // Find the matching remote prebuilt collection by title instead of uploading it
                    val matchedRemote = remoteCollections.find { it.title.equals(localCol.name, ignoreCase = true) }
                    if (matchedRemote != null) {
                        collectionIdMap[localCol.id] = matchedRemote.id
                    }
                    continue // Never upload prebuilt collections to the backend
                }
                // Check if a user-created collection with the same title already exists on the server
                val matchedRemote = remoteCollections.find { it.title.equals(localCol.name, ignoreCase = true) }
                if (matchedRemote != null) {
                    collectionIdMap[localCol.id] = matchedRemote.id
                } else {
                    // Upload the new user-created collection
                    try {
                        val createResponse = ApiClient.api.createCollection(
                            CreateCollectionRequest(
                                id = localCol.id,
                                userId = userId,
                                title = localCol.name,
                                color = localCol.hexColor,
                                icon = localCol.iconName
                            )
                        )
                        if (createResponse.isSuccessful && createResponse.body() != null) {
                            collectionIdMap[localCol.id] = createResponse.body()!!.id
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 3. Migrate transactions
            // Fetch existing remote transactions to avoid duplicating transactions
            val remoteTransactionsResponse = ApiClient.api.getTransactions(userId)
            val remoteTransactions = if (remoteTransactionsResponse.isSuccessful) {
                remoteTransactionsResponse.body() ?: emptyList()
            } else {
                emptyList()
            }

            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

            for (localTx in localTransactions) {
                val remoteColId = collectionIdMap[localTx.collectionId] ?: continue

                // Check if a remote transaction with the same description, amount and approximate time already exists
                val localAmount = if (localTx.type == "EXPENSE") -localTx.amount else localTx.amount
                val isAlreadySynced = remoteTransactions.any { rt ->
                    rt.id == localTx.id || (
                        rt.title.equals(localTx.description, ignoreCase = true) &&
                                Math.abs(rt.amount - localAmount) < 0.01 &&
                                rt.collectionId == remoteColId
                    )
                }

                if (!isAlreadySynced) {
                    try {
                        val dateStr = sdfDate.format(Date(localTx.timestamp))
                        val timeStr = sdfTime.format(Date(localTx.timestamp))
                        ApiClient.api.createTransaction(
                            CreateTransactionRequest(
                                id = localTx.id,
                                userId = userId,
                                collectionId = remoteColId,
                                title = localTx.description,
                                amount = localAmount,
                                category = localTx.notes.ifEmpty { "General" },
                                date = dateStr,
                                time = timeStr
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteUserRemote(userId: String): Boolean {
        return try {
            val response = ApiClient.api.deleteUser(userId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

