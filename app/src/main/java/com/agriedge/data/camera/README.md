# Camera Module

This module provides camera functionality for capturing crop leaf images for disease diagnosis.

## Components

### CameraManager
Manages camera operations using CameraX API with the following features:
- **Preview**: Real-time camera preview with 4:3 aspect ratio
- **Image Capture**: High-quality image capture optimized for disease analysis
- **Image Analysis**: Real-time frame analysis for guidance feedback
- **Torch Control**: Flash/torch control for low-light conditions

**Requirements**: 1.1, 1.2

**Usage**:
```kotlin
val cameraManager = CameraManager(context, lifecycleOwner)

// Start camera with preview
cameraManager.startCamera(
    previewView = previewView,
    analyzer = LeafDetectionAnalyzer { guidance ->
        // Handle guidance updates
    }
)

// Capture image
cameraManager.captureImage(
    onImageCaptured = { bitmap ->
        // Process captured image
    },
    onError = { exception ->
        // Handle error
    }
)

// Or use coroutines
val bitmap = cameraManager.captureImageAsync()
```

### LeafDetectionAnalyzer
Real-time image analyzer that provides guidance to users for optimal leaf capture:
- Detects green regions (leaf presence)
- Checks leaf size and position
- Evaluates lighting conditions
- Provides actionable feedback

**Requirements**: 1.2

**Guidance Types**:
- `NO_LEAF_DETECTED`: No leaf visible in frame
- `MOVE_CLOSER`: Leaf too small, user should move closer
- `CENTER_LEAF`: Leaf not centered in frame
- `IMPROVE_LIGHTING`: Lighting too dark or too bright
- `READY_TO_CAPTURE`: Optimal conditions for capture

**Usage**:
```kotlin
val analyzer = LeafDetectionAnalyzer { guidance ->
    when (guidance.type) {
        GuidanceType.READY_TO_CAPTURE -> {
            // Enable capture button
            showMessage(guidance.message)
        }
        else -> {
            // Show guidance to user
            showMessage(guidance.message)
        }
    }
}
```

### CameraPermissionHandler
Handles camera permission requests with proper rationale and error handling:
- Requests camera permission
- Provides rationale for permission request
- Handles permission denied scenarios
- Detects permanently denied state

**Requirements**: 1.1

**Usage**:
```kotlin
val permissionHandler = CameraPermissionHandler(
    activity = activity,
    onPermissionGranted = {
        // Start camera
    },
    onPermissionDenied = { shouldShowRationale ->
        if (shouldShowRationale) {
            // Show rationale and request again
        } else {
            // Permission permanently denied, direct to settings
        }
    }
)

permissionHandler.requestPermission()
```

### CameraPermissionScreen
Jetpack Compose UI component for handling camera permissions:
- Displays permission rationale
- Handles permission request flow
- Shows settings navigation for permanently denied state

**Requirements**: 1.1

**Usage**:
```kotlin
@Composable
fun DiagnosisScreen() {
    var hasPermission by remember { mutableStateOf(false) }
    
    if (hasPermission) {
        CameraScreen()
    } else {
        CameraPermissionScreen(
            onPermissionGranted = {
                hasPermission = true
            }
        )
    }
}
```

## Image Quality Requirements

The camera system is designed to capture images that meet the following quality standards:
- **Resolution**: Minimum 512x512 pixels
- **Brightness**: Average brightness between 30-220 (0-255 scale)
- **Sharpness**: Laplacian variance > 100 (blur detection)
- **Leaf Presence**: At least 30% green pixels in frame

These requirements are validated by the `ImagePreprocessor` in the `ml.preprocessor` package.

## Configuration

### Camera Settings
- **Aspect Ratio**: 4:3 (optimal for leaf capture)
- **Capture Mode**: MAXIMIZE_QUALITY
- **Camera Selector**: Back camera (default)
- **Analysis Strategy**: KEEP_ONLY_LATEST (for real-time guidance)

### Performance Optimization
- Image analysis throttled to 200ms intervals
- Pixel sampling (every 4th-8th pixel) for performance
- Efficient HSV color space conversion for green detection
- Laplacian variance calculation for blur detection

## Testing

Unit tests are provided for all components:
- `CameraManagerTest`: Tests camera initialization and capture
- `LeafDetectionAnalyzerTest`: Tests guidance generation
- `CameraPermissionHandlerTest`: Tests permission handling

Run tests:
```bash
./gradlew test
```

## Dependencies

Required dependencies (already included in app/build.gradle.kts):
```kotlin
// CameraX
implementation("androidx.camera:camera-core:1.3.0")
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")

// Compose
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
```

## Permissions

Required permissions (already declared in AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

## Architecture

The camera module follows Clean Architecture principles:
- **Data Layer**: `CameraManager`, `LeafDetectionAnalyzer` (this package)
- **Presentation Layer**: `CameraPermissionScreen` (presentation.camera package)
- **Domain Layer**: Image quality validation (ml.preprocessor package)

## Future Enhancements

Potential improvements for future versions:
- Manual focus control
- Exposure compensation
- HDR capture mode
- Multiple image capture for better accuracy
- ML-based leaf detection (instead of color-based)
