package com.adamratzman.lrrr

import com.adamratzman.lrrr.language.evaluation.EvaluationScope
import com.adamratzman.lrrr.language.evaluation.globalEvaluation
import com.adamratzman.lrrr.language.evaluation.toEvaluatableObject
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.ParseObj
import com.adamratzman.lrrr.language.parsing.parseStructures
import com.adamratzman.lrrr.language.types.LrrrVariable
import com.adamratzman.lrrr.language.types.LrrrVoid
import com.adamratzman.lrrr.language.utils.getAllFunctions
import com.adamratzman.lrrr.language.utils.parseForLrrrValues
import java.util.*

val globalLrrr = Lrrr()

fun main() {
    println(">>> Lrrr REPL (Version 0.0)")

    val lrrr = Lrrr()
    while (true) {
        lrrr.loadAndEvaluateProgram()
    }
}

class Lrrr {
    val functions = getAllFunctions()

    fun createInterpreter() = Interpreter.createInterpreter(this)

    fun loadAndEvaluateProgram() {
        val scanner = Scanner(System.`in`)
        val interpreter = Interpreter.createInterpreter(this)

        println("Enter code")
        print(">>> ")
        interpreter.loadCode(scanner.nextLine())

        println("Enter context code")
        print(">>> ")
        interpreter.loadContext(scanner.nextLine())

        println("Enter context variable names")
        print(">>> ")
        interpreter.loadVariableNames(scanner.nextLine())

        println("Evaluating...")
        val value = interpreter.evaluate()
        if (value !is LrrrVoid) println(value)
    }
}

class Interpreter private constructor(val lrrr: Lrrr) {
    lateinit var globalContext: LrrrContext
    lateinit var code: String

    fun loadContext(contextCode: String) {
        globalContext =
            LrrrContext(parseForLrrrValues(contextCode).map { LrrrVariable(null, it) }.toMutableList(), null, lrrr)
    }

    fun loadVariableNames(variableNames: String) {
        if (variableNames.isNotEmpty()) variableNames.split(",").forEachIndexed { i, s -> globalContext.contextValues[i].identifier = s }
    }

    fun loadCode(code: String) {
        this.code = code
        println(parseCodeToEvaluatables(code))
    }

    fun parseCodeToEvaluatables(code: String): EvaluationScope =
        parseCodeStructuresToEvaluatables(parseCodeStructures(code))

    fun parseCodeStructures(code: String): List<ParseObj> = parseStructures(code)
    fun parseCodeStructuresToEvaluatables(structures: List<ParseObj>): EvaluationScope = toEvaluatableObject(structures)

    fun evaluate() = globalEvaluation(globalContext, parseCodeToEvaluatables(code))

    companion object {
        fun createInterpreter(lrrr: Lrrr) = Interpreter(lrrr)
    }
}