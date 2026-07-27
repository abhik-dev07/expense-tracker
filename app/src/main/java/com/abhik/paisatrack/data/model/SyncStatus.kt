package com.abhik.paisatrack.data.model

enum class SyncStatus {
    SYNCED,
    PENDING_INSERT,
    PENDING_UPDATE,
    PENDING_DELETE,
    FAILED
}
