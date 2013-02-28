/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

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
public final class SidedInventoryAdapter implements IInventory {

	public final ISidedInventory _sidedInventory;
	private final int _slotMap[];
	
	public SidedInventoryAdapter(ISidedInventory sidedInventory, ForgeDirection side) {
		_sidedInventory = sidedInventory;
		if(side == ForgeDirection.UNKNOWN) {
			_slotMap = buildAllSidedMap(sidedInventory);
		} else {
			int nslots = _sidedInventory.getSizeInventorySide(side);
			int offset = _sidedInventory.getStartInventorySide(side);
			_slotMap = new int[nslots];
			for(int i = 0; i < nslots; i++) {
				_slotMap[i] = i + offset;
			}
		}
	}

	private int[] buildAllSidedMap(ISidedInventory sidedInventory) {
		int[] invSizes = new int[6];
		int[] invStarts = new int[6];
		int nUnique = 0;

		//get start and size for sides, skip empty and duplicates
outer:
		for(int i = 0; i < 6; i++) {
			ForgeDirection tside = ForgeDirection.VALID_DIRECTIONS[i];
			int size = sidedInventory.getSizeInventorySide(tside);
			if(size == 0)
				continue;
			int start = sidedInventory.getStartInventorySide(tside);
			for(int j = 0; j < nUnique; j++) {
				if(invStarts[j] == start && invSizes[j] == size)
					continue outer;
			}
			invStarts[nUnique] = start;
			invSizes[nUnique] = size;
			nUnique++;
		}

		//selection sort by start ascending, at equal start by size descending
		for(int i = 0; i < nUnique; i++) {
			int best = i;
			for(int j = i + 1; j < nUnique; j++) {
				if(invStarts[j] < invStarts[best]) {
					best = j;
					continue;
				}
				if(invStarts[j] == invStarts[best] && invSizes[j] > invSizes[best]) {
					best = j;
					continue;
				}
			}
			//swap
			if(best != i) {
				int tstart = invStarts[i];
				int tsize = invSizes[i];
				invStarts[i] = invStarts[best];
				invSizes[i] = invSizes[best];
				invStarts[best] = tstart;
				invSizes[best] = tsize;
			}
		}

		//remove overlaps, maintaining the start ascending invariant
		for(int i = 0; i < nUnique; i++) {
			if(invSizes[i] == 0)
				continue;
			for(int j = i + 1; j < nUnique; j++) {
				if(invSizes[j] == 0)
					continue;
				//identical or j contained within i
				if(invStarts[j] + invSizes[j] <= invStarts[i] + invSizes[i]) {
					invSizes[j] = 0;
					continue;
				}
				//j overlapping or directly adjacent to end of i
				if(invStarts[j] <= invStarts[i] + invSizes[i] && invStarts[j] + invSizes[j] >= invStarts[i] + invSizes[i]) {
					invSizes[i] = invStarts[j] - invStarts[i] + invSizes[j];
					invSizes[j] = 0;
					continue;
				}
			}
		}

		//count total
		int totalslots = 0;
		for(int i = 0; i < nUnique; i++) {
			totalslots += invSizes[i];
		}

		//and finally fill the map
		int[] slotmap = new int[totalslots];
		for(int i = 0, curidx = 0; i < nUnique; i++) {
			for(int j = invStarts[i]; j < invStarts[i] + invSizes[i]; j++) {
				slotmap[curidx++] = j;
			}
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
}
