package com.adamratzman.lrrr.language.evaluation

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.ParseObj
import com.adamratzman.lrrr.language.parsing.StructureType
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

interface Evaluatable {
    fun evaluate(context: LrrrContext): LrrrValue
}

@Suppress("UNCHECKED_CAST")
data class ForEvaluationScope(
    val _objects: List<Evaluatable>,
    val _condition: Evaluatable?,
    val incrementorFunction: Evaluatable?,
    val initialVariable: Evaluatable?
) : EvaluationScope(_objects, StructureType.FOR, _condition) {
    override fun evaluate(context: LrrrContext): LrrrValue {
        try {
            val evaluatedValue = condition?.evaluate(context) ?: LrrrBoolean.lrrrFalse

            val results = mutableListOf<LrrrValue>()

            if (evaluatedValue is LrrrNumber || evaluatedValue is LrrrString || evaluatedValue is LrrrFiniteSequence<*>) {
                val sequence = if (evaluatedValue is LrrrNumber) {
                    LrrrFiniteSequence((1..evaluatedValue.numberInteger).map { it.toLrrValue() }.toMutableList())
                } else evaluatedValue as LrrrFiniteSequence<LrrrValue>

                results.addAll(sequence.list.mapIndexed { i, value ->
                    val forContext = context.newChildContext.apply {
                        contextValues.add(LrrrVariable("i", i.toLrrValue()))
                        contextValues.add(LrrrVariable("j", (i + 1).toLrrValue()))
                        contextValues.add(LrrrVariable("v", value))
                    }

                    objects.map { inner -> inner.evaluate(forContext) }
                        .let { result -> if (result.size == 1) result.first() else LrrrFiniteSequence(result.filter { it !is LrrrNoReturn }.toMutableList()) }
                        .apply { context.backreference = this }
                })
            } else if (evaluatedValue is LrrrBoolean) {
                if (evaluatedValue.boolean) {
                    var i = 0
                    while (true) {
                        val forContext = context.newChildContext.apply {
                            contextValues.add(LrrrVariable("i", i.toLrrValue()))
                            contextValues.add(LrrrVariable("j", (i + 1).toLrrValue()))
                        }
                        objects.map { inner -> inner.evaluate(forContext) }
                            .let { result -> if (result.size == 1) result.first() else LrrrFiniteSequence(result.filter { it !is LrrrNoReturn }.toMutableList()) }
                            .apply { context.backreference = this }
                        i++
                    }
                }
            }


            return when {
                results.isEmpty() -> LrrrVoid.lrrrVoid
                results.size == 1 -> results[0]
                else -> LrrrFiniteSequence(results.toMutableList())
            }
        } catch (ignored: Exception) {
        }

        val initial = initialVariable?.evaluate(context)?.let { evaluatedVariable ->
            evaluatedVariable as? LrrrNumber
                ?: evaluatedVariable as? LrrrVariable
                ?: (evaluatedVariable as? LrrrString)?.string?.length?.let { LrrrNumber(it.toDouble()) }
                ?: (evaluatedVariable as? LrrrChar)?.number?.let { LrrrNumber(it) }
                ?: (evaluatedVariable as? LrrrFiniteSequence<*>)?.list?.size?.let { LrrrNumber(it.toDouble()) }
                ?: (evaluatedVariable as? LrrrBoolean)?.let { (-1).toLrrValue() }
                ?: throw IllegalArgumentException("Evaluated variable $evaluatedVariable in $this is not a string, char, sequence, or number!")
        } ?: LrrrNumber(0.toDouble())

        val variable = (initial as? LrrrVariable ?: (LrrrVariable(null, initial)))

        val results = mutableListOf<LrrrValue>()

        while (true) {
            val forContext = context.newChildContext.apply { contextValues.add(LrrrVariable("i", variable.value)) }
            val shouldContinue =
                condition?.evaluate(forContext)?.let { if (it is LrrrBoolean) it.boolean else (it !is LrrrNoReturn && it !is LrrrNull) }
                    ?: true

            if (!shouldContinue) break

            val result = objects.map { inner -> inner.evaluate(forContext) }
                .let { if (it.size == 1) it.first() else LrrrFiniteSequence(it.toMutableList()) }

            if (result !is LrrrNoReturn) results.add(result)

            variable.value =
                (variable.value as LrrrNumber) + if (incrementorFunction == null) LrrrNumber(1.0) else (incrementorFunction.evaluate(
                    forContext
                ) as LrrrNumber)
        }

        return when {
            results.isEmpty() -> LrrrVoid.lrrrVoid
            results.size == 1 -> results[0]
            else -> LrrrFiniteSequence(
                results.filter { it !is LrrrNoReturn }.toMutableList()
            )
        }
    }

}

data class GenericEvaluationScope(
    val _objects: List<Evaluatable>,
    val _type: StructureType,
    val _condition: Evaluatable?
) : EvaluationScope(_objects, _type, _condition) {
    override fun evaluate(context: LrrrContext): LrrrValue {
        val conditionResult = condition?.evaluate(context)
        val shouldEvaluate = when (type) {
            StructureType.EMPTY -> conditionResult?.let {
                conditionResult !is LrrrVoid
                        && conditionResult !is LrrrNull
                        && (if (conditionResult is LrrrBoolean) conditionResult.boolean else true)
            } ?: true
            StructureType.NOT, StructureType.ELSE -> conditionResult == null
                    || conditionResult is LrrrVoid
                    || conditionResult is LrrrNull
                    || (conditionResult is LrrrBoolean && !conditionResult.boolean)
            StructureType.IF -> conditionResult != null
                    && conditionResult !is LrrrVoid
                    && conditionResult !is LrrrNull
                    && (if (conditionResult is LrrrBoolean) conditionResult.boolean else true)
            else -> throw IllegalStateException("Invalid type $type")
        }

        return when {
            type == StructureType.IF -> {
                if (shouldEvaluate) objects.map { inner -> inner.evaluate(context) }.lastOrNull { it !is LrrrNoReturn }
                    ?: LrrrVoid.lrrrVoid
                else {
                    val foundElse = objects.filter { it is GenericEvaluationScope && it.type == StructureType.ELSE }
                    when {
                        foundElse.isEmpty() -> LrrrVoid.lrrrVoid
                        foundElse.size == 1 -> foundElse.first().evaluate(context)
                        else -> LrrrFiniteSequence(foundElse.map { it.evaluate(context) }.toMutableList())
                    }
                }
            }
            shouldEvaluate -> objects.map { inner -> inner.evaluate(context) }.let { result ->
                if (context.parentContext == null) result.last()
                else result.lastOrNull { it !is LrrrNoReturn } ?: LrrrVoid.lrrrVoid
            }
            else -> LrrrVoid.lrrrVoid
        }.apply { context.parentContext?.backreference = this }
    }
}

abstract class EvaluationScope(
    val objects: List<Evaluatable>,
    val type: StructureType,
    val condition: Evaluatable?
) : Evaluatable


fun toEvaluatableObject(parsedStructures: List<ParseObj>) =
    GenericEvaluationScope(parsedStructures.map { it.toEvaluatable() }, StructureType.EMPTY, null)
