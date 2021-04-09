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

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.util.math.Rectangle

open class LPGuiButton2(id: Int, x: Int, y: Int, width: Int, height: Int) : GuiButton(id, 24, 24, "") {
    val body = Rectangle(x, y, width, height)

    // Position relative to body.
    open val bodyTrigger = Rectangle(width, height)
    private var onClickAction: ((Int) -> Boolean)? = null

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean = isHovered(mouseX, mouseY)

    override fun isMouseOver(): Boolean = hovered

    override fun getButtonWidth(): Int = body.roundedWidth

    override fun setWidth(width: Int) {
        body.setSize(newWidth = width, body.roundedHeight)
    }

    override fun getHoverState(mouseOver: Boolean): Int = if (!enabled) 2 else if (hovered) 1 else 0

    internal fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        enabled && visible && bodyTrigger.translated(body.x0, body.y0).contains(mouseX, mouseY)

    open fun setPos(newX: Int, newY: Int) {
        body.setPos(newX, newY)
    }

    open fun setOnClickAction(newOnClickAction: (Int) -> Boolean): LPGuiButton2 {
        onClickAction = newOnClickAction
        return this
    }

    open fun click(mouseButton: Int): Boolean = onClickAction?.invoke(mouseButton) ?: false

    open fun getTooltipText(): String = ""

    open fun drawTooltip(x: Int, y: Int, horizontalAlign: HorizontalAlignment, verticalAlign: VerticalAlignment){
        getTooltipText().let { tooltipText ->
            if(tooltipText.isNotBlank()){
                GuiGuideBook.drawBoxedString(getTooltipText(), x, y, GuideBookConstants.Z_TOOLTIP, horizontalAlign, verticalAlign)
            }
        }
    }
}