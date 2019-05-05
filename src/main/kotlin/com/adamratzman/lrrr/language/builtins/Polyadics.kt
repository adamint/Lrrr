package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrrValue
import com.adamratzman.lrrr.language.types.*

class PrintFunction : PolyadicFunction("P", true, allowNoParameters = true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        if (arguments.size == 1 && arguments[0] is LrrrVoid && arguments[0] !is LrrrNoReturn) println()

        val toPrint = arguments.filter { it !is LrrrNoReturn }.map { it.toString() }
        if (toPrint.isNotEmpty()) {
            println(
                if (toPrint.size == 1) toPrint[0]
                else LrrrFiniteSequence(toPrint.map { it.toLrrrValue() }.toMutableList())
            )
        }
        return LrrrVoid.lrrrVoid
    }
}