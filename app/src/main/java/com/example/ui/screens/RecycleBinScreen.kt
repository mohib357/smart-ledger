package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.util.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    deletedTransactions: List<TransactionEntity>,
    useBengaliDigits: Boolean,
    onRestore: (TransactionEntity) -> Unit,
    onPermanentDelete: (TransactionEntity) -> Unit
) {
    var confirmDeleteTx by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Auto-delete hint banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = stringResource(R.string.recycle_bin_auto_delete_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        if (deletedTransactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.recycle_bin_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deletedTransactions, key = { it.id }) { tx ->
                    RecycleBinItem(
                        tx = tx,
                        useBengaliDigits = useBengaliDigits,
                        onRestore = { onRestore(tx) },
                        onPermanentDelete = { confirmDeleteTx = tx }
                    )
                }
            }
        }
    }

    // Permanent delete confirmation dialog
    if (confirmDeleteTx != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTx = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(stringResource(R.string.recycle_bin_delete_confirm_title)) },
            text = { Text(stringResource(R.string.recycle_bin_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteTx?.let { onPermanentDelete(it) }
                        confirmDeleteTx = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTx = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun RecycleBinItem(
    tx: TransactionEntity,
    useBengaliDigits: Boolean,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
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
                    text = tx.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = Formatters.formatTaka(tx.amount, useBengali = useBengaliDigits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (tx.type == "INCOME")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                if (tx.deletedAt != null) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(tx.deletedAt))
                    Text(
                        text = "মুছেছেন: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Restore button
            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Default.RestoreFromTrash,
                    contentDescription = stringResource(R.string.recycle_bin_restore),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Permanent delete button
            IconButton(onClick = onPermanentDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.recycle_bin_permanent_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
