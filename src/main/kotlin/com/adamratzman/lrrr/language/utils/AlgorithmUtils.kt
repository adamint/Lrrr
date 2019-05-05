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

fun nextPrime(i: Int): Int = generateSequence(i + 1, { it + 1 }).first { num -> (2..num / 2).none { num % it == 0 } }
fun primes() = generateSequence(2, { last -> nextPrime(last) })

fun factors(i: Int) = (1..i / 2).filter { i % it == 0 } + i