package com.agriedge.data.ml.preprocessor

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@DisplayName("ImagePreprocessor Tests")
class ImagePreprocessorTest {
    
    private lateinit var preprocessor: ImagePreprocessor
    
    @BeforeEach
    fun setup() {
        preprocessor = ImagePreprocessor()
    }
    
    @Test
    @DisplayName("Should pass validation for good quality image")
    fun testValidImage() {
        // Create a mock bitmap with good quality
        val bitmap = createMockBitmap(
            width = 1024,
            height = 1024,
            brightness = 128,
            hasGreen = true,
            isSharp = true
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Valid)
    }
    
    @Test
    @DisplayName("Should fail validation for low resolution image")
    fun testLowResolutionImage() {
        val bitmap = createMockBitmap(
            width = 256,
            height = 256,
            brightness = 128,
            hasGreen = true,
            isSharp = true
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("resolution", ignoreCase = true) })
    }
    
    @Test
    @DisplayName("Should fail validation for too dark image")
    fun testTooDarkImage() {
        val bitmap = createMockBitmap(
            width = 1024,
            height = 1024,
            brightness = 20,
            hasGreen = true,
            isSharp = true
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("dark", ignoreCase = true) })
    }
    
    @Test
    @DisplayName("Should fail validation for too bright image")
    fun testTooBrightImage() {
        val bitmap = createMockBitmap(
            width = 1024,
            height = 1024,
            brightness = 240,
            hasGreen = true,
            isSharp = true
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("bright", ignoreCase = true) })
    }
    
    @Test
    @DisplayName("Should fail validation for image without leaf")
    fun testNoLeafImage() {
        val bitmap = createMockBitmap(
            width = 1024,
            height = 1024,
            brightness = 128,
            hasGreen = false,
            isSharp = true
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("leaf", ignoreCase = true) })
    }
    
    @Test
    @DisplayName("Should return multiple error messages for multiple failures")
    fun testMultipleFailures() {
        val bitmap = createMockBitmap(
            width = 256,
            height = 256,
            brightness = 20,
            hasGreen = false,
            isSharp = false
        )
        
        val result = preprocessor.validate(bitmap)
        
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.size >= 2)
    }
    
    /**
     * Helper function to create a mock bitmap with specified characteristics.
     * Note: In a real Android environment, this would create actual bitmaps.
     * For unit tests, we use mocking.
     */
    private fun createMockBitmap(
        width: Int,
        height: Int,
        brightness: Int,
        hasGreen: Boolean,
        isSharp: Boolean
    ): Bitmap {
        val bitmap = mock<Bitmap>()
        whenever(bitmap.width).thenReturn(width)
        whenever(bitmap.height).thenReturn(height)
        
        // Mock pixel colors based on requirements
        whenever(bitmap.getPixel(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenAnswer {
            val x = it.getArgument<Int>(0)
            val y = it.getArgument<Int>(1)
            
            if (hasGreen) {
                // Return green pixel (RGB: 0, 128, 0)
                Color.rgb(0, 128, 0)
            } else {
                // Return non-green pixel (gray)
                Color.rgb(brightness, brightness, brightness)
            }
        }
        
        return bitmap
    }
}
