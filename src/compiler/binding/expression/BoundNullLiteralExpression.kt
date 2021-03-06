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
import compiler.ast.expression.NullLiteralExpression
import compiler.binding.BoundExecutable
import compiler.binding.context.CTContext
import compiler.binding.type.BaseTypeReference
import compiler.lexer.SourceLocation

class BoundNullLiteralExpression(
    override val context: CTContext,
    override val declaration: NullLiteralExpression
) : BoundExpression<NullLiteralExpression>
{
    override val type: BaseTypeReference? = null

    override val isGuaranteedToThrow = null

    override fun findReadsBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> = emptySet()

    override fun findWritesBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> = emptySet()

    companion object {
        fun getInstance(context: CTContext, sourceLocation: SourceLocation = SourceLocation.UNKNOWN) =
            BoundNullLiteralExpression(
                context,
                NullLiteralExpression(sourceLocation)
            )
    }
}
