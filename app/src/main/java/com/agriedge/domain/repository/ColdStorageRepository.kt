package com.agriedge.domain.repository

import com.agriedge.domain.model.ColdStorageFacility

interface ColdStorageRepository {
    suspend fun searchColdStorage(
        latitude: Double,
        longitude: Double,
        radius: Int,
        requiredCapacity: Double
    ): Result<List<ColdStorageFacility>>
    
    suspend fun getColdStorageDetails(id: String): Result<ColdStorageFacility>
    
    suspend fun bookColdStorage(
        facilityId: String,
        capacity: Double,
        duration: Int
    ): Result<String> // Returns booking ID
}
