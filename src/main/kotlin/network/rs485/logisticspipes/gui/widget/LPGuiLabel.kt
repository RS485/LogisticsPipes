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
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPBaseGuiContainer.Companion.helper
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.util.math.Rectangle

class LPGuiLabel(
        parent: Drawable,
        xPosition: HorizontalPosition,
        yPosition: VerticalPosition,
        xSize: HorizontalSize,
        private val textGetter: () -> String,
        private val textColor: Int = MinecraftColor.WHITE.colorCode) : LPGuiWidget
(
        parent = parent,
        xPosition = xPosition,
        yPosition = yPosition,
        xSize = xSize,
        ySize = AbsoluteSize(helper.mcFontRenderer.FONT_HEIGHT)
) , MouseHoverable {

    private var text: String = textGetter()
    private val textArea = Rectangle(relativeBody.roundedX, relativeBody.roundedY - 1, helper.mcFontRenderer.getStringWidth(text) + 1, helper.mcFontRenderer.FONT_HEIGHT + 1)
    private var drawXOffset = 0
    private var extendable = false
    private var trimmedText = TextUtil.getTrimmedString(text, width, helper.mcFontRenderer)
    private var alignment = HorizontalAlignment.LEFT
    private var backgroundColor = helper.BACKGROUND_LIGHT

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        val hovering = isMouseHovering(mouseX, mouseY)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, z)
        GlStateManager.enableDepth()
        if (hovering) helper.drawRect(textArea.translated(drawXOffset, 0), -5f, backgroundColor)
        helper.mcFontRenderer.drawString(if (hovering) text else trimmedText, relativeBody.roundedX + drawXOffset, relativeBody.roundedY, textColor)
        GlStateManager.disableDepth()
        GlStateManager.translate(0f, 0f, -z)
        GlStateManager.popMatrix()
    }

    fun updateText(): LPGuiLabel {
        text = textGetter()
        trimmedText = TextUtil.getTrimmedString(text, width, helper.mcFontRenderer)
        textArea.setSize(helper.mcFontRenderer.getStringWidth(text), helper.mcFontRenderer.FONT_HEIGHT + 1)
        return setAlignment(alignment)
    }

    fun setExtendable(newExtendable: Boolean, newBackgroundColor: Int): LPGuiLabel {
        extendable = newExtendable
        backgroundColor = newBackgroundColor
        return this
    }

    fun setAlignment(newAlignment: HorizontalAlignment): LPGuiLabel {
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
        return this
    }

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = relativeBody.contains(mouseX, mouseY)

    private fun String.width() = helper.mcFontRenderer.getStringWidth(this)
}