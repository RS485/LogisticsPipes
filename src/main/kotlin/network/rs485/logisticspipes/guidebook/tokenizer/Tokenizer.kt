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

package network.rs485.logisticspipes.guidebook.tokenizer

import logisticspipes.utils.MinecraftColor

object Tokenizer {
    private var definition = Definition.None
    private val current = mutableListOf<TokenTag>()

    private fun countChars(char: Char, str: String, index: Int): Int {
        if (str[index] != char) return 0
        var count = 1
        while ((index + count) < str.length && str[index + count] == char) ++count
        return count
    }

    /**
     * First phase -> responsible for splitting the whole text into Paragraphs.
     * The splitting is done at every LineBreak and at every Image tag
     * Second phase -> responsible for turning the paragraph list into a Token List.
     * The splitting of the paragraphs is make at every space and each token stores all the applied markdown tags accordingly.
     */
    fun parseParagraphs(str: String): List<GenericParagraphToken> {
        val paragraphs = ArrayList<GenericParagraphToken>()
        val sb = StringBuilder()
        val lines = str.split('\n')

        fun completeTextParagraph() {
            paragraphs.add(TextParagraph(listOf(Text(sb.toString(), emptyList()))))
            // TODO: add ImageParagraph
            // TODO: parse TokenText(s)
            sb.clear()
        }

        lines.flatMap { line ->
            if (line.endsWith('\\')) {
                sb.append(line.substring(0, line.length - 1))
                return@flatMap emptyList<String>()
            }
            sb.append(line)
            val concatLine = sb.toString()
            sb.clear()
            return@flatMap listOf(concatLine)
        }.forEach { dirtyLine ->
            val line = dirtyLine.trimStart()
            when {
                line.isEmpty() -> completeTextParagraph()
                line.startsWith(Tag.HEADER.char) -> {
                    completeTextParagraph()
                    val headerLevel = countChars(Tag.HEADER.char, line, 0)
                    // TODO: parse TokenText(s)
                    val text = Text(line.substring(headerLevel).trim(), emptyList())
                    paragraphs.add(HeaderParagraph(listOf(text), headerLevel))
                }
                // TODO: add MenuParagraph
                // TODO: add ListParagraph
                else -> {
                    if (sb.isNotEmpty()) sb.append(' ')
                    sb.append(line.trimEnd())
                }
            }
        }
        if (sb.isNotEmpty()) completeTextParagraph()
        return paragraphs
    }

    /*
    * Turns a well formatted string into an Image token
    * */
    private fun String.toImageToken(): ImageParagraph {
        val strC = toCharArray()
        val alternativeText = StringBuilder()
        val imagePath = StringBuilder()
        var readingPath = false;
        strC.forEachIndexed { index, c ->
            if (!readingPath) {
                when (c) {
                    '!', '[', ']' -> {
                        if (strC.isEscaped(index)) alternativeText.append(c)
                    }
                    '(' -> {
                        if (strC.isEscaped(index)) alternativeText.append(c)
                        else readingPath = true
                    }
                    else -> {
                        alternativeText.append(c)
                    }
                }
            } else {
                if (c == ')' && strC.isNotEscaped(index))
                else imagePath.append(c)
            }
        }
        return ImageParagraph(alternativeText.toString().toTokens(), imagePath.toString())
    }

    /*
    * Turns a well formatted string into an Item token
    * */
    private fun String.toItemToken(): ListParagraph {
        val strC = toCharArray()
        val name = StringBuilder()
        val item = StringBuilder()
        var readingUnlocalizedName = false;
        strC.forEachIndexed { index, c ->
            if (!readingUnlocalizedName) {
                when (c) {
                    '$', '[', ']' -> {
                        if (strC.isEscaped(index)) name.append(c)
                    }
                    '(' -> {
                        if (strC.isEscaped(index)) name.append(c)
                        else readingUnlocalizedName = true
                    }
                    else -> {
                        name.append(c)
                    }
                }
            } else {
                if (c == ')' && strC.isNotEscaped(index))
                else item.append(c)
            }
        }
        return ListParagraph(listOf(listOf()))
    }

    /*
    * Turns a well formatted string into a Menu token
    * */
    private fun String.toMenuToken(): MenuParagraph {
        val strC = toCharArray()
        val key = StringBuilder()
        val options = StringBuilder()
        var readingUnlocalizedName = false;
        strC.forEachIndexed { index, c ->
            if (!readingUnlocalizedName) {
                when (c) {
                    '#', '[', ']' -> {
                        if (strC.isEscaped(index)) key.append(c)
                    }
                    '(' -> {
                        if (strC.isEscaped(index)) key.append(c)
                        else readingUnlocalizedName = true
                    }
                    else -> {
                        key.append(c)
                    }
                }
            } else {
                if (c == ')' && strC.isNotEscaped(index))
                else options.append(c)
            }
        }
        return MenuParagraph(key.toString(), options.toString())
    }

    /*
    * Turns a well formatted string into a List of Linked tokens
    * */
    private fun String.toLinkedTokens(): MutableList<Link> {
        val strC = toCharArray()
        val linkedText = StringBuilder()
        val linkedPath = StringBuilder()
        var gettingUrl = false;
        strC.forEachIndexed { index, c ->
            if (!gettingUrl) {
                when (c) {
                    '[', ']' -> {
                        if (strC.isEscaped(index)) linkedText.append(c)
                    }
                    '(' -> {
                        if (strC.isEscaped(index)) linkedText.append(c)
                        else gettingUrl = true
                    }
                    else -> {
                        linkedText.append(c)
                    }
                }
            } else {
                if (c == ')' && strC.isNotEscaped(index))
                else linkedPath.append(c)
            }
        }
//        val tokens = mutableListOf<Text>()
//        linkedText.toString().toTokens().forEach {
//            tokens.add(Link((it as Text).str, linkedPath.toString()))
//        }
//        return tokens
        return mutableListOf()
    }

    /*
    * Turns a well formatted string into a List of Tokens with overwritten color
    * */
    private fun String.toColoredTokens(): MutableList<Text> {
        val strC = toCharArray()
        val coloredText = StringBuilder()
        val colorString = StringBuilder()
        var gettingColor = false;
        strC.forEachIndexed { index, c ->
            if (!gettingColor) {
                when (c) {
                    '+', '[', ']' -> {
                        if (strC.isEscaped(index)) coloredText.append(c)
                    }
                    '(' -> {
                        if (strC.isEscaped(index)) coloredText.append(c)
                        else gettingColor = true
                    }
                    else -> {
                        coloredText.append(c)
                    }
                }
            } else {
                if (c == ')' && strC.isNotEscaped(index))
                else colorString.append(c)
            }
        }
        return coloredText.toString().toTokens(color = colorString.toString().toColor())
    }

    /*
    * Tokenizes a String to any kinds of Tokens
    * */
    private fun String.toTokens(color: Int = MinecraftColor.WHITE.colorCode): MutableList<Text> {
        val tokens = mutableListOf<Text>()
        val strC = toCharArray()
        val string = StringBuilder()
        strC.forEachIndexed { index, c ->
            handleCharacter(c, index, tokens, strC, string, color)
        }
        definition = Definition.None
        current.clear()
        return tokens
    }

    /*
    * Opens and closes Special definitions like TokenImages or TokenLinks
    * */
    private fun StringBuilder.handleSpecialDefinitions(c: Char, index: Int, tokens: MutableList<Text>, strC: CharArray) {
        fun start(pChar: Char, def: Definition) {
            if (isNotEmpty() && this[lastIndex] == pChar) deleteCharAt(lastIndex)
            addToken(tokens, MinecraftColor.WHITE.colorCode)
            clear()
            append("$pChar[")
            definition = def
        }

        fun close() {
            append(c)
            val tDef = definition
            definition = Definition.None
            when (tDef) {
                Definition.Color -> tokens.addAll(toString().toColoredTokens())
                Definition.Link -> tokens.addAll(toString().toLinkedTokens())
                Definition.None -> {
                    tokens.addAll(toString().toTokens(MinecraftColor.WHITE.colorCode))
                }
            }
            clear()
        }

        when (definition) {
            Definition.None -> {
                when (strC.prevChar(index)) {
                    '+' -> start('+', Definition.Color)
                    ' ' -> start(' ', Definition.Link)
                    else -> Unit
                }
            }
            else -> {
                close()
            }
        }
    }

    /*
    * Handles cumulative tags as well as the line break Token.
    * */
    private fun StringBuilder.handleSpecialTag(c: Char, index: Int, tokens: MutableList<Text>, strC: CharArray, color: Int) {
        fun StringBuilder.handleLineBreak() {
            when {
                strC.isAfterTwoSpaces(index) -> {
                    tokens.add(Break)
                }
                strC.isAfterOneSpace(index) -> Unit
                else -> {
                    append(' ')
                    tokens.add(Text(toString(), current.toMutableList()))
                }
            }
            clear()
        }

        fun StringBuilder.handleSingleCharacter(tag: TokenTag) {
            fun toggleTag() {
                addToken(tokens, color)
                when {
                    current.contains(tag) -> current.remove(tag)
                    else -> current.add(tag)
                }
                clear()
            }

            when (c) {
                strC.nextChar(index) -> Unit
                else -> toggleTag()
            }
        }

        fun StringBuilder.handleDoubleCharacter(tag: TokenTag, singleTag: TokenTag) {
            fun toggleTag() {
                addToken(tokens, color)
                when {
                    current.contains(tag) -> current.remove(tag)
                    else -> current.add(tag)
                }
                clear()
            }

            fun toggleTags() {
                toggleTag()
                when {
                    current.contains(singleTag) -> current.remove(singleTag)
                    else -> current.add(singleTag)
                }
            }

            when (c) {
                strC.nextChar(index) -> Unit
                strC.prevChar(index) -> {
                    if (strC.prevChar(index - 1) == c) {
                        toggleTags()
                    } else {
                        toggleTag()
                    }
                }
                else -> handleSingleCharacter(singleTag)
            }
        }

        when (c) {
            '\n' -> this.handleLineBreak()
            '_', '*' -> handleDoubleCharacter(Tokenizer.TokenTag.Bold, Tokenizer.TokenTag.Italic)
            '~' -> handleDoubleCharacter(Tokenizer.TokenTag.Strikethrough, Tokenizer.TokenTag.Underline)
            '^' -> handleSingleCharacter(Tokenizer.TokenTag.Shadow)
            else -> Unit
        }
    }

    /*
    * Adds a normal token to the given Token MutableList the text being the StringBuilder itself.
    * */
    private fun StringBuilder.addToken(tokens: MutableList<Text>, color: Int) {
        if (isNotEmpty()) {
            tokens.add(Text(toString(), current.toMutableList(), color = color))
        }
    }

    /*
    * Handles non-special characters, if not instructed not to, it also creates a new token at every space character and at the end of the CharSequence.
    * */
    private fun StringBuilder.handleTextDefault(c: Char, index: Int, tokens: MutableList<Text>, strC: CharArray, color: Int, ignoreSpaces: Boolean = false) {
        append(c)
        if ((c == ' ' && strC.nextChar(index) != ' ' && strC.nextChar(index) != '\n' && strC.nextChar(index) != '[' && !ignoreSpaces) || index == strC.lastIndex) {
            addToken(tokens, color)
            clear()
        }
    }

    /*
    * Handles a single character in an array, checks for escaping and reacts according to the character itself
    * */
    private fun handleCharacter(c: Char, index: Int, tokens: MutableList<Text>, strC: CharArray, string: StringBuilder, color: Int) {
        when (definition) {
            Definition.None -> when (c) {
                '_', '*', '~', '^', '\n' -> if (strC.isEscaped(index)) string.handleTextDefault(
                        c,
                        index,
                        tokens,
                        strC,
                        color
                ) else string.handleSpecialTag(c, index, tokens, strC, color)
                '[' -> if (strC.isEscaped(index)) string.handleTextDefault(
                        c,
                        index,
                        tokens,
                        strC,
                        color
                ) else string.handleSpecialDefinitions(c, index, tokens, strC)
                else -> string.handleTextDefault(c, index, tokens, strC, color)
            }
            else -> {
                if (c == ')' && strC.isNotEscaped(index)) string.handleSpecialDefinitions(c, index, tokens, strC)
                else string.handleTextDefault(c, index, tokens, strC, color, ignoreSpaces = true)
            }
        }
    }

    /*
    * Returns the next Char in the array:
    * If the previous index is out of bounds returns an indifferent character, in this case: 'a'
    * If said character was escaped using '\' the function returns the same indifferent character as before.
    * */
    private fun CharArray.prevChar(index: Int): Char {
        return if (index == 0 || (index >= 1 && this[index - 1] == '\\')) 'a' else this[index - 1]
    }

    /*
    * Returns the next Char in the array:
    * If the next index is out of bounds returns an indifferent character, in this case: 'a'
    * */
    private fun CharArray.nextChar(index: Int): Char {
        return if (index == lastIndex) 'a' else this[index + 1]
    }

    /*
    * Checks if the previous character is a backslash and if the one prior to that is also a backslash, because if the backslash itself is escaped it does not escape the current character.
    * */
    private fun CharArray.isEscaped(index: Int): Boolean {
        return (prevChar(index) == '\\') && (prevChar(index - 1) != '\\')
    }

    /*
    * Returns the opposite of isEscaped()
    * */
    private fun CharArray.isNotEscaped(index: Int): Boolean {
        return !isEscaped(index)
    }

    /*
    * Checks if the two previous characters are spaces.
    * */
    private fun CharArray.isAfterTwoSpaces(index: Int): Boolean {
        return ((isAfterOneSpace(index)) && (isAfterOneSpace(index - 1)))
    }

    /*
    * Checks if the previous character is a space
    * */
    private fun CharArray.isAfterOneSpace(index: Int): Boolean {
        return (prevChar(index) == ' ')
    }

    /*
    * Converts a string of type #hex or a color name to a valid non-null Color object.
    * */
    private fun String.toColor(): Int {
        return if (isNotEmpty() && first() == '#') {
            this.substring(1, lastIndex).toInt(16)
        } else MinecraftColor.WHITE.colorCode
    }

    enum class Tag(val char: Char) {
        HEADER('#')
    }

    /*
    * Used to track the tags a token has so the renderer knows how to draw said token.
    * This is also a sealed class instead of an enum class because this way I can store the linked page in the Link tag as well as an image in an Image tag, for example.
    * */
    enum class TokenTag {
        Italic,
        Bold,
        Strikethrough,
        Underline,
        Shadow
    }

    /*
    * Used to track which kind of []() definition is being handled, or None
    * */
    enum class Definition {
        None,  // Defines the normal state of text handling.
        Color, // '+'
        Link,  // ' '
    }
}