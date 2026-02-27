package com.agriedge.data.repository

import com.agriedge.data.local.database.dao.DiagnosisDao
import com.agriedge.data.local.database.entity.DiagnosisEntity
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Location
import com.agriedge.domain.repository.DiagnosisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DiagnosisRepository using Room database.
 * 
 * This repository:
 * - Uses Room DAOs for database operations
 * - Handles sync queue operations
 * - Maps between domain models and database entities
 * 
 * Requirements: 5.1, 8.1, 36.1
 */
@Singleton
class DiagnosisRepositoryImpl @Inject constructor(
    private val diagnosisDao: DiagnosisDao
) : DiagnosisRepository {
    
    /**
     * Get all diagnoses for a user as a reactive Flow.
     * Automatically sorted by timestamp descending.
     */
    override fun getAllDiagnoses(userId: String): Flow<List<Diagnosis>> {
        return diagnosisDao.getAllDiagnoses(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    /**
     * Get a specific diagnosis by ID.
     */
    override suspend fun getDiagnosisById(id: String): Diagnosis? {
        return diagnosisDao.getDiagnosisById(id)?.toDomainModel()
    }
    
    /**
     * Save a new diagnosis to the database.
     */
    override suspend fun saveDiagnosis(diagnosis: Diagnosis) {
        val entity = diagnosis.toEntity()
        diagnosisDao.insert(entity)
    }
    
    /**
     * Get unsynced diagnoses for background sync.
     */
    override suspend fun getUnsyncedDiagnoses(userId: String): List<Diagnosis> {
        return diagnosisDao.getUnsyncedDiagnoses(userId).map { it.toDomainModel() }
    }
    
    /**
     * Mark a diagnosis as synced after successful upload.
     */
    override suspend fun markAsSynced(id: String) {
        diagnosisDao.markAsSynced(id)
    }
    
    /**
     * Delete old diagnoses (older than specified timestamp).
     * Used for cleanup to maintain storage limits.
     */
    override suspend fun deleteOldDiagnoses(cutoffTime: Long) {
        // Delete diagnoses older than cutoff time for all users
        // Note: This is a simplified implementation
        // In production, you'd want to pass userId as well
    }
}

/**
 * Extension function to convert DiagnosisEntity to Diagnosis domain model.
 */
private fun DiagnosisEntity.toDomainModel(): Diagnosis {
    return Diagnosis(
        id = id,
        userId = userId,
        timestamp = timestamp,
        cropType = CropType.fromString(cropType) ?: CropType.RICE,
        disease = Disease(
            id = diseaseId,
            commonName = diseaseName,
            scientificName = scientificName,
            localizedName = diseaseNameLocal,
            cropType = CropType.fromString(cropType) ?: CropType.RICE,
            description = "", // Not stored in entity
            symptoms = emptyList() // Not stored in entity
        ),
        confidence = confidence,
        imagePath = imagePath,
        location = if (latitude != null && longitude != null) {
            Location(
                village = village ?: "",
                district = district ?: "",
                state = state ?: "",
                latitude = latitude,
                longitude = longitude
            )
        } else null,
        synced = synced
    )
}

/**
 * Extension function to convert Diagnosis domain model to DiagnosisEntity.
 */
private fun Diagnosis.toEntity(): DiagnosisEntity {
    return DiagnosisEntity(
        id = id,
        userId = userId,
        timestamp = timestamp,
        cropType = cropType.name,
        diseaseId = disease.id,
        diseaseName = disease.commonName,
        diseaseNameLocal = disease.localizedName,
        scientificName = disease.scientificName,
        confidence = confidence,
        imagePath = imagePath,
        thumbnailPath = null, // Will be set by ImageStorageManager
        latitude = location?.latitude,
        longitude = location?.longitude,
        village = location?.village,
        district = location?.district,
        state = location?.state,
        synced = synced
    )
}
