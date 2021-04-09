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
import network.rs485.logisticspipes.util.math.Rectangle
import kotlin.math.floor
import kotlin.math.roundToInt

private const val minimumHeight = 16
private val texture = Rectangle(96, 0, 12, 16)

class SliderButton2(x: Int, y: Int, width: Int, railHeight: Int, private var progress: Float, val setProgressCallback: (progress: Float) -> Unit) : LPGuiButton2(0, x, y, width, railHeight) {
    private val sliderButton: Rectangle = Rectangle()
    private val movementDistance: Int get() = body.roundedHeight - sliderButton.roundedHeight
    private var dragging: Boolean = false
    private var initialMouseYOffset: Int = 0
    private var hoveredBar: Boolean = false

    init {
        zLevel = GuideBookConstants.Z_TITLE_BUTTONS
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        hoveredBar = sliderButton.translated(body).contains(mouseX, mouseY)
        hovered = body.contains(mouseX, mouseY)
        GuiGuideBook.drawSliderButton(sliderButton.translated(body), texture.translated(0, getHoverState(hoveredBar) * texture.roundedHeight))
        mouseDragged(mc, mouseX, mouseY)
    }

    override fun getHoverState(mouseOver: Boolean): Int = if (dragging) 3 else if (!enabled) 2 else if (hoveredBar) 1 else 0

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        if (dragging) {
            dragging = false
            setProgressI((mouseY - body.roundedY) - initialMouseYOffset)
            initialMouseYOffset = 0
            setProgressCallback(progress)
        }
        super.mouseReleased(mouseX, mouseY)
    }

    override fun mouseDragged(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (dragging) {
            setProgressI((mouseY - body.roundedY) - initialMouseYOffset)
            setProgressCallback(progress)
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (visible && enabled) {
            if (hoveredBar) {
                dragging = true
                initialMouseYOffset = mouseY - sliderButton.translated(body).y0.toInt()
                return true
            } else if(hovered) {
                setProgressI(mouseY - body.roundedTop - (sliderButton.roundedHeight / 2))
                setProgressCallback(progress)
            }
        }
        return false
    }

    private fun updateButtonY() {
        val y: Int = (movementDistance * progress).toInt()
        sliderButton.setPos(sliderButton.roundedX, y)
    }

    fun updateSlider(extraHeight: Int, newProgress: Float): SliderButton2 {
        if (extraHeight > 0) {
            enabled = true
            sliderButton.setPos(0, calculateProgressI())
            val possibleHeight: Int = body.roundedHeight - extraHeight
            sliderButton.setSize(
                newWidth = body.roundedWidth,
                newHeight = (if ((possibleHeight % 2) == 0) possibleHeight - 1 else possibleHeight).coerceIn(minimumHeight..body.roundedHeight)
            )
            progress = newProgress
            updateButtonY()
        } else {
            enabled = false
            sliderButton.setPos(0, 0)
            sliderButton.setSize(body.roundedWidth, newHeight = minimumHeight)
            progress = 0.0f
            updateButtonY()
        }
        return this
    }

    // Set button y level as well as update progress value.
    private fun setProgressI(progressI: Int) {
        sliderButton.setPos(sliderButton.roundedX, progressI.coerceIn(0, movementDistance))
        progress = calculateProgressF()
    }

    fun changeProgress(amount: Int) {
        sliderButton.setPos(sliderButton.x0, (sliderButton.y0 + amount).coerceIn(0.0F, movementDistance.toFloat()))
        progress = calculateProgressF()
        setProgressCallback(progress)
    }

    // Calculates y level from given progress
    private fun calculateProgressI(): Int = (movementDistance * progress).roundToInt()

    // Calculates progress from given y level
    private fun calculateProgressF(): Float = sliderButton.y0 / movementDistance
}