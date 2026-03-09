package com.agriedge.data.ml.classifier

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.agriedge.domain.model.ClassificationResult
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.Disease
import com.agriedge.domain.model.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * TensorFlow Lite classifier for crop disease detection
 * 
 * This class handles:
 * - Loading TFLite model from assets
 * - Configuring interpreter with GPU delegate and NNAPI
 * - Image preprocessing (resize to 224x224, normalize)
 * - Parsing model output to ClassificationResult
 * 
 * Requirements: 2.1, 2.2, 2.4
 */
@Singleton
class DiseaseClassifier @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var isInitialized = false
    private var outputSize = 0
    private var inputWidth = 224
    private var inputHeight = 224
    private var labelMappings: List<DiseaseLabel> = emptyList()
    
    companion object {
        private const val TAG = "DiseaseClassifier"
        private const val MODEL_PATH = "models/crop_disease_classifier.tflite"
        private const val LABELS_PATH = "models/crop_disease_labels.json"
        private const val NUM_THREADS = 4
        
        // Normalization constants (ImageNet standard)
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
    }
    
    /**
     * Initialize the TFLite interpreter with GPU acceleration
     * Falls back to CPU if GPU is unavailable
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "Classifier already initialized")
            return@withContext
        }
        
        try {
            // Validate artifacts first. This gives deterministic diagnostics before loading.
            val validation = validateModelArtifacts()
            if (!validation.isReady) {
                throw IllegalStateException(validation.issues.joinToString("; "))
            }

            // Try to load the actual model
            val modelFile = loadModelFile(MODEL_PATH)
            labelMappings = loadDiseaseLabels()
            if (labelMappings.isEmpty()) {
                throw IllegalStateException("Label mapping is empty at $LABELS_PATH")
            }
            
            // Configure interpreter options
            val options = Interpreter.Options().apply {
                // Try GPU delegate first
                try {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                    Log.d(TAG, "GPU delegate enabled")
                } catch (t: Throwable) {
                    // Some devices/runtime combos throw NoClassDefFoundError here.
                    gpuDelegate = null
                    Log.w(TAG, "GPU delegate not available, using CPU/NNAPI", t)
                }
                
                // Enable NNAPI as fallback
                useNNAPI = true
                numThreads = NUM_THREADS
            }
            
            interpreter = Interpreter(modelFile, options)
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val width = inputShape?.getOrNull(2) ?: 0
            val height = inputShape?.getOrNull(1) ?: 0
            if (width <= 0 || height <= 0) {
                throw IllegalStateException("Invalid model input tensor shape: ${inputShape?.contentToString()}")
            }
            inputWidth = width
            inputHeight = height
            outputSize = interpreter?.getOutputTensor(0)?.shape()?.lastOrNull() ?: 0
            if (outputSize <= 0) {
                throw IllegalStateException("Invalid model output tensor shape")
            }
            if (labelMappings.size < outputSize) {
                throw IllegalStateException(
                    "Label mapping count (${labelMappings.size}) is smaller than model output classes ($outputSize)"
                )
            }
            Log.i(
                TAG,
                "TFLite model loaded successfully with input=${inputWidth}x$inputHeight outputSize=$outputSize labels=${labelMappings.size}"
            )
            isInitialized = true
            
        } catch (e: Exception) {
            isInitialized = false
            Log.e(TAG, "Failed to load production TFLite model", e)
            throw e
        }
    }

    data class ModelValidationResult(
        val isReady: Boolean,
        val issues: List<String>
    )

    /**
     * Validation hook for release checks.
     * Ensures model + label files are available before enabling production inference.
     */
    fun validateModelArtifacts(): ModelValidationResult {
        val issues = mutableListOf<String>()

        try {
            context.assets.openFd(MODEL_PATH).close()
        } catch (e: Exception) {
            issues.add("Missing model asset at $MODEL_PATH")
        }

        try {
            context.assets.open(LABELS_PATH).close()
        } catch (e: Exception) {
            issues.add("Missing label mapping asset at $LABELS_PATH")
        }

        return ModelValidationResult(
            isReady = issues.isEmpty(),
            issues = issues
        )
    }
    
    /**
     * Classify a crop disease from an image
     * 
     * @param bitmap Input image
     * @param cropType Type of crop being diagnosed
     * @return ClassificationResult with top predictions and inference time
     */
    suspend fun classify(
        bitmap: Bitmap,
        cropType: CropType
    ): ClassificationResult = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            throw IllegalStateException("Classifier not initialized. Call initialize() first.")
        }
        if (outputSize <= 0) {
            throw IllegalStateException("Invalid classifier output size")
        }
        val outputBuffer = Array(1) { FloatArray(outputSize) }
        lateinit var result: ClassificationResult

        val inferenceTime = measureTimeMillis {
            val inputBuffer = preprocessImage(bitmap)
            interpreter?.run(inputBuffer, outputBuffer)
            result = parseOutput(outputBuffer[0], cropType)
        }

        Log.d(TAG, "Inference completed in ${inferenceTime}ms")

        return@withContext result.copy(inferenceTime = inferenceTime)
    }
    
    /**
     * Preprocess image for model input
     * - Resize to 224x224
     * - Normalize pixel values to [-1, 1]
     * - Convert to ByteBuffer
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        // Resize bitmap to model input size
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            inputWidth,
            inputHeight,
            true
        )
        
        // Allocate ByteBuffer for model input
        val inputBuffer = ByteBuffer.allocateDirect(
            4 * inputWidth * inputHeight * 3 // 4 bytes per float
        ).apply {
            order(ByteOrder.nativeOrder())
        }
        
        // Extract pixel values and normalize
        val pixels = IntArray(inputWidth * inputHeight)
        resizedBitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        
        for (pixel in pixels) {
            // Extract RGB channels
            val r = ((pixel shr 16) and 0xFF)
            val g = ((pixel shr 8) and 0xFF)
            val b = (pixel and 0xFF)
            
            // Normalize to [-1, 1] range
            inputBuffer.putFloat((r - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat((g - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat((b - IMAGE_MEAN) / IMAGE_STD)
        }
        
        return inputBuffer
    }
    
    /**
     * Parse model output to ClassificationResult
     * Extracts top 3 predictions with highest confidence
     */
    private fun parseOutput(
        output: FloatArray,
        cropType: CropType
    ): ClassificationResult {
        if (output.isEmpty()) {
            throw IllegalStateException("Model returned empty output")
        }
        val probabilities = softmax(output)
        val allPredictions = probabilities
            .mapIndexed { index, confidence -> index to confidence.coerceIn(0f, 1f) }
            .sortedByDescending { it.second }
            .map { (diseaseIndex, confidence) ->
                Prediction(
                    disease = getDiseaseForIndex(diseaseIndex, cropType),
                    confidence = confidence
                )
            }

        // If cropType is UNKNOWN, return all predictions sorted by confidence (no filtering).
        // Otherwise, filter to crop-specific predictions.
        val cropSpecificPredictions = if (cropType == CropType.UNKNOWN) {
            allPredictions.take(3)
        } else {
            allPredictions
                .filter { it.disease.cropType == cropType }
                .ifEmpty { allPredictions }
                .take(3)
        }

        return ClassificationResult(
            topPredictions = cropSpecificPredictions,
            inferenceTime = 0L // Will be set by caller
        )
    }
    
    /**
     * Map disease index to Disease object
     * This would use actual model metadata in production
     */
    private fun getDiseaseForIndex(index: Int, cropType: CropType): Disease {
        val mapped = labelMappings.getOrNull(index)
        if (mapped != null) {
            return Disease(
                id = mapped.id,
                commonName = mapped.commonName,
                scientificName = mapped.scientificName,
                localizedName = mapped.localizedName,
                cropType = CropType.fromString(mapped.cropType)
                    ?: cropType.takeIf { it != CropType.UNKNOWN }
                    ?: CropType.UNKNOWN,
                description = mapped.description,
                symptoms = mapped.symptoms
            )
        }
        throw IllegalStateException(
            "No disease label mapping for model output index $index (labels=${labelMappings.size})"
        )
    }

    private fun softmax(logits: FloatArray): FloatArray {
        if (logits.isEmpty()) return logits
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { kotlin.math.exp((it - maxLogit).toDouble()) }
        val sum = exps.sum().takeIf { it > 0.0 } ?: 1.0
        return exps.map { (it / sum).toFloat() }.toFloatArray()
    }
    
    /**
     * Load TFLite model file from assets
     */
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadDiseaseLabels(): List<DiseaseLabel> {
        return try {
            val jsonText = context.assets.open(LABELS_PATH).bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)
            val labelsArray = json.optJSONArray("labels") ?: JSONArray()
            buildList {
                for (i in 0 until labelsArray.length()) {
                    val item = labelsArray.optJSONObject(i) ?: continue
                    add(
                        DiseaseLabel(
                            id = item.optString("id", "disease_$i"),
                            commonName = item.optString("commonName", "Disease $i"),
                            scientificName = item.optString("scientificName", "Unknown"),
                            localizedName = item.optString("localizedName", item.optString("commonName", "Disease $i")),
                            cropType = item.optString("cropType", "RICE"),
                            description = item.optString("description", ""),
                            symptoms = item.optJSONArray("symptoms")?.let { array ->
                                buildList {
                                    for (j in 0 until array.length()) {
                                        add(array.optString(j))
                                    }
                                }
                            } ?: emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load disease label mapping", e)
            emptyList()
        }
    }
    
    /**
     * Release resources
     */
    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
        isInitialized = false
        Log.d(TAG, "Classifier resources released")
    }
}

private data class DiseaseLabel(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val localizedName: String,
    val cropType: String,
    val description: String,
    val symptoms: List<String>
)
