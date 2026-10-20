# Changelog

All notable changes to Smart Ledger will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned Features (See ROADMAP.md)
- Category-wise transactions
- Search & filter system
- Charts (Pie/Bar)
- Calendar view
- PDF export
- Multi-language support (Arabic, Hindi, Urdu)
- App lock (PIN/Fingerprint)
- Daily & monthly reminders
- Product/Stock management
- Invoice generator
- SMS marketing
- Voice entry
- AdMob integration
- Premium subscription
- Cloud backup & sync
- OTP login

---

## [1.0.0] - 2026-10-19

### Current Features ✨

#### Core Functionality
- ✅ আয়-ব্যয় ট্র্যাকার (Income/Expense tracking)
- ✅ মাল্টি-ওয়ালেট সিস্টেম (Cash, bKash, Nagad, Rocket, Bank)
- ✅ দেনা-পাওনা খাতা (Credit ledger with parties)
- ✅ Party লেজার এন্ট্রি (GIVE/TAKE tracking)
- ✅ Wallet-to-wallet ট্রান্সফার

#### Advanced Features
- ✅ উত্তরাধিকার এক্সেস কন্ট্রোল (Nominee system with access scopes)
- ✅ Audit snapshot (আর্থিক স্থিতির স্ন্যাপশট)
- ✅ Gemini AI চ্যাট সহকারী (contextual financial advice)
- ✅ Quick calculator (বাংলা সংখ্যা সাপোর্ট)
- ✅ Backup/Restore (JSON + CSV export)

#### UI/UX
- ✅ Jetpack Compose Material3 design
- ✅ Dark/Light theme
- ✅ বাংলা ফন্ট (Hind Siliguri)
- ✅ বাংলা সংখ্যা টগল
- ✅ Responsive layouts
- ✅ 7 screens + 9 dialogs

#### Database
- ✅ Room database (SQLite)
- ✅ Offline-first architecture
- ✅ 6 entities with relationships
- ✅ Reactive Flow-based queries

### Tech Stack
- Kotlin 2.2.10
- Jetpack Compose BOM 2024.09.00
- Room 2.7.0
- Material3
- Coroutines 1.10.2
- OkHttp 4.10.0 + Moshi
- Firebase AI + App Check
- KSP for annotation processing

### Known Issues 🐛
- Migration strategy uses fallbackToDestructiveMigration (data loss risk)
- Gemini model name "gemini-3.8-flash" needs verification
- Chat history not persisted (memory-only)
- CSV export lacks proper escaping
- No unit/integration tests for DAO/ViewModel
- Nominee cross-device access is prototype only

---

## Version History Template

### [X.Y.Z] - YYYY-MM-DD

#### Added
- New features

#### Changed
- Changes to existing functionality

#### Deprecated
- Soon-to-be removed features

#### Removed
- Removed features

#### Fixed
- Bug fixes

#### Security
- Security improvements

---

**Note:** Version 1.0.0 represents the current state analyzed from the codebase. Future versions will follow the ROADMAP.md phases.
