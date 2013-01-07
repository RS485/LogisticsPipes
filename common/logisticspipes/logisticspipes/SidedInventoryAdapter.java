/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

/**
 * This class is responsible for abstracting an ISidedInventory as a normal IInventory
 * @author Krapht
 *
 */
public class SidedInventoryAdapter implements IInventory {

	public final ISidedInventory _sidedInventory;
	private final int _slotMap[];
	
	public SidedInventoryAdapter(ISidedInventory sidedInventory, ForgeDirection side){
		_sidedInventory = sidedInventory;
		if(side == ForgeDirection.UNKNOWN) {
			TreeSet<Integer> slotset = new TreeSet<Integer>();
			for(ForgeDirection tside : ForgeDirection.VALID_DIRECTIONS) {
				int nslots = _sidedInventory.getSizeInventorySide(tside);
				int offset = _sidedInventory.getStartInventorySide(tside);
				for(int i = 0; i < nslots; i++) {
					slotset.add(i + offset);
				}
			}
			_slotMap = new int[slotset.size()];
			int j = 0;
			for(int i : slotset) {
				_slotMap[j] = i;
				j++;
			}
		} else {
			int nslots = _sidedInventory.getSizeInventorySide(side);
			int offset = _sidedInventory.getStartInventorySide(side);
			_slotMap = new int[nslots];
			for(int i = 0; i < nslots; i++) {
				_slotMap[i] = i + offset;
			}
		}
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
}
