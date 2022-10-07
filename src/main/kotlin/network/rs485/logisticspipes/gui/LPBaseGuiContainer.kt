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
import network.rs485.logisticspipes.gui.guidebook.MouseHoverable
import network.rs485.logisticspipes.gui.guidebook.MouseInteractable
import network.rs485.logisticspipes.gui.guidebook.Screen
import network.rs485.logisticspipes.gui.widget.Tooltipped
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.math.MutableRectangle
import kotlin.math.roundToInt

@ModDependentInterface(modId = [LPConstants.neiModID], interfacePath = ["codechicken.nei.api.INEIGuiHandler"])
abstract class LPBaseGuiContainer(
    inventorySlotsIn: Container,
    private val xOffset: Int = 0,
    private val yOffset: Int = 0
) : GuiContainer(inventorySlotsIn), Drawable {

    final override var parent: Drawable? = Screen
    final override val relativeBody = MutableRectangle()
    private var hoveredWidget: MouseHoverable? = null

    protected abstract val widgets: ComponentContainer
    private var widgetContainer: WidgetContainer = VerticalWidgetContainer(emptyList(), parent, Margin.DEFAULT)


    override fun initGui() {
        // In case the screen size has changed.
        Screen.relativeBody.setSize(width, height)

        // Create gui widgets from dls components.
        widgetContainer = GuiRenderer.render(widgets, relativeBody).also {
            it.parent = this@LPBaseGuiContainer
        }

        // Set position back to 0 before placing children to respect minecraft's gui translation.
        widgetContainer.relativeBody.resetPos()

        // Initialize every widget and place it relative to it's parent.
        widgetContainer.apply {
            initWidget()
            placeChildren()
        }

        // Set size of the main container to the minimum necessary size to fit all children.
        widgetContainer.relativeBody.setSize(
            widgetContainer.minWidth,
            widgetContainer.minHeight,
        ).translate(
            widgetContainer.margin.left,
            widgetContainer.margin.top,
        )

        // Set the root body of the gui based on the size of the first container
        // and taking into account it's margin.
        relativeBody.setSizeFromRectangle(
            widgetContainer.relativeBody.copy().grow(
                widgetContainer.margin.horizontal,
                widgetContainer.margin.vertical,
            )
        )

        // Center gui with possible offsets
        relativeBody.setPos(
            newX = (Screen.xCenter - relativeBody.width / 2) + xOffset,
            newY = (Screen.yCenter - relativeBody.height / 2) + yOffset
        )

        // To use minecraft's slot and item rendering. Might remove later.
        guiLeft = widgetContainer.absoluteBody.roundedLeft
        guiTop = widgetContainer.absoluteBody.roundedTop

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
        helper.drawGuiBackground(absoluteBody, guiLeft to guiTop, inventorySlots)
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
        widgetContainer.draw(mouseX, mouseY, partialTicks, Screen.absoluteBody)
        (hoveredWidget as? Tooltipped)?.getTooltipText()?.takeIf { it.isNotEmpty() }?.also {
            drawHoveringText(it, mouseX.roundToInt(), mouseY.roundToInt())
        } ?: renderHoveredToolTip(mouseX.roundToInt(), mouseY.roundToInt())
    }

    private fun getHovered(mouseX: Float, mouseY: Float): MouseHoverable? =
        widgetContainer.getHovered(mouseX, mouseY)

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
        hoveredWidget = getHovered(floatMouseX, floatMouseY)
        drawForegroundLayer(floatMouseX, floatMouseY, partialTicks)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val currentHovered = hoveredWidget
        if (currentHovered is MouseInteractable) {
            if (currentHovered.mouseClicked(
                    mouseX = mouseX.toFloat(),
                    mouseY = mouseY.toFloat(),
                    mouseButton = mouseButton,
                    guideActionListener = null
                )
            ) {
                currentHovered.playPressedSound(mc.soundHandler)
            }
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

    fun List<Drawable>.draw(mouseX: Float, mouseY: Float, partialTicks: Float, visibleArea: IRectangle) =
        forEach {
            it.draw(mouseX, mouseY, partialTicks, visibleArea)
        }


}
