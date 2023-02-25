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

package network.rs485.logisticspipes.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

interface IRectangle {
    // Size getters
    val width: Float
    val height: Float

    // Position getters
    val x0: Float
    val y0: Float
    val x1: Float
        get() = x0 + width
    val y1: Float
        get() = y0 + height

    // Named getters
    val left: Float
        get() = x0
    val right: Float
        get() = x1
    val top: Float
        get() = y0
    val bottom: Float
        get() = y1

    // Corners
    val topRight: Pair<Float, Float>
        get() = right to top
    val topLeft: Pair<Float, Float>
        get() = left to top
    val bottomLeft: Pair<Float, Float>
        get() = left to bottom
    val bottomRight: Pair<Float, Float>
        get() = right to bottom

    // Integer getters, only to be used when precision is not important
    val roundedX: Int
        get() = x0.roundToInt()
    val roundedY: Int
        get() = y0.roundToInt()
    val roundedWidth: Int
        get() = width.roundToInt()
    val roundedHeight: Int
        get() = height.roundToInt()
    val roundedLeft: Int
        get() = roundedX
    val roundedRight: Int
        get() = right.roundToInt()
    val roundedTop: Int
        get() = roundedY
    val roundedBottom: Int
        get() = bottom.roundToInt()

    // Non-destructive
    fun translated(translateX: Float, translateY: Float): IRectangle
    fun translated(translateX: Int, translateY: Int): IRectangle
    fun translated(rect: IRectangle): IRectangle = translated(rect.x0, rect.y0)

    // Non-destructive
    fun scaled(multiplier: Float): IRectangle

    // Logic checks
    fun contains(x: Float, y: Float): Boolean = x in this.x0..x1 && y in this.y0..y1
    fun contains(x: Int, y: Int): Boolean = contains(x.toFloat(), y.toFloat())
    fun contains(rect: IRectangle): Boolean = contains(rect.x0, rect.y0) && contains(rect.x1, rect.y1)
    fun intersects(rect: IRectangle): Boolean =
        !(right < rect.left || rect.right < left || bottom < rect.top || rect.bottom < top)

    // Operations
    fun overlap(rect: IRectangle): IRectangle

}

data class Rectangle(
    override val x0: Float = 0.0f,
    override val y0: Float = 0.0f,
    override val width: Float,
    override val height: Float,
) : IRectangle {

    /**
     * Default constructor with integers as parameters.
     */
    constructor(x: Int = 0, y: Int = 0, width: Int, height: Int) : this(
        x0 = x.toFloat(),
        y0 = y.toFloat(),
        width = width.toFloat(),
        height = height.toFloat()
    )

    /**
     * Creates rectangle from two points, since dimensions are Int the second point will be rounded down to accommodate.
     * The exact corner that each point represents is not enforced but some things might break if it doesn't match this.
     * @param firstPoint top-left point of the rectangle.
     * @param secondPoint bottom-right point of the rectangle.
     */
    constructor(firstPoint: Pair<Float, Float>, secondPoint: Pair<Float, Float>) : this(
        x0 = firstPoint.first,
        y0 = firstPoint.second,
        width = (secondPoint.first - firstPoint.first),
        height = (secondPoint.second - firstPoint.second)
    )

    override fun translated(translateX: Float, translateY: Float): Rectangle =
        Rectangle(x0 = x0 + translateX, y0 = y0 + translateY, width = width, height = height)

    override fun translated(translateX: Int, translateY: Int): Rectangle =
        Rectangle(x0 = x0 + translateX, y0 = y0 + translateY, width = width, height = height)

    override fun scaled(multiplier: Float): Rectangle = Rectangle(
        x0 = x0 * multiplier,
        y0 = y0 * multiplier,
        width = width * multiplier,
        height = height * multiplier
    )

    override fun overlap(rect: IRectangle): Rectangle =
        Rectangle(max(x0, rect.x0) to max(y0, rect.y0), min(x1, rect.x1) to min(y1, rect.y1))
}
