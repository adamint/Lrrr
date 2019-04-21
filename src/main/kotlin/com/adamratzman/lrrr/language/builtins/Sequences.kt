package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.splitIndices

class Clear : MonadicFunction("c", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrFiniteSequence<*>
        argument.list.clear()
        return argument
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
class GetFromSequenceByIndex : PolyadicFunction("g", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        val indices = arguments.subList(0, arguments.lastIndex).map { it as LrrrNumber }.map { it.numberInteger }

        val result = indices.map { sequence.list[it] }
        return if (result.isEmpty()) LrrrNull.lrrrNull
        else if (result.size == 1) result.first()
        else LrrrFiniteSequence(result.toMutableList())
    }
}

// class Reverse:MonadicFunction("")

@Suppress("UNCHECKED_CAST")
class SubsequenceFunction : PolyadicFunction("l", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        return when {
            arguments.size == 1 -> sequence
            arguments.size == 2 -> LrrrFiniteSequence(
                sequence.list.subList(
                    (arguments[0] as LrrrNumber).numberInteger,
                    sequence.list.size
                ).toMutableList()
            )
            else -> LrrrFiniteSequence(
                sequence.list.subList(
                    (arguments[0] as LrrrNumber).numberInteger,
                    (arguments[1] as LrrrNumber).numberInteger
                ).toMutableList()
            )
        }
    }
}

class RangeFunction : MonadicFunction("R", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((1..number).map { it.toLrrValue() }.toMutableList())
    }
}

class ExclusiveBoundsRange : MonadicFunction("B", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((2 until number).map { it.toLrrValue() }.toMutableList())
    }
}


class UntilFunction : MonadicFunction("U", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((0 until number).map { it.toLrrValue() }.toMutableList())
    }
}

@Suppress("UNCHECKED_CAST")
class Sum : MonadicFunction("S", true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrFiniteSequence<LrrrValue>
        val list = argument.list
        return list.sumByDouble {
            when (it) {
                is LrrrNumber -> it.number
                is LrrrFiniteSequence<*> -> it.list.size.toDouble()
                is LrrrString -> it.string.length.toDouble()
                is LrrrBoolean -> if (it.boolean) 1.0 else 0.0
                is LrrrNull, is LrrrVoid -> 0.0
                else -> 0.0
            }
        }.toLrrValue()
    }
}