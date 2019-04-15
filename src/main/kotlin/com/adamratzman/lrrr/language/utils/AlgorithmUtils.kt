package com.adamratzman.lrrr.language.utils

fun mod(one: Double, two: Double): Double {
    return ((one % two) + two) % two
}