package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.*

class GetAllContextsValues : NonadicFunction("o",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val values = context.getAllContextsValues().map { it.value }.toMutableList()
        return if (values.size == 1) values.first()
        else LrrrFiniteSequence(values)
    }
}

class GetCurrentContext : NonadicFunction("Ḥ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.contextValues)
    }
}

class GetGlobalContext : NonadicFunction("Ḃ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.getGlobalContext().contextValues)
    }
}

class GetLastCurrentContextValue : NonadicFunction("ð", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.contextValues.last().value
    }
}

class GetAllCurrentContextValues : NonadicFunction("Ƒ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.contextValues.map { it.value }.toMutableList())
    }
}

class FirstGlobalElement : NonadicFunction("#", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().firstValue.value
    }
}

class SecondGlobalElement : NonadicFunction("|", false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().contextValues[1].value
    }
}


class AddToCurrentContext : PolyadicFunction("Ɓ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        context.contextValues.addAll(arguments.map { LrrrVariable(null, it) })
        return LrrrVoid.lrrrVoid
    }
}

class AddToGlobalContext : PolyadicFunction("ɠ", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        context.getGlobalContext().contextValues.addAll(arguments.map { LrrrVariable(null, it) })
        return LrrrVoid.lrrrVoid
    }
}

class FirstParent : NonadicFunction("$",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return context.parentContext?.firstValue?.value ?: LrrrNull.lrrrNull
    }
}