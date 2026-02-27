package com.agriedge.domain.model

/**
 * Domain model representing a service provider (buyer, cold storage, equipment rental)
 * Requirements: 16.1, 17.1, 20.1, 21.1, 23.1, 24.1, 29.1
 */
data class Provider(
    val id: String,
    val name: String,
    val type: ProviderType,
    val location: Location,
    val distance: Double,
    val rating: Rating,
    val contactPhone: String,
    val details: ProviderDetails
)

enum class ProviderType {
    BUYER,
    COLD_STORAGE,
    EQUIPMENT_RENTAL
}

data class Rating(
    val averageRating: Float,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
) {
    init {
        require(averageRating in 0f..5f) { "Average rating must be between 0 and 5" }
    }
}

sealed class ProviderDetails {
    data class BuyerDetails(
        val pricePerUnit: Double,
        val unit: QuantityUnit,
        val pickupAvailable: Boolean,
        val estimatedPickupDate: Long?
    ) : ProviderDetails()
    
    data class ColdStorageDetails(
        val dailyRate: Double,
        val unit: String,
        val availableCapacity: Double,
        val capacityUnit: String,
        val address: String
    ) : ProviderDetails()
    
    data class EquipmentDetails(
        val equipmentType: String,
        val model: String,
        val specifications: Map<String, String>,
        val dailyRate: Double,
        val deliveryAvailable: Boolean,
        val deliveryCharge: Double?
    ) : ProviderDetails()
}
