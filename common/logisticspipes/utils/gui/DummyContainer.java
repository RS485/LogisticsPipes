/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DummyContainer extends Container{
	
	private final IInventory _playerInventory;
	private final IInventory _dummyInventory;
	private final IGuiOpenControler _controler;

	public DummyContainer(IInventory playerInventory, IInventory dummyInventory){
		_playerInventory = playerInventory;
		_dummyInventory = dummyInventory;
		_controler = null;
	}

	public DummyContainer(EntityPlayer player, IInventory dummyInventory, IGuiOpenControler controler){
		_playerInventory = player.inventory;
		_dummyInventory = dummyInventory;
		_controler = controler;
		_controler.guiOpenedByPlayer(player);
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
                addSlotToContainer(new Slot(_playerInventory, column + row * 9 + 9, xOffset + column * 18, yOffset + row * 18));
            }
        }

        //Player "hotbar"
        for(int i1 = 0; i1 < 9; i1++) {
        	addSlotToContainer(new Slot(_playerInventory, i1, xOffset + i1 * 18, yOffset + 58));
        }
	}
	
	/**
	 * Add a dummy slot that will not consume players items
	 * @param slotId The slot number in the dummy IInventory this slot should map
	 * @param xCoord xCoord of TopLeft corner of where the slot should be rendered
	 * @param yCoord yCoord of TopLeft corner of where the slot should be rendered
	 */
	public void addDummySlot(int slotId, int xCoord, int yCoord){
		addSlotToContainer(new DummySlot(_dummyInventory, slotId, xCoord, yCoord));
	}
	
	public void addNormalSlot(int slotId, IInventory inventory, int xCoord, int yCoord){
		addSlotToContainer(new Slot(inventory, slotId, xCoord, yCoord));
	}

	public void addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, int ItemID) {
		addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, ItemID));
	}

	public void addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck) {
		addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck));
	}
	
	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassi pipe) {
		addSlotToContainer(new ModuleSlot(inventory, slotId, xCoord, yCoord, pipe));
	}
	
	/**
	 * Disable whatever this is 
	 **/
	//@Override
	//public void updateCraftingResults() {}
	
	/**
	 * Disable shift-clicking to transfer items
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int i)
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
	public ItemStack slotClick(int slotId, int mouseButton, int isShift, EntityPlayer entityplayer) {
		if (slotId < 0) return super.slotClick(slotId, mouseButton, isShift, entityplayer);
		Slot slot = (Slot)inventorySlots.get(slotId);
		if (slot == null || (!(slot instanceof DummySlot) && !(slot instanceof UnmodifiableSlot))) {
			ItemStack stack1 = super.slotClick(slotId, mouseButton, isShift, entityplayer);
			ItemStack stack2 = slot.getStack();
			if(stack2 != null && stack2.getItem().shiftedIndex == LogisticsPipes.ModuleItem.shiftedIndex) {
				if(entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
					((EntityPlayerMP)entityplayer).sendSlotContents(this, slotId, stack2);
				}
			}
			return stack1;
		}
		
		InventoryPlayer inventoryplayer = entityplayer.inventory;
		
		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();
		
		if(slot instanceof UnmodifiableSlot) {
			return currentlyEquippedStack;
		}
		
		if (currentlyEquippedStack == null){
			if (slot.getStack() != null && mouseButton == 1){
				if (isShift == 1){
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
			int counter = isShift == 1?10:1;
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
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
		if(_controler != null) {
			_controler.guiClosedByPlayer(par1EntityPlayer);
		}
		super.onCraftGuiClosed(par1EntityPlayer);
	}

	@Override
	protected void retrySlotClick(int i, int j, boolean flag,
			EntityPlayer entityplayer) {
		
	}

	public void addRestrictedHotbarForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null){
			return;
		}
		//Player "hotbar"
        for(int i1 = 0; i1 < 9; i1++) {
        	addSlotToContainer(new UnmodifiableSlot(_playerInventory, i1, xOffset + i1 * 18, yOffset));
        }
	}
}
