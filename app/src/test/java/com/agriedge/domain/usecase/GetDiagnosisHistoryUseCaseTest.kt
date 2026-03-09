package com.agriedge.domain.usecase

import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Location
import com.agriedge.domain.repository.DiagnosisRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for GetDiagnosisHistoryUseCase.
 * 
 * Tests the diagnosis history retrieval:
 * - Fetching all diagnoses for a user
 * - Reactive Flow updates
 * - Fetching specific diagnosis by ID
 */
class GetDiagnosisHistoryUseCaseTest {
    
    private lateinit var diagnosisRepository: DiagnosisRepository
    private lateinit var useCase: GetDiagnosisHistoryUseCase
    
    @BeforeEach
    fun setup() {
        diagnosisRepository = mockk()
        useCase = GetDiagnosisHistoryUseCase(diagnosisRepository)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `invoke should return Flow of diagnoses sorted by timestamp descending`() = runTest {
        // Given
        val userId = "user123"
        val diagnoses = listOf(
            createMockDiagnosis("id1", userId, 1000L, CropType.COTTON),
            createMockDiagnosis("id2", userId, 2000L, CropType.WHEAT),
            createMockDiagnosis("id3", userId, 3000L, CropType.RICE)
        )
        
        every { diagnosisRepository.getAllDiagnoses(userId) } returns flowOf(diagnoses)
        
        // When
        val result = useCase(userId).first()
        
        // Then
        assertEquals(3, result.size)
        assertEquals("id1", result[0].id)
        assertEquals("id2", result[1].id)
        assertEquals("id3", result[2].id)
        
        // Verify repository was called
        coVerify { diagnosisRepository.getAllDiagnoses(userId) }
    }
    
    @Test
    fun `invoke should return empty Flow when no diagnoses exist`() = runTest {
        // Given
        val userId = "user123"
        every { diagnosisRepository.getAllDiagnoses(userId) } returns flowOf(emptyList())
        
        // When
        val result = useCase(userId).first()
        
        // Then
        assertTrue(result.isEmpty())
        coVerify { diagnosisRepository.getAllDiagnoses(userId) }
    }
    
    @Test
    fun `getDiagnosisById should return diagnosis when it exists`() = runTest {
        // Given
        val diagnosisId = "diagnosis123"
        val diagnosis = createMockDiagnosis(diagnosisId, "user123", 1000L, CropType.TOMATO)
        
        coEvery { diagnosisRepository.getDiagnosisById(diagnosisId) } returns diagnosis
        
        // When
        val result = useCase.getDiagnosisById(diagnosisId)
        
        // Then
        assertNotNull(result)
        assertEquals(diagnosisId, result?.id)
        assertEquals(CropType.TOMATO, result?.cropType)
        
        coVerify { diagnosisRepository.getDiagnosisById(diagnosisId) }
    }
    
    @Test
    fun `getDiagnosisById should return null when diagnosis does not exist`() = runTest {
        // Given
        val diagnosisId = "nonexistent"
        coEvery { diagnosisRepository.getDiagnosisById(diagnosisId) } returns null
        
        // When
        val result = useCase.getDiagnosisById(diagnosisId)
        
        // Then
        assertNull(result)
        coVerify { diagnosisRepository.getDiagnosisById(diagnosisId) }
    }
    
    @Test
    fun `invoke should handle multiple users correctly`() = runTest {
        // Given
        val user1 = "user1"
        val user2 = "user2"
        
        val user1Diagnoses = listOf(
            createMockDiagnosis("id1", user1, 1000L, CropType.COTTON)
        )
        val user2Diagnoses = listOf(
            createMockDiagnosis("id2", user2, 2000L, CropType.WHEAT),
            createMockDiagnosis("id3", user2, 3000L, CropType.RICE)
        )
        
        every { diagnosisRepository.getAllDiagnoses(user1) } returns flowOf(user1Diagnoses)
        every { diagnosisRepository.getAllDiagnoses(user2) } returns flowOf(user2Diagnoses)
        
        // When
        val result1 = useCase(user1).first()
        val result2 = useCase(user2).first()
        
        // Then
        assertEquals(1, result1.size)
        assertEquals(user1, result1[0].userId)
        
        assertEquals(2, result2.size)
        assertEquals(user2, result2[0].userId)
        assertEquals(user2, result2[1].userId)
    }
    
    private fun createMockDiagnosis(
        id: String,
        userId: String,
        timestamp: Long,
        cropType: CropType
    ): Diagnosis {
        return Diagnosis(
            id = id,
            userId = userId,
            timestamp = timestamp,
            cropType = cropType,
            disease = Disease(
                id = "disease_$id",
                commonName = "Test Disease",
                scientificName = "Testus diseaseus",
                localizedName = "परीक्षण रोग",
                cropType = cropType,
                description = "Test disease description",
                symptoms = listOf("Symptom 1", "Symptom 2")
            ),
            confidence = 0.85f,
            imagePaths = listOf("/storage/images/$id.jpg"),
            primaryImagePath = "/storage/images/$id.jpg",
            location = Location("Village", "District", "State", 21.0, 74.5),
            synced = false
        )
    }
}
