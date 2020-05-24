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

import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.guidebook.YamlPageMetadata
import network.rs485.logisticspipes.util.math.Rectangle

const val MENU_ITEM_SIZE = 40
const val MARGIN = 2


class DrawableMenuItem(val metadata: YamlPageMetadata, val targetPath: String) : IDrawableWidget {

    override val rect = Rectangle(MENU_ITEM_SIZE, MENU_ITEM_SIZE)
    override var visible = true
    override var active = false
    override var hovered = false
    override var enabled = true

    override fun draw(mouseX: Int, mouseY: Int, yOffset: Int, delta: Float) {
        super.draw(mouseX, mouseY, yOffset, delta)
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        drawMenuItemFrame(mc, mouseX, mouseY, rect.x0, rect.y0, rect.width, rect.height)
        val iconScale = 1.0F
        var icon = Rectangle(16, 16).scale(iconScale)
        icon = icon.pos((rect.width - icon.width) / 2 , (rect.height - icon.height) / 2)
        GlStateManager.pushMatrix()
        GlStateManager.translate(rect.x0 + icon.x0.toFloat(), rect.y0 + icon.x0.toFloat(), 5.0F)
        GlStateManager.scale(iconScale, iconScale, 0.0F)
        RenderHelper.enableGUIStandardItemLighting()
        val item = Item.REGISTRY.getObject(ResourceLocation(metadata.icon))
        if (LogisticsPipes.isDEBUG() && item == null) LogisticsPipes.log.error("Something is wrong with the item: " + metadata.icon)
        mc.renderItem.renderItemAndEffectIntoGUI(ItemStack(item ?: Items.STICK), 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.scale(1 / iconScale, 1 / iconScale, 0.0F)
        GlStateManager.popMatrix()
    }

    private fun drawMenuItemFrame(mc: Minecraft, mouseX: Int, mouseY: Int, x: Int, y: Int, sizeX: Int, sizeY: Int) {
        mc.renderEngine.bindTexture(GuiGuideBook.GUI_BOOK_TEXTURE)
        /*run {
            btnBgX0 = x + 1
            btnBgY0 = y + 1
            btnBgX1 = x + sizeX - 1
            btnBgY1 = y + sizeY - 1
            btnX0 = x
            btnY0 = y
            btnX1 = x + 2
            btnY1 = y + 2
            btnX2 = x + sizeX - 2
            btnY2 = y + sizeY - 2
            btnX3 = x + sizeX
            btnY3 = y + sizeY
        }
        val i = if (hovered) 1 else 0
        val j = if (enabled) 1 else 2
        if (visible) {
            // Fill: Middle
            GuiGuideBook.drawRepeatingSquare(btnBgX0, btnBgY0, btnBgX1, btnBgY1, zText - 1, btnBgAtlasU0.toDouble(), btnBgAtlasV0 + (i * j * 32).toDouble(), btnBgAtlasU1.toDouble(), btnBgAtlasV1 + (i * j * 32).toDouble(), false)
            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            GuiGuideBook.drawStretchingSquare(btnX0, btnY0, btnX1, btnY1, zText, btnAtlasU0.toDouble(), btnAtlasV0 + (i * j * 16).toDouble(), btnAtlasU1.toDouble(), btnAtlasV1 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX2, btnY0, btnX3, btnY1, zText, btnAtlasU2.toDouble(), btnAtlasV0 + (i * j * 16).toDouble(), btnAtlasU3.toDouble(), btnAtlasV1 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX0, btnY2, btnX1, btnY3, zText, btnAtlasU0.toDouble(), btnAtlasV2 + (i * j * 16).toDouble(), btnAtlasU1.toDouble(), btnAtlasV3 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX2, btnY2, btnX3, btnY3, zText, btnAtlasU2.toDouble(), btnAtlasV2 + (i * j * 16).toDouble(), btnAtlasU3.toDouble(), btnAtlasV3 + (i * j * 16).toDouble())
            // Edges: Top, Bottom, Left & Right
            GuiGuideBook.drawStretchingSquare(btnX1, btnY0, btnX2, btnY1, zText, btnAtlasU1.toDouble(), btnAtlasV0 + (i * j * 16).toDouble(), btnAtlasU2.toDouble(), btnAtlasV1 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX1, btnY2, btnX2, btnY3, zText, btnAtlasU1.toDouble(), btnAtlasV2 + (i * j * 16).toDouble(), btnAtlasU2.toDouble(), btnAtlasV3 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX0, btnY1, btnX1, btnY2, zText, btnAtlasU0.toDouble(), btnAtlasV1 + (i * j * 16).toDouble(), btnAtlasU1.toDouble(), btnAtlasV2 + (i * j * 16).toDouble())
            GuiGuideBook.drawStretchingSquare(btnX2, btnY1, btnX3, btnY2, zText, btnAtlasU2.toDouble(), btnAtlasV1 + (i * j * 16).toDouble(), btnAtlasU3.toDouble(), btnAtlasV2 + (i * j * 16).toDouble())
        }*/
    }
}