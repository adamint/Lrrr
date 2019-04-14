package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.LrrrString
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.MonadicFunction

class LrrrVariableResolverFunction : MonadicFunction("", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.findContextValue { it.identifier == (arguments[0] as LrrrString).string }?.value
            ?: throw IllegalArgumentException("Variable $arguments not found in context $context")
    }
}