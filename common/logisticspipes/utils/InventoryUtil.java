/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.utils.item.ItemIdentifier;

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
	public int itemCount(@Nonnull ItemIdentifier item) {
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
	@Nonnull
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		Map<ItemIdentifier, Integer> items = new LinkedHashMap<>();
		for (int i = _cropStart; i < _inventory.getSlots() - _cropEnd; i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.getCount() - (_hideOnePerStack ? 1 : 0);
			Integer currentSize = items.get(itemId);
			if (currentSize == null) {
				items.put(itemId, stackSize - (_hideOne ? 1 : 0));
			} else {
				items.put(itemId, currentSize + stackSize);
			}
		}
		return items;
	}

	@Override
	@Nonnull
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
	@Nonnull
	public ItemStack getSingleItem(ItemIdentifier item) {
		return getMultipleItems(item, 1);
	}

	@Override
	@Nonnull
	public ItemStack getMultipleItems(@Nonnull ItemIdentifier item, int count) {
		if (itemCount(item) < count) {
			return ItemStack.EMPTY;
		}
		ItemStack outputStack = ItemStack.EMPTY;
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
			if (outputStack.isEmpty()) {
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
	public boolean containsUndamagedItem(@Nonnull ItemIdentifier item) {
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

	@Override
	public int roomForItem(@Nonnull ItemStack stack) {
		// Special casing for "unlimited" storage items
		if (_inventory.getSlots() == 1 && _inventory.getSlotLimit(0) == Integer.MAX_VALUE) {
			ItemStack content = _inventory.extractItem(0, Integer.MAX_VALUE, true);
			if (content.isEmpty()) {
				return Integer.MAX_VALUE;
			}
			return Integer.MAX_VALUE - content.getCount();
		}

		int totalRoom = 0;
		for (int i = 0; i < _inventory.getSlots() && stack.getCount() > totalRoom; i++) {
			ItemStack leftover = _inventory.insertItem(i, stack, true);
			totalRoom += stack.getCount() - leftover.getCount();
		}
		return totalRoom;
	}

	@Override
	public int getSizeInventory() {
		return _inventory.getSlots();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		return _inventory.getStackInSlot(i);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int i, int j) {
		return _inventory.extractItem(i, j, false);
	}

	@Override
	public int addToSlot(@Nonnull ItemStack stack, int slot) {
		int wanted = stack.getCount();
		ItemStack rest = _inventory.insertItem(slot, stack, false);
		return wanted - rest.getCount();
	}
}
