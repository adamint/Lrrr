package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.splitIndices

class Clear : NonadicFunction("c",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments[0] as LrrrFiniteSequence<*>
        sequence.list.clear()
        return LrrrVoid.lrrrVoid
    }
}

@Suppress("UNCHECKED_CAST")
class AddToSequence : PolyadicFunction("a", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<Evaluatable>
        sequence.list.addAll(arguments)
        return LrrrVoid.lrrrVoid
    }
}

@Suppress("UNCHECKED_CAST")
class RemoveFromSequence : PolyadicFunction("r", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        val indices = arguments.filter { it is LrrrNumber }.map { (it as LrrrNumber).numberInteger }
        sequence.list = sequence.list.splitIndices(indices).flatten().toMutableList()
        return LrrrVoid.lrrrVoid
    }
}

@Suppress("UNCHECKED_CAST")
class GetFromSequenceByIndex : PolyadicFunction("g",true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        val indices = arguments.subList(0,arguments.lastIndex).map { it as LrrrNumber }.map { it.numberInteger }

        val result = indices.map { sequence.list[it] }
        if (result.isEmpty()) return LrrrNull.lrrrNull
        else if (result.size == 1) return result.first()
        else return LrrrFiniteSequence(result.toMutableList())
    }
}

// class Reverse:MonadicFunction("")