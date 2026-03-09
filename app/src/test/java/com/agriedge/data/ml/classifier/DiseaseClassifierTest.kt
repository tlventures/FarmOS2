package com.agriedge.data.ml.classifier

import android.content.Context
import android.graphics.Bitmap
import com.agriedge.domain.model.CropType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for DiseaseClassifier
 * 
 * Tests the mock mode functionality since we don't have a real model yet
 */
class DiseaseClassifierTest {
    
    private lateinit var context: Context
    private lateinit var classifier: DiseaseClassifier
    
    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        classifier = DiseaseClassifier(context)
    }
    
    @AfterEach
    fun tearDown() {
        classifier.close()
    }
    
    @Test
    fun `initialize should complete successfully in mock mode`() = runTest {
        // When
        classifier.initialize()
        
        // Then - no exception thrown
        assertTrue(true)
    }
    
    @Test
    fun `classify should return result with top 3 predictions`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        assertNotNull(result)
        assertEquals(3, result.topPredictions.size)
        assertTrue(result.inferenceTime > 0)
    }
    
    @Test
    fun `classify should return predictions with valid confidence scores`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        result.topPredictions.forEach { prediction ->
            assertTrue(prediction.confidence in 0f..1f, 
                "Confidence ${prediction.confidence} should be between 0 and 1")
        }
    }
    
    @Test
    fun `classify should return highest confidence for top prediction`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        val topConfidence = result.topPredictions[0].confidence
        val secondConfidence = result.topPredictions[1].confidence
        assertTrue(topConfidence > secondConfidence,
            "Top prediction confidence should be higher than second")
    }
    
    @Test
    fun `classify should return crop-specific diseases for cotton`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        result.topPredictions.forEach { prediction ->
            assertEquals(CropType.COTTON, prediction.disease.cropType)
        }
    }
    
    @Test
    fun `classify should return crop-specific diseases for wheat`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.WHEAT)
        
        // Then
        result.topPredictions.forEach { prediction ->
            assertEquals(CropType.WHEAT, prediction.disease.cropType)
        }
    }
    
    @Test
    fun `classify should return crop-specific diseases for tomato`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.TOMATO)
        
        // Then
        result.topPredictions.forEach { prediction ->
            assertEquals(CropType.TOMATO, prediction.disease.cropType)
        }
    }
    
    @Test
    fun `classify should simulate realistic inference time`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        assertTrue(result.inferenceTime in 1000L..3000L,
            "Inference time ${result.inferenceTime}ms should be between 1-3 seconds")
    }
    
    @Test
    fun `classify should include localized disease names`() = runTest {
        // Given
        classifier.initialize()
        val bitmap = createMockBitmap()
        
        // When
        val result = classifier.classify(bitmap, CropType.COTTON)
        
        // Then
        result.topPredictions.forEach { prediction ->
            assertNotNull(prediction.disease.localizedName)
            assertTrue(prediction.disease.localizedName.isNotEmpty())
        }
    }
    
    @Test
    fun `classify should throw exception if not initialized`() = runTest {
        // Given
        val bitmap = createMockBitmap()
        
        // When/Then
        try {
            classifier.classify(bitmap, CropType.COTTON)
            throw AssertionError("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("not initialized") == true)
        }
    }
    
    @Test
    fun `close should release resources`() = runTest {
        // Given
        classifier.initialize()
        
        // When
        classifier.close()
        
        // Then - no exception thrown
        assertTrue(true)
    }
    
    private fun createMockBitmap(): Bitmap {
        return Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
    }
}
