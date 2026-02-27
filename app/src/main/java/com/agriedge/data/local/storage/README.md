# Storage Layer

This package contains storage managers for secure data persistence and image management in the AgriEdge-Link application.

## Components

### EncryptedStorageManager

Manages encrypted storage for sensitive data using Android Keystore and EncryptedSharedPreferences.

**Features:**
- Hardware-backed encryption using Android Keystore
- AES-256-GCM encryption for all stored data
- Secure storage for authentication tokens (JWT)
- Database encryption key generation and management
- Generic secure key-value storage

**Usage:**

```kotlin
@Inject
lateinit var encryptedStorage: EncryptedStorageManager

// Save authentication tokens
encryptedStorage.saveAccessToken("jwt_token_here")
encryptedStorage.saveRefreshToken("refresh_token_here")

// Retrieve tokens
val accessToken = encryptedStorage.getAccessToken()
val refreshToken = encryptedStorage.getRefreshToken()

// Clear tokens on logout
encryptedStorage.clearTokens()

// Database encryption
val passphrase = encryptedStorage.getDatabasePassphrase()

// Generic storage
encryptedStorage.saveString("api_key", "secret_key")
val apiKey = encryptedStorage.getString("api_key")
```

**Security Features:**
- Keys stored in Android Keystore (hardware-backed on supported devices)
- AES-256-SIV for key encryption
- AES-256-GCM for value encryption
- Device-specific encryption keys
- No plaintext data on disk

**Requirements:** 43.1, 43.2, 43.3

---

### ImageStorageManager

Manages local image storage for diagnosis photos with compression and thumbnail generation.

**Features:**
- Automatic image compression to reduce storage footprint
- Thumbnail generation (256x256) for list views
- Cache management with configurable size limits (500MB default)
- Automatic cleanup of old images
- EXIF orientation handling
- Coroutine-based async operations

**Usage:**

```kotlin
@Inject
lateinit var imageStorage: ImageStorageManager

// Save image with compression and thumbnail generation
val bitmap: Bitmap = capturedImage
val result = imageStorage.saveImage(bitmap)

result.onSuccess { storageResult ->
    val imageId = storageResult.imageId
    val fullImagePath = storageResult.fullImagePath
    val thumbnailPath = storageResult.thumbnailPath
    val sizeBytes = storageResult.sizeBytes
}

// Load full image
val fullImage = imageStorage.loadImage(imageId)

// Load thumbnail
val thumbnail = imageStorage.loadImage(imageId, loadThumbnail = true)

// Delete image
val deleted = imageStorage.deleteImage(imageId)

// Storage management
val totalSize = imageStorage.getTotalStorageSize()
imageStorage.clearCache()
imageStorage.deleteOldImages(daysOld = 365)
```

**Image Processing:**
- Full images: Max 1024x1024, JPEG quality 85%
- Thumbnails: Max 256x256, JPEG quality 75%
- Automatic EXIF orientation correction
- Efficient memory management with bitmap recycling

**Cache Management:**
- Maximum cache size: 500 MB
- Automatic cleanup when limit exceeded
- Oldest images deleted first (FIFO)
- Separate storage for full images and thumbnails

**Requirements:** 5.1, 42.1

---

## Dependency Injection

Both storage managers are provided as singletons through Hilt:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    @Provides
    @Singleton
    fun provideEncryptedStorageManager(
        @ApplicationContext context: Context
    ): EncryptedStorageManager
    
    @Provides
    @Singleton
    fun provideImageStorageManager(
        @ApplicationContext context: Context
    ): ImageStorageManager
}
```

## Testing

Unit tests are provided for both managers:
- `EncryptedStorageManagerTest.kt`: Tests encryption and secure storage
- `ImageStorageManagerTest.kt`: Tests image compression, thumbnails, and cache management

Run tests:
```bash
./gradlew test --tests "com.agriedge.data.local.storage.*"
```

## File Structure

```
storage/
├── EncryptedStorageManager.kt    # Secure data storage
├── ImageStorageManager.kt         # Image persistence
└── README.md                      # This file
```

## Storage Locations

### EncryptedStorageManager
- Location: `EncryptedSharedPreferences` (internal app storage)
- Encryption: AES-256-GCM
- Backed by: Android Keystore

### ImageStorageManager
- Full images: `{app_files_dir}/diagnosis_images/`
- Thumbnails: `{app_files_dir}/diagnosis_thumbnails/`
- Cache: `{app_cache_dir}/image_cache/`

## Performance Considerations

### EncryptedStorageManager
- Encryption/decryption happens on-demand
- Hardware acceleration on supported devices
- Minimal performance impact for small data

### ImageStorageManager
- All operations are async (coroutines)
- Image compression reduces storage by ~70-80%
- Thumbnail generation adds ~50ms per image
- Cache cleanup runs automatically when needed

## Security Best Practices

1. **Never store sensitive data in plain text**
   - Use `EncryptedStorageManager` for all sensitive data
   - Tokens, passwords, API keys must be encrypted

2. **Database encryption**
   - Use `getDatabasePassphrase()` for Room database encryption
   - Passphrase is generated once and stored securely

3. **Key rotation**
   - Keys are device-specific and cannot be extracted
   - On device reset, all encrypted data is lost (by design)

4. **Backup considerations**
   - Encrypted data cannot be restored on different devices
   - User must re-authenticate after device migration

## Troubleshooting

### EncryptedStorageManager Issues

**Problem:** `KeyPermanentlyInvalidatedException`
- **Cause:** Device lock screen settings changed
- **Solution:** Clear app data and re-authenticate

**Problem:** `KeyStoreException`
- **Cause:** Android Keystore unavailable
- **Solution:** Check device compatibility (API 26+)

### ImageStorageManager Issues

**Problem:** Out of memory errors
- **Cause:** Loading large images
- **Solution:** Always use `loadThumbnail = true` for list views

**Problem:** Storage full
- **Cause:** Cache limit exceeded
- **Solution:** Call `deleteOldImages()` or `clearCache()`

## Future Enhancements

- [ ] Cloud backup integration for images
- [ ] Image deduplication
- [ ] Progressive image loading
- [ ] WebP format support for better compression
- [ ] Biometric authentication for encrypted storage
