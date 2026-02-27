package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing treatment recommendations locally
 * Requirements: 4.1, 4.2, 4.3, 15.1
 */
@Entity(tableName = "treatments")
data class TreatmentEntity(
    @PrimaryKey
    val id: String,
    val diseaseId: String,
    val cropType: String,
    val treatmentType: String, // ORGANIC, CHEMICAL
    val description: String,
    val descriptionLocal: String,
    val products: String, // JSON array of products
    val applicationTiming: String,
    val dosage: String,
    val preventiveMeasures: String?,
    val expectedRecoveryDays: Int?,
    val languageCode: String,
    val cachedAt: Long
)
