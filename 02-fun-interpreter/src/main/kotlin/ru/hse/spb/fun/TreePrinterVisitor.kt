package ru.hse.spb.`fun`

class TreePrinterVisitor : FunLanguageBaseVisitor<Unit>() {
    companion object {
        private const val TAB = "  "
        private const val L_BR = "("
        private const val R_BR = ")"
        private const val L_FIG_BR = " {\n"
        private const val R_FIG_BR = "}\n"
        private const val SPACE = " "
        private const val NEWLINE = "\n"
        private const val COMMA = ", "

        private const val FUN = "fun "
        private const val VAR = "var "
        private const val WHILE = "while "
        private const val IF = "if "
        private const val ELSE = "else "
        private const val RETURN = "return "

        private const val OR = " || "
        private const val AND = " && "

        private const val ASSIGNMENT = " = "
    }

    private val sb = StringBuilder()

    private var indent = -1
    private val tab: String
        get() = TAB.repeat(indent)

    override fun toString(): String {
//        for (line in sb.toString().lines()) {
//            val trimmed = line.trimEnd()
//            val x = 0
//        }
        return sb.toString().lines().joinToString(NEWLINE) { it.trimEnd() }
    }

    // ------------------------- Blocks -------------------------

    override fun visitFile(ctx: FunLanguageParser.FileContext) {
        ctx.block().accept(this)
    }

    override fun visitBlockWithBracers(ctx: FunLanguageParser.BlockWithBracersContext) {
        sb.append(L_FIG_BR)
        ctx.block().accept(this)
        sb.append(tab).append(R_FIG_BR)
    }

    override fun visitBlock(ctx: FunLanguageParser.BlockContext) {
        indent++
        for (statement in ctx.statements) {
            sb.append(tab)
            statement.accept(this)
            sb.append(NEWLINE)
        }
        indent--
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
                ?: return
        childCtx.accept(this)
    }

    override fun visitFunctionDef(ctx: FunLanguageParser.FunctionDefContext) {
        sb.append(NEWLINE).append(tab).append(FUN).append(ctx.Identifier().text)
        ctx.parameterNames().accept(this)
        ctx.blockWithBracers().accept(this)
    }

    override fun visitParameterNames(ctx: FunLanguageParser.ParameterNamesContext) {
        sb.append(L_BR).append(ctx.params.joinToString(COMMA) { it.text }).append(R_BR)
    }

    override fun visitVariable(ctx: FunLanguageParser.VariableContext) {
        sb.append(VAR).append(ctx.Identifier().text).append(ASSIGNMENT)
        ctx.intExpr().accept(this)
    }

    override fun visitWhileLoop(ctx: FunLanguageParser.WhileLoopContext) {
        sb.append(WHILE).append(L_BR)
        ctx.logicExpr().accept(this)
        sb.append(R_BR)
        ctx.blockWithBracers().accept(this)
    }

    override fun visitIfOperator(ctx: FunLanguageParser.IfOperatorContext) {
        sb.append(IF).append(L_BR)
        ctx.logicExpr().accept(this)
        sb.append(R_BR)
        ctx.thenBlock.accept(this)
        if (ctx.elseBlock != null) {
            sb.append(tab).append(ELSE)
            ctx.elseBlock.accept(this)
        }
    }

    override fun visitAssignment(ctx: FunLanguageParser.AssignmentContext) {
        sb.append(ctx.Identifier().text).append(ASSIGNMENT)
        ctx.intExpr().accept(this)
    }

    override fun visitReturnStatement(ctx: FunLanguageParser.ReturnStatementContext) {
        sb.append(RETURN)
        ctx.intExpr().accept(this)
    }

    // ------------------------- Expressions  -------------------------

    override fun visitLogicExpr(ctx: FunLanguageParser.LogicExprContext) {
        when {
            ctx.logicOrExpr() != null -> ctx.logicOrExpr().accept(this)
            ctx.logicExpr() != null -> {
                sb.append(L_BR)
                ctx.logicExpr().accept(this)
                sb.append(R_BR)
            }
        }
    }

    override fun visitIntExpr(ctx: FunLanguageParser.IntExprContext) {
        when {
            ctx.additionExp() != null -> ctx.additionExp().accept(this)
            ctx.functionCall() != null -> ctx.functionCall().accept(this)
            ctx.Identifier() != null -> sb.append(ctx.Identifier().text)
            ctx.Literal() != null -> sb.append(ctx.Literal().text)
            ctx.intExpr() != null -> ctx.intExpr().accept(this)
        }
    }

    override fun visitFunctionCall(ctx: FunLanguageParser.FunctionCallContext) {
        sb.append(ctx.Identifier().text)
        ctx.arguments().accept(this)
    }

    override fun visitArguments(ctx: FunLanguageParser.ArgumentsContext) {
        sb.append(L_BR)
        if (ctx.args.isEmpty()) {
            sb.append(R_BR)
            return
        }
        ctx.args.first().accept(this)
        for (arg in ctx.args.subList(1, ctx.args.size)) {
            sb.append(COMMA)
            arg.accept(this)
        }
        sb.append(R_BR)
    }

    // ------------------------- Logic expressions  -------------------------
    override fun visitLogicOrExpr(ctx: FunLanguageParser.LogicOrExprContext) {
        ctx.`var`.accept(this)
        for (variable in ctx.vars) {
            sb.append(OR)
            variable.accept(this)
        }
    }

    override fun visitLogicAndExpr(ctx: FunLanguageParser.LogicAndExprContext) {
        ctx.`var`.accept(this)
        for (variable in ctx.vars) {
            sb.append(AND)
            variable.accept(this)
        }
    }

    override fun visitAtomLogicExpr(ctx: FunLanguageParser.AtomLogicExprContext) {
        when {
            ctx.value != null -> ctx.value.accept(this)
            ctx.exp != null -> {
                sb.append(L_BR)
                ctx.exp.accept(this)
                sb.append(R_BR)
            }
        }
    }

    override fun visitEqualityExpr(ctx: FunLanguageParser.EqualityExprContext) {
        ctx.var1.accept(this)
        sb.append(SPACE).append(ctx.op.text).append(SPACE)
        ctx.var2.accept(this)
    }

    // ------------------------- Int expressions -------------------------

    override fun visitAdditionExp(ctx: FunLanguageParser.AdditionExpContext) {
        ctx.`var`.accept(this)
        for ((variable, op) in ctx.vars.zip(ctx.ops)) {
            sb.append(SPACE).append(op.text).append(SPACE)
            variable.accept(this)
        }
    }

    override fun visitMultiplyExp(ctx: FunLanguageParser.MultiplyExpContext) {
        ctx.`var`.accept(this)
        for ((variable, op) in ctx.vars.zip(ctx.ops)) {
            sb.append(SPACE).append(op.text).append(SPACE)
            variable.accept(this)
        }
    }

    override fun visitAtomExp(ctx: FunLanguageParser.AtomExpContext) {
        when {
            ctx.n != null -> sb.append(ctx.n.text)
            ctx.id != null -> sb.append(ctx.id.text)
            ctx.exp != null -> {
                sb.append(L_BR)
                ctx.exp.accept(this)
                sb.append(R_BR)
            }
        }
    }
}