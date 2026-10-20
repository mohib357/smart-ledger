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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun AddNomineeDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, relation: String, phone: String, email: String, accessScope: String, canViewCash: Boolean, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("ভাই") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accessScope by remember { mutableStateOf("PAYABLE_ONLY") }
    var canViewCash by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    val commonRelations = listOf("ভাই", "স্ত্রী", "সন্তান", "পিতা", "মাতা", "বোন", "বিশ্বস্ত বন্ধু", "অন্যান্য")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_nominee_dialog")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(start = 6.dp))
                        Text(
                            text = stringResource(R.string.dialog_add_nominee_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "আপনার অনুপস্থিতিতে বা দুর্ঘটনায় যেন ঋণ পরিশোধ ও পাওনা আদায় করতে পারে, তার জন্য নির্দিষ্ট এক্সেস নির্ধারণ করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_nominee_name_hint)) },
                    placeholder = { Text("যেমন: আব্দুর রহমান") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Relation
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text(stringResource(R.string.dialog_nominee_relation_hint)) },
                    placeholder = { Text("যেমন: বড় ভাই, স্ত্রী, সন্তান") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর (এসএমএস/হোয়াটসঅ্যাপ পাঠানোর জন্য)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Access Scope Selector
                Text(
                    text = "অনুমতি ও এক্সেস স্তর নির্বাচন করুন:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val scopes = listOf(
                    "PAYABLE_ONLY" to "শুধুমাত্র দেনা / ঋণ (মৃত্যুর পর ঋণ পরিশোধের জন্য)",
                    "RECEIVABLE_ONLY" to "শুধুমাত্র পাওনা (পাওনা টাকা আদায়ের জন্য)",
                    "DEBTS_AND_RECEIVABLES" to "দেনা ও পাওনা উভয় খতিয়ান",
                    "ALL" to "সম্পূর্ণ খাতা ও আর্থিক স্থিতি"
                )

                scopes.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                accessScope = key
                                if (key == "ALL") canViewCash = true
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = accessScope == key,
                            onClick = {
                                accessScope = key
                                if (key == "ALL") canViewCash = true
                            }
                        )
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (accessScope != "ALL") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { canViewCash = !canViewCash },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = canViewCash,
                            onCheckedChange = { canViewCash = it }
                        )
                        Text(
                            text = stringResource(R.string.dialog_nominee_cash_access),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note / Will / Instructions
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("জরুরি বার্তা বা ওসিয়ত (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: আমার এই ঋণগুলো দ্রুত পরিশোধ করে দিও...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), relation.trim(), phone.trim(), email.trim(), accessScope, canViewCash, note.trim())
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("প্রতিনিধি যুক্ত করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
