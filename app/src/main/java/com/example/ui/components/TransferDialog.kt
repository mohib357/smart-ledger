package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.WalletEntity
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    wallets: List<WalletEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Double) -> Unit
) {
    if (wallets.size < 2) return

    var fromWalletId by remember { mutableStateOf(wallets[0].id) }
    var toWalletId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: wallets[0].id) }
    var amountText by remember { mutableStateOf("") }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("transfer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dialog_transfer_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // From Wallet
                val fromWallet = wallets.find { it.id == fromWalletId } ?: wallets[0]
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = "${fromWallet.name} (${Formatters.formatTaka(fromWallet.balance, useBengali = useBengaliDigits)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("যে অ্যাকাউন্ট থেকে বের হবে (উৎস)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        wallets.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} - ${Formatters.formatTaka(w.balance, useBengali = useBengaliDigits)}") },
                                onClick = {
                                    fromWalletId = w.id
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // To Wallet
                val toWallet = wallets.find { it.id == toWalletId } ?: wallets.getOrNull(1) ?: wallets[0]
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = "${toWallet.name} (${Formatters.formatTaka(toWallet.balance, useBengali = useBengaliDigits)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("যে অ্যাকাউন্টে ঢুকবে (গন্তব্য)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        wallets.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} - ${Formatters.formatTaka(w.balance, useBengali = useBengaliDigits)}") },
                                onClick = {
                                    toWalletId = w.id
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("স্থানান্তর পরিমাণ (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                val parsedAmount = Formatters.parseAmount(amountText)
                Button(
                    onClick = {
                        if (parsedAmount > 0 && fromWalletId != toWalletId) {
                            onTransfer(fromWalletId, toWalletId, parsedAmount)
                        }
                    },
                    enabled = parsedAmount > 0 && fromWalletId != toWalletId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("স্থানান্তর নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
