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
)
