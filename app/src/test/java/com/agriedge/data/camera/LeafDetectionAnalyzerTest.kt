package com.agriedge.data.camera

import android.graphics.Bitmap
import android.graphics.Color
import androidx.camera.core.ImageProxy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.*

@DisplayName("LeafDetectionAnalyzer Tests")
class LeafDetectionAnalyzerTest {
    
    private lateinit var guidanceMessages: MutableList<GuidanceMessage>
    private lateinit var analyzer: LeafDetectionAnalyzer
    
    @BeforeEach
    fun setup() {
        guidanceMessages = mutableListOf()
        analyzer = LeafDetectionAnalyzer { message ->
            guidanceMessages.add(message)
        }
    }
    
    @Test
    @DisplayName("Should create analyzer with callback")
    fun testAnalyzerCreation() {
        assertNotNull(analyzer)
    }
    
    @Test
    @DisplayName("Should invoke callback when analyzing image")
    fun testCallbackInvocation() {
        val imageProxy = mock<ImageProxy>()
        val bitmap = createMockBitmap(1024, 1024, hasGreen = true)
        
        // Mock toBitmap extension
        whenever(imageProxy.toBitmap()).thenReturn(bitmap)
        
        // Analyze image
        analyzer.analyze(imageProxy)
        
        // Verify callback was invoked
        assertTrue(guidanceMessages.isNotEmpty())
        verify(imageProxy).close()
    }
    
    @Test
    @DisplayName("Should detect when no leaf is present")
    fun testNoLeafDetection() {
        val message = GuidanceMessage(
            type = GuidanceType.NO_LEAF_DETECTED,
            message = "No leaf detected",
            confidence = 0f
        )
        
        assertEquals(GuidanceType.NO_LEAF_DETECTED, message.type)
        assertTrue(message.message.contains("leaf", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should provide move closer guidance")
    fun testMoveCloserGuidance() {
        val message = GuidanceMessage(
            type = GuidanceType.MOVE_CLOSER,
            message = "Move closer",
            confidence = 0.6f
        )
        
        assertEquals(GuidanceType.MOVE_CLOSER, message.type)
        assertTrue(message.message.contains("closer", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should provide center leaf guidance")
    fun testCenterLeafGuidance() {
        val message = GuidanceMessage(
            type = GuidanceType.CENTER_LEAF,
            message = "Center the leaf",
            confidence = 0.7f
        )
        
        assertEquals(GuidanceType.CENTER_LEAF, message.type)
        assertTrue(message.message.contains("center", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should provide improve lighting guidance")
    fun testImproveLightingGuidance() {
        val message = GuidanceMessage(
            type = GuidanceType.IMPROVE_LIGHTING,
            message = "Improve lighting",
            confidence = 0.5f
        )
        
        assertEquals(GuidanceType.IMPROVE_LIGHTING, message.type)
        assertTrue(message.message.contains("light", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should provide ready to capture guidance")
    fun testReadyToCaptureGuidance() {
        val message = GuidanceMessage(
            type = GuidanceType.READY_TO_CAPTURE,
            message = "Ready to capture",
            confidence = 1.0f
        )
        
        assertEquals(GuidanceType.READY_TO_CAPTURE, message.type)
        assertTrue(message.message.contains("ready", ignoreCase = true))
        assertEquals(1.0f, message.confidence)
    }
    
    @Test
    @DisplayName("Should have confidence values between 0 and 1")
    fun testConfidenceRange() {
        val messages = listOf(
            GuidanceMessage(GuidanceType.NO_LEAF_DETECTED, "Test", 0f),
            GuidanceMessage(GuidanceType.MOVE_CLOSER, "Test", 0.6f),
            GuidanceMessage(GuidanceType.CENTER_LEAF, "Test", 0.7f),
            GuidanceMessage(GuidanceType.IMPROVE_LIGHTING, "Test", 0.5f),
            GuidanceMessage(GuidanceType.READY_TO_CAPTURE, "Test", 1.0f)
        )
        
        messages.forEach { message ->
            assertTrue(message.confidence >= 0f)
            assertTrue(message.confidence <= 1.0f)
        }
    }
    
    @Test
    @DisplayName("Should close image proxy after analysis")
    fun testImageProxyClosing() {
        val imageProxy = mock<ImageProxy>()
        val bitmap = createMockBitmap(1024, 1024, hasGreen = false)
        
        whenever(imageProxy.toBitmap()).thenReturn(bitmap)
        
        analyzer.analyze(imageProxy)
        
        verify(imageProxy).close()
    }
    
    /**
     * Helper function to create a mock bitmap.
     */
    private fun createMockBitmap(width: Int, height: Int, hasGreen: Boolean): Bitmap {
        val bitmap = mock<Bitmap>()
        whenever(bitmap.width).thenReturn(width)
        whenever(bitmap.height).thenReturn(height)
        
        whenever(bitmap.getPixel(any(), any())).thenAnswer {
            if (hasGreen) {
                Color.rgb(0, 128, 0)  // Green
            } else {
                Color.rgb(128, 128, 128)  // Gray
            }
        }
        
        return bitmap
    }
}
