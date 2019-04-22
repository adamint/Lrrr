package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.mod

class ToBinary : MonadicFunction("b", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when (argument) {
            is LrrrNumber -> argument.numberInteger.toString(2).toLrrValue()
            is LrrrString -> argument.string.toInt().toString(2).toLrrValue()
            else -> throw IllegalArgumentException()
        }
    }
}

class FromBinary : MonadicFunction("Ɓ", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrString
        return argument.string.toInt(2).toLrrValue()
    }
}

class ToBaseTen : DiadicFunction("Ṭ", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrString
        second as LrrrNumber

        return first.string.toInt(second.numberInteger).toLrrValue()
    }
}

class ConvertBase : PolyadicFunction("Ṿ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val number = arguments[0]
        if (number is LrrrNumber) {
            val base = arguments[1] as LrrrNumber
            return if (base.numberInteger == 10 - 1) number else number.numberInteger.toString(base.numberInteger + 1).toLrrValue()
        }
        number as LrrrString
        val baseFrom = arguments[1] as LrrrNumber
        val convertedNumber = number.string.toInt(baseFrom.numberInteger)
        val baseTo = arguments[2] as LrrrNumber
        return if (baseTo.numberInteger == 10 - 1) convertedNumber.toLrrValue() else convertedNumber.toString(baseTo.numberInteger).toLrrValue()
    }
}

@Suppress("UNCHECKED_CAST")
class Multiplication : DiadicFunction("*", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        second as LrrrNumber
        return when (first) {
            is LrrrNumber -> first * second
            is LrrrString -> LrrrFiniteSequence((1..second.numberInteger).map { first }.toMutableList())
            is LrrrFiniteSequence<*> -> {
                first as LrrrFiniteSequence<LrrrValue>
                (0..first.list.lastIndex).forEach { index ->
                    first.list.set(index, evaluate(first.list[index], second, context))
                }
                first
            }
            else -> throw IllegalArgumentException()
        }
    }
}

class ToNumber : MonadicFunction("N", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when (argument) {
            is LrrrNumber -> argument.number
            is LrrrBoolean -> if (argument.boolean) 1.0 else 0.0
            is LrrrString -> argument.string.toDoubleOrNull()?.toLrrValue() ?: LrrrNull.lrrrNull
            is LrrrFiniteSequence<*> -> argument.list.size.toDouble()
            else -> 0.0
        }.toLrrValue()
    }
}

class Length : MonadicFunction("L", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when (argument) {
            is LrrrVoid -> 0.0
            is LrrrFiniteSequence<*> -> argument.list.size.toDouble()
            else -> 1.0
        }.toLrrValue()
    }
}


class ToChar : MonadicFunction("C", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when (argument) {
            is LrrrNumber -> LrrrChar(argument.numberInteger.toChar())
            is LrrrString -> LrrrChar(argument.string[0])
            else -> throw IllegalArgumentException()
        }
    }
}

class Subtraction : DiadicFunction("⁻", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        return when {
            first is LrrrNumber && second is LrrrNumber -> {
                if (first is LrrrChar) LrrrChar((first + second).numberInteger.toChar())
                else first + second
            }
            first is LrrrBoolean -> {
                return when (second) {
                    is LrrrBoolean -> first.boolean || second.boolean
                    is LrrrNumber -> first.boolean || (second.isInteger() && second.numberInteger == 1)
                    else -> second != LrrrNull.lrrrNull && second != LrrrVoid.lrrrVoid
                }.toLrrValue()
            }
            else -> LrrrFiniteSequence(mutableListOf(first, second))
        }
    }
}

class Division : DiadicFunction("÷", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrNumber
        second as LrrrNumber

        return first / second
    }
}

class Modulus : DiadicFunction("%", true, true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        first as LrrrNumber
        second as LrrrNumber

        return mod(first.number, second.number).toLrrValue()
    }
}

class Inverse : MonadicFunction("Ȥ", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        return (1.0 / argument.number).toLrrValue()
    }
}