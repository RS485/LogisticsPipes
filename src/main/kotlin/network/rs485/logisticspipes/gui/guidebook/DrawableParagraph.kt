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

import network.rs485.logisticspipes.util.math.Rectangle

abstract class DrawableParagraph : Drawable, MouseInteractable {
    private val preRenderCallbacks = mutableSetOf<(mouseX: Float, mouseY: Float, visibleArea: Rectangle) -> Unit>()

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        relativeBody.setSize(parent!!.width, 0)
        relativeBody.setSize(relativeBody.roundedWidth, setChildrenPos())
        return super.setPos(x, y)
    }

    /**
     * This function is supposed to update the children's position by starting
     * Y and X placement at 0 and iterating through the children while calculating their placement.
     * This function is also responsible for updating the Paragraphs height as it directly
     * depends on the placement of it's children.
     * @return the height of all the Paragraph's children combined.
     */
    open fun setChildrenPos(): Int {
        return relativeBody.roundedHeight
    }

    /**
     * This function is responsible check if the mouse is over the object
     * @param mouseX        X position of the mouse (absolute, screen)
     * @param mouseY        Y position of the mouse (absolute, screen)
     */
    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean =
            absoluteBody.contains(mouseX, mouseY)

    open fun drawChildren(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {}

    /**
     * Registers a preRender callback to call on preRender.
     */
    fun registerPreRenderCallback(callable: (mouseX: Float, mouseY: Float, visibleArea: Rectangle) -> Unit) {
        preRenderCallbacks.add(callable)
    }

    open fun preRender(mouseX: Float, mouseY: Float, visibleArea: Rectangle) =
        preRenderCallbacks.forEach { function ->
            function.invoke(mouseX, mouseY, visibleArea)
        }

}
