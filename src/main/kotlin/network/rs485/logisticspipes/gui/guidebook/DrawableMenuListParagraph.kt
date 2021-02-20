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

import logisticspipes.LPItems
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import java.util.*

private const val entryHeight = 24
private const val entrySpacing = 5

/**
 *
 */
class DrawableMenuListParagraph(private val menuTitle: List<DrawableWord>, private val listMenuGroups: List<DrawableMenuListGroup>) : DrawableParagraph() {
    private val horizontalLine = createChild { DrawableHorizontalLine(1) }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        listMenuGroups.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun setChildrenPos(): Int {
        var currentY = 0
        currentY += splitInitialize(menuTitle, 0, currentY, width)
        currentY += horizontalLine.setPos(0, currentY)
        for (group in listMenuGroups) currentY += group.setPos(0, currentY)
        return currentY + entrySpacing
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        (menuTitle + horizontalLine + listMenuGroups).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuListGroup(private val groupTitle: List<DrawableWord>, private val groupListEntries: List<DrawableMenuListEntry>) : DrawableParagraph() {
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        groupListEntries.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.onClick?.invoke(mouseButton)
    }

    override fun setChildrenPos(): Int {
        var currentY = entrySpacing
        var currentX = entrySpacing
        currentY += splitInitialize(groupTitle, currentX, currentY, width)
        for (entry in groupListEntries) {
            if (currentX + entry.width + entrySpacing > width) {
                currentX = entrySpacing
                currentY += entry.height + entrySpacing
            }
            entry.setPos(currentX, currentY)
            currentX += entry.width + entrySpacing
            if (entry == groupListEntries.last()) currentY += entry.height + entrySpacing
        }
        return currentY
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        (groupTitle + groupListEntries).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuListEntry(private val pageName: String, private val icon: String, val onClick: (mouseButton: Int) -> Unit) : Drawable() {
    private val iconScale = 1.0
    private val iconSize = (16 * iconScale).toInt()
    private val itemRect = Rectangle()
    private val itemOffset = (entryHeight - iconSize) / 2

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        hovered = hovering(mouseX, mouseY, visibleArea)
        val visibleTile = Rectangle.fromRectangle(visibleArea)
            .translate(0, -5)
            .grow(0, 10)
            .overlap(Rectangle.fromRectangle(absoluteBody))
        GuiGuideBook.drawRectangleTile(visibleTile, 4.0, true, hovered, MinecraftColor.WHITE.colorCode)
        itemRect.setPos(left + itemOffset, top + itemOffset)
        if (itemRect.intersects(visibleArea)) {
            val textColor = if (!hovered) MinecraftColor.WHITE.colorCode else MinecraftColor.YELLOW.colorCode
            val textVerticalOffset = (height - GuiGuideBook.lpFontRenderer.getFontHeight(1.0)) / 2
            GuiGuideBook.lpFontRenderer.drawString(
                string = pageName,
                x = itemRect.right + itemOffset,
                y = top + textVerticalOffset + 1,
                color = textColor,
                format = EnumSet.noneOf(TextFormat::class.java),
                scale = 1.0
            )
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            RenderHelper.enableGUIStandardItemLighting()
            val renderItem = Minecraft.getMinecraft().renderItem
            val prevZ = renderItem.zLevel
            renderItem.zLevel = -145f
            GlStateManager.disableDepth()
            renderItem.renderItemAndEffectIntoGUI(ItemStack(item), itemRect.left, itemRect.top)
            GlStateManager.enableDepth()
            renderItem.zLevel = prevZ
            RenderHelper.disableStandardItemLighting()
        }
        if (hovered) {
            GuiGuideBook.drawLinkIndicator(mouseX, mouseY)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        relativeBody.setSize(4 * itemOffset + iconSize + GuiGuideBook.lpFontRenderer.getStringWidth(pageName), entryHeight)
        itemRect.setPos(left + itemOffset, top + itemOffset)
        itemRect.setSize(iconSize, iconSize)
        return super.setPos(x, y)
    }
}
