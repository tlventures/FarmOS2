package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user profile data locally
 * Requirements: 34.1, 34.2, 34.3, 43.1
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val phoneNumber: String,
    val languageCode: String,
    val defaultLocation: String,
    val village: String,
    val district: String,
    val state: String,
    val latitude: Double?,
    val longitude: Double?,
    val primaryCrops: String, // Comma-separated crop types
    val createdAt: Long,
    val lastSyncedAt: Long?
)
