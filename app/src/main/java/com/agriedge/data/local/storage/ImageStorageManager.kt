package com.agriedge.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages local image storage for diagnosis photos with compression and thumbnail generation.
 * 
 * Features:
 * - Image compression to reduce storage footprint
 * - Thumbnail generation for list views
 * - Cache management with configurable size limits
 * - Automatic cleanup of old images
 * - EXIF orientation handling
 * 
 * Requirements: 5.1, 42.1
 */
@Singleton
class ImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val imagesDir: File by lazy {
        File(context.filesDir, IMAGES_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val thumbnailsDir: File by lazy {
        File(context.filesDir, THUMBNAILS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Saves an image with compression and generates a thumbnail.
     * 
     * @param bitmap The image to save
     * @param imageId Optional custom ID, generates UUID if not provided
     * @return ImageStorageResult containing paths to full image and thumbnail
     */
    suspend fun saveImage(
        bitmap: Bitmap,
        imageId: String = UUID.randomUUID().toString()
    ): Result<ImageStorageResult> = withContext(Dispatchers.IO) {
        try {
            // Save full-size compressed image
            val fullImageFile = File(imagesDir, "$imageId.jpg")
            val compressedBitmap = compressImage(bitmap, MAX_IMAGE_SIZE, FULL_IMAGE_QUALITY)
            saveBitmapToFile(compressedBitmap, fullImageFile, FULL_IMAGE_QUALITY)
            
            // Generate and save thumbnail
            val thumbnailFile = File(thumbnailsDir, "$imageId.jpg")
            val thumbnail = createThumbnail(bitmap, THUMBNAIL_SIZE)
            saveBitmapToFile(thumbnail, thumbnailFile, THUMBNAIL_QUALITY)
            
            // Clean up bitmaps
            if (compressedBitmap != bitmap) {
                compressedBitmap.recycle()
            }
            thumbnail.recycle()
            
            // Check and enforce cache size limit
            enforceCacheSizeLimit()
            
            Result.success(
                ImageStorageResult(
                    imageId = imageId,
                    fullImagePath = fullImageFile.absolutePath,
                    thumbnailPath = thumbnailFile.absolutePath,
                    sizeBytes = fullImageFile.length() + thumbnailFile.length()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Loads an image from storage.
     * 
     * @param imageId The ID of the image to load
     * @param loadThumbnail If true, loads thumbnail instead of full image
     * @return Bitmap or null if not found
     */
    suspend fun loadImage(
        imageId: String,
        loadThumbnail: Boolean = false
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = if (loadThumbnail) {
                File(thumbnailsDir, "$imageId.jpg")
            } else {
                File(imagesDir, "$imageId.jpg")
            }
            
            if (!file.exists()) return@withContext null
            
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            
            // Handle EXIF orientation
            bitmap?.let { handleExifOrientation(it, file.absolutePath) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deletes an image and its thumbnail.
     * 
     * @param imageId The ID of the image to delete
     * @return true if deletion was successful
     */
    suspend fun deleteImage(imageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val imageFile = File(imagesDir, "$imageId.jpg")
            val thumbnailFile = File(thumbnailsDir, "$imageId.jpg")
            
            val imageDeleted = if (imageFile.exists()) imageFile.delete() else true
            val thumbnailDeleted = if (thumbnailFile.exists()) thumbnailFile.delete() else true
            
            imageDeleted && thumbnailDeleted
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets the total size of stored images in bytes.
     */
    suspend fun getTotalStorageSize(): Long = withContext(Dispatchers.IO) {
        val imagesSize = imagesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val thumbnailsSize = thumbnailsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        imagesSize + thumbnailsSize
    }
    
    /**
     * Clears all cached images (not the main storage).
     */
    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Deletes images older than the specified number of days.
     * 
     * @param daysOld Images older than this will be deleted
     * @return Number of images deleted
     */
    suspend fun deleteOldImages(daysOld: Int = 365): Int = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        imagesDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                val imageId = file.nameWithoutExtension
                if (deleteImage(imageId)) {
                    deletedCount++
                }
            }
        }
        
        deletedCount
    }
    
    // Private helper methods
    
    private fun compressImage(bitmap: Bitmap, maxSize: Int, quality: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // If image is already smaller than max size, return original
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        // Calculate scale factor
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun createThumbnail(bitmap: Bitmap, size: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calculate scale to fit within square thumbnail
        val scale = minOf(
            size.toFloat() / width,
            size.toFloat() / height
        )
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.flush()
        }
    }
    
    private fun handleExifOrientation(bitmap: Bitmap, filePath: String): Bitmap {
        try {
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            return bitmap
        }
    }
    
    private fun enforceCacheSizeLimit() {
        val totalSize = getTotalStorageSizeSync()
        
        if (totalSize > MAX_CACHE_SIZE_BYTES) {
            // Delete oldest images until under limit
            val allImages = imagesDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            var currentSize = totalSize
            
            for (file in allImages) {
                if (currentSize <= MAX_CACHE_SIZE_BYTES) break
                
                val imageId = file.nameWithoutExtension
                val imageFile = File(imagesDir, "$imageId.jpg")
                val thumbnailFile = File(thumbnailsDir, "$imageId.jpg")
                
                val deletedSize = imageFile.length() + thumbnailFile.length()
                imageFile.delete()
                thumbnailFile.delete()
                
                currentSize -= deletedSize
            }
        }
    }
    
    private fun getTotalStorageSizeSync(): Long {
        val imagesSize = imagesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val thumbnailsSize = thumbnailsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return imagesSize + thumbnailsSize
    }
    
    companion object {
        private const val IMAGES_DIR = "diagnosis_images"
        private const val THUMBNAILS_DIR = "diagnosis_thumbnails"
        private const val CACHE_DIR = "image_cache"
        
        // Image quality settings
        private const val FULL_IMAGE_QUALITY = 85 // JPEG quality 0-100
        private const val THUMBNAIL_QUALITY = 75
        
        // Size limits
        private const val MAX_IMAGE_SIZE = 1024 // Max dimension in pixels
        private const val THUMBNAIL_SIZE = 256 // Thumbnail dimension in pixels
        private const val MAX_CACHE_SIZE_BYTES = 500L * 1024 * 1024 // 500 MB
    }
}

/**
 * Result of image storage operation.
 */
data class ImageStorageResult(
    val imageId: String,
    val fullImagePath: String,
    val thumbnailPath: String,
    val sizeBytes: Long
)
