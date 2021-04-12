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

package network.rs485.logisticspipes.gui

import logisticspipes.LPConstants
import logisticspipes.asm.ModDependentInterface
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.Container
import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseInteractable
import network.rs485.logisticspipes.gui.guidebook.Screen
import network.rs485.logisticspipes.gui.widget.LPGuiWidget
import network.rs485.logisticspipes.gui.widget.Tooltipped
import network.rs485.logisticspipes.util.math.Rectangle
import kotlin.math.roundToInt

@ModDependentInterface(modId = [LPConstants.neiModID], interfacePath = ["codechicken.nei.api.INEIGuiHandler"])
abstract class LPBaseGuiContainer(inventorySlotsIn: Container, widthIn: Int, heightIn: Int, private val xOffset: Int = 0, private val yOffset: Int = 0) : GuiContainer(inventorySlotsIn), Drawable {

    override var parent: Drawable? = Screen
    override var relativeBody = Rectangle((width - widthIn) / 2, (height - heightIn) / 2, widthIn, heightIn)
    private var hoveredWidget: Tooltipped? = null

    val widgetList: MutableList<LPGuiWidget> = mutableListOf()

    val guiWidth: Int get() = relativeBody.roundedWidth
    val guiHeight: Int get() = relativeBody.roundedHeight

    override fun initGui() {
        // In case the screen size has changed.
        Screen.relativeBody.setSize(width, height)
        // Center gui with possible offsets
        relativeBody.setPos(
                newX = (Screen.xCenter - guiWidth / 2) + xOffset,
                newY = (Screen.yCenter - guiHeight / 2) + yOffset
        )
        // To use minecraft's slot and item rendering. Might remove later.
        guiLeft = relativeBody.roundedLeft
        guiTop = relativeBody.roundedTop
        // Clear button and widget lists
        buttonList.clear()
        widgetList.clear()
        mc.player.openContainer = inventorySlots
    }

    fun addWidget(widget: LPGuiWidget): Drawable {
        widgetList.add(widget)
        widgetList.sortBy { it.z }
        return widget
    }

    /**
     * Draw what is supposed to not be important to the gui and is behind everything else.
     * Origin is top left corner of the minecraft window.
     * @param mouseX mouse position on X axis.
     * @param mouseY mouse position on Y axis.
     * @param partialTicks time so animations don't have to depend on game ticks which can be unstable.
     */
    open fun drawBackgroundLayer(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        helper.drawGuiBackground(absoluteBody, 0f, inventorySlots)
    }

    /**
     * Draw the area in between the back and fore grounds, good place to draw buttons.
     * Origin is the top left corner of the gui.
     * @param mouseX mouse position on X axis.
     * @param mouseY mouse position on Y axis.
     * @param partialTicks time so animations don't have to depend on game ticks which can be unstable.
     */
    open fun drawFocalgroundLayer(mouseX: Float, mouseY: Float, partialTicks: Float) {}

    /**
     * Draw the top layer of the screen, it could be tooltips, items on slots or even titles and text.
     * Origin is the top left corner of the gui.
     * @param mouseX mouse position on X axis.
     * @param mouseY mouse position on Y axis.
     * @param partialTicks time so animations don't have to depend on game ticks which can be unstable.
     */
    open fun drawForegroundLayer(mouseX: Float, mouseY: Float, partialTicks: Float) {
        widgetList.draw(mouseX, mouseY, partialTicks, Screen.absoluteBody)
        hoveredWidget?.getTooltipText()?.takeIf { it.isNotEmpty() }?.also {
            drawHoveringText(it, mouseX.roundToInt(), mouseY.roundToInt())
        } ?: renderHoveredToolTip(mouseX.roundToInt(), mouseY.roundToInt())
    }

    private fun getHovered(mouseX: Float, mouseY: Float): MouseInteractable? = widgetList.filterIsInstance<MouseInteractable>().firstOrNull { it.isMouseHovering(mouseX, mouseY) }

    // Call super and call all the normally used methods.
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        var currentMouseX: Float = mouseX.toFloat()
        var currentMouseY: Float = mouseY.toFloat()
        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.translate(absoluteBody.left, absoluteBody.top, 0.0f)
        currentMouseX -= absoluteBody.left
        currentMouseY -= absoluteBody.top
        drawFocalgroundLayer(currentMouseX, currentMouseY, partialTicks)
        GlStateManager.translate(0.0f, 0.0f, 10.0f)
        RenderHelper.disableStandardItemLighting()
        hoveredWidget = widgetList.filterIsInstance<Tooltipped>().firstOrNull { it.isMouseHovering(currentMouseX, currentMouseY) }
        drawForegroundLayer(currentMouseX, currentMouseY, partialTicks)
        GlStateManager.translate(-absoluteBody.left, -absoluteBody.top, -10.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val currentMouseX: Float = mouseX - absoluteBody.left
        val currentMouseY: Float = mouseY - absoluteBody.top
        if (getHovered(currentMouseX, currentMouseY)?.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), mouseButton, null) == true) {
            // Todo button sound if applicable
            return
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    // Update screen size square when resolution changes.
    override fun setWorldAndResolution(mc: Minecraft, width: Int, height: Int) {
        super.setWorldAndResolution(mc, width, height)
    }

    // Redirect vanilla background method to drawBackgroundLayer()
    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        drawBackgroundLayer(mouseX, mouseY, partialTicks)
    }

    companion object {
        val helper = LPGuiDrawer
    }

    // TODO add NEI impl

    fun List<Drawable>.draw(mouseX: Float, mouseY: Float, partialTicks: Float, visibleArea: Rectangle) = forEach { it.draw(mouseX, mouseY, partialTicks, visibleArea) }
}