package ru.hse.spb.`fun`

import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.PrintStream

class StatementsEvalVisitor(private val output: PrintStream): FunLanguageBaseVisitor<Unit>() {
    companion object {
        const val PRINTLN_FUNCTION = "println"

        val PLUS = literalName(FunLanguageLexer.PLUS)
        val MINUS = literalName(FunLanguageLexer.MINUS)
        val MULT = literalName(FunLanguageLexer.MULT)
        val DIV = literalName(FunLanguageLexer.DIV)
        val MOD = literalName(FunLanguageLexer.MOD)
        val EQUAL = literalName(FunLanguageLexer.EQUAL)
        val NOT_EQUAL = literalName(FunLanguageLexer.NOT_EQUAL)
        val GT = literalName(FunLanguageLexer.GT)
        val GE = literalName(FunLanguageLexer.GE)
        val LT = literalName(FunLanguageLexer.LT)
        val LE = literalName(FunLanguageLexer.LE)

        private fun literalName(literal: Int) = FunLanguageLexer.VOCABULARY.getLiteralName(literal).replace("'", "")
    }

    private var context = Context(null)
    private val intExpressionEvaluator = IntExpressionEvaluator()
    private val logicExpressionEvaluator = LogicExpressionEvaluator()

    private fun cutContext(depth: Int) {
        while (context.depth != depth) {
            context = context.parentContext!!
        }
    }

    // ------------------------- Blocks -------------------------

    override fun visitFile(ctx: FunLanguageParser.FileContext) {
        ctx.block().accept(this)
    }

    override fun visitBlockWithBracers(ctx: FunLanguageParser.BlockWithBracersContext) {
        ctx.block().accept(this)
    }

    override fun visitBlock(ctx: FunLanguageParser.BlockContext) {
        val startDepth = context.depth
        context = Context(context)

        for (statement in ctx.statements) {
            statement.accept(this)
            if (statement.returnStatement() != null) break
        }

        val returnValue = context.returnValue
        cutContext(startDepth)
        context.returnValue = returnValue
    }

    // ------------------------- Statements -------------------------

    override fun visitStatement(ctx: FunLanguageParser.StatementContext) {
        val childCtx = ctx.functionDef()
                ?: ctx.variable()
                ?: ctx.intExpr()
                ?: ctx.logicExpr()
                ?: ctx.whileLoop()
                ?: ctx.ifOperator()
                ?: ctx.assignment()
                ?: ctx.returnStatement()
                ?: throw EvaluatingException()
        childCtx.accept(this)
    }

    override fun visitFunctionDef(ctx: FunLanguageParser.FunctionDefContext) {
        val functionName = ctx.Identifier().extractNameFromIdentifier()
        val parameterNames = ctx.parameterNames().extractParameters()

        context.functions[functionName] = FunctionType(parameterNames, ctx.blockWithBracers())
    }

    override fun visitIntExpr(ctx: FunLanguageParser.IntExprContext) {
        ctx.accept(intExpressionEvaluator)
    }

    override fun visitLogicExpr(ctx: FunLanguageParser.LogicExprContext) {
        ctx.accept(logicExpressionEvaluator)
    }

    private fun FunLanguageParser.ParameterNamesContext.extractParameters() =
            params.map { it.extractNameFromIdentifier() }

    override fun visitVariable(ctx: FunLanguageParser.VariableContext) {
        val variable = ctx.Identifier().extractNameFromIdentifier()
        val value = ctx.intExpr()?.accept(intExpressionEvaluator)

        context.variables[variable] = value
    }

    override fun visitWhileLoop(ctx: FunLanguageParser.WhileLoopContext) {
        while (ctx.logicExpr().accept(logicExpressionEvaluator)) {
            ctx.blockWithBracers().accept(this)
        }
    }

    override fun visitIfOperator(ctx: FunLanguageParser.IfOperatorContext) {
        if (ctx.logicExpr().accept(logicExpressionEvaluator)) {
            ctx.thenBlock.accept(this)
        } else {
            ctx.elseBlock?.accept(this)
        }
    }

    override fun visitAssignment(ctx: FunLanguageParser.AssignmentContext) {
        val variable = ctx.Identifier().extractNameFromIdentifier()
        if (variable !in context.variables) throw EvaluatingException("Variable $variable is not defined")
        val value = ctx.intExpr().accept(intExpressionEvaluator)
        context.variables[variable] = value
    }

    override fun visitReturnStatement(ctx: FunLanguageParser.ReturnStatementContext) {
        val value = ctx.intExpr().accept(intExpressionEvaluator)
        context.returnValue = value
    }

    // ------------------------- Logic expressions -------------------------

    private inner class LogicExpressionEvaluator : FunLanguageBaseVisitor<Boolean>() {
        override fun visitLogicExpr(ctx: FunLanguageParser.LogicExprContext): Boolean {
            return when {
                ctx.logicOrExpr() != null -> ctx.logicOrExpr().accept(this)
                ctx.logicExpr() != null -> ctx.logicExpr().accept(this)
                else -> throw EvaluatingException()
            }
        }

        override fun visitLogicOrExpr(ctx: FunLanguageParser.LogicOrExprContext): Boolean {
            val initValue = ctx.`var`.accept(this)

            val vars = ctx.vars.map { it.accept(this) }
            return vars.fold(initValue) { left, right -> left || right }
        }

        override fun visitLogicAndExpr(ctx: FunLanguageParser.LogicAndExprContext): Boolean {
            val initValue = ctx.`var`.accept(this)

            val vars = ctx.vars.map { it.accept(this) }
            return vars.fold(initValue) { left, right -> left && right }
        }

        override fun visitAtomLogicExpr(ctx: FunLanguageParser.AtomLogicExprContext): Boolean {
            return when {
                ctx.value != null -> ctx.value.accept(this)
                ctx.exp != null -> ctx.exp.accept(this)

                else -> throw EvaluatingException()
            }
        }

        override fun visitEqualityExpr(ctx: FunLanguageParser.EqualityExprContext): Boolean {
            val value1 = ctx.var1.accept(intExpressionEvaluator)
            val value2 = ctx.var2.accept(intExpressionEvaluator)

            return when (ctx.op.text) {
                EQUAL -> value1 == value2
                NOT_EQUAL -> value1 != value2
                LT -> value1 < value2
                LE -> value1 <= value2
                GT -> value1 > value2
                GE -> value1 >= value2
                else -> throw EvaluatingException()
            }
        }
    }

    // ------------------------- Int expressions -------------------------

    private inner class IntExpressionEvaluator : FunLanguageBaseVisitor<Int>() {
        private fun FunLanguageParser.ArgumentsContext.extractArguments() = args.map { it.accept(intExpressionEvaluator) }

        override fun visitFunctionCall(ctx: FunLanguageParser.FunctionCallContext): Int {
            val functionName = ctx.Identifier().extractNameFromIdentifier()
            val parameterValues = ctx.arguments().extractArguments()

            if (functionName == PRINTLN_FUNCTION) {
                val message = parameterValues.joinToString(" ")
                output.println(message)
                return 0
            }

            val startDepth = context.depth
            context = Context(context)
            val (parameterNames, function) = context.functions[functionName] ?: throw EvaluatingException("Function $functionName not declared")
            parameterNames.zip(parameterValues).forEach { (name, value) -> context.variables[name] = value }

            function.accept(this@StatementsEvalVisitor)

            val returnValue = context.returnValue
            cutContext(startDepth)

            return returnValue ?: 0
        }

        override fun visitIntExpr(ctx: FunLanguageParser.IntExprContext): Int {
            return when {
                ctx.additionExp() != null -> ctx.additionExp().accept(this)
                ctx.functionCall() != null -> ctx.functionCall().accept(this)
                ctx.Identifier() != null -> ctx.Identifier().extractVariableFromIdentifier()
                ctx.Literal() != null -> ctx.Literal().extractValueFromLiteral()
                ctx.intExpr() != null -> ctx.intExpr().accept(this)

                else -> throw EvaluatingException()
            }
        }

        override fun visitAdditionExp(ctx: FunLanguageParser.AdditionExpContext): Int {
            val initValue = ctx.`var`.accept(this)

            val vars = ctx.vars.map { it.accept(this) }
            return vars.zip(ctx.ops).fold<Pair<Int, Token>, Int>(initValue) { left, (right, operation) ->
                when (operation.text) {
                    PLUS -> left + right
                    MINUS -> left - right
                    else -> throw EvaluatingException()
                }
            }
        }

        override fun visitMultiplyExp(ctx: FunLanguageParser.MultiplyExpContext): Int {
            val initValue = ctx.`var`.accept(this)

            val vars = ctx.vars.map { it.accept(this) }
            return vars.zip(ctx.ops).fold(initValue) { left, (right, operation) ->
                when (operation.text) {
                    // TODO: ctx.MULT == null
                    MULT -> left * right
                    DIV -> left / right
                    MOD-> left % right
                    else -> throw EvaluatingException("${operation.text}, $MULT")
                }
            }
        }

        override fun visitAtomExp(ctx: FunLanguageParser.AtomExpContext): Int {
            return when {
                ctx.n != null -> ctx.n.extractValueFromLiteral()
                ctx.id != null -> ctx.id.extractVariableFromIdentifier()
                ctx.exp != null -> ctx.exp.accept(this)

                else -> throw EvaluatingException()
            }
        }
    }

    // ------------------------- Utils -------------------------

    private fun TerminalNode.extractNameFromIdentifier() = symbol.text
    private fun TerminalNode.extractVariableFromIdentifier() = symbol.extractVariableFromIdentifier()

    private fun TerminalNode.extractValueFromLiteral() = symbol.extractValueFromLiteral()
    private fun Token.extractNameFromIdentifier() = text

    private fun Token.extractValueFromLiteral(): Int =
            text.toIntOrNull() ?: throw EvaluatingException("Illegal integer literal: $text")

    private fun Token.extractVariableFromIdentifier(): Int = context.variables[text] ?: throw EvaluatingException("Undefined identifier $text")

}

class EvaluatingException(message: String? = null) : Exception(message)
