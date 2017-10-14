package compiler.parser.grammar

import ModuleDeclaration
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import compiler.matching.ResultCertainty
import compiler.parser.rule.hasErrors
import matchers.isNotNull
import matchers.isNull

class ModuleDeclarationTest : GrammarTestCase() { init {
    "singleName" {
        val tokens = lex("module foobar\n")
        val result = ModuleDeclaration.tryMatch(tokens)

        assertThat(result.certainty, greaterThanOrEqualTo(ResultCertainty.MATCHED))
        assertThat(result.item, isNotNull)
        assertThat(result.reportings, isEmpty)
        val decl = result.item!!
        assertThat(decl.name.size, equalTo(1))
        assertThat(decl.name[0], equalTo("foobar"))
    }

    "twoNames" {
        val tokens = lex("module foo.bar\n")
        val result = ModuleDeclaration.tryMatch(tokens)

        assertThat(result.certainty, greaterThanOrEqualTo(ResultCertainty.MATCHED))
        assertThat(result.item, isNotNull)
        assertThat(result.reportings, isEmpty)
        val decl = result.item!!
        assertThat(decl.name.size, equalTo(2))
        assertThat(decl.name[0], equalTo("foo"))
        assertThat(decl.name[1], equalTo("bar"))
    }

    "threeNames" {
        val tokens = lex("module foo.bar.baz\n")
        val result = ModuleDeclaration.tryMatch(tokens)

        assertThat(result.certainty, greaterThanOrEqualTo(ResultCertainty.MATCHED))
        assertThat(result.item, isNotNull)
        assertThat(result.reportings, isEmpty)
        val decl = result.item!!
        assertThat(decl.name.size, equalTo(3))
        assertThat(decl.name[0], equalTo("foo"))
        assertThat(decl.name[1], equalTo("bar"))
        assertThat(decl.name[2], equalTo("baz"))
    }

    "missingName" {
        val tokens = lex("module \n")
        val result = ModuleDeclaration.tryMatch(tokens)

        assertThat(result.certainty, greaterThanOrEqualTo(ResultCertainty.MATCHED))
        assertThat(result.item, isNull)
        assertThat(result.hasErrors, equalTo(true))
        assertThat(result.reportings, hasSize(equalTo(1)))
        assertThat(result.reportings.first().message, contains(Regex("Unexpected .+?, expecting any identifier")))
    }

    "endsWithDot" {
        val tokens = lex("module foo.\n")
        val result = ModuleDeclaration.tryMatch(tokens)

        assertThat(result.certainty, greaterThanOrEqualTo(ResultCertainty.MATCHED))
        assertThat(result.item, isNull)
        assertThat(result.hasErrors, equalTo(true))
        assertThat(result.reportings, hasSize(equalTo(1)))

        assertThat(result.reportings.first().message, contains(Regex("Unexpected .+?, expecting any identifier")))
    }
}}