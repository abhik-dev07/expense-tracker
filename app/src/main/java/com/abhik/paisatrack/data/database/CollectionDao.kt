package com.abhik.paisatrack.data.database

import androidx.room.*
import com.abhik.paisatrack.data.model.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections WHERE syncStatus != 'PENDING_DELETE' ORDER BY createdTimestamp ASC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionById(id: String): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: String)

    @Query("SELECT * FROM collections WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingCollections(): List<CollectionEntity>

    @Query("UPDATE collections SET syncStatus = 'SYNCED', lastSyncedAt = :lastSyncedAt WHERE id = :id")
    suspend fun markCollectionSynced(id: String, lastSyncedAt: Long)

    @Query("UPDATE collections SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}
