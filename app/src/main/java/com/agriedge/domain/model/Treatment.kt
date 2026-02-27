package com.agriedge.domain.model

/**
 * Domain model representing treatment recommendations for a disease
 * Requirements: 4.1, 4.2, 4.3, 4.4, 15.1
 */
data class Treatment(
    val id: String,
    val diseaseId: String,
    val cropType: CropType,
    val organicOptions: List<TreatmentOption>,
    val chemicalOptions: List<TreatmentOption>,
    val preventiveMeasures: List<String>,
    val languageCode: String
)

data class TreatmentOption(
    val name: String,
    val localizedName: String,
    val description: String,
    val applicationTiming: String,
    val dosage: String,
    val products: List<Product>
)

data class Product(
    val name: String,
    val localizedName: String,
    val type: ProductType,
    val availability: String
)

enum class ProductType {
    ORGANIC,
    CHEMICAL,
    BIOLOGICAL
}
