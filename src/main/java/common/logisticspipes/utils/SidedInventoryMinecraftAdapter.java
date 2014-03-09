/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.primitives.Ints;

/**
 * This class is responsible for abstracting an ISidedInventory as a normal IInventory
 * @author Krapht
 *
 */
public final class SidedInventoryMinecraftAdapter implements IInventory {

	public final ISidedInventory _sidedInventory;
	private final int _side;
	private final int _slotMap[];
	
	public SidedInventoryMinecraftAdapter(ISidedInventory sidedInventory, ForgeDirection side, boolean forExtraction) {
		_sidedInventory = sidedInventory;
		_side = side.ordinal();
		if(side == ForgeDirection.UNKNOWN) {
			_slotMap = buildAllSidedMap(sidedInventory,forExtraction);
		} else {
			ArrayList<Integer> list = new ArrayList<Integer>();

			int allSlots[] = _sidedInventory.getAccessibleSlotsFromSide(_side);
			for(int number:allSlots) {
				ItemStack item=_sidedInventory.getStackInSlot(number);
				if(!list.contains(number) && (!forExtraction || // check extract condition
					(item!=null && _sidedInventory.canExtractItem(number,item,_side)))){
						list.add(number);
				}
			}
			_slotMap=Ints.toArray(list);
		}
	}

	private int[] buildAllSidedMap(ISidedInventory sidedInventory, boolean forExtraction) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(int i = 0; i < 6; i++) {
			int slots[] = _sidedInventory.getAccessibleSlotsFromSide(i);
			for(int number:slots) {
				ItemStack item=_sidedInventory.getStackInSlot(number);
				if(!list.contains(number) && (!forExtraction || // check extract condition
					(item!=null && _sidedInventory.canExtractItem(number,item,i)))){
						list.add(number);
				}
			}
		}
		int slotmap[] = new int[list.size()];
		int count = 0;
		for(int i:list) {
			slotmap[count++] = i;
		}
		return slotmap;
	}

	@Override
	public int getSizeInventory() {
		return _slotMap.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _sidedInventory.getStackInSlot(_slotMap[i]);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _sidedInventory.decrStackSize(_slotMap[i], j);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_sidedInventory.setInventorySlotContents(_slotMap[i], itemstack);
	}

	@Override
	public String getInvName() {
		return _sidedInventory.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return _sidedInventory.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
		_sidedInventory.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return _sidedInventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest() {
		_sidedInventory.openChest();
	}

	@Override
	public void closeChest() {
		_sidedInventory.closeChest();
	}


	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return _sidedInventory.getStackInSlotOnClosing(_slotMap[slot]);
	}

	@Override
	public boolean isInvNameLocalized() {
		return _sidedInventory.isInvNameLocalized();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return _sidedInventory.isItemValidForSlot(_slotMap[slot], itemstack) && _sidedInventory.canInsertItem(_slotMap[slot], itemstack, _side);
	}
}
