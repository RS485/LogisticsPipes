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

import logisticspipes.modules.ModuleItemSink
import logisticspipes.network.PacketHandler
import logisticspipes.network.packets.module.ItemSinkImportPacket
import logisticspipes.network.packets.module.ModulePropertiesUpdate
import logisticspipes.proxy.MainProxy
import logisticspipes.utils.Color
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.*
import network.rs485.logisticspipes.property.BooleanProperty
import network.rs485.logisticspipes.property.PropertyLayer
import network.rs485.logisticspipes.util.TextUtil

class ItemSinkGui private constructor(
    private val itemSinkModule: ModuleItemSink,
    private val itemSinkContainer: ItemSinkContainer,
    private val propertyLayer: PropertyLayer,
    private val inHand: Boolean,
) : LPBaseGuiContainer(itemSinkContainer) {

    companion object {
        @JvmStatic
        fun create(playerInventory: IInventory, itemSinkModule: ModuleItemSink, lockedStack: ItemStack, isFuzzy: Boolean, inHand: Boolean): ItemSinkGui {
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
                        isFuzzy = isFuzzy,
                        moduleInHand = lockedStack,
                    ),
                    propertyLayer = propertyLayer,
                    inHand = inHand
                )
            }
            return gui
        }
    }

    private val prefix: String = "gui.itemsink."

    private val defaultRoute = propertyLayer.overlay(itemSinkModule.defaultRoute)

    override val widgets = widgetContainer {
        margin = Margin.DEFAULT
        staticLabel {
            text = itemSinkModule.filterInventory.name
            textAlignment = HorizontalAlignment.CENTER
            textColor = Color.TEXT_DARK.value
        }
        customSlots {
            slots = itemSinkContainer.filterSlots
            columns = 9
            rows = 1
        }
        horizontal {
            margin = Margin(top = 3, bottom = 3)
            button {
                text = TextUtil.translate("${prefix}import")
                action = {
                    if (!inHand) {
                        MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkImportPacket::class.java).setModulePos(itemSinkModule))
                    }
                }
                enabled = !inHand
            }
            staticLabel {
                text = "${TextUtil.translate("${prefix}Defaultroute")}:"
                textColor = Color.TEXT_DARK.value
                textAlignment = HorizontalAlignment.RIGHT
                verticalAlignment = VerticalAlignment.CENTER
                horizontalSize = Size.MIN
            }
            propertyButton<Boolean, BooleanProperty> {
                property = itemSinkModule.defaultRoute
                propertyLayer = this@ItemSinkGui.propertyLayer
                propertyToText = { isDefaultRoute ->
                    if (isDefaultRoute) {
                        TextUtil.translate("${prefix}Yes")
                    } else {
                        TextUtil.translate("${prefix}No")
                    }
                }
                text = propertyToText(defaultRoute.get())
                action = { defaultRoute.write { it.toggle() } }
            }
        }
        playerSlots {
            slots = itemSinkContainer.playerSlots
        }
    }

    fun importFromInventory(importedItems: List<ItemIdentifier>) {
        if (importedItems.isEmpty()) return
        // Todo: implement after ben's fix
        //itemSinkModule.importFromInventory(importedItems.distinctBy {
        //    it.item
        //}.take(9))
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        propertyLayer.unregister()
        if (mc.player != null && propertyLayer.properties.isNotEmpty()) {
            // send update to server, when there are changed properties
            MainProxy.sendPacketToServer(
                ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(itemSinkModule)
            )
        }
    }

}
