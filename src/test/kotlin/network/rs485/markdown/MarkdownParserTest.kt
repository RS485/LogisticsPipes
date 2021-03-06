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
import network.rs485.markdown.MarkdownParser.splitAndFormatWords
import network.rs485.markdown.MarkdownParser.splitSpacesAndWords
import network.rs485.markdown.MarkdownParser.splitWhitespaceCharactersAndWords
import java.util.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MarkdownParserTest {

    @Test
    fun `default text case in splitToInlineElements`() {
        val str = "Split this please"
        val splitElements = splitAndFormatWords(str)

        assertEquals(listOf(Word("Split"), Space, Word("this"), Space, Word("please")), splitElements)
    }

    @Test
    fun `empty string in splitToInlineElements`() {
        val str = ""
        val splitElements = splitWhitespaceCharactersAndWords(str)

        assertEquals(emptyList(), splitElements)
    }

    @Test
    fun `too many spaces in splitToInlineElements`() {
        val str = "  two text    nodes   "
        val splitElements = splitAndFormatWords(str)

        assertEquals(listOf(Word("two"), Space, Word("text"), Space, Word("nodes")), splitElements)
    }

    @Test
    fun `single word in splitToInlineElements`() {
        val str = "word"
        val splitElements = splitAndFormatWords(str)

        assertEquals(listOf(Word(str)), splitElements)
    }

    @Test
    fun `special line break in splitToInlineElements`() {
        val firstStr = "one"
        val secondStr = "paragraph"
        val splitElements = splitWhitespaceCharactersAndWords("$firstStr\n$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `special line break after word in splitToInlineElements`() {
        val str = "word"
        val splitElements = splitWhitespaceCharactersAndWords("$str\n")

        assertEquals(listOf(Word(str), Break), splitElements)
    }

    @Test
    fun `special line break before word in splitToInlineElements`() {
        val str = "weird"
        val splitElements = splitWhitespaceCharactersAndWords("\n$str")

        assertEquals(listOf(Break, Word(str)), splitElements)
    }

    @Test
    fun `special line break and spaces in splitToInlineElements`() {
        val str = "weird"
        val splitElements = splitWhitespaceCharactersAndWords("  \n $str")

        assertEquals(listOf(Break, Word(str)), splitElements)
    }

    @Test
    fun `html line break in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitWhitespaceCharactersAndWords("$firstStr<br>$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitWhitespaceCharactersAndWords("$firstStr<br/>$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break with one space in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitWhitespaceCharactersAndWords("$firstStr<br />$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `xhtml line break with multiple space in splitToInlineElements`() {
        val firstStr = "Split"
        val secondStr = "text"
        val splitElements = splitWhitespaceCharactersAndWords("$firstStr<br   />$secondStr")

        assertEquals(listOf(Word(firstStr), Break, Word(secondStr)), splitElements)
    }

    @Test
    fun `parse simple text in parseParagraphs`() {
        val str = "Just some text"
        val paragraphs = parseParagraphs(str)

        assertEquals(listOf(RegularParagraph(splitAndFormatWords(str))), paragraphs)
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

        assertEquals(listOf(RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)), RegularParagraph(splitWhitespaceCharactersAndWords(secondStr))), paragraphs)
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

        assertEquals(listOf(RegularParagraph(splitWhitespaceCharactersAndWords("$firstStr $secondStr"))), paragraphs)
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

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse non-header in parseParagraphs`() {
        val noHeaderStr = "#I ain't no header!"
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitWhitespaceCharactersAndWords(noHeaderStr))), paragraphs)
    }

    @Test
    fun `parse header with spaces in parseParagraphs`() {
        val headerStr = "I am header!"
        val paragraphs = parseParagraphs("  #  $headerStr")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse level two header in parseParagraphs`() {
        val headerStr = "I am header lvl 2!"
        val paragraphs = parseParagraphs("## $headerStr")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 2)), paragraphs)
    }

    @Test
    fun `parse level six header in parseParagraphs`() {
        val headerStr = "I am header lvl 6!"
        val paragraphs = parseParagraphs("###### $headerStr")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 6)), paragraphs)
    }

    @Test
    fun `parse level seven header in parseParagraphs`() {
        val headerStr = "I am also header lvl 6!"
        val paragraphs = parseParagraphs("####### $headerStr")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 6)), paragraphs)
    }

    @Test
    fun `parse uber level header in parseParagraphs`() {
        val headerStr = "I am also header lvl 6!"
        val paragraphs = parseParagraphs("############## $headerStr")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 6)), paragraphs)
    }

    @Test
    fun `escaped line break for simple header in parseParagraphs`() {
        val firstStr = "Multiline"
        val secondStr = "Header"
        val paragraphs = parseParagraphs("# $firstStr \\\n$secondStr")

        assertEquals(listOf(HeaderParagraph(listOf(Word(firstStr), Space, Word(secondStr)), 1)), paragraphs)
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
                RegularParagraph(splitWhitespaceCharactersAndWords(textStr)),
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1)
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
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1),
                RegularParagraph(splitWhitespaceCharactersAndWords(textStr))
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
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1),
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr))
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
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 2),
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse first level underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n===")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1)), paragraphs)
    }

    @Test
    fun `parse first level uber underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n=============================")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1)), paragraphs)
    }

    @Test
    fun `dont parse first level underscored header in parseParagraphs`() {
        val str = "Not a header"
        val noHeaderStr = "$str\n=="
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitWhitespaceCharactersAndWords("$str =="))), paragraphs)
    }

    @Test
    fun `parse first level underscored header and then text in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "Hello.."
        val paragraphs = parseParagraphs("$headerStr\n=======\n$textStr")

        val expectedParagraphs = listOf(
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 1),
                RegularParagraph(splitWhitespaceCharactersAndWords(textStr))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse second level underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n---")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 2)), paragraphs)
    }

    @Test
    fun `parse second level uber underscored header in parseParagraphs`() {
        val headerStr = "Header!"
        val paragraphs = parseParagraphs("$headerStr\n----------------------------------")

        assertEquals(listOf(HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 2)), paragraphs)
    }

    @Test
    fun `dont parse second level underscored header in parseParagraphs`() {
        val str = "Not a header"
        val noHeaderStr = "$str\n--"
        val paragraphs = parseParagraphs(noHeaderStr)

        assertEquals(listOf(RegularParagraph(splitWhitespaceCharactersAndWords("$str --"))), paragraphs)
    }

    @Test
    fun `parse second level underscored header and then text in parseParagraphs`() {
        val headerStr = "Header!"
        val textStr = "Hello.."
        val paragraphs = parseParagraphs("$headerStr\n-------\n$textStr")

        val expectedParagraphs = listOf(
                HeaderParagraph(splitWhitespaceCharactersAndWords(headerStr), 2),
                RegularParagraph(splitWhitespaceCharactersAndWords(textStr))
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
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse double horizontal line with text around and less newlines in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n===\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse single horizontal line with text around in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n---\n\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse single horizontal line with text around and less newlines in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n---\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                HorizontalLineParagraph,
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `dont parse single horizontal line with text around in parseParagraphs`() {
        val firstStr = "Hello"
        val secondStr = "World!"
        val paragraphs = parseParagraphs("$firstStr\n\n--\n\n$secondStr")

        val expectedParagraphs = listOf(
                RegularParagraph(splitWhitespaceCharactersAndWords(firstStr)),
                RegularParagraph(listOf(Word("--"))),
                RegularParagraph(splitWhitespaceCharactersAndWords(secondStr)))
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse a simple tile menu tag`() {
        val text = "Main Menu"
        val link = "main_menu"
        val paragraphs = parseParagraphs("[$text](menu://$link?type=tile)")

        val expectedParagraphs = listOf(
            MenuParagraph(text, link, MenuParagraphType.TILE)
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse a simple list menu tag`() {
        val text = "Main Menu"
        val link = "main_menu"
        val paragraphs = parseParagraphs("[$text](menu://$link?type=list)")

        val expectedParagraphs = listOf(
            MenuParagraph(text, link, MenuParagraphType.LIST)
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse a list menu tag as default`() {
        val text = "Main Menu"
        val link = "main_menu"
        val paragraphs = parseParagraphs("[$text](menu://$link)")

        val expectedParagraphs = listOf(
            MenuParagraph(text, link, MenuParagraphType.LIST)
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse an incomplete menu tag`() {
        val str = "[Menu(menu://main_menu)"
        val paragraphs = parseParagraphs(str)

        val expectedParagraphs = listOf(
                RegularParagraph(listOf(Word(str)))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse an invalid combination of menu tags`() {
        val test1 = "[Test1](menu://test_menu_1)"
        val test2 = "[Test2](menu://test_menu_2)"
        val paragraphs = parseParagraphs("$test1 $test2")

        val expectedParagraphs = listOf(
                RegularParagraph(splitAndFormatWords("$test1 $test2"))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse a couple of menu tags`() {
        val expectedParagraphs = (1..5).map { n -> MenuParagraph("Test $n", "test_menu_$n", MenuParagraphType.LIST) }.toList()
        val str = expectedParagraphs.joinToString(separator = "\n") { "[${it.description}](menu://${it.link})" }

        val paragraphs = parseParagraphs(str)

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse menu tag with blank appendix`() {
        val menuText = "Menu"
        val menuReference = "theMenu"
        val word = "Well"
        val str = "[$menuText](menu://$menuReference?type=tile)  \n$word"
        val paragraphs = parseParagraphs(str)

        val expectedParagraphs = listOf(
            MenuParagraph(menuText, menuReference, MenuParagraphType.TILE),
            RegularParagraph(listOf(Word(word)))
        )
        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse a correct ImageParagraph`() {
        val alt = "Alternative Text"
        val link = "logisticspipes:itemcard"
        val str = "![$alt](image://$link)"
        val paragraphs = parseParagraphs(str)

        val expectedParagraphs = listOf(
            ImageParagraph(alternative = alt, imagePath = link)
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse an incorrect ImageParagraph`() {
        val str = "[Alternative Text](image://logisticspipes:itemcard)"
        val paragraphs = parseParagraphs(str)

        val expectedParagraphs = listOf(
            RegularParagraph(splitWhitespaceCharactersAndWords(str))
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `try to parse an incorrect inline image link`() {
        val str = "this is not ![Alternative Text](image://logisticspipes:itemcard) working"
        val paragraphs = parseParagraphs(str)

        val expectedParagraphs = listOf(
            RegularParagraph(splitWhitespaceCharactersAndWords(str))
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse an inline page link`() {
        val linkText = "page link"
        val pageLink = "yay i am at work"
        val str1 = "this is a"
        val str2 = "at work"
        val paragraphs = parseParagraphs("$str1 [$linkText](page://$pageLink) $str2")
        val linkObject = PageLink(pageLink)

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    splitWhitespaceCharactersAndWords(str1),
                    listOf(Space, LinkFormatting(linkObject), Word("page"), Space, Word("link"), LinkFormatting(null), Space),
                    splitWhitespaceCharactersAndWords(str2),
                ).flatten()
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse two page links`() {
        val link1Text = "page link 1"
        val link2Text = "page link 2"
        val pageLink = "yay i am at work"
        val str1 = "this is a"
        val str2 = "at work"
        val paragraphs = parseParagraphs("$str1 [$link1Text](page://$pageLink) [$link2Text](page://$pageLink) $str2")
        val linkObject = PageLink(pageLink)

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    splitWhitespaceCharactersAndWords(str1),
                    listOf(Space),
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("1"), LinkFormatting(null)),
                    listOf(Space),
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("2"), LinkFormatting(null)),
                    listOf(Space),
                    splitWhitespaceCharactersAndWords(str2),
                ).flatten()
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse two page links (no spacing)`() {
        val link1Text = "page link 1"
        val link2Text = "page link 2"
        val pageLink = "yay i am at work"
        val paragraphs = parseParagraphs("[$link1Text](page://$pageLink)[$link2Text](page://$pageLink)")
        val linkObject = PageLink(pageLink)

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("1"), LinkFormatting(null)),
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("2"), LinkFormatting(null)),
                ).flatten()
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse two page links (double space)`() {
        val link1Text = "page link 1"
        val link2Text = "page link 2"
        val pageLink = "yay i am at work"
        val str1 = "this is a"
        val str2 = "at work"
        val paragraphs = parseParagraphs("$str1 [$link1Text](page://$pageLink)  [$link2Text](page://$pageLink) $str2")
        val linkObject = PageLink(pageLink)

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    splitWhitespaceCharactersAndWords(str1),
                    listOf(Space),
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("1"), LinkFormatting(null)),
                    listOf(Space),
                    listOf(LinkFormatting(linkObject), Word("page"), Space, Word("link"), Space, Word("2"), LinkFormatting(null)),
                    listOf(Space),
                    splitWhitespaceCharactersAndWords(str2),
                ).flatten()
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse https web link`() {
        val url = "https://example.com"
        val linkText = "My Link"
        val paragraphs = parseParagraphs("[$linkText]($url)")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(LinkFormatting(WebLink(url = url)))
                    .plus(splitSpacesAndWords(linkText))
                    .plus(LinkFormatting(null))
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse http web link`() {
        val url = "http://example.com"
        val linkText = "My Link"
        val paragraphs = parseParagraphs("[$linkText]($url)")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(LinkFormatting(WebLink(url = url)))
                    .plus(splitSpacesAndWords(linkText))
                    .plus(LinkFormatting(null))
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse web link in header`() {
        val url = "https://example.com"
        val linkText = "My Link"
        val paragraphs = parseParagraphs("# [$linkText]($url)")

        val expectedParagraphs = listOf(
            HeaderParagraph(
                listOf(LinkFormatting(WebLink(url = url)))
                    .plus(splitSpacesAndWords(linkText))
                    .plus(LinkFormatting(null)),
                headerLevel = 1,
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse single italic formatting`() {
        val str = "a word"
        val paragraphs = parseParagraphs("*$str*")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    listOf(TextFormatting(EnumSet.of(TextFormat.Italic))),
                    splitAndFormatWords(str),
                    listOf(TextFormatting(TextFormat.none)),
                ).flatten()
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse simple nested formatting`() {
        val paragraphs = parseParagraphs("*a___b___*")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("a"),
                    TextFormatting(EnumSet.of(TextFormat.Italic, TextFormat.Bold)),
                    Word("b"),
                    TextFormatting(TextFormat.none), // TODO: should be removed
                    TextFormatting(TextFormat.none),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse simple nested formatting 2`() {
        val paragraphs = parseParagraphs("**a_b_**")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    TextFormatting(EnumSet.of(TextFormat.Bold)),
                    Word("a"),
                    TextFormatting(EnumSet.of(TextFormat.Italic, TextFormat.Bold)),
                    Word("b"),
                    TextFormatting(EnumSet.of(TextFormat.Bold)), // TODO: should be removed
                    TextFormatting(TextFormat.none),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Ignore
    @Test
    fun `parse advanced nested formatting`() {
        val paragraphs = parseParagraphs("*a__b__c*")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("a"),
                    TextFormatting(EnumSet.of(TextFormat.Italic, TextFormat.Bold)),
                    Word("b"),
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("c"),
                    TextFormatting(TextFormat.none),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse simple formatted string`() {
        val paragraphs = parseParagraphs("This _is_ example **Markdown**. This ***should*** work.")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    Word("This"),
                    Space,
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("is"),
                    TextFormatting(TextFormat.none),
                    Space,
                    Word("example"),
                    Space,
                    TextFormatting(EnumSet.of(TextFormat.Bold)),
                    Word("Markdown"),
                    TextFormatting(TextFormat.none),
                    Word("."),
                    Space,
                    Word("This"),
                    Space,
                    TextFormatting(EnumSet.of(TextFormat.Bold, TextFormat.Italic)),
                    Word("should"),
                    TextFormatting(TextFormat.none),
                    Space,
                    Word("work."),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse formatted text next to each other`() {
        val paragraphs = parseParagraphs("*a* __b__")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("a"),
                    TextFormatting(TextFormat.none),
                    Space,
                    TextFormatting(EnumSet.of(TextFormat.Bold)),
                    Word("b"),
                    TextFormatting(TextFormat.none),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

    @Test
    fun `parse formatting with spaces to trim`() {
        val paragraphs = parseParagraphs(" * a * ")

        val expectedParagraphs = listOf(
            RegularParagraph(
                listOf(
                    TextFormatting(EnumSet.of(TextFormat.Italic)),
                    Word("a"),
                    TextFormatting(TextFormat.none),
                )
            )
        )

        assertEquals(expectedParagraphs, paragraphs)
    }

}
