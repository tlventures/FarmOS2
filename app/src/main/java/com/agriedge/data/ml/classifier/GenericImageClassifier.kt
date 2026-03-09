package com.agriedge.data.ml.classifier

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.agriedge.domain.model.CropType
import com.agriedge.domain.model.GenericLabel
import com.agriedge.domain.model.ImageRecognitionResult
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stage 1 classifier: Generic image recognition using EfficientNet-Lite2.
 *
 * Determines whether an image is agriculture-related (leaf, crop, plant, fruit, vegetable)
 * and attempts to auto-detect the crop type.
 *
 * Uses EfficientNet-Lite2 int8 quantized (260×260, 77.5% top-1 ImageNet accuracy).
 * Upgrade from MobileNet V2 1.0 (71.8%) — same pipeline, higher accuracy.
 * If the model file is not available, falls back to heuristic-based detection
 * using green-pixel HSV analysis.
 */
@Singleton
class GenericImageClassifier @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var isInitialized = false
    private var labels: List<String> = emptyList()
    private var agricultureLabelIndices: Set<Int> = emptySet()
    private var genericPlantLabelIndices: Set<Int> = emptySet()
    private var cropTypeMappings: Map<CropType, Set<Int>> = emptyMap()

    companion object {
        private const val TAG = "GenericImageClassifier"
        private const val MODEL_PATH = "models/efficientnet_lite2_int8.tflite"
        private const val LABELS_PATH = "models/efficientnet_labels.txt"
        private const val MAPPING_PATH = "models/agriculture_label_mapping.json"
        private const val INPUT_SIZE = 260  // EfficientNet-Lite2 native input size
        private const val PIXEL_SIZE = 3    // RGB
        private const val NUM_THREADS = 4
        private const val AGRICULTURE_THRESHOLD = 0.10f  // Tuned for EfficientNet's sharper predictions
        private const val CROP_TYPE_THRESHOLD = 0.06f
        private const val TOP_K = 10
    }

    /**
     * Initialize the classifier by loading model and label mappings.
     * Safe to call multiple times — will skip if already initialized.
     */
    suspend fun initialize() {
        if (isInitialized) return
        try {
            // Load labels
            labels = loadLabels()
            Log.d(TAG, "Loaded ${labels.size} labels")

            // Load agriculture label mapping
            loadAgricultureMapping()

            // Load TFLite model
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(NUM_THREADS)
            }
            interpreter = Interpreter(modelBuffer, options)
            isInitialized = true
            Log.d(TAG, "GenericImageClassifier initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize GenericImageClassifier; using heuristic fallback", t)
            // Mark as initialized even on failure — we'll use fallback
            isInitialized = true
        }
    }

    /**
     * Classify whether an image is agriculture-related and detect crop type.
     *
     * @return ImageRecognitionResult with agriculture relevance and auto-detected crop type
     */
    suspend fun classify(bitmap: Bitmap): ImageRecognitionResult {
        if (!isInitialized) initialize()

        // If model failed to load, use heuristic fallback
        if (interpreter == null) {
            return heuristicClassify(bitmap)
        }

        val startTime = System.currentTimeMillis()

        // Preprocess image
        val inputBuffer = preprocessImage(bitmap)

        // Dynamically size the output buffer from the model's own output tensor
        // (EfficientNet-Lite outputs 1000 classes; MobileNet V2 outputs 1001 with background)
        val outputSize = interpreter!!.getOutputTensor(0).shape().last()
        val outputArray = Array(1) { ByteArray(outputSize) }
        interpreter!!.run(inputBuffer, outputArray)

        val inferenceTime = System.currentTimeMillis() - startTime

        // Parse results
        return parseOutput(outputArray[0], inferenceTime)
    }

    /**
     * Preprocess bitmap for EfficientNet-Lite2 (int8 quantized, 260×260, uint8 0-255 input).
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaledBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            byteBuffer.put(((pixel shr 16) and 0xFF).toByte()) // R
            byteBuffer.put(((pixel shr 8) and 0xFF).toByte())  // G
            byteBuffer.put((pixel and 0xFF).toByte())           // B
        }

        if (scaledBitmap != bitmap) scaledBitmap.recycle()
        return byteBuffer
    }

    /**
     * Parse quantized model output (uint8) into ImageRecognitionResult.
     */
    private fun parseOutput(output: ByteArray, inferenceTime: Long): ImageRecognitionResult {
        // Convert quantized output (0-255) to probabilities (0-1)
        val probabilities = FloatArray(output.size) { (output[it].toInt() and 0xFF) / 255f }

        // Get top K labels
        val topLabels = probabilities
            .mapIndexed { index, confidence -> index to confidence }
            .sortedByDescending { it.second }
            .take(TOP_K)
            .map { (index, confidence) ->
                GenericLabel(
                    name = labels.getOrElse(index) { "unknown_$index" },
                    confidence = confidence
                )
            }

        // Sum probabilities for agriculture-related labels
        val agriProbSum = agricultureLabelIndices.sumOf { idx ->
            probabilities.getOrElse(idx) { 0f }.toDouble()
        }.toFloat()

        // Sum probabilities for generic plant labels
        val plantProbSum = genericPlantLabelIndices.sumOf { idx ->
            probabilities.getOrElse(idx) { 0f }.toDouble()
        }.toFloat()

        val isAgricultureRelated = agriProbSum >= AGRICULTURE_THRESHOLD || plantProbSum >= AGRICULTURE_THRESHOLD

        // Detect crop type
        var detectedCropType = CropType.UNKNOWN
        var cropConfidence = 0f

        if (isAgricultureRelated) {
            for ((cropType, labelIndices) in cropTypeMappings) {
                val cropProb = labelIndices.sumOf { idx ->
                    probabilities.getOrElse(idx) { 0f }.toDouble()
                }.toFloat()
                if (cropProb > cropConfidence && cropProb >= CROP_TYPE_THRESHOLD) {
                    cropConfidence = cropProb
                    detectedCropType = cropType
                }
            }
        }

        Log.d(TAG, "Agriculture prob sum: $agriProbSum, Plant prob sum: $plantProbSum, " +
                "isAgri: $isAgricultureRelated, crop: $detectedCropType ($cropConfidence)")

        return ImageRecognitionResult(
            isAgricultureRelated = isAgricultureRelated,
            detectedCropType = detectedCropType,
            cropConfidence = cropConfidence,
            topLabels = topLabels,
            inferenceTime = inferenceTime
        )
    }

    /**
     * Heuristic fallback when TFLite model is not available.
     * Uses green-pixel analysis to detect plant/leaf content.
     */
    private fun heuristicClassify(bitmap: Bitmap): ImageRecognitionResult {
        val startTime = System.currentTimeMillis()

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var greenPixels = 0
        val sampleStep = 4 // Sample every 4th pixel for speed
        var sampledCount = 0

        for (i in pixels.indices step sampleStep) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // HSV-based green detection
            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val saturation = if (max > 0) (max - min).toFloat() / max else 0f
            val value = max / 255f

            // Check if pixel is green-ish (plant/leaf)
            if (g > r && g > b && saturation >= 0.15f && value >= 0.15f) {
                greenPixels++
            }
            sampledCount++
        }

        val greenRatio = greenPixels.toFloat() / sampledCount
        val isAgricultureRelated = greenRatio >= 0.15f // 15% green pixels = likely a plant

        val inferenceTime = System.currentTimeMillis() - startTime

        return ImageRecognitionResult(
            isAgricultureRelated = isAgricultureRelated,
            detectedCropType = CropType.UNKNOWN,
            cropConfidence = 0f,
            topLabels = listOf(
                GenericLabel("heuristic_green_ratio", greenRatio)
            ),
            inferenceTime = inferenceTime
        )
    }

    private fun loadLabels(): List<String> {
        return try {
            context.assets.open(LABELS_PATH).bufferedReader().useLines { lines ->
                lines.toList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Labels file not found at $LABELS_PATH, using empty labels")
            emptyList()
        }
    }

    private fun loadAgricultureMapping() {
        try {
            val jsonStr = context.assets.open(MAPPING_PATH).bufferedReader().readText()
            val json = JSONObject(jsonStr)

            // Build agriculture label indices
            val agriLabels = mutableSetOf<String>()
            json.getJSONArray("agriculture_labels").let { arr ->
                for (i in 0 until arr.length()) agriLabels.add(arr.getString(i).lowercase())
            }
            agricultureLabelIndices = labels.indices.filter { idx ->
                agriLabels.any { agriLabel ->
                    labels[idx].lowercase().contains(agriLabel)
                }
            }.toSet()

            // Build generic plant label indices
            val plantLabels = mutableSetOf<String>()
            json.getJSONArray("generic_plant_labels").let { arr ->
                for (i in 0 until arr.length()) plantLabels.add(arr.getString(i).lowercase())
            }
            genericPlantLabelIndices = labels.indices.filter { idx ->
                plantLabels.any { plantLabel ->
                    labels[idx].lowercase().contains(plantLabel)
                }
            }.toSet()

            // Build crop type mappings
            val cropMappings = mutableMapOf<CropType, Set<Int>>()
            json.getJSONObject("crop_type_mapping").let { obj ->
                for (key in obj.keys()) {
                    val cropType = CropType.fromString(key) ?: continue
                    val cropLabels = mutableSetOf<String>()
                    obj.getJSONArray(key).let { arr ->
                        for (i in 0 until arr.length()) cropLabels.add(arr.getString(i).lowercase())
                    }
                    cropMappings[cropType] = labels.indices.filter { idx ->
                        cropLabels.any { cropLabel ->
                            labels[idx].lowercase().contains(cropLabel)
                        }
                    }.toSet()
                }
            }
            cropTypeMappings = cropMappings

            Log.d(TAG, "Loaded agriculture mapping: ${agricultureLabelIndices.size} agri labels, " +
                    "${genericPlantLabelIndices.size} plant labels, ${cropTypeMappings.size} crop types")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load agriculture mapping", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        isInitialized = false
    }
}
