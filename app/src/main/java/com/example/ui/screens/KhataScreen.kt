package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.PartyEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.PayableAmberLight
import com.example.ui.theme.ReceivableBlue
import com.example.ui.theme.ReceivableBlueLight
import com.example.util.Formatters

@Composable
fun KhataScreen(
    parties: List<PartyEntity>,
    filter: String, // "ALL", "PAYABLE", "RECEIVABLE"
    searchQuery: String,
    useBengaliDigits: Boolean,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddParty: () -> Unit,
    onSelectParty: (PartyEntity) -> Unit
) {
    val filteredList = parties.filter { party ->
        val matchesFilter = when (filter) {
            "PAYABLE" -> party.partyType == "PAYABLE"
            "RECEIVABLE" -> party.partyType == "RECEIVABLE"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                party.name.contains(searchQuery, ignoreCase = true) ||
                party.note.contains(searchQuery, ignoreCase = true) ||
                party.phone.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val totalPayable = parties.filter { it.partyType == "PAYABLE" }.sumOf { it.currentAmount }
    val totalReceivable = parties.filter { it.partyType == "RECEIVABLE" }.sumOf { it.currentAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddParty,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("fab_add_party")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.action_add_party))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("khata_screen")
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("ব্যক্তি বা প্রতিষ্ঠানের নাম খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("party_search_field")
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = { Text("${stringResource(R.string.khata_filter_all)} (${filteredList.size})") }
                )
                FilterChip(
                    selected = filter == "PAYABLE",
                    onClick = { onFilterChange("PAYABLE") },
                    label = { Text("সে পাবে (দেনা)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PayableAmberLight,
                        selectedLabelColor = PayableAmber
                    )
                )
                FilterChip(
                    selected = filter == "RECEIVABLE",
                    onClick = { onFilterChange("RECEIVABLE") },
                    label = { Text("আমি পাবো (পাওনা)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ReceivableBlueLight,
                        selectedLabelColor = ReceivableBlue
                    )
                )
            }

            // Summary Totals Strip
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "মোট দেনা: ${Formatters.formatTaka(totalPayable, useBengali = useBengaliDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Text(
                        text = "মোট পাওনা: ${Formatters.formatTaka(totalReceivable, useBengali = useBengaliDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
            }

            // Parties List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো হিসাব খাতা পাওয়া যায়নি।\nনিচের '+' বোতাম দিয়ে নতুন ব্যক্তি বা প্রতিষ্ঠানের খাতা যোগ করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { party ->
                        PartyCardItem(
                            party = party,
                            useBengaliDigits = useBengaliDigits,
                            onClick = { onSelectParty(party) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PartyCardItem(
    party: PartyEntity,
    useBengaliDigits: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (party.partyType == "PAYABLE") PayableAmberLight else ReceivableBlueLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (party.partyType == "PAYABLE") PayableAmber else ReceivableBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (party.partyType == "PAYABLE") PayableAmberLight else ReceivableBlueLight
                        ) {
                            Text(
                                text = if (party.partyType == "PAYABLE") stringResource(R.string.dashboard_party_payable_label) else stringResource(R.string.dashboard_party_receivable_label),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (party.partyType == "PAYABLE") PayableAmber else ReceivableBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (party.phone.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = party.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (party.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = party.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatTaka(party.currentAmount, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (party.partyType == "PAYABLE") ExpenseRed else IncomeGreen
                    )
                    Text(
                        text = "বাকি স্থিতি",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
