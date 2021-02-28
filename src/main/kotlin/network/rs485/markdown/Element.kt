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

import com.google.common.collect.ImmutableSet
import logisticspipes.utils.MinecraftColor
import java.util.*

data class InlineDrawableState(var format: Set<TextFormat>, var color: Int, var link: Link?)

sealed class Link

data class WebLink(val url: String) : Link() {
    init {
        assert(url.isNotEmpty())
    }
}

data class PageLink(val page: String) : Link() {
    init {
        assert(page.isNotEmpty())
    }
}

sealed class InlineElement {
    open fun changeDrawableState(state: InlineDrawableState) {}
}

data class LinkFormatting(val link: Link?) : InlineElement() {
    override fun changeDrawableState(state: InlineDrawableState) {
        state.link = this.link
    }
}

data class TextFormatting(val format: EnumSet<TextFormat>) : InlineElement() {
    override fun changeDrawableState(state: InlineDrawableState) {
        state.format = this.format
    }
}

data class ColorFormatting(val color: Int) : InlineElement() {
    override fun changeDrawableState(state: InlineDrawableState) {
        state.color = this.color
    }
}

object Space : InlineElement() {
    override fun toString(): String {
        return "Markdown Space Element"
    }
}

object Break : InlineElement() {
    override fun toString(): String {
        return "Markdown Break Element"
    }
}

data class Word(val str: String) : InlineElement() {
    init {
        assert(str.isNotEmpty())
    }
}

/**
 * Used to track the tags a token has so the renderer knows how to draw said token.
 */
enum class TextFormat {
    Italic,
    Bold,
    Strikethrough,
    Underline,
    Shadow;

    companion object {
        val none: EnumSet<TextFormat>
            get() = EnumSet.noneOf(TextFormat::class.java)
    }
}

fun Set<TextFormat>.italic() = this.contains(TextFormat.Italic)

fun Set<TextFormat>.bold() = this.contains(TextFormat.Bold)

fun Set<TextFormat>.strikethrough() = this.contains(TextFormat.Strikethrough)

fun Set<TextFormat>.underline() = this.contains(TextFormat.Underline)

fun Set<TextFormat>.shadow() = this.contains(TextFormat.Shadow)

val defaultDrawableState = InlineDrawableState(
    format = ImmutableSet.of(),
    color = MinecraftColor.WHITE.colorCode,
    link = null,
)
