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

import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.util.math.Rectangle

class TabButton(buttonId: Int, x: Int, y: Int, val tab: SavedPage): GuiButton(buttonId, 24, 24, "") {
    
    // TODO look into making the TexturedButton abstract and making this and a Home button extend it

    var isActive = false
    private val buttonArea = Rectangle(x, y, 24, 24)

    init {
        visible = true
    }

    fun cycleColor() {
        if (isActive) return
        tab.cycleColor(false)
        playPressSound(Minecraft.getMinecraft().soundHandler)
    }

    fun cycleColorInverted() {
        if (isActive) return
        tab.cycleColor(true)
        playPressSound(Minecraft.getMinecraft().soundHandler)
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        hovered = buttonArea.contains(mouseX, mouseY)
        if (!visible) return
        mc.textureManager.bindTexture(GuiGuideBook.GUI_BOOK_TEXTURE)
        var drawHeight = buttonArea.height
        var drawY = buttonArea.y0
        if (!hovered && !isActive) {
            drawHeight -= 2
        } else if (isActive) {
            drawHeight += 3
            drawY += 3
        }
        GuiGuideBook.drawStretchingRectangle(buttonArea.x0, drawY - drawHeight, buttonArea.x1, drawY, zLevel.toDouble(), 40.0, 64.0, 40 + buttonArea.width.toDouble(), 64 + drawHeight.toDouble(), true, if (isActive) 0xFFFFFF else MinecraftColor.values()[tab.color].colorCode)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }
}