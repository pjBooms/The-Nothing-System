package com.excelsior.common

/**
 * Tokenizer that handles quoted strings.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
class QuotedStringTokenizer {

    private var line: String = ""
    private var length: Int = 0
    private var curPos: Int = 0

    constructor()

    constructor(line: String) {
        init(line)
    }

    fun init(line: String) {
        this.line = line
        this.length = line.length
        curPos = 0
    }

    /** Returns next token, or null if end of string */
    fun nextToken(): String? {
        while (curPos < length && Character.isWhitespace(line[curPos])) {
            curPos++
        }
        if (curPos >= length) return null

        return if (line[curPos] == '"') {
            curPos++
            if (curPos >= length) return "\""
            val startToken = curPos
            while (curPos < length && line[curPos] != '"') {
                curPos++
            }
            if (curPos >= length) {
                line.substring(startToken)
            } else {
                curPos++
                line.substring(startToken, curPos - 1)
            }
        } else {
            val startToken = curPos
            while (curPos < length && !Character.isWhitespace(line[curPos])) {
                curPos++
            }
            if (curPos >= length) {
                line.substring(startToken)
            } else {
                line.substring(startToken, curPos)
            }
        }
    }
}
