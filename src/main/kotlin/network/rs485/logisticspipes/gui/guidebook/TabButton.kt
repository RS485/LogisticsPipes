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
import network.rs485.logisticspipes.util.math.Rectangle

interface TabButtonReturn {
    fun onLeftClick(): Boolean
    fun onRightClick(shiftClick: Boolean, ctrlClick: Boolean): Boolean
    fun getColor(): Int
    fun isPageActive(): Boolean
    fun getOnHoverText(): String
}

class TabButton(x: Int, yBottom: Int, private val whisky: TabButtonReturn) : GuiButton(99, 24, 24, "") {
    private val buttonArea = Rectangle(x, yBottom - 24, 24, 32)
    private val buttonTextureArea = Rectangle(40, 64, 24, 32)
    private val circleArea = Rectangle(buttonArea.x0 + 4, buttonArea.y0 + 4, 16, 16)
    private val circleAreaTexture = Rectangle(32, 96, 16, 16)

    init {
        visible = true
    }

    fun onLeftClick() = whisky.onLeftClick()

    fun onRightClick(shiftClick: Boolean, ctrlClick: Boolean) = whisky.onRightClick(shiftClick, ctrlClick)
    
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        hovered = buttonArea.contains(mouseX, mouseY)
        if (!visible) return
        mc.textureManager.bindTexture(GuideBookConstants.guiBookTexture)
        val z = if (whisky.isPageActive()) GuideBookConstants.Z_FRAME else GuideBookConstants.Z_BACKGROUND
        val yOffset = if (whisky.isPageActive()) 0 else 3
        val color: Int = (MinecraftColor.values()[whisky.getColor()].colorCode and 0x00FFFFFF) or 0xFF000000.toInt()
        if(hovered) GuiGuideBook.drawBoxedString(
            text = whisky.getOnHoverText(),
            x = buttonArea.x1,
            y = buttonArea.y0,
            z = GuideBookConstants.Z_TOOLTIP,
            horizontalAlign = GuiGuideBook.HorizontalAlignement.RIGHT,
            verticalAlign = GuiGuideBook.VerticalAlignement.BOTTOM
        )
        GuiGuideBook.drawStretchingRectangle(
            x0 = buttonArea.x0,
            y0 = buttonArea.y0 + yOffset,
            x1 = buttonArea.x1,
            y1 = buttonArea.y1 + yOffset,
            z = z,
            u0 = buttonTextureArea.x0,
            v0 = buttonTextureArea.y0,
            u1 = buttonTextureArea.x1,
            v1 = buttonTextureArea.y1,
            blend = true,
            color = if (whisky.isPageActive()) 0xFFFFFFFF.toInt() else color
        )
        if (whisky.isPageActive()) {
            GuiGuideBook.drawStretchingRectangle(
                x0 = circleArea.x0,
                y0 = circleArea.y0,
                x1 = circleArea.x1,
                y1 = circleArea.y1,
                z = z + 0.5,
                u0 = circleAreaTexture.x0,
                v0 = circleAreaTexture.y0,
                u1 = circleAreaTexture.x1,
                v1 = circleAreaTexture.y1,
                blend = true,
                color = color
            )
        }
    }

    fun setPos(x0: Int, y0: Int){
        buttonArea.setPos(x0, y0 - 24)
        circleArea.setPos(buttonArea.x0 + 4, buttonArea.y0 + 4,)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return if (whisky.isPageActive()) Rectangle.fromRectangle(buttonArea)
            .setSize(32, 32)
            .contains(mouseX, mouseY)
        else Rectangle(buttonArea.x0, buttonArea.y0 + 3, 32, 29).contains(mouseX, mouseY)
    }
}