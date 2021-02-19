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

open class Drawable {
    companion object {
        /**
         * Assigns the parent of all children to this.
         */
        fun <T: Drawable> List<Drawable>.createParent(parentGetter: () -> T) =
            parentGetter().also { parentDrawable -> this.forEach { it.parent = parentDrawable } }
    }

    var hovered: Boolean = false
    internal var relativeBody: Rectangle = Rectangle()

    var parent: Drawable? = null
        private set

    // Relative positions/size accessors.

    val x: Int get() = relativeBody.x0
    val y: Int get() = relativeBody.y0
    val width: Int get() = relativeBody.width
    val height: Int get() = relativeBody.height

    // Absolute positions accessors.
    val left: Int get() = (parent?.left?: 0) + x
    val right: Int get() = left + width
    val top: Int get() = (parent?.top?: 0) + y
    val bottom: Int get() = top + height
    val absoluteBody: Rectangle get() = Rectangle(left, top, width, height)

    /**
     * Assigns a new child's parent to this.
     */
    fun <T: Drawable> createChild(childGetter: () -> T) = childGetter().also { it.parent = this }

    /**
     * This is just like the normal draw functions for minecraft Gui classes but with the added current Y offset.
     * @param mouseX        X position of the mouse (absolute, screen)
     * @param mouseY        Y position of the mouse (absolute, screen)
     * @param delta         Timing floating value
     * @param visibleArea   used to avoid draw calls on non-visible children
     */
    open fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {

    }

    /**
     * This function is responsible for updating the Drawable's position by giving it the exact X and Y where it
     * should start and returning it's height as an offset for the next element.
     * @param x         the X position of the Drawable.
     * @param y         the Y position of the Drawable.
     * @return the input Y level plus the current element's height and a preset vertical spacer height.
     */
    open fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        return relativeBody.height
    }

    /**
     * This function is responsible check if the mouse is over the object
     * @param mouseX        X position of the mouse (absolute, screen)
     * @param mouseY        Y position of the mouse (absolute, screen)
     * @param visibleArea   Desired visible area to check
     */
    fun hovering(mouseX: Int, mouseY: Int, visibleArea: Rectangle): Boolean {
        if(!visibleArea.contains(mouseX, mouseY)) return false
        return absoluteBody.contains(mouseX, mouseY)
    }

    /**
     * This function is responsible to check if the current Drawable is within the vertical constrains of the given area.
     * @param visibleArea   Desired visible area to check
     * @return true if within constraints false otherwise.
     */
    fun visible(visibleArea: Rectangle): Boolean {
        return visibleArea.intersects(absoluteBody)
    }
}