package it.polimi.rewatch.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilityTest {

    @Test
    fun testFormatDate() {
        val inputDate = "2026-06-17"
        val outputDate = inputDate.formatDate("dd/MM/yyyy")
        assertEquals("17/06/2026", outputDate)

        val invalidDate = "not-a-date"
        assertEquals("not-a-date", invalidDate.formatDate("dd/MM/yyyy"))
    }

    @Test
    fun testGetShortNotation() {
        assertEquals("1.5 billion", 1_500_000_000.0.getShortNotation())
        assertEquals("1.2 million", 1_200_000.0.getShortNotation())
        assertEquals("50k", 50_000.0.getShortNotation())
        assertEquals("999", 999.0.getShortNotation())
        assertEquals(" - ", 0.0.getShortNotation())
    }
}