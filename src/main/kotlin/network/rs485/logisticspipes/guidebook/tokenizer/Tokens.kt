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

import logisticspipes.LPConstants
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.gui.guidebook.IDrawable
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.YamlPageMetadata
import network.rs485.logisticspipes.util.math.Rectangle

sealed class GenericToken : IDrawable
//sealed class GenericTextToken : GenericToken()
sealed class GenericParagraphToken : GenericToken()

const val DEBUG_AREAS = false

/**
 * Stores groups of ITokenText tokens to more easily translate Tokens to Drawable elements
 */
data class TextParagraph(val textTokens: List<Text>) : GenericParagraphToken() {
    override val area = Rectangle(0, 0)
    override var isHovered = false

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(1.0F, 0.0F, 0.0F)
        for (textToken in textTokens.filter { visibleArea.translate(0, yOffset).overlaps(it.area) }) {
            if (isHovered && textToken is Link) {
                textToken.hovering(mouseX, mouseY, yOffset)
            }
            textToken.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        }
    }

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        var currentX = 1
        var currentY = 1
        for (textToken in textTokens) {
            when (textToken) {
                is Text, is Link -> {
                    if ((currentX + textToken.area.width) > maxWidth) {
                        currentX = 0
                        currentY += textToken.area.height
                    }
                }
                is Break -> {
                    currentX = 0
                    currentY += textToken.area.height
                }
            }
            textToken.init(currentX + x, currentY + y, maxWidth - 2)
            currentX += textToken.area.width
            if (textToken == textTokens.last()) currentY += textToken.area.height
        }
        currentY += 1
        area.setSize(maxWidth, currentY)
        return currentY
    }
}

/**
 * Header token, stores all the tokens that are apart of the header.
 */
data class HeaderParagraph(val textTokens: List<Text>, val headerLevel: Int) : GenericParagraphToken() {
    private val LEVELS = listOf(2.0, 1.75, 1.5, 1.25, 1.0)
    override val area = Rectangle(0, 0)
    override var isHovered = true

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (DEBUG_AREAS) area.render(0.0F, 1.0F, 0.0F)
        for (textToken in textTokens.filter { visibleArea.translate(0, yOffset).overlaps(it.area) }) {
            if (isHovered && textToken is Link) {
                textToken.hovering(mouseX, mouseY, yOffset)
            }
            textToken.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        }
    }

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        var currentX = 0
        var currentY = 0
        for (textToken in textTokens) {
            textToken.scale = LEVELS[headerLevel - 1]
            when (textToken) {
                is Link -> {
                    if ((currentX + textToken.area.width) > maxWidth - 2) {
                        currentX = 0
                        currentY += textToken.area.height
                    }
                }
                is Break -> {
                    currentX = 0
                    currentY += textToken.area.height
                }
            }
            textToken.init(currentX + x + 1, currentY + y + 1, maxWidth - 2)
            currentX += textToken.area.width
            if (textToken == textTokens.last()) currentY += textToken.area.height
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
data class ImageParagraph(val textTokens: List<Text>, val imageParameters: String) : GenericParagraphToken() {
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

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return area.height
    }
}

/**
 * Menu token, stores the key and the type of menu in a page.
 */
data class MenuParagraph(val menuId: String, val options: String) : GenericParagraphToken() {
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

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
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
                tile.init(x + currentX, y + currentY, maxWidth)
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

        override fun init(x: Int, y: Int, maxWidth: Int): Int {
            area.setPos(x, y)
            return area.height
        }
    }
}

/**
 * List token, has several items that are shown in a list.
 */
data class ListParagraph(val entries: List<List<Text>>) : GenericParagraphToken() {
    override val area: Rectangle
        get() = TODO("Not yet implemented")
    override var isHovered: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
        TODO("Not yet implemented")
    }
}

/**
 * Normal Token that stores the text and the formatting tags of said text.
 */
open class Text(private val str: String, private val tags: List<Tokenizer.TokenTag>, val color: Int = MinecraftColor.WHITE.colorCode) : GenericToken() {
    override val area = Rectangle(1, 1)
    override var isHovered = false
    var scale: Double = 1.0
        set(value) {
            area.setSize(GuiGuideBook.lpFontRenderer.getStringWidth(str, tags.contains(Tokenizer.TokenTag.Italic), tags.contains(Tokenizer.TokenTag.Bold), value), GuiGuideBook.lpFontRenderer.getFontHeight(value))
            field = value
        }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        val color = if (isHovered) MinecraftColor.BLUE.colorCode else this.color
        val r = ((color shr 16) and 0xff) / 255.0F
        val g = ((color shr 8) and 0xff) / 255.0F
        val b = (color and 0xff) / 1.0F
        if (DEBUG_AREAS) area.render(r, g, b)
        GuiGuideBook.lpFontRenderer.drawString(str, area.x0, area.y0, color, tags, scale)
    }

    override fun init(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        this.scale = scale
        return (area.height * scale).toInt()
    }
}

/**
 * Line break token, simply represents a line break in the text.
 */
object Break : Text("", emptyList())

/**
 * Link token, stores the linked string, as well as the 'url'.
 */
data class Link(val text: String, val linkUrl: String) : Text(text, listOf(Tokenizer.TokenTag.Underline), MinecraftColor.LIGHT_BLUE.colorCode)
