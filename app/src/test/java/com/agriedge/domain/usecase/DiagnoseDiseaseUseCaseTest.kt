package com.agriedge.domain.usecase

import android.graphics.Bitmap
import com.agriedge.data.local.database.entity.SyncQueueItem
import com.agriedge.data.local.storage.ImageStorageManager
import com.agriedge.data.local.storage.ImageStorageResult
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import com.agriedge.data.ml.preprocessor.ValidationResult
import com.agriedge.domain.model.ClassificationResult
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Location
import com.agriedge.domain.model.Prediction
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.repository.SyncQueueRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for DiagnoseDiseaseUseCase.
 * 
 * Tests the complete diagnosis flow:
 * - Image quality validation
 * - Disease classification
 * - Diagnosis saving
 * - Sync queue operations
 */
class DiagnoseDiseaseUseCaseTest {
    
    private lateinit var imagePreprocessor: ImagePreprocessor
    private lateinit var diseaseClassifier: DiseaseClassifier
    private lateinit var diagnosisRepository: DiagnosisRepository
    private lateinit var imageStorageManager: ImageStorageManager
    private lateinit var syncQueueRepository: SyncQueueRepository
    private lateinit var useCase: DiagnoseDiseaseUseCase
    
    private lateinit var mockBitmap: Bitmap
    
    @BeforeEach
    fun setup() {
        imagePreprocessor = mockk()
        diseaseClassifier = mockk()
        diagnosisRepository = mockk()
        imageStorageManager = mockk()
        syncQueueRepository = mockk()
        
        useCase = DiagnoseDiseaseUseCase(
            imagePreprocessor = imagePreprocessor,
            diseaseClassifier = diseaseClassifier,
            diagnosisRepository = diagnosisRepository,
            imageStorageManager = imageStorageManager,
            syncQueueRepository = syncQueueRepository
        )
        
        mockBitmap = mockk(relaxed = true)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `invoke should return success when image is valid and classification succeeds`() = runTest {
        // Given
        val userId = "user123"
        val cropType = CropType.COTTON
        val location = Location("Village", "District", "State", 21.0, 74.5)
        val imagePath = "/storage/images/test.jpg"
        
        val mockDisease = Disease(
            id = "cotton_leaf_curl",
            commonName = "Cotton Leaf Curl",
            scientificName = "Cotton leaf curl virus",
            localizedName = "कपास पत्ती कर्ल",
            cropType = CropType.COTTON,
            description = "Viral disease",
            symptoms = listOf("Leaf curling")
        )
        
        val mockPrediction = Prediction(mockDisease, 0.92f)
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(mockPrediction),
            inferenceTime = 2000L
        )
        
        // Mock behaviors
        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, userId) } returns Result.success(
            ImageStorageResult(
                imageId = userId,
                fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg",
                sizeBytes = 1024L
            )
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } just Runs
        
        // When
        val result = useCase(mockBitmap, cropType, userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        val diagnosis = result.getOrNull()
        assertNotNull(diagnosis)
        assertEquals(userId, diagnosis?.userId)
        assertEquals(cropType, diagnosis?.cropType)
        assertEquals(mockDisease, diagnosis?.disease)
        assertEquals(0.92f, diagnosis?.confidence)
        assertEquals(imagePath, diagnosis?.imagePath)
        assertEquals(location, diagnosis?.location)
        assertFalse(diagnosis?.synced ?: true)
        
        // Verify interactions
        verify { imagePreprocessor.validate(mockBitmap) }
        coVerify { diseaseClassifier.classify(mockBitmap, cropType) }
        coVerify { imageStorageManager.saveImage(mockBitmap, userId) }
        coVerify { diagnosisRepository.saveDiagnosis(any()) }
        coVerify { syncQueueRepository.addToQueue(any()) }
    }
    
    @Test
    fun `invoke should return failure when image quality validation fails`() = runTest {
        // Given
        val userId = "user123"
        val cropType = CropType.WHEAT
        val reasons = listOf("Image too dark", "Image is blurry")
        
        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Invalid(reasons)
        
        // When
        val result = useCase(mockBitmap, cropType, userId, null)
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ImageQualityException)
        assertEquals("Image quality validation failed: Image too dark, Image is blurry", exception?.message)
        
        // Verify only validation was called
        verify { imagePreprocessor.validate(mockBitmap) }
        coVerify(exactly = 0) { diseaseClassifier.classify(any(), any()) }
        coVerify(exactly = 0) { diagnosisRepository.saveDiagnosis(any()) }
    }
    
    @Test
    fun `invoke should return failure when classification returns no predictions`() = runTest {
        // Given
        val userId = "user123"
        val cropType = CropType.TOMATO
        val emptyResult = ClassificationResult(
            topPredictions = emptyList(),
            inferenceTime = 1500L
        )
        
        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns emptyResult
        
        // When
        val result = useCase(mockBitmap, cropType, userId, null)
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ClassificationException)
        assertEquals("No predictions returned from classifier", exception?.message)
        
        // Verify classification was attempted
        verify { imagePreprocessor.validate(mockBitmap) }
        coVerify { diseaseClassifier.classify(mockBitmap, cropType) }
        coVerify(exactly = 0) { diagnosisRepository.saveDiagnosis(any()) }
    }
    
    @Test
    fun `invoke should handle sync queue errors gracefully`() = runTest {
        // Given
        val userId = "user123"
        val cropType = CropType.RICE
        val imagePath = "/storage/images/test.jpg"
        
        val mockDisease = Disease(
            id = "rice_blast",
            commonName = "Rice Blast",
            scientificName = "Magnaporthe oryzae",
            localizedName = "धान का झुलसा",
            cropType = CropType.RICE,
            description = "Fungal disease",
            symptoms = listOf("Leaf spots")
        )
        
        val mockPrediction = Prediction(mockDisease, 0.85f)
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(mockPrediction),
            inferenceTime = 1800L
        )
        
        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, userId) } returns Result.success(
            ImageStorageResult(
                imageId = userId,
                fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg",
                sizeBytes = 1024L
            )
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } throws Exception("Sync queue error")
        
        // When
        val result = useCase(mockBitmap, cropType, userId, null)
        
        // Then - should still succeed even if sync queue fails
        assertTrue(result.isSuccess)
        val diagnosis = result.getOrNull()
        assertNotNull(diagnosis)
        
        // Verify diagnosis was saved despite sync queue error
        coVerify { diagnosisRepository.saveDiagnosis(any()) }
        coVerify { syncQueueRepository.addToQueue(any()) }
    }
    
    @Test
    fun `invoke should work without location`() = runTest {
        // Given
        val userId = "user123"
        val cropType = CropType.POTATO
        val imagePath = "/storage/images/test.jpg"
        
        val mockDisease = Disease(
            id = "potato_blight",
            commonName = "Potato Late Blight",
            scientificName = "Phytophthora infestans",
            localizedName = "आलू का झुलसा",
            cropType = CropType.POTATO,
            description = "Fungal disease",
            symptoms = listOf("Dark spots")
        )
        
        val mockPrediction = Prediction(mockDisease, 0.88f)
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(mockPrediction),
            inferenceTime = 2200L
        )
        
        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, userId) } returns Result.success(
            ImageStorageResult(
                imageId = userId,
                fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg",
                sizeBytes = 1024L
            )
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } just Runs
        
        // When
        val result = useCase(mockBitmap, cropType, userId, null)
        
        // Then
        assertTrue(result.isSuccess)
        val diagnosis = result.getOrNull()
        assertNotNull(diagnosis)
        assertNull(diagnosis?.location)
    }
}
