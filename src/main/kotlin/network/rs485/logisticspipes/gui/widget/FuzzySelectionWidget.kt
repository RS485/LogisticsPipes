/*
 * Copyright (c) 2022  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2022  RS485
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

import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseInteractable
import network.rs485.logisticspipes.property.BitSetProperty
import network.rs485.logisticspipes.property.layer.PropertyOverlay
import network.rs485.logisticspipes.util.FuzzyFlag
import network.rs485.logisticspipes.util.FuzzyUtil
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import logisticspipes.utils.Color
import net.minecraft.client.Minecraft
import java.util.*

class FuzzySelectionWidget(
    parent: Drawable,
    private val fuzzyFlagOverlay: PropertyOverlay<BitSet, BitSetProperty>,
) : LPGuiWidget(
    parent = parent,
    xPosition = HorizontalAlignment.LEFT,
    yPosition = VerticalAlignment.TOP,
    xSize = Size.FIXED,
    ySize = Size.FIXED,
    margin = Margin.NONE,
), MouseInteractable {

    private val flagPrefix: String = "enum.logisticspipes.fuzzy_flags."
    private val border = 5

    var active: Boolean = false

    var currentSlot: FuzzyItemSlot? = null
        set(newSlot) {
            field = newSlot?.also {
                setSize(calculateWidth(newSlot.usedFlags), calculateHeight(newSlot.usedFlags))
            }
        }

    override val minWidth: Int
        get() = calculateWidth(currentSlot?.usedFlags ?: emptySet())
    override val minHeight: Int
        get() = calculateHeight(currentSlot?.usedFlags ?: emptySet())
    override val maxWidth: Int = minWidth
    override val maxHeight: Int = minHeight

    override fun initWidget() {

    }

    private fun calculateWidth(flags: Set<FuzzyFlag>): Int = flags.maxOfOrNull {
        Minecraft.getMinecraft().fontRenderer.getStringWidth(
            TextUtil.translate("$flagPrefix${it.name.lowercase()}") + border * 2,
        )
    } ?: 0

    private fun calculateHeight(flags: Set<FuzzyFlag>): Int = flags.size * 10 + border * 2

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        if (active) {
            if ((isMouseHovering(
                    mouseX,
                    mouseY,
                ) || (Minecraft.getMinecraft().currentScreen as BaseGuiContainer).hoveredSlot == currentSlot) && currentSlot != null
            ) {
                currentSlot?.let { slot ->
                    GuiDrawer.drawGuiBackground(relativeBody)
                    var yOffset = border
                    val flags = slot.flagGetter.invoke()
                    slot.usedFlags.forEach { flag: FuzzyFlag ->
                        val color = if (FuzzyUtil.get(flags, flag)) {
                            GuiDrawer.getFuzzyColor(flag)
                        } else {
                            Color.TEXT_DARK.value
                        }
                        // TODO maybe draw string with shadow when hovered
                        GuiDrawer.mcFontRenderer.drawString(
                            TextUtil.translate(flagPrefix + flag.name.lowercase()),
                            relativeBody.roundedX + border,
                            relativeBody.roundedY + yOffset,
                            color,
                        )
                        yOffset += 10
                    }
                }
            }
        }
    }

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean {
        return relativeBody.copy().grow(2).translate(-1, -1).contains(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        currentSlot?.let { currentSlot ->
            getHoveredFlag(mouseX, mouseY)?.let { clickedFlag ->
                fuzzyFlagOverlay.write { p ->
                    p.flip(currentSlot.slotIndex * 4 + clickedFlag.bit)
                }
                return true
            }
        }
        return false
    }

    private fun getHoveredFlag(mouseX: Float, mouseY: Float): FuzzyFlag? = currentSlot?.usedFlags?.firstOrNull {
        relativeBody.copy().translate(border, border + 10 * it.ordinal).setSize(relativeBody.roundedWidth, 10)
            .contains(mouseX, mouseY)
    }


}
