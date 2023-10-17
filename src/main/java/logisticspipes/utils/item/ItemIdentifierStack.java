/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.util.LinkedList;
import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.utils.tuples.Triplet;

public final class ItemIdentifierStack implements Comparable<ItemIdentifierStack>, ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];
	private final ItemIdentifier _item;
	private int stackSize;

	public static ItemIdentifierStack getFromStack(@Nonnull ItemStack stack) {
		return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.getCount());
	}

	public ItemIdentifierStack(ItemIdentifier item, int stackSize) {
		_item = item;
		setStackSize(stackSize);
	}

	public ItemIdentifierStack(ItemIdentifierStack copy) {
		this(copy._item, copy.stackSize);
	}

	public ItemIdentifier getItem() {
		return _item;
	}

	/**
	 * @return the stackSize
	 */
	public int getStackSize() {
		return stackSize;
	}

	/**
	 * @param stackSize
	 *            the stackSize to set
	 */
	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	public void lowerStackSize(int stackSize) {
		this.stackSize -= stackSize;
	}

	@Nonnull
	public ItemStack unsafeMakeNormalStack() {
		return _item.unsafeMakeNormalStack(stackSize);
	}

	@Nonnull
	public ItemStack makeNormalStack() {
		return _item.makeNormalStack(stackSize);
	}

	@Nonnull
	public EntityItem makeEntityItem(World world, double x, double y, double z) {
		return _item.makeEntityItem(stackSize, world, x, y, z);
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof ItemIdentifierStack) {
			ItemIdentifierStack stack = (ItemIdentifierStack) that;
			return stack._item.equals(_item) && stack.getStackSize() == getStackSize();
		}
		if ((that instanceof ItemIdentifier)) {
			throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
		}

		return false;
	}

	@Override
	public int hashCode() {
		return _item.hashCode() ^ (1023 * getStackSize());
	}

	@Override
	public String toString() {
		return String.format("%dx %s", getStackSize(), _item.toString());
	}

	public String getFriendlyName() {
		return getStackSize() + " " + _item.getFriendlyName();
	}

	public static LinkedList<ItemIdentifierStack> getListFromInventory(IInventory inv) {
		return ItemIdentifierStack.getListFromInventory(inv, false);
	}

	public static LinkedList<ItemIdentifierStack> getListFromInventory(IInventory inv, boolean removeNull) {
		LinkedList<ItemIdentifierStack> list = new LinkedList<>();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).isEmpty()) {
				if (!removeNull) {
					list.add(null);
				}
			} else {
				list.add(ItemIdentifierStack.getFromStack(inv.getStackInSlot(i)));
			}
		}
		return list;
	}

	public static LinkedList<ItemIdentifierStack> getListSendQueue(LinkedList<Triplet<IRoutedItem, EnumFacing, ItemSendMode>> _sendQueue) {
		LinkedList<ItemIdentifierStack> list = new LinkedList<>();
		for (Triplet<IRoutedItem, EnumFacing, ItemSendMode> part : _sendQueue) {
			if (part == null) {
				list.add(null);
			} else {
				boolean added = false;
				for (ItemIdentifierStack stack : list) {
					if (stack.getItem().equals(part.getValue1().getItemIdentifierStack().getItem())) {
						stack.setStackSize(stack.getStackSize() + part.getValue1().getItemIdentifierStack().stackSize);
						added = true;
						break;
					}
				}
				if (!added) {
					list.add(new ItemIdentifierStack(part.getValue1().getItemIdentifierStack()));
				}
			}
		}
		return list;
	}

	@Override
	public int compareTo(ItemIdentifierStack o) {
		int c = _item.compareTo(o._item);
		if (c == 0) {
			return getStackSize() - o.getStackSize();
		}
		return c;
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

}
