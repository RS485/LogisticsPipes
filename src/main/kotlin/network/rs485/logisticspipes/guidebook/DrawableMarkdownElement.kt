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

package network.rs485.logisticspipes.guidebook

import logisticspipes.LPConstants
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.gui.guidebook.IDrawable
import network.rs485.logisticspipes.gui.guidebook.IDrawableParagraph
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import java.util.*
import kotlin.math.floor

const val DEBUG_AREAS = false

private val DEFAULT_DRAWABLE_STATE = InlineDrawableState(EnumSet.noneOf(TextFormat::class.java), MinecraftColor.WHITE.colorCode)
private val HEADER_LEVELS = listOf(2.0, 1.80, 1.60, 1.40, 1.20)

/**
 * Stores groups of ITokenText tokens to more easily translate Tokens to Drawable elements
 */
data class DrawableRegularParagraph(val drawables: List<DrawableWord>) : IDrawableParagraph {
    override val area = Rectangle(0, 0)
    override var isHovered = false

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(1.0F, 0.0F, 0.0F)
        // Split by lines
        val lines = drawables.groupBy { it.area.y0 }.values
        for (line in lines) {
            // Check if first (representative of the whole line) is visible, aka contained within the visible area.
            if (visibleArea.overlaps(line.first().area)) {
                for (drawable in line) {
                    if (isHovered && drawable is Link) {
                        drawable.hovering(mouseX, mouseY, yOffset)
                    }
                    //if(drawable !is DrawableSpace)
                    drawable.draw(mouseX, mouseY, delta, yOffset, visibleArea)
                }
            }
        }
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return setChildrenPos(x, y, maxWidth)
    }

    override fun setChildrenPos(x: Int, y: Int, maxWidth: Int): Int {
        fun initLine(x: Int, y: Int, line: MutableList<DrawableWord>, justified: Boolean, maxWidth: Int): Int {
            var maxHeight = 0
            var remainder = 0
            val spacing = if (justified && line.size != 0) {
                val wordsWidth = (line.fold(0) { i, elem -> i + if (elem !is DrawableSpace) elem.area.width else 0 })
                val remainingSpace = floor(maxWidth - wordsWidth.toDouble());
                val numberSpaces = if (line.last() is DrawableSpace) line.count { it is DrawableSpace } - 1 else line.count { it is DrawableSpace }
                remainder = remainingSpace.rem(numberSpaces).toInt()
                floor(remainingSpace / numberSpaces.toDouble()).toInt()
            } else {
                GuiGuideBook.lpFontRenderer.getStringWidth(" ")
            }
            line.foldIndexed(x) { _, currX, drawableWord ->
                when (drawableWord) {
                    is DrawableSpace -> {
                        val currentSpacing = when  {
                            (drawableWord == line.last()) -> 0
                            remainder > 0 -> {
                                remainder--
                                spacing + 1
                            }
                            else -> spacing
                        }
                        drawableWord.setPos(currX, y, maxWidth)
                        drawableWord.setWidth(currentSpacing)
                    }
                    else -> {
                        drawableWord.setPos(currX, y, maxWidth)
                    }
                }
                maxHeight = maxOf(maxHeight, drawableWord.area.height)
                currX + drawableWord.area.width
            }
            return maxHeight
        }

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
                        if (currentDrawableWord !is DrawableSpace && currentWidth + currentDrawableWord.area.width > maxWidth) {
                            currentY += initLine(x, y + currentY, currentLine, true, maxWidth)
                            currentLine.clear()
                            currentWidth = 0
                        }
                        currentLine.add(currentDrawableWord)
                        currentWidth += currentDrawableWord.area.width + GuiGuideBook.lpFontRenderer.getStringWidth(" ")
                        if (currentDrawableWord == drawables.last()) currentY += initLine(x, y + currentY, currentLine, false, maxWidth)
                    }
                }
            }
            currentY += 1
            area.setSize(maxWidth, currentY)
        }
        return currentY
    }
}

/**
 * Header token, stores all the tokens that are apart of the header.
 */
data class DrawableHeaderParagraph(val drawables: List<DrawableWord>, val headerLevel: Int = 1) : IDrawableParagraph {
    override val area = Rectangle(0, 0)
    override var isHovered = true

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(0.0F, 1.0F, 0.0F)
        for (textToken in drawables.filter { visibleArea.translate(0, yOffset).overlaps(it.area) }) {
            if (isHovered && textToken is Link) {
                textToken.hovering(mouseX, mouseY, yOffset)
            }
            textToken.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        }
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return setChildrenPos(x, y, maxWidth)
    }

    override fun setChildrenPos(x: Int, y: Int, maxWidth: Int): Int {
        fun initLine(x: Int, y: Int, line: MutableList<DrawableWord>, justified: Boolean, maxWidth: Int): Int {
            var maxHeight = 0
            var remainder = 0
            val spacing = if (justified && line.size != 0) {
                val wordsWidth = (line.fold(0) { i, elem -> i + if (elem !is DrawableSpace) elem.area.width else 0 })
                val remainingSpace = floor(maxWidth - wordsWidth.toDouble());
                val numberSpaces = if (line.last() is DrawableSpace) line.count { it is DrawableSpace } - 1 else line.count { it is DrawableSpace }
                remainder = remainingSpace.rem(numberSpaces).toInt()
                floor(remainingSpace / numberSpaces.toDouble()).toInt()
            } else {
                GuiGuideBook.lpFontRenderer.getStringWidth(" ")
            }
            line.foldIndexed(x) { _, currX, drawableWord ->
                when (drawableWord) {
                    is DrawableSpace -> {
                        val currentSpacing = when  {
                            (drawableWord == line.last()) -> 0
                            remainder > 0 -> {
                                remainder--
                                spacing + 1
                            }
                            else -> spacing
                        }
                        drawableWord.setPos(currX, y, maxWidth)
                        drawableWord.setWidth(currentSpacing)
                    }
                    else -> {
                        drawableWord.setPos(currX, y, maxWidth)
                    }
                }
                maxHeight = maxOf(maxHeight, drawableWord.area.height)
                currX + drawableWord.area.width
            }
            return maxHeight
        }

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
                        if (currentDrawableWord !is DrawableSpace && currentWidth + currentDrawableWord.area.width > maxWidth) {
                            currentY += initLine(x, y + currentY, currentLine, true, maxWidth)
                            currentLine.clear()
                            currentWidth = 0
                        }
                        currentLine.add(currentDrawableWord)
                        currentWidth += currentDrawableWord.area.width + GuiGuideBook.lpFontRenderer.getStringWidth(" ")
                        if (currentDrawableWord == drawables.last()) currentY += initLine(x, y + currentY, currentLine, false, maxWidth)
                    }
                }
            }
            currentY += 1
            area.setSize(maxWidth, currentY)
        }
        return currentY
    }
}

/**
 * Image token, stores a token list in case the image is not correctly loaded as well as the image's path
 * @param textTokens this is the alt text, only used in case the image provided via the URL fails to load.
 *
 */
data class ImageParagraph(val textTokens: List<DrawableWord>, val imageParameters: String) : IDrawable {
    // TODO
    private val image: ResourceLocation
    private var imageAvailible: Boolean

    override val area = Rectangle(0, 0)
    override var isHovered = false

    init {
        val parameters = imageParameters.split(" ")
        image = ResourceLocation(LPConstants.LP_MOD_ID, parameters.first())
        imageAvailible = true
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return area.height
    }
}

/**
 * Menu token, stores the key and the type of menu in a page.
 */
data class MenuParagraph(val menuId: String, val options: String) : IDrawable {
    // TODO how to get the actual menu Map in here?
    override val area: Rectangle = Rectangle(0, 0)
    override var isHovered = false

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
    }

    private val menu = mutableMapOf<String, List<MenuParagraphTile>>()

    fun setContent(map: Map<String, List<String>>): MenuParagraph {
        menu.clear()
        menu.putAll(map.asSequence().associate { div -> div.key to div.value.map { page -> MenuParagraphTile(BookContents.get(page).metadata) } })
        return this
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        var currentX = 0
        var currentY = 0
        for (division in menu) {
            currentY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT
            for (tile in division.value) {
                // Checks if the tile fits in the current row, if not skips to next row
                if (currentX + tile.tileSize + tile.tileSpacing > maxWidth) {
                    currentX = 0
                    currentY += tile.tileSize + tile.tileSpacing
                }
                // Sets the position of the tile
                tile.setPos(x + currentX, y + currentY, maxWidth)
                // Checks if the tile is the last on in the current list, if so add the height and spacing for the next division to be correctly drawn
                if (tile == division.value.last()) {
                    currentY += tile.tileSize + tile.tileSpacing
                }
            }
        }
        area.setSize(maxWidth, currentY)
        TODO("Not yet implemented")
    }

    override fun hovering(mouseX: Int, mouseY: Int, yOffset: Int) {
        super.hovering(mouseX, mouseY, yOffset)
    }

    // Make a custom inner class for the title of a division?

    private class MenuParagraphTile(metadata: YamlPageMetadata) : IDrawable {
        val tileSize = 40
        val tileSpacing = 5

        // Maybe there needs to be a constant Int defining the size of all the tiles
        override val area = Rectangle(tileSize, tileSize)
        override var isHovered = false

        override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
            super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
            // Draw tile bg
            // Draw icon
            // Draw tooltip
        }

        override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
            area.setPos(x, y)
            return area.height
        }
    }
}

/**
 * List token, has several items that are shown in a list.
 */
data class ListParagraph(val entries: List<List<DrawableWord>>) : IDrawable {
    override val area: Rectangle
        get() = TODO("Not yet implemented")
    override var isHovered: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        TODO("Not yet implemented")
    }
}

/**
 * Normal Token that stores the text and the formatting tags of said text.
 */
open class DrawableWord(private val str: String, private val scale: Double, state: InlineDrawableState) : IDrawable {
    val format: EnumSet<TextFormat> = state.format
    val color: Int = state.color

    override val area = Rectangle(GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), scale), GuiGuideBook.lpFontRenderer.getFontHeight(scale))
    override var isHovered = false

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(0.1f, 0.1f, 0.1f)
        GuiGuideBook.lpFontRenderer.drawString(string = str, x = area.x0, y = area.y0, color = color, format = format, scale = scale)
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return (area.height * scale).toInt()
    }

    override fun toString(): String {
        return "\"$str\""
    }
}

/**
 * Space object responsible for drawing the necessary formatting in between words.
 */
class DrawableSpace(private val scale: Double, state: InlineDrawableState) : DrawableWord(" ", scale, state) {

    fun setWidth(width: Int) {
        area.width = width;
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return (area.height * scale).toInt()
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        if (DEBUG_AREAS) area.render(0.1f, 0.1f, 0.1f)
        if (area.width > 0) GuiGuideBook.lpFontRenderer.drawSpace(x = area.x0, y = area.y0, width = area.width, color = color, italic = format.italic(), underline = format.underline(), strikethrough = format.strikethrough(), shadow = format.shadow(), scale = scale)
    }

    override fun toString(): String {
        return "Space of size ${area.width} with formatting: $format."
    }
}

object DrawableBreak : DrawableWord("", 1.0, DEFAULT_DRAWABLE_STATE)

/**
 * Link token, stores the linked string, as well as the 'url'.
 */
data class Link(private val text: String) : DrawableWord(text, 1.0, DEFAULT_DRAWABLE_STATE)

private fun toDrawables(elements: List<InlineElement>, scale: Double) = DEFAULT_DRAWABLE_STATE.let { state ->
    elements.mapNotNull { element ->
        element.changeDrawableState(state)
        when (element) {
            is Word -> DrawableWord(element.str, scale, state)
            is Space -> DrawableSpace(scale, state)
            Break -> DrawableBreak
            else -> null
        }
    }
}

private fun toDrawable(paragraph: Paragraph): IDrawable = when (paragraph) {
    is RegularParagraph -> DrawableRegularParagraph(toDrawables(paragraph.elements, 1.0))
    is HeaderParagraph -> DrawableHeaderParagraph(toDrawables(paragraph.elements, HEADER_LEVELS[paragraph.headerLevel - 1]), paragraph.headerLevel)
    HorizontalLineParagraph -> TODO()
}

fun asDrawables(paragraphs: List<Paragraph>) = paragraphs.map(::toDrawable)
