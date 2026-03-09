package com.agriedge.domain.model

/**
 * Domain model representing a crop disease
 * Requirements: 2.4, 4.1, 14.1
 */
data class Disease(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val localizedName: String,
    val cropType: CropType,
    val description: String,
    val symptoms: List<String>
) {
    companion object {
        /** Factory for creating an "unidentified disease" placeholder */
        fun unidentified(cropType: CropType) = Disease(
            id = "unidentified",
            commonName = "Unidentified Disease",
            scientificName = "Unknown",
            localizedName = "अज्ञात रोग",
            cropType = cropType,
            description = "The disease could not be identified with sufficient confidence. Further analysis is required.",
            symptoms = emptyList()
        )
    }
}
