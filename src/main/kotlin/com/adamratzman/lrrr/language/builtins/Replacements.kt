package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.isCharInString
import com.adamratzman.lrrr.language.utils.findStringLocations

val replacements = listOf(
    "ç" to "}}"
)

fun replaceShorteners(program: String): String {
    var replaced = program
    var stringLocations = findStringLocations(replaced)

    var count = 0
    replacements.forEach { replacement ->
        val replace = replacement.first
        replace.toRegex().findAll(replaced).forEach { matchResult ->
            if (!isCharInString(matchResult.range.first, stringLocations)) {
                replaced = replaced.replaceRange(matchResult.range, replacement.second)
                stringLocations = findStringLocations(replaced)
                count++
            }
        }
    }

    if (count == 0) return replaced
    return replaceShorteners(replaced)
}