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
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.gui.guidebook.IDrawable
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import network.rs485.markdown.MarkdownParser.splitToInlineElements
import java.util.*

const val DEBUG_AREAS = false

internal val DEFAULT_DRAWABLE_STATE = InlineDrawableState(EnumSet.noneOf(TextFormat::class.java), MinecraftColor.WHITE.colorCode)
internal val HEADER_LEVELS = listOf(2.0, 1.80, 1.60, 1.40, 1.20, 1.00)

/**
 * Image token, stores a token list in case the image is not correctly loaded as well as the image's path
 * @param textTokens this is the alt text, only used in case the image provided via the URL fails to load.
 *
 */
data class DrawableImageParagraph(val textTokens: List<DrawableWord>, val imageParameters: String) : IDrawable {
    // TODO
    private val image: ResourceLocation
    private var imageAvailable: Boolean

    override val area = Rectangle(0, 0)
    override var isHovered = false

    init {
        val parameters = imageParameters.split(" ")
        image = ResourceLocation(LPConstants.LP_MOD_ID, parameters.first())
        imageAvailable = true
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return area.height
    }
}

/**
 * This draws a line with a given thickness that will span the entire width of the page, minus padding.
 */

data class DrawableHorizontalLine(val thickness: Int, val padding: Int = 3) : IDrawable {
    override val area = Rectangle(0, 2 * padding + thickness)
    override var isHovered = false

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        area.setSize(maxWidth, area.height)
        return area.height + padding
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        if(DEBUG_AREAS) area.translated(0, -yOffset).render(0.0f, 0.0f, 0.0f)
        if(visibleArea.overlaps(area.translated(0, -yOffset))) GuiGuideBook.drawHorizontalLine(area.x0 + padding, area.x1 - padding, area.y0 + padding - yOffset, 5.0, thickness, MinecraftColor.WHITE.colorCode)
    }
}

/**
 * List token, has several items that are shown in a list.
 */
data class DrawableListParagraph(val entries: List<List<DrawableWord>>) : IDrawable {
    override val area: Rectangle
        get() = TODO("Not yet implemented")
    override var isHovered: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        TODO("Not yet implemented")
    }
}

private fun toDrawables(elements: List<InlineElement>, scale: Double) = DEFAULT_DRAWABLE_STATE.copy().let { state ->
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
    is HeaderParagraph -> DrawableHeaderParagraph(toDrawables(paragraph.elements, getScaleFromLevel(paragraph.headerLevel)), paragraph.headerLevel)
    is HorizontalLineParagraph -> DrawableHorizontalLine(2)
    is MenuParagraph -> DrawableMenuParagraph(toDrawables(splitToInlineElements(paragraph.description), getScaleFromLevel(3)), toMenuGroups(BookContents.get(GuiGuideBook.currentPage.page).metadata.menu[paragraph.link] ?: error("Requested menu ${paragraph.link}, not found.")))
}

fun toMenuGroups(groups: Map<String, List<String>>): List<DrawableMenuTileGroup> {
    return groups.map {
        DrawableMenuTileGroup(toDrawables(splitToInlineElements(it.key), getScaleFromLevel(6)), toMenuTiles(it.value))
    }
}

fun toMenuTiles(pages: List<String>): List<DrawableMenuTile> {
    return pages.map {
        DrawableMenuTile(BookContents.get(it).metadata)
    }
}

fun asDrawables(paragraphs: List<Paragraph>) = paragraphs.map(::toDrawable)

fun getScaleFromLevel(headerLevel: Int) = if (headerLevel > 0 && headerLevel < HEADER_LEVELS.size) HEADER_LEVELS[headerLevel - 1] else 1.00
