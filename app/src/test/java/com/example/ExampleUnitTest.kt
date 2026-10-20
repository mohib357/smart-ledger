package com.example

import com.example.data.model.PartyEntity
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBengaliNumberConversion() {
        val eng = "1234567890"
        val bng = Formatters.toBengaliDigits(eng)
        assertEquals("১২৩৪৫৬৭৮৯০", bng)

        val backToEng = Formatters.toEnglishDigits(bng)
        assertEquals(eng, backToEng)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = Formatters.formatTaka(122417.0, useBengali = true)
        assertTrue(formatted.contains("৳"))
        val digitsOnly = formatted.replace(",", "").replace(" ", "").replace("৳", "")
        assertEquals("১২২৪১৭", digitsOnly)
    }

    @Test
    fun testCalculatorExpressionEvaluation() {
        val res1 = Formatters.evaluateExpression("500 + 250 - 50")
        assertEquals(700.0, res1 ?: 0.0, 0.01)

        val res2 = Formatters.evaluateExpression("১০০ * ৫")
        assertEquals(500.0, res2 ?: 0.0, 0.01)
    }

    @Test
    fun testNomineeAccessSlipGeneration() {
        val parties = listOf(
            PartyEntity(name = "মাসুদ হুজুর", partyType = "PAYABLE", currentAmount = 40000.0),
            PartyEntity(name = "আমার আছে", partyType = "RECEIVABLE", currentAmount = 228608.0)
        )
        val slip = Formatters.generateNomineeAccessSlip(
            nomineeName = "আব্দুর রহমান",
            relation = "বড় ভাই",
            accessKey = "HSL-BR7890",
            accessScope = "PAYABLE_ONLY",
            totalPayable = 40000.0,
            totalReceivable = 228608.0,
            totalCash = 3000.0,
            note = "জরুরি ঋণগুলো দ্রুত পরিশোধ করে দিও",
            parties = parties
        )

        assertNotNull(slip)
        assertTrue(slip.contains("আব্দুর রহমান"))
        assertTrue(slip.contains("HSL-BR7890"))
        assertTrue(slip.contains("মাসুদ হুজুর"))
    }
}
