# AgriEdge Link - Android Application

An AI-powered mobile application for smallholder farmers to diagnose crop diseases offline and connect to agricultural markets.

## Features Implemented

### ✅ Complete UI Implementation

1. **Home Screen**
   - Main menu with three action cards
   - Navigate to Diagnose Disease, View History, and Market (placeholder)
   - Material 3 design with agricultural green theme

2. **Camera Screen**
   - Live camera preview using CameraX
   - Real-time leaf detection and guidance overlay
   - Crop type selector (Rice, Wheat, Tomato, Potato, Cotton, Sugarcane)
   - Capture button with processing states
   - Requirements: 1.1, 1.2, 6.2

3. **Diagnosis Result Screen**
   - Display disease name (common and scientific)
   - Confidence score as percentage with visual indicator
   - Top 3 predictions with confidence levels
   - Low confidence warning (< 70%)
   - Voice readout button (placeholder)
   - Navigate to treatment recommendations
   - Requirements: 3.1, 3.2, 3.3, 14.1

4. **Treatment Details Screen**
   - Organic treatment options with detailed instructions
   - Chemical treatment options with dosage and timing
   - Preventive measures
   - Voice readout support (placeholder)
   - Requirements: 4.1, 4.2, 4.3, 4.4, 15.1

5. **Diagnosis History Screen**
   - Reverse chronological list of diagnoses
   - Thumbnail, disease name, date, and confidence badge
   - Pull-to-refresh functionality
   - Search/filter by disease name or crop type
   - Requirements: 8.1, 8.2

### Architecture

- **Clean Architecture** with MVVM presentation pattern
- **Jetpack Compose** for modern declarative UI
- **Hilt** for dependency injection
- **Navigation Component** for type-safe navigation
- **StateFlow** for reactive state management
- **Material 3** design system with custom agricultural theme

### ViewModels

- `CameraViewModel` - Manages camera state and diagnosis process
- `DiagnosisResultViewModel` - Loads and displays diagnosis details
- `DiagnosisHistoryViewModel` - Manages history list with search/filter
- `TreatmentDetailsViewModel` - Loads treatment recommendations

### Navigation

- Home → Camera → Diagnosis Result → Treatment Details
- Home → Diagnosis History → Diagnosis Result
- Type-safe navigation with NavGraph

### Theme

- **Primary Color**: Agricultural Green (#2E7D32)
- **Secondary Color**: Light Green (#66BB6A)
- Light and Dark theme support
- Material 3 color scheme

## Project Structure

```
app/src/main/java/com/agriedge/
├── data/
│   ├── camera/          # CameraX integration
│   ├── local/           # Room database & storage
│   ├── ml/              # TensorFlow Lite classifier
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic use cases
├── presentation/
│   ├── diagnosis/       # Camera, Result, History screens
│   ├── treatment/       # Treatment details screen
│   ├── home/            # Home screen
│   ├── navigation/      # Navigation graph
│   └── theme/           # Material 3 theme
└── di/                  # Hilt dependency injection modules
```

## Tech Stack

- **Language**: Kotlin 1.9+
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room + SQLCipher
- **ML**: TensorFlow Lite
- **Camera**: CameraX
- **Image Loading**: Coil
- **Testing**: JUnit 5, Kotest, MockK

## Building the App

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Build Steps

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project: `Build > Make Project`
4. Run on emulator or device: `Run > Run 'app'`

### Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## Running the App

1. **Launch**: App opens to Home Screen
2. **Diagnose Disease**: 
   - Tap "Diagnose Disease"
   - Grant camera permission
   - Select crop type
   - Point camera at diseased leaf
   - Follow real-time guidance
   - Tap capture button
   - View diagnosis results
3. **View History**:
   - Tap "View History"
   - Browse past diagnoses
   - Search/filter by disease or crop
   - Tap any diagnosis to view details
4. **Treatment Details**:
   - From diagnosis result, tap "View Treatment Recommendations"
   - Review organic and chemical options
   - Read preventive measures

## Next Steps

To make this a fully functional app, implement:

1. **ML Model**: Add trained TFLite model to `assets/models/`
2. **Backend Integration**: Connect to AWS Lambda APIs for:
   - Treatment recommendations (Bedrock)
   - Voice services (Transcribe/Polly)
   - Data sync
3. **Voice Interface**: Implement speech-to-text and text-to-speech
4. **Market Features**: Add buyer search, cold storage, equipment rental
5. **Authentication**: Implement OTP-based login
6. **Offline Sync**: Complete WorkManager sync implementation
7. **Localization**: Add Hindi, Marathi, Tamil, Telugu, Kannada, Bengali strings

## Testing

The app includes:
- Unit tests for ViewModels and use cases
- Property-based tests for core logic
- Integration tests for repositories
- UI tests with Espresso (to be added)

Run tests:
```bash
./gradlew test
```

## License

Copyright © 2024 AgriEdge Link. All rights reserved.

## Contact

For questions or support, contact the development team.
