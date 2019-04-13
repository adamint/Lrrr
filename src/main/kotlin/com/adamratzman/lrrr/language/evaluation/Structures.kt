package com.adamratzman.lrrr.language.evaluation

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

abstract class LrrrStructure(val toEvaluate: LrrrFunction) {
    abstract fun evaluate(context: LrrrContext): LrrrValue
}

class LrrrScope(val context: LrrrContext, toEvaluate: LrrrFunction) : LrrrStructure(toEvaluate) {
    override fun evaluate(context: LrrrContext) = toEvaluate.evaluate(listOf(), context)
}

abstract class LrrrConditionalStructure(val condition: (LrrrContext) -> LrrrValue, toEvaluate: LrrrFunction) :
    LrrrStructure(toEvaluate)

class LrrrIfStructure(condition: (LrrrContext) -> LrrrValue, toEvaluate: LrrrFunction) :
    LrrrConditionalStructure(condition, toEvaluate) {
    override fun evaluate(context: LrrrContext): LrrrValue {
        return if (condition(context).let { (it is LrrrBoolean && it.boolean) || (it != LrrrVoid.lrrrVoid && it != LrrrNull.lrrrNull) }) {
            toEvaluate.evaluate(
                listOf(),
                context
            )
        } else LrrrVoid.lrrrVoid
    }
}

class LrrrNotStructure(condition: (LrrrContext) -> LrrrValue, toEvaluate: LrrrFunction) :
    LrrrConditionalStructure(condition, toEvaluate) {
    override fun evaluate(context: LrrrContext): LrrrValue {
        return if (condition(context).let { !(it is LrrrBoolean && it.boolean) || it == LrrrVoid.lrrrVoid || it == LrrrNull.lrrrNull }) {
            toEvaluate.evaluate(
                listOf(),
                context
            )
        } else LrrrVoid.lrrrVoid
    }
}

class LrrrForStructure(
    val condition: (Double, LrrrContext) -> Boolean,
    initialValue: Double = 0.0,
    var value: Double = initialValue,
    val incrementorFunction: (LrrrContext, Double) -> Double = { _, double -> double + 1 },
    toEvaluate: LrrrFunction
) : LrrrStructure(toEvaluate) {
    override fun evaluate(context: LrrrContext): LrrrValue {
        while (condition(value, context)) {
            toEvaluate.evaluate(listOf(value.toLrrValue()), context)

            value = incrementorFunction(context, value)
        }

        return LrrrVoid.lrrrVoid
    }
}
