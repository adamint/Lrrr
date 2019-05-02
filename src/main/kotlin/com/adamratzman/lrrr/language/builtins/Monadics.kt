package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrrValue
import com.adamratzman.lrrr.language.types.*

class LrrrVariableResolverFunction : MonadicFunction("", false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        val string = (argument as? LrrrString)?.string ?: (argument as? LrrrChar)?.char?.toString()
        ?: (argument as LrrrNumber).number.toChar().toString()
        return context.findContextValue { it.identifier == string }?.value
            ?: throw IllegalArgumentException("Variable $argument ($string) not found in context $context")
    }
}

class IsInteger : MonadicFunction("I", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        return argument.isInteger().toLrrrValue()
    }
}