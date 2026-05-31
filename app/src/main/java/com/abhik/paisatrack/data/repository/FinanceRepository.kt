package com.abhik.paisatrack.data.repository

import com.abhik.paisatrack.data.database.CollectionDao
import com.abhik.paisatrack.data.database.TransactionDao
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FinanceRepository(
    private val collectionDao: CollectionDao,
    private val transactionDao: TransactionDao
) {
    val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByCollection(collectionId: Long): Flow<List<TransactionEntity>> {
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

    suspend fun deleteCollectionById(id: Long) {
        transactionDao.deleteTransactionsByCollection(id)
        collectionDao.deleteCollectionById(id)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
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
}
