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

import network.rs485.logisticspipes.gui.*
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.util.math.MutableRectangle

class LabelWidget(
    parent: Drawable,
    width: Int,
    xPosition: HorizontalAlignment,
    yPosition: VerticalAlignment,
    xSize: Size,
    margin: Margin,
    private var text: String,
    private val textColor: Int,
    private val textAlignment: HorizontalAlignment,
    private var extendable: Boolean,
    private var backgroundColor: Int,
) : LPGuiWidget(
    parent = parent,
    xPosition = xPosition,
    yPosition = yPosition,
    xSize = xSize,
    ySize = Size.FIXED,
    margin = margin,
), MouseHoverable, Tooltipped {

    private var overflowing: Boolean = false

    override val minWidth: Int = when (xSize) {
        Size.FIXED -> {
            width
        }

        Size.MIN -> {
            text.width() + 2
        }

        Size.GROW -> {
            30
        }
    }

    override val minHeight: Int = GuiDrawer.mcFontRenderer.FONT_HEIGHT + 1

    override val maxWidth: Int = parent.height
    override val maxHeight: Int = parent.height

    private var trimmedText = trimText(text)
    private val fullBody = MutableRectangle()

    override fun initWidget() {
        setSize(minWidth, minHeight)
        updateConstraints()
    }

    fun updateText(newText: String) {
        text = newText
        updateConstraints()
    }

    private fun updateConstraints() {
        trimmedText = trimText(text)
        fullBody.setPosFromRectangle(absoluteBody).translate(translateY = -2).grow(1)
        fullBody.setSize(text.width(), minHeight)
        overflowing = text != trimmedText
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        val hovering = isMouseHovering(mouseX, mouseY)
        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        if (hovering && overflowing && extendable) {
            GuiDrawer.drawRect(fullBody.translated(absoluteBody), backgroundColor)
        }
        val (effectiveAlignment, effectiveText) = if (overflowing) {
            HorizontalAlignment.LEFT to if (hovering && extendable) text else trimmedText
        } else {
            textAlignment to text
        }
        GuiDrawer.mcFontRenderer.drawString(
            effectiveText,
            when (effectiveAlignment) {
                HorizontalAlignment.LEFT -> absoluteBody.roundedLeft
                HorizontalAlignment.CENTER -> absoluteBody.roundedLeft + (width - effectiveText.width()) / 2
                HorizontalAlignment.RIGHT -> absoluteBody.roundedRight - effectiveText.width()
            },
            absoluteBody.roundedY,
            textColor
        )
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }

    private fun trimText(text: String): String {
        return TextUtil.getTrimmedString(text, width, GuiDrawer.mcFontRenderer)
    }

    override fun setSize(newWidth: Int, newHeight: Int) {
        relativeBody.setSize(newWidth, newHeight)
        updateConstraints()
    }

    override fun getTooltipText(): List<String> = if (overflowing && !extendable) {
        listOf(text)
    } else {
        emptyList()
    }

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = absoluteBody.contains(mouseX, mouseY)

    private fun String.width() = GuiDrawer.mcFontRenderer.getStringWidth(this)

    override fun toString(): String {
        return "LabelWidget: $text, $absoluteBody"
    }
}
