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

import network.rs485.markdown.MarkdownParser.parseParagraphs
import network.rs485.markdown.MarkdownParser.splitToInlineElements
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MarkdownParserTest {

    @Test
    fun `default text case in splitToInlineElements`() {
        val str = "Split this please"
        val splitElements = splitToInlineElements(str)

        assertEquals(listOf(Word("Split"), Word("this"), Word("please")), splitElements)
    }

    @Test
    fun `empty string in splitToInlineElements`() {
        val str = ""
        val splitElements = splitToInlineElements(str)

        assertEquals(emptyList(), splitElements)
    }

    @Test
    fun `too many spaces in splitToInlineElements`() {
        val str = "  two text    nodes   "
        val splitElements = splitToInlineElements(str)

        assertEquals(listOf(Word("two"), Word("text"), Word("nodes")), splitElements)
    }

    @Test
    fun `single word in splitToInlineElements`() {
        val str = "word"
        val splitElements = splitToInlineElements(str)

        assertEquals(listOf(Word(str)), splitElements)
    }

    @Test
    fun `special line break in splitToInlineElements`() {
        val firstStr = "one"
        val secondStr = "paragraph"
        val splitElements = splitToInlineElements("$firstStr\n$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `special line break after word in splitToInlineElements`() {
        val str = "word"
        val splitElements = splitToInlineElements("$str\n")

        assertEquals(listOf(Word(str), Break), splitElements)
    }

    @Test
    fun `special line break before word in splitToInlineElements`() {
        val str = "weird"
        val splitElements = splitToInlineElements("\n$str")

        assertEquals(listOf(Break, Word(str)), splitElements)
    }

    @Test
    fun `special line break and spaces in splitToInlineElements`() {
        val str = "weird"
        val splitElements = splitToInlineElements("  \n $str")

        assertEquals(listOf(Break, Word(str)), splitElements)
    }

    @Test
    fun `html line break in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitToInlineElements("$firstStr<br>$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitToInlineElements("$firstStr<br/>$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break with one space in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitToInlineElements("$firstStr<br />$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break with multiple space in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitToInlineElements("$firstStr<br   />$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `parse simple text in parseParagraphs`() {
        val str = "Just some text"
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(RegularParagraph(splitToInlineElements(str))), paragraphs)
    }

    @Test
    fun `parse empty text in parseParagraphs`() {
        val paragraphs = parseParagraphs("")

        assertEquals(emptyList(), paragraphs)
    }

    @Test
    fun `parse two regular paragraphs with text in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr\n\n$secondStr")

        assertEquals(listOf(RegularParagraph(splitToInlineElements(firstStr)), RegularParagraph(splitToInlineElements(secondStr))), paragraphs)
    }

    @Test
    fun `parse html line break in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr<br>$secondStr")

        assertEquals(listOf(RegularParagraph(listOf(Word(firstStr), Break, Word(secondStr)))), paragraphs)
    }

    @Test
    fun `parse xhtml line break in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr<br />$secondStr")

        assertEquals(listOf(RegularParagraph(listOf(Word(firstStr), Break, Word(secondStr)))), paragraphs)
    }

    @Test
    fun `special line break with two spaces in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr  \n$secondStr")

        assertEquals(listOf(RegularParagraph(listOf(Word(firstStr), Break, Word(secondStr)))), paragraphs)
    }

    @Test
    fun `special line break with too many spaces in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr     \n $secondStr")

        assertEquals(listOf(RegularParagraph(listOf(Word(firstStr), Break, Word(secondStr)))), paragraphs)
    }

    @Test
    fun `line break with one space in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr \n$secondStr")

        assertEquals(listOf(RegularParagraph(splitToInlineElements("$firstStr $secondStr"))), paragraphs)
    }

    @Test
    fun `escaped line break in parseParagraphs`() {
        val firstStr = "Split"
        val secondStr = "text"
        val paragraphs = parseParagraphs("$firstStr\\\n$secondStr")

        assertEquals(listOf(RegularParagraph(listOf(Word("$firstStr$secondStr")))), paragraphs)
    }

    @Test
    fun `escaped line break and no content in parseParagraphs`() {
        val paragraphs = parseParagraphs("\\\n")

        assertEquals(emptyList(), paragraphs)
    }

    @Test
    fun `parse simple header in parseParagraphs`() {
        val headerStr = "I am header!"
        val paragraphs = parseParagraphs("# $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse non-header in parseParagraphs`() {
        val noHeaderStr = "#I ain't no header!"
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitToInlineElements(noHeaderStr))), paragraphs)
    }

    @Test
    fun `parse header with spaces in parseParagraphs`() {
        val headerStr = "I am header!"
        val paragraphs = parseParagraphs("  #  $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse level two header in parseParagraphs`() {
        val headerStr = "I am header lvl 2!"
        val paragraphs = parseParagraphs("## $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 2)), paragraphs)
    }

    @Test
    fun `parse level six header in parseParagraphs`() {
        val headerStr = "I am header lvl 6!"
        val paragraphs = parseParagraphs("###### $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 6)), paragraphs)
    }

    @Test
    fun `parse level seven header in parseParagraphs`() {
        val headerStr = "I am also header lvl 6!"
        val paragraphs = parseParagraphs("####### $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 6)), paragraphs)
    }

    @Test
    fun `parse uber level header in parseParagraphs`() {
        val headerStr = "I am also header lvl 6!"
        val paragraphs = parseParagraphs("############## $headerStr")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 6)), paragraphs)
    }

    @Test
    fun `escaped line break for simple header in parseParagraphs`() {
        val firstStr = "Multiline"
        val secondStr = "Header"
        val paragraphs = parseParagraphs("# $firstStr \\\n$secondStr")

        assertEquals(listOf(HeaderParagraph(listOf(Word(firstStr), Word(secondStr)), 1)), paragraphs)
    }

    @Test
    fun `parse text paragraph before header in parseParagraphs`() {
        val textStr = "Before!"
        val headerStr = "Header!"
        val paragraphs = parseParagraphs(
                "$textStr\n" +
                        "\n" +
                        "# $headerStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(textStr)),
                HeaderParagraph(splitToInlineElements(headerStr), 1)
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse text paragraph after header in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "After!"
        val paragraphs = parseParagraphs(
                "# $headerStr\n" +
                        "\n" +
                        textStr)

        val expectedParagraphs = listOf(
                HeaderParagraph(splitToInlineElements(headerStr), 1),
                RegularParagraph(splitToInlineElements(textStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse text paragraph before and after header in parseParagraphs`() {
        val firstStr = "Before!"
        val headerStr = "Header!"
        val secondStr = "After!"
        val paragraphs = parseParagraphs(
                "$firstStr\n" +
                        "\n" +
                        "# $headerStr\n" +
                        "\n" +
                        secondStr)

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HeaderParagraph(splitToInlineElements(headerStr), 1),
                RegularParagraph(splitToInlineElements(secondStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse text without extra newlines before and after header in parseParagraphs`() {
        val firstStr = "Before!"
        val headerStr = "Header!"
        val secondStr = "After!"
        val paragraphs = parseParagraphs("$firstStr\n ## $headerStr\n $secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HeaderParagraph(splitToInlineElements(headerStr), 2),
                RegularParagraph(splitToInlineElements(secondStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse first level underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n===")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse first level uber underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n=============================")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 1)), paragraphs)
    }

    @Test
    fun `dont parse first level underscored header in parseParagraphs`() {
        val str = "Not a header"
        val noHeaderStr = "$str\n=="
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitToInlineElements("$str =="))), paragraphs)
    }

    @Test
    fun `parse first level underscored header and then text in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "Hello.."
        val paragraphs = parseParagraphs("$headerStr\n=======\n$textStr")

        val expectedParagraphs = listOf(
                HeaderParagraph(splitToInlineElements(headerStr), 1),
                RegularParagraph(splitToInlineElements(textStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse second level underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n---")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 2)), paragraphs)
    }

    @Test
    fun `parse second level uber underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n----------------------------------")

        assertEquals(listOf(HeaderParagraph(splitToInlineElements(headerStr), 2)), paragraphs)
    }

    @Test
    fun `dont parse second level underscored header in parseParagraphs`() {
        val str = "Not a header"
        val noHeaderStr = "$str\n--"
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitToInlineElements("$str --"))), paragraphs)
    }

    @Test
    fun `parse second level underscored header and then text in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "Hello.."
        val paragraphs = parseParagraphs("$headerStr\n-------\n$textStr")

        val expectedParagraphs = listOf(
                HeaderParagraph(splitToInlineElements(headerStr), 2),
                RegularParagraph(splitToInlineElements(textStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse double horizontal line in parseParagraphs`() {
        val str = "==="
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(HorizontalLineParagraph), paragraphs)
    }

    @Test
    fun `dont parse double horizontal line in parseParagraphs`() {
        val str = "=="
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(RegularParagraph(listOf(Word(str)))), paragraphs)
    }

    @Test
    fun `parse single horizontal line in parseParagraphs`() {
        val str = "---"
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(HorizontalLineParagraph), paragraphs)
    }

    @Test
    fun `dont parse single horizontal line in parseParagraphs`() {
        val str = "--"
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(RegularParagraph(listOf(Word(str)))), paragraphs)
    }

    @Test
    fun `parse double horizontal line with text around in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n===\n\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitToInlineElements(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse double horizontal line with text around and less newlines in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n===\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitToInlineElements(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse single horizontal line with text around in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n---\n\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitToInlineElements(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse single horizontal line with text around and less newlines in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n---\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitToInlineElements(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `dont parse single horizontal line with text around in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n--\n\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitToInlineElements(firstStr)),
                RegularParagraph(listOf(Word("--"))),
                RegularParagraph(splitToInlineElements(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }
}
