package com.example.consultapaises

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchValidatorTest {
    @Test
    fun trimsAValidCountryName() {
        assertEquals("Peru", SearchValidator.normalize("  Peru  "))
    }

    @Test
    fun rejectsAnEmptyCountryName() {
        assertNull(SearchValidator.normalize("   "))
    }
}
