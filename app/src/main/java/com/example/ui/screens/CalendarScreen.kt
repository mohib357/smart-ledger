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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Formatters
import java.util.Calendar

@Composable
fun CalendarScreen(
    transactions: List<TransactionEntity>,
    useBengaliDigits: Boolean
) {
    val today = Calendar.getInstance()
    val currentYear = today.get(Calendar.YEAR)
    val currentMonth = today.get(Calendar.MONTH) // 0-based

    var displayYear by remember { mutableIntStateOf(currentYear) }
    var displayMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(-1) }

    // Group transactions by day-start millis
    val txByDay: Map<Long, List<TransactionEntity>> = transactions.groupBy { tx ->
        Calendar.getInstance().apply {
            timeInMillis = tx.dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Earliest month from data
    val earliestMillis = transactions.minOfOrNull { it.dateMillis }
    val earliestCal = earliestMillis?.let {
        Calendar.getInstance().apply { timeInMillis = it }
    }

    // Check navigation bounds
    val atEarliest = earliestCal?.let {
        displayYear < it.get(Calendar.YEAR) ||
        (displayYear == it.get(Calendar.YEAR) && displayMonth <= it.get(Calendar.MONTH))
    } ?: false
    val atLatest = displayYear == currentYear && displayMonth == currentMonth

    // Month grid math
    val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Map to 0-6 offset (শনি=0, রবি=1, সোম=2, মঙ্গল=3, বুধ=4, বৃহ=5, শুক্র=6)
    // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6, Sat=7
    val leadingBlanks = when (firstDayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }

    val bengaliMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val weekDays = listOf(stringResource(R.string.calendar_weekday_sat), stringResource(R.string.calendar_weekday_sun), stringResource(R.string.calendar_weekday_mon), stringResource(R.string.calendar_weekday_tue), stringResource(R.string.calendar_weekday_wed), stringResource(R.string.calendar_weekday_thu), stringResource(R.string.calendar_weekday_fri))

    // Selected day transactions
    val selectedDayTxs: List<TransactionEntity> = if (selectedDay > 0) {
        val selCal = Calendar.getInstance().apply {
            set(displayYear, displayMonth, selectedDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        txByDay[selCal.timeInMillis] ?: emptyList()
    } else emptyList()

    val selectedDayMillis: Long = if (selectedDay > 0) {
        Calendar.getInstance().apply {
            set(displayYear, displayMonth, selectedDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } else 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Month navigation header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!atEarliest) {
                                selectedDay = -1
                                if (displayMonth == 0) {
                                    displayMonth = 11
                                    displayYear--
                                } else {
                                    displayMonth--
                                }
                            }
                        },
                        modifier = Modifier.alpha(if (atEarliest) 0.38f else 1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "আগের মাস",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val monthYear = "${bengaliMonths[displayMonth]} ${
                            if (useBengaliDigits) Formatters.toBengaliDigits(displayYear.toString())
                            else displayYear.toString()
                        }"
                        Text(
                            text = "📅 $monthYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!atLatest) {
                            TextButton(onClick = {
                                selectedDay = -1
                                displayYear = currentYear
                                displayMonth = currentMonth
                            }) {
                                Text(stringResource(R.string.calendar_today), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        IconButton(
                            onClick = {
                                if (!atLatest) {
                                    selectedDay = -1
                                    if (displayMonth == 11) {
                                        displayMonth = 0
                                        displayYear++
                                    } else {
                                        displayMonth++
                                    }
                                }
                            },
                            modifier = Modifier.alpha(if (atLatest) 0.38f else 1f)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "পরের মাস",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Weekday row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Calendar grid - build rows of 7
        val totalCells = leadingBlanks + daysInMonth
        val numRows = (totalCells + 6) / 7

        items(numRows) { rowIdx ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (colIdx in 0..6) {
                    val cellIdx = rowIdx * 7 + colIdx
                    val day = cellIdx - leadingBlanks + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        // Check if this day has transactions
                        val dayCal = Calendar.getInstance().apply {
                            set(displayYear, displayMonth, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dayTxs = txByDay[dayCal.timeInMillis] ?: emptyList()
                        val dayIncome = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val dayExpense = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val hasTx = dayTxs.isNotEmpty()
                        val isSelected = day == selectedDay
                        val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                                      displayMonth == currentMonth &&
                                      displayYear == currentYear

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (useBengaliDigits) Formatters.toBengaliDigits(day.toString()) else day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasTx) {
                                    val dotColor = when {
                                        dayIncome > dayExpense -> IncomeGreen
                                        dayExpense > dayIncome -> ExpenseRed
                                        else -> androidx.compose.ui.graphics.Color(0xFFD97706) // AccentGold
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected day transactions section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    when {
                        selectedDay == -1 -> {
                            Text(
                                text = stringResource(R.string.calendar_select_prompt),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        selectedDayTxs.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.calendar_selected_day) + " — " + Formatters.formatDate(selectedDayMillis, useBengali = useBengaliDigits),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.calendar_no_transactions),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.calendar_selected_day) + " — " + Formatters.formatDate(selectedDayMillis, useBengali = useBengaliDigits),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            selectedDayTxs.forEach { tx ->
                                CalendarDayTxRow(tx = tx, useBengaliDigits = useBengaliDigits)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayTxRow(
    tx: TransactionEntity,
    useBengaliDigits: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Formatters.formatTaka(tx.amount, showSign = true, useBengali = useBengaliDigits),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
            )
        }
    }
}
