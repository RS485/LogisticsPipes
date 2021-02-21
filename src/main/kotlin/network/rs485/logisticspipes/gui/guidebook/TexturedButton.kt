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

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook.Companion.drawStretchingRectangle
import network.rs485.logisticspipes.util.math.Rectangle

class TexturedButton(buttonId: Int, x: Int, y: Int, widthIn: Int, heighIn: Int, z: Double, u: Int, v: Int, val hasDisabledState: Boolean, val type: ButtonType) : GuiButton(buttonId, x, y, widthIn, heighIn, "") {
    private val buttonOverlayArea = Rectangle()
    private val buttonOverlayTextureArea = Rectangle()
    private val buttonArea = Rectangle(x, y, width, height)
    private val buttonTextureArea = Rectangle(u, v, width, height)
    private lateinit var onHoverTextGetter: () -> String

    init {
        zLevel = z.toFloat()
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        hovered = buttonArea.contains(mouseX, mouseY)
        val j = if (hasDisabledState && !enabled) 2 else if (hovered) 1 else 0
        if(::onHoverTextGetter.isInitialized && hovered) {
            GuiGuideBook.drawBoxedString(
                text = onHoverTextGetter(),
                x = buttonArea.x1,
                y = buttonArea.y0,
                z = GuideBookConstants.Z_TOOLTIP,
                horizontalAlign = GuiGuideBook.HorizontalAlignement.RIGHT,
                verticalAlign = GuiGuideBook.VerticalAlignement.BOTTOM
            )
        }
        when (type) {
            ButtonType.TAB -> {
                drawStretchingRectangle(buttonArea.x0, buttonArea.y0, buttonArea.x1, buttonArea.y1, zLevel.toDouble(), buttonTextureArea.x0, buttonTextureArea.y0, buttonTextureArea.x1, buttonTextureArea.y1, false)
                if (buttonOverlayTextureArea.width > 0 && buttonOverlayTextureArea.height > 0) {
                    drawStretchingRectangle(buttonOverlayArea.x0, buttonOverlayArea.y0, buttonOverlayArea.x1, buttonOverlayArea.y1, zLevel.toInt() + 1.toDouble(), buttonOverlayTextureArea.x0, buttonOverlayTextureArea.y0 + buttonOverlayTextureArea.height * j, buttonOverlayTextureArea.x1, buttonOverlayTextureArea.y1 + buttonOverlayTextureArea.height * j, false)
                }
            }
            ButtonType.NORMAL -> {
                GlStateManager.enableAlpha()
                drawStretchingRectangle(buttonArea.x0, buttonArea.y0, buttonArea.x1, buttonArea.y1, zLevel.toInt() + 1.toDouble(), buttonTextureArea.x0, buttonTextureArea.y0 + buttonTextureArea.height * j, buttonTextureArea.x1, buttonTextureArea.y1 + buttonTextureArea.height * j, false)
                GlStateManager.disableAlpha()
            }
        }
    }

    fun setBackgroundTexture(u: Int, v: Int): TexturedButton {
        buttonTextureArea.setPos(u, v)
        return this
    }

    fun setOverlayTexture(u: Int, v: Int, size: Int): TexturedButton {
        return setOverlayTexture(u, v, size, size)
    }

    fun setOnHoverTextGetter(newOnHoverTextGetter: () -> String): TexturedButton{
        onHoverTextGetter = newOnHoverTextGetter
        return this
    }

    fun setOverlayTexture(u: Int, v: Int, w: Int, h: Int): TexturedButton {
        buttonOverlayTextureArea.setPos(u, v).setSize(w, h)
        buttonOverlayArea.setPos(buttonArea.x0, buttonArea.y0).translate((buttonArea.width - buttonOverlayTextureArea.width) / 2, (buttonArea.height - buttonOverlayTextureArea.height) / 2).setSize(w, h)
        return this
    }

    fun setX(x: Int) {
        super.x = x
        val xOffset = x - buttonArea.x0
        buttonArea.translate(xOffset, 0)
        buttonOverlayArea.translate(xOffset, 0)
    }
}

enum class ButtonType {
    TAB,
    NORMAL
}