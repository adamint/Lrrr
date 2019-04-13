package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.LrrrFiniteSequence
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.LrrrVoid
import com.adamratzman.lrrr.language.types.PolyadicFunction

class Print : PolyadicFunction("P", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val toPrint = arguments.map { it.toString() }
        println(
            when {
                toPrint.isEmpty() -> "".toLrrValue()
                toPrint.size == 1 -> toPrint[0]
                else -> LrrrFiniteSequence(toPrint.toMutableList())
            }
        )

        return LrrrVoid.lrrrVoid
    }
}