package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    existingProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, stock: Int, buyPrice: Double, sellPrice: Double, expiryDate: Long?, category: String) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var stockText by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "0") }
    var buyPriceText by remember { mutableStateOf(existingProduct?.buyPrice?.toString() ?: "0.0") }
    var sellPriceText by remember { mutableStateOf(existingProduct?.sellPrice?.toString() ?: "0.0") }
    var expiryDate by remember { mutableStateOf<Long?>(existingProduct?.expiryDate) }
    var category by remember { mutableStateOf(existingProduct?.category ?: "সাধারণ") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val categories = listOf("সাধারণ", "খাদ্য", "পোশাক", "ওষুধ", "প্রসাধনী", "অন্যান্য")

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (expiryDate != null) {
            cal.timeInMillis = expiryDate!!
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day, 0, 0, 0)
                selected.set(Calendar.MILLISECOND, 0)
                expiryDate = selected.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (existingProduct == null) "নতুন পণ্য যোগ করুন" else "পণ্য সম্পাদনা করুন",
                    style = MaterialTheme.typography.titleMedium
                )

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("পণ্যের নাম") },
                    isError = nameError,
                    supportingText = if (nameError) ({ Text("নাম আবশ্যিক") }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Stock
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("স্টক পরিমাণ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Buy Price
                OutlinedTextField(
                    value = buyPriceText,
                    onValueChange = { buyPriceText = it },
                    label = { Text("ক্রয়মূল্য (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Sell Price
                OutlinedTextField(
                    value = sellPriceText,
                    onValueChange = { sellPriceText = it },
                    label = { Text("বিক্রয়মূল্য (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ক্যাটাগরি নির্বাচন করুন") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Expiry date picker row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (expiryDate != null)
                                dateFormatter.format(expiryDate!!)
                            else "তারিখ নির্বাচন করুন"
                        )
                    }
                    if (expiryDate != null) {
                        TextButton(onClick = { expiryDate = null }) {
                            Text("সাফ")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@OutlinedButton
                            }
                            val stock = stockText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            val buyPrice = buyPriceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                            val sellPrice = sellPriceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                            onSave(name.trim(), stock, buyPrice, sellPrice, expiryDate, category)
                        }
                    ) {
                        Text("পণ্য সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}
