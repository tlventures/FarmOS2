package com.agriedge.domain.repository

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Treatment

/**
 * Repository interface for treatment recommendation data operations
 * Requirements: 4.1, 15.1
 */
interface TreatmentRepository {
    
    /**
     * Get treatment recommendations for a disease
     */
    suspend fun getTreatment(
        diseaseId: String,
        cropType: CropType,
        languageCode: String
    ): Treatment?
    
    /**
     * Save treatment recommendations locally
     */
    suspend fun saveTreatment(treatment: Treatment)
    
    /**
     * Fetch treatment from remote API (Bedrock)
     */
    suspend fun fetchTreatmentFromRemote(
        diseaseId: String,
        cropType: CropType,
        languageCode: String
    ): Result<Treatment>
}
