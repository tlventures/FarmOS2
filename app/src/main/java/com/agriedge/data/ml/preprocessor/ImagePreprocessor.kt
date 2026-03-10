package com.agriedge.data.ml.preprocessor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

/**
 * Image preprocessor for validating image quality before disease classification.
 * Validates brightness, blur, resolution, and leaf presence.
 * 
 * Requirements: 1.3, 1.4
 */
class ImagePreprocessor {
    
    companion object {
        // Quality thresholds
        private const val MIN_BRIGHTNESS = 30
        private const val MAX_BRIGHTNESS = 220
        private const val MIN_BLUR_VARIANCE = 100.0
        private const val MIN_RESOLUTION = 512
        private const val MIN_GREEN_PERCENTAGE = 0.30f
        private const val MAX_BLUR_SIZE = 256 // downsample target for blur check
        
        // HSV thresholds for green detection
        private const val GREEN_HUE_MIN = 60f
        private const val GREEN_HUE_MAX = 180f
        private const val GREEN_SATURATION_MIN = 0.2f
        private const val GREEN_VALUE_MIN = 0.2f
    }
    
    /**
     * Validates image quality for disease classification.
     * 
     * @param bitmap The image to validate
     * @return ValidationResult indicating if image is valid or reasons for rejection
     */
    fun validate(bitmap: Bitmap): ValidationResult {
        // Note: checkLeafPresence removed from standard pipeline.
        // Agriculture validation is now handled by GenericImageClassifier (Stage 1).
        val checks = listOf(
            checkResolution(bitmap),
            checkBrightness(bitmap),
            checkBlur(bitmap)
        )
        
        val failedChecks = checks.filter { !it.passed }
        
        return if (failedChecks.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(failedChecks.map { it.message })
        }
    }
    
    /**
     * Checks if image resolution meets minimum requirements.
     */
    private fun checkResolution(bitmap: Bitmap): QualityCheck {
        val width = bitmap.width
        val height = bitmap.height
        val minDimension = minOf(width, height)
        
        return if (minDimension >= MIN_RESOLUTION) {
            QualityCheck(
                passed = true,
                message = "Resolution OK: ${width}x${height}"
            )
        } else {
            QualityCheck(
                passed = false,
                message = "Image resolution too low. Minimum ${MIN_RESOLUTION}x${MIN_RESOLUTION} required."
            )
        }
    }
    
    /**
     * Checks if image brightness is within acceptable range.
     * Calculates average brightness across all pixels.
     */
    private fun checkBrightness(bitmap: Bitmap): QualityCheck {
        var totalBrightness = 0L
        var pixelCount = 0
        
        // Sample pixels for performance (every 4th pixel)
        val step = 4
        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                
                // Calculate perceived brightness
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                totalBrightness += brightness
                pixelCount++
            }
        }
        
        val avgBrightness = (totalBrightness / pixelCount).toInt()
        
        return when {
            avgBrightness < MIN_BRIGHTNESS -> QualityCheck(
                passed = false,
                message = "Image too dark. Please improve lighting."
            )
            avgBrightness > MAX_BRIGHTNESS -> QualityCheck(
                passed = false,
                message = "Image too bright. Avoid direct sunlight."
            )
            else -> QualityCheck(
                passed = true,
                message = "Brightness OK: $avgBrightness"
            )
        }
    }
    
    /**
     * Checks if image is blurry using Laplacian variance method.
     * Downsamples to MAX_BLUR_SIZE before processing to avoid OOM on full-res images.
     */
    private fun checkBlur(bitmap: Bitmap): QualityCheck {
        // Downsample to at most 256×256 to avoid OOM and per-pixel JNI overhead
        val blurBitmap = if (bitmap.width > MAX_BLUR_SIZE || bitmap.height > MAX_BLUR_SIZE) {
            val scale = MAX_BLUR_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
            val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }

        val grayscale = convertToGrayscale(blurBitmap)
        if (blurBitmap !== bitmap) blurBitmap.recycle()

        val variance = calculateLaplacianVariance(grayscale)

        return if (variance >= MIN_BLUR_VARIANCE) {
            QualityCheck(
                passed = true,
                message = "Image sharpness OK: ${variance.toInt()}"
            )
        } else {
            QualityCheck(
                passed = false,
                message = "Image is blurry. Hold camera steady and focus on the leaf."
            )
        }
    }
    
    /**
     * Checks if image contains sufficient green regions (leaf presence).
     * Uses HSV color space for robust green detection.
     * Public for optional use — no longer part of the standard validate() pipeline.
     */
    fun checkLeafPresence(bitmap: Bitmap): QualityCheck {
        var greenPixels = 0
        var totalPixels = 0
        
        // Sample pixels for performance (every 4th pixel)
        val step = 4
        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                val pixel = bitmap.getPixel(x, y)
                if (isGreenPixel(pixel)) {
                    greenPixels++
                }
                totalPixels++
            }
        }
        
        val greenPercentage = greenPixels.toFloat() / totalPixels
        
        return if (greenPercentage >= MIN_GREEN_PERCENTAGE) {
            QualityCheck(
                passed = true,
                message = "Leaf detected: ${(greenPercentage * 100).toInt()}%"
            )
        } else {
            QualityCheck(
                passed = false,
                message = "No leaf detected. Please capture a clear image of the affected leaf."
            )
        }
    }
    
    /**
     * Determines if a pixel is green based on HSV thresholds.
     */
    private fun isGreenPixel(pixel: Int): Boolean {
        val r = Color.red(pixel) / 255f
        val g = Color.green(pixel) / 255f
        val b = Color.blue(pixel) / 255f
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        // Calculate HSV
        val value = max
        val saturation = if (max == 0f) 0f else delta / max
        
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6)
            max == g -> 60f * (((b - r) / delta) + 2)
            else -> 60f * (((r - g) / delta) + 4)
        }.let { if (it < 0) it + 360f else it }
        
        // Check if pixel is green
        return hue in GREEN_HUE_MIN..GREEN_HUE_MAX &&
               saturation >= GREEN_SATURATION_MIN &&
               value >= GREEN_VALUE_MIN
    }
    
    /**
     * Converts bitmap to grayscale for blur detection.
     * Uses a single batch getPixels() call to avoid per-pixel JNI overhead.
     */
    private fun convertToGrayscale(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        return Array(height) { y ->
            IntArray(width) { x ->
                val pixel = pixels[y * width + x]
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }
    }
    
    /**
     * Calculates Laplacian variance to detect blur.
     * Applies Laplacian kernel and calculates variance of result.
     */
    private fun calculateLaplacianVariance(grayscale: Array<IntArray>): Double {
        val height = grayscale.size
        val width = grayscale[0].size
        
        // Laplacian kernel
        val kernel = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, -4, 1),
            intArrayOf(0, 1, 0)
        )
        
        val laplacian = mutableListOf<Int>()
        
        // Apply Laplacian kernel (skip borders)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var sum = 0
                for (ky in 0..2) {
                    for (kx in 0..2) {
                        sum += grayscale[y + ky - 1][x + kx - 1] * kernel[ky][kx]
                    }
                }
                laplacian.add(sum)
            }
        }
        
        // Calculate variance
        val mean = laplacian.average()
        val variance = laplacian.map { (it - mean) * (it - mean) }.average()
        
        return variance
    }
}

/**
 * Result of image quality validation.
 */
sealed class ValidationResult {
    /**
     * Image passed all quality checks.
     */
    object Valid : ValidationResult()
    
    /**
     * Image failed one or more quality checks.
     * @param reasons List of specific error messages
     */
    data class Invalid(val reasons: List<String>) : ValidationResult()
}

/**
 * Result of a single quality check.
 */
data class QualityCheck(
    val passed: Boolean,
    val message: String
)
