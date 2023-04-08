/*
 * Copyright (c) 2023  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2023  RS485
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

import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.gui.guidebook.Screen
import network.rs485.logisticspipes.util.math.MutableRectangle

abstract class WidgetScreen : Drawable {

    private val widgets: ComponentContainer by lazy { constructWidgetContainer() }

    final override var parent: Drawable? = Screen
    override val relativeBody: MutableRectangle = MutableRectangle()
    var hoveredWidget: MouseHoverable? = null
        private set

    var widgetContainer: WidgetContainer = VerticalWidgetContainer(emptyList(), parent, Margin.DEFAULT, 0)
        private set

    protected abstract fun constructWidgetContainer(): ComponentContainer

    fun initGuiWidget(parent: Drawable, width: Int, height: Int, xOffset: Int = 0, yOffset: Int = 0) {
        // In case the screen size has changed.
        Screen.relativeBody.setSize(width, height)

        // Create gui widgets from dls components.
        widgetContainer = GuiRenderer.render(widgets).also {
            it.parent = parent
        }

        // Set position back to 0 before placing children to respect minecraft's gui translation.
        widgetContainer.relativeBody.resetPos()

        // Initialize every widget and place it relative to its parent.
        widgetContainer.apply {
            initWidget()
            placeChildren()
        }

        // Set size of the main container to the minimum necessary size to fit all children.
        widgetContainer.relativeBody.setSize(
            widgetContainer.minWidth,
            widgetContainer.minHeight,
        ).translate(
            widgetContainer.margin.left,
            widgetContainer.margin.top,
        )

        // initialize our relativeBody
        relativeBody.resetPos()
        // Set the root body of the gui based on the size of the first container
        // and taking into account it's margin.
        relativeBody.setSizeFromRectangle(
            widgetContainer.relativeBody.copy().grow(
                widgetContainer.margin.horizontal,
                widgetContainer.margin.vertical,
            ),
        )

        // Center gui with possible offsets
        relativeBody.setPos(
            newX = (Screen.xCenter - relativeBody.width / 2) + xOffset,
            newY = (Screen.yCenter - relativeBody.height / 2) + yOffset,
        )
    }

    fun getHovered(mouseX: Float, mouseY: Float): MouseHoverable? =
        widgetContainer.getHovered(mouseX, mouseY)

    fun updateHoveredState(mouseX: Float, mouseY: Float) {
        hoveredWidget = getHovered(mouseX, mouseY)
    }
}
