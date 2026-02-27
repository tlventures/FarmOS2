package com.agriedge.domain.repository

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Location
import com.agriedge.domain.model.Provider
import com.agriedge.domain.model.Quantity

/**
 * Repository interface for market search operations
 * Requirements: 16.1, 20.1, 23.1
 */
interface MarketRepository {
    
    /**
     * Search for buyers
     */
    suspend fun searchBuyers(
        cropType: CropType,
        quantity: Quantity,
        location: Location,
        radius: Int
    ): Result<List<Provider>>
    
    /**
     * Search for cold storage facilities
     */
    suspend fun searchColdStorage(
        location: Location,
        radius: Int,
        requiredCapacity: Double,
        duration: Int
    ): Result<List<Provider>>
    
    /**
     * Search for equipment rental services
     */
    suspend fun searchEquipment(
        equipmentType: String,
        location: Location,
        radius: Int,
        startDate: Long,
        endDate: Long
    ): Result<List<Provider>>
    
    /**
     * Get provider details including reviews
     */
    suspend fun getProviderDetails(providerId: String): Result<Provider>
    
    /**
     * Get provider reviews
     */
    suspend fun getProviderReviews(providerId: String): Result<List<ProviderReview>>
}

data class ProviderReview(
    val id: String,
    val providerId: String,
    val userId: String,
    val rating: Int,
    val reviewText: String?,
    val createdAt: Long
)
