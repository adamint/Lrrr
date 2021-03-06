package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrrValue
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.splitIndices

class Clear : MonadicFunction("c", true, true) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrFiniteSequence<*>
        argument.list.clear()
        return argument
    }
}

@Suppress("UNCHECKED_CAST")
class AddToSequence : PolyadicFunction("a", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<Evaluatable>
        sequence.list.addAll(arguments)
        return sequence
    }
}

@Suppress("UNCHECKED_CAST")
class RemoveFromSequence : PolyadicFunction("r", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        val indices = arguments.filter { it is LrrrNumber }.map { (it as LrrrNumber).numberInteger }
        sequence.list = sequence.list.splitIndices(indices).flatten().toMutableList()
        return sequence
    }
}

@Suppress("UNCHECKED_CAST")
class GetOneFromSequenceByIndex : DiadicFunction("g", true, true, false) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        val sequence = first as? LrrrSequence<LrrrValue> ?: second as LrrrSequence<LrrrValue>
        val index = (first as? LrrrNumber ?: second as LrrrNumber).numberInteger

        return sequence.get(index)
    }

    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrSequence<LrrrValue>
        val indices = arguments.subList(0, arguments.lastIndex).map { it as LrrrNumber }.map { it.numberInteger }

        val result = indices.map { sequence.get(it) }
        return when {
            result.isEmpty() -> LrrrNull.lrrrNull
            result.size == 1 -> result.first()
            else -> LrrrFiniteSequence(result.toMutableList())
        }
    }
}


@Suppress("UNCHECKED_CAST")
class GetManyFromSequenceByIndex : PolyadicFunction("m", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val sequence = arguments.last() as LrrrFiniteSequence<LrrrValue>
        val indices = arguments.subList(0, arguments.lastIndex).map { it as LrrrNumber }.map { it.numberInteger }

        val result = indices.map { sequence.list[it] }
        return when {
            result.isEmpty() -> LrrrNull.lrrrNull
            result.size == 1 -> result.first()
            else -> LrrrFiniteSequence(result.toMutableList())
        }
    }
}

// class Reverse:MonadicFunction("")

@Suppress("UNCHECKED_CAST")
class SubsequenceFunction : PolyadicFunction("l", true, allowNoParameters = false) {
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

class RangeFunction : MonadicFunction("R", true, allowNoParameters = false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((1..number).map { it.toLrrrValue() }.toMutableList())
    }
}

class ExclusiveBoundsRange : MonadicFunction("B", true, allowNoParameters = false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((2 until number).map { it.toLrrrValue() }.toMutableList())
    }
}


class UntilFunction : MonadicFunction("U", true, allowNoParameters = false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrNumber
        val number = argument.numberInteger
        return LrrrFiniteSequence((0 until number).map { it.toLrrrValue() }.toMutableList())
    }
}

@Suppress("UNCHECKED_CAST")
class Sum : MonadicFunction("S", true, allowNoParameters = false) {
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
        }.toLrrrValue()
    }
}

@Suppress("UNCHECKED_CAST")
class Product : MonadicFunction("p", true, allowNoParameters = false) {
    override fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue {
        argument as LrrrFiniteSequence<LrrrValue>
        var product = 1.0
        argument.list.map { (it as LrrrNumber).number }.forEach { product *= it }
        return product.toLrrrValue()
    }
}

class Find : DiadicFunction("f", true, true, allowNoParameters = false) {
    override fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue {
        val string = first.toString()
        val toFind = second.toString()

        val foundStarts = toFind.toRegex().findAll(string)
            .map { LrrrFiniteSequence(mutableListOf(it.range.first.toLrrrValue(), it.range.last.toLrrrValue())) }
            .toMutableList()
        return if (foundStarts.isEmpty()) LrrrNull.lrrrNull
        else LrrrFiniteSequence(foundStarts)
    }
}

class CreateFiniteSequence : PolyadicFunction("s", true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrFiniteSequence(arguments.toMutableList())
    }
}

class CreateInfiniteSequence : PolyadicFunction("c", true, listOf(0), allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}