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

import logisticspipes.utils.string.StringUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.text.TextFormatting

/*
object DrawablePage {
    const val HEADER_SCALING = 1.5f
    fun draw(mc: Minecraft, page: SavedTab, gui: GuiGuideBook, mouseX: Int, mouseY: Int, yOffset: Int): Int {
        // Draw menu items if applicable
        var mouseX = mouseX
        var mouseY = mouseY
        var areaCurrentY = 0
        mouseX = if (mouseX < gui.guiX0 || mouseX > gui.guiX3) 0 else mouseX
        mouseY = if (mouseY < gui.guiY0 || mouseY > gui.guiY3) 0 else mouseY
        if (!page.menuItems.isEmpty()) {
            for (menuItemsDivision in page.menuItems) {
                GuiGuideBook.drawMenuText(mc, gui.areaX0, gui.areaY0 + areaCurrentY + yOffset, gui.areaAcrossX, 19, menuItemsDivision.name)
                areaCurrentY += 20
                var currentTileIndex = 0
                for (menuItem in menuItemsDivision.list) {
                    menuItem.drawMenuItem(mc, mouseX, mouseY, gui.areaX0 + currentTileIndex % gui.tileMax * (gui.tileSize + gui.tileSpacing), gui.areaY0 + areaCurrentY + (currentTileIndex / gui.tileMax.toFloat()).toInt() * (gui.tileSize + gui.tileSpacing) + yOffset,
                            gui.tileSize, gui.tileSize, false)
                    val tileBottom = gui.areaY0 + areaCurrentY + yOffset + gui.tileSize
                    val maxBottom = gui.areaY1
                    val above = tileBottom > maxBottom
                    menuItem.drawTitle(mc, mouseX, mouseY, above)
                    currentTileIndex++
                }
                areaCurrentY += ((currentTileIndex - 1) / gui.tileMax.toFloat()).toInt() * (gui.tileSize + gui.tileSpacing) + (gui.tileSize + gui.tileSpacing)
            }
        }

        // Drawing the text after the menu
        val unformattedText = GuiGuideBook.currentPage.getText()
        if (!unformattedText.isEmpty()) {
            val text = StringUtils.splitLines(unformattedText, mc.fontRenderer, gui.areaAcrossX)
            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, gui.zText.toFloat())
            var lastFormatIndex = 0
            var previousFormat = TextFormatting.RESET
            for (line in text) {
                if (line.contains("=====") || line.contains("-----")) {
                    GuiGuideBook.drawStretchingSquare(gui.areaX0, gui.areaY0 + areaCurrentY + yOffset + 1, gui.areaX1 - 2, gui.areaY0 + areaCurrentY + yOffset + 2, 15, 3.0, 3.0, 4.0, 4.0)
                    areaCurrentY += 6
                } else if (line.contains("##")) {
                    GlStateManager.pushMatrix()
                    GlStateManager.translate(gui.areaX0.toFloat(), gui.areaY0 + areaCurrentY + yOffset.toFloat(), 0f)
                    GlStateManager.scale(HEADER_SCALING - 0.01f, HEADER_SCALING - 0.01f, 1.0f)
                    mc.fontRenderer.drawString(previousFormat.toString() + line.replace("##", "") + TextFormatting.RESET, 0, 0, 0xFFFFFF)
                    GlStateManager.popMatrix()
                    areaCurrentY += (10 * HEADER_SCALING).toInt()
                } else {
                    mc.fontRenderer.drawString(previousFormat.toString() + line + TextFormatting.RESET, gui.areaX0, gui.areaY0 + areaCurrentY + yOffset, 0xFFFFFF)
                    lastFormatIndex = 0
                    for (format in TextFormatting.values()) {
                        if (line.lastIndexOf(format.toString()) > lastFormatIndex) {
                            previousFormat = format
                            lastFormatIndex = line.lastIndexOf(format.toString())
                        }
                    }
                    areaCurrentY += 10
                }
            }
            GlStateManager.popMatrix()
            GlStateManager.pushMatrix()
            GlStateManager.translate(0.0f, 0.0f, gui.zTitleButtons.toFloat())
            GlStateManager.popMatrix()
        }
        return areaCurrentY
    }
}*/
