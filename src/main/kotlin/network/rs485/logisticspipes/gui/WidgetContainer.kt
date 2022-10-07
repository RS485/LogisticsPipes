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

package network.rs485.logisticspipes.gui

import logisticspipes.LogisticsPipes
import network.rs485.logisticspipes.gui.guidebook.*
import network.rs485.logisticspipes.gui.widget.LPGuiWidget
import network.rs485.logisticspipes.util.IRectangle

abstract class WidgetContainer(
    val children: List<LPGuiWidget>,
    override var parent: Drawable? = null,
    margin: Margin? = null
) : MouseHoverable, LPGuiWidget(
    parent = parent ?: Screen,
    xPosition = HorizontalAlignment.LEFT,
    yPosition = VerticalAlignment.TOP,
    xSize = Grow,
    ySize = Grow,
    margin = margin ?: Margin.NONE,
) {

    override fun <T : Drawable> createChild(childGetter: () -> T): T {
        if (LogisticsPipes.isDEBUG()) {
            LogisticsPipes.log.warn("createChild called on WidgetContainer, but WidgetContainer does not support lazy child creation")
            Throwable().printStackTrace()
        }
        return childGetter()
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: IRectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        //LPGuiDrawer.drawOutlineRect(MutableRectangle.fromRectangle(absoluteBody).grow(-1), MinecraftColor.CYAN.colorCode)
        children.forEach {
            it.draw(mouseX = mouseX, mouseY = mouseY, delta = delta, visibleArea = visibleArea)
        }
    }

    /**
     * This method will place the children in their final positions.
     * The container's size should be defined before running this.
     * @return the width and height of the container.
     */
    abstract fun placeChildren(): Pair<Int, Int>

    fun getHovered(mouseX: Float, mouseY: Float): MouseHoverable? {
        for (child in children) {
            if (child is WidgetContainer) {
                child.getHovered(mouseX, mouseY)?.let {
                    return it
                }
            } else if (child is MouseHoverable && child.isMouseHovering(mouseX, mouseY)) {
                return child
            }
        }
        return null
    }

}

class HorizontalWidgetContainer(
    children: List<LPGuiWidget>,
    parent: Drawable? = null,
    margin: Margin? = null,
) : WidgetContainer(children = children, parent = parent, margin = margin) {

    override val maxWidth: Int = Int.MAX_VALUE
    override val maxHeight: Int = Int.MAX_VALUE

    override fun initWidget() {
        setSize(minWidth, minHeight)
        children.forEach { it.initWidget() }
    }

    private fun growChildren() {
        val canGrow = children.filter { it.xSize == Grow || it.ySize == Grow }
        canGrow.forEach {
            if (it.xSize == Grow) {
                it.setSize(it.width + (width - minWidth) / canGrow.count(), it.height)
            }
            if (it.ySize == Grow) {
                it.setSize(newHeight = height - it.margin.vertical)
            }
        }
    }

    override fun placeChildren(): Pair<Int, Int> {
        growChildren()
        var xOffset = 0
        for (child in children) {
            xOffset += if (child is WidgetContainer) {
                child.setPos(xOffset, 0)
                child.placeChildren().x
            } else {
                child.setPos(
                    xOffset + child.margin.left,
                    when (child.yPosition) {
                        VerticalAlignment.TOP -> 0
                        VerticalAlignment.BOTTOM -> height - child.height
                        VerticalAlignment.CENTER -> (height - child.height) / 2
                    }
                ).x + child.margin.horizontal
            }
        }
        return width to height
    }

    override val minWidth: Int
        get() = children.sumOf { drawable ->
            drawable.minWidth + drawable.margin.horizontal
        }

    override val minHeight: Int
        get() = (children.maxOfOrNull { drawable ->
            drawable.minHeight
        } ?: 0)
}

class VerticalWidgetContainer(
    children: List<LPGuiWidget>,
    parent: Drawable? = null,
    margin: Margin? = null,
) : WidgetContainer(children = children, parent = parent, margin = margin) {

    override val maxWidth: Int = Int.MAX_VALUE
    override val maxHeight: Int = Int.MAX_VALUE

    override fun initWidget() {
        setSize(minWidth, minHeight)
        children.forEach { it.initWidget() }
    }

    private fun growChildren() {
        val canGrow = children.filter { it.xSize == Grow || it.ySize == Grow }
        canGrow.forEach {
            if (it.xSize == Grow) {
                it.setSize(newWidth = width)
            }
            if (it.ySize == Grow) {
                it.setSize(newHeight = it.height + (height - minHeight) / canGrow.count())
            }
        }
    }

    override fun placeChildren(): Pair<Int, Int> {
        growChildren()
        var yOffset = 0
        for (child in children) {
            yOffset += if (child is WidgetContainer) {
                child.setPos(0, yOffset)
                child.placeChildren().y
            } else {
                child.setPos(
                    when (child.xPosition) {
                        HorizontalAlignment.LEFT -> 0
                        HorizontalAlignment.RIGHT -> width - child.width
                        HorizontalAlignment.CENTER -> (width - child.width) / 2
                    },
                    yOffset
                ).y
            }
        }
        return width to height
    }

    override val minWidth: Int
        get() = (children.maxOfOrNull { drawable ->
            drawable.minWidth
        } ?: 0)
    override val minHeight: Int
        get() = children.sumOf { drawable ->
            drawable.minHeight + drawable.margin.vertical
        }
}
