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

package network.rs485.logisticspipes.gui

import logisticspipes.utils.MinecraftColor
import logisticspipes.utils.string.StringUtils
import network.rs485.logisticspipes.util.math.Rectangle

class LPGuiLabel(private var text: String, val x: Int, private val y: Int, private val width: Int, private val textColor: Int = MinecraftColor.WHITE.colorCode) : LPGuiWidget() {

    override val area: Rectangle = Rectangle(x, y, width, fontRenderer.FONT_HEIGHT)
    private val textArea = Rectangle(x, y - 1, fontRenderer.getStringWidth(text), fontRenderer.FONT_HEIGHT + 1)
    private var drawXOffset = 0
    private var extendable = false
    private var trimmedText = ""
    private var alignment = HorizontalAlignment.LEFT
    private var backgroundColor = MinecraftColor.LIGHT_GRAY.colorCode

    override fun draw(mouseX: Int, mouseY: Int, delta: Float) {
        val hovering = area.contains(mouseX, mouseY)
        if(extendable && hovering){
            helper.drawRect(textArea.translated(drawXOffset, 0), zLevel.toDouble(), backgroundColor)
            fontRenderer.drawString(text, x + drawXOffset, y, textColor)
        } else {
            fontRenderer.drawString(trimmedText, x + drawXOffset, y, textColor)
        }
    }

    fun updateText(newText: String): LPGuiLabel {
        text = newText
        trimmedText = StringUtils.getCuttedString(text, width, fontRenderer)
        textArea.setSize(fontRenderer.getStringWidth(text), fontRenderer.FONT_HEIGHT + 1)
        return setAlignment(alignment)
    }

    fun setExtendable(newExtendable: Boolean, newBackgroundColor: Int): LPGuiLabel {
        extendable = newExtendable
        backgroundColor = newBackgroundColor
        return this
    }

    fun setAlignment(newAlignment: HorizontalAlignment): LPGuiLabel {
        alignment = if(text.width() > width){
            HorizontalAlignment.LEFT
        } else {
            newAlignment
        }
        drawXOffset = when(alignment){
            HorizontalAlignment.CENTER -> (width - text.width()) / 2
            HorizontalAlignment.LEFT -> 0
            HorizontalAlignment.RIGHT -> width - text.width()
        }
        return this
    }

    private fun String.width() = fontRenderer.getStringWidth(this)
}