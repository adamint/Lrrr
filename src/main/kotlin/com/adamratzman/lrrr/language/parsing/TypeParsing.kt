package com.adamratzman.lrrr.language.parsing

import com.adamratzman.lrrr.language.types.*

fun Any?.toLrrValue(): LrrrValue = when {
    this is LrrrValue -> this
    this == null -> LrrrNull.lrrrNull
    this == true -> LrrrBoolean.lrrrTrue
    this == false -> LrrrBoolean.lrrrFalse
    this is String -> LrrrString(this)
    this is Number -> LrrrNumber(this.toDouble())
    this is Char -> LrrrChar(this)
    this is List<*> -> LrrrFiniteSequence(this.map { it.toLrrValue() }.toMutableList())
    else -> throw IllegalArgumentException("Unknown variable $this")
}

fun String.convertToNumber() =
    replace("A", "10")
        .replace("B", "11")
        .replace("C", "12")
        .replace("D", "13")
        .replace("E", "14")
        .replace("F", "15")
        .let { it.toDoubleOrNull() }