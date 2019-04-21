package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

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
class Multiplication : DiadicFunction("*",true,true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        second as LrrrNumber
        return when (first) {
            is LrrrNumber -> first * second
            is LrrrString ->LrrrFiniteSequence( (1..second.numberInteger).map { first }.toMutableList())
            is LrrrFiniteSequence<*> -> {
                first as LrrrFiniteSequence<LrrrValue>
                (0..first.list.lastIndex).forEach { index ->
                    first.list.set(index, evaluate(first.list[index],second, context))
                }
                first
            }
            else -> throw IllegalArgumentException()
        }
    }
}

class ToNumber : MonadicFunction("N",true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when(argument) {
            is LrrrNumber -> argument.number
            is LrrrBoolean -> if (argument.boolean) 1.0 else 0.0
            is LrrrString -> argument.string.length.toDouble()
            is LrrrFiniteSequence<*> -> argument.list.size.toDouble()
            else -> 0.0
        }.toLrrValue()
    }
}

class ToChar : MonadicFunction("C",true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        return when (argument) {
            is LrrrNumber -> LrrrChar(argument.numberInteger.toChar())
            is LrrrString -> LrrrChar(argument.string[0])
            else -> throw IllegalArgumentException()
        }
    }
}