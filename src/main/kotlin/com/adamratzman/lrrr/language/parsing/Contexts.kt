package com.adamratzman.lrrr.language.parsing

import com.adamratzman.lrrr.Lrrr
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.LrrrVariable
import com.adamratzman.lrrr.language.utils.VariableNotFoundException

data class LrrrContext(val contextValues: MutableList<LrrrVariable>,
                       val parentContext: LrrrContext?,
                       val lrrr: Lrrr,
                       var backreference: LrrrValue? = null) {

    val firstValue get() = contextValues.first()

    val immediateParentFirstValue get() = parentContext!!.firstValue

    val newChildContext get() = LrrrContext(mutableListOf(), this, lrrr)

    fun getGlobalContext(): LrrrContext = parentContext?.getGlobalContext() ?: this

    fun findContextValue(predicate: (LrrrVariable) -> Boolean): LrrrVariable? {
        val found = contextValues.firstOrNull { predicate(it) }
        if (found != null) return found
        if (parentContext != null) return parentContext.findContextValue(predicate)

        throw VariableNotFoundException("Variable not found")
    }

    fun getAllContextsValues(): List<LrrrVariable> = if (parentContext == null) contextValues else contextValues + parentContext.getAllContextsValues()
}