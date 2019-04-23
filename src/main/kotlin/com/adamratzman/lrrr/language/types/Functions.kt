package com.adamratzman.lrrr.language.types

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext

abstract class LrrrFunction(val identifier: String, val shouldEvaluateParameters: Boolean) : LrrrValue() {
    abstract fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue
    override fun evaluate(context: LrrrContext) = this
}

abstract class ReplacementFunction(identifier: String, val replaceWith: String) : LrrrFunction(identifier, true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = LrrrVoid.lrrrVoid
}

/*
class NonadicSuspendedFunction(val arguments: List<List<LrrrValue>>):LrrrFunction("nonadic suspended function",false) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = arguments.map { lrrr.evaluate(it, context) }
}*/

abstract class NonadicFunction(identifier: String, shouldEvaluateParameters: Boolean) :
    LrrrFunction(identifier, shouldEvaluateParameters) {
    abstract fun evaluate(backreference: LrrrValue?, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = evaluate(context.parentContext?.backreference, context)

}

abstract class MonadicFunction(identifier: String, shouldEvaluateParameters: Boolean) :
    LrrrFunction(identifier, shouldEvaluateParameters) {
    abstract fun evaluate(argument: LrrrValue, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = evaluate(arguments[0], context)
}

abstract class DiadicFunction(identifier: String, shouldEvaluateParameters: Boolean, val strictRightArgument: Boolean) :
    LrrrFunction(identifier, shouldEvaluateParameters) {
    abstract fun evaluate(first: LrrrValue, second: LrrrValue, context: LrrrContext): LrrrValue
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext) = evaluate(arguments[0], arguments[1], context)
}

abstract class PolyadicFunction(identifier: String, shouldEvaluateParameters: Boolean, val unevaluatedParamIndices: List<Int> = listOf()) :
    LrrrFunction(identifier, shouldEvaluateParameters)

data class FunctionInvocation(val parameters: List<Evaluatable>, val function: LrrrFunction) : LrrrValue() {
    override fun evaluate(context: LrrrContext): LrrrValue {
        return function.evaluate(parameters.map { it.evaluate(context) }, context)
    }
}