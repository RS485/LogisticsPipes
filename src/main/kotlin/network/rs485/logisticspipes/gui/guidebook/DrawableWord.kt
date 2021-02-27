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

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.utils.MinecraftColor
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import kotlin.math.floor

/**
 * Normal Token that stores the text and the formatting tags of said text.
 */
open class DrawableWord(private val str: String, private val scale: Double, state: InlineDrawableState) : Drawable() {
    val format: Set<TextFormat> = state.format
    val color: Int = state.color

    init {
        relativeBody.setSize(GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), scale), GuiGuideBook.lpFontRenderer.getFontHeight(scale))
    }
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        GuiGuideBook.lpFontRenderer.drawString(string = str, x = left, y = top, color = color, format = format, scale = scale)
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        return super.setPos(x, y)
    }

    override fun toString(): String {
        return "\"$str\""
    }
}

/**
 * Space object responsible for drawing the necessary formatting in between words.
 */
class DrawableSpace(private val scale: Double, state: InlineDrawableState) : DrawableWord(" ", scale, state) {
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        if (width > 0) GuiGuideBook.lpFontRenderer.drawSpace(x = left, y = top, width = width, color = color, italic = format.italic(), underline = format.underline(), strikethrough = format.strikethrough(), shadow = format.shadow(), scale = scale)
    }

    fun setWidth(newWidth: Int) {
        relativeBody.setSize(newWidth = newWidth)
    }

    fun resetWidth() {
        setWidth(newWidth = GuiGuideBook.lpFontRenderer.getStringWidth(" ", format.italic(), format.bold(), scale))
    }

    override fun toString(): String {
        return "Space of size $width with formatting: $format."
    }
}

object DrawableBreak : DrawableWord("", 1.0, defaultDrawableState)

/**
 * TODO Link token, stores the linked string, as well as the 'url'.
 */
class DrawableLinkWord(val str: String, val scale: Double, val state: InlineDrawableState, val onClick: (mouseButton: Int) -> Unit) : DrawableWord(str, scale, defaultDrawableState){
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        hovered = this.hovering(mouseX, mouseY, visibleArea)
        val currentColor = if(hovered) MinecraftColor.BLUE.colorCode else state.color
        GuiGuideBook.lpFontRenderer.drawString(string = str, x = left, y = top, color = currentColor, format = format, scale = scale)
    }
    // TODO "link" all words connected to the same link synced when hovered.
}

internal fun splitAndInitialize(drawables: List<DrawableWord>, x: Int, y: Int, maxWidth: Int, justify: Boolean): Int {
    var currentHeight = 0
    val splitLines = splitLines(drawables, maxWidth)

    fun isLastLine(line: List<DrawableWord>) = line == splitLines.last()
    fun hasBreak(line: List<DrawableWord>) = line.contains(DrawableBreak)

    for (line in splitLines){
        currentHeight += if(!justify || isLastLine(line) || hasBreak(line)){
            initializeLine(line, x, y + currentHeight)
        } else {
            initializeJustifiedLine(line, x, y + currentHeight, maxWidth)
        }
    }
    return currentHeight
}

private fun initializeJustifiedLine(line: List<DrawableWord>, x: Int, y: Int, maxWidth: Int): Int {
    val spacesExceptIfLast = line.filterIsInstance<DrawableSpace>().filterNot { it == line.last() }
    val spaceIfLast: DrawableSpace? = line.find { it is DrawableSpace && it == line.last() } as DrawableSpace?
    val totalSpaceWidth = maxWidth - line.filterNot { it is DrawableSpace }.fold(0) { currentWidth, word -> currentWidth + word.width }
    val spaceWidthBase = floor(totalSpaceWidth.toFloat() / spacesExceptIfLast.size).toInt()
    var remainder = if(spacesExceptIfLast.isNotEmpty()) totalSpaceWidth % spacesExceptIfLast.size else 0
    spacesExceptIfLast.forEach { space ->
        val currentSpaceWidth = if(remainder > 0) {
            remainder--
            spaceWidthBase + 1
        } else {
            spaceWidthBase
        }
        space.setWidth(currentSpaceWidth)
    }
    spaceIfLast?.setWidth(0)
    return initializeLine(line, x, y)
}

private fun initializeLine(line: List<DrawableWord>, x: Int, y: Int): Int {
    if(line.isEmpty()) return 0
    line.fold(x) { currentX, word ->
        word.setPos(currentX, y)
        currentX + word.width
    }
    return line.maxOf { word -> word.height }
}

private fun splitLines(originalWords: List<DrawableWord>, maxWidth: Int): List<List<DrawableWord>> {
    val line = mutableListOf<DrawableWord>()
    val lines = mutableListOf<List<DrawableWord>>()
    var currentWidth = 0

    fun breakLine() {
        currentWidth = 0
        lines.add(line.toMutableList())
        line.clear()
    }

    fun addWordToLine(word: DrawableWord) {
        currentWidth += word.width
        line.add(word)
    }

    for (word in originalWords) {
        if (word is DrawableSpace) word.resetWidth()
        if (currentWidth + word.width > maxWidth && word !is DrawableSpace) {
            breakLine()
        }
        addWordToLine(word)
        if (word is DrawableBreak || word == originalWords.last()) breakLine()
    }

    return lines
}