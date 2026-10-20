package com.example

import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testBengaliDigitsConversion() {
        assertEquals("১২৩৪৫৬৭৮৯০", Formatters.toBengaliDigits("1234567890"))
        assertEquals("1234567890", Formatters.toEnglishDigits("১২৩৪৫৬৭৮৯০"))
    }

    @Test
    fun testParseAmount() {
        assertEquals(1250.0, Formatters.parseAmount("1250"), 0.001)
        assertEquals(1250.0, Formatters.parseAmount("১২৫০"), 0.001)
        assertEquals(254344.0, Formatters.parseAmount("২৫৪,৩৪৪"), 0.001)
    }

    @Test
    fun testExpressionEvaluation() {
        assertEquals(1550.0, Formatters.evaluateExpression("1200 + 350") ?: 0.0, 0.001)
        assertEquals(1000.0, Formatters.evaluateExpression("১২০০ - ২০০") ?: 0.0, 0.001)
        assertEquals(6000.0, Formatters.evaluateExpression("2000 * 3") ?: 0.0, 0.001)
    }
}
