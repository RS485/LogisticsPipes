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
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.util.math.Rectangle
import kotlin.math.roundToInt

open class TextButton(
        parent: Drawable,
        xPosition: HorizontalPosition,
        yPosition: VerticalPosition,
        xSize: HorizontalSize,
        ySize: VerticalSize,
        private val textGetter: () -> String,
        onClickAction: (Int) -> Boolean
) : LPGuiButton(parent, xPosition, yPosition, xSize, ySize, onClickAction), Tooltipped {

    var text: String = ""
        set(value){
            field = TextUtil.getTrimmedString(value, relativeBody.roundedWidth - 4, helper.mcFontRenderer)
        }
    val yOffset: Int = ((relativeBody.roundedHeight - helper.mcFontRenderer.FONT_HEIGHT) / 2) + 1
    private val centerX: Float
        get() = relativeBody.width / 2

    init {
        updateText()
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        val color = if (isMouseHovering(mouseX, mouseY)) {
            helper.TEXT_HOVERED
        } else {
            helper.TEXT_WHITE
        }
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        helper.drawCenteredString(text, (relativeBody.left + centerX).roundToInt(), relativeBody.roundedY + yOffset, color, true)
        GlStateManager.disableBlend()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
            onClickAction.invoke(mouseButton).also {
                text = textGetter()
            }

    override fun getTooltipText(): List<String> = if(text != textGetter()) listOf(textGetter()) else emptyList()

    fun updateText() {
        text = textGetter()
    }
}