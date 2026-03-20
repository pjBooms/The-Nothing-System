package com.excelsior.nothing

/**
 * Expression evaluator using JavaScript engine.
 * Translated from Java original by hedjuo, Excelsior LLC.
 */
object Calc {

    fun eval(expression: String): Int {
        return try {
            evalWithRhino(expression)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            evalSimple(expression)
        }
    }

    private fun evalWithRhino(expression: String): Int {
        val cx = org.mozilla.javascript.Context.enter()
        return try {
            cx.optimizationLevel = -1
            val scope = cx.initStandardObjects()
            val result = cx.evaluateString(scope, expression, "<eval>", 1, null)
            when (result) {
                is Double -> result.toInt()
                is Int -> result
                is Long -> result.toInt()
                is Number -> result.toInt()
                else -> evalSimple(expression)
            }
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }

    /**
     * Simple fallback evaluator for basic arithmetic when no script engine is available.
     * Handles +, -, *, / with integer operands (no parentheses).
     */
    private fun evalSimple(expression: String): Int {
        return try {
            val expr = expression.trim()
            // Try multiplication/division first (left to right), then addition/subtraction
            val tokens = tokenize(expr)
            evalTokens(tokens)
        } catch (e: Exception) {
            println("Eval error: ${e.message}")
            0
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || (c == '-' && tokens.isEmpty()) -> {
                    val start = i
                    if (c == '-') i++
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens.add(expr.substring(start, i))
                }
                c in "+-*/" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun evalTokens(tokens: List<String>): Int {
        if (tokens.isEmpty()) return 0
        // Handle * and / first
        val step1 = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if ((tokens[i] == "*" || tokens[i] == "/") && step1.isNotEmpty()) {
                val left = step1.removeLast().toDouble()
                val right = tokens[i + 1].toDouble()
                val result = if (tokens[i] == "*") left * right else left / right
                step1.add(result.toInt().toString())
                i += 2
            } else {
                step1.add(tokens[i])
                i++
            }
        }
        // Handle + and -
        var result = step1[0].toDouble()
        var j = 1
        while (j < step1.size) {
            val op = step1[j]
            val operand = step1[j + 1].toDouble()
            result = if (op == "+") result + operand else result - operand
            j += 2
        }
        return result.toInt()
    }
}
