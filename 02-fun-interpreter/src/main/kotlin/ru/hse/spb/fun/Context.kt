package ru.hse.spb.`fun`

class Context(val parentContext: Context?) {
    val variables: VariableMap = VariableMap()
    val functions: FunctionMap = FunctionMap()
    var returnValue: Int? = null
    val depth: Int

    init {
        depth = if (parentContext == null) 0 else parentContext.depth + 1
    }

    inner class VariableMap {
        private val variables: MutableMap<String, Int?> = mutableMapOf()
        operator fun get(variable: String): Int? = variables[variable] ?: parentContext?.variables?.get(variable)
        operator fun set(variable: String, value: Int?) {
            variables[variable] = value
        }
        operator fun contains(variable: String) = variable in variables
    }

    inner class FunctionMap {
        private val functions: MutableMap<String, FunctionType> = mutableMapOf()
        operator fun get(function: String): FunctionType? = functions[function] ?: parentContext?.functions?.get(function)
        operator fun set(function: String, value: FunctionType) {
            if (function in functions) throw FunctionOverrideException("You try to override function '$function'")
            functions[function] = value
        }
    }
}

class FunctionOverrideException(message: String? = null) : Exception(message)