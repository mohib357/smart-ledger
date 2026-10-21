package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DashboardPrefs

@Composable
fun CustomizeDashboardDialog(
    current: DashboardPrefs,
    onSave: (DashboardPrefs) -> Unit,
    onDismiss: () -> Unit
) {
    val orderedKeys = remember { SnapshotStateList<String>().also { it.addAll(current.widgetOrder) } }
    val toggles = remember {
        SnapshotStateMap<String, Boolean>().also { map ->
            map["quick_actions"] = current.showQuickActions
            map["earn_coins"] = current.showEarnCoins
            map["nominee_banner"] = current.showNomineeBanner
            map["ai_insight"] = current.showAiInsight
            map["chart"] = current.showChart
            map["wallets"] = current.showWallets
            map["parties"] = current.showParties
            map["recent_transactions"] = current.showRecentTransactions
        }
    }

    fun labelRes(key: String): Int = when (key) {
        "quick_actions" -> R.string.dashboard_section_quick_actions
        "earn_coins" -> R.string.dashboard_section_earn_coins
        "nominee_banner" -> R.string.dashboard_section_nominee
        "ai_insight" -> R.string.dashboard_section_ai_insight
        "chart" -> R.string.dashboard_section_chart
        "wallets" -> R.string.dashboard_section_wallets
        "parties" -> R.string.dashboard_section_parties
        "recent_transactions" -> R.string.dashboard_section_recent
        else -> R.string.dashboard_section_quick_actions
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_customize_title)) },
        text = {
            LazyColumn {
                itemsIndexed(orderedKeys) { index, key ->
                    var dragOffsetY by remember { mutableFloatStateOf(0f) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(key) {
                                detectDragGestures(
                                    onDragEnd = { dragOffsetY = 0f },
                                    onDragCancel = { dragOffsetY = 0f }
                                ) { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                    val steps = (dragOffsetY / 56f).toInt()
                                    if (steps != 0) {
                                        val newIndex = (index + steps).coerceIn(0, orderedKeys.size - 1)
                                        if (newIndex != index) {
                                            val item = orderedKeys.removeAt(index)
                                            orderedKeys.add(newIndex, item)
                                            dragOffsetY -= steps * 56f
                                        }
                                    }
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = stringResource(R.string.dashboard_drag_handle_desc),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(labelRes(key)),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = toggles[key] ?: true,
                            onCheckedChange = { toggles[key] = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val saved = DashboardPrefs(
                    showQuickActions = toggles["quick_actions"] ?: true,
                    showEarnCoins = toggles["earn_coins"] ?: true,
                    showNomineeBanner = toggles["nominee_banner"] ?: true,
                    showAiInsight = toggles["ai_insight"] ?: true,
                    showChart = toggles["chart"] ?: true,
                    showWallets = toggles["wallets"] ?: true,
                    showParties = toggles["parties"] ?: true,
                    showRecentTransactions = toggles["recent_transactions"] ?: true,
                    widgetOrder = orderedKeys.toList()
                )
                onSave(saved)
                onDismiss()
            }) {
                Text(stringResource(R.string.dashboard_customize_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
