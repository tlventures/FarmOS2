# Machine Learning Module

This module contains the TensorFlow Lite integration for on-device crop disease classification.

## Components

### DiseaseClassifier

The main classifier class that handles:
- Loading TFLite models from assets
- Configuring GPU acceleration and NNAPI
- Image preprocessing (resize to 224x224, normalize to [-1, 1])
- Running inference on device
- Parsing model output to structured results

**Location**: `com.agriedge.data.ml.classifier.DiseaseClassifier`

## Current Status: Mock Mode

The classifier currently operates in **mock mode** because no trained TFLite model is available yet. This allows the rest of the application to be developed and tested while the ML model is being trained.

### Mock Mode Features

- Generates realistic classification results
- Simulates inference time (1-3 seconds)
- Provides crop-specific diseases:
  - **Cotton**: Leaf Curl, Bollworm, Healthy
  - **Wheat**: Rust, Blight, Healthy
  - **Tomato**: Late Blight, Leaf Curl, Healthy
- Realistic confidence scores (top: 75-95%, others: 5-20%)
- Includes localized disease names in Hindi

### Switching to Real Model

To use a real TFLite model:

1. Place the trained model at: `app/src/main/assets/models/crop_disease_classifier.tflite`
2. The classifier will automatically detect it and disable mock mode
3. Ensure the model has the correct input/output shapes:
   - Input: `[1, 224, 224, 3]` (NHWC format)
   - Output: `[1, 40]` (40 disease classes)

## Usage

### Initialization

```kotlin
@Inject
lateinit var classifier: DiseaseClassifier

// Initialize in a coroutine
lifecycleScope.launch {
    classifier.initialize()
}
```

### Classification

```kotlin
val bitmap: Bitmap = // captured image
val cropType = CropType.COTTON

val result = classifier.classify(bitmap, cropType)

// Access results
val topPrediction = result.topPredictions[0]
println("Disease: ${topPrediction.disease.commonName}")
println("Confidence: ${topPrediction.confidence * 100}%")
println("Inference time: ${result.inferenceTime}ms")
```

### Cleanup

```kotlin
override fun onDestroy() {
    super.onDestroy()
    classifier.close()
}
```

## Hardware Acceleration

The classifier attempts to use hardware acceleration in this order:

1. **GPU Delegate** (fastest)
   - Uses device GPU for inference
   - Typically 2-5x faster than CPU
   - Falls back to NNAPI if unavailable

2. **NNAPI** (Android Neural Networks API)
   - Uses device-specific accelerators (DSP, NPU)
   - Good performance on modern devices
   - Falls back to CPU if unavailable

3. **CPU** (fallback)
   - Uses 4 threads for parallel processing
   - Works on all devices
   - Slowest option but most compatible

## Image Preprocessing

The classifier preprocesses images using ImageNet normalization:

1. Resize to 224x224 pixels
2. Extract RGB channels
3. Normalize to [-1, 1] range:
   - `normalized = (pixel - 127.5) / 127.5`

## Performance Requirements

Based on Requirements 2.2 and 47.3:

- **Inference time**: < 3 seconds on Snapdragon 665
- **Model size**: < 50MB
- **Accuracy**: > 85% top-1 accuracy
- **Memory usage**: < 200MB during inference

## Testing

Unit tests are available at:
- `app/src/test/java/com/agriedge/data/ml/classifier/DiseaseClassifierTest.kt`

Run tests with:
```bash
./gradlew test --tests "com.agriedge.data.ml.classifier.DiseaseClassifierTest"
```

## Dependencies

The classifier uses:
- `org.tensorflow:tensorflow-lite:2.14.0`
- `org.tensorflow:tensorflow-lite-support:0.4.4`
- `org.tensorflow:tensorflow-lite-gpu:2.14.0`

## Requirements Mapping

- **Requirement 2.1**: On-device processing without network
- **Requirement 2.2**: < 3 second inference time
- **Requirement 2.4**: Support for 40+ disease classes

## Future Enhancements

1. **Model Updates**: OTA model updates via backend
2. **Model Metadata**: Include disease mappings in model
3. **Multi-model Support**: Crop-specific models for better accuracy
4. **Quantization**: INT8 quantization for smaller size
5. **Batch Processing**: Process multiple images at once
