package com.adamratzman.lrrr

import com.adamratzman.lrrr.language.evaluation.EvaluationScope
import com.adamratzman.lrrr.language.evaluation.toEvaluatableObject
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.ParseObj
import com.adamratzman.lrrr.language.parsing.parseStructures
import com.adamratzman.lrrr.language.types.LrrrVariable
import com.adamratzman.lrrr.language.utils.getAllFunctions
import com.adamratzman.lrrr.language.utils.parseForLrrrValues

val globalLrrr = Lrrr()

class Lrrr {
    val functions = getAllFunctions()

    fun createInterpreter() = Interpreter.createInterpreter(this)
}

class Interpreter private constructor(val lrrr: Lrrr) {
    lateinit var globalContext: LrrrContext
    lateinit var code: String

    fun loadContext(contextCode: String) {
        globalContext =
            LrrrContext(parseForLrrrValues(contextCode).map { LrrrVariable(null, it) }.toMutableList(), null, lrrr)
    }

    fun loadVariableNames(variableNames: String) {
        variableNames.split(",").forEachIndexed { i, s -> globalContext.contextValues[i].identifier = s }
    }

    fun loadCode(code: String) {
        this.code = code
    }

    fun parseCodeToEvaluatables(code: String): EvaluationScope =
        parseCodeStructuresToEvaluatables(parseCodeStructures(code))

    fun parseCodeStructures(code: String): List<ParseObj> = parseStructures(code)
    fun parseCodeStructuresToEvaluatables(structures: List<ParseObj>): EvaluationScope = toEvaluatableObject(structures)


    companion object {
        fun createInterpreter(lrrr: Lrrr) = Interpreter(lrrr)
    }
}