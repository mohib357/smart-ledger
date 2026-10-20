# Changelog

All notable changes to Smart Ledger will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned (Phase 6)
- Voice entry
- Advanced UX & Polish
- Widget support

---

## [1.5.0] - 2026-10-20

### Added — Phase 5: Cloud & Account System
- ✅ Firebase Authentication (FirebaseAuthManager, LoginScreen)
- ✅ Firebase Cloud Backup & Restore (CloudBackupManager, CloudBackupWorker)
- ✅ Firestore Real-time Sync (SyncManager)
- ✅ User Profile Screen (ProfileScreen)
- ✅ Nominee cross-device access via Firestore
- ✅ Multi-device data synchronization

---

## [1.4.0] - 2026-10-20

### Added — Phase 4: Monetization
- ✅ AdMob integration (banner, interstitial, rewarded ads — AdManager)
- ✅ Coin System (CoinEntity, CoinDao, MIGRATION_5_6)
- ✅ Google Play Billing (BillingManager — premium subscription)
- ✅ Feature Gating (FeatureGate — free/premium limits)

---

## [1.3.0] - 2026-10-20

### Added — Phase 3: Advanced Business Features
- ✅ Product/Stock management (ProductEntity, ProductDao, MIGRATION_3_4)
- ✅ Expiry tracking with 7-day warning notifications (ExpiryCheckWorker)
- ✅ Invoice system (InvoiceEntity, InvoiceItemEntity, InvoiceDao, MIGRATION_4_5)
- ✅ Invoice PDF Generator (InvoicePdfGenerator, InvoicesScreen, CreateInvoiceDialog)
- ✅ SMS Marketing dialog (SEND_SMS permission, SmsMarketingDialog)
- ✅ Profit/Loss analysis in ReportsScreen (productProfitSummary)

---

## [1.2.0] - 2026-10-20

### Added — Phase 2: Security & Notifications
- ✅ App Lock screen (BiometricPrompt + PIN + SHA-256 — AppLockScreen)
- ✅ Daily reminder notifications (DailyReminderWorker via WorkManager)
- ✅ Monthly summary notifications (MonthlySummaryWorker)
- ✅ Transaction PIN gate (PinConfirmDialog)
- ✅ Feedback dialog (star rating + email intent — FeedbackDialog)

---

## [1.1.0] - 2026-10-20

### Added — Phase 1: UI/UX Polish & Core Features
- ✅ Category system (Categories.kt, FilterChips)
- ✅ Search & filter (date range, search bar)
- ✅ Charts (Canvas donut chart in Dashboard, bar chart in Reports)
- ✅ Calendar Screen
- ✅ PDF Exporter
- ✅ Dark/Light/System theme toggle
- ✅ Multi-language support (Bengali, English, Arabic, Hindi, Urdu — 377 string keys each)

---

## [1.0.0] - 2026-10-19

### Added — Phase 0: Foundation Fixes
- ✅ Room Migration 2→3 (no fallbackToDestructiveMigration)
- ✅ Gemini model name set to `gemini-3.8-flash`
- ✅ ChatMessageEntity/Dao (chat persistence in Room)
- ✅ CSV security (escapeCsvField)

### Core Features (Initial Release)
- ✅ আয়-ব্যয় ট্র্যাকার (Income/Expense tracking)
- ✅ মাল্টি-ওয়ালেট সিস্টেম (Cash, bKash, Nagad, Rocket, Bank)
- ✅ দেনা-পাওনা খাতা (Credit ledger with parties)
- ✅ Party লেজার এন্ট্রি (GIVE/TAKE tracking)
- ✅ Wallet-to-wallet ট্রান্সফার
- ✅ উত্তরাধিকার এক্সেস কন্ট্রোল (Nominee system)
- ✅ Audit snapshot
- ✅ Gemini AI Chat সহকারী
- ✅ Quick calculator (বাংলা সংখ্যা সাপোর্ট)
- ✅ Backup/Restore (JSON + CSV export)
- ✅ Jetpack Compose Material3 design
- ✅ বাংলা ফন্ট (Hind Siliguri)
- ✅ Offline-first Room database

---

**Repository:** https://github.com/mohib357/smart-ledger
