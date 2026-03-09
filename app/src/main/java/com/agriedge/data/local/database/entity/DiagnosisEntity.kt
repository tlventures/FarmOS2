package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for storing diagnosis records locally
 * Requirements: 5.1, 5.2, 43.1
 */
@Entity(tableName = "diagnoses")
data class DiagnosisEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Long,
    val cropType: String,
    val diseaseId: String,
    val diseaseName: String,
    val diseaseNameLocal: String,
    val scientificName: String,
    val confidence: Float,
    val imagePath: String,
    val additionalImagePaths: String? = null,
    val thumbnailPath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val village: String?,
    val district: String?,
    val state: String?,
    val synced: Boolean = false,
    val requiresFurtherAnalysis: Boolean = false,
    val backendFallbackUsed: Boolean = false
)
