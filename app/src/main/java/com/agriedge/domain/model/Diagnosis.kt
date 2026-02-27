package com.agriedge.domain.model

import java.util.UUID

/**
 * Domain model representing a crop disease diagnosis
 * Requirements: 2.1, 5.1
 */
data class Diagnosis(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Long,
    val cropType: CropType,
    val disease: Disease,
    val confidence: Float,
    val imagePath: String,
    val location: Location?,
    val synced: Boolean = false
) {
    init {
        require(confidence in 0f..1f) { "Confidence must be between 0 and 1" }
    }
    
    val confidencePercentage: Int
        get() = (confidence * 100).toInt()
    
    val isLowConfidence: Boolean
        get() = confidence < 0.7f
}
