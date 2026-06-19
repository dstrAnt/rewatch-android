package it.polimi.rewatch.common

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test suite for common utility and extension functions.
 * Verifies the correctness of general data formatting logic used throughout the application.
 */
class UtilityTest {

    /**
     * Verifies the [formatDate] string extension function.
     * Ensures that valid date strings are successfully converted to the specified target pattern
     * (e.g., "yyyy-MM-dd" to "dd/MM/yyyy"), and that invalid or unparseable date strings gracefully
     * fall back by returning the original input string.
     */
    @Test
    fun testFormatDate() {
        val inputDate = "2026-06-17"
        val outputDate = inputDate.formatDate("dd/MM/yyyy")
        assertEquals("17/06/2026", outputDate)

        val invalidDate = "not-a-date"
        assertEquals("not-a-date", invalidDate.formatDate("dd/MM/yyyy"))
    }

    /**
     * Verifies the [getShortNotation] numeric extension function.
     * Ensures that large numbers are correctly abbreviated into human-readable string formats
     * (such as billions, millions, and thousands/k), small numbers are kept as is,
     * and exactly zero is represented by a dash (" - ").
     */
    @Test
    fun testGetShortNotation() {
        assertEquals("1.5 billion", 1_500_000_000.0.getShortNotation())
        assertEquals("1.2 million", 1_200_000.0.getShortNotation())
        assertEquals("50k", 50_000.0.getShortNotation())
        assertEquals("999", 999.0.getShortNotation())
        assertEquals(" - ", 0.0.getShortNotation())
    }
}