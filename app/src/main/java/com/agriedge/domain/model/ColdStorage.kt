package com.agriedge.domain.model

data class ColdStorageFacility(
    val id: String,
    val facilityName: String,
    val location: String,
    val distance: Double, // in km
    val dailyRate: Double,
    val currency: String = "INR",
    val availableCapacity: Double, // in tons
    val totalCapacity: Double,
    val rating: Float?,
    val reviewCount: Int,
    val address: String,
    val contact: String,
    val features: List<String>,
    val temperatureRange: String,
    val operatingHours: String
)

data class ColdStorageBooking(
    val id: String,
    val facilityId: String,
    val userId: String,
    val capacity: Double,
    val duration: Int, // in days
    val startDate: Long,
    val totalCost: Double,
    val status: BookingStatus
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
