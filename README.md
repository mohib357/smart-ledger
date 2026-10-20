# হিসাব - Smart Ledger

একটি সম্পূর্ণ **offline-first** বাংলা হিসাব ব্যবস্থাপনা Android অ্যাপ।

## বৈশিষ্ট্য

### Core
- 💰 আয়-ব্যয় ট্র্যাকার (ক্যাটাগরি, সার্চ, ফিল্টার)
- 🏦 মাল্টি-ওয়ালেট (ক্যাশ, বিকাশ, নগদ, রকেট, ব্যাংক)
- 📒 দেনা-পাওনা খাতা (Party/Khata)
- 👥 Nominee/উত্তরাধিকার এক্সেস
- 🤖 Gemini AI Chat সহকারী
- 💾 Backup/Restore (JSON/CSV)
- 📊 Charts (Canvas donut + bar chart)
- 📅 Calendar View
- 📄 PDF Export
- 🌙 Dark/Light/System থিম
- 🌐 Multi-language (বাংলা, English, Arabic, Hindi, Urdu)

### Phase 2 — Security & Notifications
- 🔐 App Lock (Biometric + PIN)
- 🔔 Daily & Monthly Reminders
- 🔏 Transaction PIN Gate
- ⭐ Feedback Dialog

### Phase 3 — Advanced Business Features
- 📦 Product/Stock Management with Expiry Tracking
- 🧾 Invoice Generator (PDF)
- 📱 SMS Marketing
- 📈 Profit/Loss Analysis

### Phase 4 — Monetization
- 💳 AdMob (Banner + Interstitial + Rewarded)
- 🪙 Coin System
- 💎 Google Play Billing (Premium Subscription)
- 🚦 Feature Gating (Free/Premium limits)

### Phase 5 — Cloud & Account System
- 🔑 Firebase Authentication
- ☁️ Firebase Cloud Backup & Sync
- 📲 Multi-device Real-time Sync (Firestore)
- 👤 User Profile Management
- 🔗 Nominee Cross-device Access

## Tech Stack

- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose + Material3
- **Database:** Room 2.7.0 (SQLite, offline-first, DB version 6)
- **AI:** Google Gemini API (`gemini-3.8-flash`)
- **Architecture:** MVVM (ViewModel + Repository + Flow)
- **Backend:** Firebase Auth + Firestore + Cloud Storage
- **Monetization:** AdMob + Google Play Billing
- **Background:** WorkManager (reminders, expiry check, cloud backup)
- **Build:** Android Gradle Plugin 9.4.1, Gradle 9.8.0
- **Version:** 1.5

## Build

```bash
# Android Studio দিয়ে open করুন এবং Run করুন
# অথবা Gradle দিয়ে:
./gradlew assembleDebug
```

## Requirements

- Android 7.0+ (API 24+)
- Target: Android 15 (API 36)

## Development Roadmap

- [x] Phase 0 — Foundation Fixes
- [x] Phase 1 — UI/UX Polish & Core Features
- [x] Phase 2 — Security & Notifications
- [x] Phase 3 — Advanced Business Features
- [x] Phase 4 — Monetization
- [x] Phase 5 — Cloud & Account System
- [ ] Phase 6 — Advanced UX & Polish
