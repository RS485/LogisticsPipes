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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

import com.google.common.primitives.Ints;

/**
 * This class is responsible for abstracting an ISidedInventory as a normal
 * IInventory
 * 
 * @author Krapht
 */
public final class SidedInventoryMinecraftAdapter implements IInventory {

	public UtilEnumFacing utilEnumFacing;
	public final ISidedInventory _sidedInventory;
	private final int _side;
	private final int _slotMap[];

	public SidedInventoryMinecraftAdapter(ISidedInventory sidedInventory, EnumFacing side, boolean forExtraction) {
		_sidedInventory = sidedInventory;
		_side = side.ordinal();
		if (side == UtilEnumFacing.UNKNOWN) {
			_slotMap = buildAllSidedMap(sidedInventory, forExtraction);
		} else {
			ArrayList<Integer> list = new ArrayList<Integer>();

			int allSlots[] = _sidedInventory.getSlotsForFace(utilEnumFacing.getOrientation(_side));
			for (int number : allSlots) {
				ItemStack item = _sidedInventory.getStackInSlot(number);
				if (!list.contains(number) && (!forExtraction || // check extract condition
						(item != null && _sidedInventory.canExtractItem(number, item, utilEnumFacing.getOrientation(_side))))) {
					list.add(number);
				}
			}
			_slotMap = Ints.toArray(list);
		}
	}

	private int[] buildAllSidedMap(ISidedInventory sidedInventory, boolean forExtraction) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < 6; i++) {
			int slots[] = _sidedInventory.getSlotsForFace(utilEnumFacing.getOrientation(i));
			for (int number : slots) {
				ItemStack item = _sidedInventory.getStackInSlot(number);
				if (!list.contains(number) && (!forExtraction || // check extract condition
						(item != null && _sidedInventory.canExtractItem(number, item, utilEnumFacing.getOrientation(i))))) {
					list.add(number);
				}
			}
		}
		int slotmap[] = new int[list.size()];
		int count = 0;
		for (int i : list) {
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
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_sidedInventory.setInventorySlotContents(_slotMap[i], itemstack);
	}

	@Override
	public String getInventoryName() {
		return _sidedInventory.getInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return _sidedInventory.getInventoryStackLimit();
	}

	@Override
	public void markDirty() {
		_sidedInventory.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return _sidedInventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return _sidedInventory.getStackInSlotOnClosing(_slotMap[slot]);
	}



	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return _sidedInventory.isItemValidForSlot(_slotMap[slot], itemstack) && _sidedInventory.canInsertItem(_slotMap[slot], itemstack, utilEnumFacing.getOrientation(_side));
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public String getName() {
		return _sidedInventory.getName()
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return _sidedInventory.getDisplayName();
	}
}
