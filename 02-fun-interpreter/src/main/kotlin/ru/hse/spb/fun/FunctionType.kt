package ru.hse.spb.`fun`

data class FunctionType(
        val parameterNames: List<String>,
        val function: FunLanguageParser.BlockWithBracersContext
)