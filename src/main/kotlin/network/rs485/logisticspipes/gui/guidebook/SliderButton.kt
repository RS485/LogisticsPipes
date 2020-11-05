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

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import network.rs485.logisticspipes.gui.guidebook.GuideBookConstants.GUI_BOOK_TEXTURE
import network.rs485.logisticspipes.util.math.Rectangle

class SliderButton(val gui: GuiGuideBook, buttonId: Int, x: Int, y: Int, railHeight: Int, buttonWidth: Int, buttonHeight: Int, private var progress: Float) : GuiButton(buttonId, x, y, buttonWidth, buttonHeight, "") {
    private val rail = Rectangle(x, y, buttonWidth, railHeight)
    private val buttonArea = Rectangle(buttonWidth, buttonHeight)
    private val movementDistance = rail.height - buttonArea.height
    private var dragging = false

    init {
        buttonArea.setPos(x, calculateProgressI(progress))
        enabled = false
        zLevel = 15f
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        mc.textureManager.bindTexture(GUI_BOOK_TEXTURE)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        hovered = buttonArea.contains(mouseX, mouseY)
        val btnAtlasOffsetY = hovered && !dragging || !enabled
        val btnAtlasOffsetX = dragging || !enabled
        // TODO make this draw a bordered rectangle
        GuiGuideBook.drawStretchingRectangle(buttonArea.x0, buttonArea.y0, buttonArea.x1, buttonArea.y1, zLevel.toDouble(), 96 + (if (btnAtlasOffsetX) 1 else 0) * 12, 0 + (if (btnAtlasOffsetY) 1 else 0) * 15, 108 + (if (btnAtlasOffsetX) 1 else 0) * 12, (if (btnAtlasOffsetY) 1 else 0) * 15 + 15, false)
        mouseDragged(mc, mouseX, mouseY)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        if (dragging) {
            dragging = false
            setProgressI((mouseY - height / 2.0f).toInt())
            // TODO make proper way to update progress leaving currentPage private.
            gui.currentPage.progress = progress
        }
        super.mouseReleased(mouseX, mouseY)
    }

    override fun mouseDragged(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (dragging) {
            setProgressI((mouseY - height / 2.0f).toInt())
            // TODO make proper way to update progress leaving currentPage private.
            gui.currentPage.progress = progress
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (visible && enabled && hovered) {
            dragging = true
            return true
        }
        return false
    }

    fun reset() = setProgressF(0.0f)

    fun getProgress(): Float = progress

    // Set button y level as well as update progress value.
    fun setProgressI(progressI: Int) {
        buttonArea.y0 = progressI.coerceIn(rail.y0, rail.y1 - buttonArea.height)
        progress = calculateProgressF(buttonArea.y0)
    }

    // Set progress value as well as update button y level.
    fun setProgressF(progressF: Float) {
        progress = progressF.coerceIn(0.0f, 1.0f)
        buttonArea.y0 = calculateProgressI(progress)
    }

    // Calculates y level from given progress
    private fun calculateProgressI(progressF: Float): Int = rail.y0 + (movementDistance * progressF).toInt()

    // Calculates progress from given y level
    private fun calculateProgressF(progressI: Int): Float = (1.0f * (buttonArea.y0 - rail.y0)) / (rail.y1 - rail.y0 - buttonArea.height)
}