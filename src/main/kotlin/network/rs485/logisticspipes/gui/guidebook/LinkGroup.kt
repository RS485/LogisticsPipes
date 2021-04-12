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

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.utils.MinecraftColor
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.Link
import network.rs485.markdown.PageLink
import network.rs485.markdown.TextFormat
import network.rs485.markdown.WebLink

interface LinkInteractable : MouseInteractable {
    /**
     * Returns an updated color depending on any mouse interaction.
     */
    fun updateColor(baseColor: Int): Int

    /**
     * Returns an updated format depending on any mouse interaction.
     */
    fun updateFormat(baseFormat: Set<TextFormat>): Set<TextFormat>

    /**
     * Update mouse state for any state changes.
     */
    fun updateState(mouseX: Float, mouseY: Float, visibleArea: Rectangle)

}

class LinkGroup(private val link: Link) : LinkInteractable {
    private val orderedChildren: MutableList<DrawableWord> = mutableListOf()
    var hovered: Boolean = false
        internal set

    fun addChild(linkWord: DrawableWord) = orderedChildren.add(linkWord)

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean =
            orderedChildren.any {
                it.isMouseHovering(mouseX, mouseY)
            }

    override fun updateState(mouseX: Float, mouseY: Float, visibleArea: Rectangle) =
            isMouseHovering(mouseX, mouseY).let { hovered = it }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean {
        if (guideActionListener != null) {
            when (link) {
                is PageLink -> guideActionListener.onPageLinkClick(link.page)
                is WebLink -> guideActionListener.onWebLinkClick(link.url)
            }
            return true
        }
        return false
    }

    override fun updateColor(baseColor: Int): Int = MinecraftColor.BLUE.colorCode

    override fun updateFormat(baseFormat: Set<TextFormat>): Set<TextFormat> =
            (if (hovered) baseFormat::minusElement else baseFormat::plusElement).invoke(TextFormat.Underline)

}
