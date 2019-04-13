package com.adamratzman.lrrr.language.parsing

import com.adamratzman.lrrr.Lrrr
import com.adamratzman.lrrr.language.types.LrrrVariable

data class LrrrContext(val contextValues: MutableList<LrrrVariable>, val parentContext: LrrrContext?, val lrrr: Lrrr) {
    val firstValue get() = contextValues.first()

    val immediateParentFirstValue get() = parentContext!!.firstValue

    fun getGlobalContext(): LrrrContext = parentContext?.getGlobalContext() ?: this
}