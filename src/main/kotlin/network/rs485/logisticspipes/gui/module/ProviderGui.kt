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

package network.rs485.logisticspipes.gui.module

import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.inventory.ProviderMode
import network.rs485.logisticspipes.inventory.container.ProviderContainer
import network.rs485.logisticspipes.property.BooleanProperty
import network.rs485.logisticspipes.property.EnumProperty
import network.rs485.logisticspipes.property.layer.PropertyLayer
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import logisticspipes.modules.ModuleProvider
import logisticspipes.network.packets.module.ModulePropertiesUpdate
import logisticspipes.proxy.MainProxy
import logisticspipes.utils.Color
import mezz.jei.api.gui.IGhostIngredientHandler
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import java.awt.Rectangle
import java.util.concurrent.atomic.AtomicReference


class ProviderWidgetScreen(private val guiReference: AtomicReference<ProviderGui>) : WidgetScreen() {
    override fun constructWidgetContainer(): ComponentContainer = widgetContainer {
        val gui = guiReference.get() ?: return@widgetContainer
        margin = Margin.DEFAULT
        staticLabel {
            text = gui.providerModule.filterInventory.name
            textAlignment = HorizontalAlignment.CENTER
            textColor = Color.TEXT_DARK.value
            extendable = true
            backgroundColor = Color.BACKGROUND_LIGHT.value
            horizontalSize = Size.GROW
        }
        horizontal {
            gap = 6
            button {
                text = TextUtil.translate("${gui.prefix}Switch")
                action = { gui.providerMode.write { it.next() } }
                verticalAlignment = VerticalAlignment.CENTER
            }
            customSlots {
                slots = gui.providerContainer.filterSlots
                columns = 3
                rows = 3
                verticalAlignment = VerticalAlignment.CENTER
            }
            propertyButton<Boolean, BooleanProperty> {
                property = gui.providerModule.isExclusionFilter
                propertyLayer = gui.propertyLayer
                propertyToText = { isExclude ->
                    if (isExclude) {
                        TextUtil.translate("${gui.prefix}Exclude")
                    } else {
                        TextUtil.translate("${gui.prefix}Include")
                    }
                }
                text = propertyToText(gui.isExclusionFilter.get())
                action = { gui.isExclusionFilter.write { it.toggle() } }
                verticalAlignment = VerticalAlignment.CENTER
            }
        }
        staticLabel {
            text = TextUtil.translate("${gui.prefix}ExcessInventory")
            textAlignment = HorizontalAlignment.LEFT
            textColor = Color.TEXT_DARK.value
        }
        label<ProviderMode, EnumProperty<ProviderMode>> {
            property = gui.providerModule.providerMode
            propertyLayer = gui.propertyLayer
            textAlignment = HorizontalAlignment.LEFT
            propertyToText = { providerMode ->
                TextUtil.translate(providerMode.modeTranslationKey)
            }
            textColor = Color.TEXT_DARK.value
            text = propertyToText(gui.providerMode.get())
        }
        playerSlots {
            slots = gui.providerContainer.playerSlots
        }
    }
}

// TODO create different buttons.
class ProviderGui private constructor(
    internal val providerModule: ModuleProvider,
    internal val providerContainer: ProviderContainer,
    internal val propertyLayer: PropertyLayer,
    guiReference: AtomicReference<ProviderGui> = AtomicReference(),
) : BaseGuiContainer(providerContainer, widgetScreen = ProviderWidgetScreen(guiReference)) {

    companion object {
        @JvmStatic
        fun create(playerInventory: IInventory, providerModule: ModuleProvider, lockedStack: ItemStack): ProviderGui {
            val propertyLayer = PropertyLayer(providerModule.propertyList)
            val filterInventoryOverlay = propertyLayer.overlay(providerModule.filterInventory)
            return ProviderGui(
                providerModule = providerModule,
                providerContainer = ProviderContainer(
                    providerModule = providerModule,
                    playerInventoryIn = playerInventory,
                    filterInventoryPropertyOverlay = filterInventoryOverlay,
                    moduleInHand = lockedStack,
                ),
                propertyLayer = propertyLayer,
            )
        }
    }

    internal val prefix: String = "gui.providerpipe."

    internal val providerMode = propertyLayer.overlay(providerModule.providerMode)
    internal val isExclusionFilter = propertyLayer.overlay(providerModule.isExclusionFilter)

    init {
        guiReference.set(this)
    }

    override fun drawFocalgroundLayer(mouseX: Float, mouseY: Float, partialTicks: Float) {
        for (guiButton in buttonList) {
            guiButton.drawButton(mc, mouseX.toInt(), mouseY.toInt(), partialTicks)
        }
    }

    override fun <I> getFilterSlots(): MutableList<IGhostIngredientHandler.Target<I>> {
        // TODO create method to turn list of filter slots into list of Target<I>
        return providerContainer.filterSlots.map { slot ->
            object : IGhostIngredientHandler.Target<I> {
                override fun accept(ingredient: I) {
                    if (ingredient is ItemStack) {
                        slot.putStack(ingredient)
                    }
                }

                override fun getArea(): Rectangle = Rectangle(guiLeft + slot.xPos, guiTop + slot.yPos, 17, 17)
            }
        }.toMutableList()
    }

    override fun getExtraGuiAreas(): List<IRectangle> = emptyList()

    override fun onGuiClosed() {
        super.onGuiClosed()
        propertyLayer.unregister()
        if (mc.player != null && propertyLayer.properties.isNotEmpty()) {
            // send update to server, when there are changed properties
            MainProxy.sendPacketToServer(
                ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(providerModule),
            )
        }
    }
}
