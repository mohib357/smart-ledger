package com.example.ui.components

import android.os.Build
import android.telephony.SmsManager
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.PartyEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmsMarketingDialog(
    parties: List<PartyEntity>,
    onDismiss: () -> Unit,
    onSmsDone: (count: Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedGroup by remember { mutableStateOf("ALL") } // ALL, PAYABLE, RECEIVABLE
    var templateText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var sentCount by remember { mutableIntStateOf(0) }
    var totalCount by remember { mutableIntStateOf(0) }

    val targetParties = parties.filter { p ->
        p.phone.isNotBlank() && when (selectedGroup) {
            "PAYABLE" -> p.partyType == "PAYABLE"
            "RECEIVABLE" -> p.partyType == "RECEIVABLE"
            else -> true
        }
    }

    Dialog(onDismissRequest = { if (!isSending) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
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
                        text = stringResource(R.string.dialog_sms_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { if (!isSending) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Group selector
                Text(
                    text = stringResource(R.string.dialog_sms_group_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "ALL" to stringResource(R.string.dialog_sms_group_all),
                        "PAYABLE" to stringResource(R.string.dialog_sms_group_payable),
                        "RECEIVABLE" to stringResource(R.string.dialog_sms_group_receivable)
                    ).forEach { (key, label) ->
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

                Text(
                    text = "প্রাপক: ${targetParties.size} জন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Template
                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    label = { Text(stringResource(R.string.dialog_sms_template_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // Preview
                if (templateText.isNotBlank() && targetParties.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.dialog_sms_preview_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = templateText.replace("{name}", targetParties.first().name),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                // Progress
                if (isSending) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(
                            stringResource(R.string.dialog_sms_sending_progress),
                            sentCount, totalCount
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) sentCount.toFloat() / totalCount else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isSending = true
                        totalCount = targetParties.size
                        sentCount = 0
                        scope.launch {
                            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.getSystemService(SmsManager::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                SmsManager.getDefault()
                            }
                            targetParties.forEach { party ->
                                val msg = templateText.replace("{name}", party.name)
                                try {
                                    smsManager.sendTextMessage(party.phone, null, msg, null, null)
                                } catch (_: Exception) { }
                                sentCount++
                                delay(200)
                            }
                            isSending = false
                            onSmsDone(sentCount)
                        }
                    },
                    enabled = !isSending && templateText.isNotBlank() && targetParties.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.dialog_sms_send_button), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
