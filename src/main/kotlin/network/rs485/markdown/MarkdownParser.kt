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

import java.lang.Integer.min

object MarkdownParser {
    private val linkRegex = Regex("!?\\[(.+?)]\\((.+?)\\)")
    private val webLinkRegex = Regex("^https?://", RegexOption.IGNORE_CASE)

    private class LinkMatcher(val match: MatchResult) {
        val text: String?
            get() = match.groups[1]?.value
        val link: String?
            get() = match.groups[2]?.value
        val linkWithoutProtocol: String?
            get() = link?.run { substring(indexOf("://") + 3) }
        val imageLinkFlag: Boolean
            get() = match.value.trimStart().startsWith('!')

        fun isMenuLink() = link?.startsWith("menu://", ignoreCase = true) ?: false
        fun isPageLink() = link?.startsWith("page://", ignoreCase = true) ?: false
        fun isWebLink() = link?.matches(webLinkRegex) ?: false
        fun isImageLink() = link?.startsWith("image://", ignoreCase = true) ?: false
    }

    private val htmlBreakRegex = Regex("<br(\\s*/)?>")

    internal fun splitWhitespaceCharactersAndWords(inputChars: CharSequence): List<InlineElement> {
        // newlines are already handled as special break characters at this point
        val chars = htmlBreakRegex.replace(inputChars, "\n")

        // breaks up all the words and returns them as inline elements without spaces
        return chars
            .split('\n')
            .map { words ->
                splitSpacesAndWords(words) + listOf(Break)
            }
            .flatten()
            .dropLast(1)
    }

    internal fun splitSpacesAndWords(inputChars: CharSequence, makeWord: (String) -> Word? = ::parseTextElement): List<InlineElement> {
        val mappedWords = inputChars
            .split(' ')
            .flatMap { word -> listOfNotNull(makeWord(word)) }
        return if (mappedWords.isEmpty()) {
            emptyList()
        } else {
            mappedWords
                .plus(Space) // appended Space to be removed as the last right element in the following zipWithNext
                .zipWithNext { left, right ->
                    // adds Spaces in between, makes sure Space always follows a Word
                    when {
                        left is Word && right is Word -> listOf(left, Space)
                        else -> listOf(left)
                    }
                }
                .flatten()
        }
    }

    private fun parseTextElement(word: String) = if (word.isNotBlank()) Word(word) else null

    private fun elements(inputChars: CharSequence): List<InlineElement> {
        // TODO: parse colors, formatting
        var lastCharLookedAt = 0
        val postfixGetter = { if (lastCharLookedAt < inputChars.length) inputChars.subSequence(lastCharLookedAt, inputChars.length) else "" }
        val isLastWhitespace = { lastCharLookedAt in 1 until inputChars.length && inputChars[lastCharLookedAt].isWhitespace() }
        return linkRegex.findAll(inputChars)
            .map { LinkMatcher(it) }
            .flatMap { link ->
                // checks link
                val before = inputChars.subSequence(lastCharLookedAt, link.match.range.first)
                listOf(
                    splitWhitespaceCharactersAndWords(before).let { if (!isLastWhitespace() && it.isEmpty()) emptyList() else it + listOf(Space) },
                    when {
                        link.isPageLink() -> PageLink(link.linkWithoutProtocol!!)
                        link.isWebLink() -> WebLink(link.link!!)
                        else -> null
                    }?.let { linkRef ->
                        listOf(
                            listOfNotNull(Word("!").takeIf { link.imageLinkFlag }),
                            splitSpacesAndWords(link.text!!) { word -> if (word.isNotBlank()) LinkWord(word, linkRef) else null }
                        ).flatten()
                    } ?: splitWhitespaceCharactersAndWords(link.match.value),
                ).flatten().also { lastCharLookedAt = link.match.range.last + 1 }
            }
            .toList()
            .plus(if (isLastWhitespace()) listOf(Space) else emptyList())
            .plus(splitWhitespaceCharactersAndWords(postfixGetter()))
    }

    private fun countChars(char: Char, str: String, index: Int = 0, maximum: Int = str.length): Int {
        if (str[index] != char) return 0
        var count = 1
        val actualMax = min(str.length - index, maximum)
        while (count < actualMax && str[index + count] == char) ++count
        return count
    }

    fun parseParagraphs(str: String): List<Paragraph> {
        val paragraphs = ArrayList<Paragraph>()
        val sb = StringBuilder()
        val lines = str.split('\n')

        fun completeParagraph() {
            if (sb.isNotBlank()) {
                paragraphs.add(RegularParagraph(elements(sb)))
            }
            sb.clear()
        }

        lines.flatMap { line ->
            // repack lines with special endings
            bufferOrReturnLine(line, sb)
        }.forEach { dirtyLine ->
            val line = dirtyLine.trimStart()

            fun dumpLineToBuffer() {
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(line)
            }

            val lineLinkMatch: LinkMatcher? by lazy {
                linkRegex.find(line)?.takeIf { it.value == line }?.let { LinkMatcher(it) }
            }
            when {
                // every handling that is creating paragraphs
                line.isEmpty() -> completeParagraph()
                line.startsWith('#') -> {
                    val charCount = countChars('#', line, maximum = 6)
                    // check if there is at least one space and characters after the header indicators
                    val text = line.trimStart('#')
                    when {
                        text.isBlank() -> dumpLineToBuffer()
                        text[0] == ' ' -> {
                            // it's an header!
                            completeParagraph()
                            paragraphs.add(HeaderParagraph(splitWhitespaceCharactersAndWords(text.trimEnd('#')), charCount))
                        }
                        else -> dumpLineToBuffer()
                    }
                }
                line.startsWith("===") -> {
                    paragraphs.add(if (sb.isBlank()) HorizontalLineParagraph else HeaderParagraph(splitWhitespaceCharactersAndWords(sb.toString()), 1))
                    sb.clear()
                }
                line.startsWith("---") -> {
                    paragraphs.add(if (sb.isBlank()) HorizontalLineParagraph else HeaderParagraph(splitWhitespaceCharactersAndWords(sb.toString()), 2))
                    sb.clear()
                }
                lineLinkMatch?.isMenuLink() == true && !lineLinkMatch!!.imageLinkFlag -> {
                    completeParagraph()
                    paragraphs.add(MenuParagraph(lineLinkMatch!!.text!!, lineLinkMatch!!.linkWithoutProtocol!!))
                }
                lineLinkMatch?.isImageLink() == true && lineLinkMatch!!.imageLinkFlag -> {
                    completeParagraph()
                    paragraphs.add(ImageParagraph(lineLinkMatch!!.text!!, lineLinkMatch!!.linkWithoutProtocol!!))
                }
                // TODO: add ListParagraph
                else -> dumpLineToBuffer()
            }
        }
        completeParagraph()
        return paragraphs
    }

    private fun bufferOrReturnLine(line: String, buffer: StringBuilder): List<String> {
        when {
            line.endsWith('\\') -> {
                // special handling for escaped line endings -> buffer line
                buffer.append(line.substring(0, line.length - 1))
                return emptyList()
            }
            line.endsWith("  ") -> {
                // special handling for special line breaks -> add newline char
                buffer.append(line.substring(0, line.length - 2))
                buffer.append('\n')
            }
            else -> buffer.append(line)
        }
        val concatLine = buffer.toString()
        buffer.clear()
        return listOf(concatLine)
    }

}
