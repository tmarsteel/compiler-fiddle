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

package compiler.binding.struct

import compiler.ast.Executable
import compiler.ast.FunctionDeclaration
import compiler.ast.struct.StructDeclaration
import compiler.ast.type.TypeReference
import compiler.binding.BoundElement
import compiler.binding.BoundExecutable
import compiler.binding.context.CTContext
import compiler.binding.type.Any
import compiler.binding.type.BaseType
import compiler.reportings.Reporting
import kotlinext.duplicatesBy

class Struct(
    override val context: StructContext,
    override val declaration: StructDeclaration,
    val members: Set<StructMember>
) : BaseType, BoundElement<StructDeclaration> {
    override val simpleName: String = declaration.name.value

    override val reference: TypeReference = TypeReference(simpleName, false)

    override val superTypes: Set<BaseType> = setOf(Any)

    override fun resolveMemberFunction(name: String): Collection<FunctionDeclaration> = emptySet()

    override fun semanticAnalysisPhase1(): Collection<Reporting> {
        val reportings = mutableSetOf<Reporting>()

        members.forEach {
            reportings.addAll(it.semanticAnalysisPhase1())
        }

        // duplicate members
        members.duplicatesBy(StructMember::name).forEach { (name, dupMembers) ->
            reportings.add(Reporting.duplicateTypeMembers(this, dupMembers))
        }

        return reportings
    }

    override fun semanticAnalysisPhase2(): Collection<Reporting> {
        return members.flatMap { it.semanticAnalysisPhase2() }
    }

    override fun semanticAnalysisPhase3(): Collection<Reporting> {
        return members.flatMap { it.semanticAnalysisPhase3() }
    }

    override fun findReadsBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> {
        return members.flatMap { it.findReadsBeyond(context) }
    }

    override fun findWritesBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> {
        return members.flatMap { it.findWritesBeyond(context) }
    }
}