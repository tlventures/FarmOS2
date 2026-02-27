package com.agriedge.domain.repository

import com.agriedge.domain.model.Diagnosis
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for diagnosis data operations
 * Requirements: 5.1, 8.1, 36.1
 */
interface DiagnosisRepository {
    
    /**
     * Get all diagnoses for a user as a reactive Flow
     */
    fun getAllDiagnoses(userId: String): Flow<List<Diagnosis>>
    
    /**
     * Get a specific diagnosis by ID
     */
    suspend fun getDiagnosisById(id: String): Diagnosis?
    
    /**
     * Save a new diagnosis
     */
    suspend fun saveDiagnosis(diagnosis: Diagnosis)
    
    /**
     * Get unsynced diagnoses for background sync
     */
    suspend fun getUnsyncedDiagnoses(userId: String): List<Diagnosis>
    
    /**
     * Mark a diagnosis as synced
     */
    suspend fun markAsSynced(id: String)
    
    /**
     * Delete old diagnoses (older than specified timestamp)
     */
    suspend fun deleteOldDiagnoses(cutoffTime: Long)
}
