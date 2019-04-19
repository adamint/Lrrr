package com.adamratzman.lrrr.language.parsing

import com.adamratzman.lrrr.globalLrrr
import com.adamratzman.lrrr.language.builtins.GetAllCurrentContextValues
import com.adamratzman.lrrr.language.builtins.GetLastCurrentContextValue
import com.adamratzman.lrrr.language.builtins.ParamSplitFunction
import com.adamratzman.lrrr.language.evaluation.Evaluatable
import com.adamratzman.lrrr.language.evaluation.ForEvaluationScope
import com.adamratzman.lrrr.language.evaluation.GenericEvaluationScope
import com.adamratzman.lrrr.language.types.*
import com.adamratzman.lrrr.language.utils.*

val stringChar = '"'
val charChar = '\''

val contextStartChar = '{'
val contextEndChar = '}'

val ifStartChar = '?'
val notStartChar = '^'
val forStartChar = '('

val elseChar = '@'

val contextCharArray = listOf(
    contextStartChar,
    contextEndChar,
    ifStartChar,
    notStartChar,
    forStartChar,
    elseChar
)

val contextOpeningCharArray = contextCharArray
    .toMutableList()
    .apply { remove(contextEndChar) }


data class ForParseStructure(
    val _children: List<ParseObj>,
    val initialValue: ParseObj?,
    val incrementorFunction: ParseObj?,
    val _condition: ParseObj?,
    val _codeString: String
) : ParseStructure(_children, StructureType.FOR, _codeString, _condition) {
    override fun toEvaluatable() =
        ForEvaluationScope(
            children.map { it.toEvaluatable() },
            condition?.toEvaluatable(),
            incrementorFunction?.toEvaluatable(),
            initialValue?.toEvaluatable()
        )
}

data class GenericParseStructure(
    val _children: List<ParseObj>,
    val _type: StructureType,
    val _codeString: String,
    val _condition: ParseObj?
) : ParseStructure(_children, _type, _codeString, _condition) {
    override fun toEvaluatable() =
        GenericEvaluationScope(children.map { it.toEvaluatable() }, type, condition?.toEvaluatable())
}

abstract class ParseStructure(
    val children: List<ParseObj>,
    val type: StructureType,
    codeString: String,
    val condition: ParseObj? = null
) : ParseObj(codeString)

data class ParseContextSubsection(
    val children: List<ParseObj>,
    private val _code: String
) : ParseObj(_code) {
    override fun toEvaluatable() =
        GenericEvaluationScope(children.map { it.toEvaluatable() }, StructureType.EMPTY, null)
}

open class ParseObj(val code: String) {
    override fun toString() = "ParseObj(code=$code)"

    /**
    we have to worry about type parsing and function parameterizing here
     **/
    @Suppress("UNCHECKED_CAST")
    open fun toEvaluatable(): Evaluatable {
        val lrrrValues = mutableListOf<LrrrValue>()

        var workingCode = code

        while (workingCode.isNotEmpty()) {
            workingCode = workingCode.trim()

            val stringLocations = findStringLocations(workingCode)

            val functionIndices =
                workingCode.filterMapIndex { index, c ->
                    !isCharInString(index, stringLocations) && !isCharInCharDeclaration(index, workingCode)
                            && c.toString() in globalLrrr.functions.map { it.identifier }
                }

            if (functionIndices.isEmpty()) {
                lrrrValues.addAll(parseForLrrrValues(workingCode))
                if (lrrrValues.size == 1) return lrrrValues.first()
                return LrrrFiniteSequence(lrrrValues)
            }

            val splitBetweenFunctions = workingCode
                .splitIndices(functionIndices)
                .mapIndexed { i, s ->
                    val function = try {
                        globalLrrr.functions.find { it.identifier == workingCode[functionIndices[i]].toString() }!!
                    } catch (e: IndexOutOfBoundsException) {
                        ParamSplitFunction()
                    }

                    if (functionIndices.getOrNull(i) == 0) function to s
                    else s to function
                }
                .toMutableList()

          /*  globalLrrr.functions.find { it.identifier == workingCode.last().toString() }
                ?.let { splitBetweenFunctions.add(it to "") }
          */
            val allLrrrParams = splitBetweenFunctions.map { (first, second) ->

                val parsed =
                    mutableListOf<LrrrValue>(LrrrFiniteSequence(parseForLrrrValues(if (first is String) first else second as String).toMutableList()))
                parsed.apply {
                    val function = if (first is LrrrFunction) first else second as LrrrFunction
                    if (function !is ParamSplitFunction) {
                        if (first is LrrrFunction) add(0, function)
                        else add(function)
                    }
                }
            }.flatten().toMutableList()

            loop@ while (allLrrrParams.any { it is LrrrFunction }) {
                allLrrrParams.removeIf { it is LrrrFiniteSequence<*> && (it as LrrrFiniteSequence<LrrrValue>).list.isEmpty() }
                val functionIndex = allLrrrParams.indexOfFirst { it is LrrrFunction }
                val function = allLrrrParams[functionIndex] as LrrrFunction

                when (function) {
                    is NonadicFunction -> allLrrrParams[functionIndex] = FunctionInvocation(listOf(), function)
                    else -> {
                        if (functionIndex == 0) {
                            if (function !is PolyadicFunction) {
                                allLrrrParams.add(0, FunctionInvocation(listOf(), GetLastCurrentContextValue()))
                            } else {
                                allLrrrParams.add(0, FunctionInvocation(listOf(), GetAllCurrentContextValues()))
                            }

                            continue@loop
                        }

                        var arguments = allLrrrParams[functionIndex - 1]
                        // ?: throw IllegalArgumentException("${allLrrrParams[functionIndex - 1]} not a finite sequence")
                        // if (arguments.isEmpty()) throw IllegalArgumentException("Non-Nonadic Function invocation requires at least 1 argument ($function) ($workingCode) ($code)")

                        when (function) {
                            is NonadicFunction -> {
                                allLrrrParams[functionIndex] = FunctionInvocation(listOf(), function)
                            }
                            is MonadicFunction -> {
                                if (arguments is FunctionInvocation) {
                                    allLrrrParams[functionIndex] = FunctionInvocation(listOf(arguments), function)
                                }
                                arguments = arguments.toSequence()
                                if (arguments.list.size != 1) throw IllegalArgumentException("Nonadic function $function requires 1 arg. Given: $arguments ($workingCode)")
                                allLrrrParams[functionIndex] = FunctionInvocation(arguments.list, function)
                                allLrrrParams.removeAt(functionIndex - 1)
                            }
                            is PolyadicFunction -> {
                                val previousArguments = allLrrrParams.subList(0, functionIndex)
                                    .map { if (it is LrrrFiniteSequence<*>) it.list as List<LrrrValue> else listOf(it) }
                                    .flatten()
                                repeat((0..functionIndex).count()) { allLrrrParams.removeAt(0) }
                                allLrrrParams.add(0, FunctionInvocation(previousArguments, function))
                            }
                            is DiadicFunction -> {
                                // we're trying to find two arguments to apply to this function, first looking on the left, then on the right
                                val argumentList = mutableListOf<LrrrValue>()
                                val argumentIndicesToDelete = mutableListOf<Int>()
                                for (i in (functionIndex - 1) downTo 0) {
                                    var leftElement = allLrrrParams[i]
                                    if (leftElement is FunctionInvocation) {
                                        argumentList.add(leftElement)
                                        argumentIndicesToDelete.add(i)
                                    } else {
                                        leftElement = leftElement.toSequence()
                                        val list = leftElement.list
                                        argumentList.add(list.removeAt(list.lastIndex))
                                        if (argumentList.size == 1 && list.isNotEmpty()) {
                                            argumentList.add(list.removeAt(list.lastIndex))
                                        }
                                        if (list.isEmpty()) argumentIndicesToDelete.add(i)
                                    }
                                    if (argumentList.size == 2) break
                                }

                                if (argumentList.isEmpty()) throw IllegalArgumentException("There must be a diadic argument on the left in $workingCode")

                                if (argumentList.size != 2) {
                                    if (functionIndex == allLrrrParams.lastIndex) throw IllegalArgumentException("No last diadic argument in $workingCode")
                                    var nextArgument = allLrrrParams[functionIndex + 1]
                                    if (nextArgument is NonadicFunction || nextArgument is FunctionInvocation) {
                                        if (nextArgument is NonadicFunction) argumentList.add(
                                            FunctionInvocation(
                                                listOf(),
                                                nextArgument
                                            )
                                        )
                                        else argumentList.add(nextArgument)
                                        argumentIndicesToDelete.add(functionIndex + 1)
                                    } else if (nextArgument is LrrrFunction) throw IllegalArgumentException("Non-nonadic arg specified $workingCode")
                                    else {
                                        nextArgument = nextArgument.toSequence()
                                        val list = nextArgument.list
                                        argumentList.add(list.removeAt(0))
                                        if (list.isEmpty()) argumentIndicesToDelete.add(functionIndex + 1)
                                    }
                                }

                                if (argumentList.size != 2) throw IllegalArgumentException("Diadic function requires 2 arguments ($function)> Given: $arguments ($workingCode)")

                                allLrrrParams[functionIndex] = FunctionInvocation(argumentList, function)

                                if (argumentIndicesToDelete.isNotEmpty()) {
                                    allLrrrParams.removeAll(argumentIndicesToDelete.map { allLrrrParams[it] })
                                }
                            }
                        }

                    }
                }
            }

            lrrrValues.addAll(allLrrrParams)
            break

        }


        return GenericEvaluationScope(lrrrValues, StructureType.EMPTY, null)
    }
}


enum class StructureType {
    IF, NOT, ELSE, FOR, EMPTY
}


fun parseStructures(_program: String): List<ParseObj> {
    var program = _program
    var stringLocations = findStringLocations(program)

    val replacables = globalLrrr.functions.filter { it is ReplacementFunction }.map { it as ReplacementFunction }

    replacables.forEach { replacableFunction ->
        var i = 0
        while (i <= program.length - replacableFunction.identifier.length) {
            if (replacableFunction.identifier == program.substring(i,i+replacableFunction.identifier.length)
                && !isCharInCharDeclaration(i,program) && !isCharInString(i,stringLocations))  {
                program = program.substring(0, i) + replacableFunction.replaceWith + program.substring(i + replacableFunction.identifier.length)
                stringLocations = findStringLocations(program)
                i = 0
            } else i++
        }
    }

    val contextLocations = program
        .findAllLocations(*contextOpeningCharArray.toCharArray())
        .filter { location ->
            !isCharInString(location, stringLocations) && !isCharInCharDeclaration(
                location,
                program
            )
        }

    if (contextLocations.isEmpty()) {
        return createParseObjectsNoStructures(program)
    }

    val parseObjects = mutableListOf<ParseObj>()

    val min = contextLocations.min()!!
    val lastEndContextIndex =
        getCorrespondingIndex(program.substring(min + 1), contextOpeningCharArray, listOf('}'))
            ?.plus(min + 1)
            ?: program.length

    val contextCode = program.substring(min + 1, lastEndContextIndex)
    when {
        program[min] == contextStartChar -> {
            if (min != 0) {
                parseObjects.addAll(createParseObjectsNoStructures(program.substring(0, min)))
            }

            val scope = GenericParseStructure(parseStructures(contextCode), StructureType.EMPTY, contextCode, null)
            parseObjects.add(scope)
        }
        program[min] == forStartChar -> {
            val beforeForStart = program.substring(0, min)

            val beforeSplitByComma = beforeForStart.split(",")

            val firstParseObjects = createParseObjectsNoStructures(beforeSplitByComma.first())

            parseObjects.addAll(
                firstParseObjects.subList(
                    0,
                    firstParseObjects.lastIndex
                )
            )

            val condition = firstParseObjects.last()

            val incrementorFunction =
                if (beforeSplitByComma.size < 2 || beforeSplitByComma[1].isEmpty()) null else createParseObjectsNoStructures(
                    beforeSplitByComma[1]
                ).first()

            val initialValueParseObj =
                if (beforeSplitByComma.size < 3) null else createParseObjectsNoStructures(beforeSplitByComma[2]).first()

            val forStructure = ForParseStructure(
                parseStructures(contextCode),
                initialValueParseObj,
                incrementorFunction,
                condition,
                program.substring(0, lastEndContextIndex)
            )
            parseObjects.add(forStructure)

        }
        else -> {
            val beforeContextCode = program.substring(0, min)
            val parseObjectsBefore = createParseObjectsNoStructures(beforeContextCode)

            if (program[min] != elseChar) parseObjects.addAll(
                parseObjectsBefore.subList(
                    0,
                    parseObjectsBefore.lastIndex
                )
            )
            else parseObjects.addAll(parseObjectsBefore)

            val condition = if (program[min] != elseChar) parseObjectsBefore.last() else null

            val structure = GenericParseStructure(
                parseStructures(contextCode),
                when (program[min]) {
                    ifStartChar -> StructureType.IF
                    forStartChar -> StructureType.FOR
                    notStartChar -> StructureType.NOT
                    elseChar -> StructureType.ELSE
                    else -> throw IllegalArgumentException("Unknown context char ${program[min]}")
                },
                contextCode,
                condition
            )
            parseObjects.add(structure)
        }
    }

    if (lastEndContextIndex != program.length) {
        parseObjects.addAll(parseStructures(program.substring(lastEndContextIndex + 1)))
    }

    return parseObjects
}

fun createParseObjectsNoStructures(program: String): List<ParseObj> {
    val stringLocations = findStringLocations(program)
    return program.splitIf(';') { index -> !isCharInString(index, stringLocations) }.map { section ->
        ParseObj(section)
    }.filter { it.code.isNotEmpty() }
}

fun getCorrespondingIndex(string: String, startChars: List<Char>, endChars: List<Char>): Int? {
    val stringLocations = findStringLocations(string)

    var startCount = 1
    var endCount = 0

    string.forEachIndexed { i, char ->
        if ((char in startChars || char in endChars) && !isCharInString(i, stringLocations)) {
            if (char in startChars) startCount++
            else endCount++
        }
        if (startCount > 0 && startCount == endCount) return i
    }

    return null
}

fun isCharInString(index: Int, locations: List<StringLocation>) = locations.any { index in it.start..it.end }

fun isCharInCharDeclaration(index: Int, string: String) = index > 0 && string[index - 1] == '\''
        && if (index > 1) string[index - 1] != '\\' else true

fun String.splitIf(delimeter: Char, condition: (Int) -> Boolean): List<String> {
    val substrings = mutableListOf<String>()
    val charLocations = findAllLocations(delimeter).filter { index -> condition(index) }
    if (charLocations.isEmpty()) return listOf(this)
    if (charLocations.first() > 0) substrings.add(substring(0, charLocations.first()))
    charLocations.forEachIndexed { i, location ->
        if (i != charLocations.lastIndex) substrings.add(substring(location + 1, charLocations[i + 1]))
        else substrings.add(substring(location + 1))
    }

    return substrings.filter { it.isNotEmpty() }
}

fun findNextUnescapedStringCharacter(string: String): Int? {
    string.forEachIndexed { i, char -> if (char == '"' && (i == 0 || (string[i - 1] != '\\'))) return i }
    return null
}

fun splitParameters(string: String): List<String> {
    val stringLocations = findStringLocations(string)
    val splits = mutableListOf<Int>()
    var index = 0
    while (index < string.length) {
        if (string[index] == ',' && !isCharInString(index, stringLocations)) {
            splits.add(index)
        }
        index++
    }
    if (splits.isEmpty()) return listOf(string)

    return mutableListOf(
        string.substring(
            0,
            splits.first()
        )
    ).apply {
        addAll(splits.mapIndexed { i, location ->
            if (i == splits.lastIndex) string.substring(
                location + 1,
                string.length
            ) else string.substring(location + 1, splits[i + 1])
        })
    }.filter { it.isNotEmpty() }
}