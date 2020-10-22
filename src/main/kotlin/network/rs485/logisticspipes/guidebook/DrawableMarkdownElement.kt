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
import network.rs485.logisticspipes.util.blueF
import network.rs485.logisticspipes.util.greenF
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.logisticspipes.util.redF
import network.rs485.markdown.*
import java.util.*

const val DEBUG_AREAS = false

private val DEFAULT_DRAWABLE_STATE = InlineDrawableState(EnumSet.noneOf(TextFormat::class.java), MinecraftColor.WHITE.colorCode)

/**
 * Stores groups of ITokenText tokens to more easily translate Tokens to Drawable elements
 */
data class DrawableRegularParagraph(val drawables: List<DrawableWord>) : IDrawable {
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
        fun initLine(x: Int, y: Int, line: MutableList<DrawableWord>, justified: Boolean, maxWidth: Int): Int {
            var maxHeight = 0
            val spacing = if (justified && line.size != 0) {
                (maxWidth - (line.fold(0) { i, elem -> i + elem.area.width })) / line.size
            } else {
                GuiGuideBook.lpFontRenderer.getStringWidth(" ")
            }
            line.foldIndexed(x) { index, currX, drawableWord ->
                when (drawableWord) {
                    is DrawableSpace -> {
                        drawableWord.init(currX, y, maxWidth, index != line.lastIndex, spacing)
                    }
                    else -> {
                        drawableWord.init(currX, y, maxWidth)
                    }
                }
                maxHeight = maxOf(maxHeight, drawableWord.area.height)
                if (!GuiGuideBook.usableArea.contains(drawableWord.area)) println("Is not contained in: ${GuiGuideBook.usableArea}!")
                println("Initialized: $drawableWord at ${drawableWord.area}")
                currX + drawableWord.area.width
            }
            return maxHeight
        }

        area.setPos(x, y)
        var currentY = 1
        var currentWidth = 0
        if (maxWidth > 0) {
            val currentLine = mutableListOf<DrawableWord>()
            for (currentDrawableWord in drawables) {
                currentDrawableWord.scale = 1.0
                when (currentDrawableWord) {
                    is DrawableBreak -> {
                        currentLine.add(currentDrawableWord)
                        currentY += initLine(x, y + currentY, currentLine, false, maxWidth)
                        currentLine.clear()
                        currentWidth = 0
                    }
                    else -> {
                        if (currentWidth + currentDrawableWord.area.width > maxWidth) {
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
data class DrawableHeaderParagraph(val drawables: List<DrawableWord>, val headerLevel: Int = 1) : IDrawable {
    private val LEVELS = listOf(2.0, 1.75, 1.5, 1.25, 1.0)
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
        var currentX = 0
        var currentY = 0
        for (textToken in drawables) {
            textToken.scale = LEVELS[headerLevel - 1]
            when (textToken) {
                is Link -> {
                    if ((currentX + textToken.area.width) > maxWidth - 2) {
                        currentX = 0
                        currentY += textToken.area.height
                    }
                }
                is DrawableBreak -> {
                    currentX = 0
                    currentY += textToken.area.height
                }
            }
            textToken.setPos(currentX + x + 1, currentY + y + 1, maxWidth - 2)
            currentX += textToken.area.width
            if (textToken == drawables.last()) currentY += textToken.area.height
        }
        currentY += 2
        area.setSize(maxWidth, currentY)
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
open class DrawableWord(private val str: String, state: InlineDrawableState) : IDrawable {
    val format: EnumSet<TextFormat> = state.format
    val color: Int = state.color

    override val area = Rectangle(GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), 1.0), GuiGuideBook.lpFontRenderer.getFontHeight(1.0))
    override var isHovered = false

    var scale: Double = 1.0
        set(value) {
            area.setSize(GuiGuideBook.lpFontRenderer.getStringWidth(str, format.italic(), format.bold(), value), GuiGuideBook.lpFontRenderer.getFontHeight(value))
            field = value
        }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(redF(color), greenF(color), blueF(color))
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
class DrawableSpace(state: InlineDrawableState) : DrawableWord(" ", state) {
    private var drawn: Boolean = format.isNotEmpty()

    fun init(x: Int, y: Int, maxWidth: Int, lastOfLine: Boolean, width: Int): Int {
        drawn = drawn.and(!lastOfLine)
        area.setSize(width, 1)
        return super.init(x, y, maxWidth)
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        if (drawn) {
            GuiGuideBook.lpFontRenderer.drawSpace(this)
        }
    }

    override fun toString(): String {
        return "Space of size ${area.width} with formatting: $format. Space! I'm in space. SPAAAAAACE!"
    }
}

object DrawableBreak : DrawableWord("", DEFAULT_DRAWABLE_STATE)

/**
 * Link token, stores the linked string, as well as the 'url'.
 */
data class Link(private val text: String) : DrawableWord(text, DEFAULT_DRAWABLE_STATE)

private fun toDrawables(elements: List<InlineElement>) = DEFAULT_DRAWABLE_STATE.let { state ->
    elements.mapNotNull { element ->
        element.changeDrawableState(state)
        when (element) {
            is Word -> DrawableWord(element.str, state)
            Space -> DrawableSpace(state)
            Break -> DrawableBreak
            else -> null
        }
    }
}

private fun toDrawable(paragraph: Paragraph): IDrawable = when (paragraph) {
    is RegularParagraph -> DrawableRegularParagraph(toDrawables(paragraph.elements))
    is HeaderParagraph -> DrawableHeaderParagraph(toDrawables(paragraph.elements), paragraph.headerLevel)
    HorizontalLineParagraph -> TODO()
}

fun asDrawables(paragraphs: List<Paragraph>) = paragraphs.map(::toDrawable)
