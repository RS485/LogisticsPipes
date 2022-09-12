/*
 * Copyright (c) 2022  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2022  RS485
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

package network.rs485.logisticspipes.gui.widget.module

import logisticspipes.modules.ModuleProvider
import logisticspipes.network.packets.module.ModulePropertiesUpdate
import logisticspipes.proxy.MainProxy
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPBaseGuiContainer
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.gui.widget.*
import network.rs485.logisticspipes.property.PropertyLayer
import network.rs485.logisticspipes.util.TextUtil

// TODO create different buttons.
class ProviderGui(
    playerInventory: IInventory,
    private val providerModule: ModuleProvider,
    lockedStack: ItemStack
) : LPBaseGuiContainer(ProviderContainer(playerInventory, providerModule, lockedStack), 174, 177) {
    override var z = 1.0f

    private val prefix: String = "gui.providerpipe."

    private val propertyLayer: PropertyLayer = PropertyLayer(providerModule.propertyList)

    private val providerMode = propertyLayer.overlay(providerModule.providerMode)
    private val isExclusionFilter = propertyLayer.overlay(providerModule.isExclusionFilter)

    private val title: LPGuiLabel = LPGuiLabel(
        parent = this,
        xPosition = Center,
        yPosition = Top(6),
        xSize = FullSize(6),
        textColor = LPGuiDrawer.TEXT_DARK,
        textGetter = {
            providerModule.filterInventory.name
        })
        .setAlignment(HorizontalAlignment.CENTER)
    private val extractionModeLabel: LPGuiLabel = LPGuiLabel(
        parent = this,
        xPosition = Left(6),
        yPosition = Top(80),
        xSize = FullSize(6),
        textColor = LPGuiDrawer.TEXT_DARK,
        textGetter = {
            "${TextUtil.translate("${prefix}ExcessInventory")} ${TextUtil.translate(providerMode.get().extractionModeTranslationKey)}"
        })
        .setExtendable(true, LPGuiDrawer.BACKGROUND_LIGHT)
    private val extractionModeButton: TextButton = TextButton(
        parent = this,
        xPosition = Left(6),
        yPosition = Top(35),
        xSize = AbsoluteSize(50),
        ySize = AbsoluteSize(20),
        textGetter = {
            TextUtil.translate("${prefix}Switch")
        },
        onClickAction = { mouseButton ->
            if (mouseButton == 0) {
                providerMode.write { it.next() }
                extractionModeLabel.updateText()
                return@TextButton true
            }
            return@TextButton false
        })
    private val providerModeButton: TextButton = TextButton(
        parent = this,
        xPosition = Right(6),
        yPosition = Top(35),
        xSize = AbsoluteSize(50),
        ySize = AbsoluteSize(20),
        textGetter = {
            if (isExclusionFilter.get()) {
                TextUtil.translate("${prefix}Exclude")
            } else {
                TextUtil.translate("${prefix}Include")
            }
        },
        onClickAction = { mouseButton ->
            if (mouseButton == 0) {
                isExclusionFilter.write { it.toggle() }
                return@TextButton true
            }
            return@TextButton false
        }
    )
    private val filterSlots: SlotGroup = SlotGroup(
        parent = this,
        xPosition = Center,
        yPosition = Top(18),
        slots = (inventorySlots as ProviderContainer).filterSlots,
        columns = 3,
        rows = 3
    )
    private val playerSlots: PlayerInventorySlotGroup = PlayerInventorySlotGroup(
        this,
        Center,
        Bottom(6),
        (inventorySlots as ProviderContainer).playerSlots
    )

    init {
        propertyLayer.addObserver(providerModule.isExclusionFilter) {
            providerModeButton.updateText()
        }
    }

    override fun initGui() {
        super.initGui()
        addWidget(title)
        addWidget(extractionModeLabel)
        addWidget(extractionModeButton)
        addWidget(providerModeButton)
        addWidget(filterSlots)
        addWidget(playerSlots)
    }

    override fun drawFocalgroundLayer(mouseX: Float, mouseY: Float, partialTicks: Float) {
        for (guiButton in buttonList) {
            guiButton.drawButton(mc, mouseX.toInt(), mouseY.toInt(), partialTicks)
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        propertyLayer.unregister()
        if (mc.player != null && propertyLayer.properties.isNotEmpty()) {
            // send update to server, when there are changed properties
            MainProxy.sendPacketToServer(
                ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(providerModule)
            )
        }
    }
}