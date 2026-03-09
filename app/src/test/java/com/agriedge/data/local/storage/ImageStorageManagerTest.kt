package com.agriedge.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Unit tests for ImageStorageManager.
 * 
 * Tests image storage, compression, thumbnail generation, and cache management.
 * 
 * Requirements: 5.1, 42.1
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageStorageManagerTest : DescribeSpec({
    
    lateinit var context: Context
    lateinit var storageManager: ImageStorageManager
    
    beforeEach {
        context = ApplicationProvider.getApplicationContext()
        storageManager = ImageStorageManager(context)
    }
    
    afterEach {
        runBlocking {
            // Clean up test files
            storageManager.clearCache()
        }
    }
    
    describe("Image Saving") {
        
        it("should save image and generate thumbnail") {
            val bitmap = createTestBitmap(800, 600)
            
            val result = runBlocking {
                storageManager.saveImage(bitmap)
            }
            
            result.isSuccess shouldBe true
            val storageResult = result.getOrNull()
            storageResult shouldNotBe null
            storageResult!!.imageId shouldNotBe null
            storageResult.fullImagePath shouldNotBe null
            storageResult.thumbnailPath shouldNotBe null
            storageResult.sizeBytes shouldBeGreaterThan 0L
            
            // Verify files exist
            File(storageResult.fullImagePath).exists() shouldBe true
            File(storageResult.thumbnailPath).exists() shouldBe true
        }
        
        it("should save image with custom ID") {
            val bitmap = createTestBitmap(400, 400)
            val customId = "test_image_123"
            
            val result = runBlocking {
                storageManager.saveImage(bitmap, customId)
            }
            
            result.isSuccess shouldBe true
            result.getOrNull()?.imageId shouldBe customId
        }
        
        it("should compress large images") {
            val largeBitmap = createTestBitmap(2048, 2048)
            
            val result = runBlocking {
                storageManager.saveImage(largeBitmap)
            }
            
            result.isSuccess shouldBe true
            val storageResult = result.getOrNull()!!
            
            // Load the saved image and verify it's compressed
            val loadedBitmap = runBlocking {
                storageManager.loadImage(storageResult.imageId)
            }
            
            loadedBitmap shouldNotBe null
            // Image should be scaled down (max dimension 1024)
            loadedBitmap!!.width shouldBeLessThan 2048
            loadedBitmap.height shouldBeLessThan 2048
        }
    }
    
    describe("Image Loading") {
        
        it("should load saved image") {
            val originalBitmap = createTestBitmap(600, 400)
            
            val saveResult = runBlocking {
                storageManager.saveImage(originalBitmap)
            }
            val imageId = saveResult.getOrNull()!!.imageId
            
            val loadedBitmap = runBlocking {
                storageManager.loadImage(imageId)
            }
            
            loadedBitmap shouldNotBe null
            loadedBitmap!!.width shouldBe originalBitmap.width
            loadedBitmap.height shouldBe originalBitmap.height
        }
        
        it("should load thumbnail") {
            val originalBitmap = createTestBitmap(800, 600)
            
            val saveResult = runBlocking {
                storageManager.saveImage(originalBitmap)
            }
            val imageId = saveResult.getOrNull()!!.imageId
            
            val thumbnail = runBlocking {
                storageManager.loadImage(imageId, loadThumbnail = true)
            }
            
            thumbnail shouldNotBe null
            // Thumbnail should be smaller than original
            thumbnail!!.width shouldBeLessThan originalBitmap.width
            thumbnail.height shouldBeLessThan originalBitmap.height
        }
        
        it("should return null for non-existent image") {
            val bitmap = runBlocking {
                storageManager.loadImage("non_existent_id")
            }
            
            bitmap shouldBe null
        }
    }
    
    describe("Image Deletion") {
        
        it("should delete image and thumbnail") {
            val bitmap = createTestBitmap(400, 400)
            
            val saveResult = runBlocking {
                storageManager.saveImage(bitmap)
            }
            val imageId = saveResult.getOrNull()!!.imageId
            
            val deleted = runBlocking {
                storageManager.deleteImage(imageId)
            }
            
            deleted shouldBe true
            
            // Verify image is gone
            val loadedBitmap = runBlocking {
                storageManager.loadImage(imageId)
            }
            loadedBitmap shouldBe null
        }
        
        it("should return true when deleting non-existent image") {
            val deleted = runBlocking {
                storageManager.deleteImage("non_existent_id")
            }
            
            deleted shouldBe true
        }
    }
    
    describe("Storage Management") {
        
        it("should calculate total storage size") {
            val bitmap1 = createTestBitmap(400, 400)
            val bitmap2 = createTestBitmap(600, 600)
            
            runBlocking {
                storageManager.saveImage(bitmap1)
                storageManager.saveImage(bitmap2)
            }
            
            val totalSize = runBlocking {
                storageManager.getTotalStorageSize()
            }
            
            totalSize shouldBeGreaterThan 0L
        }
        
        it("should clear cache") {
            val bitmap = createTestBitmap(400, 400)
            
            runBlocking {
                storageManager.saveImage(bitmap)
            }
            
            val cleared = runBlocking {
                storageManager.clearCache()
            }
            
            cleared shouldBe true
        }
        
        it("should delete old images") {
            val bitmap = createTestBitmap(400, 400)
            
            val saveResult = runBlocking {
                storageManager.saveImage(bitmap)
            }
            val imageId = saveResult.getOrNull()!!.imageId
            
            // Delete images older than 0 days (should delete all)
            val deletedCount = runBlocking {
                storageManager.deleteOldImages(daysOld = 0)
            }
            
            deletedCount shouldBeGreaterThan 0
            
            // Verify image is deleted
            val loadedBitmap = runBlocking {
                storageManager.loadImage(imageId)
            }
            loadedBitmap shouldBe null
        }
    }
    
    describe("Thumbnail Generation") {
        
        it("should generate thumbnail smaller than original") {
            val originalBitmap = createTestBitmap(1000, 800)
            
            val saveResult = runBlocking {
                storageManager.saveImage(originalBitmap)
            }
            val imageId = saveResult.getOrNull()!!.imageId
            
            val thumbnail = runBlocking {
                storageManager.loadImage(imageId, loadThumbnail = true)
            }
            
            thumbnail shouldNotBe null
            thumbnail!!.width shouldBeLessThan originalBitmap.width
            thumbnail.height shouldBeLessThan originalBitmap.height
            // Thumbnail should be around 256px (as per THUMBNAIL_SIZE constant)
            thumbnail.width shouldBeLessThan 300
            thumbnail.height shouldBeLessThan 300
        }
    }
    
    describe("Image Compression") {
        
        it("should compress images to reduce file size") {
            val largeBitmap = createTestBitmap(1500, 1500)
            
            val saveResult = runBlocking {
                storageManager.saveImage(largeBitmap)
            }
            
            saveResult.isSuccess shouldBe true
            val storageResult = saveResult.getOrNull()!!
            
            // File size should be reasonable (compressed)
            // A 1500x1500 uncompressed bitmap would be ~9MB
            // Compressed should be much smaller
            storageResult.sizeBytes shouldBeLessThan 2_000_000L // Less than 2MB
        }
    }
})

/**
 * Helper function to create a test bitmap with specified dimensions.
 */
private fun createTestBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    // Fill with a color to make it non-empty
    bitmap.eraseColor(Color.BLUE)
    return bitmap
}
