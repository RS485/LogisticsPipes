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

package network.rs485.logisticspipes.inventory.container

import network.rs485.logisticspipes.gui.widget.GhostItemSlot
import network.rs485.logisticspipes.gui.widget.GhostSlot
import network.rs485.logisticspipes.property.InventoryProperty
import network.rs485.logisticspipes.property.layer.PropertyOverlay
import network.rs485.logisticspipes.property.layer.PropertyOverlayInventoryAdapter
import logisticspipes.modules.ModuleProvider
import logisticspipes.utils.item.ItemIdentifierInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

class ProviderContainer(
    providerModule: ModuleProvider,
    playerInventoryIn: IInventory,
    filterInventoryOverlay: PropertyOverlay<ItemIdentifierInventory, out InventoryProperty<ItemIdentifierInventory>>,
    moduleInHand: ItemStack,
) : LPBaseContainer<ModuleProvider>(providerModule) {

    val playerSlots = addPlayerSlotsToContainer(playerInventoryIn, 0, 0, moduleInHand)

    val filterSlots = addDummySlotsToContainer(
        overlayInventory = PropertyOverlayInventoryAdapter(filterInventoryOverlay),
        baseProperty = module.filterInventory,
        startX = 0,
        startY = 0,
    )

    // Add 3x3 grid of dummy slots.
    override fun addDummySlotsToContainer(
        overlayInventory: IInventory,
        baseProperty: InventoryProperty<*>?,
        startX: Int,
        startY: Int
    ): List<GhostSlot> {
        val filterSlots = mutableListOf<GhostSlot>()

        for (row in 0..2) {
            for (column in 0..2) {
                filterSlots.add(
                    addGhostItemSlotToContainer(
                        dummyInventoryIn = overlayInventory,
                        baseProperty = baseProperty,
                        slotId = column + row * 3,
                        posX = startX + column * slotSize,
                        posY = startY + row * slotSize,
                    ),
                )
            }
        }

        return filterSlots
    }

    override fun tryTransferSlotToGhostSlot(slotIdx: Int): Boolean {
        val playerInvSlot = inventorySlots.getOrNull(slotIdx)?.takeIf { playerSlots.contains(it) } ?: return false
        var firstFreeSlotId = Int.MAX_VALUE
        for (filterSlot in filterSlots.withIndex()) {
            if (filterSlot.value.hasStack) {
                if (filterSlot.value.stack.isItemEqual(playerInvSlot.stack)) {
                    // item already in filter slots
                    return false
                }
            } else {
                firstFreeSlotId = minOf(firstFreeSlotId, filterSlot.index)
            }
        }

        return firstFreeSlotId.takeIf { it in filterSlots.indices }
            ?.let { filterSlots[it] as? GhostItemSlot }
            ?.let { firstFreeSlot ->
                applyItemStackToGhostItemSlot(playerInvSlot.stack, firstFreeSlot)
                true
            }
            ?: false
    }

    override fun canInteractWith(playerIn: EntityPlayer): Boolean = true

    override fun applyItemStackToGhostItemSlot(itemStack: ItemStack, slot: GhostSlot) {
        val copiedStack = itemStack.copy().apply { count = 1 }
        slot.putStack(copiedStack)
    }
}
