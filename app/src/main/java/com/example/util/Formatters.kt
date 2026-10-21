package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = bengaliDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parseAmount(input: String): Double {
        val englishStr = toEnglishDigits(input.trim())
            .replace(",", "")
            .replace("৳", "")
            .replace("$", "")
            .replace("€", "")
            .replace("₹", "")
            .replace("﷼", "")
            .replace("د.إ", "")
            .replace("£", "")
            .trim()
        return englishStr.toDoubleOrNull() ?: 0.0
    }

    fun formatTaka(
        amount: Double,
        showSign: Boolean = false,
        useBengali: Boolean = true,
        currencySymbol: String = "৳"
    ): String {
        val isNegative = amount < 0
        val absVal = kotlin.math.abs(amount)
        val df = DecimalFormat("#,##,##0.##")
        val formattedNumber = df.format(absVal)
        val numStr = if (useBengali) toBengaliDigits(formattedNumber) else formattedNumber

        val sign = when {
            isNegative -> "-"
            showSign && amount > 0 -> "+"
            else -> ""
        }
        return "$sign$currencySymbol $numStr"
    }

    /**
     * RFC 4180 CSV field escaping. Wraps the value in double quotes if it contains a
     * comma, double-quote, CR, or LF, and escapes any internal double-quote by doubling it.
     * Use for every cell written to a CSV so user input (titles, notes, names) can't break
     * the column structure or inject extra rows.
     */
    fun escapeCsvField(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    fun formatDate(millis: Long, formatPattern: String = "dd/MM/yyyy", useBengali: Boolean = true): String {
        val sdf = SimpleDateFormat(formatPattern, Locale.getDefault())
        val dateStr = sdf.format(Date(millis))
        return if (useBengali) toBengaliDigits(dateStr) else dateStr
    }

    fun formatDateReadable(millis: Long, useBengali: Boolean = true): String {
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.forLanguageTag("bn-BD"))
        val dateStr = try {
            sdf.format(Date(millis))
        } catch (_: Exception) {
            SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(millis))
        }
        return if (useBengali) toBengaliDigits(dateStr) else dateStr
    }

    fun evaluateExpression(expr: String): Double? {
        val clean = toEnglishDigits(expr).replace("×", "*").replace("÷", "/").trim()
        return try {
            val tokens = mutableListOf<String>()
            var current = StringBuilder()
            for (ch in clean) {
                if (ch in "+-*/") {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString().trim())
                        current = StringBuilder()
                    }
                    tokens.add(ch.toString())
                } else if (!ch.isWhitespace()) {
                    current.append(ch)
                }
            }
            if (current.isNotEmpty()) {
                tokens.add(current.toString().trim())
            }

            if (tokens.isEmpty()) return null

            val firstPass = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token == "*" || token == "/") {
                    val prev = firstPass.removeAt(firstPass.lastIndex).toDouble()
                    val next = tokens[++i].toDouble()
                    val res = if (token == "*") prev * next else if (next != 0.0) prev / next else 0.0
                    firstPass.add(res.toString())
                } else {
                    firstPass.add(token)
                }
                i++
            }

            var total = firstPass[0].toDouble()
            var j = 1
            while (j < firstPass.size) {
                val op = firstPass[j]
                val next = firstPass[j + 1].toDouble()
                if (op == "+") total += next else if (op == "-") total -= next
                j += 2
            }
            total
        } catch (_: Exception) {
            null
        }
    }

    fun generateNomineeAccessSlip(
        nomineeName: String,
        relation: String,
        accessKey: String,
        accessScope: String,
        totalPayable: Double,
        totalReceivable: Double,
        totalCash: Double?,
        note: String,
        parties: List<com.example.data.model.PartyEntity> = emptyList()
    ): String {
        val scopeText = when (accessScope) {
            "PAYABLE_ONLY" -> "শুধুমাত্র ঋণ / দেনার হিসাব (যাতে মৃত্যুর পর ঋণ পরিশোধ করা যায়)"
            "RECEIVABLE_ONLY" -> "শুধুমাত্র পাওনা হিসাব (যাতে পাওনা টাকা আদায় করা যায়)"
            "DEBTS_AND_RECEIVABLES" -> "দেনা ও পাওনা উভয় খতিয়ান"
            else -> "সম্পূর্ণ হিসাব খাতা ও আর্থিক স্থিতি"
        }

        val sb = StringBuilder()
        sb.append("📋 【হিসাব - Smart Ledger】উত্তরাধিকার ও জরুরী আর্থিক এক্সেস স্লিপ\n\n")
        sb.append("সম্মানিত প্রতিনিধি: $nomineeName ($relation)\n")
        sb.append("এক্সেস অনুমতি স্তর: $scopeText\n")
        sb.append("আপনার গোপন ভেরিফিকেশন কোড: $accessKey\n\n")
        sb.append("📊 সংক্ষিপ্ত হিসাব বিবরণী:\n")

        val canViewPayables = accessScope == "ALL" || accessScope == "PAYABLE_ONLY" || accessScope == "DEBTS_AND_RECEIVABLES"
        val canViewReceivables = accessScope == "ALL" || accessScope == "RECEIVABLE_ONLY" || accessScope == "DEBTS_AND_RECEIVABLES"

        if (canViewPayables) {
            sb.append("• মোট দেনা (ঋণ যা পরিশোধ করতে হবে): ${formatTaka(totalPayable)}\n")
            val payableParties = parties.filter { it.partyType == "PAYABLE" && it.currentAmount > 0 }
            if (payableParties.isNotEmpty()) {
                payableParties.forEach { p ->
                    sb.append("   - ${p.name}: ${formatTaka(p.currentAmount)}${if (p.phone.isNotBlank()) " (মোবাইল: ${p.phone})" else ""}\n")
                }
            }
        }
        if (canViewReceivables) {
            sb.append("• মোট পাওনা (যা অন্যদের থেকে আদায় করতে হবে): ${formatTaka(totalReceivable)}\n")
            val receivableParties = parties.filter { it.partyType == "RECEIVABLE" && it.currentAmount > 0 }
            if (receivableParties.isNotEmpty()) {
                receivableParties.forEach { p ->
                    sb.append("   - ${p.name}: ${formatTaka(p.currentAmount)}${if (p.phone.isNotBlank()) " (মোবাইল: ${p.phone})" else ""}\n")
                }
            }
        }
        if (totalCash != null && (accessScope == "ALL")) {
            sb.append("• বর্তমান ক্যাশ ও ব্যাংক স্থিতি: ${formatTaka(totalCash)}\n")
        }

        if (note.isNotBlank()) {
            sb.append("\n📝 বিশেষ নির্দেশিকা / ওসিয়ত:\n\"$note\"\n")
        }

        sb.append("\n(হিসাব - Smart Ledger অ্যাপে আপনার গোপন কোড প্রদান করে সম্পূর্ণ লাইভ তথ্য দেখতে পারবেন।)")
        return sb.toString()
    }

    fun parseVoiceInput(text: String): VoiceParseResult {
        val normalized = toEnglishDigits(text)
        // Detect amount: first sequence of digits (with optional decimal)
        val amountRegex = Regex("""[\d]+(?:[.,]\d+)?""")
        val amount = amountRegex.find(normalized)?.value ?: ""
        // Detect type
        val type = when {
            text.contains("আয়") || text.contains("income", ignoreCase = true) || text.contains("জমা") -> "INCOME"
            text.contains("ব্যয়") || text.contains("expense", ignoreCase = true) ||
                text.contains("খরচ") || text.contains("বের") -> "EXPENSE"
            else -> null
        }
        // Detect category: match against Categories.unified
        val category = com.example.data.model.Categories.unified.firstOrNull { cat ->
            text.contains(cat, ignoreCase = true)
        }
        return VoiceParseResult(amount = amount, type = type, category = category)
    }
}

data class VoiceParseResult(val amount: String, val type: String?, val category: String?)
