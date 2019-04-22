package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

class LrrrVariableResolverFunction : MonadicFunction("", false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        val string = (argument as? LrrrString)?.string ?: (argument as? LrrrChar)?.char?.toString() ?: (argument as LrrrNumber).number.toChar().toString()
        return context.findContextValue { it.identifier == string }?.value
            ?: throw IllegalArgumentException("Variable $argument ($string) not found in context $context")
    }
}

class IsInteger : MonadicFunction("I",true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        return argument.isInteger().toLrrValue()
    }
}


class Find : DiadicFunction("f",true,true) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        val string = (first as LrrrString).string
        val toFind = second.toString()

        val foundStarts = string.toRegex().findAll(toFind).map { it.range.first.toLrrValue() }.toMutableList()
        return when {
            foundStarts.isEmpty() -> LrrrNull.lrrrNull
            foundStarts.size == 1 -> return foundStarts.first()
            else -> return LrrrFiniteSequence(foundStarts)
        }
    }
}