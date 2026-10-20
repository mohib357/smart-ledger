package com.example.ui.components

import android.os.Build
import android.telephony.SmsManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.PartyEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmsMarketingDialog(
    parties: List<PartyEntity>,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    hasSmsPermission: Boolean,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedGroup by remember { mutableStateOf("ALL") } // ALL, PAYABLE, RECEIVABLE
    var messageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var sentCount by remember { mutableIntStateOf(0) }

    val recipientList = when (selectedGroup) {
        "PAYABLE" -> parties.filter { it.partyType == "PAYABLE" }
        "RECEIVABLE" -> parties.filter { it.partyType == "RECEIVABLE" }
        else -> parties
    }
    val validRecipients = recipientList.filter { it.phone.isNotBlank() }

    val groups = listOf(
        "ALL" to stringResource(R.string.dialog_sms_group_all),
        "PAYABLE" to stringResource(R.string.dialog_sms_group_payable),
        "RECEIVABLE" to stringResource(R.string.dialog_sms_group_receivable)
    )

    Dialog(onDismissRequest = { if (!isSending) onDismiss() }) {
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
                // Title
                Text(
                    text = "📱 SMS মার্কেটিং",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Group label
                Text(
                    text = stringResource(R.string.dialog_sms_group_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Group chips in a LazyRow
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(groups) { (key, label) ->
                        FilterChip(
                            selected = selectedGroup == key,
                            onClick = { selectedGroup = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text(stringResource(R.string.dialog_sms_template_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Preview (first 100 chars)
                Text(
                    text = messageText.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Recipient count
                Text(
                    text = "গ্রাহক: ${validRecipients.size} জন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Progress
                if (isSending) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = String.format(
                            stringResource(R.string.dialog_sms_sending_progress),
                            sentCount, validRecipients.size
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSending,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        enabled = validRecipients.isNotEmpty() && messageText.isNotBlank() && !isSending,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                if (!hasSmsPermission) {
                                    onRequestPermission()
                                    return@launch
                                }
                                isSending = true
                                sentCount = 0
                                validRecipients.forEachIndexed { i, party ->
                                    if (party.phone.isNotBlank()) {
                                        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                            context.getSystemService(SmsManager::class.java)
                                        else
                                            @Suppress("DEPRECATION")
                                            SmsManager.getDefault()
                                        try {
                                            smsManager?.sendTextMessage(party.phone, null, messageText, null, null)
                                        } catch (_: Exception) {}
                                    }
                                    sentCount = i + 1
                                    delay(200)
                                }
                                isSending = false
                                onShowSnackbar(String.format(context.getString(R.string.dialog_sms_done), sentCount))
                                onDismiss()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.dialog_sms_send_button), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
