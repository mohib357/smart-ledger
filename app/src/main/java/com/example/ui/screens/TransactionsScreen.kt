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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    filter: String,
    searchQuery: String,
    useBengaliDigits: Boolean,
    categoryFilter: String,
    dateFrom: Long?,
    dateTo: Long?,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onDateFromChange: (Long?) -> Unit,
    onDateToChange: (Long?) -> Unit,
    onAddTransaction: (String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val filteredList = transactions.filter { tx ->
        val matchesFilter = when (filter) {
            "INCOME" -> tx.type == "INCOME"
            "EXPENSE" -> tx.type == "EXPENSE"
            else -> true
        }
        val matchesCategory = categoryFilter == "ALL" || tx.category == categoryFilter
        val matchesDateFrom = dateFrom == null || tx.dateMillis >= dateFrom
        val matchesDateTo = dateTo == null || tx.dateMillis <= dateTo
        val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.walletName.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesCategory && matchesDateFrom && matchesDateTo && matchesSearch
    }

    val totalIncome = filteredList.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    // Category chips from data
    val dataCategories = transactions.map { it.category }.distinct().sorted()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTransaction("EXPENSE") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন লেনদেন")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("transactions_screen")
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(stringResource(R.string.transactions_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("transaction_search_field")
            )

            // Filter Chips (type)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = { Text("${stringResource(R.string.transactions_filter_all)} (${filteredList.size})") }
                )
                FilterChip(
                    selected = filter == "INCOME",
                    onClick = { onFilterChange("INCOME") },
                    label = { Text(stringResource(R.string.transactions_filter_income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreenLight,
                        selectedLabelColor = IncomeGreen
                    )
                )
                FilterChip(
                    selected = filter == "EXPENSE",
                    onClick = { onFilterChange("EXPENSE") },
                    label = { Text(stringResource(R.string.transactions_filter_expense)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRedLight,
                        selectedLabelColor = ExpenseRed
                    )
                )
            }

            // Category filter chips (from data)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = categoryFilter == "ALL",
                        onClick = { onCategoryFilterChange("ALL") },
                        label = { Text(stringResource(R.string.transactions_category_all), fontSize = 12.sp) }
                    )
                }
                items(dataCategories) { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick = { onCategoryFilterChange(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Date range row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showFromPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (dateFrom != null)
                            "শুরু: ${Formatters.formatDate(dateFrom, useBengali = useBengaliDigits)}"
                        else stringResource(R.string.transactions_date_from),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                OutlinedButton(
                    onClick = { showToPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (dateTo != null)
                            "শেষ: ${Formatters.formatDate(dateTo, useBengali = useBengaliDigits)}"
                        else stringResource(R.string.transactions_date_to),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                if (dateFrom != null || dateTo != null) {
                    IconButton(onClick = {
                        onDateFromChange(null)
                        onDateToChange(null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.transactions_date_clear), modifier = Modifier.size(18.dp))
                    }
                }
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
                        text = stringResource(R.string.transactions_summary_income) + " " + Formatters.formatTaka(totalIncome, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                    Text(
                        text = stringResource(R.string.transactions_summary_expense) + " " + Formatters.formatTaka(totalExpense, useBengali = useBengaliDigits),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }

            // Transactions List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.transactions_empty),
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
                    items(filteredList, key = { it.id }) { tx ->
                        TransactionItemCard(
                            tx = tx,
                            useBengaliDigits = useBengaliDigits,
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // From date picker
    if (showFromPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = state.selectedDateMillis
                    if (sel != null) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateFromChange(cal.timeInMillis)
                    }
                    showFromPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    // To date picker
    if (showToPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = state.selectedDateMillis
                    if (sel != null) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        onDateToChange(cal.timeInMillis)
                    }
                    showToPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(stringResource(R.string.transactions_delete_title)) },
            text = { Text("'${tx.title}' (${Formatters.formatTaka(tx.amount, useBengali = useBengaliDigits)}) এর রেকর্ড মুছে ফেলা হবে এবং অ্যাকাউন্ট ব্যালেন্স স্বয়ংক্রিয়ভাবে সমন্বয় হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.transactions_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun TransactionItemCard(
    tx: TransactionEntity,
    useBengaliDigits: Boolean,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (tx.type == "INCOME") IncomeGreenLight else ExpenseRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "INCOME") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (tx.type == "INCOME") IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tx.category,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${tx.walletName} • ${Formatters.formatDate(tx.dateMillis, useBengali = useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (tx.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tx.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatTaka(tx.amount, showSign = true, useBengali = useBengaliDigits),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
