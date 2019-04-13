package com.adamratzman.lrrr.language.utils

import org.junit.Test

internal class ParsingUtilsKtTest {

    @Test
    fun findStringLocations() {
        println(findStringLocations(""""String 1", "" "String 3" 49234"String4"""))
        println(findStringLocations("\"String\""))
        println(findStringLocations("\"String"))
    }

    @Test
    fun findStringAndOtherLocations() {
        println(findStringAndOtherLocations("\"hello\"abc\"world\"123\""))
    }

    @Test
    fun findAllLocations() {
        println("ABCDEFGHJIKLAAA".findAllLocations('A', 'C'))
    }

    @Test
    fun parseForLrrrValues() {
        println(parseForLrrrValues("1.4'a\"hello\"A"))
    }

    @Test
    fun splitIndices() {
        val numbers = listOf(0, 1, 2, 3, 4, 5, 6, 7)
        println(numbers.splitIndices(listOf(3, 6)))

        val string = "Hello, World! This is a test L string"
        println(string.filterMapIndex { i, char -> char == 'l' || char == 'L' })
        println(string.splitIndices(string.filterMapIndex { i, char -> char == 'l' || char == 'L' }))
    }
}