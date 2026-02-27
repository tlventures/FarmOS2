# Demo Credentials & Quick Start Guide

## 🔐 Authentication

The app uses **in-memory authentication** (resets when app closes). You can create any account you want!

### Option 1: Register New Account (Recommended)

1. **Launch App** → You'll see the Login screen
2. **Tap "Register"** at the bottom
3. **Fill in the form:**
   - Full Name: `Rajesh Kumar` (or any name)
   - Phone Number: `9876543210` (or any 10-digit number)
   - Email: (optional) `rajesh@example.com`
   - Location: `Delhi, India` (or any location)
   - Password: `password123` (or any password)
   - Confirm Password: `password123` (same as above)
4. **Tap "Register"**
5. **You're in!** → Automatically logged in and taken to Home screen

### Option 2: Login with Existing Account

**Important:** Since authentication is in-memory, you can only login with accounts you've registered in the current app session.

**First Time:**
1. Register an account (see Option 1)
2. Use those credentials to login later

**Example Credentials** (after you register):
- Phone: `9876543210`
- Password: `password123`

---

## 🚀 Quick Start Flow

### Complete Demo Flow (5 minutes)

#### 1. First Launch & Registration (1 min)
```
Launch App
  ↓
Tap "Register"
  ↓
Enter Details:
  - Name: Rajesh Kumar
  - Phone: 9876543210
  - Location: Delhi, India
  - Password: password123
  ↓
Tap "Register"
  ↓
✅ Logged in!
```

#### 2. Explore Home Screen (30 sec)
- See feature cards: Diagnose, History, Market
- Check drawer menu (swipe from left or tap ☰)
- View your profile name in drawer

#### 3. Diagnose Disease (2 min)
```
Home → Tap "Diagnose Disease"
  ↓
Select Crop Type: Cotton
  ↓
Tap "Continue"
  ↓
Grant Camera Permission
  ↓
Point camera at any leaf/plant
  ↓
Tap white capture button
  ↓
Wait 1-3 seconds (processing)
  ↓
✅ See diagnosis results!
```

#### 4. View Results (1 min)
- Disease name (e.g., "Cotton Leaf Curl")
- Confidence score (e.g., 87%)
- Top 3 predictions
- Treatment recommendations

#### 5. Check History (30 sec)
```
Back to Home
  ↓
Tap "History"
  ↓
See all your diagnoses
  ↓
Tap any diagnosis to view details
```

---

## 📱 Demo Accounts (Pre-configured Examples)

Since authentication is in-memory, here are some example accounts you can create:

### Farmer 1 - Cotton Farmer
- **Name:** Rajesh Kumar
- **Phone:** 9876543210
- **Location:** Delhi, India
- **Password:** password123

### Farmer 2 - Wheat Farmer
- **Name:** Priya Sharma
- **Phone:** 9876543211
- **Location:** Gurgaon, India
- **Password:** password123

### Farmer 3 - Tomato Farmer
- **Name:** Amit Patel
- **Phone:** 9876543212
- **Location:** Faridabad, India
- **Password:** password123

**Note:** You need to register these accounts first before you can login with them!

---

## 🎯 Testing Different Features

### Test Diagnosis with Different Crops

1. **Cotton:**
   - Select "Cotton" crop type
   - Capture image
   - Expected: Cotton Leaf Curl, Bollworm, or Healthy

2. **Wheat:**
   - Select "Wheat" crop type
   - Capture image
   - Expected: Wheat Rust, Blight, or Healthy

3. **Tomato:**
   - Select "Tomato" crop type
   - Capture image
   - Expected: Late Blight, Leaf Curl, or Healthy

### Test Market Features

```
Home → Tap "Market"
  ↓
Browse products (seeds, fertilizers, tools)
  ↓
Tap any product
  ↓
See seller details, price, ratings
  ↓
Tap "Contact Seller" or "Buy Now"
```

### Test Cold Storage

```
Home → Drawer → (scroll down)
  ↓
Or from Market → "Cold Storage" card
  ↓
See facilities in Delhi NCR
  ↓
Tap any facility
  ↓
See rates, capacity, location
```

### Test Equipment Rental

```
Home → Drawer → (scroll down)
  ↓
Or from Market → "Equipment Rental" card
  ↓
See tractors, sprayers, etc.
  ↓
Tap any equipment
  ↓
See rental rates, specifications
```

---

## 🔄 Reset & Start Fresh

### To Start Over:
1. **Close the app completely** (swipe away from recent apps)
2. **Reopen the app**
3. **All data is cleared** (in-memory authentication)
4. **Register a new account**

### To Keep Data:
- Don't close the app
- Use logout from drawer menu
- Login again with same credentials

---

## 🐛 Troubleshooting

### "User not found" Error
**Problem:** Trying to login without registering first  
**Solution:** Tap "Register" and create an account

### "Phone number already registered"
**Problem:** Trying to register with same phone twice  
**Solution:** Use a different phone number or login instead

### "Passwords do not match"
**Problem:** Password and Confirm Password are different  
**Solution:** Make sure both fields have the same password

### Can't login after closing app
**Problem:** Authentication is in-memory (resets on app close)  
**Solution:** Register a new account each time you restart the app

---

## 💡 Pro Tips

### For Demo Purposes:
1. **Use simple credentials:**
   - Phone: 9876543210
   - Password: password123
   - Easy to remember!

2. **Test multiple crops:**
   - Shows variety of diseases
   - Demonstrates ML capabilities

3. **Check diagnosis history:**
   - Shows data persistence
   - Demonstrates offline capability

4. **Explore all features:**
   - Market, Cold Storage, Equipment
   - Settings, Profile, etc.

### For Development:
1. **Check logs:**
   ```bash
   adb logcat | grep AgriEdge
   ```

2. **Clear app data:**
   ```bash
   adb shell pm clear com.agriedge.link
   ```

3. **Reinstall app:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📋 Quick Reference Card

```
┌─────────────────────────────────────┐
│     AGRIEDGE LINK DEMO GUIDE        │
├─────────────────────────────────────┤
│ REGISTER:                           │
│   Name: Rajesh Kumar                │
│   Phone: 9876543210                 │
│   Location: Delhi, India            │
│   Password: password123             │
├─────────────────────────────────────┤
│ FEATURES:                           │
│   ✓ Diagnose Disease (Camera + ML) │
│   ✓ View History                    │
│   ✓ Browse Market                   │
│   ✓ Find Cold Storage               │
│   ✓ Rent Equipment                  │
│   ✓ View Profile                    │
│   ✓ Change Settings                 │
├─────────────────────────────────────┤
│ CROPS SUPPORTED:                    │
│   • Rice                            │
│   • Wheat                           │
│   • Tomato                          │
│   • Potato                          │
│   • Cotton                          │
│   • Sugarcane                       │
├─────────────────────────────────────┤
│ LANGUAGES:                          │
│   • English                         │
│   • Hindi (हिंदी)                   │
│   • Marathi (मराठी)                 │
└─────────────────────────────────────┘
```

---

## 🎬 Demo Script

### 30-Second Pitch:
"AgriEdge Link helps farmers diagnose crop diseases using AI on their phone - no internet needed. Just point your camera at a leaf, and get instant diagnosis with treatment recommendations. It also connects farmers to buyers, cold storage, and equipment rental."

### 2-Minute Demo:
1. **Show Login/Register** (15 sec)
   - "Easy registration with just phone number"
   
2. **Diagnose Disease** (60 sec)
   - "Select crop type"
   - "Point camera at leaf"
   - "Get instant AI diagnosis"
   - "See confidence score and treatments"

3. **Show History** (15 sec)
   - "All diagnoses saved locally"
   - "Works completely offline"

4. **Browse Market** (30 sec)
   - "Connect directly to buyers"
   - "Find cold storage and equipment"
   - "All in one app"

---

## 📞 Support

### For Questions:
- Check `TESTING_GUIDE.md` for detailed testing
- Check `CAMERA_ML_IMPLEMENTATION_SUMMARY.md` for technical details
- Check `FEATURES_TODO.md` for upcoming features

### For Issues:
- Check app logs: `adb logcat | grep AgriEdge`
- Restart app and try again
- Clear app data and reinstall

---

**Happy Testing! 🌾📱**
