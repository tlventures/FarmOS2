package com.agriedge.domain.usecase

import android.graphics.Bitmap
import com.agriedge.data.local.database.entity.SyncQueueItem
import com.agriedge.data.local.storage.ImageStorageManager
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import com.agriedge.data.ml.preprocessor.ValidationResult
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.model.Location
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.repository.SyncQueueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.inject.Inject

/**
 * Use case for diagnosing crop diseases from images.
 *
 * This use case:
 * 1. Validates image quality before classification
 * 2. Runs TFLite inference on preprocessed image
 * 3. Creates Diagnosis domain model with results
 * 4. Saves diagnosis to local database
 * 5. Adds to sync queue for later upload
 *
 * Requirements: 2.1, 2.2, 5.1, 36.1
 */
class DiagnoseDiseaseUseCase @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val diseaseClassifier: DiseaseClassifier,
    private val diagnosisRepository: DiagnosisRepository,
    private val imageStorageManager: ImageStorageManager,
    private val syncQueueRepository: SyncQueueRepository
) {

    /**
     * Diagnose a crop disease from an image.
     *
     * @param bitmap The captured image
     * @param cropType The type of crop being diagnosed
     * @param userId The ID of the user performing the diagnosis
     * @param location Optional location information
     * @return Result containing the Diagnosis or an error
     */
    suspend operator fun invoke(
        bitmap: Bitmap,
        cropType: CropType,
        userId: String,
        location: Location? = null
    ): Result<Diagnosis> = withContext(Dispatchers.Default) {
        try {
            // Step 1: Validate image quality
            val validationResult = imagePreprocessor.validate(bitmap)
            if (validationResult is ValidationResult.Invalid) {
                return@withContext Result.failure(
                    ImageQualityException(validationResult.reasons)
                )
            }

            // Step 2: Run TFLite inference
            val classificationResult = diseaseClassifier.classify(bitmap, cropType)

            // Get the top prediction
            val topPrediction = classificationResult.topPredictions.firstOrNull()
                ?: return@withContext Result.failure(
                    ClassificationException("No predictions returned from classifier")
                )

            // Step 3: Save image to local storage
            val imageStorageResult = imageStorageManager.saveImage(bitmap, userId)
                .getOrElse {
                    return@withContext Result.failure(
                        ImageStorageException("Failed to save image: ${it.message}", it)
                    )
                }
            val imagePath = imageStorageResult.fullImagePath

            // Step 4: Create Diagnosis domain model
            val diagnosis = Diagnosis(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                cropType = cropType,
                disease = topPrediction.disease,
                confidence = topPrediction.confidence,
                imagePath = imagePath,
                location = location,
                synced = false
            )

            // Step 5: Save diagnosis to local database
            diagnosisRepository.saveDiagnosis(diagnosis)

            // Step 6: Add to sync queue for later upload
            addToSyncQueue(diagnosis)

            Result.success(diagnosis)

        } catch (e: Exception) {
            Result.failure(DiagnosisException("Failed to diagnose disease: ${e.message}", e))
        }
    }

    /**
     * Add diagnosis to sync queue for background upload.
     */
    private suspend fun addToSyncQueue(diagnosis: Diagnosis) {
        try {
            val syncItem = SyncQueueItem(
                entityType = "DIAGNOSIS",
                entityId = diagnosis.id,
                operation = "CREATE",
                payload = serializeDiagnosis(diagnosis),
                timestamp = System.currentTimeMillis(),
                status = "PENDING"
            )

            syncQueueRepository.addToQueue(syncItem)
        } catch (e: Exception) {
            // Log error but don't fail the diagnosis
            // Sync will be retried later
        }
    }

    /**
     * Serialize diagnosis to JSON for sync queue.
     */
    private fun serializeDiagnosis(diagnosis: Diagnosis): String {
        // Simple JSON serialization
        // In production, use kotlinx.serialization or Moshi
        return """
            {
                "id": "${diagnosis.id}",
                "userId": "${diagnosis.userId}",
                "timestamp": ${diagnosis.timestamp},
                "cropType": "${diagnosis.cropType.name}",
                "diseaseId": "${diagnosis.disease.id}",
                "diseaseName": "${diagnosis.disease.commonName}",
                "confidence": ${diagnosis.confidence},
                "imagePath": "${diagnosis.imagePath}",
                "latitude": ${diagnosis.location?.latitude},
                "longitude": ${diagnosis.location?.longitude}
            }
        """.trimIndent()
    }
}

/**
 * Exception thrown when image quality validation fails.
 */
class ImageQualityException(val reasons: List<String>) : Exception(
    "Image quality validation failed: ${reasons.joinToString(", ")}"
)

/**
 * Exception thrown when classification fails.
 */
class ClassificationException(message: String) : Exception(message)

/**
 * Exception thrown when diagnosis process fails.
 */
class DiagnosisException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when image storage fails.
 */
class ImageStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
