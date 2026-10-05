package com.nextstepai.paperless.documents.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomFieldValueTest {

    private fun parseDocumentIds(raw: String): List<Long> {
        if (raw.isBlank()) return emptyList()
        val clean = raw.trim().removePrefix("[").removeSuffix("]")
        return clean.split(",").mapNotNull { it.trim().trim('"', '\'').toLongOrNull() }
    }

    private fun serializeDocumentIds(ids: List<Long>): String {
        if (ids.isEmpty()) return ""
        return "[${ids.joinToString(",")}]"
    }

    private fun parseSelectOptions(rawJson: String?): List<String> {
        if (rawJson.isNullOrBlank()) return emptyList()
        val trimmed = rawJson.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed.removePrefix("[").removeSuffix("]")
                .split(",")
                .map { it.trim().trim('"', '\'') }
                .filter { it.isNotBlank() }
        }
        return trimmed.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    @Test
    fun testParseDocumentIds() {
        val parsed = parseDocumentIds("[12, 45, 99]")
        assertEquals(3, parsed.size)
        assertEquals(listOf(12L, 45L, 99L), parsed)
    }

    @Test
    fun testParseEmptyDocumentIds() {
        val parsed = parseDocumentIds("")
        assertTrue(parsed.isEmpty())
    }

    @Test
    fun testSerializeDocumentIds() {
        val serialized = serializeDocumentIds(listOf(5L, 10L))
        assertEquals("[5,10]", serialized)
    }

    @Test
    fun testParseSelectOptionsJson() {
        val options = parseSelectOptions("[\"Option A\", \"Option B\", \"Option C\"]")
        assertEquals(3, options.size)
        assertEquals(listOf("Option A", "Option B", "Option C"), options)
    }
}
