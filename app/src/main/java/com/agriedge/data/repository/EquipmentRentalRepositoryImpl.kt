package com.agriedge.data.repository

import com.agriedge.domain.model.EquipmentRental
import com.agriedge.domain.model.EquipmentSpecs
import com.agriedge.domain.model.EquipmentType
import com.agriedge.domain.repository.EquipmentRentalRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRentalRepositoryImpl @Inject constructor() : EquipmentRentalRepository {
    
    private val mockEquipment = listOf(
        EquipmentRental(
            id = "eq1",
            providerName = "Sharma Farm Equipment",
            equipmentType = EquipmentType.TRACTOR,
            model = "Mahindra 575 DI",
            specifications = EquipmentSpecs(
                horsepower = 45,
                capacity = "N/A",
                age = 2,
                fuelType = "Diesel",
                condition = "Excellent"
            ),
            dailyRate = 2500.0,
            currency = "INR",
            distance = 8.5,
            rating = 4.6f,
            reviewCount = 28,
            deliveryAvailable = true,
            deliveryCharge = 500.0,
            contact = "+91 98765 11111",
            location = "Gurgaon, Haryana"
        ),
        EquipmentRental(
            id = "eq2",
            providerName = "AgriTech Rentals",
            equipmentType = EquipmentType.SPRAYER,
            model = "Aspee HTP Power Sprayer",
            specifications = EquipmentSpecs(
                horsepower = null,
                capacity = "16L Tank",
                age = 1,
                fuelType = "Petrol",
                condition = "Like New"
            ),
            dailyRate = 600.0,
            currency = "INR",
            distance = 5.2,
            rating = 4.8f,
            reviewCount = 45,
            deliveryAvailable = true,
            deliveryCharge = 200.0,
            contact = "+91 98765 22222",
            location = "New Delhi, India"
        ),
        EquipmentRental(
            id = "eq3",
            providerName = "Harvest Solutions",
            equipmentType = EquipmentType.HARVESTER,
            model = "Preet 987 Self Propelled",
            specifications = EquipmentSpecs(
                horsepower = 95,
                capacity = "8,000 kg/hr",
                age = 3,
                fuelType = "Diesel",
                condition = "Very Good"
            ),
            dailyRate = 6000.0,
            currency = "INR",
            distance = 15.0,
            rating = 4.7f,
            reviewCount = 19,
            deliveryAvailable = true,
            deliveryCharge = 1500.0,
            contact = "+91 98765 33333",
            location = "Faridabad, Haryana"
        ),
        EquipmentRental(
            id = "eq4",
            providerName = "Kumar Equipment Hire",
            equipmentType = EquipmentType.PLOUGH,
            model = "Lemken Vari-Opal",
            specifications = EquipmentSpecs(
                horsepower = null,
                capacity = "3-Furrow",
                age = 4,
                fuelType = "N/A",
                condition = "Good"
            ),
            dailyRate = 800.0,
            currency = "INR",
            distance = 10.5,
            rating = 4.4f,
            reviewCount = 32,
            deliveryAvailable = false,
            deliveryCharge = null,
            contact = "+91 98765 44444",
            location = "Noida, Uttar Pradesh"
        ),
        EquipmentRental(
            id = "eq5",
            providerName = "Modern Farming Ltd",
            equipmentType = EquipmentType.SEEDER,
            model = "Fieldking Seed Drill",
            specifications = EquipmentSpecs(
                horsepower = null,
                capacity = "9 Tyne",
                age = 2,
                fuelType = "N/A",
                condition = "Excellent"
            ),
            dailyRate = 1000.0,
            currency = "INR",
            distance = 12.0,
            rating = 4.5f,
            reviewCount = 24,
            deliveryAvailable = true,
            deliveryCharge = 400.0,
            contact = "+91 98765 55555",
            location = "Gurgaon, Haryana"
        ),
        EquipmentRental(
            id = "eq6",
            providerName = "Farm Power Services",
            equipmentType = EquipmentType.TRACTOR,
            model = "Swaraj 855 FE",
            specifications = EquipmentSpecs(
                horsepower = 60,
                capacity = "N/A",
                age = 1,
                fuelType = "Diesel",
                condition = "Like New"
            ),
            dailyRate = 3000.0,
            currency = "INR",
            distance = 7.8,
            rating = 4.9f,
            reviewCount = 52,
            deliveryAvailable = true,
            deliveryCharge = 600.0,
            contact = "+91 98765 66666",
            location = "New Delhi, India"
        )
    )
    
    override suspend fun searchEquipment(
        latitude: Double,
        longitude: Double,
        radius: Int,
        equipmentType: EquipmentType?,
        startDate: Long,
        endDate: Long
    ): Result<List<EquipmentRental>> {
        return try {
            delay(500)
            val filtered = mockEquipment.filter {
                val withinRadius = it.distance <= radius
                val matchesType = equipmentType == null || it.equipmentType == equipmentType
                withinRadius && matchesType
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEquipmentDetails(id: String): Result<EquipmentRental> {
        return try {
            delay(300)
            val equipment = mockEquipment.find { it.id == id }
            if (equipment != null) {
                Result.success(equipment)
            } else {
                Result.failure(Exception("Equipment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun bookEquipment(
        equipmentId: String,
        startDate: Long,
        endDate: Long,
        deliveryRequired: Boolean
    ): Result<String> {
        return try {
            delay(800)
            val bookingId = "EQBOOK-${System.currentTimeMillis()}"
            Result.success(bookingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
