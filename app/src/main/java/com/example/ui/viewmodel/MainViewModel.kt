package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AuditSnapshotEntity
import com.example.data.model.CashBoxEntryEntity
import com.example.data.model.NomineeEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyLedgerEntryEntity
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WalletEntity
import com.example.data.repository.AccountRepository
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiService
import com.example.data.ai.MessageSender
import com.example.data.cloud.AuthState
import com.example.data.cloud.BackupStatus
import com.example.data.cloud.CloudBackupManager
import com.example.data.cloud.FirebaseAuthManager
import com.example.data.cloud.UserProfile
import com.example.data.model.DashboardPrefs
import com.example.util.Formatters
import com.example.util.PdfExporter
import com.example.util.PdfExportResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode { SYSTEM, LIGHT, DARK }

sealed class ExcelExportResult {
    data class Success(val uri: android.net.Uri) : ExcelExportResult()
    data class Error(val message: String) : ExcelExportResult()
    object Idle : ExcelExportResult()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountRepository

    val allTransactions: StateFlow<List<TransactionEntity>>
    val allParties: StateFlow<List<PartyEntity>>
    val allWallets: StateFlow<List<WalletEntity>>
    val allSnapshots: StateFlow<List<AuditSnapshotEntity>>
    val allNominees: StateFlow<List<NomineeEntity>>
    val dashboardStats: StateFlow<AccountRepository.DashboardStats>
    lateinit var allProducts: StateFlow<List<ProductEntity>>
        private set
    lateinit var productProfitSummary: StateFlow<List<ProductProfitSummary>>
        private set
    lateinit var todaySales: StateFlow<Double>
        private set
    lateinit var todayCashEntries: StateFlow<List<CashBoxEntryEntity>>
        private set
    lateinit var cashBoxEntries: StateFlow<List<CashBoxEntryEntity>>
        private set
    lateinit var currentCashBalance: StateFlow<Double>
        private set
    lateinit var allNotifications: StateFlow<List<NotificationEntity>>
        private set
    lateinit var unreadNotificationCount: StateFlow<Int>
        private set

    // Navigation and Filters
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _useBengaliDigits = MutableStateFlow(true)
    val useBengaliDigits: StateFlow<Boolean> = _useBengaliDigits.asStateFlow()

    private val _transactionFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

    private val _partyFilter = MutableStateFlow("ALL") // "ALL", "PAYABLE", "RECEIVABLE"
    val partyFilter: StateFlow<String> = _partyFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Feature 1 — Category filter
    private val _transactionCategoryFilter = MutableStateFlow("ALL")
    val transactionCategoryFilter: StateFlow<String> = _transactionCategoryFilter.asStateFlow()

    // Feature 6 — Theme mode
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Feature 2 — Date range filter
    private val _transactionDateFrom = MutableStateFlow<Long?>(null)
    val transactionDateFrom: StateFlow<Long?> = _transactionDateFrom.asStateFlow()

    private val _transactionDateTo = MutableStateFlow<Long?>(null)
    val transactionDateTo: StateFlow<Long?> = _transactionDateTo.asStateFlow()

    // Feature 7 — App locale
    private val _appLocale = MutableStateFlow("bn")
    val appLocale: StateFlow<String> = _appLocale.asStateFlow()

    // Dialog states
    val showAddTransaction = MutableStateFlow(false)
    val addTransactionInitialType = MutableStateFlow("EXPENSE")

    val showAddParty = MutableStateFlow(false)
    val showAddNominee = MutableStateFlow(false)
    val showBackupRestore = MutableStateFlow(false)
    val showCalculator = MutableStateFlow(false)
    val showTransfer = MutableStateFlow(false)
    val showSettings = MutableStateFlow(false)

    // Phase 2 dialog state
    val showFeedback = MutableStateFlow(false)

    // Phase 3: F1 — Product/Stock Management dialog states
    val showAddProduct = MutableStateFlow(false)
    val editingProduct = MutableStateFlow<ProductEntity?>(null)

    // Phase 3: F4 — SMS Marketing dialog state
    val showSmsMarketing = MutableStateFlow(false)

    // Phase 4 — Monetization
    val showPremiumScreen = MutableStateFlow(false)
    private val billingManager by lazy { com.example.util.BillingManager(getApplication()) }

    // Phase 5 — Auth
    val firebaseAuthManager by lazy { FirebaseAuthManager(getApplication()) }
    val authState: StateFlow<AuthState> by lazy { firebaseAuthManager.authState }
    val userProfile: StateFlow<UserProfile> by lazy { firebaseAuthManager.userProfile }
    val showLoginScreen = MutableStateFlow(false)
    val showProfileScreen = MutableStateFlow(false)

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    val isPremium: StateFlow<Boolean> by lazy {
        billingManager.isPremiumFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = getApplication<Application>()
                .getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
                .getBoolean("is_premium", false)
        )
    }
    val hasActiveCoins: StateFlow<Boolean> by lazy {
        repository.allCoins.map { repository.getTotalActiveCoins() > 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    }
    private var _actionCount = 0

    data class ProductProfitSummary(
        val product: ProductEntity,
        val profitMargin: Double,
        val profitPerUnit: Double
    )

    // Phase 2: F2 — Daily Reminder
    private val _reminderEnabled = MutableStateFlow(false)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()
    private val _reminderHour = MutableStateFlow(20)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()
    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // Phase 2: F3 — Monthly Summary
    private val _monthlySummaryEnabled = MutableStateFlow(true)
    val monthlySummaryEnabled: StateFlow<Boolean> = _monthlySummaryEnabled.asStateFlow()

    // Phase 2: F4 — Transaction PIN
    private val _txnPinEnabled = MutableStateFlow(false)
    val txnPinEnabled: StateFlow<Boolean> = _txnPinEnabled.asStateFlow()

    // Phase 2: F1 — App Lock
    private val _appLockEnabled = MutableStateFlow(false)
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    // Phase 3: F2 — Expiry Notification
    private val _expiryNotifEnabled = MutableStateFlow(true)
    val expiryNotifEnabled: StateFlow<Boolean> = _expiryNotifEnabled.asStateFlow()

    // Party Detail view
    private val _selectedParty = MutableStateFlow<PartyEntity?>(null)
    val selectedParty: StateFlow<PartyEntity?> = _selectedParty.asStateFlow()

    val showPartyPayment = MutableStateFlow(false)
    val partyPaymentType = MutableStateFlow("GIVE") // "GIVE" or "TAKE"

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPartyEntries: StateFlow<List<PartyLedgerEntryEntity>> = _selectedParty
        .flatMapLatest { party ->
            if (party != null) repository.getPartyEntries(party.id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── Phase 6: Dashboard Prefs, AI Insights & Excel Export ──────────────

    private val _dashboardPrefs = MutableStateFlow(DashboardPrefs())
    val dashboardPrefs: StateFlow<DashboardPrefs> = _dashboardPrefs.asStateFlow()

    private val _aiInsight = MutableStateFlow("")
    val aiInsight: StateFlow<String> = _aiInsight.asStateFlow()

    private val _excelExportResult = MutableStateFlow<ExcelExportResult>(ExcelExportResult.Idle)
    val excelExportResult: StateFlow<ExcelExportResult> = _excelExportResult.asStateFlow()

    fun updateDashboardPrefs(p: DashboardPrefs) {
        _dashboardPrefs.value = p
        getApplication<Application>()
            .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("dashboard_prefs", p.toJson()).apply()
    }

    init {
        val prefs = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Read theme mode
        _themeMode.value = try {
            ThemeMode.valueOf(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
        } catch (_: Exception) { ThemeMode.SYSTEM }

        // Read app locale
        _appLocale.value = prefs.getString("app_locale", "bn") ?: "bn"

        // Phase 2 prefs
        _reminderEnabled.value = prefs.getBoolean("reminder_enabled", false)
        _reminderHour.value = prefs.getInt("reminder_hour", 20)
        _reminderMinute.value = prefs.getInt("reminder_minute", 0)
        _monthlySummaryEnabled.value = prefs.getBoolean("monthly_summary_enabled", true)
        _txnPinEnabled.value = prefs.getBoolean("txn_pin_enabled", false)
        _appLockEnabled.value = prefs.getBoolean("app_lock_enabled", false)
        _expiryNotifEnabled.value = prefs.getBoolean("expiry_notif_enabled", true)

        val database = AppDatabase.getDatabase(application)
        repository = AccountRepository(database)

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allParties = repository.allParties.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allWallets = repository.allWallets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSnapshots = repository.allSnapshots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNominees = repository.allNominees.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        dashboardStats = repository.dashboardStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountRepository.DashboardStats()
        )

        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        productProfitSummary = repository.allProducts
            .map { list ->
                list.map { p ->
                    val pu = p.sellPrice - p.buyPrice
                    ProductProfitSummary(p, pu * p.stock, pu)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch {
            repository.initializeDefaultWalletsIfEmpty()
        }
        // Clean expired coins on startup
        viewModelScope.launch {
            repository.cleanExpiredCoins()
        }
        // Start billing connection
        billingManager.startConnection()
        // Load dashboard prefs
        val savedPrefsJson = prefs.getString("dashboard_prefs", null)
        _dashboardPrefs.value = if (savedPrefsJson != null) DashboardPrefs.fromJson(savedPrefsJson) else DashboardPrefs()
        // Load AI insight on startup
        refreshAiInsight()

        // FEAT-003: Cash Box init
        val (startDay, endDay) = todayRange()
        todaySales = repository.getTodaySales(startDay, endDay)
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        todayCashEntries = repository.getTodayEntries(startDay, endDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        cashBoxEntries = repository.getCashBoxEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        currentCashBalance = repository.getCashBalance()
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        allNotifications = repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        unreadNotificationCount = repository.unreadNotificationCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        // Sync notifications when user is authenticated
        viewModelScope.launch {
            val uid = firebaseAuthManager.getCurrentUid()
            repository.syncNotificationsFromFirestore(uid)
        }
    }

    // ── Phase 6: AI Insights ───────────────────────────────────────────────

    fun refreshAiInsight() {
        viewModelScope.launch {
            val stats = dashboardStats.value
            val ctx = "মোট আয়: ${stats.totalIncome}, মোট ব্যয়: ${stats.totalExpense}, নিট স্থিতি: ${stats.netBalance}, মোট পাওনা: ${stats.totalReceivable}, মোট দেনা: ${stats.totalPayable}"
            _aiInsight.value = GeminiService.generateInsight(ctx)
        }
    }

    // ── Phase 6: Excel Export ──────────────────────────────────────────────

    fun exportExcel(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val uri = repository.exportToExcel(context)
                _excelExportResult.value = ExcelExportResult.Success(uri)
            } catch (e: Exception) {
                _excelExportResult.value = ExcelExportResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearExcelExportResult() { _excelExportResult.value = ExcelExportResult.Idle }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun toggleBengaliDigits() {
        _useBengaliDigits.value = !_useBengaliDigits.value
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setPartyFilter(filter: String) {
        _partyFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Feature 1
    fun setTransactionCategoryFilter(cat: String) {
        _transactionCategoryFilter.value = cat
    }

    // Feature 6
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        getApplication<Application>()
            .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("theme_mode", mode.name).apply()
    }

    // Feature 2
    fun setTransactionDateFrom(millis: Long?) {
        _transactionDateFrom.value = millis
    }

    fun setTransactionDateTo(millis: Long?) {
        _transactionDateTo.value = millis
    }

    // Feature 7
    fun setAppLocale(tag: String) {
        _appLocale.value = tag
        getApplication<Application>()
            .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("app_locale", tag).apply()
    }

    fun selectParty(party: PartyEntity?) {
        _selectedParty.value = party
    }

    fun openAddTransaction(initialType: String = "EXPENSE") {
        addTransactionInitialType.value = initialType
        showAddTransaction.value = true
    }

    fun addTransaction(
        type: String,
        title: String,
        amount: Double,
        category: String,
        walletId: Long,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                type = type,
                title = title,
                amount = amount,
                category = category,
                walletId = walletId,
                dateMillis = dateMillis,
                note = note
            )
            showAddTransaction.value = false
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addParty(
        name: String,
        phone: String,
        partyType: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            repository.addParty(name, phone, partyType, amount, note)
            showAddParty.value = false
        }
    }

    fun deleteParty(party: PartyEntity) {
        viewModelScope.launch {
            repository.deleteParty(party)
            if (_selectedParty.value?.id == party.id) {
                _selectedParty.value = null
            }
        }
    }

    fun recordPartyPayment(
        amount: Double,
        walletId: Long?,
        note: String
    ) {
        val party = _selectedParty.value ?: return
        val type = partyPaymentType.value
        viewModelScope.launch {
            repository.recordPartyTransaction(
                party = party,
                entryType = type,
                amount = amount,
                walletId = walletId,
                note = note
            )
            // Refresh party in state
            val updated = repository.allParties.stateIn(viewModelScope).value.find { it.id == party.id }
            if (updated != null) {
                _selectedParty.value = updated
            }
            showPartyPayment.value = false
        }
    }

    fun transferBetweenWallets(fromId: Long, toId: Long, amount: Double) {
        viewModelScope.launch {
            repository.transferBetweenWallets(fromId, toId, amount)
            showTransfer.value = false
        }
    }

    fun saveTodaySnapshot(note: String = "") {
        viewModelScope.launch {
            repository.recordAuditSnapshot(note)
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.prepopulateSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.initializeDefaultWalletsIfEmpty()
            _selectedParty.value = null
        }
    }

    // Phase 3: F1 — Product CRUD
    fun addProduct(
        name: String,
        stock: Int,
        buyPrice: Double,
        sellPrice: Double,
        expiryDate: Long?,
        category: String
    ) {
        viewModelScope.launch {
            repository.addProduct(name, stock, buyPrice, sellPrice, expiryDate, category)
            showAddProduct.value = false
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            editingProduct.value = null
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // ── Invoice StateFlow & functions (Phase 3 F3) ──────────────────────────
    val allInvoices: StateFlow<List<com.example.data.model.InvoiceEntity>> by lazy {
        repository.allInvoices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val showCreateInvoice = MutableStateFlow(false)

    // ── FEAT-003: Cash Box StateFlows ──────────────────────────────────────
    val showAddCashBoxEntry = MutableStateFlow(false)

    fun createInvoice(
        customerName: String,
        customerPhone: String,
        items: List<com.example.data.model.InvoiceItemEntity>,
        note: String
    ) {
        viewModelScope.launch {
            repository.createInvoice(customerName, customerPhone, items, note)
            showCreateInvoice.value = false
        }
    }

    fun deleteInvoice(invoice: com.example.data.model.InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    suspend fun getItemsForInvoice(invoiceId: Long): List<com.example.data.model.InvoiceItemEntity> {
        return repository.getItemsForInvoice(invoiceId)
    }

    // ── Phase 4: Monetization ────────────────────────────────────────────────
    fun earnCoin() {
        viewModelScope.launch { repository.addCoins(1) }
    }

    fun checkCanAddTransaction(): Boolean =
        com.example.util.FeatureGate.canAddTransaction(allTransactions.value.size, isPremium.value)

    fun onActionPerformed(activity: android.app.Activity) {
        _actionCount++
        if (_actionCount >= 5) {
            _actionCount = 0
            if (!isPremium.value && !hasActiveCoins.value) {
                com.example.util.AdManager.showInterstitial(activity) {
                    com.example.util.AdManager.loadInterstitial(activity)
                }
            }
        }
    }

    // ── Phase 5: Firebase Auth ─────────────────────────────────────────────

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = firebaseAuthManager.signIn(email, password)
            result.onFailure { _authError.value = it.message }
            _authLoading.value = false
            if (result.isSuccess) showLoginScreen.value = false
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = firebaseAuthManager.signUp(name, email, password)
            result.onFailure { _authError.value = it.message }
            _authLoading.value = false
            if (result.isSuccess) showLoginScreen.value = false
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            firebaseAuthManager.sendPasswordReset(email)
        }
    }

    fun signOut() {
        firebaseAuthManager.signOut()
    }

    fun updateUserProfile(name: String, phone: String) {
        viewModelScope.launch {
            firebaseAuthManager.updateProfile(name, phone)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            firebaseAuthManager.deleteAccount()
            showProfileScreen.value = false
        }
    }

    // ── Phase 5: Cloud Backup ──────────────────────────────────────────────

    val cloudBackupManager by lazy {
        CloudBackupManager(getApplication(), repository, firebaseAuthManager)
    }
    val backupStatus: StateFlow<BackupStatus> by lazy { cloudBackupManager.backupStatus }
    val lastBackupTime: StateFlow<Long> by lazy { cloudBackupManager.lastBackupTime }

    val isCloudBackupEnabled: Boolean
        get() = cloudBackupManager.isCloudBackupEnabled

    fun setCloudBackupEnabled(enabled: Boolean, context: android.content.Context) {
        cloudBackupManager.isCloudBackupEnabled = enabled
        if (enabled && isPremiumValue()) {
            scheduleAutoBackup(context)
        } else {
            cancelAutoBackup(context)
        }
    }

    fun performManualBackup() {
        viewModelScope.launch {
            cloudBackupManager.performBackup()
        }
    }

    fun performCloudRestore() {
        viewModelScope.launch {
            cloudBackupManager.performRestore()
        }
    }

    private fun isPremiumValue(): Boolean =
        getApplication<Application>().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .getBoolean("is_premium", false)

    private fun scheduleAutoBackup(context: android.content.Context) {
        val request = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.CloudBackupWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        ).addTag(com.example.worker.CloudBackupWorker.TAG).build()
        androidx.work.WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                com.example.worker.CloudBackupWorker.TAG,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    private fun cancelAutoBackup(context: android.content.Context) {
        androidx.work.WorkManager.getInstance(context)
            .cancelUniqueWork(com.example.worker.CloudBackupWorker.TAG)
    }

    override fun onCleared() {
        billingManager.disconnect()
        super.onCleared()
    }

    // Feature 5 — PDF Export
    suspend fun exportReportPdf(fromMillis: Long, toMillis: Long): PdfExportResult {
        val txList = allTransactions.first()
        return PdfExporter.generateTransactionReport(
            context = getApplication<Application>(),
            transactions = txList,
            fromMillis = fromMillis,
            toMillis = toMillis
        )
    }

    // Nominees & Emergency Access Management
    fun uploadNomineeKeys() {
        val uid = firebaseAuthManager.getCurrentUid()
        if (uid.isEmpty()) return
        if (!isPremiumValue()) return
        viewModelScope.launch {
            repository.uploadNomineeAccessKeys(uid)
        }
    }

    fun addNominee(
        name: String,
        relation: String,
        phone: String,
        email: String,
        accessScope: String,
        canViewCash: Boolean,
        note: String
    ) {
        viewModelScope.launch {
            repository.addNominee(
                name = name,
                relation = relation,
                phone = phone,
                email = email,
                accessScope = accessScope,
                canViewCash = canViewCash,
                note = note
            )
            showAddNominee.value = false
            uploadNomineeKeys()
        }
    }

    fun deleteNominee(nominee: NomineeEntity) {
        viewModelScope.launch {
            repository.deleteNominee(nominee)
        }
    }

    // Backup & Restore
    suspend fun exportJsonBackup(): String {
        return repository.exportDatabaseToJson()
    }

    suspend fun exportCsvReport(): String {
        val txCsv = repository.exportTransactionsToCsv()
        val partyCsv = repository.exportPartiesToCsv()
        return "=== লেনদেনের তালিকা ===\n$txCsv\n\n=== দেনা-পাওনা খতিয়ান ===\n$partyCsv"
    }

    fun restoreJsonBackup(jsonString: String) {
        viewModelScope.launch {
            repository.restoreDatabaseFromJson(jsonString)
        }
    }

    // Chat State and Actions
    val chatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id.toString(),
                    sender = if (entity.isFromUser) MessageSender.USER else MessageSender.ASSISTANT,
                    text = entity.content,
                    timestamp = entity.timestamp
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatErrorMessage = MutableStateFlow<String?>(null)
    val chatErrorMessage: StateFlow<String?> = _chatErrorMessage.asStateFlow()

    fun sendChatMessage(userText: String) {
        _isChatLoading.value = true
        _chatErrorMessage.value = null

        viewModelScope.launch {
            val currentHistory = chatMessages.value
            repository.insertChatMessage(userText, true)

            val stats = dashboardStats.value
            val contextSummary = """
মোট ক্যাশ ও জমা: ${Formatters.formatTaka(stats.totalWalletBalance, useBengali = false)}
মোট পাওনা (আমার প্রাপ্য): ${Formatters.formatTaka(stats.totalReceivable, useBengali = false)}
মোট দেনা (অন্যরা পাবে): ${Formatters.formatTaka(stats.totalPayable, useBengali = false)}
বর্তমান নিট স্থিতি: ${Formatters.formatTaka(stats.netBalance, useBengali = false)}
মোট আয়: ${Formatters.formatTaka(stats.totalIncome, useBengali = false)}
মোট ব্যয়: ${Formatters.formatTaka(stats.totalExpense, useBengali = false)}
মোট লাভ/উদ্বৃত্ত: ${Formatters.formatTaka(stats.surplus, useBengali = false)}
            """.trimIndent()

            val result = GeminiService.sendMessage(
                history = currentHistory,
                userMessage = userText,
                contextSummary = contextSummary
            )

            _isChatLoading.value = false
            result.onSuccess { reply ->
                repository.insertChatMessage(reply, false)
            }.onFailure { err ->
                _chatErrorMessage.value = err.message ?: "উত্তরে ত্রুটি দেখা দিয়েছে।"
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
        _chatErrorMessage.value = null
    }

    // ── Notifications ──────────────────────────────────────────────────────

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch { repository.deleteNotification(notification) }
    }

    // ── Phase 2: F5 — Feedback ──────────────────────────────────────────────

    // (showFeedback declared above near dialog states)

    // ── Phase 2: F2 — Daily Reminder ───────────────────────────────────────

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        _reminderEnabled.value = enabled
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("reminder_enabled", enabled).apply()
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
    }

    fun scheduleOrCancelDailyReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork("daily_reminder")
            return
        }
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis
        val request = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.DailyReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS,
            1, java.util.concurrent.TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    // ── Phase 2: F3 — Monthly Summary ──────────────────────────────────────

    fun setMonthlySummaryEnabled(context: Context, enabled: Boolean) {
        _monthlySummaryEnabled.value = enabled
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("monthly_summary_enabled", enabled).apply()
    }

    fun getMonthlySummaryEnabled(context: Context): Boolean =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getBoolean("monthly_summary_enabled", true)

    fun scheduleOrCancelMonthlySummary(context: Context, enabled: Boolean) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork("monthly_summary")
            return
        }
        val request = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.MonthlySummaryWorker>(
            30, java.util.concurrent.TimeUnit.DAYS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "monthly_summary",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    // ── Phase 2: F4 — Transaction PIN ──────────────────────────────────────

    fun setTxnPinEnabled(context: Context, enabled: Boolean) {
        _txnPinEnabled.value = enabled
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("txn_pin_enabled", enabled).apply()
    }

    fun saveTxnPin(context: Context, rawPin: String) {
        val hash = com.example.util.PinHelper.hash(rawPin)
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("txn_pin", hash).apply()
    }

    fun isTxnPinEnabled(context: Context): Boolean =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getBoolean("txn_pin_enabled", false)

    // ── Phase 2: F1 — App Lock ──────────────────────────────────────────────

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        _appLockEnabled.value = enabled
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("app_lock_enabled", enabled).apply()
    }

    fun saveAppPin(context: Context, rawPin: String) {
        val hash = com.example.util.PinHelper.hash(rawPin)
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("app_pin", hash).apply()
    }

    // ── Phase 3: F2 — Expiry Notification ─────────────────────────────────

    fun setExpiryNotifEnabled(context: Context, enabled: Boolean) {
        _expiryNotifEnabled.value = enabled
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("expiry_notif_enabled", enabled).apply()
    }

    fun scheduleOrCancelExpiryCheck(context: Context, enabled: Boolean) {
        val wm = androidx.work.WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork("expiry_check")
            return
        }
        val req = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.ExpiryCheckWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        wm.enqueueUniquePeriodicWork(
            "expiry_check",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    // ── Phase 5: F3 — Multi-device Sync ───────────────────────────────────

    val syncManager by lazy {
        com.example.data.cloud.SyncManager(getApplication(), repository, firebaseAuthManager)
    }
    val syncStatus: StateFlow<com.example.data.cloud.SyncStatus> by lazy { syncManager.syncStatus }

    fun startSync() { syncManager.startSync() }
    fun stopSync() { syncManager.stopSync() }
    fun pushToCloud() {
        viewModelScope.launch { syncManager.pushAllToFirestore() }
    }

    fun triggerManualSync() {
        val uid = firebaseAuthManager.getCurrentUid()
        if (uid.isEmpty()) return
        viewModelScope.launch { syncManager.pushAllToFirestore() }
    }

    // ── FEAT-003: Cash Box helpers ─────────────────────────────────────────

    private fun todayRange(): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 86400000L
        return Pair(start, end)
    }

    fun addCashBoxEntry(type: String, amount: Double, note: String) {
        viewModelScope.launch {
            repository.addCashBoxEntry(CashBoxEntryEntity(type = type, amount = amount, note = note))
        }
    }
}

