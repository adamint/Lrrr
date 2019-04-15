package com.adamratzman.lrrr.language.utils

import com.adamratzman.lrrr.globalLrrr
import com.adamratzman.lrrr.language.builtins.LrrrVariableResolverFunction
import com.adamratzman.lrrr.language.parsing.convertToNumber
import com.adamratzman.lrrr.language.parsing.findNextUnescapedStringCharacter
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

open class LrrrException(message: String?) : Exception(message)

class NeedsArgumentException(message: String?) : LrrrException(message)

class VariableNotFoundException(message: String?) : LrrrException(message)

open class Location(val start: Int, val end: Int) {
    override fun toString() = "[$start, $end]"
}

class StringLocation(start: Int, end: Int) : Location(start, end)

fun findStringLocations(code: String): List<StringLocation> {
    return code.findAllLocations('"').chunked(2)
        .map { StringLocation(it[0], if (it.size == 2) it[1] else code.length) }
}

fun findStringAndOtherLocations(code: String): List<Location> {
    val locations = mutableListOf<Location>()

    val stringLocations = findStringLocations(code)
    if (stringLocations.isEmpty()) return listOf(Location(0, code.lastIndex))
    stringLocations.forEachIndexed { i, strLocation ->
        if (i == 0 && strLocation.start != 0) locations.add(Location(0, stringLocations.first().start - 1))
        locations.add(strLocation)

        if (i != stringLocations.lastIndex && stringLocations[i + 1].start != strLocation.end) {
            locations.add(Location(strLocation.end + 1, stringLocations[i + 1].start - 1))
        } else if (i == stringLocations.lastIndex && strLocation.end < code.lastIndex) {
            locations.add(Location(strLocation.end + 1, code.lastIndex))
        }
    }

    return locations
}

fun String.findAllLocations(vararg chars: Char) =
    mapIndexed { i, char -> i to char }
        .filter { it.second in chars }.map { it.first }

fun String.first(char: Char): Int? = indexOfFirst { it == char }.let { if (it == -1) null else it }

fun findFirstFunction(code: String): Pair<Int, LrrrFunction>? {
    code.forEachIndexed { i, char ->
        globalLrrr.functions.find { it.identifier == char.toString() }?.let { return i to it }
    }
    return null
}

fun parseForLrrrValues(code: String): List<LrrrValue> {
    val values = mutableListOf<LrrrValue>()

    var workingCode = code
    while (workingCode.isNotEmpty()) {
        when {
            workingCode[0] == 't' || workingCode[0] == 'f' -> {
                values.add((workingCode[0] == 't').toLrrValue())
                workingCode = workingCode.substring(1)
            }
            workingCode.startsWith("null") -> {
                values.add(LrrrNull.lrrrNull)
                workingCode = workingCode.substring(4)
            }
            workingCode.startsWith("void") -> {
                values.add(LrrrVoid.lrrrVoid)
                workingCode = workingCode.substring(4)
            }
            workingCode.startsWith("'") -> workingCode = if (workingCode.length == 1) {
                values.add('\u0000'.toLrrValue())
                ""
            } else {
                values.add(workingCode[1].toLrrValue())
                if (workingCode.length == 2 || workingCode[2] != '\'') workingCode.substring(2)
                else workingCode.substring(3)
            }
            workingCode.startsWith("\"") -> {
                val endIndex = findNextUnescapedStringCharacter(workingCode.substring(1))?.plus(1)
                workingCode = if (endIndex == null) {
                    values.add(workingCode.substring(1).toLrrValue())
                    ""
                } else {
                    values.add(
                        workingCode.substring(1, endIndex)
                            .replace("\\\"", "\"").toLrrValue()
                    )
                    if (endIndex < workingCode.lastIndex) workingCode.substring(endIndex + 1)
                    else ""
                }
            }
            else -> {
                workingCode = try {
                    val decimalString = workingCode.takeWhile { it in '0'..'9' || it in 'A'..'F' || it == '.' || it == '-' }
                    val decimal = decimalString.convertToNumber()
                        ?: throw IllegalStateException("Unknown state $decimalString in $workingCode ($code)")
                    values.add(decimal.toLrrValue())
                    workingCode.substring(decimalString.length)
                } catch (e: IllegalStateException) {
                    values.add(
                        FunctionInvocation(
                            listOf(LrrrString(workingCode[0].toString())),
                            globalLrrr.functions.first { it is LrrrVariableResolverFunction })
                    )
                    workingCode.substring(1)
                }
            }
        }
    }

    return values
}

fun String.splitIndices(indices: List<Int>) = toList().splitIndices(indices).map { it.joinToString("") }

fun <T> List<T>.splitIndices(indicesTemp: List<Int>): List<List<T>> {
    val list = mutableListOf<List<T>>()
    val indices = indicesTemp.distinct().sorted()
    if (indices.isEmpty()) return listOf(this)
    if (indices.any { it < 0 || it > lastIndex }) throw IllegalArgumentException("Invalid input indices $indices")
    if (indices[0] != 0) list.add(subList(0, indices[0]))

    indices.forEachIndexed { num, index ->
        if (num < indices.lastIndex) list.add(subList(index + 1, indices[num + 1]))
        else if (num == indices.lastIndex && index < lastIndex) list.add(subList(index + 1, size))
    }

    return list
}

fun String.filterMapIndex(predicate: (Int, Char) -> Boolean) = toList().filterMapIndex(predicate)

fun <T> List<T>.filterMapIndex(predicate: (Int, T) -> Boolean): MutableList<Int> {
    val indices = mutableListOf<Int>()
    forEachIndexed { index, t -> if (predicate(index, t)) indices.add(index) }
    return indices
}

fun String.splitByFilter(predicate: (Int, Char) -> Boolean) = splitIndices(filterMapIndex(predicate))

fun <T> List<T>.splitByFilter(predicate: (Int, T) -> Boolean) = splitIndices(filterMapIndex(predicate))
