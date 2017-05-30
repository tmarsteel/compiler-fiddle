package compiler.binding.expression

import compiler.ast.expression.NumericLiteralExpression
import compiler.binding.context.CTContext
import compiler.binding.type.BaseTypeReference
import compiler.parser.Reporting
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Should only be used when the numeric literal cannot be parsed correctly. Otherwise, use
 * [BoundIntegerLiteral] and [BoundFloatingPointLiteral].
 */
open class BoundNumericLiteral(
    override val context: CTContext,
    override val declaration: NumericLiteralExpression,
    private val reportings: Collection<Reporting>
) : BoundExpression<NumericLiteralExpression> {
    override fun semanticAnalysisPhase1() = reportings
    override val type: BaseTypeReference? = null // unknown
}

class BoundIntegerLiteral(
    context: CTContext,
    declaration: NumericLiteralExpression,
    val integer: BigInteger,
    reportings: Collection<Reporting>
) : BoundNumericLiteral(context, declaration, reportings) {
    override val type = compiler.binding.type.Int.baseReference(context)
}

class BoundFloatingPointLiteral(
    context: CTContext,
    declaration: NumericLiteralExpression,
    val floatingPointNumber: BigDecimal,
    reportings: Collection<Reporting>
) : BoundNumericLiteral(context, declaration, reportings) {
    override val type = compiler.binding.type.Float.baseReference(context)
}