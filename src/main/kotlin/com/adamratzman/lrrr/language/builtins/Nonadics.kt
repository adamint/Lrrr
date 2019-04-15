package com.adamratzman.lrrr.language.builtins

import com.adamratzman.lrrr.language.parsing.LrrrContext
import com.adamratzman.lrrr.language.types.LrrrFiniteSequence
import com.adamratzman.lrrr.language.types.LrrrValue
import com.adamratzman.lrrr.language.types.LrrrVoid
import com.adamratzman.lrrr.language.types.NonadicFunction

class ParamSplitFunction : NonadicFunction(",", true) {
    override fun evaluate(arguments: List<LrrrValue>, context: LrrrContext): LrrrValue {
        return LrrrVoid.lrrrVoid
    }
}
