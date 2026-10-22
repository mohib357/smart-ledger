package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var chatInputText by remember { mutableStateOf("") }
    var showTicketDialog by remember { mutableStateOf(false) }
    var showAdminChat by remember { mutableStateOf(false) }

    // Admin chat state
    var adminChatMessages by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var adminChatInput by remember { mutableStateOf("") }
    var adminChatError by remember { mutableStateOf(false) }

    val presetQuestions = listOf(
        "ট্রানজেকশন কীভাবে যোগ করবো?",
        "রিপোর্ট কীভাবে PDF করবো?",
        "ব্যাকআপ কীভাবে নেবো?",
        "পার্টি লেজার কী?",
        "কয়েন কীভাবে অর্জন করবো?",
        "প্রিমিয়াম প্ল্যান কী কী সুবিধা?"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setTab(0) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "পিছনে"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.support_center),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.support_center_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // AI Helper card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.support_ai_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.support_ai_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset question chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetQuestions) { question ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.sendChatMessage(question)
                                    viewModel.setTab(5)
                                },
                                label = { Text(question, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text input + send button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text(stringResource(R.string.support_chat_hint)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val text = chatInputText.trim()
                                if (text.isNotEmpty()) {
                                    viewModel.sendChatMessage(text)
                                    chatInputText = ""
                                    viewModel.setTab(5)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "পাঠান",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Contact section heading
        item {
            Text(
                text = "যোগাযোগ করুন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // WhatsApp card
        item {
            SupportContactCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "হোয়াটসঅ্যাপে যোগাযোগ",
                subtitle = "সরাসরি কথা বলুন",
                buttonText = "হোয়াটসঅ্যাপ খুলুন",
                onButtonClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801700000000"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "হোয়াটসঅ্যাপ খোলা যায়নি", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Support Ticket card
        item {
            SupportContactCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "সাপোর্ট টিকেট",
                subtitle = "সমস্যা জানান, আমরা সমাধান করবো",
                buttonText = "টিকেট পাঠান",
                onButtonClick = { showTicketDialog = true }
            )
        }

        // Admin Chat card
        item {
            SupportContactCard(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "অ্যাডমিন চ্যাট",
                subtitle = "সরাসরি সাপোর্ট টিমের সাথে কথা বলুন",
                buttonText = "চ্যাট শুরু করুন",
                onButtonClick = {
                    showAdminChat = true
                    // Start listening to admin chat messages
                    try {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@SupportContactCard
                        FirebaseFirestore.getInstance()
                            .collection("admin_chats")
                            .document(userId)
                            .collection("messages")
                            .orderBy("timestamp")
                            .addSnapshotListener { snapshot, error ->
                                if (error != null || snapshot == null) {
                                    adminChatError = true
                                    return@addSnapshotListener
                                }
                                adminChatMessages = snapshot.documents.map { doc ->
                                    val text = doc.getString("text") ?: ""
                                    val fromUser = doc.getBoolean("fromUser") ?: true
                                    text to fromUser
                                }
                            }
                    } catch (_: Exception) {
                        adminChatError = true
                    }
                }
            )
        }

        // Admin chat inline
        item {
            AnimatedVisibility(visible = showAdminChat) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (adminChatError) {
                            Text(
                                text = "অ্যাডমিন চ্যাট সংযোগ নেই",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Messages list
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                adminChatMessages.forEach { (text, fromUser) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .background(
                                                    color = if (fromUser) MaterialTheme.colorScheme.primaryContainer
                                                            else MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(text = text, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Input row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = adminChatInput,
                                    onValueChange = { adminChatInput = it },
                                    placeholder = { Text(stringResource(R.string.support_chat_hint)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val msg = adminChatInput.trim()
                                        if (msg.isNotEmpty()) {
                                            try {
                                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                if (userId != null) {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("admin_chats")
                                                        .document(userId)
                                                        .collection("messages")
                                                        .add(
                                                            mapOf(
                                                                "text" to msg,
                                                                "fromUser" to true,
                                                                "timestamp" to System.currentTimeMillis()
                                                            )
                                                        )
                                                    adminChatInput = ""
                                                }
                                            } catch (_: Exception) {
                                                adminChatError = true
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "পাঠান",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ভার্সন ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "হিসাব - Smart Ledger © 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Support Ticket Dialog
    if (showTicketDialog) {
        SupportTicketDialog(
            onDismiss = { showTicketDialog = false }
        )
    }
}

@Composable
private fun SupportContactCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onButtonClick) {
                Text(buttonText, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportTicketDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    val categories = listOf("প্রযুক্তিগত সমস্যা", "বিলিং", "ফিচার অনুরোধ", "অন্যান্য")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val ticketSuccessMsg = stringResource(R.string.support_ticket_success)
    val ticketErrorMsg = stringResource(R.string.support_ticket_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.support_ticket)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.support_ticket_category)) },
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
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(R.string.support_ticket_subject)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.support_ticket_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                        val deviceInfo = "${Build.MODEL} Android ${Build.VERSION.RELEASE}"
                        FirebaseFirestore.getInstance()
                            .collection("support_tickets")
                            .add(
                                mapOf(
                                    "userId" to userId,
                                    "category" to selectedCategory,
                                    "subject" to subject,
                                    "description" to description,
                                    "timestamp" to System.currentTimeMillis(),
                                    "status" to "open",
                                    "deviceInfo" to deviceInfo
                                )
                            )
                        Toast.makeText(context, ticketSuccessMsg, Toast.LENGTH_SHORT).show()
                    } catch (_: Exception) {
                        Toast.makeText(context, ticketErrorMsg, Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.support_ticket_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
