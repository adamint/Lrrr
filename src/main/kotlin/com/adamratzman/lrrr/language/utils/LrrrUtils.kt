package com.adamratzman.lrrr.language.utils

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.charChar
import com.adamratzman.lrrr.language.parsing.contextCharArray
import com.adamratzman.lrrr.language.parsing.stringChar
import com.adamratzman.lrrr.language.types.LrrrFiniteSequence
import com.adamratzman.lrrr.language.types.LrrrFunction
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.NonadicFunction
import org.reflections.Reflections
import java.lang.reflect.Modifier

fun getAllFunctions() = Reflections("com.adamratzman.lrrr.language.builtins")
    .getSubTypesOf(LrrrFunction::class.java)
    .filter { !Modifier.isAbstract(it.modifiers) }
    .map { it.newInstance() }

fun getNumberFunctions() = ((0..9) + ('A'..'F') + '.').map {
    object : NonadicFunction("$it", true) {
        override fun evaluate(backreference: LrrrValue?, context: LrrrContext) = throw IllegalStateException()
    } as LrrrFunction
}

fun getControlStructureFunctions() =
    (contextCharArray + stringChar + charChar
            + '_' + 't' + 'f' + ',' + "void" + "null"
            + listOf('i', 'j', 'k', 'v')
            )
        .map {
            object : NonadicFunction("$it", true) {
                override fun evaluate(backreference: LrrrValue?, context: LrrrContext) = throw IllegalStateException()
            } as LrrrFunction
        }

fun <T : Evaluatable> List<T>.toLrrrFiniteSequence() = LrrrFiniteSequence(toMutableList())