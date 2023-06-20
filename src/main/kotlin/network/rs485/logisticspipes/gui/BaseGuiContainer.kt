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

import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.guidebook.MouseInteractable
import network.rs485.logisticspipes.gui.guidebook.Screen
import network.rs485.logisticspipes.gui.widget.FuzzyItemSlot
import network.rs485.logisticspipes.gui.widget.FuzzySelectionWidget
import network.rs485.logisticspipes.gui.widget.GhostSlot
import network.rs485.logisticspipes.gui.widget.Tooltipped
import network.rs485.logisticspipes.inventory.container.LPBaseContainer
import network.rs485.logisticspipes.util.IRectangle
import logisticspipes.modules.LogisticsModule
import logisticspipes.utils.gui.DummySlot
import mezz.jei.api.gui.IGhostIngredientHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Slot
import kotlin.math.roundToInt

abstract class BaseGuiContainer(
    private val baseContainer: LPBaseContainer<LogisticsModule>,
    val xOffset: Int = 0,
    val yOffset: Int = 0,
    private val widgetScreen: WidgetScreen,
) : GuiContainer(baseContainer), Drawable by widgetScreen {

    // TODO
    // Make it so only the highest "z" widget can be drawn as hovered - hovered state should be managed by gui class.

    open val fuzzySelector: FuzzySelectionWidget? = null

    override fun initGui() {
        widgetScreen.initGuiWidget(this@BaseGuiContainer, super<GuiContainer>.width, super<GuiContainer>.height)

        // To use minecraft's slot and item rendering. Might remove later.
        guiLeft = widgetScreen.widgetContainer.absoluteBody.roundedLeft
        guiTop = widgetScreen.widgetContainer.absoluteBody.roundedTop

        // Clear button and widget lists
        buttonList.clear()
        mc.player.openContainer = inventorySlots
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
        GuiDrawer.drawGuiContainerBackground(absoluteBody, guiLeft to guiTop, inventorySlots)
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
        widgetScreen.widgetContainer.draw(mouseX, mouseY, partialTicks, Screen.absoluteBody)
        (widgetScreen.hoveredWidget as? Tooltipped)?.getTooltipText()?.takeIf { it.isNotEmpty() }?.also {
            drawHoveringText(it, mouseX.roundToInt(), mouseY.roundToInt())
        } ?: renderHoveredToolTip(mouseX.roundToInt(), mouseY.roundToInt())
    }

    // Call super and call all the normally used methods.
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        val floatMouseX: Float = mouseX.toFloat()
        val floatMouseY: Float = mouseY.toFloat()
        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        drawFocalgroundLayer(floatMouseX, floatMouseY, partialTicks)
        GlStateManager.translate(0.0f, 0.0f, 10.0f)
        RenderHelper.disableStandardItemLighting()
        widgetScreen.updateHoveredState(floatMouseX, floatMouseY)
        drawForegroundLayer(floatMouseX, floatMouseY, partialTicks)
        RenderHelper.disableStandardItemLighting()
        fuzzySelector?.let { fuzzySelector ->
            if (hoveredSlot == null && fuzzySelector.active && !fuzzySelector.isMouseHovering(
                    floatMouseX,
                    floatMouseY,
                )
            ) {
                fuzzySelector.active = false
                fuzzySelector.currentSlot = null
            } else if (hoveredSlot != null && hoveredSlot != fuzzySelector.currentSlot && hoveredSlot is FuzzyItemSlot) {
                val slot = hoveredSlot as FuzzyItemSlot
                fuzzySelector.active = true
                fuzzySelector.currentSlot = slot
                fuzzySelector.setPos(guiLeft + slot.xPos, guiTop + slot.yPos + 17)
            }
            fuzzySelector.draw(floatMouseX, floatMouseY, partialTicks, Screen.screen)
        }
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (fuzzySelector?.mouseClicked(
                mouseX = mouseX.toFloat(),
                mouseY = mouseY.toFloat(),
                mouseButton = mouseButton,
            ) == true
        ) {
            return
        }
        val currentHovered = widgetScreen.hoveredWidget
        if (currentHovered is MouseInteractable) {
            if (currentHovered.mouseClicked(
                    mouseX = mouseX.toFloat(),
                    mouseY = mouseY.toFloat(),
                    mouseButton = mouseButton,
                )
            ) {
                currentHovered.playPressedSound(mc.soundHandler)
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun handleMouseClick(slotIn: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {
        if (slotIn is DummySlot || slotIn is GhostSlot) {
            inventorySlots.slotClick(slotId, mouseButton, type, mc.player)
        } else if (type == ClickType.QUICK_MOVE && baseContainer.tryTransferSlotToGhostSlot(slotId)) {
            // transfer to ghost slot succeeded
        } else {
            super.handleMouseClick(slotIn, slotId, mouseButton, type)
        }
    }

    // Update screen size square when resolution changes.
    override fun setWorldAndResolution(mc: Minecraft, width: Int, height: Int) {
        super.setWorldAndResolution(mc, width, height)
    }

    // Redirect vanilla background method to drawBackgroundLayer()
    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        drawBackgroundLayer(mouseX, mouseY, partialTicks)
    }

    fun List<Drawable>.draw(mouseX: Float, mouseY: Float, partialTicks: Float, visibleArea: IRectangle) =
        forEach {
            it.draw(mouseX, mouseY, partialTicks, visibleArea)
        }

    /**
     * Returns of JEI targets for ghost items and fluids to be placed on.
     */
    abstract fun <I> getFilterSlots(): MutableList<IGhostIngredientHandler.Target<I>>

    /**
     * Returns a list of rectangles that overflow from the main gui area, so that JEI can avoid it.
     */
    abstract fun getExtraGuiAreas(): List<IRectangle>
}
