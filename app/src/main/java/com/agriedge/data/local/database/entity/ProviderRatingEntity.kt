package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for storing provider ratings locally
 * Requirements: 31.1, 31.2, 31.3, 43.1
 */
@Entity(tableName = "provider_ratings")
data class ProviderRatingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val providerId: String,
    val transactionId: String,
    val rating: Int, // 1-5 stars
    val reviewText: String?,
    val createdAt: Long,
    val synced: Boolean = false
)
