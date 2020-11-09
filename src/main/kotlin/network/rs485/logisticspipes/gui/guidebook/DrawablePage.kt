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
import network.rs485.markdown.Paragraph

private const val PAGE_VERTICAL_PADDING = 5

class DrawablePage(paragraphs: List<Paragraph>) : IDrawableParagraph {
    override val parent: IDrawable? = null
    override var hovered = false
    override var x = 0
    override var y = 0
    override var width = 0
    override var height = 0
    private val drawableParagraphs = asDrawables(this, paragraphs)

    fun setPosition(x: Int, y: Int, width: Int){
        this.width = width
        setPos(x, y)
    }

    override fun setPos(x: Int, y: Int): Pair<Int, Int> {
        this.x = x
        this.y = y
        this.height = setChildrenPos()
        return super.setPos(x, y)
    }

    override fun setChildrenPos(): Int {
        var currentY = PAGE_VERTICAL_PADDING
         currentY += drawableParagraphs.fold(currentY) { y, paragraph ->
             y + paragraph.setPos(0, currentY + y).second
         }
        return PAGE_VERTICAL_PADDING + currentY
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        hovered = hovering(mouseX, mouseY, visibleArea)
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        val visibleParagraphs = drawableParagraphs.filter { it.visible(visibleArea) }
        visibleParagraphs.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}