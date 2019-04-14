package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.LrrrFiniteSequence
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.LrrrVoid
import com.adamratzman.lrrr.language.types.NonadicFunction

class FirstGlobalElement : NonadicFunction("#", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().firstValue.value
    }
}

class SecondGlobalElement: NonadicFunction("|", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().contextValues[1].value
    }
}

class ParamSplitFunction : NonadicFunction(",", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrVoid.lrrrVoid
    }
}

class GetLastCurrentContextValue : NonadicFunction("ð",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.contextValues.last().value
    }
}

class GetAllCurrentContextValues : NonadicFunction("Ƒ",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.contextValues.map { it.value }.toMutableList())
    }
}