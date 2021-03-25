/*
 * Copyright (c) 2020-2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020-2021  RS485
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
import logisticspipes.utils.gui.GuiGraphics
import logisticspipes.utils.item.ItemStackRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

private const val listEntryHeight = 24
private const val tileSize = 40
private const val tileSpacing = 5

/**
 * Menu token, stores the key and the type of menu in a page.
 */
class DrawableMenuParagraph<T : Drawable>(private val menuTitle: List<DrawableWord>, private val menuGroups: List<DrawableMenuGroup<T>>) : DrawableParagraph() {
    private val horizontalLine = createChild { DrawableHorizontalLine(1) }

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, visibleArea: Rectangle, guideActionListener: GuiGuideBook.ActionListener) =
        menuGroups.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, visibleArea, guideActionListener) ?: Unit

    override fun setChildrenPos(): Int {
        var currentY = 1
        currentY += splitAndInitialize(menuTitle, 0, currentY, width, false)
        currentY += horizontalLine.setPos(0, currentY)
        for (group in menuGroups) currentY += group.setPos(0, currentY)
        return currentY
    }

    override fun drawChildren(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        (menuTitle + horizontalLine + menuGroups).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuGroup<T : Drawable>(private val groupTitle: List<DrawableWord>, private val groupTiles: List<T>) : DrawableParagraph() {
    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, visibleArea: Rectangle, guideActionListener: GuiGuideBook.ActionListener) =
        groupTiles.firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, visibleArea, guideActionListener) ?: Unit

    override fun setChildrenPos(): Int {
        var currentY = 0
        var currentX = tileSpacing
        currentY += splitAndInitialize(groupTitle, currentX, currentY, width, false)
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
    private val iconScale = 1.5f
    private val iconBody = Rectangle()

    companion object {
        val itemStackRenderer by lazy {
            ItemStackRenderer(0, 0, 0f, true, false)
        }
    }

    init {
        relativeBody.setSize(tileSize, tileSize)
        iconBody.setSize((16 * iconScale).toInt(), (16 * iconScale).toInt())
        iconBody.setPos((tileSize - iconBody.width) / 2, (tileSize - iconBody.height) / 2)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, visibleArea: Rectangle, guideActionListener: GuiGuideBook.ActionListener) =
        guideActionListener.onMenuButtonClick(linkedPage)

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        val hovered = isHovering(mouseX, mouseY, visibleArea)
        GuiGuideBook.drawRectangleTile(absoluteBody, visibleArea, GuideBookConstants.Z_TEXT - 1.0f, true, hovered, MinecraftColor.WHITE.colorCode)
        val itemRect = Rectangle.fromRectangle(iconBody.translated(absoluteBody))
        if (visibleArea.intersects(iconBody.translated(absoluteBody))) {
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            itemStackRenderer.renderItemInGui(itemRect.left, itemRect.top, item, GuideBookConstants.Z_TEXT, iconScale)
        }
        if (hovered) {
            GuiGuideBook.drawLinkIndicator(mouseX, mouseY)
            GuiGuideBook.drawBoxedString(pageName, mid(), min(bottom, visibleArea.bottom).roundToInt(), GuideBookConstants.Z_TOOLTIP, HorizontalAlignment.CENTER, VerticalAlignment.TOP)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        return super.setPos(x, y)
    }

    fun mid(): Int = left.toInt() + (width / 2)
}

class DrawableMenuListEntry(private val linkedPage: String, private val pageName: String, private val icon: String) : Drawable() {
    private val iconScale = 1.0f
    private val iconSize = (16 * iconScale).toInt()
    private val itemRect = Rectangle()
    private val itemOffset = (listEntryHeight - iconSize) / 2

    companion object {
        val itemStackRenderer by lazy {
            ItemStackRenderer(0, 0, 0f, true, false)
        }
    }

    init {
        relativeBody.setSize(4 * itemOffset + iconSize + GuiGuideBook.lpFontRenderer.getStringWidth(pageName), listEntryHeight)
        itemRect.setSize(iconSize, iconSize)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, visibleArea: Rectangle, guideActionListener: GuiGuideBook.ActionListener) =
        guideActionListener.onMenuButtonClick(linkedPage)

    override fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        val hovered = isHovering(mouseX, mouseY, visibleArea)
        GuiGuideBook.drawRectangleTile(absoluteBody, visibleArea, GuideBookConstants.Z_TEXT - 1.0f, true, hovered, MinecraftColor.WHITE.colorCode)
        itemRect.setPos(left + itemOffset, top + itemOffset)
        if (itemRect.intersects(visibleArea)) {
            val textColor: Int = if (!hovered) MinecraftColor.WHITE.colorCode else 0xffffffa0.toInt()
            val textVerticalOffset = (height - GuiGuideBook.lpFontRenderer.getFontHeight(1.0f)) / 2
            GuiGuideBook.lpFontRenderer.drawString(
                string = pageName,
                x = itemRect.right + itemOffset,
                y = top + textVerticalOffset,
                color = textColor,
                format = EnumSet.of(TextFormat.Shadow),
                scale = 1.0f
            )
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            DrawableMenuTile.itemStackRenderer.renderItemInGui(itemRect.left, itemRect.top, item, GuideBookConstants.Z_TEXT, iconScale)
        }
        if (hovered) {
            GuiGuideBook.drawLinkIndicator(mouseX, mouseY)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        itemRect.setPos(left + itemOffset, top + itemOffset)
        return super.setPos(x, y)
    }
}
