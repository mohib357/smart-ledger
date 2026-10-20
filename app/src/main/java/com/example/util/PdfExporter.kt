package com.example.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.TransactionEntity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

sealed class PdfExportResult {
    data class Success(val uri: Uri) : PdfExportResult()
    data class Error(val message: String) : PdfExportResult()
}

object PdfExporter {

    private val asciiAmountFmt = DecimalFormat(
        "#,##,##0.##",
        DecimalFormatSymbols.getInstance(Locale.US)
    )

    private fun formatAmountAscii(amount: Double): String =
        "৳ " + asciiAmountFmt.format(kotlin.math.abs(amount))

    private val asciiDateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private fun formatDateAscii(millis: Long): String = asciiDateFmt.format(java.util.Date(millis))

    fun generateTransactionReport(
        context: Context,
        transactions: List<TransactionEntity>,
        fromMillis: Long,
        toMillis: Long
    ): PdfExportResult {
        if (fromMillis > toMillis) {
            return PdfExportResult.Error("শেষ তারিখ শুরুর আগে হতে পারবে না")
        }

        val filtered = transactions
            .filter { it.dateMillis in fromMillis..toMillis }
            .sortedBy { it.dateMillis }

        if (filtered.isEmpty()) {
            return PdfExportResult.Error("এই সময়ের মধ্যে কোনো লেনদেন নেই")
        }

        val typeface: Typeface = ResourcesCompat.getFont(context, R.font.hind_siliguri)
            ?: run {
                Log.w("PdfExporter", "ResourcesCompat.getFont returned null, using DEFAULT")
                Typeface.DEFAULT
            }

        val document = PdfDocument()

        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 36f
            val contentWidth = pageWidth - margin * 2

            // Paint objects
            val titlePaint = Paint().apply {
                this.typeface = typeface
                textSize = 18f
                isFakeBoldText = true
                color = android.graphics.Color.BLACK
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
                color = android.graphics.Color.DKGRAY
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                this.typeface = typeface
                textSize = 12f
                isFakeBoldText = true
                color = android.graphics.Color.BLACK
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
                color = android.graphics.Color.BLACK
                isAntiAlias = true
            }
            val incomePaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
                color = android.graphics.Color.rgb(22, 163, 74)
                isAntiAlias = true
            }
            val expensePaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
                color = android.graphics.Color.rgb(220, 38, 38)
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                strokeWidth = 1f
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = margin + 10f

            // Draw header
            canvas.drawText("হিসাব — Smart Ledger", margin, yPos, titlePaint)
            yPos += 24f
            canvas.drawText("রিপোর্ট", margin, yPos, subtitlePaint)
            yPos += 18f
            canvas.drawText(
                "তারিখ: ${formatDateAscii(fromMillis)} — ${formatDateAscii(toMillis)}",
                margin, yPos, subtitlePaint
            )
            yPos += 18f
            canvas.drawText(
                "তৈরির সময়: ${formatDateAscii(System.currentTimeMillis())}",
                margin, yPos, subtitlePaint
            )
            yPos += 10f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            // Summary block
            val totalIncome = filtered.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = filtered.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val surplus = totalIncome - totalExpense

            canvas.drawText("মোট আয়: ${formatAmountAscii(totalIncome)}", margin, yPos, incomePaint)
            yPos += 16f
            canvas.drawText("মোট ব্যয়: ${formatAmountAscii(totalExpense)}", margin, yPos, expensePaint)
            yPos += 16f
            val surplusPaint = if (surplus >= 0) incomePaint else expensePaint
            canvas.drawText("উদ্বৃত্ত: ${formatAmountAscii(surplus)}", margin, yPos, surplusPaint)
            yPos += 16f
            canvas.drawText("মোট লেনদেন: ${filtered.size}", margin, yPos, bodyPaint)
            yPos += 10f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            // Table header
            val col1W = 70f
            val col2W = 150f
            val col3W = 100f
            val col4W = 50f
            val col5W = contentWidth - col1W - col2W - col3W - col4W

            canvas.drawText("তারিখ", margin, yPos, headerPaint)
            canvas.drawText("খাত/বিবরণ", margin + col1W, yPos, headerPaint)
            canvas.drawText("ক্যাটাগরি", margin + col1W + col2W, yPos, headerPaint)
            canvas.drawText("ধরন", margin + col1W + col2W + col3W, yPos, headerPaint)
            canvas.drawText("পরিমাণ", margin + col1W + col2W + col3W + col4W, yPos, headerPaint)
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 14f

            // Transaction rows
            val rowHeight = 16f
            for (tx in filtered) {
                if (yPos + rowHeight > pageHeight - margin) {
                    // New page
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin
                    canvas.drawText("— (পরবর্তী পৃষ্ঠা) —", margin, yPos, subtitlePaint)
                    yPos += 18f
                    // Repeat table header
                    canvas.drawText("তারিখ", margin, yPos, headerPaint)
                    canvas.drawText("খাত/বিবরণ", margin + col1W, yPos, headerPaint)
                    canvas.drawText("ক্যাটাগরি", margin + col1W + col2W, yPos, headerPaint)
                    canvas.drawText("ধরন", margin + col1W + col2W + col3W, yPos, headerPaint)
                    canvas.drawText("পরিমাণ", margin + col1W + col2W + col3W + col4W, yPos, headerPaint)
                    yPos += 6f
                    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
                    yPos += 14f
                }

                val amtPaint = if (tx.type == "INCOME") incomePaint else expensePaint
                val typeLabel = if (tx.type == "INCOME") "আয়" else "ব্যয়"
                val titleText = if (tx.title.length > 20) tx.title.take(18) + "…" else tx.title
                val catText = if (tx.category.length > 12) tx.category.take(10) + "…" else tx.category

                canvas.drawText(formatDateAscii(tx.dateMillis), margin, yPos, bodyPaint)
                canvas.drawText(titleText, margin + col1W, yPos, bodyPaint)
                canvas.drawText(catText, margin + col1W + col2W, yPos, bodyPaint)
                canvas.drawText(typeLabel, margin + col1W + col2W + col3W, yPos, bodyPaint)
                canvas.drawText(formatAmountAscii(tx.amount), margin + col1W + col2W + col3W + col4W, yPos, amtPaint)
                yPos += rowHeight
            }

            document.finishPage(page)

            // Write to cache file
            val file = java.io.File(context.cacheDir, "report_${System.currentTimeMillis()}.pdf")
            try {
                document.writeTo(file.outputStream())
            } catch (e: java.io.IOException) {
                Log.w("PdfExporter", "PDF write failed", e)
                return PdfExportResult.Error("PDF তৈরি ব্যর্থ হয়েছে")
            }

            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            return PdfExportResult.Success(uri)
        } finally {
            document.close()
        }
    }
}
