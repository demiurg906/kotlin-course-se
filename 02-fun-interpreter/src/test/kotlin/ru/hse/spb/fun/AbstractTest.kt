package ru.hse.spb.`fun`

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.net.URI
import java.nio.file.Paths

abstract class AbstractTest {
    companion object {
        internal const val SCRIPT_EXTENSION = ".fun"
        internal const val CODE_EXTENSION = ".code"
        internal const val RESULT_EXTENSION = ".result"
    }

    protected fun extractUri(fileName: String) =
            ClassLoader.getSystemResources(fileName).toList().firstOrNull()?.toURI()

    protected fun getFunLanguageParser(scriptFileUri: URI): FunLanguageParser {
        val lexer = FunLanguageLexer(CharStreams.fromPath(Paths.get(scriptFileUri)))
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)

        val tokens = CommonTokenStream(lexer)
        val parser = FunLanguageParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        return parser
    }
}