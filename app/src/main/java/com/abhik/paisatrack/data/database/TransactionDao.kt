package com.abhik.paisatrack.data.database

import androidx.room.*
import com.abhik.paisatrack.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE syncStatus != 'PENDING_DELETE' ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE collectionId = :collectionId AND syncStatus != 'PENDING_DELETE' ORDER BY timestamp DESC")
    fun getTransactionsByCollection(collectionId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions WHERE collectionId = :collectionId")
    suspend fun deleteTransactionsByCollection(collectionId: String)

    @Query("UPDATE transactions SET syncStatus = 'PENDING_DELETE', lastSyncedAt = :lastSyncedAt WHERE collectionId = :collectionId")
    suspend fun markTransactionsInCollectionPendingDelete(collectionId: String, lastSyncedAt: Long)

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = 'SYNCED', lastSyncedAt = :lastSyncedAt WHERE id = :id")
    suspend fun markTransactionSynced(id: String, lastSyncedAt: Long)

    @Query("UPDATE transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}
