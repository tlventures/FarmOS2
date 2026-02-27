package com.agriedge.data.repository

import com.agriedge.domain.model.ColdStorageFacility
import com.agriedge.domain.repository.ColdStorageRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColdStorageRepositoryImpl @Inject constructor() : ColdStorageRepository {
    
    private val mockFacilities = listOf(
        ColdStorageFacility(
            id = "cs1",
            facilityName = "Delhi Cold Storage Co.",
            location = "New Delhi, India",
            distance = 5.2,
            dailyRate = 800.0,
            currency = "INR",
            availableCapacity = 15.0,
            totalCapacity = 50.0,
            rating = 4.5f,
            reviewCount = 42,
            address = "Azadpur Mandi, Delhi",
            contact = "+91 98765 43210",
            features = listOf("24/7 Security", "Temperature Monitoring", "Loading Bay"),
            temperatureRange = "-5°C to 5°C",
            operatingHours = "24/7"
        ),
        ColdStorageFacility(
            id = "cs2",
            facilityName = "Fresh Harvest Storage",
            location = "Gurgaon, Haryana",
            distance = 12.8,
            dailyRate = 750.0,
            currency = "INR",
            availableCapacity = 25.0,
            totalCapacity = 80.0,
            rating = 4.7f,
            reviewCount = 68,
            address = "Manesar Industrial Area, Gurgaon",
            contact = "+91 98765 43211",
            features = listOf("Humidity Control", "Backup Power", "Easy Access"),
            temperatureRange = "0°C to 10°C",
            operatingHours = "6 AM - 10 PM"
        ),
        ColdStorageFacility(
            id = "cs3",
            facilityName = "AgriCool Facilities",
            location = "Faridabad, Haryana",
            distance = 25.5,
            dailyRate = 700.0,
            currency = "INR",
            availableCapacity = 40.0,
            totalCapacity = 100.0,
            rating = 4.3f,
            reviewCount = 35,
            address = "NH-2, Faridabad",
            contact = "+91 98765 43212",
            features = listOf("Large Capacity", "Affordable Rates", "Flexible Terms"),
            temperatureRange = "-10°C to 5°C",
            operatingHours = "24/7"
        ),
        ColdStorageFacility(
            id = "cs4",
            facilityName = "Premium Cold Chain",
            location = "Noida, Uttar Pradesh",
            distance = 45.0,
            dailyRate = 900.0,
            currency = "INR",
            availableCapacity = 20.0,
            totalCapacity = 60.0,
            rating = 4.8f,
            reviewCount = 91,
            address = "Sector 63, Noida",
            contact = "+91 98765 43213",
            features = listOf("Premium Service", "Quality Assurance", "Insurance Available"),
            temperatureRange = "-15°C to 5°C",
            operatingHours = "24/7"
        )
    )
    
    override suspend fun searchColdStorage(
        latitude: Double,
        longitude: Double,
        radius: Int,
        requiredCapacity: Double
    ): Result<List<ColdStorageFacility>> {
        return try {
            delay(500) // Simulate network delay
            val filtered = mockFacilities.filter {
                it.distance <= radius && it.availableCapacity >= requiredCapacity
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getColdStorageDetails(id: String): Result<ColdStorageFacility> {
        return try {
            delay(300)
            val facility = mockFacilities.find { it.id == id }
            if (facility != null) {
                Result.success(facility)
            } else {
                Result.failure(Exception("Facility not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun bookColdStorage(
        facilityId: String,
        capacity: Double,
        duration: Int
    ): Result<String> {
        return try {
            delay(800)
            val bookingId = "BOOK-${System.currentTimeMillis()}"
            Result.success(bookingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
