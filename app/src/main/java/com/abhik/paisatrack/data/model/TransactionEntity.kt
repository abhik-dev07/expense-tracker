package com.abhik.paisatrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val collectionId: Long, // References CollectionEntity.id
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
