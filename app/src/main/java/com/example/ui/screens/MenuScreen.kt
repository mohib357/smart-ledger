package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLocale by viewModel.appLocale.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Section 1 — Profile Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with initials
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = userProfile.name
                            .split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .joinToString("")
                            .ifEmpty { "?" }
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name.ifEmpty { "অতিথি ব্যবহারকারী" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (userProfile.email.isNotEmpty()) {
                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.showProfileScreen.value = true }
                    ) {
                        Text("প্রোফাইল", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 2 — ভাষা ও প্রদর্শন
        item {
            SectionHeader(stringResource(R.string.menu_language))
        }
        item {
            MenuListItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings_language_section),
                subtitle = localeLabel(appLocale),
                trailing = {
                    Text(
                        "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showLanguageDialog = true }
            )
        }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.menu_bengali_digits)) },
                supportingContent = null,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Switch(
                        checked = useBengaliDigits,
                        onCheckedChange = { viewModel.toggleBengaliDigits() }
                    )
                }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings_theme_section),
                subtitle = themeModeLabel(themeMode),
                trailing = {
                    Text(
                        "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showThemeDialog = true }
            )
        }

        // Section 3 — হিসাব সরঞ্জাম
        item { SectionHeader(stringResource(R.string.menu_tools)) }
        item {
            MenuListItem(
                icon = Icons.Default.DateRange,
                title = stringResource(R.string.menu_calendar),
                onClick = { viewModel.setTab(6) }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.menu_reports),
                onClick = { viewModel.setTab(7) }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Inventory,
                title = stringResource(R.string.menu_products),
                onClick = { viewModel.setTab(8) }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Receipt,
                title = stringResource(R.string.menu_invoices),
                onClick = { viewModel.setTab(9) }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = stringResource(R.string.menu_cashbox),
                onClick = { viewModel.setTab(10) }
            )
        }

        // Section 4 — উত্তরাধিকার ও নিরাপত্তা
        item { SectionHeader(stringResource(R.string.menu_heritage)) }
        item {
            MenuListItem(
                icon = Icons.Default.Security,
                title = stringResource(R.string.menu_nominees),
                onClick = { viewModel.setTab(14) }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.menu_app_lock),
                onClick = { viewModel.showSettings.value = true }
            )
        }

        // Section 5 — ডেটা ও সিঙ্ক
        item { SectionHeader(stringResource(R.string.menu_data_sync)) }
        item {
            MenuListItem(
                icon = Icons.Default.CloudSync,
                title = stringResource(R.string.menu_backup_restore),
                onClick = { viewModel.showBackupRestore.value = true }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Sms,
                title = stringResource(R.string.menu_sms_marketing),
                onClick = { viewModel.showSmsMarketing.value = true }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.CameraAlt,
                title = stringResource(R.string.menu_snapshot),
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveTodaySnapshot()
                    }
                }
            )
        }

        // Section 6 — অ্যাকাউন্ট ও অ্যাপ
        item { SectionHeader(stringResource(R.string.menu_account)) }
        item {
            MenuListItem(
                icon = Icons.Default.Star,
                title = stringResource(R.string.menu_premium),
                onClick = { viewModel.showPremiumScreen.value = true }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.menu_settings),
                onClick = { viewModel.showSettings.value = true }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Person,
                title = stringResource(R.string.menu_login),
                onClick = { viewModel.showLoginScreen.value = true }
            )
        }
        item {
            MenuListItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.menu_data_reset),
                titleColor = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error,
                onClick = { showResetDialog = true }
            )
        }

        // Section 7 — সাহায্য
        item { SectionHeader(stringResource(R.string.menu_help)) }
        item {
            MenuListItem(
                icon = Icons.Default.HeadsetMic,
                title = stringResource(R.string.menu_support_center),
                onClick = { viewModel.setTab(11) }
            )
        }

        // Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ভার্সন ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "হিসাব - Smart Ledger © 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Language picker dialog
    if (showLanguageDialog) {
        val localeOptions = listOf(
            "bn" to "বাংলা",
            "en" to "English",
            "ar" to "العربية",
            "hi" to "हिन्दी",
            "ur" to "اردو",
            "system" to "সিস্টেম"
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("ভাষা নির্বাচন করুন") },
            text = {
                Column {
                    localeOptions.forEach { (tag, label) ->
                        TextButton(
                            onClick = {
                                viewModel.setAppLocale(tag)
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (appLocale == tag) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Theme picker dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("থিম নির্বাচন করুন") },
            text = {
                Column {
                    listOf(
                        ThemeMode.SYSTEM to "সিস্টেম",
                        ThemeMode.LIGHT to "হালকা থিম",
                        ThemeMode.DARK to "গাঢ় থিম"
                    ).forEach { (mode, label) ->
                        TextButton(
                            onClick = {
                                viewModel.setThemeMode(mode)
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Data reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("ডেটা রিসেট") },
            text = { Text("সমস্ত ডেটা মুছে যাবে। এই কাজটি স্থায়ী। আপনি কি নিশ্চিত?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("রিসেট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Divider(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuListItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = titleColor
            )
        },
        supportingContent = if (subtitle != null) {
            { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        },
        trailingContent = trailing,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
}

private fun localeLabel(tag: String): String = when (tag) {
    "bn" -> "বাংলা"
    "en" -> "English"
    "ar" -> "العربية"
    "hi" -> "हिन्दी"
    "ur" -> "اردو"
    "system" -> "সিস্টেম"
    else -> tag
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "সিস্টেম"
    ThemeMode.LIGHT -> "হালকা"
    ThemeMode.DARK -> "গাঢ়"
}
