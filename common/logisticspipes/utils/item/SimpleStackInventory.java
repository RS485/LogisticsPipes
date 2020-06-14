/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.util.items.ItemStackLoader;

public class SimpleStackInventory implements IInventory, ISaveState, Iterable<Pair<ItemStack, Integer>> {

	private static final TextComponentString TEXT_COMPONENT_EMPTY = new TextComponentString("");

	private final NonNullList<ItemStack> stackList;
	private final String _name;
	private final int _stackLimit;

	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<>();

	public SimpleStackInventory(int size, String name, int stackLimit) {
		stackList = NonNullList.withSize(size, ItemStack.EMPTY);
		_name = name;
		_stackLimit = stackLimit;
	}

	@Override
	public int getSizeInventory() {
		return stackList.size();
	}

	@Override
	public boolean isEmpty() {
		return stackList.stream().allMatch(ItemStack::isEmpty);
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int i) {
		return stackList.get(i);
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize(int slot, int count) {
		final ItemStack stack = stackList.get(slot);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (stack.getCount() > count) {
			ItemStack ret = stack.copy();
			ret.setCount(count);
			stack.setCount(stack.getCount() - count);
			return ret;
		}
		return stackList.set(slot, ItemStack.EMPTY);
	}

	@Override
	public void setInventorySlotContents(int slot, @Nonnull ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			stackList.set(slot, ItemStack.EMPTY);
		} else {
			stackList.set(slot, itemstack.copy());
		}
	}

	@Nonnull
	@Override
	public String getName() {
		return _name;
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return TEXT_COMPONENT_EMPTY;
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
	}

	@Override
	public void markDirty() {
		for (ISimpleInventoryEventHandler handler : _listener) {
			handler.InventoryChanged(this);
		}
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return false;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		readFromNBT(nbttagcompound, "");
	}

	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = nbttagcompound.getTagList(prefix + "items", nbttagcompound.getId());

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			NBTTagCompound nbttagcompound2 = nbttaglist.getCompoundTagAt(j);
			int index = nbttagcompound2.getInteger("index");
			if (index < stackList.size()) {
				stackList.set(index, ItemStackLoader.loadAndFixItemStackFromNBT(nbttagcompound2));
			} else {
				LogisticsPipes.log.fatal("SimpleInventory: java.lang.ArrayIndexOutOfBoundsException: " + index + " of " + stackList.size());
			}
		}
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		writeToNBT(nbttagcompound, "");
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int j = 0; j < stackList.size(); ++j) {
			final ItemStack stack = stackList.get(j);
			if (!stack.isEmpty()) {
				NBTTagCompound nbttagcompound2 = new NBTTagCompound();
				nbttaglist.appendTag(nbttagcompound2);
				nbttagcompound2.setInteger("index", j);
				stack.writeToNBT(nbttagcompound2);
			}
		}
		nbttagcompound.setTag(prefix + "items", nbttaglist);
		nbttagcompound.setInteger(prefix + "itemsCount", stackList.size());
	}

	public void dropContents(World world, BlockPos pos) {
		if (MainProxy.isServer(world)) {
			for (int i = 0; i < stackList.size(); i++) {
				dropSlot(i, world, pos);
			}
		}
	}

	private void dropSlot(int slot, World world, BlockPos pos) {
		final ItemStack slotStack = stackList.get(slot);
		IntStream.range(0, (slotStack.getCount() / slotStack.getMaxStackSize()) + 1)
				.mapToObj(i -> decrStackSize(slot, slotStack.getMaxStackSize()))
				.filter(dropStack -> !dropStack.isEmpty())
				.forEach(dropStack -> {
					float f1 = 0.7F;
					double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
					double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
					double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
					EntityItem entityitem = new EntityItem(world, pos.getX() + d, pos.getY() + d1, pos.getZ() + d2, dropStack);
					entityitem.setDefaultPickupDelay();
					world.spawnEntity(entityitem);
				});
	}

	public void addListener(ISimpleInventoryEventHandler listner) {
		if (!_listener.contains(listner)) {
			_listener.add(listner);
		}
	}

	public void removeListener(ISimpleInventoryEventHandler listner) {
		_listener.remove(listner);
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int i) {
		return stackList.set(i, ItemStack.EMPTY);
	}

	private int tryAddToSlot(int i, @Nonnull ItemStack stack, int realstacklimit) {
		ItemStack slotStack = stackList.get(i);
		if (slotStack.isEmpty()) {
			final ItemStack copy = stack.copy();
			stackList.set(i, copy);
			copy.setCount(Math.min(copy.getCount(), realstacklimit));
			return copy.getCount();
		}
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		ItemIdentifier slotIdent = ItemIdentifier.get(slotStack);
		if (slotIdent.equals(stackIdent)) {
			slotStack.setCount(slotStack.getCount() + stack.getCount());
			if (slotStack.getCount() > realstacklimit) {
				int ans = stack.getCount() - (slotStack.getCount() - realstacklimit);
				slotStack.setCount(realstacklimit);
				return ans;
			} else {
				return stack.getCount();
			}
		} else {
			return 0;
		}
	}

	public int addCompressed(@Nonnull ItemStack stack, boolean ignoreMaxStackSize) {
		if (stack.isEmpty()) return 0;
		stack = stack.copy();

		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		int stacklimit = _stackLimit;
		if (!ignoreMaxStackSize) {
			stacklimit = Math.min(stacklimit, stackIdent.getMaxStackSize());
		}

		for (int i = 0; i < stackList.size(); i++) {
			if (stack.getCount() <= 0) {
				break;
			}
			if (stackList.get(i).isEmpty()) {
				continue; //Skip Empty Slots on first attempt.
			}
			int added = tryAddToSlot(i, stack, stacklimit);
			stack.setCount(stack.getCount() - added);
		}
		for (int i = 0; i < stackList.size(); i++) {
			if (stack.getCount() <= 0) {
				break;
			}
			int added = tryAddToSlot(i, stack, stacklimit);
			stack.setCount(stack.getCount() - added);
		}
		markDirty();
		return stack.getCount();
	}

	@Override
	public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {}

	public void clearInventorySlotContents(int i) {
		stackList.set(i, ItemStack.EMPTY);
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Nonnull
	@Override
	public Iterator<Pair<ItemStack, Integer>> iterator() {
		final Iterator<ItemStack> iter = stackList.iterator();
		return new Iterator<Pair<ItemStack, Integer>>() {

			int pos = -1;

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Pair<ItemStack, Integer> next() {
				pos++;
				return new Pair<>(iter.next(), pos);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
