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

package network.rs485.logisticspipes.guidebook

import buildcraft.api.items.FluidItemDrops.item
import codechicken.lib.util.ServerUtils.mc
import logisticspipes.LPItems
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook
import network.rs485.logisticspipes.gui.guidebook.IDrawable
import network.rs485.logisticspipes.gui.guidebook.IDrawableParagraph
import network.rs485.logisticspipes.util.math.Rectangle
import kotlin.math.floor


private const val tileSize = 40
private const val tileSpacing = 5

/**
 * Menu token, stores the key and the type of menu in a page.
 */
data class DrawableMenuParagraph(val menuTitle: List<DrawableWord>, val menuGroups: List<DrawableMenuTileGroup>) : IDrawableParagraph {
    override val area: Rectangle = Rectangle(0, 0)
    override var isHovered = false
    val horizontalLine = DrawableHorizontalLine(1)

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        if (DEBUG_AREAS) area.translated(0, -yOffset).render(0.0f, 0.0f, 0.0f)
        menuTitle.filter { visibleArea.overlaps(it.area.translated(0, -yOffset)) }.forEach { it.draw(mouseX, mouseY, delta, yOffset, visibleArea) }
        if (visibleArea.overlaps(horizontalLine.area.translated(0, -yOffset))) horizontalLine.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        menuGroups.filter { visibleArea.overlaps(it.area.translated(0, -yOffset)) }.forEach { it.draw(mouseX, mouseY, delta, yOffset, visibleArea) }
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return setChildrenPos(x, y, maxWidth)
    }

    override fun setChildrenPos(x: Int, y: Int, maxWidth: Int): Int {
        var currentY = 0
        currentY += splitInitialize(menuTitle, x, y + currentY, maxWidth)
        currentY += horizontalLine.setPos(x, y + currentY, maxWidth)
        for (group in menuGroups) currentY += group.setPos(x, currentY + y, maxWidth)
        area.setSize(maxWidth, currentY)
        return area.height
    }

    override fun hovering(mouseX: Int, mouseY: Int, yOffset: Int) {
        super.hovering(mouseX, mouseY, yOffset)
    }
}

class DrawableMenuTileGroup(val groupTitle: List<DrawableWord>, val groupTiles: List<DrawableMenuTile>) : IDrawableParagraph {
    override val area = Rectangle(0, 0)
    override var isHovered = false

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        return setChildrenPos(x, y, maxWidth)
    }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        groupTitle.filter { visibleArea.overlaps(it.area.translated(0, -yOffset)) }.forEach { it.draw(mouseX, mouseY, delta, yOffset, visibleArea) }
        groupTiles.filter { visibleArea.overlaps(it.area.translated(0, -yOffset)) }.forEach { it.draw(mouseX, mouseY, delta, yOffset, visibleArea) }
    }

    override fun setChildrenPos(x: Int, y: Int, maxWidth: Int): Int {
        var currentY = tileSpacing
        var currentX = tileSpacing
        currentY += splitInitialize(groupTitle, x, y, maxWidth)
        for (tile in groupTiles) {
            if (currentX + tile.area.width + tileSpacing > maxWidth) {
                currentX = tileSpacing
                currentY += tile.area.height + tileSpacing
            }
            tile.setPos(x + currentX, y + currentY, maxWidth)
            currentX += tile.area.width + tileSpacing
            if (tile == groupTiles.last()) currentY += tile.area.height + tileSpacing
        }
        area.setSize(maxWidth, currentY)
        return currentY
    }
}

class DrawableMenuTile(metadata: YamlPageMetadata) : IDrawable {
    // Maybe there needs to be a constant Int defining the size of all the tiles
    override val area = Rectangle(tileSize, tileSize)
    override var isHovered = false
    private val pageName = metadata.title
    private val iconScale = 1.0
    private val icon = metadata.icon
    private val iconArea = Rectangle(16 * iconScale.toInt(), 16 * iconScale.toInt())

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, yOffset: Int, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        if (isHovered) GuiGuideBook.drawBoxedCenteredString(Minecraft.getMinecraft(), pageName, area.x0 + area.width / 2, area.y1 - yOffset, 15)
        val visibleTile = visibleArea.overlap(area.translated(0, -yOffset))
        GuiGuideBook.drawRectangleTile(visibleTile, 4, true, isHovered, MinecraftColor.WHITE.colorCode)
        if (visibleArea.overlaps(iconArea.translated(0, -yOffset))) {
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            RenderHelper.enableGUIStandardItemLighting();
            val renderItem = Minecraft.getMinecraft().renderItem
            val prevZ = renderItem.zLevel
            renderItem.zLevel = -145f
            if(iconScale != 1.0) GlStateManager.scale(iconScale, iconScale, 1.0)
            renderItem.renderItemAndEffectIntoGUI(ItemStack(item), floor(iconArea.x0 / iconScale).toInt(), floor((iconArea.y0 - yOffset) / iconScale).toInt())
            if(iconScale != 1.0)GlStateManager.scale(1 / iconScale, 1 / iconScale, 1.0)
            renderItem.zLevel = prevZ
            RenderHelper.disableStandardItemLighting();
        }
        // Draw tile bg
        // Draw icon
        // Draw tooltip
    }

    override fun setPos(x: Int, y: Int, maxWidth: Int): Int {
        area.setPos(x, y)
        iconArea.setPos(x + (area.width - iconArea.width) / 2, y + (area.height - iconArea.height) / 2)
        return area.height
    }
}



