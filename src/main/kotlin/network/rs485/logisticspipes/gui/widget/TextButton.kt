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

import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import kotlin.math.roundToInt

open class TextButton(
    parent: Drawable,
    xPosition: HorizontalAlignment,
    yPosition: VerticalAlignment,
    xSize: HorizontalSize,
    ySize: VerticalSize,
    margin: Margin,
    text: String,
    onClickAction: (Int) -> Boolean
) : LPGuiButton(
    parent = parent,
    xPosition = xPosition,
    yPosition = yPosition,
    xSize = xSize,
    ySize = ySize,
    margin = margin,
    onClickAction = onClickAction
), Tooltipped {

    var text: String = text
        set(value) {
            field = value
            trimmedText = trimText(value)
        }
    var trimmedText: String = trimText(text)
    val yOffset: Int = ((relativeBody.roundedHeight - helper.mcFontRenderer.FONT_HEIGHT) / 2) + 1
    private val centerX: Float
        get() = relativeBody.width / 2

    private fun trimText(text: String): String {
        return TextUtil.getTrimmedString(text, relativeBody.roundedWidth - 4, helper.mcFontRenderer)
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        val color = if (isMouseHovering(mouseX, mouseY)) {
            helper.TEXT_HOVERED
        } else {
            helper.TEXT_WHITE
        }
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        helper.drawCenteredString(trimmedText, (relativeBody.left + centerX).roundToInt(), relativeBody.roundedY + yOffset, color, true)
        GlStateManager.disableBlend()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
        onClickAction.invoke(mouseButton)

    override fun getTooltipText(): List<String> = if (trimmedText != text) listOf(text) else emptyList()
}
