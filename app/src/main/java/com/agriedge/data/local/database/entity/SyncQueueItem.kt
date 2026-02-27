package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing sync queue items
 * Requirements: 36.1, 36.2, 36.3
 */
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String, // DIAGNOSIS, USER_PROFILE, TRANSACTION, RATING, TELEMETRY
    val entityId: String,
    val operation: String, // CREATE, UPDATE, DELETE
    val payload: String, // JSON serialized entity
    val timestamp: Long,
    val retryCount: Int = 0,
    val status: String, // PENDING, IN_PROGRESS, FAILED, COMPLETED
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null
)
