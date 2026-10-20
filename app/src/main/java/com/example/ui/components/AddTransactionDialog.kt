package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.Categories
import com.example.data.model.WalletEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    initialType: String = "EXPENSE",
    wallets: List<WalletEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (type: String, title: String, amount: Double, category: String, walletId: Long, note: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember {
        mutableStateOf(
            if (initialType == "INCOME") Categories.incomeCategories.first()
            else Categories.expenseCategories.first()
        )
    }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 1L) }
    var note by remember { mutableStateOf("") }

    var walletDropdownExpanded by remember { mutableStateOf(false) }

    val commonCategories = if (type == "INCOME") {
        Categories.incomeCategories
    } else {
        Categories.expenseCategories
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_transaction_dialog")
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
                        text = if (type == "INCOME") stringResource(R.string.dialog_add_income_title) else stringResource(R.string.dialog_add_expense_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "INCOME") IncomeGreen else ExpenseRed
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle (আয় vs ব্যয়)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Income button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "INCOME") IncomeGreen else Color.Transparent)
                            .clickable {
                                type = "INCOME"
                                category = Categories.incomeCategories.first()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.dialog_income_toggle),
                                fontWeight = FontWeight.Bold,
                                color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Expense button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "EXPENSE") ExpenseRed else Color.Transparent)
                            .clickable {
                                type = "EXPENSE"
                                category = Categories.expenseCategories.first()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.dialog_expense_toggle),
                                fontWeight = FontWeight.Bold,
                                color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.dialog_title_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.dialog_amount_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_amount_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categories Quick Select
                Text(
                    text = stringResource(R.string.dialog_category_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == "INCOME") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wallet Selection Dropdown
                val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                ExposedDropdownMenuBox(
                    expanded = walletDropdownExpanded,
                    onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedWallet?.name ?: "ক্যাশ"} (${Formatters.formatTaka(selectedWallet?.balance ?: 0.0, useBengali = useBengaliDigits)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dialog_wallet_label)) },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dialog_note_label)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                val parsedAmount = Formatters.parseAmount(amountText)
                Button(
                    onClick = {
                        if (title.isNotBlank() && parsedAmount > 0) {
                            onSave(type, title.trim(), parsedAmount, category, selectedWalletId, note.trim())
                        }
                    },
                    enabled = title.isNotBlank() && parsedAmount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "INCOME") IncomeGreen else ExpenseRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = if (type == "INCOME") stringResource(R.string.dialog_save_income) else stringResource(R.string.dialog_save_expense),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
