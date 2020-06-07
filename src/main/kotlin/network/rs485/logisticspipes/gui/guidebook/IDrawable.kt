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

interface IDrawable {
    val area: Rectangle
    var isHovered: Boolean

    /**
     * This is just like the normal draw functions for minecraft Gui classes but with the added current Y offset.
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     * @param delta Timing floating value
     * @param yOffset The current Y offset on the drawn page.
     * @param visibleArea used to avoid draw calls on non-visible children
     */
    fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle){
        hovering(mouseX, mouseY, yOffset)
    }

    /**
     * This function is supposed to init the Drawable element by giving it the exact Y where it
     * should start and returning the Y where the next element should need to be started in.
     * @param x the X position of the Drawable.
     * @param y the Y position of the Drawable.
     * @param maxWidth the the width of the parent, meaning the maximum width the child could have.
     * @return the input Y level plus the current element's height and a preset vertical spacer height.
     */
    fun init(x: Int, y: Int, maxWidth: Int): Int

    /**
     * This function is responsible to update the isHovered field
     * @param mouseX X position of the mouse (absolute, screen)
     * @param mouseY Y position of the mouse (absolute, screen)
     * @param yOffset The current Y offset on the drawn page.
     */
    fun hovering(mouseX: Int, mouseY: Int, yOffset: Int) {
        isHovered = area.contains(mouseX, mouseY + yOffset)
    }
}