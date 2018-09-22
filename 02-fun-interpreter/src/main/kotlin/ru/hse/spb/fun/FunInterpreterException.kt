package ru.hse.spb.`fun`

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

class FunInterpreterException(val line: Int, val position: Int, message: String? = null) : Exception(message) {
    constructor(token: Token, message: String? = null) : this(token.line, token.charPositionInLine, message)
    constructor(ctx: ParserRuleContext, message: String? = null) : this(ctx.start, message)
}