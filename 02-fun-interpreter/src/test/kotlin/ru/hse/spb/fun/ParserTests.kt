package ru.hse.spb.`fun`

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Paths

class ParserTests {
    companion object {
        private const val SCRIPT_EXTENSION = ".fun"
        private const val CODE_EXTENSION = ".code"
    }

    private fun testCode(testName: String) {


        val scriptFileName = testName + SCRIPT_EXTENSION
        val codeFileName = testName + CODE_EXTENSION

        val scriptFileUri = ClassLoader.getSystemResources(scriptFileName).toList().firstOrNull()?.toURI()
        val codeFileUri = ClassLoader.getSystemResources(codeFileName).toList().firstOrNull()?.toURI()

        if (scriptFileUri == null) {
            fail("Error: $scriptFileName does not exists")
            return
        }

        if (codeFileUri == null) {
            fail("Error: $codeFileName does not exists")
            return
        }


        val expectedCode = File(codeFileUri).readText()

        val lexer = FunLanguageLexer(CharStreams.fromPath(Paths.get(scriptFileUri)))
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)
        val tokens = CommonTokenStream(lexer)

        val parser = FunLanguageParser(tokens)

        val printerVisitor = TreePrinterVisitor()
        parser.file().accept(printerVisitor)

        val actualCode = printerVisitor.toString()

        assertEquals(expectedCode, actualCode)
    }

    @Test
    fun testSimple() {
        testCode("simple")
    }

    @Test
    fun testFunctions() {
        testCode("functions")
    }

    @Test
    fun testLogic() {
        testCode("logic")
    }

    @Test
    fun testArithmetic() {
        testCode("arithmetic")
    }
}