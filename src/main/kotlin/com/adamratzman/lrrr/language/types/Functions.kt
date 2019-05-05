package com.adamratzman.lrrr.language.types

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.evaluation.GenericEvaluationScope
import com.adamratzman.lrrr.language.evaluation.globalEvaluation
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.StructureType
import com.adamratzman.lrrr.language.parsing.toLrrrValue

abstract class LrrrFunction(
    val identifier: String,
    val shouldEvaluateParameters: Boolean,
    val allowNoParameters: Boolean
) : LrrrValue() {
    abstract fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue
    override fun evaluate(context: LrrrContext) = this
}

abstract class ReplacementFunction(identifier: String, val replaceWith: String) :
    LrrrFunction(identifier, true, false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = LrrrVoid.lrrrVoid
}

abstract class TransformationFunction(identifier: String) :
    PolyadicFunction(identifier, true, allowNoParameters = false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = throw IllegalStateException()
    abstract fun evaluate(arguments: List<LrrrValue>, transformer: List<Evaluatable>, context: LrrrContext): LrrrValue

    fun map(
        objects: LrrrFiniteSequence<Evaluatable>?,
        transformer: List<Evaluatable>,
        context: LrrrContext,
        vararg variables: LrrrVariable
    ): List<LrrrValue> {
        val transformed = mutableListOf<LrrrValue>()

        val iterator =
            objects?.list?.asIterable() ?: generateSequence({ 0 }) { it + 1 }.map { it.toLrrrValue() }.asIterable()

        iterator.forEachIndexed { i, obj ->
            transformed.add(
                globalEvaluation(
                    context.newChildContext.apply {
                        contextValues.addAll(variables)
                        contextValues.add(LrrrVariable("v", obj.evaluate(this)))
                        contextValues.add(LrrrVariable("i", i.toLrrrValue()))
                        if (i != 0) contextValues.add(LrrrVariable("j", transformed[i - 1]))
                        contextValues.add(LrrrVariable("k", LrrrFiniteSequence(transformed.toMutableList())))
                    },
                    GenericEvaluationScope(transformer, StructureType.EMPTY, null)
                ).let { result ->
                    if (result is LrrrFiniteSequence<*> && result.list.size == 1) result.list[0] as LrrrValue else result
                }
            )
        }

        return transformed
    }
}

/*
class NonadicSuspendedFunction(val arguments: List<List<LrrrValue>>):LrrrFunction("nonadic suspended function",false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = arguments.map { lrrr.evaluate(it, context) }
}*/

abstract class NonadicFunction(identifier: String, shouldEvaluateParameters: Boolean) :
    LrrrFunction(identifier, shouldEvaluateParameters, false) {
    abstract fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) =
        evaluate(context.backreference ?: context.parentContext?.backreference, context)

}

abstract class MonadicFunction(identifier: String, shouldEvaluateParameters: Boolean, allowNoParameters: Boolean) :
    LrrrFunction(identifier, shouldEvaluateParameters, allowNoParameters) {
    abstract fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = evaluate(arguments[0], context)
}

abstract class DiadicFunction(
    identifier: String,
    shouldEvaluateParameters: Boolean,
    val strictRightArgument: Boolean,
    allowNoParameters: Boolean
) :
    LrrrFunction(identifier, shouldEvaluateParameters, allowNoParameters) {
    abstract fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) =
        evaluate(arguments[0], arguments[1], context)
}

abstract class PolyadicFunction(
    identifier: String,
    shouldEvaluateParameters: Boolean,
    val unevaluatedParamIndices: List<Int> = listOf(),
    allowNoParameters: Boolean
) : LrrrFunction(identifier, shouldEvaluateParameters, allowNoParameters)

data class FunctionInvocation(
    val parameters: List<Evaluatable>,
    val function: LrrrFunction,
    var transformer: List<Evaluatable>? = null
) : LrrrValue() {
    override fun evaluate(context: LrrrContext): LrrrValue {
        return when (function) {
            !is TransformationFunction -> function.evaluate(
                parameters.map { it.evaluate(context) },
                context
            )
            else -> function.evaluate(parameters.map { it.evaluate(context) }, transformer ?: listOf(), context)
        }
    }
}