package com.adamratzman.lrrr.language.utils

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

fun mod(one: Double, two: Double): Double {
    return ((one % two) + two) % two
}

fun logGamma(x: Double): Double {
    val tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5)
    val ser =
        1.0 + 76.18009173 / (x + 0) - 86.50532033 / (x + 1) + 24.01409822 / (x + 2) - 1.231739516 / (x + 3) + 0.00120858003 / (x + 4) - 0.00000536382 / (x + 5)
    return tmp + Math.log(ser * Math.sqrt(2 * Math.PI))
}

fun gamma(x: Double): Double {
    val result = exp(logGamma(x))
    val abs = abs(result.roundToInt().toDouble() - result)
    return if (abs <= 0.001) result.roundToInt().toDouble() else result
}
