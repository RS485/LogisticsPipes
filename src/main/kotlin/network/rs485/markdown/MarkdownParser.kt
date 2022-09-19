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

import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import java.lang.Integer.min
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.stream.IntStream
import kotlin.collections.ArrayList

typealias WordCreator = (String) -> Word?
typealias TextToElements = (CharSequence) -> List<InlineElement>

object MarkdownParser {
    private val linkRegex = Regex("!?\\[(.+?)]\\((.+?)\\)")
    private val webLinkRegex = Regex("^https?://.+", RegexOption.IGNORE_CASE)
    private val boldItalicRegexes = (3 downTo 1)
        .flatMap { times -> listOf("_", "\\*").map { times to it.repeat(times) } }
        .map { (times, formatChain) -> times to Regex("(?!\\\\)$formatChain(.+?)(?!\\\\)$formatChain") }
    private val pathRegex = Regex("^.*?://([^?]+)")

    private class LinkMatcher(val match: MatchResult) {
        val text: String?
            get() = match.groups[1]?.value
        val link: String?
            get() = match.groups[2]?.value
        val path: String?
            get() = link?.let { pathRegex.find(it) }?.groups?.get(1)?.value
        val imageLinkFlag: Boolean
            get() = match.value.trimStart().startsWith('!')
        val uri: URI? by lazy {
            link?.let {
                try {
                    URI(it)
                } catch (error: URISyntaxException) {
                    null
                }
            }
        }
        val queryComponents: List<NameValuePair>
            get() = uri?.let { URLEncodedUtils.parse(it.query, Charsets.UTF_8) } ?: emptyList()

        fun getMenuType() = if (uri?.scheme == "menu")
            queryComponents.find { it.name == "type" }.let { type ->
                when {
                    type == null -> MenuParagraphType.LIST
                    type.value == "list" -> MenuParagraphType.LIST
                    type.value == "tile" -> MenuParagraphType.TILE
                    else -> null
                }
            } else null

        fun isPageLink() = link?.startsWith("page://", ignoreCase = true) ?: false
        fun isWebLink() = link?.matches(webLinkRegex) ?: false
        fun isImageLink() = link?.startsWith("image://", ignoreCase = true) ?: false
        fun isItemLink() = link?.startsWith("item://", ignoreCase = true) ?: false
    }

    private val htmlBreakRegex = Regex("<br(\\s*/)?>")

    internal fun splitWhitespaceCharactersAndWords(inputChars: CharSequence): List<InlineElement> {
        // newlines are already handled as special break characters at this point
        val chars = htmlBreakRegex.replace(inputChars, "\n")

        // breaks up all the words and returns them as inline elements without spaces
        return chars
            .split('\n')
            .map { words ->
                splitAndFormatWords(words) + listOf(Break)
            }
            .flatten()
            .dropLast(1)
    }

    class ReplacedCharSequence(val original: CharSequence) : CharSequence {
        private val formattingStart = mutableListOf<Pair<Int, EnumSet<TextFormat>>>()
        private val replacementMap = mutableListOf<Pair<IntRange, InlineElement>>()
        private val stagingReplacementMap = mutableListOf<Triple<Int, IntRange, InlineElement>>()
        private var cache: String? = null
        val current: String
            get() = cache ?: updateCachedSequence()

        private fun updateCachedSequence(): String {
            val sb = StringBuilder()
            val append = { range: IntRange -> range.takeIf { !it.isEmpty() }?.also { sb.append(original.subSequence(it)) } }
            var lastIdx = 0
            replacementMap.forEach { (range, _) ->
                assert(range.first >= lastIdx) { "trying to replace already replaced index $lastIdx in $replacementMap" }
                append(lastIdx until range.first)
                lastIdx = range.last + 1
            }
            append(lastIdx until original.length)
            return sb.toString().also { cache = it }
        }

        private fun spaceReplacement(cutIndex: Int) = isWhitespaceAt(cutIndex).also { isSpace ->
            if (isSpace) stageReplacingElement(cutIndex..cutIndex, Space)
        }

        fun addReplacement(fullRange: IntRange, innerRange: IntRange, format: EnumSet<TextFormat>) {
            if (fullRange.first > 0) {
                if (!spaceReplacement(innerRange.first)) spaceReplacement(fullRange.first - 1)
            }
            // add format end before start, so that $format is not detected as existing formatting and added here
            stageReplacingElement((innerRange.last + 1)..fullRange.last, TextFormatting(TextFormat.none))
            stageReplacingElement(fullRange.first until innerRange.first, TextFormatting(format.clone()))
            enhanceExistingReplacements(innerRange, format)
            if (fullRange.last + 1 < length) {
                if (!spaceReplacement(innerRange.last)) spaceReplacement(fullRange.last + 1)
            }
        }

        private fun enhanceExistingReplacements(range: IntRange, format: Set<TextFormat>) {
            val (endReplacementMapIdx, _) = translateIndex(range.last)
            if (endReplacementMapIdx == -1) return
            val (startReplacementMapIdx, _) = translateIndex(range.first)
            replacementMap.subList(startReplacementMapIdx + 1, endReplacementMapIdx + 1).forEach {
                (it.second as? TextFormatting)?.format?.addAll(format)
            }
        }

        private fun stageReplacingElement(range: IntRange, element: InlineElement) {
            val (replacementMapIdx, translatedIdx) = translateIndex(range.first)
            stagingReplacementMap.add(
                Triple(replacementMapIdx + 1, translatedIdx..(range.last + translatedIdx - range.first), element)
            )
            if (element is TextFormatting) {
                val newFormat = (translatedIdx to element.format)
                if (formattingStart.isEmpty()) {
                    formattingStart.add(newFormat)
                } else {
                    val formatIdx = formattingStart.binarySearch(newFormat, comparator = { o1, o2 ->
                        o1.first.compareTo(o2.first)
                    })
                    val index = if (formatIdx < 0) -formatIdx - 1 else formatIdx + 1
                    // add previous formatting, if applicable
                    if (index > 0) newFormat.second.addAll(formattingStart[index - 1].second)
                    formattingStart.add(index, newFormat)
                }
            }
        }

        private fun translateIndex(start: Int): Pair<Int, Int> {
            var translatedIdx = start
            var replacementMapIdx = -1
            replacementMap.asSequence()
                .takeWhile { translatedIdx >= it.first.first }
                .forEachIndexed { curry, (range, _) ->
                    translatedIdx += range.count()
                    replacementMapIdx = curry
                }
            return replacementMapIdx to translatedIdx
        }

        fun commit() {
            if (stagingReplacementMap.isNotEmpty()) {
                stagingReplacementMap.sortedByDescending {
                    // insertion into replacementMap needs to be ordered,
                    // because the replacements' indices are translated
                    it.second.last
                }.forEach { (index: Int, replaceRange: IntRange, element: InlineElement) ->
                    (index - 1).takeIf { replacementIndexIsTextFormatting(it, element) }
                        ?.let { previousIdx -> replaceFormattingIfApplicable(previousIdx, replacementMap[previousIdx].first, replaceRange, element) }
                    ?: index.takeIf { replacementIndexIsTextFormatting(index, element) }
                        ?.let { idx -> replaceFormattingIfApplicable(idx, replaceRange, replacementMap[idx].first, replacementMap[idx].second) }
                    ?: replacementMap.add(index, replaceRange to element)
                }
                cache = null
                stagingReplacementMap.clear()
            }
        }

        private fun replacementIndexIsTextFormatting(index: Int, element: InlineElement): Boolean =
            index in replacementMap.indices && element is TextFormatting && replacementMap[index].second is TextFormatting

        private fun replaceFormattingIfApplicable(
            index: Int,
            lowRange: IntRange,
            highRange: IntRange,
            element: InlineElement,
        ): Pair<IntRange, InlineElement>? =
            if (lowRange.last + 1 == highRange.first) {
                (lowRange.first..highRange.last to element).also { replacementMap[index] = it }
            } else null

        fun forEachReplace(action: (Pair<IntRange, InlineElement>) -> Unit) = replacementMap.forEach(action)

        override val length: Int
            get() = current.length

        fun isWhitespaceAt(index: Int): Boolean = get(index).isWhitespace()
        override fun get(index: Int): Char = current[index]
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = current.subSequence(startIndex, endIndex)
        override fun chars(): IntStream = current.chars()
        override fun codePoints(): IntStream = current.codePoints()
        override fun equals(other: Any?): Boolean =
            other is ReplacedCharSequence && original == other.original && replacementMap == other.replacementMap

        override fun hashCode(): Int = original.hashCode() + replacementMap.hashCode()
        override fun toString(): String =
            "${super.toString()}(original=\"${original.take(16)}[…]\"," +
                    "replacementMap=${replacementMap.toString().take(16)}[…])"

    }

    private fun splitAndFormatWords(inputChars: CharSequence, wordSplitter: TextToElements): List<InlineElement> {
        val textLine = inputChars.trimEnd()
        val replacingChars = ReplacedCharSequence(textLine)
        boldItalicRegexes.forEach { (level, regex) ->
            val format = when (level) {
                3 -> EnumSet.of(TextFormat.Italic, TextFormat.Bold)
                2 -> EnumSet.of(TextFormat.Bold)
                1 -> EnumSet.of(TextFormat.Italic)
                else -> TextFormat.none
            }
            regex.findAll(replacingChars).forEach { match ->
                val textGroup = match.groups[1]!!
                replacingChars.addReplacement(match.range, textGroup.range, format)
            }
            replacingChars.commit()
        }
        // TODO: add parsing for underline, strikethrough and shadow (with ~)
        // TODO: add color parsing
        val mappedWords = arrayListOf<InlineElement>()
        fun addMappedWords(originalRange: IntRange) {
            if (originalRange.isEmpty()) return
            mappedWords.addAll(wordSplitter(textLine.subSequence(originalRange)))
        }

        var startIdx = 0
        replacingChars.forEachReplace { (range, inlineElem) ->
            addMappedWords(startIdx until range.first)
            mappedWords.add(inlineElem)
            startIdx = range.last + 1
        }
        addMappedWords(startIdx..replacingChars.original.lastIndex)
        return mappedWords
    }

    internal fun splitAndFormatWords(inputChars: CharSequence): List<InlineElement> = splitAndFormatWords(inputChars, ::splitSpacesAndWords)

    private fun splitSpacesAndWords(inputChars: CharSequence, makeWord: WordCreator): List<InlineElement> {
        return inputChars
            .split(' ')
            .flatMap { word -> listOfNotNull(makeWord(word)) }
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

    internal fun splitSpacesAndWords(inputChars: CharSequence): List<InlineElement> = splitSpacesAndWords(inputChars, ::parseTextElement)

    private fun parseTextElement(word: String) = if (word.isNotBlank()) Word(word) else null

    private fun parseLinks(inputChars: CharSequence): List<InlineElement> {
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
                        link.isPageLink() -> PageLink(link.path!!)
                        link.isWebLink() -> WebLink(link.link!!)
                        link.isItemLink() -> ItemLink(link.path!!)
                        else -> null
                    }?.let { linkRef ->
                        listOf(
                            listOfNotNull(Word("!").takeIf { link.imageLinkFlag }),
                            listOf(LinkFormatting(linkRef)),
                            splitAndFormatWords(link.text!!),
                            listOf(LinkFormatting(null)),
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
                paragraphs.add(RegularParagraph(parseLinks(sb)))
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
                linkRegex.find(line)?.takeIf { it.value == line.trimEnd() }?.let { LinkMatcher(it) }
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
                            paragraphs.add(HeaderParagraph(parseLinks(text.trimEnd('#')), charCount))
                        }
                        else -> dumpLineToBuffer()
                    }
                }
                line.startsWith("===") -> {
                    paragraphs.add(if (sb.isBlank()) HorizontalLineParagraph else HeaderParagraph(parseLinks(sb.toString()), 1))
                    sb.clear()
                }
                line.startsWith("---") -> {
                    paragraphs.add(if (sb.isBlank()) HorizontalLineParagraph else HeaderParagraph(parseLinks(sb.toString()), 2))
                    sb.clear()
                }
                lineLinkMatch?.getMenuType() != null && !lineLinkMatch!!.imageLinkFlag ->
                    lineLinkMatch?.also {
                        completeParagraph()
                        paragraphs.add(MenuParagraph(it.text!!, it.path!!, it.getMenuType()!!))
                    }
                lineLinkMatch?.isImageLink() == true && lineLinkMatch!!.imageLinkFlag ->
                    lineLinkMatch?.also {
                        completeParagraph()
                        paragraphs.add(ImageParagraph(it.text!!, it.path!!))
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
