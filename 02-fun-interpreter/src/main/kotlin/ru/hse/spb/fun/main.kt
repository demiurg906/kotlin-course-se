package ru.hse.spb.`fun`

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream

fun main(args: Array<String>) {
    val fileName = args.getOrNull(0)
    if (fileName == null) {
        println("Not enough arguments")
        return
    }

    val lexer = FunLanguageLexer(CharStreams.fromFileName(fileName))
    val tokens = CommonTokenStream(lexer)

    val parser = FunLanguageParser(tokens)
    parser.file().accept(StatementsEvalVisitor())
}

// -----------------------------------------------------------------------

// -----------------------------------------------------------------------

class TokenStreamIterator(private val stream: TokenStream) {
    private var current = 0

    operator fun next(): Token {
        val token = stream.get(current)
        current += 1
        return token
    }

    operator fun hasNext(): Boolean = current < stream.size()
}

operator fun TokenStream.iterator() = TokenStreamIterator(this)

fun TokenStream.toList(): List<Token> {
    val res = mutableListOf<Token>()
    for (token in this) {
        res.add(token)
    }
    return res
}

fun TokenStream.println() = this.toList().forEach { println("${FunLanguageLexer.VOCABULARY.getSymbolicName(it.type)}, $it") }