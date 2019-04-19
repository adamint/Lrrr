package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.mod

@Suppress("UNCHECKED_CAST")
class Addition : DiadicFunction("+", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0]
        val second = arguments[1]

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
                    LrrrFiniteSequence((first.list + second.list).map { it.toLrrValue() }.toMutableList())
                } else LrrrFiniteSequence((first.list + second).toMutableList())
            }
            first is LrrrBoolean -> {
                return when (second) {
                    is LrrrBoolean -> first.boolean && second.boolean
                    is LrrrNumber -> first.boolean && second.isInteger() && second.numberInteger == 1
                    else -> second != LrrrNull.lrrrNull && second != LrrrVoid.lrrrVoid
                }.toLrrValue()
            }
            else -> LrrrFiniteSequence(mutableListOf(first, second))
        }
    }
}

class Subtraction : DiadicFunction("⁻", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0]
        val second = arguments[1]

        return when {
            first is LrrrNumber && second is LrrrNumber -> {
                if (first is LrrrChar) LrrrChar((first + second).numberInteger.toChar())
                else first + second
            }
            first is LrrrBoolean -> {
                return when (second) {
                    is LrrrBoolean -> first.boolean || second.boolean
                    is LrrrNumber -> first.boolean ||( second.isInteger() && second.numberInteger == 1)
                    else -> second != LrrrNull.lrrrNull && second != LrrrVoid.lrrrVoid
                }.toLrrValue()
            }
            else -> LrrrFiniteSequence(mutableListOf(first, second))
        }
    }
}

class Division : DiadicFunction("÷", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return first / second
    }
}

class Modulus : DiadicFunction("%", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return mod(first.number, second.number).toLrrValue()
    }
}


class GreaterThan : DiadicFunction(">", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber
        return lrrrBooleanOf(first.number > second.number)
    }
}

class LessThan : DiadicFunction("<", true, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber
        return lrrrBooleanOf(first.number < second.number)
    }
}

class Equals : DiadicFunction("=",true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrType
        val second = arguments[1] as LrrrType
        return first.identical(second).toLrrValue()
    }
}