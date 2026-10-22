package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NomineeEntity
import com.example.data.model.DashboardPrefs
import com.example.R
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.AddNomineeDialog
import com.example.ui.components.AddPartyDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.AppTopBar
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.CustomizeDashboardDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.CreateInvoiceDialog
import com.example.ui.components.SmsMarketingDialog
import com.example.ui.components.PartyPaymentDialog
import com.example.ui.components.PinConfirmDialog
import com.example.ui.components.QuickCalculatorDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TransferDialog
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CashBoxScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HelpGuideScreen
import com.example.ui.screens.KhataScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.NomineesScreen
import com.example.ui.screens.PartyDetailScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ThemeMode
import com.example.ui.viewmodel.ExcelExportResult
import com.example.data.cloud.AuthState
import com.example.data.cloud.BackupStatus
import com.example.util.Formatters
import com.example.util.NotificationHelper
import com.example.util.PdfExportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val tag = prefs.getString("app_locale", "bn") ?: "bn"
        if (tag == "system") {
            super.attachBaseContext(newBase)
        } else {
            val locale = Locale.forLanguageTag(tag)
            val config = Configuration(newBase.resources.configuration)
            config.setLocales(LocaleList(Locale.forLanguageTag(tag)))
            config.setLayoutDirection(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Initialize AdMob
        com.example.util.AdManager.initialize(this)
        com.example.util.AdManager.loadInterstitial(this)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MyApplicationTheme(darkTheme = darkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }

        // Schedule monthly summary on first launch (if enabled)
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("monthly_summary_enabled", true)) {
            viewModel.scheduleOrCancelMonthlySummary(this, true)
        }
        // Schedule expiry check on launch (if enabled)
        if (prefs.getBoolean("expiry_notif_enabled", true)) {
            viewModel.scheduleOrCancelExpiryCheck(this, true)
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current

    // FEAT-001 — ViewModel-driven onboarding gate
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    if (showOnboarding) {
        OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
        return
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val wallets by viewModel.allWallets.collectAsStateWithLifecycle()
    val snapshots by viewModel.allSnapshots.collectAsStateWithLifecycle()
    val nominees by viewModel.allNominees.collectAsStateWithLifecycle()

    val transactionFilter by viewModel.transactionFilter.collectAsStateWithLifecycle()
    val partyFilter by viewModel.partyFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val transactionCategoryFilter by viewModel.transactionCategoryFilter.collectAsStateWithLifecycle()
    val dateFrom by viewModel.transactionDateFrom.collectAsStateWithLifecycle()
    val dateTo by viewModel.transactionDateTo.collectAsStateWithLifecycle()

    val showAddTransaction by viewModel.showAddTransaction.collectAsStateWithLifecycle()
    val addTxInitialType by viewModel.addTransactionInitialType.collectAsStateWithLifecycle()
    val showAddParty by viewModel.showAddParty.collectAsStateWithLifecycle()
    val showAddNominee by viewModel.showAddNominee.collectAsStateWithLifecycle()
    val showBackupRestore by viewModel.showBackupRestore.collectAsStateWithLifecycle()
    val showCalculator by viewModel.showCalculator.collectAsStateWithLifecycle()
    val showTransfer by viewModel.showTransfer.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLocale by viewModel.appLocale.collectAsStateWithLifecycle()

    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val partyEntries by viewModel.selectedPartyEntries.collectAsStateWithLifecycle()
    val showPartyPayment by viewModel.showPartyPayment.collectAsStateWithLifecycle()
    val partyPaymentType by viewModel.partyPaymentType.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val chatErrorMessage by viewModel.chatErrorMessage.collectAsStateWithLifecycle()

    // Phase 2 state
    val showFeedback by viewModel.showFeedback.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()
    val monthlySummaryEnabled by viewModel.monthlySummaryEnabled.collectAsStateWithLifecycle()
    val txnPinEnabled by viewModel.txnPinEnabled.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()

    // Phase 3 state
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()

    // Phase 4 state
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val hasActiveCoins by viewModel.hasActiveCoins.collectAsStateWithLifecycle()
    val showPremiumScreen by viewModel.showPremiumScreen.collectAsStateWithLifecycle()
    val showAddProduct by viewModel.showAddProduct.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()
    val productProfitSummary by viewModel.productProfitSummary.collectAsStateWithLifecycle()
    val showSmsMarketing by viewModel.showSmsMarketing.collectAsStateWithLifecycle()
    val expiryNotifEnabled by viewModel.expiryNotifEnabled.collectAsStateWithLifecycle()

    // Phase 5 auth states
    val showLoginScreen by viewModel.showLoginScreen.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val showProfileScreen by viewModel.showProfileScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Phase 5 cloud backup states
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()

    // Phase 5 sync state
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // Phase 6 AI Insights
    val aiInsight by viewModel.aiInsight.collectAsStateWithLifecycle()

    // Phase 6 Dashboard Prefs
    val dashboardPrefs by viewModel.dashboardPrefs.collectAsStateWithLifecycle()
    var showCustomizeDashboard by remember { mutableStateOf(false) }

    // Phase 6 Excel Export
    val excelExportResult by viewModel.excelExportResult.collectAsStateWithLifecycle()

    // FEAT-003: Cash Box state
    val cashBoxEntries by viewModel.cashBoxEntries.collectAsStateWithLifecycle()
    val currentCashBalance by viewModel.currentCashBalance.collectAsStateWithLifecycle()
    val todaySales by viewModel.todaySales.collectAsStateWithLifecycle()
    val todayCashEntries by viewModel.todayCashEntries.collectAsStateWithLifecycle()

    // Notifications
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startSync()
    }

    // Product search query (local to MainActivity)
    var productSearchQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Phase 6: Excel export result handler
    androidx.compose.runtime.LaunchedEffect(excelExportResult) {
        when (val result = excelExportResult) {
            is ExcelExportResult.Success -> {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(android.content.Intent.EXTRA_STREAM, result.uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, ""))
                snackbarHostState.showSnackbar(context.getString(R.string.export_excel_success))
                viewModel.clearExcelExportResult()
            }
            is ExcelExportResult.Error -> {
                snackbarHostState.showSnackbar(context.getString(R.string.export_excel_error))
                viewModel.clearExcelExportResult()
            }
            else -> {}
        }
    }

    // SMS permission
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasSmsPermission = granted }

    // App lock state — starts unlocked if lock is disabled, locked if enabled
    var isUnlocked by rememberSaveable {
        mutableStateOf(
            !context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .getBoolean("app_lock_enabled", false)
        )
    }

    // PIN gate states for TransferDialog and PartyPaymentDialog
    var pendingTransfer by remember { mutableStateOf(false) }
    var pendingPartyPayment by remember { mutableStateOf(false) }

    // Handle back button on secondary views
    if (selectedParty != null) {
        BackHandler {
            viewModel.selectParty(null)
        }
    } else if (currentTab != 0) {
        BackHandler {
            viewModel.setTab(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (selectedParty == null) {
                    AppTopBar(
                        useBengaliDigits = useBengaliDigits,
                        unreadNotificationCount = unreadNotificationCount,
                        onNotificationClick = { viewModel.setTab(13) },
                        onSupportClick = { viewModel.setTab(11) }
                    )
                }
            },
            bottomBar = {
                if (selectedParty == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        val navItems = listOf(
                            Triple(0, stringResource(R.string.nav_dashboard), Icons.Filled.Dashboard to Icons.Outlined.Dashboard),
                            Triple(1, stringResource(R.string.nav_transactions), Icons.Filled.ReceiptLong to Icons.Outlined.ReceiptLong),
                            Triple(2, stringResource(R.string.nav_khata), Icons.Filled.People to Icons.Outlined.People),
                            Triple(3, stringResource(R.string.nav_wallets), Icons.Filled.AccountBalanceWallet to Icons.Outlined.AccountBalanceWallet),
                            Triple(4, stringResource(R.string.nav_menu), Icons.Filled.Menu to Icons.Outlined.Menu)
                        )

                        navItems.forEach { (index, label, icons) ->
                            val isSelected = currentTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setTab(index)
                                    viewModel.setSearchQuery("")
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_$index")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedParty != null) {
                    PartyDetailScreen(
                        party = selectedParty!!,
                        entries = partyEntries,
                        useBengaliDigits = useBengaliDigits,
                        onBack = { viewModel.selectParty(null) },
                        onOpenPayment = { type ->
                            viewModel.partyPaymentType.value = type
                            viewModel.showPartyPayment.value = true
                        },
                        onDeleteParty = { party ->
                            viewModel.deleteParty(party)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("'${party.name}' ${context.getString(R.string.snackbar_party_deleted)}")
                            }
                        },
                        onExportReport = { android.widget.Toast.makeText(context, "শীঘ্রই আসছে", android.widget.Toast.LENGTH_SHORT).show() }
                    )
                } else {
                    when (currentTab) {
                        0 -> DashboardScreen(
                            stats = stats,
                            wallets = wallets,
                            recentTransactions = transactions,
                            topParties = parties,
                            useBengaliDigits = useBengaliDigits,
                            onAddIncome = {
                                if (viewModel.checkCanAddTransaction()) {
                                    viewModel.openAddTransaction("INCOME")
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.gate_transaction_limit))
                                        viewModel.showPremiumScreen.value = true
                                    }
                                }
                            },
                            onAddExpense = {
                                if (viewModel.checkCanAddTransaction()) {
                                    viewModel.openAddTransaction("EXPENSE")
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.gate_transaction_limit))
                                        viewModel.showPremiumScreen.value = true
                                    }
                                }
                            },
                            onAddParty = { viewModel.showAddParty.value = true },
                            onNavigateToTransactions = { viewModel.setTab(1) },
                            onNavigateToKhata = { viewModel.setTab(2) },
                            onNavigateToNominees = { viewModel.setTab(14) },
                            onOpenBackupRestore = { viewModel.showBackupRestore.value = true },
                            onNavigateToChat = { viewModel.setTab(5) },
                            onNavigateToCalendar = { viewModel.setTab(6) },
                            onNavigateToReports = { viewModel.setTab(7) },
                            onSelectParty = { viewModel.selectParty(it) },
                            onLoadSampleData = {
                                viewModel.loadSampleData()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.snackbar_sample_loaded))
                                }
                            },
                            onNavigateToProducts = { viewModel.setTab(8) },
                            onNavigateToInvoices = { viewModel.setTab(9) },
                            onNavigateToCashBox = { viewModel.setTab(10) },
                            isPremium = isPremium,
                            hasActiveCoins = hasActiveCoins,
                            onEarnCoin = {
                                val activity = context as? android.app.Activity
                                if (activity != null) {
                                    com.example.util.AdManager.loadRewarded(context) { ad ->
                                        ad.show(activity) { viewModel.earnCoin() }
                                    }
                                }
                            },
                            aiInsight = aiInsight,
                            onRefreshInsight = { viewModel.refreshAiInsight() },
                            dashboardPrefs = dashboardPrefs
                        )
                        1 -> TransactionsScreen(
                            transactions = transactions,
                            filter = transactionFilter,
                            searchQuery = searchQuery,
                            useBengaliDigits = useBengaliDigits,
                            categoryFilter = transactionCategoryFilter,
                            dateFrom = dateFrom,
                            dateTo = dateTo,
                            onFilterChange = { viewModel.setTransactionFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onCategoryFilterChange = { viewModel.setTransactionCategoryFilter(it) },
                            onDateFromChange = { newFrom ->
                                val clampedTo = dateTo
                                if (clampedTo != null && newFrom != null && newFrom > clampedTo) {
                                    viewModel.setTransactionDateFrom(clampedTo)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.snackbar_date_range_error))
                                    }
                                } else {
                                    viewModel.setTransactionDateFrom(newFrom)
                                }
                            },
                            onDateToChange = { newTo ->
                                val clampedFrom = dateFrom
                                if (clampedFrom != null && newTo != null && newTo < clampedFrom) {
                                    viewModel.setTransactionDateTo(clampedFrom)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.snackbar_date_range_error))
                                    }
                                } else {
                                    viewModel.setTransactionDateTo(newTo)
                                }
                            },
                            onAddTransaction = { type ->
                                if (viewModel.checkCanAddTransaction()) {
                                    viewModel.openAddTransaction(type)
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.gate_transaction_limit))
                                        viewModel.showPremiumScreen.value = true
                                    }
                                }
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.snackbar_transaction_deleted))
                                }
                            }
                        )
                        2 -> KhataScreen(
                            parties = parties,
                            filter = partyFilter,
                            searchQuery = searchQuery,
                            useBengaliDigits = useBengaliDigits,
                            onFilterChange = { viewModel.setPartyFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onAddParty = { viewModel.showAddParty.value = true },
                            onSelectParty = { viewModel.selectParty(it) },
                            onExportPartyPdf = { android.widget.Toast.makeText(context, "শীঘ্রই আসছে", android.widget.Toast.LENGTH_SHORT).show() }
                        )
                        3 -> WalletsScreen(
                            wallets = wallets,
                            snapshots = snapshots,
                            stats = stats,
                            useBengaliDigits = useBengaliDigits,
                            onOpenTransfer = { viewModel.showTransfer.value = true },
                            onSaveSnapshot = {
                                viewModel.saveTodaySnapshot()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.snackbar_snapshot_saved))
                                }
                            }
                        )
                        4 -> MenuScreen(viewModel = viewModel, onNavigateToHelp = { viewModel.setTab(15) })
                        5 -> ChatScreen(
                            messages = chatMessages,
                            isLoading = isChatLoading,
                            errorMessage = chatErrorMessage,
                            useBengaliDigits = useBengaliDigits,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onClearChat = { viewModel.clearChat() }
                        )
                        6 -> CalendarScreen(
                            transactions = transactions,
                            useBengaliDigits = useBengaliDigits
                        )
                        7 -> ReportsScreen(
                            transactions = transactions,
                            useBengaliDigits = useBengaliDigits,
                            productProfitSummary = productProfitSummary,
                            products = allProducts,
                            onExportPdf = { fromMillis, toMillis ->
                                coroutineScope.launch {
                                    when (val result = viewModel.exportReportPdf(fromMillis, toMillis)) {
                                        is PdfExportResult.Success -> {
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(android.content.Intent.EXTRA_STREAM, result.uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            try {
                                                withContext(Dispatchers.Main) {
                                                    context.startActivity(
                                                        android.content.Intent.createChooser(shareIntent, context.getString(R.string.action_share))
                                                    )
                                                }
                                            } catch (e: android.content.ActivityNotFoundException) {
                                                android.util.Log.w("MainActivity", "No share app found", e)
                                                snackbarHostState.showSnackbar(context.getString(R.string.snackbar_no_share_app))
                                            }
                                        }
                                        is PdfExportResult.Error -> {
                                            snackbarHostState.showSnackbar(result.message)
                                        }
                                    }
                                }
                            },
                            isPremium = isPremium,
                            onPremiumRequired = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.gate_pdf_premium))
                                    viewModel.showPremiumScreen.value = true
                                }
                            }
                        )
                        8 -> ProductsScreen(
                            products = allProducts,
                            searchQuery = productSearchQuery,
                            onSearchChange = { productSearchQuery = it },
                            onAdd = {
                                viewModel.editingProduct.value = null
                                viewModel.showAddProduct.value = true
                            },
                            onEdit = { product ->
                                viewModel.editingProduct.value = product
                                viewModel.showAddProduct.value = true
                            },
                            onDelete = { product ->
                                viewModel.deleteProduct(product)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.products_deleted))
                                }
                            }
                        )
                        9 -> InvoicesScreen(
                            invoices = allInvoices,
                            useBengaliDigits = useBengaliDigits,
                            onCreateInvoice = { viewModel.showCreateInvoice.value = true },
                            onDeleteInvoice = { invoice ->
                                viewModel.deleteInvoice(invoice)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.snackbar_invoice_deleted))
                                }
                            }
                        )
                        10 -> CashBoxScreen(
                            cashEntries = todayCashEntries,
                            todaySales = todaySales,
                            currentCashBalance = currentCashBalance,
                            useBengaliDigits = useBengaliDigits,
                            onAddEntry = { type, amount, note ->
                                viewModel.addCashBoxEntry(type, amount, note)
                            }
                        )
                        11 -> SupportScreen(viewModel = viewModel)
                        13 -> NotificationsScreen(viewModel = viewModel)
                        14 -> NomineesScreen(
                            nominees = nominees,
                            stats = stats,
                            parties = parties,
                            useBengaliDigits = useBengaliDigits,
                            onAddNominee = { viewModel.showAddNominee.value = true },
                            onDeleteNominee = { nom ->
                                viewModel.deleteNominee(nom)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("'${nom.name}' ${context.getString(R.string.nominees_delete_confirm)}")
                                }
                            }
                        )
                        15 -> HelpGuideScreen(onBack = { viewModel.setTab(4) })
                    }
                }
            }
        }

        // Modal Dialogs
        if (showAddTransaction) {
            AddTransactionDialog(
                initialType = addTxInitialType,
                wallets = wallets,
                useBengaliDigits = useBengaliDigits,
                onDismiss = { viewModel.showAddTransaction.value = false },
                onSave = { type, title, amount, category, walletId, note ->
                    viewModel.addTransaction(type, title, amount, category, walletId, System.currentTimeMillis(), note)
                    coroutineScope.launch {
                        val msg = if (type == "INCOME") context.getString(R.string.snackbar_income_added) else context.getString(R.string.snackbar_expense_added)
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            )
        }

        if (showAddParty) {
            AddPartyDialog(
                initialType = "PAYABLE",
                onDismiss = { viewModel.showAddParty.value = false },
                onSave = { name, phone, partyType, amount, note ->
                    viewModel.addParty(name, phone, partyType, amount, note)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("$name ${context.getString(R.string.snackbar_nominee_added)}")
                    }
                }
            )
        }

        if (showAddNominee) {
            AddNomineeDialog(
                onDismiss = { viewModel.showAddNominee.value = false },
                onSave = { name, relation, phone, email, accessScope, canViewCash, note ->
                    viewModel.addNominee(name, relation, phone, email, accessScope, canViewCash, note)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("'$name' ($relation) ${context.getString(R.string.snackbar_nominee_added)}")
                    }
                }
            )
        }

        if (showBackupRestore) {
            BackupRestoreDialog(
                onDismiss = { viewModel.showBackupRestore.value = false },
                onExportJson = { viewModel.exportJsonBackup() },
                onRestoreJson = { json ->
                    viewModel.restoreJsonBackup(json)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.snackbar_backup_restored))
                    }
                },
                onExportCsv = { viewModel.exportCsvReport() },
                onExportExcel = { viewModel.exportExcel(context) }
            )
        }

        // F4: Party payment with optional PIN gate
        if (showPartyPayment && selectedParty != null) {
            if (pendingPartyPayment || !viewModel.isTxnPinEnabled(context)) {
                PartyPaymentDialog(
                    party = selectedParty!!,
                    initialType = partyPaymentType,
                    wallets = wallets,
                    useBengaliDigits = useBengaliDigits,
                    onDismiss = {
                        viewModel.showPartyPayment.value = false
                        pendingPartyPayment = false
                    },
                    onSave = { amount, walletId, note ->
                        viewModel.recordPartyPayment(amount, walletId, note)
                        pendingPartyPayment = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.snackbar_payment_done))
                        }
                    }
                )
            } else {
                PinConfirmDialog(
                    onPinCorrect = { pendingPartyPayment = true },
                    onDismiss = { viewModel.showPartyPayment.value = false }
                )
            }
        }

        // F4: Transfer with optional PIN gate
        if (showTransfer) {
            if (pendingTransfer || !viewModel.isTxnPinEnabled(context)) {
                TransferDialog(
                    wallets = wallets,
                    useBengaliDigits = useBengaliDigits,
                    onDismiss = {
                        viewModel.showTransfer.value = false
                        pendingTransfer = false
                    },
                    onTransfer = { fromId, toId, amount ->
                        viewModel.transferBetweenWallets(fromId, toId, amount)
                        pendingTransfer = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.snackbar_transfer_done))
                        }
                    }
                )
            } else {
                PinConfirmDialog(
                    onPinCorrect = { pendingTransfer = true },
                    onDismiss = { viewModel.showTransfer.value = false }
                )
            }
        }

        if (showCalculator) {
            QuickCalculatorDialog(
                useBengaliDigits = useBengaliDigits,
                onDismiss = { viewModel.showCalculator.value = false },
                onUseResult = { result ->
                    if (result > 0) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("${context.getString(R.string.dialog_calculator_title)}: ${Formatters.formatTaka(result, useBengali = useBengaliDigits)}")
                        }
                    }
                }
            )
        }

        // Phase 3: F1 — Add/Edit Product Dialog
        if (showAddProduct || editingProduct != null) {
            AddEditProductDialog(
                existingProduct = editingProduct,
                onDismiss = {
                    viewModel.showAddProduct.value = false
                    viewModel.editingProduct.value = null
                },
                onSave = { name, stock, buyPrice, sellPrice, expiryDate, category ->
                    if (editingProduct != null) {
                        viewModel.updateProduct(
                            editingProduct!!.copy(
                                name = name,
                                stock = stock,
                                buyPrice = buyPrice,
                                sellPrice = sellPrice,
                                expiryDate = expiryDate,
                                category = category
                            )
                        )
                    } else {
                        viewModel.addProduct(name, stock, buyPrice, sellPrice, expiryDate, category)
                    }
                    viewModel.showAddProduct.value = false
                    viewModel.editingProduct.value = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.products_saved))
                    }
                }
            )
        }

        if (showSettings) {
            SettingsDialog(
                themeMode = themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                useBengaliDigits = useBengaliDigits,
                onToggleDigits = { viewModel.toggleBengaliDigits() },
                appLocale = appLocale,
                onLocaleChange = { tag -> viewModel.setAppLocale(tag) },
                onDismiss = { viewModel.showSettings.value = false },
                // Profile
                onOpenProfile = {
                    viewModel.showSettings.value = false
                    viewModel.showProfileScreen.value = true
                },
                // F5
                onOpenFeedback = { viewModel.showFeedback.value = true },
                // Phase 4
                onGoPremium = { viewModel.showPremiumScreen.value = true },
                // F2
                reminderEnabled = reminderEnabled,
                onReminderEnabledChange = { enabled ->
                    viewModel.setReminderEnabled(context, enabled)
                    viewModel.scheduleOrCancelDailyReminder(context, enabled, reminderHour, reminderMinute)
                },
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                onReminderTimeChange = { h, m ->
                    viewModel.setReminderTime(context, h, m)
                    if (reminderEnabled) {
                        viewModel.scheduleOrCancelDailyReminder(context, true, h, m)
                    }
                },
                // F3
                monthlySummaryEnabled = monthlySummaryEnabled,
                onMonthlySummaryEnabledChange = { enabled ->
                    viewModel.setMonthlySummaryEnabled(context, enabled)
                    viewModel.scheduleOrCancelMonthlySummary(context, enabled)
                },
                // F4
                txnPinEnabled = txnPinEnabled,
                onTxnPinEnabledChange = { enabled ->
                    if (!enabled) {
                        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                            .edit().remove("txn_pin").apply()
                    }
                    viewModel.setTxnPinEnabled(context, enabled)
                },
                onTxnPinSetup = { pin -> viewModel.saveTxnPin(context, pin) },
                // F1
                appLockEnabled = appLockEnabled,
                onAppLockEnabledChange = { enabled ->
                    if (!enabled) {
                        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                            .edit().remove("app_pin").apply()
                    }
                    viewModel.setAppLockEnabled(context, enabled)
                },
                onAppLockPinSetup = { pin -> viewModel.saveAppPin(context, pin) },
                // Phase 3: F2
                expiryNotifEnabled = expiryNotifEnabled,
                onExpiryNotifEnabledChange = { enabled ->
                    viewModel.setExpiryNotifEnabled(context, enabled)
                    viewModel.scheduleOrCancelExpiryCheck(context, enabled)
                },
                // Phase 5 — Cloud Backup
                backupStatus = backupStatus,
                lastBackupTime = lastBackupTime,
                onManualBackup = { viewModel.performManualBackup() },
                onCloudRestore = { viewModel.performCloudRestore() },
                onCloudBackupToggle = { viewModel.setCloudBackupEnabled(it, context) },
                isCloudBackupEnabled = viewModel.isCloudBackupEnabled,
                isPremium = isPremium,
                onManualSync = { viewModel.triggerManualSync() },
                syncStatus = syncStatus,
                onOpenCustomizeDashboard = { showCustomizeDashboard = true }
            )
        }

        // Phase 4: Premium Screen
        if (showPremiumScreen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { viewModel.showPremiumScreen.value = false }
            ) {
                PremiumScreen(
                    isPremium = isPremium,
                    onDismiss = { viewModel.showPremiumScreen.value = false },
                    onPurchase = { _ ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.premium_purchase_success))
                        }
                        viewModel.showPremiumScreen.value = false
                    }
                )
            }
        }

        // Phase 3 F3: Create Invoice dialog
        val showCreateInvoice by viewModel.showCreateInvoice.collectAsStateWithLifecycle()
        if (showCreateInvoice) {
            CreateInvoiceDialog(
                products = allProducts,
                useBengaliDigits = useBengaliDigits,
                onDismiss = { viewModel.showCreateInvoice.value = false },
                onSave = { customerName, customerPhone, items, note ->
                    viewModel.createInvoice(customerName, customerPhone, items, note)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.snackbar_invoice_saved))
                    }
                }
            )
        }

        // Phase 3 F4: SMS Marketing dialog
        if (showSmsMarketing) {
            SmsMarketingDialog(
                parties = parties,
                onDismiss = { viewModel.showSmsMarketing.value = false },
                onRequestPermission = { smsPermissionLauncher.launch(Manifest.permission.SEND_SMS) },
                hasSmsPermission = hasSmsPermission,
                onShowSnackbar = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        }

        // F5: Feedback dialog
        if (showFeedback) {
            FeedbackDialog(
                onDismiss = { viewModel.showFeedback.value = false },
                onNoEmailApp = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.feedback_no_email_app))
                    }
                }
            )
        }

        // Phase 6: Customize Dashboard dialog
        if (showCustomizeDashboard) {
            CustomizeDashboardDialog(
                current = dashboardPrefs,
                onSave = { viewModel.updateDashboardPrefs(it) },
                onDismiss = { showCustomizeDashboard = false }
            )
        }

        // F1: App Lock overlay
        AnimatedVisibility(
            visible = appLockEnabled && !isUnlocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppLockScreen(onUnlocked = { isUnlocked = true })
        }

        // Phase 5: LoginScreen overlay — show if user explicitly opens it
        if (showLoginScreen) {
            LoginScreen(
                isLoading = authLoading,
                errorMessage = authError,
                onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                onSignUp = { name, email, pass -> viewModel.signUp(name, email, pass) },
                onForgotPassword = { email -> viewModel.forgotPassword(email) },
                onSkip = { viewModel.showLoginScreen.value = false }
            )
        }

        // Phase 5: ProfileScreen overlay
        if (showProfileScreen) {
            ProfileScreen(
                userProfile = userProfile,
                isLoading = authLoading,
                onUpdateProfile = { name, phone -> viewModel.updateUserProfile(name, phone) },
                onLogout = {
                    viewModel.signOut()
                    viewModel.showProfileScreen.value = false
                },
                onDeleteAccount = { viewModel.deleteAccount() },
                onDismiss = { viewModel.showProfileScreen.value = false }
            )
        }
    }
}
