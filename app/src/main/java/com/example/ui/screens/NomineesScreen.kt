package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.NomineeEntity
import com.example.data.model.PartyEntity
import com.example.data.repository.AccountRepository
import com.example.ui.components.NomineeAccessPreviewDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.PayableAmberLight
import com.example.ui.theme.ReceivableBlue
import com.example.ui.theme.ReceivableBlueLight
import com.example.util.Formatters

@Composable
fun NomineesScreen(
    nominees: List<NomineeEntity>,
    stats: AccountRepository.DashboardStats,
    parties: List<PartyEntity>,
    useBengaliDigits: Boolean,
    onAddNominee: () -> Unit,
    onDeleteNominee: (NomineeEntity) -> Unit
) {
    val context = LocalContext.current
    var previewNominee by remember { mutableStateOf<NomineeEntity?>(null) }
    var nomineeToDelete by remember { mutableStateOf<NomineeEntity?>(null) }
    var nomineeKeyInput by remember { mutableStateOf("") }
    var enteredNomineeMatch by remember { mutableStateOf<NomineeEntity?>(null) }
    var showKeySearchError by remember { mutableStateOf(false) }

    fun shareNomineeSlip(nominee: NomineeEntity) {
        val slip = Formatters.generateNomineeAccessSlip(
            nomineeName = nominee.name,
            relation = nominee.relation,
            accessKey = nominee.accessKey,
            accessScope = nominee.accessScope,
            totalPayable = stats.totalPayable,
            totalReceivable = stats.totalReceivable,
            totalCash = if (nominee.canViewCash) stats.totalWalletBalance else null,
            note = nominee.note,
            parties = parties
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, slip)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "উত্তরাধিকার এক্সেস স্লিপ শেয়ার করুন")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nominees_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Information Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "উত্তরাধিকার ও জরুরী আর্থিক সুরক্ষা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "জরুরি পরিস্থিতিতে ঋণ পরিশোধ ও পাওনা আদায়ের জন্য",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "গুগল শিটের মতো আপনি যাদের অনুমতি দেবেন, তারা আপনার মৃত্যুর পর বা দুর্ঘটনায় আপনার খাতার নির্ধারিত অংশ (যেমন: ঋণ পরিশোধ বা পাওনা আদায়) দেখতে পারবেন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onAddNominee,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ নতুন উত্তরাধিকারী / প্রতিনিধি যোগ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Key-based Unlock Box for Nominee on another device
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "প্রতিনিধি এক্সেস কোড যাচাই (Nominee Verification)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nomineeKeyInput,
                            onValueChange = {
                                nomineeKeyInput = it
                                showKeySearchError = false
                            },
                            placeholder = { Text("যেমন: HSL-BR7890") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val match = nominees.find { it.accessKey.equals(nomineeKeyInput.trim(), ignoreCase = true) }
                                if (match != null) {
                                    previewNominee = match
                                    showKeySearchError = false
                                } else {
                                    showKeySearchError = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("খুলুন")
                        }
                    }

                    if (showKeySearchError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ভুল এক্সেস কোড। সঠিক কোডটি প্রদান করুন।",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Nominees List Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "অনুমোদিত প্রতিনিধিবৃন্দ (${nominees.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (nominees.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "এখনও কোনো প্রতিনিধি যোগ করা হয়নি।\nউপরে থাকা '+ নতুন প্রতিনিধি' বোতামে চাপ দিয়ে ভাই, স্ত্রী, সন্তান বা বিশ্বস্ত কাউকে যুক্ত করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(nominees) { nominee ->
                NomineeCardItem(
                    nominee = nominee,
                    onPreview = { previewNominee = nominee },
                    onShare = { shareNomineeSlip(nominee) },
                    onDelete = { nomineeToDelete = nominee }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Preview / Share Dialog
    if (previewNominee != null) {
        NomineeAccessPreviewDialog(
            nominee = previewNominee!!,
            stats = stats,
            parties = parties,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { previewNominee = null }
        )
    }

    // Delete Confirmation
    if (nomineeToDelete != null) {
        val nom = nomineeToDelete!!
        AlertDialog(
            onDismissRequest = { nomineeToDelete = null },
            title = { Text("প্রতিনিধি বাতিল করবেন?") },
            text = { Text("'${nom.name}' (${nom.relation})-এর এক্সেস ও অনুমতি বাতিল করা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteNominee(nom)
                        nomineeToDelete = null
                    }
                ) {
                    Text("বাতিল করুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { nomineeToDelete = null }) {
                    Text("না")
                }
            }
        )
    }
}

@Composable
private fun NomineeCardItem(
    nominee: NomineeEntity,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nominee.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = nominee.relation,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (nominee.phone.isNotBlank()) {
                        Text(
                            text = "মোবাইল: ${nominee.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Access Scope Pill
            val (scopeLabel, scopeBg, scopeFg) = when (nominee.accessScope) {
                "PAYABLE_ONLY" -> Triple("অনুমতি: শুধুমাত্র দেনা / ঋণ পরিশোধ", PayableAmberLight, PayableAmber)
                "RECEIVABLE_ONLY" -> Triple("অনুমতি: শুধুমাত্র পাওনা আদায়", ReceivableBlueLight, ReceivableBlue)
                "DEBTS_AND_RECEIVABLES" -> Triple("অনুমতি: দেনা ও পাওনা উভয় খতিয়ান", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                else -> Triple("অনুমতি: সম্পূর্ণ খাতা ও আর্থিক স্থিতি", IncomeGreen.copy(alpha = 0.15f), IncomeGreen)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = scopeBg
            ) {
                Text(
                    text = scopeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scopeFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Token + Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "কোড: ${nominee.accessKey}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPreview,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিভিউ", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্লিপ শেয়ার", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
