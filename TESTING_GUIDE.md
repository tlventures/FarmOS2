# Testing Guide - Camera & ML Features

## Quick Start Testing

### Prerequisites
- Android device or emulator with camera
- App installed and running
- Camera permissions granted

### Test Flow
1. Launch app → Login/Register
2. Home screen → Tap "Diagnose Disease"
3. Crop Selection → Select crop type
4. Camera → Grant permission, capture image
5. Results → View diagnosis
6. History → Check saved diagnoses

---

## Detailed Test Scenarios

### 1. Camera Integration Test

#### Test: Camera Preview
**Steps:**
1. Open app and login
2. Tap "Diagnose Disease" on home screen
3. Select any crop type (e.g., Cotton)
4. Tap "Continue" or navigate to camera

**Expected:**
- ✅ Camera permission prompt appears
- ✅ After granting, live camera preview shows
- ✅ Preview is clear and responsive
- ✅ Back camera is used (not front)

#### Test: Crop Type Selection
**Steps:**
1. On camera screen, tap crop type dropdown
2. Select different crop types

**Expected:**
- ✅ Dropdown shows all 6 crop types
- ✅ Selection updates immediately
- ✅ Selected crop persists during session

#### Test: Real-time Guidance
**Steps:**
1. Point camera at different objects
2. Move closer/farther from subject
3. Change lighting conditions

**Expected:**
- ✅ Guidance messages appear in overlay
- ✅ Messages update based on conditions
- ✅ "Ready to capture" when optimal

#### Test: Image Capture
**Steps:**
1. Point camera at a leaf or plant
2. Tap the white capture button
3. Wait for processing

**Expected:**
- ✅ Capture button responds immediately
- ✅ Processing indicator appears
- ✅ Screen doesn't freeze
- ✅ Navigates to results after 1-3 seconds

---

### 2. ML Classification Test

#### Test: Disease Detection (Mock Mode)
**Steps:**
1. Capture image of any plant/leaf
2. Wait for processing
3. View results screen

**Expected:**
- ✅ Processing takes 1-3 seconds
- ✅ Results screen shows disease name
- ✅ Confidence score displayed (75-95% for top)
- ✅ Top 3 predictions shown
- ✅ Disease names in English and Hindi

#### Test: Different Crop Types
**Steps:**
1. Test with Cotton → Capture image
2. Test with Wheat → Capture image
3. Test with Tomato → Capture image

**Expected:**
- ✅ Cotton: Shows Cotton Leaf Curl, Bollworm, or Healthy
- ✅ Wheat: Shows Wheat Rust, Blight, or Healthy
- ✅ Tomato: Shows Late Blight, Leaf Curl, or Healthy
- ✅ Each has appropriate disease names

#### Test: Confidence Scores
**Steps:**
1. Capture multiple images
2. Check confidence scores for each

**Expected:**
- ✅ Top prediction: 75-95% confidence
- ✅ Second prediction: 10-20% confidence
- ✅ Third prediction: 5-10% confidence
- ✅ Warning shown if top < 70%

---

### 3. Database Persistence Test

#### Test: Save Diagnosis
**Steps:**
1. Capture and diagnose an image
2. View results
3. Navigate back to home
4. Go to "History"

**Expected:**
- ✅ Diagnosis appears in history
- ✅ Shows thumbnail image
- ✅ Shows disease name
- ✅ Shows date/time
- ✅ Shows confidence score

#### Test: Multiple Diagnoses
**Steps:**
1. Perform 3-5 diagnoses with different crops
2. Check history after each

**Expected:**
- ✅ All diagnoses saved
- ✅ Sorted by most recent first
- ✅ Each has unique ID
- ✅ Correct crop type shown

#### Test: Offline Persistence
**Steps:**
1. Perform diagnosis
2. Close app completely
3. Reopen app
4. Check history

**Expected:**
- ✅ Diagnosis still present
- ✅ Image still accessible
- ✅ All data intact
- ✅ No data loss

---

### 4. Offline Functionality Test

#### Test: Airplane Mode
**Steps:**
1. Enable airplane mode
2. Open app
3. Perform diagnosis
4. Check results and history

**Expected:**
- ✅ Camera works offline
- ✅ ML inference works offline
- ✅ Diagnosis saves locally
- ✅ History accessible offline
- ✅ No errors or crashes

#### Test: No Internet Connection
**Steps:**
1. Disconnect WiFi and mobile data
2. Perform complete diagnosis flow
3. Check all features

**Expected:**
- ✅ All core features work
- ✅ No "no internet" errors
- ✅ Smooth user experience
- ✅ Data queued for sync

---

### 5. Error Handling Test

#### Test: Permission Denial
**Steps:**
1. Deny camera permission
2. Try to access camera

**Expected:**
- ✅ Clear message shown
- ✅ Option to grant permission
- ✅ No crash
- ✅ Can retry after granting

#### Test: Low Storage
**Steps:**
1. Fill device storage (if possible)
2. Try to capture image

**Expected:**
- ✅ Error message shown
- ✅ Graceful degradation
- ✅ No crash
- ✅ User informed of issue

#### Test: App Restart During Processing
**Steps:**
1. Start diagnosis
2. Force close app during processing
3. Reopen app

**Expected:**
- ✅ App recovers gracefully
- ✅ No corrupted data
- ✅ Can retry diagnosis
- ✅ No crash on restart

---

### 6. UI/UX Test

#### Test: Navigation Flow
**Steps:**
1. Navigate through entire diagnosis flow
2. Use back button at each step
3. Check all transitions

**Expected:**
- ✅ Smooth transitions
- ✅ Back button works correctly
- ✅ No stuck screens
- ✅ Proper navigation stack

#### Test: Loading States
**Steps:**
1. Observe UI during processing
2. Check all loading indicators

**Expected:**
- ✅ Loading spinner shows during processing
- ✅ UI remains responsive
- ✅ Clear feedback to user
- ✅ Can't double-tap capture

#### Test: Error Messages
**Steps:**
1. Trigger various errors
2. Read error messages

**Expected:**
- ✅ Clear, user-friendly messages
- ✅ Actionable guidance
- ✅ No technical jargon
- ✅ Proper localization

---

### 7. Performance Test

#### Test: App Launch Time
**Steps:**
1. Close app completely
2. Launch app
3. Time until home screen

**Expected:**
- ✅ Cold start < 4 seconds
- ✅ Warm start < 1 second
- ✅ No ANR (App Not Responding)
- ✅ Smooth startup

#### Test: Inference Time
**Steps:**
1. Capture multiple images
2. Measure processing time for each

**Expected:**
- ✅ Processing: 1-3 seconds
- ✅ Consistent performance
- ✅ No significant slowdown
- ✅ UI remains responsive

#### Test: Memory Usage
**Steps:**
1. Perform 10+ diagnoses
2. Monitor app memory
3. Check for leaks

**Expected:**
- ✅ Memory usage stable
- ✅ No memory leaks
- ✅ No crashes
- ✅ Smooth operation

---

### 8. Integration Test

#### Test: Complete User Journey
**Steps:**
1. Register new account
2. Complete profile setup
3. Perform 3 diagnoses (different crops)
4. View history
5. Check each diagnosis detail
6. Logout and login again
7. Verify data persists

**Expected:**
- ✅ All steps complete successfully
- ✅ Data persists across sessions
- ✅ No errors or crashes
- ✅ Smooth user experience

---

## Test Results Template

### Test Session Information
- **Date**: _____________
- **Tester**: _____________
- **Device**: _____________
- **Android Version**: _____________
- **App Version**: 1.0.0

### Test Results

| Test Case | Status | Notes |
|-----------|--------|-------|
| Camera Preview | ☐ Pass ☐ Fail | |
| Crop Selection | ☐ Pass ☐ Fail | |
| Image Capture | ☐ Pass ☐ Fail | |
| ML Classification | ☐ Pass ☐ Fail | |
| Database Save | ☐ Pass ☐ Fail | |
| History View | ☐ Pass ☐ Fail | |
| Offline Mode | ☐ Pass ☐ Fail | |
| Error Handling | ☐ Pass ☐ Fail | |
| Performance | ☐ Pass ☐ Fail | |

### Issues Found
1. _____________________________________________
2. _____________________________________________
3. _____________________________________________

### Overall Assessment
☐ Ready for Demo
☐ Needs Minor Fixes
☐ Needs Major Fixes

---

## Automated Testing

### Unit Tests
```bash
./gradlew test
```

Tests:
- DiseaseClassifier logic
- ImagePreprocessor validation
- Repository operations
- Use case business logic

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

Tests:
- Database operations
- Camera integration
- UI components
- Navigation flow

---

## Troubleshooting

### Issue: Camera not working
**Solution:**
1. Check camera permission granted
2. Verify device has camera
3. Restart app
4. Check other apps can use camera

### Issue: Processing takes too long
**Solution:**
1. Check device performance
2. Close other apps
3. Restart device
4. Check available storage

### Issue: Diagnosis not saving
**Solution:**
1. Check storage space
2. Verify database initialized
3. Check app logs
4. Restart app

### Issue: Images not displaying
**Solution:**
1. Check storage permissions
2. Verify image path correct
3. Check file exists
4. Clear app cache

---

## Demo Script

### 5-Minute Demo Flow

**Minute 1: Introduction**
- "AgriEdge Link helps farmers diagnose crop diseases offline"
- "Uses AI on your phone - no internet needed"

**Minute 2: Authentication**
- Show login/register
- Quick registration
- Profile setup

**Minute 3: Diagnosis**
- Navigate to diagnose
- Select crop type (Cotton)
- Show camera preview
- Capture leaf image
- Show processing

**Minute 4: Results**
- Show disease name
- Explain confidence score
- Show top 3 predictions
- View treatment recommendations

**Minute 5: History & Offline**
- Show diagnosis history
- Demonstrate offline mode
- Explain sync queue
- Highlight key features

---

## Success Criteria

### Must Pass:
- ✅ Camera captures images
- ✅ ML processes images (mock mode)
- ✅ Diagnoses save to database
- ✅ History displays correctly
- ✅ Works completely offline
- ✅ No crashes or ANRs
- ✅ Smooth user experience

### Nice to Have:
- ⏳ Real ML model
- ⏳ Background sync
- ⏳ Cloud storage
- ⏳ Voice interface
- ⏳ Treatment recommendations

---

## Next Steps After Testing

1. **If all tests pass:**
   - Ready for demo
   - Can show to stakeholders
   - Proceed with real ML model

2. **If minor issues:**
   - Document issues
   - Fix and retest
   - Update documentation

3. **If major issues:**
   - Prioritize critical bugs
   - Fix systematically
   - Full regression test

---

## Contact

For issues or questions:
- Check logs: `adb logcat | grep AgriEdge`
- Review code: See implementation files
- Test again: Follow this guide

**Happy Testing! 🧪**
