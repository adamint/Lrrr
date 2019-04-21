package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.*

class PrintFunction : PolyadicFunction("P", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val toPrint = arguments.filter { it !is LrrrNoReturn }.map { it.toString() }
        if (toPrint.isNotEmpty()) {
            println(
                if (toPrint.size == 1) toPrint[0]
                else LrrrFiniteSequence(toPrint.map { it.toLrrValue() }.toMutableList())
            )
        }
        return LrrrVoid.lrrrVoid
    }
}