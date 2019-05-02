package com.adamratzman.lrrr.language.parsing

import org.junit.Assert.assertEquals
import org.junit.Test

internal class TypeParsingKtTest {
    @Test
    fun convertToNumber() {
        val numbers = listOf(
            "A0",
            "134",
            "9B5C"
        ).map { it.convertToNumber() }

        assertEquals(listOf(100, 134, 911512).map { it.toDouble() }, numbers)
    }

    @Test
    fun toLrrValue() {
        println("1201023.9k".toLrrrValue())
        println(1293.toLrrrValue())
        println('a'.toLrrrValue())
    }
}