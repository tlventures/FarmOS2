package com.agriedge.domain.usecase

import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.repository.DiagnosisRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving diagnosis history.
 * 
 * This use case:
 * 1. Queries diagnoses from local database
 * 2. Sorts by timestamp descending (most recent first)
 * 3. Returns as Flow for reactive updates
 * 
 * Requirements: 8.1, 8.2
 */
class GetDiagnosisHistoryUseCase @Inject constructor(
    private val diagnosisRepository: DiagnosisRepository
) {
    
    /**
     * Get all diagnoses for a user as a reactive Flow.
     * The Flow will emit new values whenever the database is updated.
     * 
     * @param userId The ID of the user
     * @return Flow of diagnosis list, sorted by timestamp descending
     */
    operator fun invoke(userId: String): Flow<List<Diagnosis>> {
        // Repository already returns Flow sorted by timestamp DESC
        return diagnosisRepository.getAllDiagnoses(userId)
    }
    
    /**
     * Get a specific diagnosis by ID.
     * 
     * @param diagnosisId The ID of the diagnosis
     * @return The diagnosis if found, null otherwise
     */
    suspend fun getDiagnosisById(diagnosisId: String): Diagnosis? {
        return diagnosisRepository.getDiagnosisById(diagnosisId)
    }
}
