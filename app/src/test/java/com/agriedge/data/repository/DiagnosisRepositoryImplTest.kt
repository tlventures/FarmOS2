package com.agriedge.data.repository

import com.agriedge.data.local.database.dao.DiagnosisDao
import com.agriedge.data.local.database.entity.DiagnosisEntity
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Location
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for DiagnosisRepositoryImpl.
 * 
 * Tests the repository implementation:
 * - Database operations via DAO
 * - Entity to domain model mapping
 * - Domain model to entity mapping
 */
class DiagnosisRepositoryImplTest {
    
    private lateinit var diagnosisDao: DiagnosisDao
    private lateinit var repository: DiagnosisRepositoryImpl
    
    @BeforeEach
    fun setup() {
        diagnosisDao = mockk()
        repository = DiagnosisRepositoryImpl(diagnosisDao)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `getAllDiagnoses should map entities to domain models`() = runTest {
        // Given
        val userId = "user123"
        val entities = listOf(
            createMockEntity("id1", userId, CropType.COTTON),
            createMockEntity("id2", userId, CropType.WHEAT)
        )
        
        every { diagnosisDao.getAllDiagnoses(userId) } returns flowOf(entities)
        
        // When
        val result = repository.getAllDiagnoses(userId).first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("id1", result[0].id)
        assertEquals(userId, result[0].userId)
        assertEquals(CropType.COTTON, result[0].cropType)
        assertEquals("id2", result[1].id)
        assertEquals(CropType.WHEAT, result[1].cropType)
        
        verify { diagnosisDao.getAllDiagnoses(userId) }
    }
    
    @Test
    fun `getDiagnosisById should return domain model when entity exists`() = runTest {
        // Given
        val diagnosisId = "diagnosis123"
        val entity = createMockEntity(diagnosisId, "user123", CropType.TOMATO)
        
        coEvery { diagnosisDao.getDiagnosisById(diagnosisId) } returns entity
        
        // When
        val result = repository.getDiagnosisById(diagnosisId)
        
        // Then
        assertNotNull(result)
        assertEquals(diagnosisId, result?.id)
        assertEquals(CropType.TOMATO, result?.cropType)
        assertEquals("Cotton Leaf Curl", result?.disease?.commonName)
        
        coVerify { diagnosisDao.getDiagnosisById(diagnosisId) }
    }
    
    @Test
    fun `getDiagnosisById should return null when entity does not exist`() = runTest {
        // Given
        val diagnosisId = "nonexistent"
        coEvery { diagnosisDao.getDiagnosisById(diagnosisId) } returns null
        
        // When
        val result = repository.getDiagnosisById(diagnosisId)
        
        // Then
        assertNull(result)
        coVerify { diagnosisDao.getDiagnosisById(diagnosisId) }
    }
    
    @Test
    fun `saveDiagnosis should convert domain model to entity and insert`() = runTest {
        // Given
        val diagnosis = createMockDiagnosis("id1", "user123", CropType.RICE)
        
        coEvery { diagnosisDao.insert(any()) } returns 1L
        
        // When
        repository.saveDiagnosis(diagnosis)
        
        // Then
        coVerify {
            diagnosisDao.insert(match { entity ->
                entity.id == "id1" &&
                entity.userId == "user123" &&
                entity.cropType == "RICE" &&
                entity.confidence == 0.92f &&
                !entity.synced
            })
        }
    }
    
    @Test
    fun `getUnsyncedDiagnoses should return only unsynced diagnoses`() = runTest {
        // Given
        val userId = "user123"
        val unsyncedEntities = listOf(
            createMockEntity("id1", userId, CropType.COTTON, synced = false),
            createMockEntity("id2", userId, CropType.WHEAT, synced = false)
        )
        
        coEvery { diagnosisDao.getUnsyncedDiagnoses(userId) } returns unsyncedEntities
        
        // When
        val result = repository.getUnsyncedDiagnoses(userId)
        
        // Then
        assertEquals(2, result.size)
        assertFalse(result[0].synced)
        assertFalse(result[1].synced)
        
        coVerify { diagnosisDao.getUnsyncedDiagnoses(userId) }
    }
    
    @Test
    fun `markAsSynced should call DAO markAsSynced`() = runTest {
        // Given
        val diagnosisId = "diagnosis123"
        coEvery { diagnosisDao.markAsSynced(diagnosisId) } just Runs
        
        // When
        repository.markAsSynced(diagnosisId)
        
        // Then
        coVerify { diagnosisDao.markAsSynced(diagnosisId) }
    }
    
    @Test
    fun `entity mapping should handle null location fields`() = runTest {
        // Given
        val entity = DiagnosisEntity(
            id = "id1",
            userId = "user123",
            timestamp = 1000L,
            cropType = "COTTON",
            diseaseId = "cotton_leaf_curl",
            diseaseName = "Cotton Leaf Curl",
            diseaseNameLocal = "कपास पत्ती कर्ल",
            scientificName = "Cotton leaf curl virus",
            confidence = 0.85f,
            imagePath = "/storage/images/test.jpg",
            thumbnailPath = null,
            latitude = null,
            longitude = null,
            village = null,
            district = null,
            state = null,
            synced = false
        )
        
        every { diagnosisDao.getAllDiagnoses("user123") } returns flowOf(listOf(entity))
        
        // When
        val result = repository.getAllDiagnoses("user123").first()
        
        // Then
        assertEquals(1, result.size)
        assertNull(result[0].location)
    }
    
    @Test
    fun `entity mapping should handle complete location fields`() = runTest {
        // Given
        val entity = createMockEntity("id1", "user123", CropType.COTTON)
        
        every { diagnosisDao.getAllDiagnoses("user123") } returns flowOf(listOf(entity))
        
        // When
        val result = repository.getAllDiagnoses("user123").first()
        
        // Then
        assertEquals(1, result.size)
        assertNotNull(result[0].location)
        assertEquals("Village", result[0].location?.village)
        assertEquals("District", result[0].location?.district)
        assertEquals("State", result[0].location?.state)
        assertEquals(21.0, result[0].location?.latitude)
        assertEquals(74.5, result[0].location?.longitude)
    }
    
    private fun createMockEntity(
        id: String,
        userId: String,
        cropType: CropType,
        synced: Boolean = false
    ): DiagnosisEntity {
        return DiagnosisEntity(
            id = id,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            cropType = cropType.name,
            diseaseId = "cotton_leaf_curl",
            diseaseName = "Cotton Leaf Curl",
            diseaseNameLocal = "कपास पत्ती कर्ल",
            scientificName = "Cotton leaf curl virus",
            confidence = 0.92f,
            imagePath = "/storage/images/$id.jpg",
            thumbnailPath = "/storage/thumbnails/$id.jpg",
            latitude = 21.0,
            longitude = 74.5,
            village = "Village",
            district = "District",
            state = "State",
            synced = synced
        )
    }
    
    private fun createMockDiagnosis(
        id: String,
        userId: String,
        cropType: CropType
    ): Diagnosis {
        return Diagnosis(
            id = id,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            cropType = cropType,
            disease = Disease(
                id = "cotton_leaf_curl",
                commonName = "Cotton Leaf Curl",
                scientificName = "Cotton leaf curl virus",
                localizedName = "कपास पत्ती कर्ल",
                cropType = cropType,
                description = "Viral disease",
                symptoms = listOf("Leaf curling")
            ),
            confidence = 0.92f,
            imagePaths = listOf("/storage/images/$id.jpg"),
            primaryImagePath = "/storage/images/$id.jpg",
            location = Location("Village", "District", "State", 21.0, 74.5),
            synced = false
        )
    }
}
