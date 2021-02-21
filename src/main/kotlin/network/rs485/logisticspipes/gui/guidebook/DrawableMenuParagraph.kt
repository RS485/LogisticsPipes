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

import logisticspipes.LPItems
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.util.math.Rectangle


private const val tileSize = 40
private const val tileSpacing = 5

/**
 * Menu token, stores the key and the type of menu in a page.
 */
class DrawableMenuParagraph(private val menuTitle: List<DrawableWord>, private val menuGroups: List<DrawableMenuTileGroup>) : DrawableParagraph() {
    private val horizontalLine = createChild { DrawableHorizontalLine(1) }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, guideActionListener: GuiGuideBook.ActionListener) =
        menuGroups.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, guideActionListener) ?: Unit

    override fun setChildrenPos(): Int {
        var currentY = 0
        currentY += splitInitialize(menuTitle, 0, currentY, width)
        currentY += horizontalLine.setPos(0, currentY)
        for (group in menuGroups) currentY += group.setPos(0, currentY)
        return currentY
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        (menuTitle + horizontalLine + menuGroups).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuTileGroup(private val groupTitle: List<DrawableWord>, private val groupTiles: List<DrawableMenuTile>) : DrawableParagraph() {
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, guideActionListener: GuiGuideBook.ActionListener) =
        groupTiles.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, guideActionListener) ?: Unit

    override fun setChildrenPos(): Int {
        var currentY = tileSpacing
        var currentX = tileSpacing
        currentY += splitInitialize(groupTitle, currentX, currentY, width)
        for (tile in groupTiles) {
            if (currentX + tile.width + tileSpacing > width) {
                currentX = tileSpacing
                currentY += tile.height + tileSpacing
            }
            tile.setPos(currentX, currentY)
            currentX += tile.width + tileSpacing
            if (tile == groupTiles.last()) currentY += tile.height + tileSpacing
        }
        return currentY
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        (groupTitle + groupTiles).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuTile(private val linkedPage: String, private val pageName: String, private val icon: String) : Drawable() {
    private val iconScale = 1.0

    init {
        relativeBody.setSize(tileSize, tileSize)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, guideActionListener: GuiGuideBook.ActionListener) =
        guideActionListener.onMenuButtonClick(linkedPage)

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        hovered = hovering(mouseX, mouseY, visibleArea)
        GuiGuideBook.drawRectangleTile(absoluteBody, visibleArea, 4.0, true, hovered, MinecraftColor.WHITE.colorCode)
        val itemRect = Rectangle(left + (width - 16) / 2, top + (height - 16) / 2, 16, 16)
        if (itemRect.intersects(visibleArea)) {
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            RenderHelper.enableGUIStandardItemLighting()
            val renderItem = Minecraft.getMinecraft().renderItem
            val prevZ = renderItem.zLevel
            renderItem.zLevel = -145f
            if (iconScale != 1.0) GlStateManager.scale(iconScale, iconScale, 1.0)
            GlStateManager.disableDepth()
            renderItem.renderItemAndEffectIntoGUI(ItemStack(item), itemRect.left, itemRect.top)
            if (iconScale != 1.0) GlStateManager.scale(1 / iconScale, 1 / iconScale, 1.0)
            GlStateManager.enableDepth()
            renderItem.zLevel = prevZ
            RenderHelper.disableStandardItemLighting()
        }
        if (hovered){
            GuiGuideBook.drawLinkIndicator(mouseX, mouseY)
            GuiGuideBook.drawBoxedString(pageName, mid(), minOf(bottom, visibleArea.y1), GuideBookConstants.Z_TOOLTIP, GuiGuideBook.HorizontalAlignment.CENTER, GuiGuideBook.VerticalAlignment.TOP)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        return super.setPos(x, y)
    }

    fun mid(): Int = left + (width / 2)
}
