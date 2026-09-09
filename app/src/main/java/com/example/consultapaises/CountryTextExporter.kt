package com.example.consultapaises

object CountryTextExporter {
    fun content(title: String, details: String): String =
        "$title\r\n\r\n${details.replace("\r\n", "\n").replace("\n", "\r\n")}\r\n"

    fun filename(countryName: String): String {
        val safeName = countryName.trim().replace(Regex("[^\\p{L}\\p{N}._ -]"), "_")
            .take(80).ifBlank { "pais" }
        return "Consulta_${safeName}.txt"
    }
}
