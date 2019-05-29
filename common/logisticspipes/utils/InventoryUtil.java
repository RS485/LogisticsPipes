/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public class InventoryUtil implements IInventoryUtil, ISpecialInsertion {

	protected final IItemHandler _inventory;
	private final boolean _hideOnePerStack;
	private final boolean _hideOne;
	private final int _cropStart;
	private final int _cropEnd;

	public InventoryUtil(IItemHandler inventory, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_inventory = inventory;
		_hideOnePerStack = hideOnePerStack;
		_hideOne = hideOne;
		_cropStart = cropStart;
		_cropEnd = cropEnd;
	}

	@Override
	public int itemCount(ItemIdentifier item) {
		int count = 0;
		boolean first = true;
		for (int i = _cropStart; i < _inventory.getSlots() - _cropEnd; i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack.isEmpty() || !ItemIdentifier.get(stack).equals(item)) {
				continue;
			}
			if (first) {
				count = stack.getCount() - ((_hideOne || _hideOnePerStack) ? 1 : 0);
				first = false;
			} else {
				count += stack.getCount() - (_hideOnePerStack ? 1 : 0);
			}
		}
		return count;
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount()
	{
		//System.out.println("I am searching inventory from " + _cropStart + " to " + (_inventory.getSlots() -  _cropEnd));
		Map<ItemIdentifier, Integer> items = new LinkedHashMap<>();
		for (int i = _cropStart; i < _inventory.getSlots() - _cropEnd; i++)
		{
			ItemStack stack = _inventory.getStackInSlot(i);

			if (stack.isEmpty())
				continue;

			//System.out.println("Found " + stack + "in slot " + i);
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.getCount() - (_hideOnePerStack ? 1 : 0);
			Integer currentSize = items.get(itemId);
			if (currentSize == null)
				items.put(itemId, stackSize - (_hideOne ? 1 : 0));
			else
				items.put(itemId, currentSize + stackSize);

		}
		return items;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> items = new TreeSet<>();
		for (int i = _cropStart; i < _inventory.getSlots() - _cropEnd; i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			items.add(ItemIdentifier.get(stack));
		}
		return items;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		return getMultipleItems(item, 1);
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier item, int count) {
		if (itemCount(item) < count) {
			return null;
		}
		ItemStack outputStack = null;
		boolean first = true;

		for (int i = _cropStart; i < _inventory.getSlots() - _cropEnd && count > 0; i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack.isEmpty() || (stack.getCount() == 1 && _hideOnePerStack) || !ItemIdentifier.get(stack).equals(item)) {
				continue;
			}
			int itemsToSplit = Math.min(count, stack.getCount() - (((first && _hideOne) || _hideOnePerStack) ? 1 : 0));
			first = false;
			if (itemsToSplit == 0) {
				continue;
			}
			ItemStack removed = _inventory.extractItem(i, itemsToSplit, false);
			if (outputStack == null) {
				outputStack = removed;
			} else {
				outputStack.setCount(outputStack.getCount() + removed.getCount());
			}
			count -= removed.getCount();
		}
		return outputStack;
	}

	//Ignores slot/item hiding
	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		for (int i = 0; i < _inventory.getSlots(); i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (ItemIdentifier.get(stack).getUndamaged().equals(item)) {
				return true;
			}
		}
		return false;
	}

	//Ignores slot/item hiding
	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, Integer.MAX_VALUE);
	}

	@Override
	public int roomForItem(ItemIdentifier item, int count) {
		int totalRoom = 0;
		for (int i = 0; i < _inventory.getSlots() && count > totalRoom; i++) {
			ItemStack leftover = _inventory.insertItem(i, item.unsafeMakeNormalStack(count), true);
			totalRoom += count - leftover.getCount();
		}
		return totalRoom;
	}

	@Override
	public boolean isSpecialInventory() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return _inventory.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _inventory.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _inventory.extractItem(i, j, false);
	}

	@Override
	public int addToSlot(ItemStack stack, int slot) {
		int wanted = stack.getCount();
		ItemStack rest = _inventory.insertItem(slot, stack, true);
		return wanted - rest.getCount();
	}
}
