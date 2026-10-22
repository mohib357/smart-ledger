package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CashBoxEntryEntity
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ReceivableBlue
import com.example.util.Formatters

private val entryTypes = listOf(
    "CASH_IN", "CASH_OUT", "EXPENSE", "OWNER_IN", "OWNER_OUT"
)

@Composable
private fun entryTypeButtonLabel(key: String): String = when (key) {
    "CASH_IN"   -> stringResource(R.string.cashbox_cash_in)
    "CASH_OUT"  -> stringResource(R.string.cashbox_cash_out)
    "EXPENSE"   -> stringResource(R.string.cashbox_expense)
    "OWNER_IN"  -> stringResource(R.string.cashbox_owner_in)
    "OWNER_OUT" -> stringResource(R.string.cashbox_owner_out)
    else        -> key
}

@Composable
private fun typeLabel(type: String): String = when (type) {
    "CASH_IN"   -> stringResource(R.string.cashbox_type_cash_in)
    "CASH_OUT"  -> stringResource(R.string.cashbox_type_cash_out)
    "EXPENSE"   -> stringResource(R.string.cashbox_expense)
    "OWNER_IN"  -> stringResource(R.string.cashbox_owner_in)
    "OWNER_OUT" -> stringResource(R.string.cashbox_owner_out)
    else        -> type
}

@Composable
fun CashBoxScreen(
    cashEntries: List<CashBoxEntryEntity>,
    todaySales: Double,
    currentCashBalance: Double,
    useBengaliDigits: Boolean,
    onAddEntry: (type: String, amount: Double, note: String) -> Unit
) {
    // Which entry-type dialog is open (null = none)
    var showEntryDialog by remember { mutableStateOf<String?>(null) }
    // Reconciliation dialog
    var showReconcileDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── (A) Header summary cards ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = IncomeGreen.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.cashbox_today_sales),
                            style = MaterialTheme.typography.labelMedium,
                            color = IncomeGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTaka(todaySales, useBengali = useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = ReceivableBlue.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.cashbox_current_cash),
                            style = MaterialTheme.typography.labelMedium,
                            color = ReceivableBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTaka(currentCashBalance, useBengali = useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReceivableBlue
                        )
                    }
                }
            }

            // ── (B) Quick entry type buttons ─────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entryTypes) { key ->
                    Button(
                        onClick = { showEntryDialog = key },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (key == "CASH_IN" || key == "OWNER_IN")
                                IncomeGreen else ReceivableBlue
                        )
                    ) {
                        Text(text = entryTypeButtonLabel(key), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── (C) Today's entries list ──────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (cashEntries.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.cashbox_no_entries),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(cashEntries, key = { it.id }) { entry ->
                        CashBoxEntryCard(
                            entry = entry,
                            useBengaliDigits = useBengaliDigits
                        )
                    }
                }
            }

            // ── (D) Reconcile button ──────────────────────────────────────
            Button(
                onClick = { showReconcileDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = stringResource(R.string.cashbox_reconcile))
            }
        }
    }

    // Entry input dialog
    showEntryDialog?.let { typeKey ->
        EntryInputDialog(
            typeKey = typeKey,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showEntryDialog = null },
            onConfirm = { amount, note ->
                onAddEntry(typeKey, amount, note)
                showEntryDialog = null
            }
        )
    }

    // Reconcile confirmation dialog
    if (showReconcileDialog) {
        AlertDialog(
            onDismissRequest = { showReconcileDialog = false },
            title = { Text(text = stringResource(R.string.cashbox_reconcile)) },
            text = {
                Text(
                    text = stringResource(R.string.cashbox_reconcile_message)
                )
            },
            confirmButton = {
                TextButton(onClick = { showReconcileDialog = false }) {
                    Text(text = stringResource(R.string.cashbox_reconcile_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReconcileDialog = false }) {
                    Text(text = stringResource(R.string.cashbox_reconcile_no))
                }
            }
        )
    }
}

@Composable
private fun CashBoxEntryCard(
    entry: CashBoxEntryEntity,
    useBengaliDigits: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel(entry.type),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = Formatters.formatDate(entry.dateMillis, useBengali = useBengaliDigits),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Formatters.formatTaka(entry.amount, useBengali = useBengaliDigits),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (entry.type == "CASH_IN" || entry.type == "OWNER_IN") IncomeGreen else ReceivableBlue
            )
        }
    }
}

@Composable
private fun EntryInputDialog(
    typeKey: String,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = typeLabel(typeKey)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.dialog_payment_amount_hint)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(stringResource(R.string.cashbox_note_optional)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = Formatters.parseAmount(amountText)
                    if (amount > 0) onConfirm(amount, noteText.trim())
                }
            ) {
                Text(text = stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}
