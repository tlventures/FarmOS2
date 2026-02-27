package com.agriedge.domain.usecase

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Treatment
import com.agriedge.domain.repository.TreatmentRepository
import javax.inject.Inject

/**
 * Implementation of GetTreatmentRecommendationUseCase
 * Requirements: 4.1, 4.2, 4.3
 */
class GetTreatmentRecommendationUseCaseImpl @Inject constructor(
    private val treatmentRepository: TreatmentRepository
) : GetTreatmentRecommendationUseCase {
    
    override suspend fun invoke(
        diseaseId: String,
        cropType: CropType,
        languageCode: String
    ): Result<Treatment> {
        return try {
            // Try to get from local cache first
            val localTreatment = treatmentRepository.getTreatment(diseaseId, cropType, languageCode)
            if (localTreatment != null) {
                return Result.success(localTreatment)
            }
            
            // Fetch from remote if not in cache
            val result = treatmentRepository.fetchTreatmentFromRemote(diseaseId, cropType, languageCode)
            result.onSuccess { treatment ->
                // Cache the treatment locally
                treatmentRepository.saveTreatment(treatment)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
