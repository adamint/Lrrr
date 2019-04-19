package com.adamratzman.lrrr.language.types

import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.parsing.LrrrContext

abstract class LrrrSequence<T> : LrrrType() {
    abstract fun takeFirst(n: Int): List<T>
    abstract fun take(start: Int, end: Int): List<T>

    abstract fun get(index: Int): T
}

open class LrrrFiniteSequence<T: Evaluatable>(var list: MutableList<T>) : LrrrSequence<T>() {
    override fun takeFirst(n: Int) = list.take(n)
    override fun take(start: Int, end: Int) = list.subList(start, end)

    fun takeLast(n: Int) = list.takeLast(n)

    override fun get(index: Int) = list[index]

    fun set(index: Int, value: T) {
        list[index] = value
    }

    fun clear() = list.clear()

    override fun identical(other: Any?) = other is LrrrFiniteSequence<*> && other.list == list
    override fun toString() = list.toString()
    override fun evaluate(context: LrrrContext) = this

}

class LrrrGeneratingSequence<T>(val generator: (Int, List<T>) -> T, val initialValues: List<T>) : LrrrSequence<T>() {
    var generated = mutableListOf<T>()

    private fun generateTo(n:Int) {
        while (generated.size < n) generated.add(generator(generated.size, generated))
    }

    override fun takeFirst(n: Int): List<T> {
        generateTo(n)
        return generated.take(n)
    }

    override fun take(start: Int, end: Int): List<T> {
        generateTo(end)
        return generated.subList(start,end)
    }


    override fun get(index: Int): T {
        generateTo(index + 1)
        return generated[index]
    }

    override fun identical(other: Any?) =
        other is LrrrGeneratingSequence<*> && other.generator == generator && other.initialValues == initialValues

    override fun toString() = "Lrrr Generating Sequence of initial size ${initialValues.size}"

    override fun evaluate(context: LrrrContext) = this
}

