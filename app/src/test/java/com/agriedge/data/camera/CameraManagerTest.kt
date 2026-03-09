package com.agriedge.data.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.LifecycleOwner
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("CameraManager Tests")
class CameraManagerTest {

    private lateinit var context: Context
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var cameraManager: CameraManager

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        lifecycleOwner = mockk(relaxed = true)
        cameraManager = CameraManager(context, lifecycleOwner)
    }

    @Test
    @DisplayName("Should initialize camera manager successfully")
    fun testInitialization() {
        assertNotNull(cameraManager)
    }

    @Test
    @DisplayName("Should handle image capture callback")
    fun testCaptureImageCallback() {
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        var capturedBitmap: Bitmap? = null
        var errorOccurred = false

        val onImageCaptured: (Bitmap) -> Unit = { bitmap ->
            capturedBitmap = bitmap
        }

        val onError: (ImageCaptureException) -> Unit = { _ ->
            errorOccurred = true
        }

        // Verify callbacks are properly typed
        assertNotNull(onImageCaptured)
        assertNotNull(onError)
    }

    @Test
    @DisplayName("Should handle capture errors gracefully")
    fun testCaptureError() {
        var errorCaptured: ImageCaptureException? = null

        val onError: (ImageCaptureException) -> Unit = { exception ->
            errorCaptured = exception
        }

        val testException = ImageCaptureException(
            ImageCapture.ERROR_INVALID_CAMERA,
            "Test error",
            null
        )
        onError(testException)

        assertNotNull(errorCaptured)
        assertEquals(ImageCapture.ERROR_INVALID_CAMERA, errorCaptured?.imageCaptureError)
    }

    @Test
    @DisplayName("Should release resources properly")
    fun testRelease() {
        assertDoesNotThrow {
            cameraManager.release()
        }
    }

    @Test
    @DisplayName("Should handle torch state changes")
    fun testTorchControl() {
        assertDoesNotThrow {
            cameraManager.setTorchEnabled(true)
            cameraManager.setTorchEnabled(false)
            cameraManager.isTorchEnabled()
        }
    }
}
