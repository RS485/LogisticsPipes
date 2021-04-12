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
import logisticspipes.utils.item.ItemStackRenderer
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPGuiDrawer
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
class DrawableMenuParagraph<T>(private val menuTitle: List<DrawableWord>, private val menuGroups: List<DrawableMenuGroup<T>>) : DrawableParagraph() where T : Drawable, T : MouseInteractable {
    override var relativeBody = Rectangle()
    override var parent: Drawable? = null
    override var z: Float = GuideBookConstants.Z_TEXT

    private val horizontalLine = createChild { DrawableHorizontalLine(1) }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        super.draw(mouseX, mouseY, delta, visibleArea)
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
            menuGroups.firstOrNull { it.isMouseHovering(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, mouseButton, guideActionListener)
                    ?: false

    override fun setChildrenPos(): Int {
        var currentY = 1
        currentY += splitAndInitialize(menuTitle, 0, currentY, width, false)
        currentY += horizontalLine.setPos(0, currentY)
        currentY += 5
        for (group in menuGroups) currentY += group.setPos(0, currentY)
        return currentY
    }

    override fun drawChildren(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        (menuTitle + horizontalLine + menuGroups).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuGroup<T>(private val groupTitle: List<DrawableWord>, private val groupTiles: List<T>) : DrawableParagraph() where T : Drawable, T : MouseInteractable {
    override var relativeBody = Rectangle()
    override var parent: Drawable? = null
    override var z: Float = GuideBookConstants.Z_TEXT

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        drawChildren(mouseX, mouseY, delta, visibleArea)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
            groupTiles.firstOrNull { it.isMouseHovering(mouseX, mouseY) }?.mouseClicked(mouseX, mouseY, mouseButton, guideActionListener)
                    ?: false

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

    override fun drawChildren(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        (groupTitle + groupTiles).filter { it.visible(visibleArea) }.forEach { it.draw(mouseX, mouseY, delta, visibleArea) }
    }
}

class DrawableMenuTile(private val linkedPage: String, private val pageName: String, private val icon: String) : Drawable, MouseInteractable {
    private val iconScale = 1.5f
    private val iconBody = Rectangle()

    override var relativeBody = Rectangle()
    override var parent: Drawable? = null
    override var z: Float = GuideBookConstants.Z_TEXT

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

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = absoluteBody.contains(mouseX, mouseY)

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
            guideActionListener?.onMenuButtonClick(linkedPage) != null

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        val hovered = isMouseHovering(mouseX, mouseY)
        LPGuiDrawer.drawBorderedTile(
                rect = absoluteBody,
                z = GuideBookConstants.Z_TEXT,
                hovered = hovered,
                enabled = true,
                light = true,
                thickerBottomBorder = false
        )
        val itemRect = Rectangle.fromRectangle(iconBody.translated(absoluteBody))
        if (visibleArea.intersects(iconBody.translated(absoluteBody))) {
            val item = Item.REGISTRY.getObject(ResourceLocation(icon)) ?: LPItems.blankModule
            itemStackRenderer.renderItemInGui(itemRect.left, itemRect.top, item, GuideBookConstants.Z_TEXT, iconScale)
        }
        if (hovered) {
            LPGuiDrawer.drawInteractionIndicator(mouseX, mouseY, 25f)
            GuiGuideBook.drawBoxedString(pageName, mid(), min(bottom, visibleArea.bottom).roundToInt(), GuideBookConstants.Z_TOOLTIP, HorizontalAlignment.CENTER, VerticalAlignment.TOP)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        return super.setPos(x, y)
    }

    fun mid(): Int = left.toInt() + (width / 2)
}

class DrawableMenuListEntry(private val linkedPage: String, private val pageName: String, private val icon: String) : Drawable, MouseInteractable {
    private val iconScale = 1.0f
    private val iconSize = (16 * iconScale).toInt()
    private val itemRect = Rectangle()
    private val itemOffset = (listEntryHeight - iconSize) / 2

    override var relativeBody = Rectangle()
    override var parent: Drawable? = null
    override var z: Float = GuideBookConstants.Z_TEXT

    companion object {
        val itemStackRenderer by lazy {
            ItemStackRenderer(0, 0, 0f, true, false)
        }
    }

    init {
        relativeBody.setSize(4 * itemOffset + iconSize + GuiGuideBook.lpFontRenderer.getStringWidth(pageName), listEntryHeight)
        itemRect.setSize(iconSize, iconSize)
    }

    override fun isMouseHovering(mouseX: Float, mouseY: Float): Boolean = absoluteBody.contains(mouseX, mouseY)

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int, guideActionListener: GuiGuideBook.ActionListener?): Boolean =
            guideActionListener?.onMenuButtonClick(linkedPage) != null

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        val hovered = isMouseHovering(mouseX, mouseY)
        LPGuiDrawer.drawBorderedTile(
                rect = absoluteBody,
                z = GuideBookConstants.Z_TEXT,
                hovered = hovered,
                enabled = true,
                light = true,
                thickerBottomBorder = false
        )
        itemRect.setPos(left + itemOffset, top + itemOffset)
        if (itemRect.intersects(visibleArea)) {
            val textColor: Int = if (!hovered) MinecraftColor.WHITE.colorCode else 0xffffffa0.toInt()
            val textVerticalOffset = (height - GuiGuideBook.lpFontRenderer.getFontHeight(1.0f)) / 2
            LPGuiDrawer.lpFontRenderer.drawString(
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
            LPGuiDrawer.drawInteractionIndicator(mouseX, mouseY, 25f)
        }
    }

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        itemRect.setPos(left + itemOffset, top + itemOffset)
        return super.setPos(x, y)
    }
}
