package ru.hse.spb.`fun`

import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

class StatementsEvalVisitor : FunLanguageBaseVisitor<Unit>() {
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
        val AND = literalName(FunLanguageLexer.AND)
        val OR = literalName(FunLanguageLexer.OR)

        private fun literalName(literal: Int) = FunLanguageLexer.VOCABULARY.getLiteralName(literal).replace("'", "")
    }

    private var context = Context(null)
    private val expressionEvaluator = ExprEvalVisitor()

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
        ctx.accept(expressionEvaluator)
    }

    override fun visitLogicExpr(ctx: FunLanguageParser.LogicExprContext) {
        ctx.accept(expressionEvaluator)
    }

    private fun FunLanguageParser.ParameterNamesContext.extractParameters() =
            params.map { it.extractNameFromIdentifier() }

    override fun visitVariable(ctx: FunLanguageParser.VariableContext) {
        val variable = ctx.Identifier().extractNameFromIdentifier()
        val value = ctx.intExpr()?.accept(expressionEvaluator)

        context.variables[variable] = value
    }

    override fun visitWhileLoop(ctx: FunLanguageParser.WhileLoopContext) {
        while (ctx.logicExpr().extractValue()) {
            ctx.blockWithBracers().accept(this)
        }
    }

    override fun visitIfOperator(ctx: FunLanguageParser.IfOperatorContext) {
        if (ctx.logicExpr().extractValue()) {
            ctx.thenBlock.accept(this)
        } else {
            ctx.elseBlock?.accept(this)
        }
    }

    override fun visitAssignment(ctx: FunLanguageParser.AssignmentContext) {
        val variable = ctx.Identifier().extractNameFromIdentifier()
        if (variable !in context.variables) throw EvaluatingException("Variable $variable is not defined")
        val value = ctx.intExpr().extractValue()
        context.variables[variable] = IntType(value)
    }

    override fun visitReturnStatement(ctx: FunLanguageParser.ReturnStatementContext) {
        val value = ctx.intExpr().extractValue()
        context.returnValue = value
    }

    // ------------------------- Expressions -------------------------

    private inner class ExprEvalVisitor : FunLanguageBaseVisitor<VariableType>() {
        private fun FunLanguageParser.ArgumentsContext.extractArguments() =
            args.map { it.accept(expressionEvaluator) }
                    .map { it as? IntType }
                    .map { it?.value ?: throw EvaluatingException() }

        override fun visitFunctionCall(ctx: FunLanguageParser.FunctionCallContext): VariableType {
            val functionName = ctx.Identifier().extractNameFromIdentifier()
            val parameterValues = ctx.arguments().extractArguments()

            if (functionName == PRINTLN_FUNCTION) {
                val message = parameterValues.joinToString(" ")
                println(message)
                return IntType(0)
            }

            val startDepth = context.depth
            context = Context(context)
            val (parameterNames, function) = context.functions[functionName] ?: throw EvaluatingException("Function $functionName not declared")
            parameterNames.zip(parameterValues).forEach { (name, value) -> context.variables[name] = IntType(value) }

            function.accept(this@StatementsEvalVisitor)

            val returnValue = context.returnValue
            cutContext(startDepth)

            return IntType(returnValue ?: 0)
        }

        // ------------------------- Arithmetic -------------------------

        override fun visitIntExpr(ctx: FunLanguageParser.IntExprContext): VariableType {
            return when {
                ctx.additionExp() != null -> ctx.additionExp().accept(this)
                ctx.functionCall() != null -> ctx.functionCall().accept(this)
                ctx.Identifier() != null -> ctx.Identifier().extractVariableFromIdentifier()
                ctx.Literal() != null -> ctx.Literal().extractValueFromLiteral()
                ctx.intExpr() != null -> ctx.intExpr().accept(this)

                else -> throw EvaluatingException()
            }
        }

        override fun visitAdditionExp(ctx: FunLanguageParser.AdditionExpContext): VariableType {
            val initValue = (ctx.`var`.accept(this) as? IntType)?.value ?: throw EvaluatingException()

            val vars = ctx.vars.map { (it.accept(this) as? IntType)?.value ?: throw EvaluatingException() }
            val resultValue =vars.zip(ctx.ops).fold(initValue) { left, (right, operation) ->
                when (operation.text) {
                    PLUS -> left + right
                    MINUS -> left - right
                    else -> throw EvaluatingException()
                }
            }
            return IntType(resultValue)
        }

        override fun visitMultiplyExp(ctx: FunLanguageParser.MultiplyExpContext): VariableType {
            val initValue = (ctx.`var`.accept(this) as? IntType)?.value ?: throw EvaluatingException()

            val vars = ctx.vars.map { (it.accept(this) as? IntType)?.value ?: throw EvaluatingException() }
            val resultValue = vars.zip(ctx.ops).fold(initValue) { left, (right, operation) ->
                when (operation.text) {
                    // TODO: ctx.MULT == null
                    MULT -> left * right
                    DIV -> left / right
                    MOD-> left % right
                    else -> throw EvaluatingException("${operation.text}, $MULT")
                }
            }
            return IntType(resultValue)
        }

        override fun visitAtomExp(ctx: FunLanguageParser.AtomExpContext): VariableType {
            return when {
                ctx.n != null -> ctx.n.extractValueFromLiteral()
                ctx.id != null -> ctx.id.extractVariableFromIdentifier()
                ctx.exp != null -> ctx.exp.accept(this)

                else -> throw EvaluatingException()
            }
        }

        // ------------------------- Logic -------------------------

        override fun visitLogicExpr(ctx: FunLanguageParser.LogicExprContext): VariableType {
            return when {
                ctx.logicOrExpr() != null -> ctx.logicOrExpr().accept(this)
                ctx.logicExpr() != null -> ctx.logicExpr().accept(this)
                else -> throw EvaluatingException()
            }
        }

        override fun visitLogicOrExpr(ctx: FunLanguageParser.LogicOrExprContext): VariableType {
            val initValue = (ctx.`var`.accept(this) as? BoolType)?.value ?: throw EvaluatingException()

            val vars = ctx.vars.map { (it.accept(this) as? BoolType)?.value ?: throw EvaluatingException() }
            val resultValue = vars.fold(initValue) { left, right -> left || right }
            return BoolType(resultValue)
        }

        override fun visitLogicAndExpr(ctx: FunLanguageParser.LogicAndExprContext): VariableType {
            val initValue = (ctx.`var`.accept(this) as? BoolType)?.value ?: throw EvaluatingException()

            val vars = ctx.vars.map { (it.accept(this) as? BoolType)?.value ?: throw EvaluatingException() }
            val resultValue = vars.fold(initValue) { left, right -> left && right }
            return BoolType(resultValue)
        }

        override fun visitAtomLogicExpr(ctx: FunLanguageParser.AtomLogicExprContext): VariableType {
            return when {
                ctx.value != null -> ctx.value.accept(this)
                ctx.`var` != null -> ctx.`var`.extractVariableFromIdentifier()
                ctx.exp != null -> ctx.exp.accept(this)

                else -> throw EvaluatingException()
            }
        }

        override fun visitEqualityExpr(ctx: FunLanguageParser.EqualityExprContext): VariableType {
            val value1 = ctx.var1.extractValue()
            val value2 = ctx.var2.extractValue()

            val result = when (ctx.op.text) {
                EQUAL -> value1 == value2
                NOT_EQUAL -> value1 != value2
                LT -> value1 < value2
                LE -> value1 <= value2
                GT -> value1 > value2
                GE -> value1 >= value2
                else -> throw EvaluatingException()
            }
            return BoolType(result)
        }
    }

    // ------------------------- Utils -------------------------

    private fun FunLanguageParser.IntExprContext.extractValue() = (accept(expressionEvaluator)as? IntType)?.value ?: throw EvaluatingException()
    private fun FunLanguageParser.LogicExprContext.extractValue() = (accept(expressionEvaluator) as? BoolType)?.value ?: throw EvaluatingException()

    private fun TerminalNode.extractNameFromIdentifier() = symbol.text
    private fun TerminalNode.extractVariableFromIdentifier() = symbol.extractVariableFromIdentifier()

    private fun TerminalNode.extractValueFromLiteral() = symbol.extractValueFromLiteral()
    private fun Token.extractNameFromIdentifier() = text

    private fun Token.extractValueFromLiteral(): IntType {
        val value = text.toIntOrNull() ?: throw EvaluatingException("Illegal integer literal: ${text}")
        return IntType(value)
    }

    private fun Token.extractVariableFromIdentifier(): VariableType {
        return context.variables[text] ?: throw EvaluatingException("Undefined identifier $text")
    }
}

class EvaluatingException(message: String? = null) : Exception(message)
