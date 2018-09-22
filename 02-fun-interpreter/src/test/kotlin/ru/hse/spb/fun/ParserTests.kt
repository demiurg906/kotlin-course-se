package ru.hse.spb.`fun`

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class ParserTests : AbstractTest() {
    private fun testCode(testName: String) {
        val scriptFileName = testName + SCRIPT_EXTENSION
        val codeFileName = testName + CODE_EXTENSION

        val scriptFileUri = extractUri(scriptFileName)
        val codeFileUri = extractUri(codeFileName)

        if (scriptFileUri == null) {
            fail("Error: $scriptFileName does not exists")
            return
        }

        if (codeFileUri == null) {
            fail("Error: $codeFileName does not exists")
            return
        }

        val parser = getFunLanguageParser(scriptFileUri)
        val printerVisitor = TreePrinterVisitor()
        parser.file().accept(printerVisitor)

        val actualCode = printerVisitor.toString()
        val expectedCode = File(codeFileUri).readText()

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