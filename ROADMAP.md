# হিসাব - Smart Ledger: Development Roadmap 🚀

> পুরো প্রজেক্টের implementation plan — phase-wise prompts সহ

---

## 📋 প্রজেক্ট ওভারভিউ

**বর্তমান স্থিতি:**
- ✅ Kotlin + Jetpack Compose + Room (SQLite)
- ✅ আয়-ব্যয় ট্র্যাকার
- ✅ মাল্টি-ওয়ালেট
- ✅ দেনা-পাওনা খাতা (Party/Khata)
- ✅ Nominee/উত্তরাধিকার এক্সেস
- ✅ Gemini AI Chat
- ✅ Backup/Restore (JSON/CSV)
- ✅ বাংলা ফন্ট + সংখ্যা

**লক্ষ্য:** একটি সম্পূর্ণ professional, offline-first, multi-language হিসাব ব্যবস্থাপনা অ্যাপ।

---

## 🎯 Development Phases

### Phase 0: Foundation Fixes    ✅
**কেন:** বর্তমান অ্যাপে critical issues ঠিক করা

**কাজ:**
1. ✅ Migration strategy (fallbackToDestructiveMigration সরানো)
2. ✅ Gemini model name যাচাই (gemini-3.8-flash → সঠিক নাম)
3. ✅ Chat history persist করা (Room-এ)
4. ✅ CSV export security (input escaping)

**Prompt for AI:**
```
আমার Smart Ledger অ্যাপে কিছু critical fix করতে হবে:

1. AppDatabase.kt-এ fallbackToDestructiveMigration() আছে — এটা সরিয়ে proper Migration strategy যোগ করো। বর্তমান schema version 2, পরবর্তী version 3-এর জন্য migration class তৈরি করো যাতে user data কখনো মুছে না যায়।

2. GeminiService.kt-এ model name "gemini-3.8-flash" — এটা verify করো এবং সঠিক Gemini API model name দিয়ে replace করো (২০২৬ সালের standard)।

3. ChatMessage data শুধু memory-তে (StateFlow) — এটাকে Room-এ persist করো। ChatMessageEntity তৈরি করো, ChatMessageDao বানাও, এবং MainViewModel update করো।

4. Formatters.kt-এ CSV export functions (exportTransactionsToCsv, exportPartiesToCsv) — এগুলোতে proper CSV escaping যোগ করো (title/note-এ quote/comma থাকলে যাতে ভাঙে না)।

সব পরিবর্তন করার পর build করে verify করো।
```

---

### Phase 1: UI/UX Polish & Core Features
**কেন:** ব্যবহারকারী অভিজ্ঞতা উন্নত করা

**কাজ:**
1. ✅ Category-wise Transaction (খাবার, ভাড়া, বেতন, লেনদেন ইত্যাদি)
2. ✅ Search + Filter System (তারিখ, ক্যাটাগরি, নাম)
3. ✅ Pie Chart / Bar Chart (আয়-ব্যয় visualization)
4. ✅ Calendar View (মাসিক ট্র্যাকিং)
5. ✅ PDF Export (তারিখ অনুযায়ী রিপোর্ট)
6. ✅ Dark Mode / Light Mode Toggle (already আছে, verify করো)
7. ✅ Multi-language support (বাংলা, ইংরেজি, আরবি, হিন্দি, উর্দু)

**Prompt for AI:**
```
Smart Ledger অ্যাপে নিম্নলিখিত features যোগ করো (Phase 1):

1. **Category System:**
   - TransactionEntity-তে "category" field যোগ করো (Migration 3→4)
   - Predefined categories: খাবার, ভাড়া, বেতন, লেনদেন, স্বাস্থ্য, শিক্ষা, বিনোদন, যাতায়াত, অন্যান্য
   - AddTransactionDialog-এ category dropdown
   - TransactionsScreen-এ category filter chips

2. **Search + Filter:**
   - TransactionsScreen-এ search bar (title/note)
   - Date range picker (from-to)
   - Multi-filter: category + date + type (income/expense)

3. **Charts:**
   - MPAndroidChart বা Compose Charts dependency যোগ
   - DashboardScreen-এ PieChart (category-wise breakdown)
   - নতুন "Reports" tab-এ BarChart (মাসিক trend)

4. **Calendar View:**
   - নতুন "CalendarScreen" তৈরি করো
   - Month view with daily transaction indicators
   - Date click করলে সেই দিনের transactions দেখাবে

5. **PDF Export:**
   - iText or PdfDocument API ব্যবহার
   - Date range সিলেক্ট করে PDF তৈরি
   - Transaction list + summary সহ

6. **Multi-language:**
   - strings.xml-এ সব hardcoded text move করো
   - values-bn, values-ar, values-hi, values-ur folders তৈরি
   - ViewModel-এ locale change করার logic

প্রতিটি feature implement করার পর build ও test করো। Material3 design maintain করো।
```

---

### Phase 2: Security & Notifications (2-3 দিন)
**কেন:** ব্যবহারকারী নিরাপত্তা ও engagement

**কাজ:**
1. ✅ PIN/Fingerprint App Lock (BiometricPrompt API)
2. ✅ Daily Reminder Notification (WorkManager)
3. ✅ Monthly Summary Notification
4. ✅ Transaction PIN (sensitive operations-এর জন্য)
5. ✅ User Feedback System (in-app form)

**Prompt for AI:**
```
Smart Ledger-এ security ও notification features যোগ করো (Phase 2):

1. **App Lock:**
   - SharedPreferences-এ "appLockEnabled" flag
   - Settings screen-এ toggle
   - BiometricPrompt API দিয়ে fingerprint/face unlock
   - PIN fallback (4-6 digit)
   - MainActivity onCreate-এ verification check

2. **Daily Reminder:**
   - WorkManager dependency যোগ
   - Settings-এ reminder time picker
   - Daily notification: "আজকের হিসাব যোগ করুন"
   - Notification click → AddTransactionDialog open

3. **Monthly Summary:**
   - প্রতি মাসের শেষ দিন notification
   - "এই মাসে: আয় ৳X, ব্যয় ৳Y, লাভ ৳Z"
   - Click → Reports screen

4. **Transaction PIN:**
   - Wallet transfer ও party payment-এর আগে PIN চাইবে
   - Settings-এ transaction PIN সেট করা
   - 3 বার ভুল হলে 5 মিনিট lock

5. **Feedback System:**
   - Settings-এ "Send Feedback" button
   - Dialog: rating (1-5 stars) + text
   - Firebase or email intent দিয়ে submit

সব implement করার পর verify করো। UX smooth রাখো।
```

---

### Phase 3: Advanced Business Features (4-6 দিন)
**কেন:** দোকানদারদের জন্য professional tools

**কাজ:**
1. ✅ Product/Stock Management (নাম, স্টক, ক্রয়মূল্য, বিক্রয়মূল্য)
2. ✅ Expiry Date Tracking + Notification
3. ✅ Invoice Generator (receipt/bill)
4. ✅ SMS Marketing (customer reminder)
5. ✅ Profit/Loss Calculation (product-wise)

**Prompt for AI:**
```
Smart Ledger-এ business features যোগ করো (Phase 3):

1. **Product System:**
   - নতুন entity: ProductEntity (id, name, stock, buyPrice, sellPrice, expiryDate, category)
   - ProductDao + CRUD operations
   - নতুন "Products" screen (list + add/edit)
   - Stock in/out tracking (TransactionEntity-তে productId link)

2. **Expiry Notification:**
   - WorkManager দিয়ে daily check
   - 7 দিন আগে warning: "X পণ্যের মেয়াদ শেষ হচ্ছে"
   - Products screen-এ expiring items highlight

3. **Invoice Generator:**
   - নতুন InvoiceEntity (invoice number, customer, items, total)
   - Invoice screen: customer select, product list, quantity
   - PDF generation (company header, item table, signature)
   - Share via Intent

4. **SMS Marketing:**
   - SMS permission যোগ (SEND_SMS)
   - Customer group select (all/payable/receivable)
   - Template message customization
   - Batch send with delay

5. **Profit/Loss:**
   - Repository-তে calculateProductProfit()
   - Reports screen-এ product-wise profit table
   - Time range filter (weekly/monthly/yearly)

প্রতিটি feature test করো। Permission runtime request করো।
```

---

### Phase 4: Monetization (3-4 দিন)
**কেন:** অ্যাপ থেকে revenue generate করা

**কাজ:**
1. ✅ AdMob Integration (Banner + Interstitial + Rewarded)
2. ✅ Coin System (rewarded ad → coins → ad-free period)
3. ✅ Premium Subscription (Google Play Billing)
4. ✅ Feature gating (free vs premium)

**Prompt for AI:**
```
Smart Ledger-এ monetization যোগ করো (Phase 4):

1. **AdMob Setup:**
   - AdMob SDK dependency যোগ
   - AndroidManifest-এ AdMob App ID
   - Banner Ad: DashboardScreen নিচে
   - Interstitial Ad: প্রতি 5টি action পর
   - Rewarded Ad: "Earn Coins" button

2. **Coin System:**
   - CoinEntity (balance, earnedDate, expiryDate)
   - Rewarded ad দেখলে +1 coin
   - 5 coins = 1 day ad-free
   - Countdown timer (SharedPreferences)
   - Ad visibility logic: if (isPremium || hasActiveCoins) hide ads

3. **Google Play Billing:**
   - Billing Library 6+ dependency
   - Products: 1 month (৳99), 3 months (৳249), 6 months (৳449), 1 year (৳799)
   - PurchaseRepository + verification
   - Settings-এ "Go Premium" screen
   - Premium benefits: no ads, cloud backup, export unlimited

4. **Feature Gating:**
   - Free: 50 transactions/month, basic export, ads
   - Premium: unlimited, PDF/Excel, cloud sync, priority support

Test করার জন্য test product IDs ব্যবহার করো।
```

---

### Phase 5: Cloud & Account System (5-7 দিন)
**কেন:** cross-device sync ও data safety

**কাজ:**
1. ✅ Firebase Authentication (Phone/Email + OTP)
2. ✅ Cloud Backup (Firestore or Firebase Storage)
3. ✅ Multi-device Sync
4. ✅ User Profile Management
5. ✅ Nominee Cross-device Access

**Prompt for AI:**
```
Smart Ledger-এ cloud features যোগ করো (Phase 5):

1. **Firebase Setup:**
   - Firebase project তৈরি (google-services.json)
   - Auth, Firestore, Storage enable করো
   - Dependencies যোগ করো

2. **Authentication:**
   - Login/Signup screen (phone/email)
   - OTP verification (FirebaseAuth)
   - User profile: name, phone, email
   - First-time setup wizard

3. **Cloud Backup:**
   - Settings-এ "Cloud Backup" toggle
   - Manual backup button
   - Auto backup (daily, premium only)
   - Backup format: JSON → Firestore collections or Storage file
   - Restore: download → parse → insert to local Room

4. **Multi-device Sync:**
   - Real-time sync (Firestore listeners)
   - Conflict resolution strategy (last-write-wins)
   - Sync indicator UI (syncing/synced/error)

5. **Nominee Cross-device:**
   - Nominee access key → Firestore query
   - Nominee app mode: read-only view (scope-based)
   - Secure token generation ও verification

Security best practices follow করো। Network error handling করো।
```

---

### Phase 6: Advanced UX & Polish (3-4 দিন)
**কেন:** professional-grade user experience

**কাজ:**
1. ✅ Voice Entry (Speech-to-Text)
2. ✅ Customizable Dashboard Widgets
3. ✅ Drag & Drop shortcuts
4. ✅ AI-based Insights (Gemini enhanced)
5. ✅ Export to Excel/CSV (improved)
6. ✅ Onboarding Tutorial

**Prompt for AI:**
```
Smart Ledger-এ advanced UX features যোগ করো (Phase 6):

1. **Voice Entry:**
   - Speech Recognizer API (android.speech)
   - AddTransactionDialog-এ mic button
   - Voice command parsing: "আয় ৫০০ টাকা খাবার"
   - Confirmation dialog before save

2. **Customizable Dashboard:**
   - WidgetPreferences data class (enabled widgets list)
   - Settings-এ "Customize Dashboard" screen
   - Available widgets: quick add, chart, wallet, reminder
   - Grid layout with toggle switches

3. **Drag & Drop:**
   - LazyVerticalGrid with ReorderableList
   - Long-press → drag to reorder widgets
   - Save order to SharedPreferences

4. **AI Insights:**
   - Gemini prompt enhancement: "আপনার গত মাসের ব্যয় ২০% বেড়েছে"
   - DashboardScreen-এ "AI Tips" card
   - Personalized suggestions

5. **Excel Export:**
   - Apache POI library or ExcelKt
   - Multi-sheet: Transactions, Parties, Products
   - Formatting: headers, totals, conditional colors

6. **Onboarding:**
   - First-launch tutorial (ViewPager2 + fragments)
   - Feature highlights: 4-5 screens
   - "Skip" ও "Next" buttons

Visual polish করো। Animations smooth রাখো।
```

---

## 🗄️ Database Strategy

**সিদ্ধান্ত: Room (SQLite) রাখবো**

**কারণ:**
- ✅ Offline-first architecture
- ✅ Fast local queries
- ✅ Privacy (local data)
- ✅ No internet dependency for core features
- ✅ Android standard

**Cloud Sync Strategy (Phase 5):**
- Local: Room → primary source of truth
- Remote: Firestore/Storage → backup + sync
- Sync logic: bidirectional, conflict resolution
- User controls when to sync (manual + auto for premium)

**Migration Plan:**
- Version 2 (current) → Version 3 (chat persist)
- Version 3 → Version 4 (categories)
- Version 4 → Version 5 (products)
- Version 5 → Version 6 (user account)
- প্রতিটি migration-এ addColumn/createTable, data কখনো drop করো না

---

## 📱 Responsive Design Strategy

**Mobile-First Approach:**
- Base: 360dp width (small phones)
- Breakpoints: 600dp (tablet), 840dp (large tablet)
- WindowSizeClass ব্যবহার করো

**Implementation:**
```kotlin
// Responsive grid columns
val columns = when {
    windowWidth < 600.dp -> 1
    windowWidth < 840.dp -> 2
    else -> 3
}
```

**Typography:**
- Small: 12sp, 14sp, 16sp
- Medium: 18sp, 20sp, 24sp
- Large (tablet): +2sp each

**Touch Targets:**
- Minimum 48dp (Material guideline)
- FAB size: 56dp (mobile), 64dp (tablet)

**Orientation:**
- Portrait: stack vertically
- Landscape: two-column layout (list + detail)

---

## 🌐 Offline-First Architecture

**Principles:**
1. সব data local Room-এ primary
2. UI সবসময় local data দেখায়
3. Network operations background-এ (WorkManager)
4. Sync status indicator দেখাবো
5. Conflict resolution transparent

**Network States:**
```
- Online + Synced ✓
- Online + Syncing ⟳
- Offline + Last synced: 2h ago ⚠
- Offline + Never synced ⚠
```

**Sync Strategy:**
- Incremental sync (শুধু পরিবর্তন)
- Timestamp-based (modifiedAt field)
- Retry with exponential backoff
- User-triggered force sync

---

## 🎨 Design System

**Color Palette (current — Material3):**
- Primary: Emerald/Teal (financial theme)
- Light/Dark variants
- Keep existing or update if needed

**Typography:**
- Hind Siliguri (বাংলা)
- Roboto (English, Arabic, Hindi, Urdu)

**Iconography:**
- Material Icons Extended (already আছে)
- Custom icons for wallet types (bKash, Nagad, etc.)

**Spacing:**
- 4dp grid system
- Padding: 8dp, 16dp, 24dp

---

## 🧪 Testing Strategy

**Unit Tests:**
- Repository functions (data logic)
- Formatters (বাংলা সংখ্যা, calculator)
- ViewModel state transformations

**Instrumented Tests:**
- DAO operations (Room)
- Navigation flow
- UI interactions (Compose testing)

**Manual Testing Checklist:**
- [ ] Fresh install → onboarding
- [ ] Add transaction → verify in list
- [ ] Backup → restore → data intact
- [ ] Language change → all text updates
- [ ] Offline → online → sync works
- [ ] Premium purchase → features unlock
- [ ] Nominee access → correct scope

---

## 📦 Release Checklist

**Before Each Phase Release:**
1. [ ] Code cleanup (unused imports, TODOs)
2. [ ] Run unit tests
3. [ ] Build debug + test on emulator
4. [ ] Build release + test on physical device
5. [ ] Update version (versionCode, versionName)
6. [ ] Git commit + tag
7. [ ] Document new features in CHANGELOG.md

**Final Release (Phase 6 শেষে):**
1. [ ] ProGuard rules finalize
2. [ ] App icon + splash screen
3. [ ] Play Store listing (screenshots, description)
4. [ ] Privacy policy + Terms of Service
5. [ ] Beta testing (internal track)
6. [ ] Production release

---

## 📚 Resources & Dependencies

**Core:**
- Kotlin 2.2.10
- Jetpack Compose BOM 2024.09.00
- Room 2.7.0
- Material3

**Phase-specific:**
- Charts: MPAndroidChart or Vico
- PDF: iText or PdfDocument API
- Excel: Apache POI or ExcelKt
- Speech: Android SpeechRecognizer
- Biometric: androidx.biometric
- WorkManager: androidx.work
- AdMob: com.google.android.gms:play-services-ads
- Billing: com.android.billingclient
- Firebase: BoM + auth, firestore, storage

---

## 🚦 Next Steps

1. **Phase 0 শুরু করো** — critical fixes
2. প্রতিটি phase শেষে git commit
3. README.md update করো (completed features)
4. CHANGELOG.md maintain করো

**কাজ শুরুর prompt:**
```
আমি Smart Ledger প্রজেক্টে Phase 0 শুরু করতে চাই। ROADMAP.md অনুযায়ী Phase 0 এর সব কাজ করো। প্রতিটি fix-এর পর build করে verify করবে। সব শেষে একটি summary দিবে যে কী কী করা হয়েছে।
```

---

**তৈরি করেছেন:** AI Assistant  
**শেষ আপডেট:** 2026-10-19  
**Version:** 1.0
