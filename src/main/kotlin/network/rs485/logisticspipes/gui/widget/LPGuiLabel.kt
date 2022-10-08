/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

package network.rs485.logisticspipes.gui.widget

import logisticspipes.utils.MinecraftColor
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.gui.LPBaseGuiContainer.Companion.helper
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.util.math.MutableRectangle

class LPGuiLabel(
    parent: Drawable,
    xPosition: HorizontalAlignment,
    yPosition: VerticalAlignment,
    xSize: HorizontalSize,
    margin: Margin,
    text: String,
    private val textColor: Int = MinecraftColor.WHITE.colorCode
) : LPGuiWidget(
    parent = parent,
    xPosition = xPosition,
    yPosition = yPosition,
    xSize = xSize,
    ySize = Fixed,
    margin = margin,
), MouseHoverable {
    private var _text: String = text

    override val minWidth: Int = 50
    override val minHeight: Int = helper.mcFontRenderer.FONT_HEIGHT + 1

    override val maxWidth: Int = Int.MAX_VALUE
    override val maxHeight: Int = minHeight

    var text: String
        get() = _text
        set(value) {
            _text = value
            extendedBody.setSize(value.width(), minHeight)
            setTextAlignment(alignment)
            trimmedText = trimText(value)
        }

    private val extendedBody = MutableRectangle(
        x = absoluteBody.roundedX,
        y = absoluteBody.roundedY - 1,
        width = _text.width() + 1,
        height = helper.mcFontRenderer.FONT_HEIGHT + 1,
    )
    private var drawXOffset = 0
    private var extendable = false
    private var trimmedText = trimText(_text)
    private var alignment: HorizontalAlignment = HorizontalAlignment.LEFT
    private var backgroundColor = helper.BACKGROUND_LIGHT

    override fun initWidget() {
        setSize(minWidth, minHeight)
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        val hovering = isMouseHovering(mouseX, mouseY)
        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        if (hovering) helper.drawRect(extendedBody.translated(absoluteBody), backgroundColor)
        helper.mcFontRenderer.drawString(if (hovering) text else trimmedText, absoluteBody.roundedX + drawXOffset, absoluteBody.roundedY, textColor)
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }

    private fun trimText(text: String): String {
        return TextUtil.getTrimmedString(text, width, helper.mcFontRenderer)
    }

    fun setExtendable(newExtendable: Boolean, newBackgroundColor: Int): LPGuiLabel {
        extendable = newExtendable
        backgroundColor = newBackgroundColor
        return this
    }

    override fun setSize(newWidth: Int, newHeight: Int) {
        relativeBody.setSize(newWidth, newHeight)
        text = _text
    }

    fun setTextAlignment(newAlignment: HorizontalAlignment) {
        // FIXME: does not remember original alignment and always overrides it when text width is > width
        alignment = if (text.width() > width) {
            HorizontalAlignment.LEFT
        } else {
            newAlignment
        }
        drawXOffset = when (alignment) {
            HorizontalAlignment.CENTER -> (width - text.width()) / 2
            HorizontalAlignment.LEFT -> 0
            HorizontalAlignment.RIGHT -> width - text.width()
        }
    }

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = absoluteBody.contains(mouseX, mouseY)

    private fun String.width() = helper.mcFontRenderer.getStringWidth(this)

    override fun toString(): String {
        return "LabelWidget: $_text, $absoluteBody"
    }
}
