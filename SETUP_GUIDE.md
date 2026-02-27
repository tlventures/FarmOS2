# AgriEdge Link - Setup Guide

This guide will help you set up and run the AgriEdge Link Android application.

## Prerequisites

### Required Software

1. **Android Studio Hedgehog (2023.1.1) or later**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **JDK 17**
   - Android Studio includes JDK 17
   - Or download from: https://adoptium.net/

3. **Android SDK**
   - API Level 26 (Android 8.0) - Minimum
   - API Level 34 (Android 14) - Target
   - Install via Android Studio SDK Manager

### Optional

- Android device with Android 8.0+ for testing
- Android Emulator (Pixel 5 or later recommended)

## Setup Steps

### 1. Clone/Open Project

```bash
# If cloning from repository
git clone <repository-url>
cd agriedge-link

# Open in Android Studio
# File > Open > Select project directory
```

### 2. Sync Gradle

1. Open the project in Android Studio
2. Wait for Gradle sync to complete automatically
3. If sync fails, click "Sync Project with Gradle Files" in toolbar

### 3. Configure Android SDK

1. Open SDK Manager: `Tools > SDK Manager`
2. Ensure these are installed:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator
   - Android SDK Platform-Tools

### 4. Set Up Emulator (Optional)

1. Open AVD Manager: `Tools > Device Manager`
2. Click "Create Device"
3. Select "Pixel 5" or newer
4. Select System Image: API 34 (Android 14)
5. Click "Finish"

### 5. Build Project

```bash
# From terminal
./gradlew build

# Or in Android Studio
Build > Make Project (Ctrl+F9 / Cmd+F9)
```

### 6. Run Application

#### On Emulator

1. Start emulator from Device Manager
2. Click "Run" button (green play icon)
3. Select emulator from device list
4. App will install and launch

#### On Physical Device

1. Enable Developer Options on device:
   - Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings > Developer Options
   - Enable "USB Debugging"
3. Connect device via USB
4. Click "Run" button
5. Select device from list
6. Grant camera permission when prompted

## Project Configuration

### Gradle Properties

The project uses these key configurations:

```kotlin
// app/build.gradle.kts
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
}
```

### Dependencies

All dependencies are managed in `app/build.gradle.kts`:
- Jetpack Compose
- Hilt (Dependency Injection)
- Room (Database)
- CameraX
- TensorFlow Lite
- Retrofit (Networking)
- Coil (Image Loading)

## Troubleshooting

### Gradle Sync Issues

**Problem**: Gradle sync fails with dependency errors

**Solution**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

### Camera Permission Issues

**Problem**: Camera doesn't work on emulator

**Solution**:
- Use a physical device for camera testing
- Or configure emulator to use webcam:
  - AVD Manager > Edit Device > Show Advanced Settings
  - Camera: Front/Back = Webcam0

### Build Errors

**Problem**: "Cannot resolve symbol" errors

**Solution**:
1. File > Invalidate Caches > Invalidate and Restart
2. Sync Gradle files
3. Rebuild project

### Hilt Errors

**Problem**: Hilt dependency injection errors

**Solution**:
- Ensure `@HiltAndroidApp` is on Application class
- Ensure `@AndroidEntryPoint` is on MainActivity
- Clean and rebuild project

## Running Tests

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "DiagnosisRepositoryImplTest"

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Instrumentation Tests

```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest
```

## Development Workflow

### 1. Make Changes

Edit Kotlin files in `app/src/main/java/com/agriedge/`

### 2. Hot Reload (Compose)

Compose supports live updates:
- Make UI changes
- Save file (Ctrl+S / Cmd+S)
- Changes appear in running app

### 3. Debug

1. Set breakpoints in code
2. Click "Debug" button (bug icon)
3. Use debugger controls to step through code

### 4. Check Diagnostics

Android Studio shows errors in real-time:
- Red underlines = errors
- Yellow underlines = warnings
- Hover for details

## Next Steps

### Add ML Model

1. Train or obtain TFLite model
2. Place in `app/src/main/assets/models/crop_disease_classifier.tflite`
3. Update model path in `DiseaseClassifier.kt` if needed

### Configure Backend

1. Set up AWS infrastructure (see `terraform/` directory)
2. Update API endpoints in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"https://your-api.com\"")
   ```

### Add Localization

1. Create `values-hi/strings.xml` for Hindi
2. Create `values-mr/strings.xml` for Marathi
3. Add translations for all strings

## Resources

- [Android Developer Docs](https://developer.android.com/)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [CameraX Documentation](https://developer.android.com/training/camerax)

## Support

For issues or questions:
1. Check existing issues in repository
2. Review troubleshooting section above
3. Contact development team

## License

Copyright © 2024 AgriEdge Link. All rights reserved.
