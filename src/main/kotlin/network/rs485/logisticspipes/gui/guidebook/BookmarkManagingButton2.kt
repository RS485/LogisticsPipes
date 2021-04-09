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
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.util.TextUtil
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.util.math.Rectangle

val additionTexture = Rectangle(192, 0, 16, 16)
val subtractionTexture = Rectangle.fromRectangle(additionTexture).translate(additionTexture.width, 0.0f)

/*
* This button's position is set based on the right and bottom constraints
*/
class BookmarkManagingButton2(x: Int, y: Int, onClickAction: (ButtonState) -> Boolean, val additionStateUpdater: (() -> ButtonState)): LPGuiButton2(2, x - additionTexture.roundedWidth, y - additionTexture.roundedHeight, additionTexture.roundedWidth, additionTexture.roundedHeight) {
    private var buttonState: ButtonState = ButtonState.ADD
    var onClickActionStated: (ButtonState) -> Boolean = onClickAction

    init {
        zLevel = GuideBookConstants.Z_TITLE_BUTTONS
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if(buttonState != ButtonState.DISABLED) {
            hovered = isHovered(mouseX, mouseY)
            if (hovered) {
                drawTooltip(
                    x = body.roundedLeft + body.roundedHeight / 2,
                    y = body.roundedTop,
                    horizontalAlign = HorizontalAlignment.CENTER,
                    verticalAlign = VerticalAlignment.BOTTOM
                )
            }
            val yOffset = getHoverState(hovered) * additionTexture.roundedHeight
            GlStateManager.enableAlpha()
            GuiGuideBook.drawStretchingRectangle(body, zLevel, (if (buttonState == ButtonState.ADD) additionTexture else subtractionTexture).translated(0, yOffset), false, MinecraftColor.WHITE.colorCode)
            GlStateManager.disableAlpha()
        }
    }

    fun setX(newX: Int){
        body.setPos(newX.toFloat(), body.y0)
    }

    fun updateState(){
        buttonState = additionStateUpdater()
    }

    override fun getTooltipText(): String = when(buttonState){
        ButtonState.ADD, ButtonState.REMOVE -> TextUtil.translate("misc.guide_book.bookmark_button.${buttonState.toString().toLowerCase()}")
        ButtonState.DISABLED -> ""
    }

    override fun getHoverState(mouseOver: Boolean): Int = if(buttonState == ButtonState.DISABLED) 2 else if (hovered) 1 else 0

    override fun click(mouseButton: Int) = onClickActionStated(buttonState)

    enum class ButtonState {
        ADD,
        REMOVE,
        DISABLED
    }
}
