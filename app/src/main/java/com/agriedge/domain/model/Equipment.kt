package com.agriedge.domain.model

data class EquipmentRental(
    val id: String,
    val providerName: String,
    val equipmentType: EquipmentType,
    val model: String,
    val specifications: EquipmentSpecs,
    val dailyRate: Double,
    val currency: String = "INR",
    val distance: Double,
    val rating: Float?,
    val reviewCount: Int,
    val deliveryAvailable: Boolean,
    val deliveryCharge: Double?,
    val contact: String,
    val imageUrl: String? = null,
    val location: String
)

enum class EquipmentType {
    TRACTOR,
    SPRAYER,
    HARVESTER,
    PLOUGH,
    SEEDER;

    fun displayName(): String = when (this) {
        TRACTOR -> "Tractor"
        SPRAYER -> "Sprayer"
        HARVESTER -> "Harvester"
        PLOUGH -> "Plough"
        SEEDER -> "Seeder"
    }
}

data class EquipmentSpecs(
    val horsepower: Int?,
    val capacity: String?,
    val age: Int?, // in years
    val fuelType: String?,
    val condition: String?
)

data class EquipmentBooking(
    val id: String,
    val equipmentId: String,
    val userId: String,
    val startDate: Long,
    val endDate: Long,
    val deliveryRequired: Boolean,
    val totalCost: Double,
    val status: com.agriedge.domain.model.BookingStatus
)
