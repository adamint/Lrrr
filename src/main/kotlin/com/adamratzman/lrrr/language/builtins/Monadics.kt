package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

class LrrrVariableResolverFunction : MonadicFunction("", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.findContextValue { it.identifier == (arguments[0] as LrrrString).string }?.value
            ?: throw IllegalArgumentException("Variable $arguments not found in context $context")
    }
}

class IsInteger : MonadicFunction("I",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return (arguments[0] as LrrrNumber).isInteger().toLrrValue()
    }
}

class ToNumber : MonadicFunction("N",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val argument = arguments[0]
        return when(argument) {
            is LrrrNumber -> argument.number
            is LrrrBoolean -> if (argument.boolean) 1.0 else 0.0
            is LrrrString -> argument.string.length.toDouble()
            is LrrrFiniteSequence<*> -> argument.list.size.toDouble()
            else -> 0.0
        }.toLrrValue()
    }
}