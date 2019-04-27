@file:Suppress("UNCHECKED_CAST")

package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

class Map : TransformationFunction("M") {
    override fun evaluate(arguments: List<LrrrValue>, transformer: List<Evaluatable>, context: LrrrContext): LrrrValue {
        val param = arguments[0]

        val sequence: LrrrFiniteSequence<Evaluatable>? = when {
            param is LrrrFiniteSequence<*> -> param as LrrrFiniteSequence<Evaluatable>
            param is LrrrBoolean && param.boolean -> null
            else -> LrrrFiniteSequence((1..(param as LrrrNumber).numberInteger).map { it.toLrrValue() as Evaluatable }.toMutableList())
        }

        return LrrrFiniteSequence(map(sequence, transformer, context).toMutableList())
    }
}