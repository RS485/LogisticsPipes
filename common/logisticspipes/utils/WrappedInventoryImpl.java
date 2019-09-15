/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.SpecialInsertion;
import logisticspipes.interfaces.WrappedInventory;
import network.rs485.logisticspipes.util.ItemVariant;

public class WrappedInventoryImpl implements WrappedInventory, SpecialInsertion {

	protected final Inventory inventory;
	private final boolean hideOnePerStack;
	private final boolean hideOne;
	private final int cropStart;
	private final int cropEnd;

	public WrappedInventoryImpl(Inventory inventory, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		this.inventory = inventory;
		this.hideOnePerStack = hideOnePerStack;
		this.hideOne = hideOne;
		this.cropStart = cropStart;
		this.cropEnd = cropEnd;
	}

	@Override
	public int itemCount(ItemVariant item) {
		int count = 0;
		boolean first = true;
		for (int i = cropStart; i < inventory.getInvSize() - cropEnd; i++) {
			ItemStack stack = inventory.getInvStack(i);
			if (stack.isEmpty() || !ItemVariant.fromStack(stack).equals(item)) {
				continue;
			}
			if (first) {
				count = stack.getCount() - ((hideOne || hideOnePerStack) ? 1 : 0);
				first = false;
			} else {
				count += stack.getCount() - (hideOnePerStack ? 1 : 0);
			}
		}
		return count;
	}

	@Override
	public Set<ItemStack> getItemsAndCount() {
		return IntStream.range(cropStart, inventory.getInvSize() - cropEnd)
				.mapToObj(inventory::getInvStack)
				.filter(it -> !it.isEmpty())
				.map(it -> adjustStackCount(it, hideOnePerStack))
				.collect(Collectors.toMap(ItemVariant::fromStack, ItemStack::getCount, Integer::sum)).entrySet().stream()
				.map(it -> it.getKey().makeStack(it.getValue()))
				.map(it -> adjustStackCount(it, hideOne))
				.collect(Collectors.toSet());
	}

	private ItemStack adjustStackCount(ItemStack stack, boolean takeOne) {
		if (!takeOne) return stack;
		stack = stack.copy();
		stack.decrement(1);
		return stack;
	}

	@Override
	public Set<ItemVariant> getItems() {
		return IntStream.range(cropStart, inventory.getInvSize() - cropEnd)
				.mapToObj(inventory::getInvStack)
				.filter(it -> !it.isEmpty())
				.map(ItemVariant::fromStack)
				.collect(Collectors.toSet());
	}

	@Override
	@Nonnull
	public ItemStack getSingleItem(ItemVariant item) {
		return getMultipleItems(item.makeStack(1));
	}

	@Override
	@Nonnull
	public ItemStack getMultipleItems(ItemStack stack) {
		final ItemVariant variant = ItemVariant.fromStack(stack);
		int count = stack.getCount();

		if (itemCount(variant) < count) return ItemStack.EMPTY;

		ItemStack outputStack = ItemStack.EMPTY;

		boolean first = true;

		for (int i = cropStart; i < inventory.getInvSize() - cropEnd && count > 0; i++) {
			ItemStack invStack = inventory.getInvStack(i);
			if (invStack.isEmpty() || (invStack.getCount() == 1 && hideOnePerStack) || !ItemVariant.fromStack(invStack).equals(variant)) {
				continue;
			}
			int itemsToSplit = Math.min(count, invStack.getCount() - (((first && hideOne) || hideOnePerStack) ? 1 : 0));
			first = false;
			if (itemsToSplit == 0) {
				continue;
			}
			ItemStack removed = inventory.takeInvStack(i, itemsToSplit);
			if (outputStack.isEmpty()) {
				outputStack = removed;
			} else {
				outputStack.setCount(outputStack.getCount() + removed.getCount());
			}
			count -= removed.getCount();
		}

		return outputStack;
	}

	// Ignores slot/item hiding
	@Override
	public boolean containsUndamagedItem(ItemVariant item) {
		for (int i = 0; i < inventory.getInvSize(); i++) {
			ItemStack stack = inventory.getInvStack(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (ItemVariant.fromStack(stack).getUndamaged().equals(item)) {
				return true;
			}
		}
		return false;
	}

	// Ignores slot/item hiding
	@Override
	public int roomForItem(ItemVariant item) {
		return roomForItem(item.makeStack(Integer.MAX_VALUE));
	}

	@Override
	public int roomForItem(ItemStack stack) {
		// Special casing for "unlimited" storage items
		if (inventory.getInvSize() == 1 && inventory.getInvMaxStackAmount() == Integer.MAX_VALUE) {
			ItemStack content = inventory.getInvStack(0);
			if (content.isEmpty()) {
				return Integer.MAX_VALUE;
			}
			return Integer.MAX_VALUE - content.getCount();
		}

		int totalRoom = 0;
		int count = stack.getCount();

		for (int i = 0; i < inventory.getInvSize() && count > totalRoom; i++) {
			ItemStack leftover = inventory.insertItem(i, item.unsafeMakeNormalStack(count), true);
			totalRoom += count - leftover.getCount();
		}
		return totalRoom;
	}

	@Override
	public int getSizeInventory() {
		return inventory.getInvSize();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		return inventory.getInvStack(i);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int i, int j) {
		return inventory.takeInvStack(i, j);
	}

	@Override
	public int addToSlot(ItemStack stack, int slot) {
		int wanted = stack.getCount();

		ItemStack rest = inventory.insertItem(slot, stack, true);
		return wanted - rest.getCount();
	}
}
