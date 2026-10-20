# হিসাব - Smart Ledger

একটি সম্পূর্ণ **offline-first** বাংলা হিসাব ব্যবস্থাপনা Android অ্যাপ।

## বৈশিষ্ট্য

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
- 🔔 Daily Reminder & Monthly Summary Notifications *(Phase 2)*
- 🔐 App Lock (Biometric + PIN) *(Phase 2)*

## Tech Stack

- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose + Material3
- **Database:** Room 2.7.0 (SQLite, offline-first)
- **AI:** Google Gemini API (`gemini-3.8-flash`)
- **Architecture:** MVVM (ViewModel + Repository + Flow)
- **Build:** Android Gradle Plugin 9.4.1, Gradle 9.8.0

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
- [ ] Phase 2 — Security & Notifications *(in progress)*
- [ ] Phase 3 — Advanced Business Features
- [ ] Phase 4 — Monetization
- [ ] Phase 5 — Cloud & Account System
- [ ] Phase 6 — Advanced UX & Polish
