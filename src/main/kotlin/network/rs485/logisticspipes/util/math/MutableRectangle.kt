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

import network.rs485.logisticspipes.util.IRectangle
import org.jetbrains.annotations.Contract
import kotlin.math.max
import kotlin.math.min

class MutableRectangle(
    private var x: Float = 0.0f,
    private var y: Float = 0.0f,
    private var _width: Float,
    private var _height: Float,
) : IRectangle {

    companion object {
        fun fromRectangle(rect: IRectangle): MutableRectangle {
            return MutableRectangle(rect.x0, rect.y0, rect.width, rect.height)
        }
    }

    override val width: Float
        get() = _width
    override val height: Float
        get() = _height
    override val x0: Float
        get() = x
    override val y0: Float
        get() = y

    /**
     * Default constructor, sets all fields to 0.
     */
    constructor() : this(0f, 0f, 0f, 0f)

    /**
     * Default constructor with integers as parameters.
     */
    constructor(x: Int, y: Int, width: Int, height: Int) : this(
        x.toFloat(),
        y.toFloat(),
        width.toFloat(),
        height.toFloat()
    )

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
    constructor(firstPoint: Pair<Float, Float>, secondPoint: Pair<Float, Float>) : this(
        firstPoint.first,
        firstPoint.second,
        (secondPoint.first - firstPoint.first),
        (secondPoint.second - firstPoint.second)
    )

    // Transformations
    @Contract("_ -> this", mutates = "this")
    fun setSize(newWidth: Float, newHeight: Float): MutableRectangle = apply {
        _width = newWidth
        _height = newHeight
    }

    @Contract("_ -> this", mutates = "this")
    fun setSize(newWidth: Int, newHeight: Int): MutableRectangle = setSize(newWidth.toFloat(), newHeight.toFloat())

    @Contract("_ -> this", mutates = "this")
    fun grow(growX: Float, growY: Float): MutableRectangle = apply {
        _width += growX
        _height += growY
    }

    @Contract("_ -> this", mutates = "this")
    fun grow(grow: Float): MutableRectangle = grow(grow, grow)

    @Contract("_ -> this", mutates = "this")
    fun grow(growX: Int, growY: Int): MutableRectangle = grow(growX.toFloat(), growY.toFloat())
    @Contract("_ -> this", mutates = "this")
    fun grow(grow: Int): MutableRectangle = grow(grow, grow)

    fun setSizeFromRectangle(rect: IRectangle) = setSize(rect.width, rect.height)

    @Contract("_ -> this", mutates = "this")
    fun scaleSize(multiplier: Float): MutableRectangle = apply {
        _width *= multiplier
        _height *= multiplier
    }

    // Translations
    @Contract("_ -> this", mutates = "this")
    fun setPos(newX: Float, newY: Float): MutableRectangle = apply {
        x = newX
        y = newY
    }

    @Contract("_ -> this", mutates = "this")
    fun setPos(newX: Int, newY: Int): MutableRectangle = setPos(newX.toFloat(), newY.toFloat())

    @Contract("_ -> this", mutates = "this")
    fun setPosFromRectangle(rect: IRectangle): MutableRectangle = setPos(rect.x0, rect.y0)

    @Contract("_ -> this", mutates = "this")
    fun resetPos(): MutableRectangle = apply { setPos(0.0f, 0.0f) }

    @Contract("_ -> this", mutates = "this")
    fun scalePos(multiplier: Float): MutableRectangle = apply {
        x *= multiplier
        y *= multiplier
    }

    @Contract("_ -> this", mutates = "this")
    fun translate(translate: Float): MutableRectangle = translate(translate, translate)
    @Contract("_ -> this", mutates = "this")
    fun translate(translateX: Float = 0.0f, translateY: Float = 0.0f): MutableRectangle = apply {
        x += translateX
        y += translateY
    }

    @Contract("_ -> this", mutates = "this")
    fun translate(translate: Int): MutableRectangle = translate(translate, translate)
    @Contract("_ -> this", mutates = "this")
    fun translate(translateX: Int = 0, translateY: Int = 0): MutableRectangle = apply {
        x += translateX
        y += translateY
    }

    // Non-destructive
    override fun translated(translateX: Float, translateY: Float): MutableRectangle =
        copy().translate(translateX, translateY)
    override fun translated(translateX: Int, translateY: Int): MutableRectangle =
        copy().translate(translateX, translateY)

    // Both
    @Contract("_ -> this", mutates = "this")
    fun scale(multiplier: Float): MutableRectangle = scalePos(multiplier).scaleSize(multiplier)

    // Non-destructive
    override fun scaled(multiplier: Float): MutableRectangle = copy().scalePos(multiplier).scaleSize(multiplier)

    // Operations
    override fun overlap(rect: IRectangle): MutableRectangle =
        MutableRectangle(max(this.x, rect.x0) to max(this.y, rect.y0), min(this.x1, rect.x1) to min(this.y1, rect.y1))

    fun copy(): MutableRectangle = MutableRectangle(x, y, _width, _height)

    override fun toString(): String {
        return "Rectangle(x = $x, y = $y, width = $_width, height = $_height)"
    }
}
