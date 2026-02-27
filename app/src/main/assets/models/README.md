# TensorFlow Lite Models

This directory contains the TensorFlow Lite models for crop disease classification.

## Model Requirements

- **Model Name**: `crop_disease_classifier.tflite`
- **Input Shape**: `[1, 224, 224, 3]` (batch, height, width, channels)
- **Input Type**: Float32
- **Input Range**: [-1, 1] (normalized)
- **Output Shape**: `[1, 40]` (batch, number of disease classes)
- **Output Type**: Float32 (confidence scores)
- **Model Size**: < 50MB (quantized)

## Model Format

The model should be a quantized TensorFlow Lite model (.tflite file) that:
1. Accepts 224x224 RGB images
2. Outputs confidence scores for 40+ disease classes
3. Supports 6 crop types: Rice, Wheat, Tomato, Potato, Cotton, Sugarcane

## Adding a Model

1. Place your `.tflite` model file in this directory
2. Name it `crop_disease_classifier.tflite`
3. The app will automatically load it on startup
4. If no model is found, the app will use mock classification for testing

## Mock Mode

Currently, the app runs in **mock mode** because no actual TFLite model is present.

Mock mode provides:
- Realistic disease classifications for testing
- Simulated inference times (1-3 seconds)
- Confidence scores that vary by prediction rank
- Support for all 6 crop types

To disable mock mode and use a real model:
1. Add a valid `crop_disease_classifier.tflite` file to this directory
2. Rebuild the app
3. The classifier will automatically detect and load the model

## Model Training

For information on training a custom disease classification model, see:
- TensorFlow Lite Model Maker: https://www.tensorflow.org/lite/models/modify/model_maker
- Plant Disease Dataset: https://www.kaggle.com/datasets/vipoooool/new-plant-diseases-dataset

## Model Optimization

Recommended optimizations for mobile deployment:
- Post-training quantization (INT8)
- Dynamic range quantization for weights
- Pruning to reduce model size
- GPU delegate support for faster inference

## Testing

To test the model:
1. Use the Camera screen to capture a leaf image
2. Select the appropriate crop type
3. Capture the image
4. View the diagnosis results with confidence scores

The app will display:
- Top 3 disease predictions
- Confidence scores for each prediction
- Warning if confidence < 70%
- Inference time in milliseconds
