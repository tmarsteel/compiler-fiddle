package compiler.ast.expression

import compiler.InternalCompilerError
import compiler.ast.Executable
import compiler.binding.BindingResult
import compiler.binding.context.CTContext
import compiler.binding.expression.BoundExpression
import compiler.binding.expression.BoundInvocationExpression
import compiler.binding.expression.BoundNullLiteralExpression
import compiler.binding.expression.StaticDispatchInvocationExpression
import compiler.binding.filterAndSortByMatchForInvocationTypes
import compiler.lexer.SourceLocation
import compiler.parser.Reporting

class InvocationExpression(
    /**
     * The target of the invocation. e.g.:
     * * `doStuff()` => `IdentifierExpression(doStuff)`
     * * `obj.doStuff()` => `MemberAccessExpression(obj, doStuff)`
     */
    val targetExpression: Expression<*>,
    val parameterExprs: List<Expression<*>>
) : Expression<BoundInvocationExpression>, Executable<BoundInvocationExpression> {
    override val sourceLocation: SourceLocation = when(targetExpression) {
        is MemberAccessExpression -> targetExpression.memberName.sourceLocation
        else -> targetExpression.sourceLocation
    }

    override fun bindTo(context: CTContext): BoundInvocationExpression {
        val reportings = mutableListOf<Reporting>()

        // bind all the parameters
        val boundParameterValueExprs = parameterExprs.map { it.bindTo(context) }

        if (targetExpression is MemberAccessExpression) {
            return BoundInvocationExpression(
                    context,
                    this,
                    targetExpression.valueExpression.bindTo(context),
                    targetExpression.memberName,
                    boundParameterValueExprs
            )
        }
        else if (targetExpression is IdentifierExpression) {
            return BoundInvocationExpression(
                    context,
                    this,
                    null,
                    targetExpression.identifier,
                    boundParameterValueExprs
            )
        }
        else throw InternalCompilerError("What the heck is going on?? The parser should never have allowed this!")
    }
}