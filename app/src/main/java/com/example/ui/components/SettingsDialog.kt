package com.example.ui.components

import android.app.Activity
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.cloud.BackupStatus
import com.example.ui.viewmodel.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    useBengaliDigits: Boolean,
    onToggleDigits: () -> Unit,
    appLocale: String,
    onLocaleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    // Profile
    onOpenProfile: () -> Unit = {},
    // F5 — Feedback
    onOpenFeedback: () -> Unit = {},
    // Phase 4 — Premium
    onGoPremium: () -> Unit = {},
    // F2 — Daily Reminder
    reminderEnabled: Boolean = false,
    onReminderEnabledChange: (Boolean) -> Unit = {},
    reminderHour: Int = 20,
    reminderMinute: Int = 0,
    onReminderTimeChange: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    // F3 — Monthly Summary
    monthlySummaryEnabled: Boolean = true,
    onMonthlySummaryEnabledChange: (Boolean) -> Unit = {},
    // F4 — Transaction PIN
    txnPinEnabled: Boolean = false,
    onTxnPinEnabledChange: (Boolean) -> Unit = {},
    onTxnPinSetup: (pin: String) -> Unit = {},
    // F1 — App Lock
    appLockEnabled: Boolean = false,
    onAppLockEnabledChange: (Boolean) -> Unit = {},
    onAppLockPinSetup: (pin: String) -> Unit = {},
    // Phase 3: F2 — Expiry Notification
    expiryNotifEnabled: Boolean = true,
    onExpiryNotifEnabledChange: (Boolean) -> Unit = {},
    // Phase 5 — Cloud Backup
    backupStatus: BackupStatus = BackupStatus.IDLE,
    lastBackupTime: Long = 0L,
    onManualBackup: () -> Unit = {},
    onCloudRestore: () -> Unit = {},
    onCloudBackupToggle: (Boolean) -> Unit = {},
    isCloudBackupEnabled: Boolean = false,
    isPremium: Boolean = false,
    // Phase 5 — Sync
    onManualSync: () -> Unit = {},
    syncStatus: com.example.data.cloud.SyncStatus = com.example.data.cloud.SyncStatus.IDLE,
    // Phase 6 — Customize Dashboard
    onOpenCustomizeDashboard: () -> Unit = {}
) {    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile section
                Button(
                    onClick = { onOpenProfile(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_profile_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme section
                Text(
                    text = stringResource(R.string.settings_theme_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                        label = { Text(stringResource(R.string.settings_theme_system), fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                        label = { Text(stringResource(R.string.settings_theme_light), fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeChange(ThemeMode.DARK) },
                        label = { Text(stringResource(R.string.settings_theme_dark), fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Digit toggle section
                Text(
                    text = stringResource(R.string.settings_digits_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = useBengaliDigits,
                        onClick = { if (!useBengaliDigits) onToggleDigits() },
                        label = { Text("বাংলা ০১২৩", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = !useBengaliDigits,
                        onClick = { if (useBengaliDigits) onToggleDigits() },
                        label = { Text("English 0123", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language section
                Text(
                    text = stringResource(R.string.settings_language_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val languages = listOf(
                    "bn" to stringResource(R.string.settings_lang_bengali),
                    "en" to stringResource(R.string.settings_lang_english),
                    "ar" to stringResource(R.string.settings_lang_arabic),
                    "hi" to stringResource(R.string.settings_lang_hindi),
                    "ur" to stringResource(R.string.settings_lang_urdu),
                    "system" to stringResource(R.string.settings_lang_system)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    languages.forEach { (tag, label) ->
                        FilterChip(
                            selected = appLocale == tag,
                            onClick = {
                                if (appLocale != tag) {
                                    onLocaleChange(tag)
                                    (context as? Activity)?.recreate()
                                }
                            },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // ── F5: Feedback section ────────────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_feedback_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onOpenFeedback(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.feedback_button_label))
                }

                // ── Phase 4: Go Premium ──────────────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = { onGoPremium(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_go_premium))
                }

                // ── F2: Daily Reminder section ──────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_reminder_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminder_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = reminderEnabled, onCheckedChange = onReminderEnabledChange)
                }
                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val timeLabel = String.format("%d:%02d", reminderHour, reminderMinute)
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> onReminderTimeChange(h, m) },
                                reminderHour,
                                reminderMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${stringResource(R.string.settings_reminder_time)}: $timeLabel")
                    }
                }

                // ── F3: Monthly Summary section ─────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_monthly_summary_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_monthly_summary_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = monthlySummaryEnabled, onCheckedChange = onMonthlySummaryEnabledChange)
                }

                // ── Phase 3: F2 — Expiry Notification section ──────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_expiry_notif_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_expiry_notif_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = expiryNotifEnabled, onCheckedChange = onExpiryNotifEnabledChange)
                }

                // ── F4: Transaction PIN section ─────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_txn_pin_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_txn_pin_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = txnPinEnabled, onCheckedChange = onTxnPinEnabledChange)
                }
                if (txnPinEnabled) {
                    var pinInput by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text(stringResource(R.string.pin_setup_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinInput.length == 4) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onTxnPinSetup(pinInput); pinInput = "" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_txn_pin_setup))
                        }
                    }
                }

                // ── F1: App Lock section ────────────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_app_lock_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_app_lock_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = appLockEnabled, onCheckedChange = onAppLockEnabledChange)
                }
                if (appLockEnabled) {
                    var appPinInput by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) appPinInput = it },
                        label = { Text(stringResource(R.string.settings_app_lock_pin_setup)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (appPinInput.length in 4..6) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onAppLockPinSetup(appPinInput); appPinInput = "" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_app_lock_pin_setup))
                        }
                    }
                }

                // ── Phase 5: Cloud Backup section ─────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.cloud_backup_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.cloud_backup_toggle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = isCloudBackupEnabled, onCheckedChange = onCloudBackupToggle)
                }

                // Status text
                Spacer(modifier = Modifier.height(4.dp))
                val statusText = when (backupStatus) {
                    BackupStatus.SYNCING -> stringResource(R.string.cloud_backup_status_syncing)
                    BackupStatus.SYNCED -> stringResource(R.string.cloud_backup_status_synced)
                    BackupStatus.ERROR -> stringResource(R.string.cloud_backup_status_error)
                    BackupStatus.IDLE -> if (lastBackupTime > 0L) {
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = lastBackupTime
                        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        val minute = cal.get(java.util.Calendar.MINUTE)
                        "শেষ ব্যাকআপ: %02d:%02d".format(hour, minute)
                    } else {
                        stringResource(R.string.cloud_backup_status_never)
                    }
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (backupStatus) {
                        BackupStatus.SYNCED -> MaterialTheme.colorScheme.primary
                        BackupStatus.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual backup button
                Button(
                    onClick = onManualBackup,
                    enabled = isCloudBackupEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cloud_backup_manual_button))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Restore button
                OutlinedButton(
                    onClick = onCloudRestore,
                    enabled = isCloudBackupEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cloud_backup_restore_button))
                }

                // ── Phase 5: Sync section ────────────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.sync_section_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val syncStatusText = when (syncStatus) {
                    com.example.data.cloud.SyncStatus.SYNCING -> stringResource(R.string.sync_status_syncing)
                    com.example.data.cloud.SyncStatus.SYNCED -> stringResource(R.string.sync_status_synced)
                    com.example.data.cloud.SyncStatus.OFFLINE -> stringResource(R.string.sync_status_offline)
                    com.example.data.cloud.SyncStatus.ERROR -> stringResource(R.string.sync_status_error)
                    com.example.data.cloud.SyncStatus.IDLE -> ""
                }
                if (syncStatusText.isNotEmpty()) {
                    Text(
                        text = syncStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (syncStatus) {
                            com.example.data.cloud.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                            com.example.data.cloud.SyncStatus.ERROR, com.example.data.cloud.SyncStatus.OFFLINE -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = onManualSync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.sync_manual_button))
                }

                // ── Phase 6: Customize Dashboard ────────────────────────────────────
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onOpenCustomizeDashboard(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎨 " + stringResource(R.string.dashboard_customize_title))
                }
            }
        }
    }
}
