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
    val imagePaths: List<String>,
    val primaryImagePath: String,
    val location: Location?,
    val synced: Boolean = false,
    val requiresFurtherAnalysis: Boolean = false,
    val backendFallbackUsed: Boolean = false
) {
    /** Backward-compatible single image path accessor */
    val imagePath: String get() = primaryImagePath

    init {
        require(confidence in 0f..1f) { "Confidence must be between 0 and 1" }
    }

    val confidencePercentage: Int
        get() = (confidence * 100).toInt()

    val isLowConfidence: Boolean
        get() = confidence < 0.7f

    companion object {
        /** Create a Diagnosis with a single image (backward compatibility) */
        fun withSingleImage(
            id: String = UUID.randomUUID().toString(),
            userId: String,
            timestamp: Long,
            cropType: CropType,
            disease: Disease,
            confidence: Float,
            imagePath: String,
            location: Location?,
            synced: Boolean = false,
            requiresFurtherAnalysis: Boolean = false,
            backendFallbackUsed: Boolean = false
        ) = Diagnosis(
            id = id,
            userId = userId,
            timestamp = timestamp,
            cropType = cropType,
            disease = disease,
            confidence = confidence,
            imagePaths = listOf(imagePath),
            primaryImagePath = imagePath,
            location = location,
            synced = synced,
            requiresFurtherAnalysis = requiresFurtherAnalysis,
            backendFallbackUsed = backendFallbackUsed
        )
    }
}
