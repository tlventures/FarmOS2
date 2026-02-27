package com.agriedge.data.camera

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.max
import kotlin.math.min

/**
 * Real-time image analyzer for detecting leaf presence and providing guidance.
 * Analyzes camera frames to detect green regions and provide feedback to users.
 * 
 * Requirements: 1.2
 */
class LeafDetectionAnalyzer(
    private val onGuidanceUpdate: ((String?) -> Unit)? = null
) : ImageAnalysis.Analyzer {
    
    companion object {
        // Detection thresholds
        private const val MIN_LEAF_SIZE_RATIO = 0.15f  // Leaf should be at least 15% of frame
        private const val OPTIMAL_LEAF_SIZE_RATIO = 0.40f  // Optimal leaf size is 40% of frame
        private const val CENTER_TOLERANCE = 0.15f  // 15% tolerance from center
        private const val MIN_BRIGHTNESS = 40
        private const val OPTIMAL_BRIGHTNESS_MIN = 80
        private const val OPTIMAL_BRIGHTNESS_MAX = 180
        
        // HSV thresholds for green detection
        private const val GREEN_HUE_MIN = 60f
        private const val GREEN_HUE_MAX = 180f
        private const val GREEN_SATURATION_MIN = 0.2f
        private const val GREEN_VALUE_MIN = 0.2f
        
        // Analysis throttling (analyze every N frames)
        private const val ANALYSIS_INTERVAL_MS = 200L
    }
    
    private var lastAnalysisTime = 0L
    
    override fun analyze(image: ImageProxy) {
        try {
            // Throttle analysis to avoid excessive processing
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalysisTime < ANALYSIS_INTERVAL_MS) {
                image.close()
                return
            }
            lastAnalysisTime = currentTime
            
            // Convert to bitmap for analysis
            val bitmap = image.toBitmap()
            
            // Analyze the image
            val guidance = analyzeImage(bitmap)
            
            // Notify listener
            onGuidanceUpdate?.invoke(guidance.message)
            
        } finally {
            image.close()
        }
    }
    
    /**
     * Analyzes the image and generates guidance message.
     */
    private fun analyzeImage(bitmap: Bitmap): GuidanceMessage {
        val width = bitmap.width
        val height = bitmap.height
        
        // Detect green regions
        val greenRegion = detectGreenRegion(bitmap)
        
        // Check if leaf is present
        if (greenRegion == null) {
            return GuidanceMessage(
                type = GuidanceType.NO_LEAF_DETECTED,
                message = "No leaf detected. Please point camera at a leaf.",
                confidence = 0f
            )
        }
        
        // Calculate leaf size ratio
        val leafArea = greenRegion.width() * greenRegion.height()
        val frameArea = width * height
        val leafSizeRatio = leafArea.toFloat() / frameArea
        
        // Check brightness
        val brightness = calculateAverageBrightness(bitmap, greenRegion)
        
        // Generate guidance based on analysis
        return when {
            brightness < MIN_BRIGHTNESS -> GuidanceMessage(
                type = GuidanceType.IMPROVE_LIGHTING,
                message = "Too dark. Improve lighting or use flash.",
                confidence = 0.5f
            )
            
            leafSizeRatio < MIN_LEAF_SIZE_RATIO -> GuidanceMessage(
                type = GuidanceType.MOVE_CLOSER,
                message = "Move closer to the leaf.",
                confidence = 0.6f
            )
            
            !isLeafCentered(greenRegion, width, height) -> GuidanceMessage(
                type = GuidanceType.CENTER_LEAF,
                message = "Center the leaf in the frame.",
                confidence = 0.7f
            )
            
            leafSizeRatio < OPTIMAL_LEAF_SIZE_RATIO -> GuidanceMessage(
                type = GuidanceType.MOVE_CLOSER,
                message = "Move a bit closer for better detail.",
                confidence = 0.8f
            )
            
            brightness < OPTIMAL_BRIGHTNESS_MIN || brightness > OPTIMAL_BRIGHTNESS_MAX -> {
                if (brightness < OPTIMAL_BRIGHTNESS_MIN) {
                    GuidanceMessage(
                        type = GuidanceType.IMPROVE_LIGHTING,
                        message = "Lighting could be better. Try brighter area.",
                        confidence = 0.85f
                    )
                } else {
                    GuidanceMessage(
                        type = GuidanceType.IMPROVE_LIGHTING,
                        message = "Too bright. Avoid direct sunlight.",
                        confidence = 0.85f
                    )
                }
            }
            
            else -> GuidanceMessage(
                type = GuidanceType.READY_TO_CAPTURE,
                message = "Perfect! Ready to capture.",
                confidence = 1.0f
            )
        }
    }
    
    /**
     * Detects the bounding box of green regions in the image.
     * Returns null if no significant green region is found.
     */
    private fun detectGreenRegion(bitmap: Bitmap): Rect? {
        val width = bitmap.width
        val height = bitmap.height
        
        var minX = width
        var minY = height
        var maxX = 0
        var maxY = 0
        var greenPixelCount = 0
        
        // Sample pixels for performance (every 8th pixel)
        val step = 8
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val pixel = bitmap.getPixel(x, y)
                if (isGreenPixel(pixel)) {
                    greenPixelCount++
                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)
                }
            }
        }
        
        // Check if we found enough green pixels
        val totalSampledPixels = (width / step) * (height / step)
        val greenRatio = greenPixelCount.toFloat() / totalSampledPixels
        
        return if (greenRatio > 0.05f) {  // At least 5% green pixels
            Rect(minX, minY, maxX, maxY)
        } else {
            null
        }
    }
    
    /**
     * Checks if a pixel is green based on HSV thresholds.
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
     * Checks if the leaf is centered in the frame.
     */
    private fun isLeafCentered(leafRect: Rect, frameWidth: Int, frameHeight: Int): Boolean {
        val leafCenterX = leafRect.centerX()
        val leafCenterY = leafRect.centerY()
        val frameCenterX = frameWidth / 2
        val frameCenterY = frameHeight / 2
        
        val toleranceX = frameWidth * CENTER_TOLERANCE
        val toleranceY = frameHeight * CENTER_TOLERANCE
        
        val deltaX = kotlin.math.abs(leafCenterX - frameCenterX)
        val deltaY = kotlin.math.abs(leafCenterY - frameCenterY)
        
        return deltaX <= toleranceX && deltaY <= toleranceY
    }
    
    /**
     * Calculates average brightness in the specified region.
     */
    private fun calculateAverageBrightness(bitmap: Bitmap, region: Rect): Int {
        var totalBrightness = 0L
        var pixelCount = 0
        
        // Sample pixels in the region (every 4th pixel)
        val step = 4
        for (y in region.top until region.bottom step step) {
            for (x in region.left until region.right step step) {
                if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
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
        }
        
        return if (pixelCount > 0) {
            (totalBrightness / pixelCount).toInt()
        } else {
            0
        }
    }
}

/**
 * Types of guidance messages.
 */
enum class GuidanceType {
    NO_LEAF_DETECTED,
    MOVE_CLOSER,
    CENTER_LEAF,
    IMPROVE_LIGHTING,
    READY_TO_CAPTURE
}

/**
 * Guidance message for the user.
 */
data class GuidanceMessage(
    val type: GuidanceType,
    val message: String,
    val confidence: Float  // 0.0 to 1.0, higher means better conditions
)
