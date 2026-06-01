package com.abhik.paisatrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val hexColor: String, // Hex string, e.g., "#3F51B5"
    val iconName: String, // Name matching a key in our icon lookup, e.g., "General", "Food", "Transport"
    val monthlyBudget: Double? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isPrebuilt: Boolean = false
)
