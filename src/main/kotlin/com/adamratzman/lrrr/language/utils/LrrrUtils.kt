package com.adamratzman.lrrr.language.utils

import com.adamratzman.lrrr.language.types.LrrrFunction
import org.reflections.Reflections
import java.lang.reflect.Modifier

fun getAllFunctions() = Reflections("com.adamratzman.lrrr.language.builtins")
    .getSubTypesOf(LrrrFunction::class.java)
    .filter { !Modifier.isAbstract(it.modifiers) }
    .map { it.newInstance() }