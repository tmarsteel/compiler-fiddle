/*
 * Copyright 2018 Tobias Marstaller
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package compiler.binding.expression

import compiler.ast.Executable
import compiler.ast.expression.NumericLiteralExpression
import compiler.binding.BoundExecutable
import compiler.binding.context.CTContext
import compiler.binding.type.BaseTypeReference
import compiler.reportings.Reporting
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

    override val isGuaranteedToThrow = false

    override fun findReadsBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> = emptySet()

    override fun findWritesBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> = emptySet()
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