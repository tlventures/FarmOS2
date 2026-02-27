# AgriEdge Link - Feature Implementation Plan

## Overview
This document outlines the implementation plan for:
1. Cold Storage & Equipment Rental features
2. Voice Interface (Android SpeechRecognizer + TextToSpeech)
3. Multi-language support infrastructure
4. Settings screen
5. Crop selection

## Implementation Status

### ✅ Completed
- Multi-language string resources (English & Hindi)
- Base infrastructure for localization

### 🚧 In Progress
- Cold Storage features
- Equipment Rental features
- Voice Interface
- Settings Screen
- Crop Selection

## Phase 1: Cold Storage & Equipment Rental

### Files to Create

#### 1. Domain Models
**File**: `app/src/main/java/com/agriedge/domain/model/ColdStorage.kt`
```kotlin
data class ColdStorageFacility(
    val id: String,
    val facilityName: String,
    val location: String,
    val distance: Double, // in km
    val dailyRate: Double,
    val currency: String = "KES",
    val availableCapacity: Double, // in tons
    val totalCapacity: Double,
    val rating: Float?,
    val reviewCount: Int,
    val address: String,
    val contact: String,
    val features: List<String>
)

data class EquipmentRental(
    val id: String,
    val providerName: String,
    val equipmentType: EquipmentType,
    val model: String,
    val specifications: EquipmentSpecs,
    val dailyRate: Double,
    val currency: String = "KES",
    val distance: Double,
    val rating: Float?,
    val reviewCount: Int,
    val deliveryAvailable: Boolean,
    val deliveryCharge: Double?,
    val contact: String
)

enum class EquipmentType {
    TRACTOR, SPRAYER, HARVESTER, PLOUGH, SEEDER
}

data class EquipmentSpecs(
    val horsepower: Int?,
    val capacity: String?,
    val age: Int?, // in years
    val fuelType: String?
)
```

#### 2. Repository Interfaces
**File**: `app/src/main/java/com/agriedge/domain/repository/ColdStorageRepository.kt`
```kotlin
interface ColdStorageRepository {
    suspend fun searchColdStorage(
        latitude: Double,
        longitude: Double,
        radius: Int,
        requiredCapacity: Double
    ): Result<List<ColdStorageFacility>>
    
    suspend fun getColdStorageDetails(id: String): Result<ColdStorageFacility>
    
    suspend fun bookColdStorage(
        facilityId: String,
        capacity: Double,
        duration: Int
    ): Result<String> // Returns booking ID
}

interface EquipmentRentalRepository {
    suspend fun searchEquipment(
        latitude: Double,
        longitude: Double,
        radius: Int,
        equipmentType: EquipmentType,
        startDate: Long,
        endDate: Long
    ): Result<List<EquipmentRental>>
    
    suspend fun getEquipmentDetails(id: String): Result<EquipmentRental>
    
    suspend fun bookEquipment(
        equipmentId: String,
        startDate: Long,
        endDate: Long,
        deliveryRequired: Boolean
    ): Result<String> // Returns booking ID
}
```

#### 3. Repository Implementations (with mock data)
**File**: `app/src/main/java/com/agriedge/data/repository/ColdStorageRepositoryImpl.kt`
**File**: `app/src/main/java/com/agriedge/data/repository/EquipmentRentalRepositoryImpl.kt`

#### 4. UI Screens
**File**: `app/src/main/java/com/agriedge/presentation/coldstorage/ColdStorageSearchScreen.kt`
**File**: `app/src/main/java/com/agriedge/presentation/coldstorage/ColdStorageDetailScreen.kt`
**File**: `app/src/main/java/com/agriedge/presentation/equipment/EquipmentSearchScreen.kt`
**File**: `app/src/main/java/com/agriedge/presentation/equipment/EquipmentDetailScreen.kt`

#### 5. ViewModels
**File**: `app/src/main/java/com/agriedge/presentation/coldstorage/ColdStorageViewModel.kt`
**File**: `app/src/main/java/com/agriedge/presentation/equipment/EquipmentViewModel.kt`

#### 6. Navigation Updates
- Add routes to `NavGraph.kt`
- Update `MarketScreen.kt` to include Cold Storage and Equipment options

## Phase 2: Voice Interface

### Files to Create

#### 1. Voice Service Interface
**File**: `app/src/main/java/com/agriedge/data/voice/VoiceService.kt`
```kotlin
interface VoiceService {
    fun startListening(
        languageCode: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    
    fun stopListening()
    
    fun speak(
        text: String,
        languageCode: String,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    )
    
    fun isAvailable(): Boolean
}
```

#### 2. Android Implementation
**File**: `app/src/main/java/com/agriedge/data/voice/AndroidVoiceService.kt`
- Uses `SpeechRecognizer` for STT
- Uses `TextToSpeech` for TTS
- Supports 6 Indian languages

#### 3. Voice Command Parser
**File**: `app/src/main/java/com/agriedge/data/voice/VoiceCommandParser.kt`
- Parses voice commands
- Extracts intents (diagnose, search market, view history)
- Extracts entities (crop type, quantity, etc.)

#### 4. UI Components
**File**: `app/src/main/java/com/agriedge/presentation/components/VoiceButton.kt`
- Floating action button for voice input
- Visual feedback during listening
- Error handling

### Android Manifest Permissions
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Phase 3: Settings Screen

### Files to Create

#### 1. Settings Data Store
**File**: `app/src/main/java/com/agriedge/data/local/preferences/SettingsDataStore.kt`
```kotlin
class SettingsDataStore(context: Context) {
    private val dataStore = context.createDataStore("settings")
    
    val languageCode: Flow<String>
    val wifiOnlySync: Flow<Boolean>
    val notificationsEnabled: Flow<Boolean>
    
    suspend fun setLanguage(code: String)
    suspend fun setWifiOnlySync(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
```

#### 2. Settings Screen
**File**: `app/src/main/java/com/agriedge/presentation/settings/SettingsScreen.kt`
- Language selection
- Sync preferences
- Notification settings
- About section

#### 3. Settings ViewModel
**File**: `app/src/main/java/com/agriedge/presentation/settings/SettingsViewModel.kt`

## Phase 4: Crop Selection

### Files to Create

#### 1. Crop Selection Screen
**File**: `app/src/main/java/com/agriedge/presentation/diagnosis/CropSelectionScreen.kt`
- Grid of crop types with icons
- Search functionality
- Recent crops

#### 2. Update Navigation
- Add crop selection before camera screen
- Pass selected crop to diagnosis flow

## Implementation Order

### Week 1: Foundation
1. ✅ Multi-language strings (DONE)
2. Settings Screen + DataStore
3. Crop Selection Screen

### Week 2: Market Features
4. Cold Storage models, repository, screens
5. Equipment Rental models, repository, screens
6. Navigation integration

### Week 3: Voice Interface
7. Voice Service implementation
8. Voice Command Parser
9. Voice Button component
10. Integration with existing screens

## Testing Strategy

### Unit Tests
- Repository implementations
- Voice command parser
- Settings data store

### UI Tests
- Cold storage search flow
- Equipment rental flow
- Voice button interaction
- Settings changes

### Integration Tests
- End-to-end cold storage booking
- End-to-end equipment rental
- Voice command to action flow

## Dependencies to Add

```kotlin
// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Already have these, but verify:
// - Hilt for DI
// - Compose Navigation
// - Room Database
```

## Estimated Effort

- **Cold Storage & Equipment**: 8-12 hours
- **Voice Interface**: 6-8 hours  
- **Settings Screen**: 3-4 hours
- **Crop Selection**: 2-3 hours
- **Testing & Integration**: 4-6 hours

**Total**: 23-33 hours of development time

## Next Steps

1. Review this plan
2. Prioritize features
3. Begin implementation in phases
4. Test each phase before moving to next

## Notes

- Voice interface will use Android's built-in APIs (no AWS initially)
- All features will have mock data for demo purposes
- Real backend integration can be added later
- Focus on UI/UX and user flow first
