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
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Colors;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.FluidIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class DummyContainer extends Container{
	
	protected IInventory _playerInventory;
	protected IInventory _dummyInventory;
	protected IGuiOpenControler _controler;

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
	public void addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, int ItemID, int stackLimit) {
		addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, ItemID, stackLimit));
	}

	public void addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck) {
		addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck));
	}

	public void addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck, int stackLimit) {
		addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck, stackLimit));
	}

	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassi pipe) {
		addSlotToContainer(new ModuleSlot(inventory, slotId, xCoord, yCoord, pipe));
	}
	
	public void addFluidSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		addSlotToContainer(new FluidSlot(inventory, slotId, xCoord, yCoord));
	}
	
	public void addColorSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		addSlotToContainer(new ColorSlot(inventory, slotId, xCoord, yCoord));
	}

	public void addUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		addSlotToContainer(new UnmodifiableSlot(inventory, slotId, xCoord, yCoord));
	}
	
	public void addCallableSlotHandler(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotClick handler) {
		addSlotToContainer(new HandelableSlot(inventory, slotId, xCoord, yCoord, handler));
	}
	
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
		if (slot == null || (!(slot instanceof DummySlot) && !(slot instanceof UnmodifiableSlot) && !(slot instanceof FluidSlot) && !(slot instanceof ColorSlot) && !(slot instanceof HandelableSlot))) {
			ItemStack stack1 = super.slotClick(slotId, mouseButton, isShift, entityplayer);
			ItemStack stack2 = slot.getStack();
			if(stack2 != null && stack2.getItem().itemID == LogisticsPipes.ModuleItem.itemID) {
				if(entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
					((EntityPlayerMP)entityplayer).sendSlotContents(this, slotId, stack2);
				}
			}
			return stack1;
		}
		
		InventoryPlayer inventoryplayer = entityplayer.inventory;
		
		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();
		
		if(slot instanceof HandelableSlot) {
			if(currentlyEquippedStack == null) {
				return ((HandelableSlot)slot).getProvidedStack();
			}
			return currentlyEquippedStack;
		}
		
		if(slot instanceof UnmodifiableSlot) {
			return currentlyEquippedStack;
		}
		//we get a leftclick *and* a doubleclick message if there's a doubleclick with no item on the pointer, filter it out
		if(currentlyEquippedStack == null && isShift == 6) {
			return currentlyEquippedStack;
		}
		
		if(slot instanceof FluidSlot) {
			if(currentlyEquippedStack != null) {
				FluidStack liquidId = FluidContainerRegistry.getFluidForFilledItem(currentlyEquippedStack);
				if (liquidId != null) {
					FluidIdentifier ident = FluidIdentifier.get(liquidId);
					if(mouseButton == 0) {
						if(ident == null) {
							slot.putStack(null);
						} else {
							slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
						}
					} else {
						slot.putStack(null);
					}
					return currentlyEquippedStack;
				}
			}
			FluidIdentifier ident = null;
			if(slot.getStack() != null) {
				ident = ItemIdentifier.get(slot.getStack()).getFluidIdentifier();
			}
			if(mouseButton == 0) {
				if(ident != null) {
					ident = ident.next();
				} else {
					ident = FluidIdentifier.first();
				}
			} else if(mouseButton == 1) {
				if(ident != null) {
					ident = ident.prev();
				} else {
					ident = FluidIdentifier.last();
				}
			} else {
				ident = null;
			}
			if(ident == null) {
				slot.putStack(null);
			} else {
				slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
			}
			if(entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
				((EntityPlayerMP)entityplayer).sendSlotContents(this, slotId, slot.getStack());
			}
			return currentlyEquippedStack;
		}
		
		if(slot instanceof ColorSlot) {
			Colors equipped = Colors.getColor(currentlyEquippedStack);
			Colors color = Colors.getColor(slot.getStack());
			if(Colors.BLANK.equals(equipped)) {
				if(mouseButton == 0) {
					color = color.getNext();
				} else if(mouseButton == 1) {
					color = color.getPrev();
				} else {
					color = Colors.BLANK;
				}
				slot.putStack(color.getItemStack());
			} else {
				if(mouseButton == 1) {
					slot.putStack(Colors.BLANK.getItemStack());
				} else {
					slot.putStack(equipped.getItemStack());
				}
			}
			if(entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
				((EntityPlayerMP)entityplayer).sendSlotContents(this, slotId, slot.getStack());
			}
			return currentlyEquippedStack;
		}
		
		if (currentlyEquippedStack == null){
			if (slot.getStack() != null && mouseButton == 1){
				if (isShift == 1){
					slot.getStack().stackSize = Math.min(slot.getSlotStackLimit(), slot.getStack().stackSize * 2);
					slot.inventory.onInventoryChanged();
				} else {
					slot.getStack().stackSize/=2;
					slot.inventory.onInventoryChanged();
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

			slot.inventory.onInventoryChanged();
			return currentlyEquippedStack;
		}
		
		ItemIdentifier currentItem = ItemIdentifier.get(currentlyEquippedStack);
		ItemIdentifier slotItem = ItemIdentifier.get(slot.getStack());
		if (currentItem == slotItem){
			//Do manual shift-checking to play nice with NEI
			int counter = isShift == 1?10:1;
			if (mouseButton == 1)  {
				if (slot.getStack().stackSize + counter <= slot.getSlotStackLimit()){
					slot.getStack().stackSize += counter;
				} else {
					slot.getStack().stackSize = slot.getSlotStackLimit();
				}
				slot.inventory.onInventoryChanged();
				return currentlyEquippedStack;
			}
			if (mouseButton == 0){
				if (slot.getStack().stackSize - counter > 0){
					slot.getStack().stackSize-=counter;	
					slot.inventory.onInventoryChanged();
				} else {
					slot.putStack(null);
				}
				return currentlyEquippedStack;
			} 
		} else {
			slot.putStack(currentlyEquippedStack.copy());
			if (slot.getStack().stackSize > slot.getSlotStackLimit()){
				slot.getStack().stackSize = slot.getSlotStackLimit();
				slot.inventory.onInventoryChanged();
			}
		}
		return currentlyEquippedStack;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		if(_controler != null) {
			_controler.guiClosedByPlayer(par1EntityPlayer);
		}
		super.onContainerClosed(par1EntityPlayer);
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

	//Hacky overrides to handle client/server player inv sync with 0-slot containers
	@Override
	public Slot getSlotFromInventory(IInventory par1IInventory, int par2)
	{
		Slot s = super.getSlotFromInventory(par1IInventory, par2);
		if(s != null)
			return s;
		if(inventorySlots.isEmpty() && par1IInventory == _playerInventory) {
			s = new Slot(_playerInventory, par2, 0, 0);
			s.slotNumber = par2;
			return s;
		}
        return null;
    }

	@Override
	public void putStackInSlot(int par1, ItemStack par2ItemStack)
	{
		if(inventorySlots.isEmpty()) {
			_playerInventory.setInventorySlotContents(par1, par2ItemStack);
			_playerInventory.onInventoryChanged();
			return;
		}
		super.putStackInSlot(par1, par2ItemStack);
	}
}
