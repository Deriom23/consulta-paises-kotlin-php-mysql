package com.example.consultapaises

object SearchValidator {
    fun normalize(value: String): String? = value.trim().takeIf { it.isNotEmpty() }
}
