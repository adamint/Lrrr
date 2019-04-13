package com.adamratzman.lrrr.language.evaluation

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.ParseObj
import com.adamratzman.lrrr.language.parsing.StructureType
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.LrrrVoid

interface Evaluatable {
    fun evaluate(context: LrrrContext): LrrrValue
}

data class EvaluationScope(
    val objects: List<Evaluatable>,
    val type: StructureType,
    val condition: Evaluatable?
) : Evaluatable {
    override fun evaluate(context: LrrrContext): LrrrValue {
        return objects.map { inner -> inner.evaluate(context.copy()) }.filter { it !is LrrrVoid }.last()
    }
}


fun toEvaluatableObject(parsedStructures: List<ParseObj>) =
    EvaluationScope(parsedStructures.map { it.toEvaluatable() }, StructureType.EMPTY, null)
