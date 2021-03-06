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

package compiler.binding.type

import compiler.ast.FunctionDeclaration
import compiler.ast.type.TypeModifier
import compiler.ast.type.TypeReference
import compiler.binding.context.CTContext
import kotlinext.get

/**
 * Base type are classes, interfaces, enums, built-in type
 */
interface BaseType {
    val impliedModifier: TypeModifier?
        get() = null

    val simpleName: String
        get() = javaClass.simpleName

    // TODO: infer this from declaring module and simpleName
    val fullyQualifiedName: String
        get() = simpleName

    val reference: TypeReference
        get() = TypeReference(fullyQualifiedName, false, impliedModifier)

    val baseReference: (CTContext) -> BaseTypeReference
        get() = { ctx -> BaseTypeReference(reference, ctx, this) }

    val superTypes: Set<BaseType>
        get() = emptySet()

    /** @return Whether this type is the same as or a subtype of the given type. */
    infix fun isSubtypeOf(other: BaseType): Boolean {
        if (other === this) return true

        return superTypes.map { it.isSubtypeOf(other) }.fold(false, Boolean::or)
    }

    /**
     * Assumes this type is a the same as or a subtype of the given type (see [BaseType.isSubtypeOf] to
     * assure that).
     * Returns how many steps in hierarchy are between this type and the given type.
     *
     * For Example: `B : A`, `C : B`, `D : B`
     *
     * |~.hierarchicalDistanceTo(A)|return value|
     * |---------------------------|------------|
     * |A                          |0           |
     * |B                          |1           |
     * |C                          |2           |
     * |D                          |3           |
     *
     * @param carry Used by recursive invocations of this function. Is added to the hierarchical distance.
     * @return The hierarchical distance
     * @throws IllegalArgumentException If the given type is not a supertype of this type.
     */
    fun hierarchicalDistanceTo(superType: BaseType, carry: Int = 0): Int {
        if (this == superType) return carry

        if (this isSubtypeOf superType) {
            return this.superTypes
                .map { it.hierarchicalDistanceTo(superType, carry + 1) }
                .sorted()
                .first()
        }

        throw IllegalArgumentException("The given type is not a supertype of the receiving type.")
    }

    /** @return The member function overloads for the given name or an empty collection if no such member function is defined. */
    fun resolveMemberFunction(name: String): Collection<FunctionDeclaration> = emptySet()

    companion object {
        /**
         * Suppose
         *
         *     class A
         *     class AB : A
         *     class ABC : AB
         *     class C
         *
         * then these are the closest common ancestors:
         *
         * | Types       | Closes common ancestor |
         * | ----------- | ---------------------- |
         * | A, AB       | A                      |
         * | AB, ABC     | AB                     |
         * | A, ABC      | A                      |
         * | C, A        | Any                    |
         * | AB, C       | Any                    |
         *
         * @return The type to which all of the given types are assignable with the minimum
         *         [hierachical distance](hierarchicalDistanceTo).
         */
        fun closestCommonAncestorOf(types: List<BaseType>): BaseType {
            if (types.size == 0) throw IllegalArgumentException("At least one type must be provided")
            if (types.size == 1) return types[0]

            var pivot = types[0]
            for (_type in types[1..types.size - 1]) {
                var type = _type
                var swapped = false
                while (!(type isSubtypeOf pivot)) {
                    if (pivot.superTypes.isEmpty()) return Any
                    if (pivot.superTypes.size > 1) {
                        if (swapped) {
                            return Any
                        }
                        val temp = pivot
                        pivot = type
                        type = temp
                        swapped = true
                    }
                    else {
                        pivot = pivot.superTypes.iterator().next()
                    }
                }
            }

            return pivot
        }

        /**
         * @see [closestCommonAncestorOf]
         */
        fun closestCommonAncestorOf(vararg types: BaseType): BaseType {
            return closestCommonAncestorOf(types.asList())
        }
    }
}