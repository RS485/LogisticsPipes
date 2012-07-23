/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht.gui;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.krapht.ItemIdentifier;

public class DummyContainer extends Container{
	
	private final IInventory _playerInventory;
	private final IInventory _dummyInventory;
	
	public DummyContainer(IInventory playerInventory, IInventory dummyInventory){
		_playerInventory = playerInventory;
		_dummyInventory = dummyInventory;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
	
	/***
	 * Adds all slots for the player inventory and hotbar
	 * @param xOffset
	 * @param yOffset
	 */
	public void addNormalSlotsForPlayerInventory(int xOffset, int yOffset){
		if (_playerInventory == null){
			return;
		}
		//Player "backpack"
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 9; column++)
            {
                addSlot(new Slot(_playerInventory, column + row * 9 + 9, xOffset + column * 18, yOffset + row * 18));
            }
        }

        //Player "hotbar"
        for(int i1 = 0; i1 < 9; i1++) {
            addSlot(new Slot(_playerInventory, i1, xOffset + i1 * 18, yOffset + 58));
        }
	}
	
	/**
	 * Add a dummy slot that will not consume players items
	 * @param slotId The slot number in the dummy IInventory this slot should map
	 * @param xCoord xCoord of TopLeft corner of where the slot should be rendered
	 * @param yCoord yCoord of TopLeft corner of where the slot should be rendered
	 */
	public void addDummySlot(int slotId, int xCoord, int yCoord){
		addSlot(new DummySlot(_dummyInventory, slotId, xCoord, yCoord));
	}
	
	public void addNormalSlot(int slotId, IInventory inventory, int xCoord, int yCoord){
		addSlot(new Slot(inventory, slotId, xCoord, yCoord));
	}
	
	public void addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, int ItemID) {
		addSlot(new RestrictedSlot(inventory, slotId, xCoord, yCoord, ItemID));
	}
	
	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassi pipe) {
		addSlot(new ModuleSlot(inventory, slotId, xCoord, yCoord, pipe));
	}
	
	/**
	 * Disable whatever this is 
	 **/
	@Override
	public void updateCraftingResults() {}
	
	/**
	 * Disable shift-clicking to transfer items
	 */
	@Override
	public ItemStack transferStackInSlot(int i)
    {
		return null;
//		Slot slot = (Slot)inventorySlots.get(i);
//		if (slot == null || slot instanceof DummySlot) return null;
//		return super.transferStackInSlot(i);
		//return null;
    }
		
	/**
	 * Clone/clear itemstacks for items
	 */
	@Override
	public ItemStack slotClick(int slotId, int mouseButton, boolean isShift, EntityPlayer entityplayer) {
		if (slotId < 0) return super.slotClick(slotId, mouseButton, isShift, entityplayer);
		Slot slot = (Slot)inventorySlots.get(slotId);
		if (slot == null || !(slot instanceof DummySlot)) {
			ItemStack stack1 = super.slotClick(slotId, mouseButton, isShift, entityplayer);
			ItemStack stack2 = slot.getStack();
			if(stack2 != null && stack2.getItem().shiftedIndex == mod_LogisticsPipes.ItemModuleId + 256) {
				DummyContainerSlopUpdateHelper.update(this,slotId,stack2,entityplayer);
			}
			return stack1;
		}

		InventoryPlayer inventoryplayer = entityplayer.inventory;
		
		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();
		if (currentlyEquippedStack == null){
			if (slot.getStack() != null && mouseButton == 1){
				if (isShift){
					slot.getStack().stackSize = Math.min(127, slot.getStack().stackSize * 2);
				} else {
					slot.getStack().stackSize/=2;
				}
			}else{
				slot.putStack(null);
			}
			return currentlyEquippedStack;
		}
		
		if (!slot.getHasStack()){
			slot.putStack(currentlyEquippedStack.copy());
			if (mouseButton == 1) {
				slot.getStack().stackSize = 1;
			}
			if (slot.getStack().stackSize > slot.getSlotStackLimit()){
				slot.getStack().stackSize = slot.getSlotStackLimit();
			}
			
			return currentlyEquippedStack;
		}
		
		ItemIdentifier currentItem = ItemIdentifier.get(currentlyEquippedStack);
		ItemIdentifier slotItem = ItemIdentifier.get(slot.getStack());
		if (currentItem == slotItem){
			//Do manual shift-checking to play nice with NEI
			int counter = isShift?10:1;
			if (mouseButton == 1 && slot.getStack().stackSize + counter <= slot.getSlotStackLimit()){
				slot.getStack().stackSize += counter;
				return currentlyEquippedStack;
			}
			if (mouseButton == 0){
				if (slot.getStack().stackSize - counter > 0){
					slot.getStack().stackSize-=counter;	
				} else {
					slot.putStack(null);
				}
				return currentlyEquippedStack;
			} 
		} else {
			slot.putStack(currentlyEquippedStack.copy());
		}
		return currentlyEquippedStack;
	}
	
	@Override
	protected void retrySlotClick(int i, int j, boolean flag,
			EntityPlayer entityplayer) {
		
	}
}
