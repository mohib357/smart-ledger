package com.example.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.DashboardPrefs

@Composable
fun CustomizeDashboardDialog(
    current: DashboardPrefs,
    onSave: (DashboardPrefs) -> Unit,
    onDismiss: () -> Unit
) {
    var prefs by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_customize_title)) },
        text = {
            LazyColumn {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_quick_actions), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showQuickActions, onCheckedChange = { prefs = prefs.copy(showQuickActions = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_ai_insight), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showAiInsight, onCheckedChange = { prefs = prefs.copy(showAiInsight = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_chart), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showChart, onCheckedChange = { prefs = prefs.copy(showChart = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_wallets), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showWallets, onCheckedChange = { prefs = prefs.copy(showWallets = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_parties), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showParties, onCheckedChange = { prefs = prefs.copy(showParties = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_recent), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showRecentTransactions, onCheckedChange = { prefs = prefs.copy(showRecentTransactions = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_earn_coins), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showEarnCoins, onCheckedChange = { prefs = prefs.copy(showEarnCoins = it) })
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.dashboard_section_nominee), modifier = Modifier.weight(1f))
                        Switch(checked = prefs.showNomineeBanner, onCheckedChange = { prefs = prefs.copy(showNomineeBanner = it) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(prefs); onDismiss() }) {
                Text(stringResource(R.string.dashboard_customize_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
