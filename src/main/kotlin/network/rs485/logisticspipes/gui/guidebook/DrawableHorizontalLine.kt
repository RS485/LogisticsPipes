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

import logisticspipes.LogisticsPipes
import logisticspipes.utils.MinecraftColor
import network.rs485.logisticspipes.util.math.Rectangle

/**
 * This draws a line with a given thickness that will span the entire width of the page, minus padding.
 */

data class DrawableHorizontalLine(override val parent: IDrawable, val thickness: Int, val padding: Int = 3, val color: Int = MinecraftColor.WHITE.colorCode) : IDrawable {
    override var hovered = false
    override var x = 0
    override var y = 0
    override var width = 0
    override var height = 2 * padding + thickness

    override fun setPos(x: Int, y: Int): Int {
        this.x = x + padding
        this.y = y + padding
        width = parent.width - 2 * padding
        LogisticsPipes.log.error("LINE PARAGRAPH SET AT: $x, $y, $width, $height")
        return super.setPos(x, y)
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        LogisticsPipes.log.warn("Drawing LP at: ${left()} → ${right()} = $width")
        GuiGuideBook.drawHorizontalLine(left(), right(), top(), 5.0, thickness, color)
    }
}