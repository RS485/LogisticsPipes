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
import network.rs485.logisticspipes.gui.widget.FuzzySelectionWidget
import network.rs485.logisticspipes.inventory.container.ItemSinkContainer
import network.rs485.logisticspipes.property.BooleanProperty
import network.rs485.logisticspipes.property.ItemIdentifierInventoryProperty
import network.rs485.logisticspipes.property.layer.PropertyLayer
import network.rs485.logisticspipes.util.IRectangle
import network.rs485.logisticspipes.util.TextUtil
import logisticspipes.modules.ModuleItemSink
import logisticspipes.network.PacketHandler
import logisticspipes.network.packets.module.ItemSinkImportPacket
import logisticspipes.network.packets.module.ModulePropertiesUpdate
import logisticspipes.proxy.MainProxy
import logisticspipes.utils.Color
import logisticspipes.utils.item.ItemIdentifier
import mezz.jei.api.gui.IGhostIngredientHandler
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import java.awt.Rectangle
import java.util.concurrent.atomic.AtomicReference

class ItemSinkWidgetScreen(private val guiReference: AtomicReference<ItemSinkGui>) : WidgetScreen() {
    override fun constructWidgetContainer() = widgetContainer {
        val gui = guiReference.get() ?: return@widgetContainer
        margin = Margin.DEFAULT
        staticLabel {
            text = gui.itemSinkModule.filterInventory.name
            textAlignment = HorizontalAlignment.CENTER
            textColor = Color.TEXT_DARK.value
        }
        customSlots {
            slots = gui.itemSinkContainer.filterSlots
            columns = 9
            rows = 1
        }
        horizontal {
            margin = Margin(top = 3, bottom = 3)
            optionalComponent {
                predicate = { gui.inHand }
                inactiveComponents {
                    button {
                        text = TextUtil.translate("${gui.prefix}import")
                        action = {
                            MainProxy.sendPacketToServer(
                                PacketHandler.getPacket(ItemSinkImportPacket::class.java).setModulePos(gui.itemSinkModule),
                            )
                        }
                        enabled = true // TODO disable button if there is no attached inventory!
                    }
                }
            }
            staticLabel {
                text = "${TextUtil.translate("${gui.prefix}Defaultroute")}:"
                textColor = Color.TEXT_DARK.value
                textAlignment = HorizontalAlignment.RIGHT
                verticalAlignment = VerticalAlignment.CENTER
                horizontalSize = Size.MIN
            }
            propertyButton<Boolean, BooleanProperty> {
                property = gui.itemSinkModule.defaultRoute
                propertyLayer = gui.propertyLayer
                propertyToText = { isDefaultRoute ->
                    if (isDefaultRoute) {
                        TextUtil.translate("${gui.prefix}Yes")
                    } else {
                        TextUtil.translate("${gui.prefix}No")
                    }
                }
                text = propertyToText(gui.defaultRouteOverlay.get())
                action = { gui.defaultRouteOverlay.write { it.toggle() } }
            }
        }
        playerSlots {
            slots = gui.itemSinkContainer.playerSlots
        }
    }

}

class ItemSinkGui private constructor(
    internal val itemSinkModule: ModuleItemSink,
    internal val itemSinkContainer: ItemSinkContainer,
    internal val propertyLayer: PropertyLayer,
    internal val inHand: Boolean,
    guiReference: AtomicReference<ItemSinkGui> = AtomicReference(),
) : BaseGuiContainer(itemSinkContainer, widgetScreen = ItemSinkWidgetScreen(guiReference)) {

    companion object {
        @JvmStatic
        fun create(
            playerInventory: IInventory,
            itemSinkModule: ModuleItemSink,
            lockedStack: ItemStack,
            isFuzzy: Boolean,
            inHand: Boolean,
        ): ItemSinkGui {
            val propertyLayer = PropertyLayer(itemSinkModule.properties)
            val filterInventoryOverlay = propertyLayer.overlay(itemSinkModule.filterInventory)
            // FIXME: we don't know if read or write, so write is the fallback -- overlay needs IInventory compatibility. Ben will work on this
            val gui = filterInventoryOverlay.write { filterInventory ->
                ItemSinkGui(
                    itemSinkModule = itemSinkModule,
                    itemSinkContainer = ItemSinkContainer(
                        playerInventory = playerInventory,
                        filterInventory = filterInventory,
                        itemSinkModule = itemSinkModule,
                        propertyLayer = propertyLayer,
                        isFuzzy = isFuzzy,
                        moduleInHand = lockedStack,
                    ),
                    propertyLayer = propertyLayer,
                    inHand = inHand,
                )
            }
            return gui
        }
    }

    internal val prefix: String = "gui.itemsink."

    internal val defaultRouteOverlay = propertyLayer.overlay(itemSinkModule.defaultRoute)
    private val filterInventoryOverlay = propertyLayer.overlay(itemSinkModule.filterInventory)

    override val fuzzySelector = FuzzySelectionWidget(this, itemSinkContainer.fuzzyFlagOverlay)

    init {
        guiReference.set(this)
    }

    fun importFromInventory(importedItems: List<ItemIdentifier>) {
        if (importedItems.isEmpty()) return
        filterInventoryOverlay.write { filterInventory: ItemIdentifierInventoryProperty ->
            for (i in filterInventory.indices) {
                if (i < importedItems.size) {
                    filterInventory.setInventorySlotContents(i, importedItems[i].makeStack(1))
                } else {
                    filterInventory.setInventorySlotContents(i, ItemStack.EMPTY)
                }
            }
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        propertyLayer.unregister()
        if (mc.player != null && propertyLayer.properties.isNotEmpty()) {
            // send update to server, when there are changed properties
            MainProxy.sendPacketToServer(
                ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(itemSinkModule),
            )
        }
    }

    override fun <I : Any?> getFilterSlots(): MutableList<IGhostIngredientHandler.Target<I>> {
        return itemSinkContainer.filterSlots.map { slot ->
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

    override fun getExtraGuiAreas(): List<IRectangle> {
        return if (fuzzySelector.active) {
            listOf(fuzzySelector.relativeBody)
        } else {
            emptyList()
        }
    }

}
