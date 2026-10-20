package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.PartyEntity
import com.example.data.model.WalletEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyPaymentDialog(
    party: PartyEntity,
    initialType: String = "GIVE", // "GIVE" (পরিশোধ) or "TAKE" (নেওয়া/বাকি)
    wallets: List<WalletEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, walletId: Long?, note: String) -> Unit
) {
    var entryType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var updateWalletBalance by remember { mutableStateOf(true) }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 1L) }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    val isPayable = party.partyType == "PAYABLE"
    val giveLabel = if (isPayable) "টাকা পরিশোধ (দেনা কমবে)" else "টাকা আদায় (পাওনা কমবে)"
    val takeLabel = if (isPayable) "টাকা গ্রহণ (দেনা বাড়বে)" else "টাকা দেওয়া (পাওনা বাড়বে)"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("party_payment_dialog")
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
                    Column {
                        Text(
                            text = party.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "বর্তমান বাকি: ${Formatters.formatTaka(party.currentAmount, useBengali = useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (entryType == "GIVE") IncomeGreen else Color.Transparent)
                            .clickable { entryType = "GIVE" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = giveLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (entryType == "GIVE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (entryType == "TAKE") ExpenseRed else Color.Transparent)
                            .clickable { entryType = "TAKE" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = takeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (entryType == "TAKE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.dialog_amount_label)) },
                    placeholder = { Text("যেমন: ৫০০০ বা 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Checkbox: adjust wallet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { updateWalletBalance = !updateWalletBalance },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = updateWalletBalance,
                        onCheckedChange = { updateWalletBalance = it }
                    )
                    Text(
                        text = "ক্যাশ বা অ্যাকাউন্টে সমন্বয় করুন",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (updateWalletBalance) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                    ExposedDropdownMenuBox(
                        expanded = walletDropdownExpanded,
                        onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedWallet?.name ?: "ক্যাশ"} (${Formatters.formatTaka(selectedWallet?.balance ?: 0.0, useBengali = useBengaliDigits)})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("কোন অ্যাকাউন্ট থেকে?") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = walletDropdownExpanded,
                            onDismissRequest = { walletDropdownExpanded = false }
                        ) {
                            wallets.forEach { w ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${w.name} - ${Formatters.formatTaka(w.balance, useBengali = useBengaliDigits)}")
                                    },
                                    onClick = {
                                        selectedWalletId = w.id
                                        walletDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিবরণ / নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                val parsedAmount = Formatters.parseAmount(amountText)
                Button(
                    onClick = {
                        if (parsedAmount > 0) {
                            onSave(
                                parsedAmount,
                                if (updateWalletBalance) selectedWalletId else null,
                                note.trim().ifEmpty { if (entryType == "GIVE") "টাকা পরিশোধ" else "টাকা নেওয়া" }
                            )
                        }
                    },
                    enabled = parsedAmount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entryType == "GIVE") IncomeGreen else ExpenseRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_payment_button")
                ) {
                    Text(
                        text = "লেনদেন নিশ্চিত করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
