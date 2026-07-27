package com.abhik.paisatrack.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abhik.paisatrack.data.database.AppDatabase
import com.abhik.paisatrack.data.model.SyncStatus
import com.abhik.paisatrack.data.network.ApiClient
import com.abhik.paisatrack.data.network.CreateCollectionRequest
import com.abhik.paisatrack.data.network.CreateTransactionRequest
import com.abhik.paisatrack.data.network.UpdateCollectionRequest
import com.abhik.paisatrack.data.network.UpdateTransactionRequest
import com.abhik.paisatrack.data.AuthManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = AuthManager.getUserId(applicationContext) ?: return Result.success()
        val db = AppDatabase.getDatabase(applicationContext)
        val collectionDao = db.collectionDao()
        val transactionDao = db.transactionDao()

        var hasFailures = false

        try {
            // 1. Process Pending Collections
            val pendingCollections = collectionDao.getPendingCollections()
            for (col in pendingCollections) {
                try {
                    when (col.syncStatus) {
                        SyncStatus.PENDING_INSERT.name -> {
                            if (!col.isPrebuilt) {
                                val response = ApiClient.api.createCollection(
                                    CreateCollectionRequest(
                                        id = col.id,
                                        userId = userId,
                                        title = col.name,
                                        color = col.hexColor,
                                        icon = col.iconName
                                    )
                                )
                                if (response.isSuccessful) {
                                    collectionDao.markCollectionSynced(col.id, System.currentTimeMillis())
                                } else {
                                    hasFailures = true
                                }
                            } else {
                                collectionDao.markCollectionSynced(col.id, System.currentTimeMillis())
                            }
                        }
                        SyncStatus.PENDING_UPDATE.name -> {
                            val response = ApiClient.api.updateCollection(
                                col.id,
                                UpdateCollectionRequest(
                                    title = col.name,
                                    color = col.hexColor,
                                    icon = col.iconName
                                )
                            )
                            if (response.isSuccessful) {
                                collectionDao.markCollectionSynced(col.id, System.currentTimeMillis())
                            } else {
                                hasFailures = true
                            }
                        }
                        SyncStatus.PENDING_DELETE.name -> {
                            val response = ApiClient.api.deleteCollection(col.id, userId)
                            if (response.isSuccessful || response.code() == 404) {
                                collectionDao.deleteCollectionById(col.id)
                            } else {
                                hasFailures = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    hasFailures = true
                }
            }

            // 2. Process Pending Transactions
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val pendingTransactions = transactionDao.getPendingTransactions()

            for (tx in pendingTransactions) {
                try {
                    when (tx.syncStatus) {
                        SyncStatus.PENDING_INSERT.name -> {
                            val amount = if (tx.type == "EXPENSE") -tx.amount else tx.amount
                            val response = ApiClient.api.createTransaction(
                                CreateTransactionRequest(
                                    id = tx.id,
                                    userId = userId,
                                    collectionId = tx.collectionId,
                                    title = tx.description,
                                    amount = amount,
                                    category = tx.notes.ifEmpty { "General" },
                                    date = sdfDate.format(Date(tx.timestamp)),
                                    time = sdfTime.format(Date(tx.timestamp))
                                )
                            )
                            if (response.isSuccessful) {
                                transactionDao.markTransactionSynced(tx.id, System.currentTimeMillis())
                            } else {
                                hasFailures = true
                            }
                        }
                        SyncStatus.PENDING_UPDATE.name -> {
                            val amount = if (tx.type == "EXPENSE") -tx.amount else tx.amount
                            val response = ApiClient.api.updateTransaction(
                                tx.id,
                                UpdateTransactionRequest(
                                    title = tx.description,
                                    amount = amount,
                                    category = tx.notes.ifEmpty { "General" },
                                    collectionId = tx.collectionId
                                )
                            )
                            if (response.isSuccessful) {
                                transactionDao.markTransactionSynced(tx.id, System.currentTimeMillis())
                            } else {
                                hasFailures = true
                            }
                        }
                        SyncStatus.PENDING_DELETE.name -> {
                            val response = ApiClient.api.deleteTransaction(tx.id, userId)
                            if (response.isSuccessful || response.code() == 404) {
                                transactionDao.deleteTransactionById(tx.id)
                            } else {
                                hasFailures = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    hasFailures = true
                }
            }

            return if (hasFailures) Result.retry() else Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
