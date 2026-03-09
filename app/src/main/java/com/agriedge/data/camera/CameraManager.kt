package com.agriedge.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Manages camera operations using CameraX API.
 * Provides preview, image capture, and image analysis capabilities.
 * 
 * Requirements: 1.1, 1.2
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    
    /**
     * Starts the camera with preview, capture, and analysis use cases.
     * 
     * @param previewView The PreviewView to display camera preview
     * @param analyzer Optional ImageAnalysis.Analyzer for real-time analysis
     * @param onGuidanceUpdate Optional callback for guidance messages
     */
    suspend fun startCamera(
        previewView: PreviewView,
        analyzer: ImageAnalysis.Analyzer? = null,
        onGuidanceUpdate: ((String?) -> Unit)? = null
    ) = suspendCoroutine<Unit> { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Preview use case - 4:3 aspect ratio
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Image capture use case - maximize quality
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(previewView.display.rotation)
                    .build()
                
                // Image analysis use case for real-time guidance
                imageAnalyzer = if (analyzer != null || onGuidanceUpdate != null) {
                    ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            val leafAnalyzer = analyzer ?: LeafDetectionAnalyzer(onGuidanceUpdate)
                            it.setAnalyzer(ContextCompat.getMainExecutor(context), leafAnalyzer)
                        }
                } else {
                    null
                }
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Unbind all use cases before rebinding
                cameraProvider?.unbindAll()
                
                // Bind use cases to camera
                camera = if (imageAnalyzer != null) {
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )
                } else {
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }
                
                continuation.resume(Unit)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * Captures an image and returns it as a Bitmap.
     * 
     * @param onImageCaptured Callback invoked with the captured bitmap
     * @param onError Callback invoked if capture fails
     */
    fun captureImage(
        onImageCaptured: (Bitmap) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(ImageCaptureException(
                ImageCapture.ERROR_INVALID_CAMERA,
                "Camera not initialized",
                null
            ))
            return
        }
        
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = image.toBitmap()
                        onImageCaptured(bitmap)
                    } catch (e: Exception) {
                        onError(ImageCaptureException(
                            ImageCapture.ERROR_CAPTURE_FAILED,
                            "Failed to convert image to bitmap",
                            e
                        ))
                    } finally {
                        image.close()
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
    
    /**
     * Captures an image asynchronously using coroutines.
     * 
     * @return Captured image as Bitmap
     * @throws ImageCaptureException if capture fails
     */
    suspend fun captureImageAsync(): Bitmap = suspendCoroutine { continuation ->
        captureImage(
            onImageCaptured = { bitmap ->
                continuation.resume(bitmap)
            },
            onError = { exception ->
                continuation.resumeWithException(exception)
            }
        )
    }
    
    /**
     * Updates the image analyzer for real-time analysis.
     * 
     * @param analyzer The new analyzer to use
     */
    fun setAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        imageAnalyzer?.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
    }
    
    /**
     * Clears the current image analyzer.
     */
    fun clearAnalyzer() {
        imageAnalyzer?.clearAnalyzer()
    }
    
    /**
     * Enables or disables the camera torch (flash).
     * 
     * @param enabled True to enable torch, false to disable
     */
    fun setTorchEnabled(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }
    
    /**
     * Gets the current torch state.
     * 
     * @return True if torch is enabled, false otherwise
     */
    fun isTorchEnabled(): Boolean {
        return camera?.cameraInfo?.torchState?.value == TorchState.ON
    }
    
    /**
     * Releases camera resources.
     * Should be called when camera is no longer needed.
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        imageAnalyzer = null
        camera = null
    }
}

/**
 * Extension function to convert ImageProxy to Bitmap.
 */
fun ImageProxy.toBitmap(): Bitmap {
    val decodedBitmap = when (format) {
        ImageFormat.JPEG -> {
            val jpegBuffer: ByteBuffer = planes[0].buffer
            val jpegBytes = ByteArray(jpegBuffer.remaining())
            jpegBuffer.get(jpegBytes)
            BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        }
        ImageFormat.YUV_420_888 -> {
            val nv21 = yuv420888ToNv21()
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            ByteArrayOutputStream().use { out ->
                yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
                val imageBytes = out.toByteArray()
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
        }
        else -> {
            val fallbackBuffer: ByteBuffer = planes[0].buffer
            val fallbackBytes = ByteArray(fallbackBuffer.remaining())
            fallbackBuffer.get(fallbackBytes)
            BitmapFactory.decodeByteArray(fallbackBytes, 0, fallbackBytes.size)
        }
    }

    val bitmap = requireNotNull(decodedBitmap) {
        "Failed to decode image from CameraX frame format=$format"
    }

    // Rotate bitmap if needed based on image rotation
    val rotationDegrees = imageInfo.rotationDegrees
    if (rotationDegrees != 0) {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    return bitmap
}

private fun ImageProxy.yuv420888ToNv21(): ByteArray {
    val imageWidth = width
    val imageHeight = height
    val ySize = imageWidth * imageHeight
    val uvSize = imageWidth * imageHeight / 2
    val nv21 = ByteArray(ySize + uvSize)

    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    var outputOffset = 0

    for (row in 0 until imageHeight) {
        val rowStart = row * yPlane.rowStride
        for (col in 0 until imageWidth) {
            nv21[outputOffset++] = yBuffer.get(rowStart + col * yPlane.pixelStride)
        }
    }

    val uvHeight = imageHeight / 2
    val uvWidth = imageWidth / 2
    for (row in 0 until uvHeight) {
        val uRowStart = row * uPlane.rowStride
        val vRowStart = row * vPlane.rowStride
        for (col in 0 until uvWidth) {
            val uIndex = uRowStart + col * uPlane.pixelStride
            val vIndex = vRowStart + col * vPlane.pixelStride
            nv21[outputOffset++] = vBuffer.get(vIndex)
            nv21[outputOffset++] = uBuffer.get(uIndex)
        }
    }

    return nv21
}
