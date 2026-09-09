package com.example.consultapaises

import org.junit.Assert.assertEquals
import org.junit.Test

class CountryTextExporterTest {
    @Test fun exportsExactlyTheDisplayedTextWithWindowsLineBreaks() {
        val text = CountryTextExporter.content("Perú (PE)", "Capital: Lima\nPoblación: 34,000,000")
        assertEquals("Perú (PE)\r\n\r\nCapital: Lima\r\nPoblación: 34,000,000\r\n", text)
        assertEquals(text, String(text.toByteArray(Charsets.UTF_8), Charsets.UTF_8))
    }

    @Test fun filenameKeepsAccentsAndRemovesPathCharacters() {
        assertEquals("Consulta_Perú.txt", CountryTextExporter.filename(" Perú "))
        assertEquals("Consulta_A_B_C.txt", CountryTextExporter.filename("A/B:C"))
        assertEquals("Consulta_pais.txt", CountryTextExporter.filename(" "))
    }
}
