package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ChartPalette
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters
import com.example.util.MonthBucket
import com.example.util.monthlyIncomeExpense
import java.util.Calendar
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    transactions: List<TransactionEntity>,
    useBengaliDigits: Boolean,
    onExportPdf: (fromMillis: Long, toMillis: Long) -> Unit,
    productProfitSummary: List<MainViewModel.ProductProfitSummary> = emptyList(),
    products: List<ProductEntity> = emptyList()
) {
    val buckets = monthlyIncomeExpense(transactions)

    var pdfFrom by remember { mutableStateOf<Long?>(null) }
    var pdfTo by remember { mutableStateOf<Long?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.reports_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Monthly bar chart
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "মাসিক আয় ও ব্যয়",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (buckets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.reports_chart_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (buckets.size <= 6) {
                        MonthlyBarChart(buckets = buckets, useBengaliDigits = useBengaliDigits)
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(buckets) { bucket ->
                                MonthlyBarChart(
                                    buckets = listOf(bucket),
                                    useBengaliDigits = useBengaliDigits,
                                    modifier = Modifier.width(64.dp)
                                )
                            }
                        }
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আয়", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ব্যয়", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // PDF Export section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.reports_pdf_section),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (pdfFrom != null)
                                    "শুরু: ${Formatters.formatDate(pdfFrom!!, useBengali = useBengaliDigits)}"
                                else stringResource(R.string.reports_pdf_from),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (pdfTo != null)
                                    "শেষ: ${Formatters.formatDate(pdfTo!!, useBengali = useBengaliDigits)}"
                                else stringResource(R.string.reports_pdf_to),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val from = pdfFrom ?: 0L
                            val to = pdfTo ?: Long.MAX_VALUE
                            onExportPdf(from, to)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PDF রিপোর্ট তৈরি করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // F5 — Profit/Loss section
        item {
            var selectedRange by remember { mutableStateOf("সব") }
            val profitRanges = listOf(
                stringResource(R.string.reports_profit_filter_all),
                stringResource(R.string.reports_profit_filter_week),
                stringResource(R.string.reports_profit_filter_month),
                stringResource(R.string.reports_profit_filter_year)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📦 ${stringResource(R.string.reports_profit_title)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter chips (display-only — margin is stock-based, not time-ranged)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(profitRanges) { range ->
                            FilterChip(
                                selected = selectedRange == range,
                                onClick = { selectedRange = range },
                                label = { Text(range, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (productProfitSummary.isEmpty()) {
                        Text(
                            text = stringResource(R.string.reports_profit_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(productProfitSummary) { summary ->
                                val isProfit = summary.profitPerUnit >= 0
                                val profitColor = if (isProfit) IncomeGreen else ExpenseRed

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.product.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${stringResource(R.string.products_stock_label)}: ${summary.product.stock}  •  " +
                                                "${stringResource(R.string.reports_profit_per_unit)}: ${Formatters.formatTaka(summary.profitPerUnit, useBengali = useBengaliDigits)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatTaka(summary.profitMargin, useBengali = useBengaliDigits),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = profitColor
                                        )
                                        Text(
                                            text = stringResource(R.string.reports_profit_total_margin),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PDF From date picker
    if (showFromPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = state.selectedDateMillis
                    if (sel != null) {
                        val truncCal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        pdfFrom = truncCal.timeInMillis
                    }
                    showFromPicker = false
                }) { Text("ঠিক আছে") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("বাতিল") }
            }
        ) { DatePicker(state = state) }
    }

    // PDF To date picker
    if (showToPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = state.selectedDateMillis
                    if (sel != null) {
                        val truncCal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        pdfTo = truncCal.timeInMillis
                    }
                    showToPicker = false
                }) { Text("ঠিক আছে") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("বাতিল") }
            }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun MonthlyBarChart(
    buckets: List<MonthBucket>,
    useBengaliDigits: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().height(220.dp)
) {
    val labelAreaHeight = 30f
    val incomeColor = IncomeGreen
    val expenseColor = ExpenseRed
    val axisColor = Color.Gray

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val chartHeight = canvasHeight - labelAreaHeight

        if (buckets.isEmpty()) return@Canvas

        val maxValue = buckets.maxOf { max(it.income, it.expense) }.let { if (it == 0.0) 1.0 else it }

        val totalBuckets = buckets.size
        val groupWidth = canvasWidth / totalBuckets
        val barWidth = groupWidth * 0.35f
        val gap = groupWidth * 0.05f

        // Draw baseline
        drawLine(
            color = axisColor,
            start = Offset(0f, chartHeight),
            end = Offset(canvasWidth, chartHeight),
            strokeWidth = 1.5f
        )

        buckets.forEachIndexed { i, bucket ->
            val groupLeft = i * groupWidth

            // Income bar
            val incomeHeight = (bucket.income / maxValue * chartHeight).toFloat()
            drawRect(
                color = incomeColor,
                topLeft = Offset(groupLeft + gap, chartHeight - incomeHeight),
                size = Size(barWidth, incomeHeight)
            )

            // Expense bar
            val expenseHeight = (bucket.expense / maxValue * chartHeight).toFloat()
            drawRect(
                color = expenseColor,
                topLeft = Offset(groupLeft + gap + barWidth + gap, chartHeight - expenseHeight),
                size = Size(barWidth, expenseHeight)
            )

            // Month label
            val label = Formatters.formatDate(bucket.monthStartMillis, "MM/yy", useBengali = useBengaliDigits)
            val labelPaint = android.graphics.Paint().apply {
                textSize = 24f
                color = android.graphics.Color.GRAY
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                groupLeft + groupWidth / 2,
                canvasHeight - 4f,
                labelPaint
            )
        }
    }
}
