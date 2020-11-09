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

import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import java.util.*
import kotlin.math.floor

/**
 * Normal Token that stores the text and the formatting tags of said text.
 */
open class DrawableWord(override val parent: IDrawable?, private val str: String, private val scale: Double, state: InlineDrawableState) : IDrawable {
    val format: EnumSet<TextFormat> = state.format
    val color: Int = state.color

    override var width = GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), scale)
    override var height = GuiGuideBook.lpFontRenderer.getFontHeight(scale)
    override var x = 0
    override var y = 0

    override var hovered = false

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        GuiGuideBook.lpFontRenderer.drawString(string = str, x = left(), y = top(), color = color, format = format, scale = scale)
    }

    override fun setPos(x: Int, y: Int): Pair<Int, Int> {
        this.x = x
        this.y = y
        return width to height
    }

    override fun toString(): String {
        return "\"$str\""
    }
}

/**
 * Space object responsible for drawing the necessary formatting in between words.
 */
class DrawableSpace(parent: IDrawable, private val scale: Double, state: InlineDrawableState) : DrawableWord(parent," ", scale, state) {

    override fun setPos(x: Int, y: Int): Pair<Int, Int> {
        this.x = x
        this.y = y
        return width to height
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        if (width > 0) GuiGuideBook.lpFontRenderer.drawSpace(x = left(), y =top(), width = width, color = color, italic = format.italic(), underline = format.underline(), strikethrough = format.strikethrough(), shadow = format.shadow(), scale = scale)
    }

    override fun toString(): String {
        return "Space of size $width with formatting: $format."
    }
}

object DrawableBreak : DrawableWord(null,"", 1.0, DEFAULT_DRAWABLE_STATE)

/**
 * TODO Link token, stores the linked string, as well as the 'url'.
 */
class Link(parent: IDrawable?, text: String) : DrawableWord(parent, text, 1.0, DEFAULT_DRAWABLE_STATE)

internal fun initLine(x: Int, y: Int, line: MutableList<DrawableWord>, justified: Boolean, maxWidth: Int): Int {
    var maxHeight = 0
    var remainder = 0
    val spacing = if (justified && line.size != 0) {
        val wordsWidth = (line.fold(0) { i, elem -> i + if (elem !is DrawableSpace) elem.width else 0 })
        val remainingSpace = floor(maxWidth - wordsWidth.toDouble())
        val numberSpaces = if (line.last() is DrawableSpace) line.count { it is DrawableSpace } - 1 else line.count { it is DrawableSpace }
        remainder = remainingSpace.rem(numberSpaces).toInt()
        floor(remainingSpace / numberSpaces.toDouble()).toInt()
    } else {
        line.find { it is DrawableSpace }?.width ?: GuiGuideBook.lpFontRenderer.getStringWidth(" ")
    }
    line.foldIndexed(x) { _, currX, drawableWord ->
        when (drawableWord) {
            is DrawableSpace -> {
                val currentSpacing = when {
                    (drawableWord == line.last()) -> 0
                    remainder > 0 -> {
                        remainder--
                        spacing + 1
                    }
                    else -> spacing
                }
                drawableWord.setPos(currX, y)
                drawableWord.width = currentSpacing
            }
            else -> {
                drawableWord.setPos(currX, y)
            }
        }
        maxHeight = maxOf(maxHeight, drawableWord.height)
        currX + drawableWord.width
    }
    return maxHeight
}

internal fun splitInitialize(drawables: List<DrawableWord>, x: Int, y: Int, maxWidth: Int): Int {
    var currentY = 1
    var currentWidth = 0
    if (maxWidth > 0) {
        val currentLine = mutableListOf<DrawableWord>()
        for (currentDrawableWord in drawables) {
            when (currentDrawableWord) {
                // Break line and setPos on the queued up words via break signal
                is DrawableBreak -> {
                    currentLine.add(currentDrawableWord)
                    currentY += initLine(x, y + currentY, currentLine, false, maxWidth)
                    currentLine.clear()
                    currentWidth = 0
                }
                else -> {
                    // Break line and setPos on the queued up words via line width
                    if (currentDrawableWord !is DrawableSpace && currentWidth + currentDrawableWord.width > maxWidth) {
                        currentY += initLine(x, y + currentY, currentLine, true, maxWidth)
                        currentLine.clear()
                        currentWidth = 0
                    }
                    currentLine.add(currentDrawableWord)
                    currentWidth += if (currentDrawableWord is DrawableSpace) GuiGuideBook.lpFontRenderer.getStringWidth(" ") else currentDrawableWord.width
                    if (currentDrawableWord == drawables.last()) currentY += initLine(x, y + currentY, currentLine, false, maxWidth)
                }
            }
        }
        currentY += 1
    }
    return currentY
}