# Features Yet to Be Built - AgriEdge Link

## Summary
The app currently has a working UI with mock data for most features. The following components need to be implemented to make the app fully functional.

---

## ✅ COMPLETED FEATURES

### Core UI & Navigation
- ✅ Login/Register screens with authentication flow
- ✅ Profile screen with view/edit modes
- ✅ Home screen with feature cards
- ✅ Navigation drawer with user info
- ✅ Settings screen with language selection
- ✅ Crop selection screen

### Market Features (UI Only - Mock Data)
- ✅ Market listing screen with search/filter
- ✅ Market detail screen with seller info
- ✅ Cold storage search and detail screens
- ✅ Equipment rental search and detail screens

### Diagnosis Features (UI Only - Mock Data)
- ✅ Camera screen placeholder
- ✅ Diagnosis result screen
- ✅ Diagnosis history screen
- ✅ Treatment details screen

### Localization
- ✅ English, Hindi, and Marathi string resources
- ✅ Language selection in settings
- ✅ Voice button component (UI only)

---

## 🚧 FEATURES TO BUILD

### 1. CAMERA & IMAGE CAPTURE (Phase 4)
**Status:** Not Started  
**Priority:** HIGH

#### Tasks:
- [ ] Implement CameraX integration for real camera capture
- [ ] Add real-time leaf detection guidance overlay
- [ ] Implement image quality validation (brightness, blur, resolution)
- [ ] Add camera permissions handling
- [ ] Create image preprocessing pipeline

**Current State:** CameraScreen exists but doesn't actually use camera

---

### 2. ML MODEL INTEGRATION (Phase 3)
**Status:** Not Started  
**Priority:** HIGH

#### Tasks:
- [ ] Integrate TensorFlow Lite for disease classification
- [ ] Add disease classification model (40+ diseases)
- [ ] Implement image preprocessing (224x224, normalization)
- [ ] Add GPU/NNAPI acceleration support
- [ ] Implement model update mechanism (OTA updates)
- [ ] Support 6 crop types: rice, wheat, tomato, potato, cotton, sugarcane

**Current State:** DiseaseClassifier class exists but uses mock data

**Requirements:**
- Model size: <50MB (quantized)
- Inference time: <3 seconds on Snapdragon 665
- Accuracy: >85% top-1 accuracy
- Support pest detection (aphids, whiteflies, bollworms)

---

### 3. REAL DIAGNOSIS FLOW (Phase 5)
**Status:** Partially Complete  
**Priority:** HIGH

#### Tasks:
- [ ] Connect camera → ML model → diagnosis result flow
- [ ] Implement DiagnoseDiseaseUseCase with real ML inference
- [ ] Save diagnoses to Room database
- [ ] Display confidence scores and top 3 predictions
- [ ] Show low confidence warning (<70%)
- [ ] Store diagnostic images locally

**Current State:** UI exists, but uses mock diagnosis data

---

### 4. AUTHENTICATION BACKEND (Phase 6)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Create AWS Lambda for phone registration
- [ ] Implement OTP generation and SMS sending (Amazon SNS)
- [ ] Create OTP verification Lambda with JWT token generation
- [ ] Implement token refresh Lambda
- [ ] Set up API Gateway endpoints
- [ ] Add rate limiting (100 requests/minute)

**Current State:** Mock authentication in AuthRepositoryImpl (in-memory only)

---

### 5. VOICE INTERFACE (Phase 9)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Integrate AWS Transcribe for speech-to-text (6 Indian languages)
- [ ] Integrate AWS Polly for text-to-speech
- [ ] Add Bhashini API as fallback
- [ ] Implement AudioRecorder for voice input
- [ ] Create VoiceCommandParser for natural language commands
- [ ] Add voice readout for diagnoses and treatments
- [ ] Support hands-free navigation

**Current State:** VoiceButton UI exists but doesn't record/process audio

**Supported Languages:** Hindi, Marathi, Tamil, Telugu, Kannada, Bengali

---

### 6. TREATMENT RECOMMENDATIONS (Phase 10)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Integrate Amazon Bedrock with Claude 3 Sonnet
- [ ] Generate treatment recommendations from disease ID
- [ ] Set up Bedrock Knowledge Base with agricultural documents
- [ ] Include organic/chemical options with dosage
- [ ] Use locally available product names
- [ ] Translate recommendations to user's language
- [ ] Cache treatments in DynamoDB (30-day TTL)

**Current State:** TreatmentDetailsScreen shows mock treatment data

---

### 7. AMAZON Q CHATBOT (Phase 11)
**Status:** Not Started  
**Priority:** LOW

#### Tasks:
- [ ] Set up Amazon Q Business application
- [ ] Upload agricultural knowledge base to S3
- [ ] Create Q chat Lambda for farmer queries
- [ ] Build ChatScreen UI with message history
- [ ] Add voice input/output for chat
- [ ] Display source attributions

**Current State:** Not implemented

---

### 8. BECKN/ONDC MARKET INTEGRATION (Phase 13-14)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Implement Beckn protocol client (Python)
- [ ] Create buyer search Lambda with Beckn network queries
- [ ] Create cold storage search Lambda
- [ ] Create equipment rental search Lambda
- [ ] Implement transaction initiation (select → init → confirm)
- [ ] Add logistics bundling for transport
- [ ] Implement provider ratings and reviews
- [ ] Set up Beckn webhook handlers

**Current State:** All market features use mock data (MarketRepositoryImpl, ColdStorageRepositoryImpl, EquipmentRentalRepositoryImpl)

**Beckn Features:**
- Search buyers by crop/quantity/location
- Find cold storage facilities
- Rent agricultural equipment
- Bundle transport with sales
- Rate providers after transactions

---

### 9. DATA SYNCHRONIZATION (Phase 12)
**Status:** Partially Complete  
**Priority:** MEDIUM

#### Tasks:
- [ ] Implement SyncWorker with WorkManager
- [ ] Create sync queue processing (FIFO order)
- [ ] Add exponential backoff retry (30s, 2m, 10m)
- [ ] Implement batch sync (50 on WiFi, 10 on metered)
- [ ] Create diagnosis sync Lambda
- [ ] Create rating sync Lambda
- [ ] Create telemetry sync Lambda
- [ ] Add sync status UI indicator

**Current State:** SyncQueueDao exists but sync logic not implemented

---

### 10. CLOUD STORAGE (Phase 15)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Set up AWS S3 buckets for images and models
- [ ] Configure CloudFront CDN for image delivery
- [ ] Create presigned URL Lambda for uploads
- [ ] Implement direct S3 upload in Android app
- [ ] Upload diagnosis images after capture
- [ ] Set up lifecycle policies (Glacier after 90 days)

**Current State:** Images stored locally only

---

### 11. LOCALIZATION COMPLETION (Phase 8)
**Status:** Partially Complete  
**Priority:** LOW

#### Tasks:
- [ ] Complete translations for Tamil, Telugu, Kannada, Bengali
- [ ] Translate disease names to all 6 languages
- [ ] Add region-specific treatment terminology
- [ ] Localize all UI strings
- [ ] Test language switching

**Current State:** Only English, Hindi, and Marathi translations exist

---

### 12. PERFORMANCE OPTIMIZATION (Phase 16)
**Status:** Not Started  
**Priority:** LOW

#### Tasks:
- [ ] Implement model quantization (INT8)
- [ ] Add GPU acceleration with TFLite GPU delegate
- [ ] Set up Redis caching for backend
- [ ] Implement image compression (JPEG quality 85%)
- [ ] Optimize background sync with battery checks
- [ ] Add in-memory LRU cache for treatments

---

### 13. SECURITY & PRIVACY (Phase 17)
**Status:** Partially Complete  
**Priority:** MEDIUM

#### Tasks:
- [ ] Implement local data encryption (AES-256) - EncryptedStorageManager exists
- [ ] Add TLS 1.3 for all network communication
- [ ] Implement certificate pinning
- [ ] Anonymize telemetry data
- [ ] Add account deletion functionality
- [ ] Implement secure token storage

**Current State:** EncryptedStorageManager exists but not fully integrated

---

### 14. TESTING (All Phases)
**Status:** Not Started  
**Priority:** MEDIUM

#### Tasks:
- [ ] Write property-based tests for all use cases
- [ ] Add unit tests for repositories
- [ ] Create integration tests for sync flow
- [ ] Add UI tests with Espresso
- [ ] Test offline functionality
- [ ] Performance testing on target devices

**Current State:** Some test files exist but are empty

---

## 📊 IMPLEMENTATION PRIORITY

### Phase 1 (MVP - Core Functionality)
1. Camera & Image Capture
2. ML Model Integration
3. Real Diagnosis Flow
4. Data Synchronization (basic)

### Phase 2 (Enhanced Features)
1. Authentication Backend
2. Voice Interface
3. Treatment Recommendations (Bedrock)
4. Beckn/ONDC Market Integration

### Phase 3 (Advanced Features)
1. Amazon Q Chatbot
2. Cloud Storage & CDN
3. Complete Localization
4. Performance Optimization

### Phase 4 (Production Ready)
1. Security Hardening
2. Comprehensive Testing
3. Error Handling & Resilience
4. Monitoring & Analytics

---

## 🎯 NEXT STEPS

To make the app functional for demo purposes, focus on:

1. **Camera Integration** - Replace mock camera with real CameraX implementation
2. **ML Model** - Add a small TFLite model for disease classification
3. **Diagnosis Flow** - Connect camera → model → results
4. **Backend Setup** - Deploy basic AWS infrastructure for authentication

The current app is a complete UI shell with navigation and mock data. All screens are built and functional, but they need to be connected to real data sources and services.

---

## 📝 NOTES

- All UI screens are complete and working with mock data
- Authentication flow works but uses in-memory storage (resets on app restart)
- Market features display mock listings from India (Delhi NCR region)
- Voice button exists but doesn't record/process audio
- Settings allow language selection but only 3 languages have translations
- Database schema is complete (Room DAOs exist)
- Repository interfaces are defined but implementations use mock data
