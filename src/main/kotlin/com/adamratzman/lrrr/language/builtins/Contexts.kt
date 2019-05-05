package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.*

class GetAllContextsValues : NonadicFunction("o", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        val values = context.getAllContextsValues().map { it.value }.toMutableList()
        return if (values.size == 1) values.first()
        else LrrrFiniteSequence(values)
    }
}

class GetCurrentContext : NonadicFunction("Ḥ", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.contextValues)
    }
}

class GetGlobalContext : NonadicFunction("Ḃ", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.getGlobalContext().contextValues)
    }
}

class GetLastCurrentContextValue : NonadicFunction("ð", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return context.contextValues.find { it.identifier == "v" }?.value
            ?: context.contextValues.find { it.identifier == "i" }?.value ?: context.contextValues.lastOrNull()?.value
            ?: context.backreference
            ?: context.parentContext?.contextValues?.lastOrNull()?.value
            ?: context.parentContext?.backreference
            ?: throw IllegalStateException()
    }
}

class GetAllCurrentContextValues : NonadicFunction("Ƒ", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(context.contextValues.map { it.value }.toMutableList())
    }
}

class FirstGlobalElement : NonadicFunction("#", false) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().firstValue.value
    }
}

class SecondGlobalElement : NonadicFunction("|", false) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return context.getGlobalContext().contextValues[1].value
    }
}


class AddToCurrentContext : PolyadicFunction("Ƈ", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        context.contextValues.addAll(arguments.map { LrrrVariable(null, it) })
        return LrrrVoid.lrrrVoid
    }
}

class AddToGlobalContext : PolyadicFunction("ɠ", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        context.getGlobalContext().contextValues.addAll(arguments.map { LrrrVariable(null, it) })
        return LrrrVoid.lrrrVoid
    }
}

class LastContextValue : NonadicFunction("¡", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return context.contextValues.lastOrNull() ?: LrrrNull.lrrrNull
    }
}

class GetBackreference : NonadicFunction("$", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        return backreference
            ?: context.parentContext?.contextValues?.lastOrNull()?.value
            ?: context.parentContext?.backreference
            ?: LrrrNull.lrrrNull
    }
}

class GetParentParentContextLastValue : NonadicFunction("§", true) {
    override fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue {
        val parentParent = context.parentContext?.parentContext ?: return LrrrNull.lrrrNull
        println(context)
        return parentParent.backreference
            ?: parentParent.parentContext?.contextValues?.lastOrNull()?.value
            ?: parentParent.parentContext?.backreference
            ?: parentParent.contextValues.lastOrNull()?.value
            ?: LrrrNull.lrrrNull
    }
}
