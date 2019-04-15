package com.adamratzman.lrrr.language.parsing

import com.adamratzman.lrrr.language.utils.findStringLocations
import org.junit.Test

internal class StructureParsingKtTest {
    val code = """
            #>|?#P@|P}}"a"P
        """.trimIndent().trim()

    @Test
    fun parseStructures() {
        println(parseStructures(code))
    }

    @Test
    fun getCorrespondingIndex() {
        println(getCorrespondingIndex(code.substring(8), contextOpeningCharArray, listOf('}')))
    }

    @Test
    fun splitIf() {
        val program = "SFKJSDFWE;\"dskfas;df\";SDKF\"hi\";"
        val stringLocations = findStringLocations(program)
        val split = program.splitIf(';') { index -> !isCharInString(index, stringLocations) }
        println(split)
    }

    @Test
    fun splitParameters() {
        println(splitParameters("8R,5,'as"))
        println(splitParameters("\"h,hel,asdfl,\",4,,'a,"))
    }

    @Test
    fun isCharInCharDeclaration() {
        println(isCharInCharDeclaration(2, "9'a"))
    }
}