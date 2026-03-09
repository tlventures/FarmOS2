package com.agriedge.domain.model

/**
 * Common domain models used across the application
 */

data class Location(
    val village: String,
    val district: String,
    val state: String,
    val latitude: Double?,
    val longitude: Double?
) {
    init {
        latitude?.let { require(it in -90.0..90.0) { "Latitude must be between -90 and 90" } }
        longitude?.let { require(it in -180.0..180.0) { "Longitude must be between -180 and 180" } }
    }
}

enum class CropType {
    RICE,
    WHEAT,
    TOMATO,
    POTATO,
    COTTON,
    SUGARCANE,
    UNKNOWN;

    companion object {
        fun fromString(value: String): CropType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

data class ClassificationResult(
    val topPredictions: List<Prediction>,
    val inferenceTime: Long
)

data class Prediction(
    val disease: Disease,
    val confidence: Float
) {
    init {
        require(confidence in 0f..1f) { "Confidence must be between 0 and 1" }
    }
}

/**
 * Result of Stage 1 generic image recognition.
 * Determines if an image is agriculture-related and attempts to detect the crop type.
 */
data class ImageRecognitionResult(
    val isAgricultureRelated: Boolean,
    val detectedCropType: CropType,
    val cropConfidence: Float,
    val topLabels: List<GenericLabel>,
    val inferenceTime: Long
)

data class GenericLabel(
    val name: String,
    val confidence: Float
)
