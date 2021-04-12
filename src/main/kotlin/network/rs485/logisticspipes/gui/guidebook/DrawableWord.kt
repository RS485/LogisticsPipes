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

import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import kotlin.math.floor

/**
 * Normal Token that stores the text and the formatting tags of said text.
 */
open class DrawableWord(
    private val str: String,
    private val scale: Float,
    state: InlineDrawableState,
    protected val linkInteractable: LinkInteractable?,
) : Drawable, MouseInteractable {

    final override var relativeBody: Rectangle = Rectangle()
    override var parent: Drawable? = null
        set(value) {
            field = value
            (value as? DrawableParagraph)?.run(::setupParent)
        }
    override var z: Float = GuideBookConstants.Z_TEXT
    val format: Set<TextFormat> = state.format
    val color: Int = state.color

    init {
        relativeBody.setSize(GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), scale), GuiGuideBook.lpFontRenderer.getFontHeight(scale))
    }

    private fun setupParent(drawableParagraph: DrawableParagraph) {
        if (linkInteractable != null) drawableParagraph.registerPreRenderCallback(linkInteractable::updateState)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?) =
        linkInteractable?.mouseClicked(mouseX, mouseY, mouseButton, guideActionListener) ?: super.mouseClicked(mouseX, mouseY, mouseButton, guideActionListener)

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean =
            absoluteBody.contains(mouseX, mouseY)

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        val hovering = linkInteractable?.isMouseHovering(mouseX, mouseY) ?: false
        val updatedColor = linkInteractable?.updateColor(color) ?: color
        val updatedFormat = linkInteractable?.updateFormat(format) ?: format
        if (hovering) {
            LPGuiDrawer.drawInteractionIndicator(mouseX, mouseY, 25f)
        }
        LPGuiDrawer.lpFontRenderer.drawString(string = str, x = left, y = top, color = updatedColor, format = updatedFormat, scale = scale)
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
class DrawableSpace(
    private val scale: Float,
    state: InlineDrawableState,
    linkInteractable: LinkInteractable?,
) : DrawableWord(" ", scale, state, linkInteractable) {

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        if (width > 0) {
            linkInteractable?.isMouseHovering(mouseX, mouseY)
            val updatedColor = linkInteractable?.updateColor(color) ?: color
            val updatedFormat = linkInteractable?.updateFormat(format) ?: format
            GuiGuideBook.lpFontRenderer.drawSpace(
                x = left,
                y = top,
                width = width,
                color = updatedColor,
                italic = updatedFormat.italic(),
                underline = updatedFormat.underline(),
                strikethrough = updatedFormat.strikethrough(),
                shadow = updatedFormat.shadow(),
                scale = scale,
            )
        }
    }

    fun setWidth(newWidth: Int) {
        relativeBody.setSize(newWidth, relativeBody.roundedHeight)
    }

    fun resetWidth() {
        setWidth(newWidth = GuiGuideBook.lpFontRenderer.getStringWidth(" ", format.italic(), format.bold(), scale))
    }

    override fun toString(): String {
        return "Space of size $width with formatting: $format."
    }
}

object DrawableBreak : DrawableWord("", 1.0f, defaultDrawableState, null)

internal fun splitAndInitialize(drawables: List<DrawableWord>, x: Int, y: Int, maxWidth: Int, justify: Boolean): Int {
    var currentHeight = 0
    val splitLines = splitLines(drawables, maxWidth)

    fun isLastLine(line: List<DrawableWord>) = line == splitLines.last()
    fun hasBreak(line: List<DrawableWord>) = line.contains(DrawableBreak)

    for (line in splitLines) {
        currentHeight += if (!justify || isLastLine(line) || hasBreak(line)) {
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
    var remainder = if (spacesExceptIfLast.isNotEmpty()) totalSpaceWidth % spacesExceptIfLast.size else 0
    spacesExceptIfLast.forEach { space ->
        val currentSpaceWidth = if (remainder > 0) {
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
    if (line.isEmpty()) return 0
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
