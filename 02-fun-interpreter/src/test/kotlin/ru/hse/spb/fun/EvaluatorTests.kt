package ru.hse.spb.`fun`

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class EvaluatorTests : AbstractTest() {
    private fun testEvaluator(testName: String) {
        val scriptFileName = testName + AbstractTest.SCRIPT_EXTENSION
        val resultFileName = testName + AbstractTest.RESULT_EXTENSION

        val scriptFileUri = extractUri(scriptFileName)
        val resultFileUri = extractUri(resultFileName)

        if (scriptFileUri == null) {
            Assert.fail("Error: $scriptFileName does not exists")
            return
        }

        if (resultFileUri == null) {
            Assert.fail("Error: $resultFileName does not exists")
            return
        }

        val parser = getFunLanguageParser(scriptFileUri)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val printStream = PrintStream(byteArrayOutputStream)
        parser.file().accept(StatementsEvalVisitor(printStream))

        val actualCode = String(byteArrayOutputStream.toByteArray())
        val expectedCode = File(resultFileUri).readText()

        Assert.assertEquals(expectedCode, actualCode)
    }

    @Test
    fun testSimple() {
        testEvaluator("simple")
    }

    @Test
    fun testFunctions() {
        testEvaluator("functions")
    }

    @Test
    fun testLogic() {
        testEvaluator("logic")
    }

    @Test
    fun testArithmetic() {
        testEvaluator("arithmetic")
    }
}