package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.cloud.SyncStatus
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    useBengaliDigits: Boolean,
    onToggleDigits: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenNominees: () -> Unit,
    onLoadSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onSaveSnapshot: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenSmsMarketing: () -> Unit,
    syncStatus: SyncStatus = SyncStatus.IDLE
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoxBadge(icon = Icons.Default.MenuBook)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.topbar_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = Formatters.formatDateReadable(System.currentTimeMillis(), useBengaliDigits),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Digit toggle pill
                    FilterChip(
                        selected = useBengaliDigits,
                        onClick = onToggleDigits,
                        label = {
                            Text(
                                text = if (useBengaliDigits) stringResource(R.string.topbar_bengali_digits) else stringResource(R.string.topbar_english_digits),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("toggle_digits_chip")
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // Sync status indicator
                    SyncStatusIndicator(syncStatus = syncStatus)

                    // Backup & Sync Quick Icon Button
                    IconButton(
                        onClick = onOpenBackupRestore,
                        modifier = Modifier.testTag("backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = stringResource(R.string.topbar_backup),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Calculator Icon Button
                    IconButton(
                        onClick = onOpenCalculator,
                        modifier = Modifier.testTag("calculator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = stringResource(R.string.topbar_calculator),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overflow Menu
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.topbar_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            text = { Text(stringResource(R.string.topbar_settings)) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_calendar)) },
                            onClick = {
                                showMenu = false
                                onOpenCalendar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_reports)) },
                            onClick = {
                                showMenu = false
                                onOpenReports()
                            }
                        )
                        // Phase 3 items — before Snapshot
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_products)) },
                            onClick = {
                                showMenu = false
                                onOpenProducts()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_invoices)) },
                            onClick = {
                                showMenu = false
                                onOpenInvoices()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_sms_marketing)) },
                            onClick = {
                                showMenu = false
                                onOpenSmsMarketing()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            text = { Text(stringResource(R.string.topbar_backup_menu)) },
                            onClick = {
                                showMenu = false
                                onOpenBackupRestore()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            text = { Text(stringResource(R.string.topbar_nominees_menu)) },
                            onClick = {
                                showMenu = false
                                onOpenNominees()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_snapshot)) },
                            onClick = {
                                showMenu = false
                                onSaveSnapshot()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.topbar_load_sample)) },
                            onClick = {
                                showMenu = false
                                onLoadSampleData()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.topbar_reset),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onClearAllData()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusIndicator(syncStatus: SyncStatus) {
    when (syncStatus) {
        SyncStatus.SYNCING -> {
            val infiniteTransition = rememberInfiniteTransition(label = "sync_rotate")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Syncing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }
        SyncStatus.SYNCED -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Synced",
                tint = Color(0xFF4CAF50), // green
                modifier = Modifier.size(20.dp)
            )
        }
        SyncStatus.ERROR -> {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Sync error",
                tint = Color(0xFFFFC107), // amber
                modifier = Modifier.size(20.dp)
            )
        }
        SyncStatus.IDLE, SyncStatus.OFFLINE -> {
            // No icon shown
        }
    }
}

@Composable
private fun BoxBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
        )
    }
}
