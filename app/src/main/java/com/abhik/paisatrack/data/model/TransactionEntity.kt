package com.abhik.paisatrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val collectionId: String, // References CollectionEntity.id
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
