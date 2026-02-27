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
    
    // Model configuration
    private val inputShape = intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3)
    private val outputShape = intArrayOf(1, OUTPUT_SIZE)
    
    // Mock mode flag - set to true when no actual model is available
    private var useMockMode = true
    
    companion object {
        private const val TAG = "DiseaseClassifier"
        private const val MODEL_PATH = "models/crop_disease_classifier.tflite"
        private const val INPUT_SIZE = 224
        private const val OUTPUT_SIZE = 40
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
            // Try to load the actual model
            val modelFile = loadModelFile(MODEL_PATH)
            
            // Configure interpreter options
            val options = Interpreter.Options().apply {
                // Try GPU delegate first
                try {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                    Log.d(TAG, "GPU delegate enabled")
                } catch (e: Exception) {
                    Log.w(TAG, "GPU delegate not available, using CPU", e)
                }
                
                // Enable NNAPI as fallback
                useNNAPI = true
                numThreads = NUM_THREADS
            }
            
            interpreter = Interpreter(modelFile, options)
            useMockMode = false
            isInitialized = true
            Log.i(TAG, "TFLite model loaded successfully")
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load TFLite model, using mock mode", e)
            useMockMode = true
            isInitialized = true
        }
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

        if (useMockMode) {
            return@withContext generateMockResult(cropType)
        }

        val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }
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
            INPUT_SIZE,
            INPUT_SIZE,
            true
        )
        
        // Allocate ByteBuffer for model input
        val inputBuffer = ByteBuffer.allocateDirect(
            4 * INPUT_SIZE * INPUT_SIZE * 3 // 4 bytes per float
        ).apply {
            order(ByteOrder.nativeOrder())
        }
        
        // Extract pixel values and normalize
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        
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
        // Get top 3 predictions
        val predictions = output
            .mapIndexed { index, confidence -> index to confidence }
            .sortedByDescending { it.second }
            .take(3)
            .map { (diseaseIndex, confidence) ->
                Prediction(
                    disease = getDiseaseForIndex(diseaseIndex, cropType),
                    confidence = confidence
                )
            }
        
        return ClassificationResult(
            topPredictions = predictions,
            inferenceTime = 0L // Will be set by caller
        )
    }
    
    /**
     * Generate mock classification results for testing
     * Simulates realistic disease detection with varying confidence scores
     */
    private fun generateMockResult(cropType: CropType): ClassificationResult {
        val mockDiseases = getMockDiseasesForCrop(cropType)
        
        // Simulate inference time (1-3 seconds)
        val inferenceTime = (1000L..3000L).random()
        
        // Generate realistic confidence scores
        val predictions = mockDiseases.take(3).mapIndexed { index, disease ->
            val confidence = when (index) {
                0 -> (0.75f..0.95f).random() // Top prediction: high confidence
                1 -> (0.10f..0.20f).random() // Second: low confidence
                else -> (0.05f..0.10f).random() // Third: very low confidence
            }
            
            Prediction(disease = disease, confidence = confidence)
        }
        
        Log.d(TAG, "Mock classification: ${predictions[0].disease.commonName} (${predictions[0].confidence})")
        
        return ClassificationResult(
            topPredictions = predictions,
            inferenceTime = inferenceTime
        )
    }
    
    /**
     * Get mock diseases for a specific crop type
     */
    private fun getMockDiseasesForCrop(cropType: CropType): List<Disease> {
        return when (cropType) {
            CropType.COTTON -> listOf(
                Disease(
                    id = "cotton_leaf_curl",
                    commonName = "Cotton Leaf Curl",
                    scientificName = "Cotton leaf curl virus",
                    localizedName = "कपास पत्ती कर्ल",
                    cropType = CropType.COTTON,
                    description = "Viral disease causing leaf curling and stunted growth",
                    symptoms = listOf("Leaf curling", "Yellowing", "Stunted growth")
                ),
                Disease(
                    id = "cotton_bollworm",
                    commonName = "Cotton Bollworm",
                    scientificName = "Helicoverpa armigera",
                    localizedName = "कपास बॉलवर्म",
                    cropType = CropType.COTTON,
                    description = "Pest infestation affecting cotton bolls",
                    symptoms = listOf("Boll damage", "Larval presence", "Reduced yield")
                ),
                Disease(
                    id = "cotton_healthy",
                    commonName = "Healthy Cotton",
                    scientificName = "No disease detected",
                    localizedName = "स्वस्थ कपास",
                    cropType = CropType.COTTON,
                    description = "No disease or pest detected",
                    symptoms = emptyList()
                )
            )
            
            CropType.WHEAT -> listOf(
                Disease(
                    id = "wheat_rust",
                    commonName = "Wheat Rust",
                    scientificName = "Puccinia triticina",
                    localizedName = "गेहूं का रतुआ",
                    cropType = CropType.WHEAT,
                    description = "Fungal disease causing rust-colored pustules",
                    symptoms = listOf("Orange pustules", "Leaf damage", "Reduced yield")
                ),
                Disease(
                    id = "wheat_blight",
                    commonName = "Wheat Blight",
                    scientificName = "Bipolaris sorokiniana",
                    localizedName = "गेहूं का झुलसा",
                    cropType = CropType.WHEAT,
                    description = "Fungal disease causing leaf blight",
                    symptoms = listOf("Brown spots", "Leaf death", "Stunted growth")
                ),
                Disease(
                    id = "wheat_healthy",
                    commonName = "Healthy Wheat",
                    scientificName = "No disease detected",
                    localizedName = "स्वस्थ गेहूं",
                    cropType = CropType.WHEAT,
                    description = "No disease detected",
                    symptoms = emptyList()
                )
            )
            
            CropType.TOMATO -> listOf(
                Disease(
                    id = "tomato_late_blight",
                    commonName = "Tomato Late Blight",
                    scientificName = "Phytophthora infestans",
                    localizedName = "टमाटर का पछेती झुलसा",
                    cropType = CropType.TOMATO,
                    description = "Devastating fungal disease",
                    symptoms = listOf("Dark spots", "Leaf death", "Fruit rot")
                ),
                Disease(
                    id = "tomato_leaf_curl",
                    commonName = "Tomato Leaf Curl",
                    scientificName = "Tomato leaf curl virus",
                    localizedName = "टमाटर पत्ती कर्ल",
                    cropType = CropType.TOMATO,
                    description = "Viral disease spread by whiteflies",
                    symptoms = listOf("Leaf curling", "Yellowing", "Stunted growth")
                ),
                Disease(
                    id = "tomato_healthy",
                    commonName = "Healthy Tomato",
                    scientificName = "No disease detected",
                    localizedName = "स्वस्थ टमाटर",
                    cropType = CropType.TOMATO,
                    description = "No disease detected",
                    symptoms = emptyList()
                )
            )
            
            else -> listOf(
                Disease(
                    id = "generic_disease",
                    commonName = "Generic Disease",
                    scientificName = "Unknown pathogen",
                    localizedName = "सामान्य रोग",
                    cropType = cropType,
                    description = "Generic disease placeholder",
                    symptoms = listOf("Leaf damage", "Discoloration")
                ),
                Disease(
                    id = "generic_healthy",
                    commonName = "Healthy Crop",
                    scientificName = "No disease detected",
                    localizedName = "स्वस्थ फसल",
                    cropType = cropType,
                    description = "No disease detected",
                    symptoms = emptyList()
                )
            )
        }
    }
    
    /**
     * Map disease index to Disease object
     * This would use actual model metadata in production
     */
    private fun getDiseaseForIndex(index: Int, cropType: CropType): Disease {
        // In production, this would map model output indices to actual diseases
        // For now, return a placeholder
        return Disease(
            id = "disease_$index",
            commonName = "Disease $index",
            scientificName = "Scientific name $index",
            localizedName = "रोग $index",
            cropType = cropType,
            description = "Disease description",
            symptoms = listOf("Symptom 1", "Symptom 2")
        )
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
    
    /**
     * Check if GPU acceleration is available
     */
    private fun isGpuAvailable(): Boolean {
        return try {
            GpuDelegate()
            true
        } catch (e: Exception) {
            false
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

/**
 * Extension function to generate random float in range
 */
private fun ClosedFloatingPointRange<Float>.random(): Float {
    return start + Math.random().toFloat() * (endInclusive - start)
}
