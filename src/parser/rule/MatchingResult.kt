package parser.rule

import matching.AbstractMatchingResult
import matching.ResultCertainty
import parser.Reporting

class MatchingResult<ResultType>(
        override val certainty: ResultCertainty,
        override val result: ResultType?,
        override val errors: Set<Reporting>
) : AbstractMatchingResult<ResultType, Reporting>
{
    val isError: Boolean
        get() = (errors.max()?.level ?: Reporting.Level.values().min()!!) >= Reporting.Level.ERROR

    val isSuccess: Boolean
        get() = !isError && result != null
}