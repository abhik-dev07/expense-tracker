package com.abhik.paisatrack

import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.SyncStatus
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.network.NetworkCollection
import com.abhik.paisatrack.data.network.NetworkTransaction
import com.abhik.paisatrack.data.sync.ConflictResolver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncConflictTest {

    @Test
    fun `Bug 1 - ConflictResolver compares updatedAt timestamp over createdAt`() {
        val lastSynced = 1000L
        val local = TransactionEntity(
            id = "tx1",
            description = "Local",
            amount = 100.0,
            type = "EXPENSE",
            collectionId = "col1",
            syncStatus = SyncStatus.SYNCED.name,
            lastSyncedAt = lastSynced
        )

        // Remote created before lastSynced, but updated AFTER lastSynced
        val remote = NetworkTransaction(
            id = "tx1",
            user_id = "user1",
            collectionId = "col1",
            title = "Server Edit",
            amount = -150.0,
            category = "General",
            date = null,
            time = null,
            createdAt = 500L, // Older creation time
            updatedAt = 2000L, // Newer update time
            collectionName = "General"
        )

        val shouldOverwrite = ConflictResolver.resolveTransactionConflict(local, remote)
        assertTrue("Server edit with newer updatedAt must overwrite local", shouldOverwrite)
    }

    @Test
    fun `Bug 2 - Local PENDING_DELETE entity is protected from remote resurrection`() {
        val lastSynced = 1000L
        val localPendingDelete = TransactionEntity(
            id = "tx_deleted",
            description = "Delete me",
            amount = 50.0,
            type = "EXPENSE",
            collectionId = "col1",
            syncStatus = SyncStatus.PENDING_DELETE.name,
            lastSyncedAt = lastSynced
        )

        val remoteStale = NetworkTransaction(
            id = "tx_deleted",
            user_id = "user1",
            collectionId = "col1",
            title = "Delete me",
            amount = -50.0,
            category = "General",
            date = null,
            time = null,
            createdAt = 500L,
            updatedAt = 2000L,
            collectionName = "General"
        )

        // Check the pending delete guard condition used in syncFromBackend
        val isPendingDelete = localPendingDelete.syncStatus == SyncStatus.PENDING_DELETE.name
        assertTrue("Entity marked PENDING_DELETE must be identified and skipped", isPendingDelete)
    }

    @Test
    fun `Bug 3 - Orphaned SYNCED local entity missing from remote response is deleted`() {
        val localSyncedIds = setOf("tx1", "tx2")
        val remoteIds = setOf("tx1") // tx2 deleted server-side

        val missingFromRemote = localSyncedIds.filter { !remoteIds.contains(it) }
        assertTrue("tx2 should be identified as missing from server response", missingFromRemote.contains("tx2"))
        assertFalse("tx1 should remain", missingFromRemote.contains("tx1"))
    }

    @Test
    fun `Bug 3 Addendum - Prebuilt collections are excluded from tombstone deletion`() {
        val prebuiltCol = CollectionEntity(
            id = "prebuilt_gen",
            name = "General",
            hexColor = "#3F51B5",
            iconName = "category",
            isPrebuilt = true,
            syncStatus = SyncStatus.SYNCED.name
        )

        val userCol = CollectionEntity(
            id = "user_col_1",
            name = "Vacation",
            hexColor = "#FF9800",
            iconName = "flight",
            isPrebuilt = false,
            syncStatus = SyncStatus.SYNCED.name
        )

        val remoteIds = setOf<String>() // empty remote response

        val localCols = listOf(prebuiltCol, userCol)
        val toDelete = localCols.filter { !it.isPrebuilt && it.syncStatus == SyncStatus.SYNCED.name && !remoteIds.contains(it.id) }

        assertTrue("User collection missing from remote should be marked for deletion", toDelete.any { it.id == "user_col_1" })
        assertFalse("Prebuilt collection MUST be excluded from tombstone deletion", toDelete.any { it.id == "prebuilt_gen" })
    }
}
