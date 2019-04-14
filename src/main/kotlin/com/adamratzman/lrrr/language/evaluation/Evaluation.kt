package com.adamratzman.lrrr.language.evaluation

import com.adamratzman.lrrr.language.parsing.LrrrContext

fun globalEvaluation(globalContext: LrrrContext, globalEvaluationScope: EvaluationScope) =
    evaluate(globalContext, globalEvaluationScope)

fun evaluate(context: LrrrContext, evaluationScope: EvaluationScope) = evaluationScope.evaluate(context)