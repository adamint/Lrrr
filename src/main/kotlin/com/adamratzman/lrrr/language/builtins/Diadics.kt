package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrrValue
import com.adamratzman.lrrr.language.types.*

@Suppress("UNCHECKED_CAST")
class Addition : DiadicFunction("+", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        return when {
            first is LrrrNumber && second is LrrrNumber -> {
                if (first is LrrrChar) LrrrChar((first + second).numberInteger.toChar())
                else first + second
            }
            first is LrrrString -> {
                LrrrString(first.string + second.toString())
            }
            first is LrrrFiniteSequence<*> -> {
                first as LrrrFiniteSequence<LrrrValue>
                return if (second is LrrrFiniteSequence<*>) {
                    LrrrFiniteSequence((first.list + second.list).map { it.toLrrrValue() }.toMutableList())
                } else if (second is LrrrVoid) first
                else LrrrFiniteSequence((first.list + second).toMutableList())
            }
            first is LrrrBoolean -> {
                return when (second) {
                    is LrrrBoolean -> first.boolean && second.boolean
                    is LrrrNumber -> first.boolean && second.isInteger() && second.numberInteger == 1
                    else -> second != LrrrNull.lrrrNull && second != LrrrVoid.lrrrVoid
                }.toLrrrValue()
            }
            else -> LrrrFiniteSequence(mutableListOf(first, second))
        }
    }
}

class GreaterThan : DiadicFunction(">", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrNumber
        second as LrrrNumber
        return lrrrBooleanOf(first.number > second.number)
    }
}

class LessThan : DiadicFunction("<", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrNumber
        second as LrrrNumber
        return lrrrBooleanOf(first.number < second.number)
    }
}

class Equals : DiadicFunction("=",true,true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrType
        second as LrrrType
        return first.identical(second).toLrrrValue()
    }
}