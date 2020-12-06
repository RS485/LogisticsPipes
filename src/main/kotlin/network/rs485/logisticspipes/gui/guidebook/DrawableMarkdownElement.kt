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

import logisticspipes.LPConstants
import logisticspipes.utils.MinecraftColor
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.*
import java.util.*

const val DEBUG_AREAS = false

var definingPage = BookContents.get(MAIN_MENU_FILE)

internal val DEFAULT_DRAWABLE_STATE = InlineDrawableState(EnumSet.noneOf(TextFormat::class.java), MinecraftColor.WHITE.colorCode)
internal val HEADER_LEVELS = listOf(2.0, 1.80, 1.60, 1.40, 1.20, 1.00)

/**
 * Image token, stores a token list in case the image is not correctly loaded as well as the image's path
 * @param textTokens this is the alt text, only used in case the image provided via the URL fails to load.
 * TODO
 */
class DrawableImageParagraph(parent: Drawable, val textTokens: List<DrawableWord>, val imageParameters: String) : Drawable(parent) {
    private val image: ResourceLocation
    private var imageAvailable: Boolean

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        TODO("Not yet implemented")
    }

    init {
        val parameters = imageParameters.split(" ")
        image = ResourceLocation(LPConstants.LP_MOD_ID, parameters.first())
        imageAvailable = true
    }

    override fun setPos(x: Int, y: Int): Int {
        area.setPos(x, y)
        return super.setPos(x, y)
    }
}

/**
 * List token, has several items that are shown in a list.
 */
class DrawableListParagraph(parent: Drawable, val entries: List<List<DrawableWord>>) : DrawableParagraph(parent) {
    override fun setChildrenPos(): Int {
        TODO("Not yet implemented")
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        TODO("Not yet implemented")
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        TODO("Not yet implemented")
    }

    override fun setPos(x: Int, y: Int): Int {
        TODO("Not yet implemented")
    }
}

internal fun toDrawables(parent: Drawable, elements: List<InlineElement>, scale: Double) = DEFAULT_DRAWABLE_STATE.copy().let { state ->
    elements.mapNotNull { element ->
        element.changeDrawableState(state)
        when (element) {
            is Word -> DrawableWord(parent, element.str, scale, state)
            is Space -> DrawableSpace(parent, scale, state)
            Break -> DrawableBreak
            else -> null
        }
    }
}

private fun toDrawable(parent: Drawable, paragraph: Paragraph): Drawable = when (paragraph) {
    is RegularParagraph -> DrawableRegularParagraph(parent, paragraph.elements)
    is HeaderParagraph -> DrawableHeaderParagraph(parent, paragraph.elements, paragraph.headerLevel)
    is HorizontalLineParagraph -> DrawableHorizontalLine(parent, 2)
    is MenuParagraph -> DrawableMenuParagraph(parent, paragraph.description, definingPage.metadata.menu[paragraph.link]?: error("Requested menu ${paragraph.link}, not found.")) // TODO have the current page path here to get the proper menu
}

fun asDrawables(parent: Drawable, paragraphs: List<Paragraph>) = paragraphs.map { toDrawable(parent, it) }

fun getScaleFromLevel(headerLevel: Int) = if (headerLevel > 0 && headerLevel < HEADER_LEVELS.size) HEADER_LEVELS[headerLevel - 1] else 1.00
