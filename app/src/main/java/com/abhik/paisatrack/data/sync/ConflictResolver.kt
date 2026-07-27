package com.abhik.paisatrack.data.sync

import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.SyncStatus
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.network.NetworkCollection
import com.abhik.paisatrack.data.network.NetworkTransaction

object ConflictResolver {

    /**
     * Resolves conflict for Collection entity using Server Wins / Timestamp Merge.
     * Returns true if server version should overwrite local database, false if local should be kept/pushed.
     */
    fun resolveCollectionConflict(
        local: CollectionEntity?,
        remote: NetworkCollection
    ): Boolean {
        if (local == null) return true // New entity from server

        val remoteTimestamp = remote.updatedAt ?: remote.createdAt

        // If local is clean (SYNCED), server wins if remote timestamp is equal or newer
        if (local.syncStatus == SyncStatus.SYNCED.name) {
            return remoteTimestamp >= local.lastSyncedAt
        }

        // If local has pending local edits, Server Wins if remote was modified after local's last sync
        return remoteTimestamp > local.lastSyncedAt
    }

    /**
     * Resolves conflict for Transaction entity using Server Wins / Timestamp Merge.
     * Returns true if server version should overwrite local database, false if local should be kept/pushed.
     */
    fun resolveTransactionConflict(
        local: TransactionEntity?,
        remote: NetworkTransaction
    ): Boolean {
        if (local == null) return true // New entity from server

        val remoteTimestamp = remote.updatedAt ?: remote.createdAt ?: 0L

        if (local.syncStatus == SyncStatus.SYNCED.name) {
            return remoteTimestamp >= local.lastSyncedAt
        }

        return remoteTimestamp > local.lastSyncedAt
    }
}
