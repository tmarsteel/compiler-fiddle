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

package compiler.reportings

import compiler.lexer.SourceContentAwareSourceDescriptor
import kotlin.math.min

fun SourceContentAwareSourceDescriptor.getIllustrationForHighlightedLines(desiredLineNumbers: Collection<Int>): String {
    val lineContext = 0 // +- X lines of context

    if (desiredLineNumbers.isEmpty()) {
        throw IllegalArgumentException("No source lines given.")
    }

    if (desiredLineNumbers.any { it < 1 || it > sourceLines.size }) {
        throw IllegalArgumentException("Source lines out of range.")
    }

    val desiredLineNumbersSorted = desiredLineNumbers.sorted()

    val lineNumbersToOutput = mutableSetOf<Int>()
    for (desiredLine in desiredLineNumbersSorted) {
        lineNumbersToOutput.add(desiredLine)
        for (i in 1 .. lineContext) {
            lineNumbersToOutput.add(desiredLine - i)
            lineNumbersToOutput.add(desiredLine + i)
        }
    }

    val lineNumbersToOutputSorted = lineNumbersToOutput.sorted()
    val lineCounterLength = min(3, lineNumbersToOutputSorted.max()!!.toString(10).length)

    val linesToOutputWithNormalizedTabs: Map<Int, String> = lineNumbersToOutputSorted.map {
        it to sourceLines[it - 1].replace("\t", "    ")
    }.toMap()

    val commonNumberOfLeadingSpaces = linesToOutputWithNormalizedTabs.values.map { it.takeWhile { it == ' ' }.length }.min()!!

    val out = StringBuilder()
    out.append(" ".repeat(lineCounterLength + 1))
    out.append("|\n")
    val lineSkippingIndicatorLine = "...".padStart(lineCounterLength, ' ') + "\n"

    for (index in 0 .. lineNumbersToOutputSorted.lastIndex) {
        val lineNumber = lineNumbersToOutputSorted[index]
        out.append(lineNumber.toString(10).padStart(lineCounterLength, ' '))
        out.append(" |  ")
        out.append(linesToOutputWithNormalizedTabs[lineNumber]!!.substring(commonNumberOfLeadingSpaces))
        out.append("\n")

        if (index != lineNumbersToOutputSorted.lastIndex) {
            val nextLineNumber = lineNumbersToOutputSorted[index + 1]
            if (lineNumber + 1 != nextLineNumber) {
                // we are skipping some lines
                out.append(lineSkippingIndicatorLine)
            }
        }
    }

    return out.toString()
}