# AgriEdge-Link Database Implementation

## Overview

This package contains the Room database implementation for AgriEdge-Link with SQLCipher encryption for secure local data storage.

## Architecture

### Database Class
- **AgriEdgeDatabase**: Main Room database class with SQLCipher encryption
  - Version: 1
  - Encryption: AES-256 via SQLCipher
  - Entities: 6 (Diagnosis, UserProfile, Treatment, Transaction, ProviderRating, SyncQueue)

### Entities

1. **DiagnosisEntity**: Stores crop disease diagnosis records
   - Primary Key: `id` (UUID)
   - Includes: crop type, disease info, confidence, image paths, location
   - Requirements: 5.1, 5.2, 43.1

2. **UserProfileEntity**: Stores user profile information
   - Primary Key: `userId`
   - Includes: phone, language, location, crops, sync status
   - Requirements: 34.1, 34.2, 34.3, 43.1

3. **TreatmentEntity**: Caches treatment recommendations
   - Primary Key: `id`
   - Includes: disease ID, treatment details, localized content
   - Requirements: 4.1, 4.2, 4.3, 15.1

4. **TransactionEntity**: Stores market transactions
   - Primary Key: `id`
   - Includes: provider info, transaction details, pickup info
   - Requirements: 18.1, 18.2, 18.3, 43.1

5. **ProviderRatingEntity**: Stores provider ratings and reviews
   - Primary Key: `id` (UUID)
   - Includes: rating (1-5), review text, sync status
   - Requirements: 31.1, 31.2, 31.3, 43.1

6. **SyncQueueItem**: Manages offline data synchronization
   - Primary Key: `id` (auto-generated)
   - Includes: entity type, operation, payload, retry logic
   - Requirements: 36.1, 36.2, 36.3

### Data Access Objects (DAOs)

Each entity has a corresponding DAO with CRUD operations and reactive queries using Flow:

- **DiagnosisDao**: Diagnosis operations with history queries
- **UserProfileDao**: User profile management
- **TreatmentDao**: Treatment caching and retrieval
- **TransactionDao**: Transaction management with status updates
- **ProviderRatingDao**: Rating submission and retrieval
- **SyncQueueDao**: Sync queue management with retry logic

### Type Converters

**Converters.kt** provides type conversion for complex types:
- `List<String>` ↔ JSON
- `Map<String, String>` ↔ JSON
- `Map<Int, Int>` ↔ JSON (for rating distributions)

Uses Moshi for JSON serialization.

## Security

### Encryption

The database uses **SQLCipher** for transparent AES-256 encryption:

1. **DatabasePassphraseManager**: Manages encryption keys
   - Uses Android Keystore for secure key generation
   - Stores passphrase in EncryptedSharedPreferences
   - Device-specific encryption keys
   - Requirements: 43.1, 43.3

2. **Key Generation**:
   - 256-bit AES key generated using Android Keystore
   - Keys never leave the secure hardware
   - No user authentication required for database access

3. **Passphrase Storage**:
   - Encrypted using Android Keystore
   - Stored in EncryptedSharedPreferences
   - Automatically retrieved on app launch

### Security Features

- All data encrypted at rest (Requirement 43.1)
- Device-specific encryption keys (Requirement 43.3)
- Secure key storage using Android Keystore
- No plaintext data on disk

## Usage

### Dependency Injection

The database is provided via Hilt in `DatabaseModule`:

```kotlin
@Inject
lateinit var database: AgriEdgeDatabase

@Inject
lateinit var diagnosisDao: DiagnosisDao
```

### Example Operations

```kotlin
// Insert diagnosis
val diagnosis = DiagnosisEntity(
    userId = "user123",
    timestamp = System.currentTimeMillis(),
    cropType = "RICE",
    diseaseId = "rice_blast",
    // ... other fields
)
diagnosisDao.insert(diagnosis)

// Query diagnoses (reactive)
diagnosisDao.getAllDiagnoses("user123")
    .collect { diagnoses ->
        // Update UI
    }

// Sync queue operations
val syncItem = SyncQueueItem(
    entityType = "DIAGNOSIS",
    entityId = diagnosis.id,
    operation = "CREATE",
    payload = json,
    timestamp = System.currentTimeMillis(),
    status = "PENDING"
)
syncQueueDao.insert(syncItem)
```

## Database Migrations

Currently using `fallbackToDestructiveMigration()` for development. In production:

1. Remove fallback strategy
2. Implement proper migrations using `Migration` class
3. Test migrations thoroughly

Example migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE diagnoses ADD COLUMN new_field TEXT")
    }
}
```

## Performance Considerations

1. **Indexing**: Add indexes for frequently queried columns
2. **Batch Operations**: Use `insertAll()` for bulk inserts
3. **Flow Queries**: Use Flow for reactive UI updates
4. **Cache Management**: Implement TTL for cached data (treatments)

## Testing

Test database operations using:
- Room's in-memory database for unit tests
- Property-based tests for data integrity
- Encryption verification tests

## Dependencies

```gradle
// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// SQLCipher
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite-ktx:2.4.0")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

## Requirements Mapping

- **5.1**: Diagnosis storage with 12-month retention
- **34.3**: User profile local storage
- **43.1**: AES-256 encryption for all data
- **43.2**: Encrypted images and transaction history
- **43.3**: Device-specific encryption keys
- **36.1-36.3**: Sync queue for offline operations
- **4.1, 15.1**: Treatment recommendation caching
- **18.1-18.3**: Transaction management
- **31.1-31.3**: Provider rating storage

## Future Enhancements

1. Add database backup/restore functionality
2. Implement data export for user (GDPR compliance)
3. Add database size monitoring and cleanup
4. Implement automatic old data archival
5. Add database integrity checks
