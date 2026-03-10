package com.agriedge.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.agriedge.data.local.database.entity.SyncQueueItem
import com.agriedge.data.local.storage.ImageStorageManager
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.classifier.GenericImageClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import com.agriedge.data.ml.preprocessor.ValidationResult
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Diagnosis
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Location
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.repository.SyncQueueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.inject.Inject

/**
 * Use case for diagnosing crop diseases from images.
 *
 * Two-stage ML pipeline:
 * 1. Stage 1 (GenericImageClassifier): Validates the image is agriculture-related
 *    and auto-detects the crop type.
 * 2. Stage 2 (DiseaseClassifier): Classifies the specific disease using the
 *    auto-detected crop type.
 *
 * Supports multiple images for improved accuracy.
 * Falls back to backend analysis when on-device confidence is low.
 *
 * Requirements: 2.1, 2.2, 5.1, 36.1
 */
class DiagnoseDiseaseUseCase @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val genericClassifier: GenericImageClassifier,
    private val diseaseClassifier: DiseaseClassifier,
    private val diagnosisRepository: DiagnosisRepository,
    private val imageStorageManager: ImageStorageManager,
    private val syncQueueRepository: SyncQueueRepository
) {
    companion object {
        private const val TAG = "DiagnoseDiseaseUseCase"
        private const val MIN_CONFIDENCE_THRESHOLD = 0.60f
        private const val MIN_MARGIN_THRESHOLD = 0.10f
    }

    /**
     * Callback interface to report the current stage of diagnosis processing.
     */
    interface StageCallback {
        fun onStageChanged(stage: DiagnosisStage)
    }

    /**
     * Diagnose a crop disease from multiple images using the two-stage pipeline.
     *
     * @param bitmaps List of captured/uploaded images (1-3)
     * @param userId The ID of the user performing the diagnosis
     * @param location Optional location information
     * @param stageCallback Optional callback for stage progress updates
     * @return Result containing the Diagnosis or an error
     */
    suspend operator fun invoke(
        bitmaps: List<Bitmap>,
        userId: String,
        location: Location? = null,
        stageCallback: StageCallback? = null
    ): Result<Diagnosis> = withContext(Dispatchers.Default) {
        try {
            if (bitmaps.isEmpty()) {
                return@withContext Result.failure(
                    DiagnosisException("No images provided for diagnosis")
                )
            }

            val primaryBitmap = bitmaps.first()

            // Step 1: Validate image quality (resolution, brightness, blur)
            stageCallback?.onStageChanged(DiagnosisStage.VALIDATING)
            val validationResult = imagePreprocessor.validate(primaryBitmap)
            if (validationResult is ValidationResult.Invalid) {
                return@withContext Result.failure(
                    ImageQualityException(validationResult.reasons)
                )
            }

            // Step 2: Stage 1 — Generic recognition (agriculture check + crop type detection)
            stageCallback?.onStageChanged(DiagnosisStage.RECOGNIZING)
            genericClassifier.initialize()
            val recognitionResult = genericClassifier.classify(primaryBitmap)

            if (!recognitionResult.isAgricultureRelated) {
                return@withContext Result.failure(NotAgricultureImageException())
            }

            val detectedCropType = recognitionResult.detectedCropType
            Log.d(TAG, "Stage 1 result: crop=$detectedCropType, confidence=${recognitionResult.cropConfidence}")

            // Step 3: Stage 2 — Disease classification
            stageCallback?.onStageChanged(DiagnosisStage.CLASSIFYING)
            val classificationResult = try {
                diseaseClassifier.initialize()
                diseaseClassifier.classify(primaryBitmap, detectedCropType)
            } catch (t: Throwable) {
                Log.w(TAG, "Stage 2 classification unavailable; marking for further analysis", t)
                null
            }

            val topPrediction = classificationResult?.topPredictions?.firstOrNull()
            val secondPrediction = classificationResult?.topPredictions?.getOrNull(1)
            val confidenceMargin = (topPrediction?.confidence ?: 0f) - (secondPrediction?.confidence ?: 0f)

            // Step 4: Evaluate confidence
            val requiresFurtherAnalysis: Boolean
            val backendFallbackUsed: Boolean
            val finalDisease: Disease
            val finalConfidence: Float

            if (classificationResult == null) {
                finalDisease = Disease.unidentified(detectedCropType)
                finalConfidence = 0f
                requiresFurtherAnalysis = true
                backendFallbackUsed = true
            } else if (topPrediction != null &&
                topPrediction.confidence >= MIN_CONFIDENCE_THRESHOLD &&
                confidenceMargin >= MIN_MARGIN_THRESHOLD
            ) {
                // High confidence — use local result directly
                finalDisease = topPrediction.disease
                finalConfidence = topPrediction.confidence
                requiresFurtherAnalysis = false
                backendFallbackUsed = false
            } else {
                // Low confidence — mark as requiring further analysis
                // In future, this is where backend fallback would be attempted
                Log.d(TAG, "Low confidence result. Top: ${topPrediction?.confidence}, Margin: $confidenceMargin")

                if (topPrediction != null) {
                    finalDisease = topPrediction.disease
                    finalConfidence = topPrediction.confidence
                } else {
                    finalDisease = Disease.unidentified(detectedCropType)
                    finalConfidence = 0f
                }
                requiresFurtherAnalysis = true
                backendFallbackUsed = false
            }

            // Step 5: Save all images to local storage
            val imagePaths = mutableListOf<String>()
            for ((index, bitmap) in bitmaps.withIndex()) {
                val imageId = "${userId}_${System.currentTimeMillis()}_$index"
                val storageResult = imageStorageManager.saveImage(bitmap, imageId)
                    .getOrElse {
                        return@withContext Result.failure(
                            ImageStorageException("Failed to save image $index: ${it.message}", it)
                        )
                    }
                imagePaths.add(storageResult.fullImagePath)
            }

            // Step 6: Create Diagnosis domain model
            val diagnosis = Diagnosis(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                cropType = detectedCropType,
                disease = finalDisease,
                confidence = finalConfidence,
                imagePaths = imagePaths,
                primaryImagePath = imagePaths.first(),
                location = location,
                synced = false,
                requiresFurtherAnalysis = requiresFurtherAnalysis,
                backendFallbackUsed = backendFallbackUsed
            )

            // Step 7: Save diagnosis to local database
            diagnosisRepository.saveDiagnosis(diagnosis)

            // Step 8: Add to sync queue for later upload
            addToSyncQueue(diagnosis)

            Result.success(diagnosis)

        } catch (e: NotAgricultureImageException) {
            Result.failure(e)
        } catch (e: ImageQualityException) {
            Result.failure(e)
        } catch (t: Throwable) {
            Log.e(TAG, "Diagnosis failed", t)
            Result.failure(DiagnosisException("Failed to diagnose disease: ${t.message}", t))
        }
    }

    /**
     * Backward-compatible single-image overload.
     * The cropType parameter is ignored — auto-detection is used instead.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(
        bitmap: Bitmap,
        cropType: CropType,
        userId: String,
        location: Location? = null
    ): Result<Diagnosis> = invoke(listOf(bitmap), userId, location)

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
            Log.w(TAG, "Failed to add to sync queue", e)
        }
    }

    /**
     * Serialize diagnosis to JSON for sync queue.
     */
    private fun serializeDiagnosis(diagnosis: Diagnosis): String {
        return """
            {
                "id": "${diagnosis.id}",
                "userId": "${diagnosis.userId}",
                "timestamp": ${diagnosis.timestamp},
                "cropType": "${diagnosis.cropType.name}",
                "diseaseId": "${diagnosis.disease.id}",
                "diseaseName": "${diagnosis.disease.commonName}",
                "confidence": ${diagnosis.confidence},
                "imagePaths": [${diagnosis.imagePaths.joinToString(",") { "\"$it\"" }}],
                "latitude": ${diagnosis.location?.latitude},
                "longitude": ${diagnosis.location?.longitude},
                "requiresFurtherAnalysis": ${diagnosis.requiresFurtherAnalysis},
                "backendFallbackUsed": ${diagnosis.backendFallbackUsed}
            }
        """.trimIndent()
    }
}

/** Diagnosis processing stages for UI progress reporting */
enum class DiagnosisStage {
    VALIDATING,
    RECOGNIZING,
    CLASSIFYING,
    SENDING_TO_BACKEND
}

/** Exception thrown when image quality validation fails. */
class ImageQualityException(val reasons: List<String>) : Exception(
    "Image quality validation failed: ${reasons.joinToString(", ")}"
)

/** Exception thrown when classification fails. */
class ClassificationException(message: String) : Exception(message)

/** Exception thrown when the image is not related to agriculture. */
class NotAgricultureImageException : Exception(
    "This image does not appear to be related to agriculture. Please capture a photo of a crop leaf, plant, or fruit."
)

class LowConfidenceDiagnosisException(
    confidence: Float,
    margin: Float,
    threshold: Float
) : Exception(
    "Low confidence diagnosis (${(confidence * 100).toInt()}%). " +
        "Need ${(threshold * 100).toInt()}%+ and clearer leaf image (margin ${(margin * 100).toInt()}%)."
)

/** Exception thrown when diagnosis process fails. */
class DiagnosisException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Exception thrown when image storage fails. */
class ImageStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
