package com.adamratzman.lrrr.language.evaluation

import com.adamratzman.lrrr.language.parsing.parseStructures
import org.junit.Test

internal class EvaluationConversionKtTest {
    val code =
            /*replaceShorteners("""
                #>|?#P@|Pç"a"P
            """.trimIndent().trim())*/
        "#>|?fP;2.0P"

    @Test
    fun toEvaluatableObject() {
        val structures = parseStructures(code)
         val evaluatable = toEvaluatableObject(parseStructures(code))

        println("Structures:\n$structures")
          println("Evaluatable:\n$evaluatable")
    }
}