package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    fun exportToXlsx(
        context: Context,
        transactions: List<TransactionEntity>,
        parties: List<PartyEntity>,
        products: List<ProductEntity>
    ): Uri {
        // Build shared strings table
        val sharedStrings = mutableListOf<String>()

        // Helper to get or add a shared string index
        fun si(value: String): Int = sharedStringIndex(value, sharedStrings)

        // Sheet 1: Transactions headers
        val txHeaders = listOf("ID", "Type", "Title", "Category", "Amount", "Wallet", "Date", "Note")
        txHeaders.forEach { si(it) }

        // Sheet 2: Parties headers
        val partyHeaders = listOf("ID", "Name", "Type", "Amount", "Phone", "Note")
        partyHeaders.forEach { si(it) }

        // Sheet 3: Products headers
        val productHeaders = listOf("ID", "Name", "Stock", "Buy Price", "Sell Price", "Category", "Expiry")
        productHeaders.forEach { si(it) }

        // Pre-populate shared strings for all data cells
        transactions.forEach { tx ->
            si(tx.type); si(tx.title); si(tx.category); si(tx.walletName); si(tx.note)
            si(formatDateUs(tx.dateMillis))
        }
        parties.forEach { p ->
            si(p.name); si(p.partyType); si(p.phone); si(p.note)
        }
        products.forEach { p ->
            si(p.name); si(p.category)
            if (p.expiryDate != null && p.expiryDate > 0) si(formatDateUs(p.expiryDate))
        }
        // Ensure empty string is available for nullable/empty cells
        si("")

        // Write to ZIP
        val outputFile = File(context.cacheDir, "smart_ledger_export.xlsx")
        ZipOutputStream(outputFile.outputStream().buffered()).use { zip ->

            // [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypesXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(rootRelsXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // xl/workbook.xml
            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(workbookXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // xl/_rels/workbook.xml.rels
            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(workbookRelsXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // xl/styles.xml
            zip.putNextEntry(ZipEntry("xl/styles.xml"))
            zip.write(stylesXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // xl/sharedStrings.xml
            zip.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zip.write(sharedStringsXml(sharedStrings).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // Sheet 1 — Transactions
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(buildTransactionsSheet(transactions, txHeaders, sharedStrings).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // Sheet 2 — Parties
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
            zip.write(buildPartiesSheet(parties, partyHeaders, sharedStrings).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // Sheet 3 — Products
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet3.xml"))
            zip.write(buildProductsSheet(products, productHeaders, sharedStrings).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        return FileProvider.getUriForFile(
            context,
            "com.aistudio.hisabkhata.mjxk.fileprovider",
            outputFile
        )
    }

    // ---- XML builders ----

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun rootRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbookXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Transactions" sheetId="1" r:id="rId1"/>
    <sheet name="Parties" sheetId="2" r:id="rId2"/>
    <sheet name="Products" sheetId="3" r:id="rId3"/>
  </sheets>
</workbook>"""

    private fun workbookRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun stylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="164" formatCode="dd/MM/yyyy"/>
  </numFmts>
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
  <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0"/>
  </cellXfs>
</styleSheet>"""

    private fun sharedStringsXml(strings: List<String>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">
""")
        strings.forEach { s ->
            sb.append("  <si><t xml:space=\"preserve\">${escapeXml(s)}</t></si>\n")
        }
        sb.append("</sst>")
        return sb.toString()
    }

    private fun buildTransactionsSheet(
        transactions: List<TransactionEntity>,
        headers: List<String>,
        sharedStrings: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append(sheetHeader())
        var rowIdx = 1
        // Header row
        sb.append("<row r=\"$rowIdx\">")
        headers.forEachIndexed { colIdx, h ->
            sb.append(stringCell(rowIdx, colIdx, sharedStrings.indexOf(h)))
        }
        sb.append("</row>\n")
        // Data rows
        transactions.forEach { tx ->
            rowIdx++
            sb.append("<row r=\"$rowIdx\">")
            sb.append(numberCell(rowIdx, 0, tx.id.toDouble()))
            sb.append(stringCell(rowIdx, 1, sharedStrings.indexOf(tx.type)))
            sb.append(stringCell(rowIdx, 2, sharedStrings.indexOf(tx.title)))
            sb.append(stringCell(rowIdx, 3, sharedStrings.indexOf(tx.category)))
            sb.append(numberCell(rowIdx, 4, tx.amount))
            sb.append(stringCell(rowIdx, 5, sharedStrings.indexOf(tx.walletName)))
            sb.append(stringCell(rowIdx, 6, sharedStrings.indexOf(formatDateUs(tx.dateMillis))))
            sb.append(stringCell(rowIdx, 7, sharedStrings.indexOf(tx.note)))
            sb.append("</row>\n")
        }
        sb.append(sheetFooter())
        return sb.toString()
    }

    private fun buildPartiesSheet(
        parties: List<PartyEntity>,
        headers: List<String>,
        sharedStrings: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append(sheetHeader())
        var rowIdx = 1
        sb.append("<row r=\"$rowIdx\">")
        headers.forEachIndexed { colIdx, h ->
            sb.append(stringCell(rowIdx, colIdx, sharedStrings.indexOf(h)))
        }
        sb.append("</row>\n")
        parties.forEach { p ->
            rowIdx++
            sb.append("<row r=\"$rowIdx\">")
            sb.append(numberCell(rowIdx, 0, p.id.toDouble()))
            sb.append(stringCell(rowIdx, 1, sharedStrings.indexOf(p.name)))
            sb.append(stringCell(rowIdx, 2, sharedStrings.indexOf(p.partyType)))
            sb.append(numberCell(rowIdx, 3, p.currentAmount))
            sb.append(stringCell(rowIdx, 4, sharedStrings.indexOf(p.phone)))
            sb.append(stringCell(rowIdx, 5, sharedStrings.indexOf(p.note)))
            sb.append("</row>\n")
        }
        sb.append(sheetFooter())
        return sb.toString()
    }

    private fun buildProductsSheet(
        products: List<ProductEntity>,
        headers: List<String>,
        sharedStrings: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append(sheetHeader())
        var rowIdx = 1
        sb.append("<row r=\"$rowIdx\">")
        headers.forEachIndexed { colIdx, h ->
            sb.append(stringCell(rowIdx, colIdx, sharedStrings.indexOf(h)))
        }
        sb.append("</row>\n")
        products.forEach { p ->
            rowIdx++
            sb.append("<row r=\"$rowIdx\">")
            sb.append(numberCell(rowIdx, 0, p.id.toDouble()))
            sb.append(stringCell(rowIdx, 1, sharedStrings.indexOf(p.name)))
            sb.append(numberCell(rowIdx, 2, p.stock.toDouble()))
            sb.append(numberCell(rowIdx, 3, p.buyPrice))
            sb.append(numberCell(rowIdx, 4, p.sellPrice))
            sb.append(stringCell(rowIdx, 5, sharedStrings.indexOf(p.category)))
            val expiryStr = if (p.expiryDate != null && p.expiryDate > 0) formatDateUs(p.expiryDate) else ""
            val expiryIdx = sharedStrings.indexOf(expiryStr).let { if (it < 0) sharedStrings.indexOf("") else it }
            sb.append(stringCell(rowIdx, 6, expiryIdx))
            sb.append("</row>\n")
        }
        sb.append(sheetFooter())
        return sb.toString()
    }

    // ---- Helpers ----

    private fun sheetHeader() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
"""

    private fun sheetFooter() = """</sheetData></worksheet>"""

    private fun colLetter(col: Int): String {
        var c = col
        var result = ""
        do {
            result = ('A' + c % 26) + result
            c = c / 26 - 1
        } while (c >= 0)
        return result
    }

    private fun cellRef(row: Int, col: Int) = "${colLetter(col)}$row"

    private fun stringCell(row: Int, col: Int, ssIndex: Int): String {
        val idx = if (ssIndex < 0) 0 else ssIndex
        return "<c r=\"${cellRef(row, col)}\" t=\"s\"><v>$idx</v></c>"
    }

    private fun numberCell(row: Int, col: Int, value: Double): String {
        return "<c r=\"${cellRef(row, col)}\"><v>${String.format(Locale.US, "%.2f", value)}</v></c>"
    }

    private fun escapeXml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatDateUs(millis: Long): String {
        val cal = Calendar.getInstance(Locale.US)
        cal.timeInMillis = millis
        return String.format(
            Locale.US, "%02d/%02d/%04d",
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    private fun sharedStringIndex(value: String, table: MutableList<String>): Int {
        val idx = table.indexOf(value)
        if (idx >= 0) return idx
        table.add(value)
        return table.size - 1
    }
}
