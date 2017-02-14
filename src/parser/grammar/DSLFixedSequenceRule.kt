package parser.grammar

import indentByFromSecondLine
import matching.ResultCertainty
import parser.TokenSequence
import parser.rule.FixedSequenceRule
import parser.rule.MatchingResult
import parser.rule.MisconfigurationException
import parser.rule.Rule

class DSLFixedSequenceRule(
    override val subRules: MutableList<Rule<*>> = mutableListOf(),
    private val certaintySteps: MutableList<Pair<Int, ResultCertainty>> = mutableListOf(0 to ResultCertainty.OPTIMISTIC)
) : FixedSequenceRule(subRules, certaintySteps), DSLCollectionRule<List<MatchingResult<*>>>
{
    /**
     * Reading from this property: returns the level of certainty the rule has at the current point of configuration
     * Writing to this property: if the previous rule matches successfully, sets the certainty level of the result
     * to the given [ResultCertainty]
     */
    var __certainty: ResultCertainty
        get() = certaintySteps.last().second
        set(c)
        {
            val lastStep = certaintySteps.last()
            val currentIndex = subRules.lastIndex
            if (lastStep.first == currentIndex)
            {
                throw MisconfigurationException("Two certainty levels for the same index - insert rules in between")
            }

            if (c.level <= lastStep.second.level)
            {
                throw MisconfigurationException("Certainty steps have to increase; last was " + lastStep.second + ", new one is " + c)
            }

            certaintySteps.add(currentIndex to c)
        }

    /**
     * Sets certainty at this matching stage to [ResultCertainty.DEFINITIVE]
     */
    fun __definitive(): Unit
    {
        __certainty = ResultCertainty.DEFINITIVE
    }

    override fun tryMatch(input: TokenSequence): MatchingResult<List<MatchingResult<*>>> {
        throw UnsupportedOperationException("not implemented") // TODO: implement
    }
}
