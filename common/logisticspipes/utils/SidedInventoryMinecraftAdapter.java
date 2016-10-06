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

import com.google.common.primitives.Ints;
import net.minecraft.util.text.ITextComponent;

/**
 * This class is responsible for abstracting an ISidedInventory as a normal
 * IInventory
 * 
 * @author Krapht
 */
public final class SidedInventoryMinecraftAdapter implements IInventory {

	public final ISidedInventory _sidedInventory;
	private final EnumFacing _side;
	private final boolean _forExtraction;
	private int[] _slotMapCache;

	public SidedInventoryMinecraftAdapter(ISidedInventory sidedInventory, EnumFacing side, boolean forExtraction) {
		_sidedInventory = sidedInventory;
		_side = side;
		_forExtraction = forExtraction;
	}

	private int[] getSlotMap() {
		if(_slotMapCache == null) {
			if (_side == null) {
				_slotMapCache = buildAllSidedMap();
			} else {
				ArrayList<Integer> list = new ArrayList<>();

				int allSlots[] = _sidedInventory.getSlotsForFace(_side);
				for (int number : allSlots) {
					ItemStack item = _sidedInventory.getStackInSlot(number);
					if (!list.contains(number) && (!_forExtraction || // check extract condition
							(item != null && _sidedInventory.canExtractItem(number, item, _side)))) {
						list.add(number);
					}
				}
				_slotMapCache = Ints.toArray(list);
			}
		}
		return _slotMapCache;
	}

	private int[] buildAllSidedMap() {
		ArrayList<Integer> list = new ArrayList<>();

		for (EnumFacing side:EnumFacing.VALUES) {
			int slots[] = _sidedInventory.getSlotsForFace(side);
			for (int number : slots) {
				ItemStack item = _sidedInventory.getStackInSlot(number);
				if (!list.contains(number) && (!_forExtraction || // check extract condition
						(item != null && _sidedInventory.canExtractItem(number, item, side)))) {
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
		return getSlotMap().length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _sidedInventory.getStackInSlot(getSlotMap()[i]);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _sidedInventory.decrStackSize(getSlotMap()[i], j);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_sidedInventory.setInventorySlotContents(getSlotMap()[i], itemstack);
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
		_sidedInventory.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		_sidedInventory.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return _sidedInventory.isItemValidForSlot(getSlotMap()[slot], itemstack) && _sidedInventory.canInsertItem(getSlotMap()[slot], itemstack, _side);
	}

	@Override
	public int getField(int id) {
		return _sidedInventory.getField(id);
	}

	@Override
	public void setField(int id, int value) {
		_sidedInventory.setField(id, value);
	}

	@Override
	public int getFieldCount() {
		return _sidedInventory.getFieldCount();
	}

	@Override
	public void clear() {
		_sidedInventory.clear();
	}

	@Override
	public String getName() {
		return _sidedInventory.getName();
	}

	@Override
	public boolean hasCustomName() {
		return _sidedInventory.hasCustomName();
	}

	@Override
	public ITextComponent getDisplayName() {
		return _sidedInventory.getDisplayName();
	}
}
