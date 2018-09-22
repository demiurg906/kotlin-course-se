package ru.hse.spb.`fun`

import org.antlr.v4.runtime.*
import java.io.File

fun main(args: Array<String>) {
    val fileName = args.getOrNull(0)
    if (fileName == null) {
        println("Not enough arguments")
        return
    }


    try {
        val lexer = FunLanguageLexer(CharStreams.fromString(readFileContent(fileName)))
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)
        val tokens = CommonTokenStream(lexer)

        val parser = FunLanguageParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)
        parser.file().accept(StatementsEvalVisitor(System.out))

//        val printerVisitor = TreePrinterVisitor()
//        parser.file().accept(printerVisitor)
//        println(printerVisitor.toString())
    } catch (e: FunInterpreterException) {
        println("Error at ${e.line}:${e.position}: ${e.message}")
//        e.printStackTrace()
    }

}

fun readFileContent(fileName: String): String {
    val file = File(fileName)
    val lines = file.readLines().toMutableList()
    if (lines.last().trim() != "") {
        lines.add("")
    }
    return lines.joinToString("\n")
}

// -----------------------------------------------------------------------

object ThrowingErrorListener : BaseErrorListener() {
    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        throw FunInterpreterException(line, charPositionInLine, "Can't parse input");
    }
}

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