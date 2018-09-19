package ru.hse.spb.`fun`

sealed class VariableType

class IntType(val value: Int) : VariableType() {
    override fun toString() = value.toString()
}

class BoolType(val value: Boolean) : VariableType() {
    override fun toString() = value.toString()
}

data class FunctionType(
        val parameterNames: List<String>,
        val function: FunLanguageParser.BlockWithBracersContext
)