package com.agriedge.domain.usecase

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Treatment

/**
 * Use case for retrieving treatment recommendations
 * Requirements: 4.1, 4.2, 4.3
 */
interface GetTreatmentRecommendationUseCase {
    
    /**
     * Get treatment recommendations for a disease
     * 
     * @param diseaseId The disease identifier
     * @param cropType The crop type
     * @param languageCode The user's language preference
     * @return Result containing treatment recommendations or error
     */
    suspend operator fun invoke(
        diseaseId: String,
        cropType: CropType,
        languageCode: String
    ): Result<Treatment>
}
