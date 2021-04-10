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

package network.rs485.logisticspipes.util.math

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Rectangle constructor(private var _x: Float = 0.0f, private var _y: Float = 0.0f, private var _width: Float, private var _height: Float) {

    companion object {
        fun fromRectangle(rect: Rectangle): Rectangle {
            return Rectangle(rect._x, rect._y, rect.width, rect.height)
        }
    }

    // Position getters
    val x0: Float
        get() = _x
    val y0: Float
        get() = _y
    val x1: Float
        get() = _x + _width
    val y1: Float
        get() = _y + _height

    // Size getters
    val width: Float
        get() = _width
    val height: Float
        get() = _height

    // Named getters
    val left: Float
        get() = _x
    val right: Float
        get() = x1
    val top: Float
        get() = _y
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
        get() = _x.roundToInt()
    val roundedY: Int
        get() = _y.roundToInt()

    val roundedWidth: Int
        get() = _width.roundToInt()
    val roundedHeight: Int
        get() = _height.roundToInt()

    val roundedLeft: Int
        get() = roundedX
    val roundedRight: Int
        get() = right.roundToInt()
    val roundedTop: Int
        get() = roundedY
    val roundedBottom: Int
        get() = bottom.roundToInt()

    /**
     * Default constructor, sets all fields to 0.
     */
    constructor() : this(0f, 0f, 0f, 0f)

    /**
     * Default constructor with integers as parameters.
     */
    constructor(x: Int, y: Int, width: Int, height: Int) : this(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

    /**
     * Creates rectangle at "origin" with set width and height
     * @param width rectangle's width.
     * @param height rectangle's height.
     */
    constructor(width: Int, height: Int) : this(0.0f, 0.0f, width.toFloat(), height.toFloat())

    /**
     * Creates rectangle from two points, since dimensions are Int the second point will be rounded down to accommodate.
     * The exact corner that each point represents is not enforced but some things might break if it doesn't match this.
     * @param firstPoint top-left point of the rectangle.
     * @param secondPoint bottom-right point of the rectangle.
     */
    constructor(firstPoint: Pair<Float, Float>, secondPoint: Pair<Float, Float>) : this(firstPoint.first, firstPoint.second, (secondPoint.first - firstPoint.first), (secondPoint.second - firstPoint.second))

    constructor(rect: Rectangle) : this(rect._x, rect._y, rect._width, rect._height)

    // Transformations
    fun setSize(newWidth: Float, newHeight: Float) = apply {
        _width = newWidth
        _height = newHeight
    }

    fun setSize(newWidth: Int, newHeight: Int) = setSize(newWidth.toFloat(), newHeight.toFloat())

    fun grow(growX: Float, growY: Float) = apply {
        _width += growX
        _height += growY
    }
    fun grow(grow: Float): Rectangle = grow(grow, grow)

    fun grow(growX: Int, growY: Int): Rectangle = grow(growX.toFloat(), growY.toFloat())
    fun grow(grow: Int): Rectangle = grow(grow, grow)

    fun setSizeFromRectangle(rect: Rectangle) = apply { setSize(rect._width, rect._height) }

    fun scaleSize(multiplier: Float) = apply {
        _width *= multiplier
        _height *= multiplier
    }

    // Translations
    fun setPos(newX: Float, newY: Float) = apply {
        _x = newX
        _y = newY
    }

    fun setPos(newX: Int, newY: Int) = setPos(newX.toFloat(), newY.toFloat())

    fun setPosFromRectangle(rect: Rectangle) = apply { setPos(rect._x, rect._y) }

    fun resetPos() = apply { setPos(0.0f, 0.0f) }

    fun scalePos(multiplier: Float) = apply {
        _x *= multiplier
        _y *= multiplier
    }

    fun translate(translate: Float): Rectangle = translate(translate, translate)
    fun translate(translateX: Float = 0.0f, translateY: Float = 0.0f) = apply {
        _x += translateX
        _y += translateY
    }

    fun translate(translate: Int): Rectangle = translate(translate, translate)
    fun translate(translateX: Int = 0, translateY: Int = 0) = translate(translateX.toFloat(), translateY.toFloat())

    // Non-destructive
    fun translated(translateX: Float, translateY: Float) = copy().translate(translateX, translateY)
    fun translated(translateX: Int, translateY: Int) = translated(translateX.toFloat(), translateY.toFloat())
    fun translated(rect: Rectangle) = translated(rect._x, rect._y)

    // Both
    fun scale(multiplier: Float) = scalePos(multiplier).scaleSize(multiplier)

    // Non-destructive
    fun scaled(multiplier: Float) = copy().scalePos(multiplier).scaleSize(multiplier)

    // Logic checks
    fun contains(x: Float, y: Float): Boolean = x in _x..x1 && y in _y..y1
    fun contains(x: Int, y: Int): Boolean = contains(x.toFloat(), y.toFloat())
    fun contains(rect: Rectangle): Boolean = contains(rect._x, rect._y) && contains(rect.x1, rect.y1)
    fun intersects(rect: Rectangle): Boolean = !(right < rect.left || rect.right < left || bottom < rect.top || rect.bottom < top)

    // Operations
    fun overlap(rect: Rectangle): Rectangle = Rectangle(max(this._x, rect._x) to max(this._y, rect._y), min(this.x1, rect.x1) to min(this.y1, rect.y1))
    fun copy(): Rectangle = Rectangle(_x, _y, _width, _height)

    override fun toString(): String {
        return "Rectangle(x = $_x, y = $_y, width = $_width, height = $_height)"
    }
}