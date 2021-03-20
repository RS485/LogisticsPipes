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

import logisticspipes.LogisticsPipes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.widget.GhostColorSlot
import network.rs485.logisticspipes.gui.widget.GhostItemSlot
import network.rs485.logisticspipes.gui.widget.GhostSlot

abstract class LPBaseContainer : Container() {

    val slotSize = 18

    /**
     * Add container-specific's DummySlots to the Container's slot list.
     * This should be implemented in a per-container basis.
     * @param dummyInventoryIn  dummy inventory from which the slots will be grabbed.
     * @param startX            starting leftmost position.
     * @param startY            starting bottommost position.
     * @return the list of added lots.
     */
    abstract fun addDummySlotsToContainer(dummyInventoryIn: IInventory, startX: Int, startY: Int): List<GhostSlot>

    /**
     * Add DummySlot to the Container's slot list.
     * @param dummyInventoryIn slot's inventory.
     */
    open fun addGhostItemSlotToContainer(dummyInventoryIn: IInventory, slotId: Int, posX: Int, posY: Int): GhostSlot {
        return addSlotToContainer(GhostItemSlot(dummyInventoryIn, slotId, posX, posY)) as GhostItemSlot
    }

    override fun slotClick(slotId: Int, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer): ItemStack {
        // slotId -999 is a special case for when the ItemStack is being drag-split between slots.
        if (slotId < 0) {
            return super.slotClick(slotId, dragType, clickTypeIn, player)
        }

        val slot = inventorySlots[slotId] ?: return ItemStack.EMPTY
        if (slot !is GhostSlot) {
            // In case slot is not a subtype of GhostSlot vanilla behaviour will be applied.
            return super.slotClick(slotId, dragType, clickTypeIn, player)
        }

        // The slot will always be a subtype of GhostSlot from this point onwards.
        val grabbedItemStack = player.inventory.itemStack
        handleGhostSlotClick(slot, grabbedItemStack, dragType, clickTypeIn, player)
        return ItemStack.EMPTY
    }

    override fun transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack {
        return ItemStack.EMPTY
    }


    /**
     * Add player 9 + 27 slots to the Container's slot list.
     * @param playerInventoryIn player's inventory from which the slots will be grabbed.
     * @param startX starting leftmost position.
     * @param startY starting topmost position.
     * @return  return all the slots in the player's inventory.
     */
    open fun addPlayerSlotsToContainer(playerInventoryIn: IInventory, startX: Int, startY: Int): List<Slot> {

        val playerSlots = mutableListOf<Slot>()

        // Add the hotbar inventory slots
        for (index in 0..8) {
            playerSlots.add(addSlotToContainer(Slot(playerInventoryIn, index, startX + index * slotSize, startY + 3 * slotSize + 4)))
        }

        // Add the top 27 inventory slots
        for (row in 0..2) {
            for (column in 0..8) {
                playerSlots.add(
                    addSlotToContainer(
                        Slot(playerInventoryIn, column + row * 9 + 9, startX + column * slotSize, startY + row * slotSize)
                    )
                )
            }
        }

        return playerSlots
    }

    /**
     * Try to apply slot's ItemStack to first empty target slot if not repeated.
     * @param slotFrom slot from which to get the ItemStack
     * @param targetList list of slots to try and apply ItemStack to
     * @return if succeeded to apply the ItemStack return true
     */
    fun handleShiftClickToGhostSlot(slotFrom: Slot, targetList: List<GhostSlot>): Boolean {
        if (!slotFrom.hasStack) return false
        val itemStack = slotFrom.stack
        if (targetList.none { isStackAppliedToGhostSlot(itemStack, it) }) {
            when (val targetSlot = targetList.first { !it.hasStack }) {
                is GhostColorSlot -> TODO()
                is GhostItemSlot -> applyItemStackToGhostItemSlot(itemStack, targetSlot)
            }
            return true
        }
        return false
    }

    // Handle click on GhostSlot

    private fun handleGhostSlotClick(slot: GhostSlot, grabbedItemStack: ItemStack, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer) = when (slot) {
        is GhostColorSlot -> handleGhostColorSlotClick(slot, grabbedItemStack, dragType, clickTypeIn, player)
        is GhostItemSlot -> handleGhostItemSlotClick(slot, grabbedItemStack, dragType, clickTypeIn, player)
    }

    open fun handleGhostColorSlotClick(slot: GhostColorSlot, grabbedItemStack: ItemStack, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer) {
        TODO("Not yet implemented")
    }

    /**
     * Decide what to do based on the input received
     * @param slot Clicked slot
     * @param grabbedItemStack ItemStack grabbed by the mouse, EMPTY if none.
     * @param dragType stage of multi slot dragging
     * @param clickTypeIn type of action being performed @see ClickType
     * @param player interacting with the container
     */
    open fun handleGhostItemSlotClick(slot: GhostItemSlot, grabbedItemStack: ItemStack, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer) {
        LogisticsPipes.log.info("DragType $dragType, ClickType: $clickTypeIn")
        // Copy the grabbedStack and insert it into the GhostItemSlot
        applyItemStackToGhostItemSlot(grabbedItemStack, slot)
    }

    // Check if ItemStack is already applied onto a GhostSlot

    private fun isStackAppliedToGhostSlot(itemStack: ItemStack, slot: GhostSlot) = when (slot) {
        is GhostColorSlot -> isStackAppliedToGhostColorSlot(itemStack, slot)
        is GhostItemSlot -> isStackAppliedToGhostItemSlot(itemStack, slot)
    }

    /**
     * Check if the ItemStack can be applied to the GhostColorSlot
     * @param itemStack desired ItemStack
     * @param slot target GhostColorSlot
     */
    open fun isStackAppliedToGhostColorSlot(itemStack: ItemStack, slot: GhostColorSlot): Boolean {
        TODO()
    }

    /**
     * Check if the ItemStack can be applied to the GhostItemSlot
     * @param itemStack desired ItemStack
     * @param slot target GhostItemSlot
     */
    open fun isStackAppliedToGhostItemSlot(itemStack: ItemStack, slot: GhostItemSlot): Boolean = slot.hasStack && slot.stack.isItemEqual(itemStack)

    // What to do when an ItemStack is clicked onto a GhostSlot

    /**
     * Copies given ItemStack and gives it to the given GhostItemSlot
     * @param itemStack ItemStack to be copied
     * @param slot target GhostItemSlot
     */
    open fun applyItemStackToGhostItemSlot(itemStack: ItemStack, slot: GhostItemSlot) {
        val copiedItemStack = itemStack.copy()
        slot.putStack(copiedItemStack)
    }
}