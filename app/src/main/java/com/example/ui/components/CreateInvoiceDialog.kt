package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.ProductEntity
import com.example.util.Formatters
import com.example.util.InvoicePdfGenerator
import com.example.util.PdfExportResult
import kotlinx.coroutines.launch

data class InvoiceLineItem(
    val product: ProductEntity,
    var quantity: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceDialog(
    products: List<ProductEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (customerName: String, customerPhone: String, items: List<InvoiceItemEntity>, note: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val lineItems = remember { mutableStateListOf<InvoiceLineItem>() }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }

    val grandTotal = lineItems.sumOf { it.product.sellPrice * it.quantity }

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
                        text = stringResource(R.string.dialog_create_invoice_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(stringResource(R.string.dialog_invoice_customer_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text(stringResource(R.string.dialog_invoice_customer_phone_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product selector
                if (products.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = productDropdownExpanded,
                            onExpandedChange = { productDropdownExpanded = !productDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.dialog_invoice_add_item)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                products.forEach { product ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${product.name} — ${Formatters.formatTaka(product.sellPrice, useBengali = useBengaliDigits)}")
                                        },
                                        onClick = {
                                            selectedProduct = product
                                            productDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                selectedProduct?.let { p ->
                                    val existing = lineItems.indexOfFirst { it.product.id == p.id }
                                    if (existing >= 0) {
                                        lineItems[existing] = lineItems[existing].copy(quantity = lineItems[existing].quantity + 1)
                                    } else {
                                        lineItems.add(InvoiceLineItem(p, 1))
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Line items
                if (lineItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    lineItems.forEachIndexed { idx, lineItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lineItem.product.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${Formatters.formatTaka(lineItem.product.sellPrice, useBengali = useBengaliDigits)} × ${lineItem.quantity} = ${Formatters.formatTaka(lineItem.product.sellPrice * lineItem.quantity, useBengali = useBengaliDigits)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (lineItems[idx].quantity > 1)
                                            lineItems[idx] = lineItems[idx].copy(quantity = lineItems[idx].quantity - 1)
                                        else lineItems.removeAt(idx)
                                    },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(R.string.dialog_invoice_total_label)}: ${Formatters.formatTaka(grandTotal, useBengali = useBengaliDigits)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dialog_invoice_note_hint)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                val canSave = customerName.isNotBlank() && lineItems.isNotEmpty()

                // Save button
                Button(
                    onClick = {
                        val entityItems = lineItems.map { li ->
                            InvoiceItemEntity(
                                invoiceId = 0,
                                productId = li.product.id,
                                productName = li.product.name,
                                quantity = li.quantity,
                                unitPrice = li.product.sellPrice,
                                totalPrice = li.product.sellPrice * li.quantity
                            )
                        }
                        onSave(customerName.trim(), customerPhone.trim(), entityItems, note.trim())
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.dialog_invoice_save), fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save & PDF button
                FilledTonalButton(
                    onClick = {
                        if (!canSave) return@FilledTonalButton
                        val entityItems = lineItems.map { li ->
                            InvoiceItemEntity(
                                invoiceId = 0,
                                productId = li.product.id,
                                productName = li.product.name,
                                quantity = li.quantity,
                                unitPrice = li.product.sellPrice,
                                totalPrice = li.product.sellPrice * li.quantity
                            )
                        }
                        onSave(customerName.trim(), customerPhone.trim(), entityItems, note.trim())
                        // Generate PDF using a temporary invoice object for preview
                        scope.launch {
                            val tempInvoice = com.example.data.model.InvoiceEntity(
                                invoiceNumber = "INV-PREVIEW",
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                totalAmount = grandTotal,
                                note = note.trim()
                            )
                            val result = InvoicePdfGenerator.generateInvoicePdf(context, tempInvoice, entityItems)
                            if (result is PdfExportResult.Success) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, result.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "চালান শেয়ার করুন"))
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.dialog_invoice_save_and_pdf), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
