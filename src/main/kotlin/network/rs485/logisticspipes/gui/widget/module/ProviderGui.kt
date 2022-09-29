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
import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.inventory.ProviderMode
import network.rs485.logisticspipes.property.BooleanProperty
import network.rs485.logisticspipes.property.EnumProperty
import network.rs485.logisticspipes.property.PropertyLayer
import network.rs485.logisticspipes.util.TextUtil

// TODO create different buttons.
class ProviderGui private constructor(
    private val providerModule: ModuleProvider,
    private val providerContainer: ProviderContainer,
    private val propertyLayer: PropertyLayer,
) : LPBaseGuiContainer(providerContainer, 174, 177) {

    companion object {
        @JvmStatic
        fun create(playerInventory: IInventory, providerModule: ModuleProvider, lockedStack: ItemStack): ProviderGui {
            val propertyLayer = PropertyLayer(providerModule.propertyList)
            val filterInventoryOverlay = propertyLayer.overlay(providerModule.filterInventory)
            // FIXME: we don't know if read or write, so write is the fallback -- overlay needs IInventory compatibility. Ben will work on this
            val gui = filterInventoryOverlay.write { filterInventory ->
                ProviderGui(
                    providerModule = providerModule,
                    providerContainer = ProviderContainer(
                        playerInventoryIn = playerInventory,
                        filterInventoryIn = filterInventory,
                        moduleInHand = lockedStack,
                    ),
                    propertyLayer = propertyLayer,
                )
            }
            return gui
        }
    }

    private val prefix: String = "gui.providerpipe."

    private val providerMode = propertyLayer.overlay(providerModule.providerMode)
    private val isExclusionFilter = propertyLayer.overlay(providerModule.isExclusionFilter)

    override val widgets = widgetContainer {
        staticLabel {
            margin = Margin(top = 6, left = 6, right = 6)
            text = providerModule.filterInventory.name
            textAlignment = HorizontalAlignment.CENTER
            textColor = LPGuiDrawer.TEXT_DARK
            extendable = LPGuiDrawer.BACKGROUND_LIGHT
        }
        horizontal {
            button {
                width = 50
                height = 20
                margin = Margin(left = 6, top = 35)
                text = TextUtil.translate("${prefix}Switch")
                action = { providerMode.write { it.next() } }
            }
            customSlots {
                margin = Margin(top = 18)
                slots = providerContainer.filterSlots
                columns = 3
                rows = 3
            }
            propertyButton<Boolean, BooleanProperty> {
                width = 50
                height = 20
                margin = Margin(right = 6, top = 35)
                property = providerModule.isExclusionFilter
                propertyLayer = this@ProviderGui.propertyLayer
                propertyToText = { isExclude ->
                    if (isExclude) {
                        TextUtil.translate("${prefix}Exclude")
                    } else {
                        TextUtil.translate("${prefix}Include")
                    }
                }
                text = propertyToText(isExclusionFilter.get())
                action = { isExclusionFilter.write { it.toggle() } }
            }
        }
        label<ProviderMode, EnumProperty<ProviderMode>> {
            margin = Margin(left = 6, top = 80, right = 6)
            property = providerModule.providerMode
            propertyLayer = this@ProviderGui.propertyLayer
            propertyToText = { providerMode ->
                TextUtil.translate("${prefix}ExcessInventory") + " " +
                        TextUtil.translate(providerMode.modeTranslationKey)
            }
            text = propertyToText(providerMode.get())
        }
        playerSlots {
            margin = Margin(bottom = 6)
            slots = providerContainer.playerSlots
        }
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
