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
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.LPBaseContainer
import network.rs485.logisticspipes.gui.widget.GhostItemSlot
import network.rs485.logisticspipes.gui.widget.GhostSlot

class ProviderContainer(
    playerInventoryIn: IInventory,
    providerModule: ModuleProvider,
    moduleInHand: ItemStack) : LPBaseContainer() {

    val playerSlots = addPlayerSlotsToContainer(playerInventoryIn, 0, 0, moduleInHand)
    val filterSlots = addDummySlotsToContainer(providerModule.filterInventory, 0, 0)

    // Add 3x3 grid of dummy slots.
    override fun addDummySlotsToContainer(dummyInventoryIn: IInventory, startX: Int, startY: Int): List<GhostSlot> {

        val filterSlots = mutableListOf<GhostSlot>()

        for (row in 0..2) {
            for (column in 0..2) {
                filterSlots.add(
                    addGhostItemSlotToContainer(
                        dummyInventoryIn = dummyInventoryIn,
                        slotId = column + row * 3,
                        posX = startX + column * slotSize,
                        posY = startY + row * slotSize
                    )
                )
            }
        }

        return filterSlots
    }

    override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack {

        // Try to add to filter inventory
        val slot = inventorySlots[index]
        if(playerSlots.contains(slot) && filterSlots.none { it.stack.isItemEqual(slot.stack) } && filterSlots.any { !it.hasStack } ){
            filterSlots.firstOrNull { targetSlot -> !targetSlot.hasStack }?.also { ghostSlot ->
                applyItemStackToGhostItemSlot(slot.stack, ghostSlot as GhostItemSlot)
                return ItemStack.EMPTY
            }
        }

        return super.transferStackInSlot(player, index)
    }

    override fun canInteractWith(playerIn: EntityPlayer): Boolean = true

    override fun applyItemStackToGhostItemSlot(itemStack: ItemStack, slot: GhostItemSlot) {
        val copiedStack = itemStack.copy().apply { count = 1 }
        slot.putStack(copiedStack)
    }
}