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

package network.rs485.logisticspipes.gui.widget

import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.util.math.Rectangle

interface Tooltipped : MouseHoverable {
    fun getTooltipText(): List<String>
}

open class LPGuiWidget(parent: Drawable, xPosition: HorizontalPosition, yPosition: VerticalPosition, xSize: HorizontalSize, ySize: VerticalSize) : Drawable {
    override var parent: Drawable? = parent

    final override var relativeBody: Rectangle = Rectangle()
    final override var z: Float = 0.0f

    val drawer = LPGuiDrawer

    init {
        relativeBody.setSize(
                handleHorizontalSize(xSize),
                handleVerticalSize(ySize)
        )
        relativeBody.setPos(
                handleHorizontalPosition(xPosition),
                handleVerticalPosition(yPosition)
        )
    }

    private fun handleHorizontalPosition(pos: HorizontalPosition): Int = when (pos) {
        Center -> {
            (parent!!.width / 2) - (width / 2)
        }
        is Left -> {
            pos.margin
        }
        is Right -> {
            parent!!.width - width - pos.margin
        }
        else -> {
            error("This should never happen the devs forgot to implement something!")
        }
    }

    private fun handleVerticalPosition(pos: VerticalPosition): Int = when (pos) {
        Center -> {
            (parent!!.height / 2) - (height / 2)
        }
        is Top -> {
            pos.margin
        }
        is Bottom -> {
            parent!!.height - height - pos.margin
        }
        else -> {
            error("This should never happen the devs forgot to implement something!")
        }
    }

    private fun handleHorizontalSize(size: HorizontalSize): Int = when (size) {
        is FullSize -> {
            parent!!.width - (2 * size.margin)
        }
        is AbsoluteSize -> {
            size.size
        }
        else -> {
            error("This should never happen the devs forgot to implement something!")
        }
    }

    private fun handleVerticalSize(size: VerticalSize): Int = when (size) {
        is FullSize -> {
            parent!!.height - (2 * size.margin)
        }
        is AbsoluteSize -> {
            size.size
        }
        else -> {
            error("This should never happen the devs forgot to implement something!")
        }
    }
}

// TODO positions and sizes relative to siblings.

interface HorizontalPosition
data class Left(val margin: Int = 0) : HorizontalPosition
data class Right(val margin: Int = 0) : HorizontalPosition

interface VerticalPosition
data class Top(val margin: Int = 0) : VerticalPosition
data class Bottom(val margin: Int = 0) : VerticalPosition

object Center : HorizontalPosition, VerticalPosition

interface HorizontalSize
interface VerticalSize

data class FullSize(val margin: Int) : HorizontalSize, VerticalSize
data class AbsoluteSize(val size: Int) : HorizontalSize, VerticalSize
