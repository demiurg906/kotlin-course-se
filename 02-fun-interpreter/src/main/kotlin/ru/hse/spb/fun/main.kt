@file:JvmName("Main")

package ru.hse.spb.`fun`

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
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
    } catch (e: FunInterpreterException) {
        println("Error at ${e.line}:${e.position}: ${e.message}")
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