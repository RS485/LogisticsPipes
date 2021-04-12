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
import logisticspipes.utils.gui.ModuleSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.guidebook.MouseInteractable
import network.rs485.logisticspipes.gui.widget.GhostItemSlot
import network.rs485.logisticspipes.gui.widget.GhostSlot
import network.rs485.logisticspipes.gui.widget.LockedSlot
import kotlin.math.min

abstract class LPBaseContainer : Container() {

    val slotSize = 18

    val playerHotbarSlots: MutableList<Slot> = mutableListOf()
    val playerBackpackSlots: MutableList<Slot> = mutableListOf()

    var selectedChild: MouseInteractable? = null

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
     * @param slotId id to be given to the slot
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

        if (slot is LockedSlot) return ItemStack.EMPTY

        if (slot !is GhostSlot) {
            // In case slot is not a subtype of GhostSlot vanilla behaviour will be applied.
            return super.slotClick(slotId, dragType, clickTypeIn, player)
        }

        // The slot will always be a subtype of GhostSlot from this point onwards.
        val grabbedItemStack = player.inventory.itemStack
        handleGhostSlotClick(slot, grabbedItemStack, dragType, clickTypeIn, player)
        return ItemStack.EMPTY

    }

    /**
     * Transfers items back and forth from the player's backpack and hotbar.
     * Override and return this to add extra functionality.
     * @param player player accessing the container
     * @param index of the slot in the inventorySlots list.
     * @return empty ItemStack to stop the weird loop that vanilla runs using this.
     */
    override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack {
        val slot = inventorySlots[index] ?: return ItemStack.EMPTY
        if (!slot.hasStack || slot is GhostSlot || slot is ModuleSlot) return ItemStack.EMPTY
        when {
            playerHotbarSlots.contains(slot) -> handleShiftClickFromSlotToList(slot, playerBackpackSlots, player)
            playerBackpackSlots.contains(slot) -> handleShiftClickFromSlotToList(slot, playerHotbarSlots, player)
            else -> LogisticsPipes.log.warn("Something is wrong, this slot is not apart of the player's inventory and wasn't dealt with properly before.")
        }
        return ItemStack.EMPTY
    }

    /**
     * Attempts to transfer the stack to one or more slots in the list.
     * @param from slot sending stack
     * @param toList slots possibly receiving stack
     * @param player player interacting with the container
     * @return true when the shifted stack was fully utilized, false otherwise
     */
    open fun handleShiftClickFromSlotToList(from: Slot, toList: List<Slot>, player: EntityPlayer): Boolean {
        if (!from.hasStack) return true
        val slots = toList.partition { it.hasStack }
        // Iterate through all non-empty slots and then through all empty slots until the initial slot is depleted or it fails.
        slots.first.takeIf { it.isNotEmpty() }?.forEach { to -> if(handleShiftClickFromSlotToSlot(from, to, player)) return true }
        slots.second.takeIf { it.isNotEmpty() }?.forEach { to -> if(handleShiftClickFromSlotToSlot(from, to, player)) return true }
        return false
    }

    /**
     * Attempts to shift a stack from one stack from the other.
     * @param from slot sending stack
     * @param to slot receiving stack
     * @param player player interacting with the container
     * @return true when from is empty false when no action taken
     */
    open fun handleShiftClickFromSlotToSlot(from: Slot, to: Slot, player: EntityPlayer): Boolean {
        if (!from.hasStack) return true
        if (to is GhostSlot || to is ModuleSlot) return false
        if(to.hasStack && from.stack.isItemEqual(to.stack)){
            // Calculate how many items can be added to stack until it is full, can be limited by the ItemStack(Item) or the Slot.
            val freeAmount = min(to.slotStackLimit, to.stack.maxStackSize) - to.stack.count
            if (freeAmount > 0) {
                // Reduce original from stack and do the same on the slot to sync.
                var shiftedStack = from.decrStackSize(freeAmount)
                shiftedStack = from.onTake(player, shiftedStack)
                if(!shiftedStack.isEmpty && !to.stack.isEmpty){
                    // Increase count on the receiving stack and also slot to sync.
                    to.stack.grow(shiftedStack.count)
                    to.putStack(to.stack)
                    return !from.hasStack
                }
            }
        } else if(!to.hasStack) {
            // Calculate how much can be added to empty slot
            val maxAmount = min(from.stack.count, to.slotStackLimit)
            if(maxAmount > 0){
                val shiftedStack = from.decrStackSize(maxAmount)
                to.putStack(shiftedStack)
                if(from.stack.isEmpty) from.putStack(ItemStack.EMPTY)
            }
            return !from.hasStack
        }
        return false
    }

    /**
     * Add player 9 + 27 slots to the Container's slot list.
     * @param playerInventoryIn player's inventory from which the slots will be grabbed.
     * @param startX starting leftmost position.
     * @param startY starting topmost position.
     * @return  return all the slots in the player's inventory.
     */
    open fun addPlayerSlotsToContainer(playerInventoryIn: IInventory, startX: Int, startY: Int, lockedStack: ItemStack): List<Slot> {

        // Minecraft expects the 27 backpack slots to be index in 0-26 on the container list, and the hotbar
        // corresponds to 27-35.

        // Add the top 27 inventory slots
        for (row in 0..2) {
            for (column in 0..8) {
                playerBackpackSlots.add(
                    addSlotToContainer(
                        Slot(playerInventoryIn, column + row * 9 + 9, startX + column * slotSize, startY + row * slotSize)
                    )
                )
            }
        }

        // Add the hotbar inventory slots
        for (index in 0..8) {
            if (!lockedStack.isEmpty && playerInventoryIn.getStackInSlot(index) == lockedStack) {
                playerHotbarSlots.add(addSlotToContainer(LockedSlot(playerInventoryIn, index, startX + index * slotSize, startY + 3 * slotSize + 4)))
            } else {
                playerHotbarSlots.add(addSlotToContainer(Slot(playerInventoryIn, index, startX + index * slotSize, startY + 3 * slotSize + 4)))
            }
        }

        return playerBackpackSlots + playerHotbarSlots
    }

    // Handle click on GhostSlot
    private fun handleGhostSlotClick(slot: GhostSlot, grabbedItemStack: ItemStack, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer) = when (slot) {
        is GhostItemSlot -> handleGhostItemSlotClick(slot, grabbedItemStack, dragType, clickTypeIn, player)
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