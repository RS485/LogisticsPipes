/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.markdown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MarkdownParserTest {

    @Test
    fun `default text case in splitToInlineElements`() {
        val str = "Split this please"
        val splitElements = MarkdownParser.splitToInlineElements(str)

        assertEquals(listOf(Text("Split"), Text("this"), Text("please")), splitElements)
    }

    @Test
    fun `empty string in splitToInlineElements`() {
        val str = ""
        val splitElements = MarkdownParser.splitToInlineElements(str)

        assertTrue(splitElements.isEmpty())
    }

    @Test
    fun `too many spaces in splitToInlineElements`() {
        val str = "  two text    nodes   "
        val splitElements = MarkdownParser.splitToInlineElements(str)

        assertEquals(listOf(Text("two"), Text("text"), Text("nodes")), splitElements)
    }

    @Test
    fun `single word in splitToInlineElements`() {
        val str = "word"
        val splitElements = MarkdownParser.splitToInlineElements(str)

        assertEquals(listOf(Text("word")), splitElements)
    }

    @Test
    fun `parse simple text in parseParagraphs`() {
        val str = "Just some text"
        val paragraphs = MarkdownParser.parseParagraphs(str)

        assertEquals(listOf(RegularParagraph(MarkdownParser.splitToInlineElements(str))), paragraphs)
    }

    @Test
    fun `parse simple header in parseParagraphs`() {
        val headerStr = "I am header!"
        val paragraphs = MarkdownParser.parseParagraphs("# $headerStr")

        assertEquals(listOf(HeaderParagraph(MarkdownParser.splitToInlineElements(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse text paragraph before header in parseParagraphs`() {
        val textStr = "Before!"
        val headerStr = "Header!"
        val paragraphs = MarkdownParser.parseParagraphs(
                "$textStr\n" +
                        "\n" +
                        "# $headerStr")

        val expectedParagraphs = listOf(
                RegularParagraph(MarkdownParser.splitToInlineElements(textStr)),
                HeaderParagraph(MarkdownParser.splitToInlineElements(headerStr), 1)
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse text paragraph after header in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "After!"
        val paragraphs = MarkdownParser.parseParagraphs(
                "# $headerStr\n" +
                        "\n" +
                        textStr)

        val expectedParagraphs = listOf(
                HeaderParagraph(MarkdownParser.splitToInlineElements(headerStr), 1),
                RegularParagraph(MarkdownParser.splitToInlineElements(textStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }
}