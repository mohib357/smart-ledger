package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.PartyEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WalletEntity
import com.example.data.repository.AccountRepository
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ChartPalette
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ReceivableBlue
import com.example.data.model.DashboardPrefs
import com.example.util.Formatters
import com.example.util.expenseByCategory

@Composable
fun DashboardScreen(
    stats: AccountRepository.DashboardStats,
    wallets: List<WalletEntity>,
    recentTransactions: List<TransactionEntity>,
    topParties: List<PartyEntity>,
    useBengaliDigits: Boolean,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddParty: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToKhata: () -> Unit,
    onNavigateToNominees: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSelectParty: (PartyEntity) -> Unit,
    onLoadSampleData: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToCashBox: () -> Unit = {},
    isPremium: Boolean = false,
    hasActiveCoins: Boolean = false,
    onEarnCoin: () -> Unit = {},
    aiInsight: String = "",
    onRefreshInsight: () -> Unit = {},
    dashboardPrefs: DashboardPrefs = DashboardPrefs()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Net Balance Banner
        item {
            HeroBalanceCard(
                stats = stats,
                useBengaliDigits = useBengaliDigits
            )
        }

        // Dynamic ordered widget sections
        items(dashboardPrefs.widgetOrder) { key ->
            when (key) {
                "quick_actions" -> {
                    if (dashboardPrefs.showQuickActions) {
                        QuickActionsGrid(
                            onAddIncome = onAddIncome,
                            onAddExpense = onAddExpense,
                            onNavigateToKhata = onNavigateToKhata,
                            onNavigateToReports = onNavigateToReports,
                            onNavigateToCalendar = onNavigateToCalendar,
                            onNavigateToProducts = onNavigateToProducts,
                            onNavigateToChat = onNavigateToChat,
                            onNavigateToCashBox = onNavigateToCashBox
                        )
                    }
                }
                "earn_coins" -> {
                    if (dashboardPrefs.showEarnCoins) {
                        EarnCoinsSection(onEarnCoin = onEarnCoin)
                    }
                }
                "nominee_banner" -> {
                    if (dashboardPrefs.showNomineeBanner) {
                        NomineeBannerSection(onNavigateToNominees = onNavigateToNominees)
                    }
                }
                "ai_insight" -> {
                    if (dashboardPrefs.showAiInsight) {
                        AiInsightSection(
                            aiInsight = aiInsight,
                            onRefreshInsight = onRefreshInsight
                        )
                    }
                }
                "chart" -> {
                    if (dashboardPrefs.showChart) {
                        ChartSection(
                            recentTransactions = recentTransactions,
                            useBengaliDigits = useBengaliDigits
                        )
                    }
                }
                "wallets" -> {
                    if (dashboardPrefs.showWallets) {
                        WalletsSection(
                            wallets = wallets,
                            stats = stats,
                            useBengaliDigits = useBengaliDigits
                        )
                    }
                }
                "parties" -> {
                    if (dashboardPrefs.showParties) {
                        PartiesSection(
                            topParties = topParties,
                            useBengaliDigits = useBengaliDigits,
                            onNavigateToKhata = onNavigateToKhata,
                            onSelectParty = onSelectParty,
                            onLoadSampleData = onLoadSampleData
                        )
                    }
                }
                "recent_transactions" -> {
                    if (dashboardPrefs.showRecentTransactions) {
                        RecentTransactionsSection(
                            recentTransactions = recentTransactions,
                            useBengaliDigits = useBengaliDigits,
                            onNavigateToTransactions = onNavigateToTransactions,
                            onLoadSampleData = onLoadSampleData
                        )
                    }
                }
            }
        }

        // Feature shortcuts replacing the 5 individual nav cards
        item {
            FeatureShortcutsGrid(
                onNavigateToChat = onNavigateToChat,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToReports = onNavigateToReports,
                onNavigateToProducts = onNavigateToProducts,
                onNavigateToInvoices = onNavigateToInvoices
            )
        }

        // Monthly Income vs Expense strip
        item {
            MonthlySummaryCard(
                stats = stats,
                useBengaliDigits = useBengaliDigits,
                onViewAll = onNavigateToTransactions
            )
        }

        // Phase 4: Banner Ad (only when not premium and no active coins)
        item {
            if (!isPremium && !hasActiveCoins) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        com.google.android.gms.ads.AdView(ctx).apply {
                            setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                            adUnitId = com.example.util.AdManager.BANNER_AD_UNIT_ID
                            loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                        }
                    },
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EarnCoinsSection(onEarnCoin: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.coin_earn_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.coin_earn_subtitle), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onEarnCoin) {
                Text(stringResource(R.string.coin_earn_button))
            }
        }
    }
}

@Composable
private fun NomineeBannerSection(onNavigateToNominees: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onNavigateToNominees() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_nominee_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.dashboard_nominee_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
            Text(
                text = stringResource(R.string.dashboard_nominee_action),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun AiInsightSection(aiInsight: String, onRefreshInsight: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_insight_title),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = if (aiInsight.isBlank()) stringResource(R.string.ai_insight_loading) else aiInsight,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRefreshInsight) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.ai_insight_refresh)
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    recentTransactions: List<TransactionEntity>,
    useBengaliDigits: Boolean
) {
    val expenseSlices = expenseByCategory(recentTransactions)
    val totalExpense = expenseSlices.sumOf { it.second }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_chart_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (totalExpense <= 0) {
                Text(
                    text = stringResource(R.string.dashboard_chart_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val displaySlices = if (expenseSlices.size <= 6) {
                    expenseSlices
                } else {
                    val top = expenseSlices.take(6)
                    val otherAmt = expenseSlices.drop(6).sumOf { it.second }
                    top + listOf("অন্যান্য" to otherAmt)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        var startAngle = -90f
                        displaySlices.forEachIndexed { i, (_, amt) ->
                            val sweep = (amt / totalExpense * 360f).toFloat()
                            drawArc(
                                color = ChartPalette[i % ChartPalette.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 36.dp.toPx())
                            )
                            startAngle += sweep
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        displaySlices.forEachIndexed { i, (cat, amt) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ChartPalette[i % ChartPalette.size])
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = Formatters.formatTaka(amt, useBengali = useBengaliDigits),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletsSection(
    wallets: List<WalletEntity>,
    stats: AccountRepository.DashboardStats,
    useBengaliDigits: Boolean
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_wallets_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${stringResource(R.string.dashboard_wallets_total)} ${Formatters.formatTaka(stats.totalWalletBalance, useBengali = useBengaliDigits)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(wallets) { wallet ->
                WalletMiniCard(wallet = wallet, useBengaliDigits = useBengaliDigits)
            }
        }
    }
}

@Composable
private fun PartiesSection(
    topParties: List<PartyEntity>,
    useBengaliDigits: Boolean,
    onNavigateToKhata: () -> Unit,
    onSelectParty: (PartyEntity) -> Unit,
    onLoadSampleData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_parties_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_parties_all),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToKhata() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (topParties.isEmpty()) {
            EmptyCard(
                message = stringResource(R.string.dashboard_empty_parties),
                actionText = "নমুনা হিসাব লোড করুন",
                onAction = onLoadSampleData
            )
        } else {
            topParties.take(5).forEach { party ->
                PartySummaryRow(
                    party = party,
                    useBengaliDigits = useBengaliDigits,
                    onClick = { onSelectParty(party) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    recentTransactions: List<TransactionEntity>,
    useBengaliDigits: Boolean,
    onNavigateToTransactions: () -> Unit,
    onLoadSampleData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_recent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_recent_all),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToTransactions() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (recentTransactions.isEmpty()) {
            EmptyCard(
                message = stringResource(R.string.dashboard_empty_transactions),
                actionText = stringResource(R.string.dashboard_load_sample),
                onAction = onLoadSampleData
            )
        } else {
            recentTransactions.take(5).forEach { tx ->
                TransactionMiniRow(
                    tx = tx,
                    useBengaliDigits = useBengaliDigits
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    stats: AccountRepository.DashboardStats,
    useBengaliDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmeraldPrimary,
                            Color(0xFF064E3B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_net_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_live_badge),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = Formatters.formatTaka(stats.netBalance, useBengali = useBengaliDigits),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown: আছে (Assets) vs পাবে (Liabilities)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dashboard_assets_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatTaka(stats.totalAssets, useBengali = useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_liabilities_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatTaka(stats.totalPayable, useBengali = useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onNavigateToKhata: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCashBox: () -> Unit
) {
    val labels = listOf("+আয়", "-ব্যয়", "দেনা/পাওনা", "রিপোর্ট", "ক্যালেন্ডার", "স্টক হিসাব", "এআই সহকারী", "ক্যাশবাক্স")
    val colors = listOf(
        IncomeGreen, ExpenseRed, ReceivableBlue, Color(0xFF7C3AED),
        Color(0xFF0D9488), Color(0xFFF97316), Color(0xFF4F46E5), Color(0xFF6B7280)
    )
    val actions = listOf(
        onAddIncome, onAddExpense, onNavigateToKhata, onNavigateToReports,
        onNavigateToCalendar, onNavigateToProducts, onNavigateToChat, onNavigateToCashBox
    )
    val icons = listOf(
        Icons.Default.ArrowDownward, Icons.Default.ArrowUpward, Icons.Default.AccountBalanceWallet, Icons.Default.BarChart,
        Icons.Default.DateRange, Icons.Default.Inventory2, Icons.Default.AutoAwesome, Icons.Default.Payments
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (rowIndex in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0..3) {
                    val itemIndex = rowIndex * 4 + colIndex
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { actions[itemIndex]() }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icons[itemIndex],
                                contentDescription = null,
                                tint = colors[itemIndex],
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = labels[itemIndex],
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors[itemIndex],
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureShortcutsGrid(
    onNavigateToChat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToInvoices: () -> Unit
) {
    val tileLabels = listOf("এআই সহকারী", "ক্যালেন্ডার", "রিপোর্ট", "স্টক", "চালান")
    val tileIcons = listOf(
        Icons.Default.AutoAwesome, Icons.Default.DateRange, Icons.Default.BarChart,
        Icons.Default.Inventory2, Icons.Default.Receipt
    )
    val tileActions = listOf(onNavigateToChat, onNavigateToCalendar, onNavigateToReports, onNavigateToProducts, onNavigateToInvoices)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0..4) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { tileActions[i]() }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tileIcons[i],
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tileLabels[i],
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
            }
        }
    }
}
@Composable
private fun MonthlySummaryCard(
    stats: AccountRepository.DashboardStats,
    useBengaliDigits: Boolean,
    onViewAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable { onViewAll() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_monthly_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_view_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.dashboard_total_income), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTaka(stats.totalIncome, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }

                // Expense
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.dashboard_total_expense), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTaka(stats.totalExpense, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                // Surplus
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (stats.surplus >= 0) IncomeGreen else ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.dashboard_surplus_label), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTaka(stats.surplus, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (stats.surplus >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletMiniCard(
    wallet: WalletEntity,
    useBengaliDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .width(140.dp)
            .height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                val icon = when (wallet.iconType) {
                    "CASH" -> Icons.Default.Payments
                    "BKASH", "NAGAD", "ROCKET" -> Icons.Default.PhoneAndroid
                    "BANK" -> Icons.Default.AccountBalance
                    else -> Icons.Default.CreditCard
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = Formatters.formatTaka(wallet.balance, useBengali = useBengaliDigits),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PartySummaryRow(
    party: PartyEntity,
    useBengaliDigits: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (party.partyType == "PAYABLE") stringResource(R.string.dashboard_party_payable_label) else stringResource(R.string.dashboard_party_receivable_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (party.partyType == "PAYABLE") PayableAmber else ReceivableBlue
                )
            }

            Text(
                text = Formatters.formatTaka(party.currentAmount, useBengali = useBengaliDigits),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (party.partyType == "PAYABLE") ExpenseRed else IncomeGreen
            )
        }
    }
}

@Composable
private fun TransactionMiniRow(
    tx: TransactionEntity,
    useBengaliDigits: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (tx.type == "INCOME") IncomeGreenLight else ExpenseRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "INCOME") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (tx.type == "INCOME") IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${tx.walletName} • ${Formatters.formatDate(tx.dateMillis, useBengali = useBengaliDigits)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = Formatters.formatTaka(tx.amount, showSign = true, useBengali = useBengaliDigits),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun EmptyCard(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(actionText)
            }
        }
    }
}
