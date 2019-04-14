package com.adamratzman.lrrr.language.types

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext

data class LrrrVariable(var identifier: String?, var value: LrrrValue):LrrrValue() {
    override fun evaluate(context: LrrrContext): LrrrValue = this

    val valueAsLrrrNumber get() = value as LrrrNumber
}

abstract class LrrrValue : Evaluatable

abstract class LrrrType : LrrrValue() {
    abstract fun identical(other: Any?): Boolean
}

class LrrrString(string: String) : LrrrFiniteSequence<Char>(string.toCharArray().toMutableList()) {
    override fun evaluate(context: LrrrContext) = this

    val string get() = list.joinToString("")

    override fun identical(other: Any?) = other is LrrrString && string == other.string
    override fun toString() = list.joinToString("")
}

open class LrrrNumber(val number: Double) : LrrrType() {
    val numberInteger = number.toInt()
    fun isInteger() = number.toInt().toDouble() == number

    override fun identical(other: Any?) = other is LrrrNumber && number == other.number
    override fun toString() = if (isInteger()) numberInteger.toString() else number.toString()
    override fun evaluate(context: LrrrContext) = this

    operator fun plus(other: LrrrNumber) = LrrrNumber(number + other.number)
    operator fun minus(other: LrrrNumber) = LrrrNumber(number - other.number)
    operator fun times(other: LrrrNumber) = LrrrNumber(number * other.number)
    operator fun div(other: LrrrNumber) = LrrrNumber(number / other.number)
}

class LrrrChar(val char: Char) : LrrrNumber(char.toInt().toDouble()) {
    override fun toString() = char.toString()
    override fun evaluate(context: LrrrContext) = this
}

class LrrrNull private constructor() : LrrrVoid() {
    override fun identical(other: Any?) = other == null || other is LrrrNull
    override fun toString() = "null"
    override fun evaluate(context: LrrrContext) = this

    companion object {
        val lrrrNull = LrrrNull()
    }
}

open class LrrrVoid internal constructor():LrrrType() {
    override fun identical(other: Any?) = other is LrrrVoid
    override fun toString() = "void"
    override fun evaluate(context: LrrrContext) = this

    companion object {
        val lrrrVoid = LrrrVoid()
    }
}

class LrrrBoolean private constructor(val boolean: Boolean) : LrrrType() {
    override fun identical(other: Any?) =
        if (other is LrrrBoolean) other.boolean == boolean else if (boolean) other != null else other == null

    override fun toString() = boolean.toString()
    override fun evaluate(context: LrrrContext) = this

    companion object {
        val lrrrTrue = LrrrBoolean(true)
        val lrrrFalse = LrrrBoolean(false)
    }
}

fun lrrrBooleanOf(boolean: Boolean) = if (boolean) LrrrBoolean.lrrrTrue else LrrrBoolean.lrrrFalse
