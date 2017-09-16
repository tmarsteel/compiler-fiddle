package compiler.binding.expression

import compiler.ast.Executable
import compiler.ast.expression.IdentifierExpression
import compiler.binding.BoundExecutable
import compiler.binding.BoundVariable
import compiler.binding.context.CTContext
import compiler.binding.type.BaseType
import compiler.binding.type.BaseTypeReference
import compiler.binding.type.BuiltinBoolean
import compiler.parser.Reporting

class BoundIdentifierExpression(
    override val context: CTContext,
    override val declaration: IdentifierExpression
) : BoundExpression<IdentifierExpression> {
    val identifier: String = declaration.identifier.value

    override val type: BaseTypeReference?
        get() = when(referredType) {
            ReferredType.LITERAL  -> referredLiteral?.type
            ReferredType.VARIABLE -> referredVariable?.type
            ReferredType.TYPENAME -> referredBaseType?.baseReference?.invoke(context)
            null -> null
        }

    /** What this expression refers to; is null if not known */
    var referredType: ReferredType? = null
        private set

    /** The literal this identifier represents, if it does (see [referredType]); otherwise null */
    var referredLiteral: BoundExpression<*>? = null
        private set

    /** The variable this expression refers to, if it does (see [referredType]); otherwise null. */
    var referredVariable: BoundVariable? = null
        private set

    /** The base type this expression referes to, if it does (see [referredType]); otherwise null. */
    var referredBaseType: BaseType? = null
        private set

    override var isGuaranteedToThrow = false

    override fun semanticAnalysisPhase1(): Collection<Reporting> {
        val reportings = mutableSetOf<Reporting>()

        // attempt boolean literal
        if (identifier == "true" || identifier == "false") {
            referredType = ReferredType.LITERAL
            referredLiteral = BoundBooleanLiteral(declaration, context)
        } else {
            // attempt variable
            val variable = context.resolveVariable(identifier)

            if (variable != null) {
                referredType = ReferredType.VARIABLE
                referredVariable = variable
            } else {
                var type: BaseType? = context.resolveDefinedType(identifier)
                if (type == null) {
                    reportings.add(Reporting.undefinedIdentifier(declaration))
                } else {
                    this.referredBaseType = type
                    this.referredType = ReferredType.TYPENAME
                }
            }
        }

        return reportings
    }

    override fun semanticAnalysisPhase2(): Collection<Reporting> {
        val reportings = mutableSetOf<Reporting>()

        if (this.referredType == null) {
            // attempt a variable
            val variable = context.resolveVariable(identifier)
            if (variable != null) {
                referredVariable = variable
                referredType = ReferredType.VARIABLE
            }
            else {
                reportings.add(Reporting.error("Cannot resolve variable $identifier", declaration.sourceLocation))
            }
        }

        // TODO: attempt to resolve type; expression becomes of type "Type/Class", ... whatever, still to be defined

        return reportings
    }

    override fun findReadsBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> {
        if (referredType == ReferredType.VARIABLE) {
            if (context.containsWithinBoundary(referredVariable!!, boundary)) {
                return emptySet()
            }
            else {
                return setOf(this)
            }
        }
        else {
            // TODO is reading type information of types declared outside the boundary considered impure?
            return emptySet() // no violation
        }
    }

    override fun findWritesBeyond(boundary: CTContext): Collection<BoundExecutable<Executable<*>>> {
        // this does not write by itself; writs are done by other statements
        return emptySet()
    }

    /** The kinds of things an identifier can refer to. */
    enum class ReferredType {
        LITERAL,
        VARIABLE,
        TYPENAME
    }
}

class BoundBooleanLiteral(
    override val declaration: IdentifierExpression,
    override val context: CTContext
) : BoundExpression<IdentifierExpression> {
    override val type: BaseTypeReference = BuiltinBoolean.baseReference(context)

    override val isGuaranteedToThrow: Boolean = false
}
