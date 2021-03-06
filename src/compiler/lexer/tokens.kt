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

package compiler.lexer

import compiler.parser.grammar.ParameterList
import compiler.parser.grammar.VariableDeclaration

enum class TokenType
{
    KEYWORD,
    IDENTIFIER,
    NUMERIC_LITERAL,
    OPERATOR
}

enum class Keyword(val text: String)
{
    MODULE("module"),
    IMPORT("import"),

    FUNCTION("fun"),
    VAL("val"),
    VAR("var"),
    MUTABLE("mutable"),
    READONLY("readonly"),
    IMMUTABLE("immutable"),
    NOTHROW("nothrow"),
    PURE("pure"),
    OPERATOR("operator"),
    EXTERNAL("external"),
    IF("if"),
    ELSE("else"),

    RETURN("return"),

    STRUCT_DEFINITION("struct"),

    PRIVATE("private"),
    PROTECTED("protected"),
    INTERNAL("internal"),
    EXPORT("export")
}

enum class Operator(val text: String, private val _humanReadableName: String? = null)
{
    // ORDER IS VERY IMPORTANT
    // e.g. if + was before +=, += would never get recognized as such (but rather as separate + and =)

    PARANT_OPEN  ("(", "left parenthesis"),
    PARANT_CLOSE (")", "right parenthesis"),
    CBRACE_OPEN  ("{", "left curly brace"),
    CBRACE_CLOSE ("}", "right curly brace"),
    DOT          (".", "dot"),
    SAFEDOT      ("?."),
    TIMES        ("*"),
    COMMA        (",", "comma"),
    SEMICOLON    (";"),
    COLON        (":", "colon"),
    NEWLINE      ("\n", "newline"),
    RETURNS      ("->"),
    PLUS         ("+"),
    MINUS        ("-"),
    DIVIDE       ("/"),
    IDENTITY_EQ  ("==="),
    IDENTITY_NEQ ("!=="),
    EQUALS       ("=="),
    NOT_EQUALS   ("!="),
    ASSIGNMENT   ("="),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN_OR_EQUALS("<="),
    GREATER_THAN (">"),
    LESS_THAN    ("<"),
    TRYCAST      ("as?", "safe cast"),
    CAST         ("as", "cast"),
    ELVIS        ("?:", "elvis operator"),
    QUESTION_MARK("?"),
    NOTNULL      ("!!"), // find a better name for this...
    NEGATE       ("!", "negation");

    /** A human readable name of this operator; if none was specified, falls back to `"operator $text"` */
    val humanReadableName: String
        get() = this._humanReadableName ?: "operator $text"
}

val DECIMAL_SEPARATOR: Char = '.'

abstract class Token
{
    abstract val type: TokenType
    abstract val sourceLocation: SourceLocation

    override fun toString(): String {
        if (sourceLocation === SourceLocation.UNKNOWN)
            return toStringWithoutLocation()
        else
            return toStringWithoutLocation() + " in " + sourceLocation.fileLineColumnText
    }

    open fun toStringWithoutLocation(): String = type.name
}

class KeywordToken(
        val keyword: Keyword,
        /** The actual CharSequence as it appears in the source code */
        val sourceText: String = keyword.text,
        override val sourceLocation: SourceLocation = SourceLocation.UNKNOWN
): Token()
{
    override val type = TokenType.KEYWORD

    override fun toStringWithoutLocation() = type.name + " " + keyword.name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as KeywordToken

        if (keyword != other.keyword) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyword.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

class OperatorToken(
        val operator: Operator,
        override val sourceLocation: SourceLocation = SourceLocation.UNKNOWN
) : Token() {
    override val type = TokenType.OPERATOR

    override fun toStringWithoutLocation(): String {
        return if (operator == Operator.NEWLINE) {
            "newline"
        } else {
            operator.humanReadableName
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as OperatorToken

        if (operator != other.operator) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

class IdentifierToken(
        val value: String,
        override val sourceLocation: SourceLocation = SourceLocation.UNKNOWN
) : Token() {
    override val type = TokenType.IDENTIFIER

    override fun toStringWithoutLocation() = type.name + " " + value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as IdentifierToken

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

class NumericLiteralToken(
        override val sourceLocation: SourceLocation,
        val stringContent: String
): Token() {
    override val type = TokenType.NUMERIC_LITERAL
}

/**
 * **FOR TESTING ONLY**
 *
 * To be used as a placeholder for nested rules. E.g. the rule [ParameterList] uses [GrammarReceiver.ref] with
 * [VariableDeclaration] as the parameter. An instance of this class can be used instead of actual tokens resembling
 * the variable declaration. The given [replacement] will be returned as if the nested rule had parsed it.
 */
class NestedRuleMockingToken(val replacement: Any, override val sourceLocation: SourceLocation) : Token() {
    override val type: TokenType = TokenType.OPERATOR
}