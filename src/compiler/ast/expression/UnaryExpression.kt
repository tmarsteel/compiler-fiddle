package compiler.ast.expression

import compiler.ast.FunctionDeclaration
import compiler.ast.context.CTContext
import compiler.ast.type.FunctionModifier
import compiler.ast.type.TypeReference
import compiler.lexer.Operator

class UnaryExpression(val operator: Operator, val valueExpression: Expression): Expression
{
    override fun determinedType(context: CTContext): TypeReference {
        return getOperatorFunction(context)?.returnType ?: compiler.ast.type.Any.defaultReference
    }

    /**
     * Attempts to resolve the operator function in the given context.
     *
     * @return The operator function to use to evaluate this expression or null the given context does not contain
     *         a suitable function.
     */
    private fun getOperatorFunction(context: CTContext): FunctionDeclaration? {
        val valueType = valueExpression.determinedType(context)

        val opFunName = "unary" + operator.name[0].toUpperCase() + operator.name.substring(1).toLowerCase()

        // functions with receiver
        val receiverOperatorFuns = context.resolveFunctions(opFunName, valueType)
            .filter { FunctionModifier.OPERATOR in it.modifiers }
            .filter { it.parameters.parameters.isEmpty() }

        return receiverOperatorFuns.firstOrNull()
    }
}