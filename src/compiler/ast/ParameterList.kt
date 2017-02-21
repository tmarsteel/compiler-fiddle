package compiler.ast

import compiler.ast.types.TypeReference
import compiler.lexer.IdentifierToken

class ParameterList (
    val parameters: List<Parameter> = emptyList()
)

class Parameter (
    val name: IdentifierToken,
    val type: TypeReference?
)