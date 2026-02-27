package com.agriedge.domain.usecase

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Location
import com.agriedge.domain.model.Provider
import com.agriedge.domain.model.Quantity

/**
 * Use case for searching buyers in the market
 * Requirements: 16.1, 16.2, 17.1
 */
interface SearchBuyersUseCase {
    
    /**
     * Search for buyers based on crop, quantity, and location
     * 
     * @param cropType The crop to sell
     * @param quantity The quantity available
     * @param location The farmer's location
     * @param radius Search radius in kilometers
     * @return Result containing list of buyers sorted by price
     */
    suspend operator fun invoke(
        cropType: CropType,
        quantity: Quantity,
        location: Location,
        radius: Int = 50
    ): Result<List<Provider>>
}
