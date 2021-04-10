/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

class BorderedRectangle(outer: Rectangle, borderTop: Int, borderLeft: Int, borderBottom: Int, borderRight: Int) {

    constructor(outer: Rectangle, border: Int) : this(outer, border, border, border, border)

    val inner = outer.copy().translate(borderLeft, borderTop).grow(-borderLeft - borderRight, -borderTop - borderBottom)

    // Corners
    val topRight: Rectangle = Rectangle(inner.right to outer.top, outer.right to inner.top)
    val topLeft: Rectangle = Rectangle(outer.left to outer.top, inner.left to inner.top)
    val bottomLeft: Rectangle = Rectangle(outer.left to inner.bottom, inner.left to outer.bottom)
    val bottomRight: Rectangle = Rectangle(inner.right to inner.bottom, outer.right to outer.bottom)

    // Sides
    val top: Rectangle = Rectangle(inner.left to outer.top, inner.right to inner.top)
    val left: Rectangle = Rectangle(outer.left to inner.top, inner.left to inner.bottom)
    val bottom: Rectangle = Rectangle(inner.left to inner.bottom, inner.right to outer.bottom)
    val right: Rectangle = Rectangle(inner.right to inner.top, outer.right to inner.bottom)

    val corners: List<Rectangle> = listOf(topRight, topLeft, bottomLeft, bottomRight)

    val sides: List<Rectangle> = listOf(top, left, bottom, right)

    val borderQuads: List<Rectangle> = listOf(topRight, top, topLeft, left, bottomLeft, bottom, bottomRight, right)

    val quads: List<Rectangle> = borderQuads + inner
}