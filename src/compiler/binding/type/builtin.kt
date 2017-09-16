package compiler.binding.type

import compiler.ast.ASTModule
import compiler.ast.FunctionDeclaration
import compiler.ast.type.TypeModifier
import compiler.ast.type.TypeReference
import compiler.binding.context.Module
import compiler.binding.context.MutableCTContext
import compiler.parseFromClasspath

/*
 * This file contains raw definitions of the builtin types.
 * Actual operations on these types are defined in the source
 * language in the stdlib as external functions with receiver.
 * The actual implementations are defined within the stdlib
 * package.
 */

val Any = object : BuiltinType("Any") {}

val Unit = object : BuiltinType("Unit", Any) {}

val Number = object : BuiltinType("Number", Any) {
    override val impliedModifier = TypeModifier.IMMUTABLE
}

val Float = object : BuiltinType("Float", Number) {
    override val impliedModifier = TypeModifier.IMMUTABLE
}

val Int = object : BuiltinType("Int", Number) {
    override val impliedModifier = TypeModifier.IMMUTABLE
}

val BuiltinBoolean = object : BuiltinType("Boolean", Any) {
    override val impliedModifier = TypeModifier.IMMUTABLE
}


/**
 * A BuiltinType is defined in the ROOT package.
 */
abstract class BuiltinType(override val simpleName: String, vararg superTypes: BaseType) : BaseType {
    override final val fullyQualifiedName = "dotlin.lang.$simpleName"

    override final val superTypes: Set<BaseType> = superTypes.toSet()

    override final val reference = TypeReference(fullyQualifiedName, false, impliedModifier)

    /**
     * BaseTypes do not define anything themselves. All of that is defined in source language in the
     * stdlib and implementation is provided from elsewhere, probably platform-specific.
     */
    final override fun resolveMemberFunction(name: String) = emptySet<FunctionDeclaration>()

    companion object {
       private val stdlib: ASTModule = parseFromClasspath("builtin.dt")
        fun getNewModule(): Module {
            val module = Module(arrayOf("dotlin", "lang"), MutableCTContext())

            module.context.addBaseType(Any)
            module.context.addBaseType(Unit)
            module.context.addBaseType(Number)
            module.context.addBaseType(Float)
            module.context.addBaseType(Int)

            stdlib.functions.forEach { module.context.addFunction(it) }
            stdlib.variables.forEach { module.context.addVariable(it) }

            return module
        }
    }
}