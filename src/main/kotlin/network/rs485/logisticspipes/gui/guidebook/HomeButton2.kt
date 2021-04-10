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

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.util.math.Rectangle

private val homeButtonTexture = Rectangle(16, 64, 24, 32)
private val homeIconTexture = Rectangle(128, 0, 16, 16)

/*
* Position on the button is set based on it's rightmost and where it needs to connect at the bottom.
*/
class HomeButton2(x: Int, y: Int, onClickAction: (Int) -> Boolean) : LPGuiButton2(1, x - 24, y - 24, homeButtonTexture.roundedWidth, homeButtonTexture.roundedHeight) {
    private val homeIconBody: Rectangle
    override val bodyTrigger = Rectangle(1, 1, 22, 22)

    init {
        this.setOnClickAction(onClickAction)
        zLevel = GuideBookConstants.Z_TITLE_BUTTONS
        val offset = (body.width - homeIconTexture.width) / 2
        homeIconBody = Rectangle(offset, offset, homeIconTexture.width, homeIconTexture.height)
    }

    override fun setPos(newX: Int, newY: Int) {
        body.setPos(newX - 24, newY + 8)
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (this.visible) {
            hovered = isHovered(mouseX, mouseY)
            if (hovered) {
                drawTooltip(
                    x = body.roundedRight,
                    y = body.roundedTop,
                    horizontalAlign = HorizontalAlignment.RIGHT,
                    verticalAlign = VerticalAlignment.BOTTOM
                )
            }
            GuiGuideBook.drawStretchingRectangle(body, zLevel, homeButtonTexture, false, MinecraftColor.WHITE.colorCode)
            drawButtonForegroundLayer(mouseX, mouseY)
        }
    }

    override fun getTooltipText(): String {
        return TextUtil.translate("misc.guide_book.home_button")
    }

    override fun drawButtonForegroundLayer(mouseX: Int, mouseY: Int) {
        val hoverStateOffset = getHoverState(hovered) * homeIconTexture.roundedHeight
        GuiGuideBook.drawStretchingRectangle(homeIconBody.translated(body), zLevel, homeIconTexture.translated(0, hoverStateOffset), false, MinecraftColor.WHITE.colorCode)
    }
}
