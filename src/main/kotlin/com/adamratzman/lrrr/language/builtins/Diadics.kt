package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.parsing.toLrrValue
import com.adamratzman.lrrr.language.types.DiadicFunction
import com.adamratzman.lrrr.language.types.LrrrNumber
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.lrrrBooleanOf
import com.adamratzman.lrrr.language.utils.mod

class Addition : DiadicFunction("+",true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return first + second
    }
}

class Subtraction : DiadicFunction("⁻", true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return first - second
    }
}

class Division:DiadicFunction("÷",true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return first / second
    }
}

class Modulus : DiadicFunction("%",true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber

        return mod(first.number,second.number).toLrrValue()
    }
}


class GreaterThan : DiadicFunction(">", true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber
        return lrrrBooleanOf(first.number > second.number)
    }
}

class LessThan : DiadicFunction("<",true,true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        val first = arguments[0] as LrrrNumber
        val second = arguments[1] as LrrrNumber
        return lrrrBooleanOf(first.number < second.number)
    }
}