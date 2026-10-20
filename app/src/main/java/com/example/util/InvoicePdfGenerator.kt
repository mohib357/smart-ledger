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
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    private val amountFmt = DecimalFormat("#,##,##0.##", DecimalFormatSymbols.getInstance(Locale.US))
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private fun fmtAmt(amount: Double): String = "৳ " + amountFmt.format(amount)
    private fun fmtDate(millis: Long): String = dateFmt.format(Date(millis))

    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): PdfExportResult {
        val typeface: Typeface = ResourcesCompat.getFont(context, R.font.hind_siliguri)
            ?: run {
                Log.w("InvoicePdfGenerator", "hind_siliguri font null, using DEFAULT")
                Typeface.DEFAULT
            }

        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val margin = 40f
            var y = margin

            // Header paints
            val titlePaint = Paint().apply {
                this.typeface = typeface
                textSize = 18f
                isFakeBoldText = true
            }
            val subtitlePaint = Paint().apply {
                this.typeface = typeface
                textSize = 12f
            }
            val labelPaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
            }
            val valuePaint = Paint().apply {
                this.typeface = typeface
                textSize = 11f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                this.typeface = typeface
                textSize = 10f
                isFakeBoldText = true
            }
            val cellPaint = Paint().apply {
                this.typeface = typeface
                textSize = 10f
            }
            val totalPaint = Paint().apply {
                this.typeface = typeface
                textSize = 12f
                isFakeBoldText = true
            }
            val linePaint = Paint().apply {
                strokeWidth = 0.5f
            }

            // Company header
            canvas.drawText("হিসাব — Smart Ledger", margin, y, titlePaint)
            y += 22f
            canvas.drawText("চালান / Invoice", margin, y, subtitlePaint)
            y += 20f

            // Horizontal line
            canvas.drawLine(margin, y, 595f - margin, y, linePaint)
            y += 12f

            // Invoice meta
            canvas.drawText("চালান নম্বর:", margin, y, labelPaint)
            canvas.drawText(invoice.invoiceNumber, margin + 100f, y, valuePaint)
            canvas.drawText("তারিখ:", 350f, y, labelPaint)
            canvas.drawText(fmtDate(invoice.dateMillis), 400f, y, valuePaint)
            y += 18f

            canvas.drawText("গ্রাহক:", margin, y, labelPaint)
            canvas.drawText(invoice.customerName, margin + 100f, y, valuePaint)
            y += 18f

            if (invoice.customerPhone.isNotBlank()) {
                canvas.drawText("মোবাইল:", margin, y, labelPaint)
                canvas.drawText(invoice.customerPhone, margin + 100f, y, valuePaint)
                y += 18f
            }

            y += 8f
            canvas.drawLine(margin, y, 595f - margin, y, linePaint)
            y += 12f

            // Table header
            val col0 = margin
            val col1 = margin + 200f
            val col2 = margin + 300f
            val col3 = margin + 400f
            canvas.drawText("পণ্যের নাম", col0, y, headerPaint)
            canvas.drawText("পরিমাণ", col1, y, headerPaint)
            canvas.drawText("একক মূল্য", col2, y, headerPaint)
            canvas.drawText("মোট", col3, y, headerPaint)
            y += 6f
            canvas.drawLine(margin, y, 595f - margin, y, linePaint)
            y += 14f

            // Table rows
            items.forEach { item ->
                canvas.drawText(item.productName.take(28), col0, y, cellPaint)
                canvas.drawText(item.quantity.toString(), col1, y, cellPaint)
                canvas.drawText(fmtAmt(item.unitPrice), col2, y, cellPaint)
                canvas.drawText(fmtAmt(item.totalPrice), col3, y, cellPaint)
                y += 16f
                if (y > 800f) {
                    document.finishPage(page)
                    // Simple single-page — truncate if too many items (edge case)
                    return@forEach
                }
            }

            y += 4f
            canvas.drawLine(margin, y, 595f - margin, y, linePaint)
            y += 16f

            // Grand total
            canvas.drawText("মোট:", col2, y, totalPaint)
            canvas.drawText(fmtAmt(invoice.totalAmount), col3, y, totalPaint)
            y += 24f

            if (invoice.note.isNotBlank()) {
                canvas.drawText("নোট: ${invoice.note}", margin, y, labelPaint)
                y += 16f
            }

            document.finishPage(page)

            // Write to cache
            val cacheDir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val file = File(cacheDir, "invoice_${invoice.invoiceNumber}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            return PdfExportResult.Success(uri)
        } catch (e: Exception) {
            Log.e("InvoicePdfGenerator", "PDF generation failed", e)
            return PdfExportResult.Error("PDF তৈরি ব্যর্থ হয়েছে: ${e.message}")
        } finally {
            document.close()
        }
    }
}
