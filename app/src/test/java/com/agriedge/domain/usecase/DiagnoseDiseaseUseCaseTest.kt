package com.agriedge.domain.usecase

import android.graphics.Bitmap
import com.agriedge.data.local.storage.ImageStorageManager
import com.agriedge.data.local.storage.ImageStorageResult
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.classifier.GenericImageClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import com.agriedge.data.ml.preprocessor.ValidationResult
import com.agriedge.domain.model.ClassificationResult
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.GenericLabel
import com.agriedge.domain.model.ImageRecognitionResult
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

class DiagnoseDiseaseUseCaseTest {

    private lateinit var imagePreprocessor: ImagePreprocessor
    private lateinit var genericClassifier: GenericImageClassifier
    private lateinit var diseaseClassifier: DiseaseClassifier
    private lateinit var diagnosisRepository: DiagnosisRepository
    private lateinit var imageStorageManager: ImageStorageManager
    private lateinit var syncQueueRepository: SyncQueueRepository
    private lateinit var useCase: DiagnoseDiseaseUseCase
    private lateinit var mockBitmap: Bitmap

    private val defaultRecognitionResult = ImageRecognitionResult(
        isAgricultureRelated = true,
        detectedCropType = CropType.COTTON,
        cropConfidence = 0.85f,
        topLabels = listOf(GenericLabel("cotton plant", 0.85f)),
        inferenceTime = 120L
    )

    @BeforeEach
    fun setup() {
        imagePreprocessor = mockk()
        genericClassifier = mockk()
        diseaseClassifier = mockk()
        diagnosisRepository = mockk()
        imageStorageManager = mockk()
        syncQueueRepository = mockk()

        useCase = DiagnoseDiseaseUseCase(
            imagePreprocessor = imagePreprocessor,
            genericClassifier = genericClassifier,
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
        val userId = "user123"
        val cropType = CropType.COTTON
        val location = Location("Village", "District", "State", 21.0, 74.5)
        val imagePath = "/storage/images/test.jpg"

        val mockDisease = Disease(
            id = "cotton_leaf_curl", commonName = "Cotton Leaf Curl",
            scientificName = "Cotton leaf curl virus", localizedName = "कपास पत्ती कर्ल",
            cropType = CropType.COTTON, description = "Viral disease",
            symptoms = listOf("Leaf curling")
        )
        val mockPrediction = Prediction(mockDisease, 0.92f)
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(mockPrediction), inferenceTime = 2000L
        )

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { genericClassifier.initialize() } just Runs
        coEvery { genericClassifier.classify(mockBitmap) } returns defaultRecognitionResult.copy(detectedCropType = cropType)
        coEvery { diseaseClassifier.initialize() } just Runs
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, any()) } returns Result.success(
            ImageStorageResult(imageId = userId, fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg", sizeBytes = 1024L)
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } just Runs

        val result = useCase(mockBitmap, cropType, userId, location)

        assertTrue(result.isSuccess)
        val diagnosis = result.getOrNull()
        assertNotNull(diagnosis)
        assertEquals(userId, diagnosis?.userId)
        assertEquals(mockDisease, diagnosis?.disease)
        assertEquals(0.92f, diagnosis?.confidence)
        assertEquals(location, diagnosis?.location)
        assertFalse(diagnosis?.synced ?: true)

        verify { imagePreprocessor.validate(mockBitmap) }
        coVerify { genericClassifier.classify(mockBitmap) }
        coVerify { diseaseClassifier.classify(mockBitmap, cropType) }
        coVerify { diagnosisRepository.saveDiagnosis(any()) }
        coVerify { syncQueueRepository.addToQueue(any()) }
    }

    @Test
    fun `invoke should return failure when image quality validation fails`() = runTest {
        val userId = "user123"
        val cropType = CropType.WHEAT
        val reasons = listOf("Image too dark", "Image is blurry")

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Invalid(reasons)

        val result = useCase(mockBitmap, cropType, userId, null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ImageQualityException)
        verify { imagePreprocessor.validate(mockBitmap) }
        coVerify(exactly = 0) { genericClassifier.classify(any()) }
        coVerify(exactly = 0) { diseaseClassifier.classify(any(), any()) }
        coVerify(exactly = 0) { diagnosisRepository.saveDiagnosis(any()) }
    }

    @Test
    fun `invoke should return failure when image is not agriculture related`() = runTest {
        val userId = "user123"
        val cropType = CropType.TOMATO
        val nonAgricultureResult = ImageRecognitionResult(
            isAgricultureRelated = false, detectedCropType = CropType.UNKNOWN,
            cropConfidence = 0.05f, topLabels = listOf(GenericLabel("car", 0.90f)),
            inferenceTime = 100L
        )

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { genericClassifier.initialize() } just Runs
        coEvery { genericClassifier.classify(mockBitmap) } returns nonAgricultureResult

        val result = useCase(mockBitmap, cropType, userId, null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotAgricultureImageException)
        coVerify(exactly = 0) { diseaseClassifier.classify(any(), any()) }
    }

    @Test
    fun `invoke should return diagnosis with requiresFurtherAnalysis when confidence is low`() = runTest {
        val userId = "user123"
        val cropType = CropType.RICE
        val imagePath = "/storage/images/test.jpg"
        val mockDisease = Disease(
            id = "rice_blast", commonName = "Rice Blast",
            scientificName = "Magnaporthe oryzae", localizedName = "धान का झुलसा",
            cropType = CropType.RICE, description = "Fungal disease",
            symptoms = listOf("Leaf spots")
        )
        val lowConfResult = ClassificationResult(
            topPredictions = listOf(Prediction(mockDisease, 0.45f), Prediction(mockDisease, 0.40f)),
            inferenceTime = 1800L
        )

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { genericClassifier.initialize() } just Runs
        coEvery { genericClassifier.classify(mockBitmap) } returns defaultRecognitionResult.copy(detectedCropType = cropType)
        coEvery { diseaseClassifier.initialize() } just Runs
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns lowConfResult
        coEvery { imageStorageManager.saveImage(mockBitmap, any()) } returns Result.success(
            ImageStorageResult(imageId = userId, fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg", sizeBytes = 1024L)
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } just Runs

        val result = useCase(mockBitmap, cropType, userId, null)

        assertTrue(result.isSuccess)
        val diagnosis = result.getOrNull()
        assertNotNull(diagnosis)
        assertTrue(diagnosis?.requiresFurtherAnalysis ?: false)
    }

    @Test
    fun `invoke should handle sync queue errors gracefully`() = runTest {
        val userId = "user123"
        val cropType = CropType.RICE
        val imagePath = "/storage/images/test.jpg"
        val mockDisease = Disease(
            id = "rice_blast", commonName = "Rice Blast",
            scientificName = "Magnaporthe oryzae", localizedName = "धान का झुलसा",
            cropType = CropType.RICE, description = "Fungal disease",
            symptoms = listOf("Leaf spots")
        )
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(Prediction(mockDisease, 0.85f)), inferenceTime = 1800L
        )

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { genericClassifier.initialize() } just Runs
        coEvery { genericClassifier.classify(mockBitmap) } returns defaultRecognitionResult.copy(detectedCropType = cropType)
        coEvery { diseaseClassifier.initialize() } just Runs
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, any()) } returns Result.success(
            ImageStorageResult(imageId = userId, fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg", sizeBytes = 1024L)
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } throws Exception("Sync queue error")

        val result = useCase(mockBitmap, cropType, userId, null)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        coVerify { diagnosisRepository.saveDiagnosis(any()) }
        coVerify { syncQueueRepository.addToQueue(any()) }
    }

    @Test
    fun `invoke should work without location`() = runTest {
        val userId = "user123"
        val cropType = CropType.POTATO
        val imagePath = "/storage/images/test.jpg"
        val mockDisease = Disease(
            id = "potato_blight", commonName = "Potato Late Blight",
            scientificName = "Phytophthora infestans", localizedName = "आलू का झुलसा",
            cropType = CropType.POTATO, description = "Fungal disease",
            symptoms = listOf("Dark spots")
        )
        val mockClassificationResult = ClassificationResult(
            topPredictions = listOf(Prediction(mockDisease, 0.88f)), inferenceTime = 2200L
        )

        every { imagePreprocessor.validate(mockBitmap) } returns ValidationResult.Valid
        coEvery { genericClassifier.initialize() } just Runs
        coEvery { genericClassifier.classify(mockBitmap) } returns defaultRecognitionResult.copy(detectedCropType = cropType)
        coEvery { diseaseClassifier.initialize() } just Runs
        coEvery { diseaseClassifier.classify(mockBitmap, cropType) } returns mockClassificationResult
        coEvery { imageStorageManager.saveImage(mockBitmap, any()) } returns Result.success(
            ImageStorageResult(imageId = userId, fullImagePath = imagePath,
                thumbnailPath = "/storage/thumbnails/test.jpg", sizeBytes = 1024L)
        )
        coEvery { diagnosisRepository.saveDiagnosis(any()) } just Runs
        coEvery { syncQueueRepository.addToQueue(any()) } just Runs

        val result = useCase(mockBitmap, cropType, userId, null)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.location)
    }
}
