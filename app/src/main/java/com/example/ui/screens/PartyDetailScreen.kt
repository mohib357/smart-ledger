package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.PartyEntity
import com.example.data.model.PartyLedgerEntryEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ReceivableBlue
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDetailScreen(
    party: PartyEntity,
    entries: List<PartyLedgerEntryEntity>,
    useBengaliDigits: Boolean,
    onBack: () -> Unit,
    onOpenPayment: (type: String) -> Unit,
    onDeleteParty: (PartyEntity) -> Unit,
    onExportReport: () -> Unit = {}
) {
    BackHandler { onBack() }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isPayable = party.partyType == "PAYABLE"
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(party.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isPayable) stringResource(R.string.party_type_payable_title) else stringResource(R.string.party_type_receivable_title),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.party_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.party_detail_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("party_detail_screen")
        ) {
            // Balance Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPayable) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPayable) stringResource(R.string.party_current_payable) else stringResource(R.string.party_current_receivable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPayable) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatTaka(party.currentAmount, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPayable) ExpenseRed else IncomeGreen
                    )
                    if (party.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(party.phone, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Quick Payment Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenPayment("GIVE") },
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPayable) stringResource(R.string.party_action_pay) else stringResource(R.string.party_action_collect), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onOpenPayment("TAKE") },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPayable) stringResource(R.string.party_action_take_due) else stringResource(R.string.party_action_new_due), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagada SMS button for PAYABLE parties with phone
            if (isPayable && party.phone.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        val smsBody = context.getString(R.string.party_reminder_sms_body, Formatters.formatTaka(party.currentAmount, useBengali = false))
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${party.phone}"))
                        intent.putExtra("sms_body", smsBody)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.party_reminder_sms))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Report export button
            OutlinedButton(
                onClick = onExportReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.party_report))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Header
            Text(
                text = stringResource(R.string.party_transaction_history, entries.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Compute running balances: reverse to oldest-first, TAKE adds, GIVE subtracts
            val runningBalances: Map<Long, Double> = run {
                val sorted = entries.sortedBy { it.dateMillis }
                var balance = 0.0
                val map = mutableMapOf<Long, Double>()
                for (e in sorted) {
                    balance = if (e.entryType == "TAKE") balance + e.amount else balance - e.amount
                    map[e.id] = balance
                }
                map
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.party_empty_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(entries, key = { _, e -> e.id }) { _, entry ->
                        PartyEntryCard(
                            entry = entry,
                            isPayable = isPayable,
                            useBengaliDigits = useBengaliDigits,
                            runningBalance = runningBalances[entry.id] ?: 0.0
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.party_delete_confirm_title)) },
            text = { Text(stringResource(R.string.party_delete_confirm_message, party.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteParty(party)
                        showDeleteConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.transactions_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun PartyEntryCard(
    entry: PartyLedgerEntryEntity,
    isPayable: Boolean,
    useBengaliDigits: Boolean,
    runningBalance: Double = 0.0
) {
    val isGive = entry.entryType == "GIVE"
    val isSettlement = isGive
    val typeTitle = if (isPayable) {
        if (isGive) stringResource(R.string.party_entry_settlement_paid) else stringResource(R.string.party_entry_due_received)
    } else {
        if (isGive) stringResource(R.string.party_entry_collected) else stringResource(R.string.party_entry_new_due_given)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSettlement) IncomeGreenLight else ExpenseRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSettlement) Icons.Default.CheckCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = if (isSettlement) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = typeTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${Formatters.formatDate(entry.dateMillis, useBengali = useBengaliDigits)}${if (entry.note.isNotBlank()) " • " + entry.note else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatTaka(entry.amount, useBengali = useBengaliDigits),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSettlement) IncomeGreen else ExpenseRed
                )
                Text(
                    text = stringResource(R.string.party_running_balance, Formatters.formatTaka(runningBalance, useBengali = useBengaliDigits)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
