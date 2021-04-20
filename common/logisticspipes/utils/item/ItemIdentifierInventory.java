/*
 * Copyright (c) Krapht, 2011
 * <p>
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.inventory.SlotAccess;
import network.rs485.logisticspipes.util.items.ItemStackLoader;

public class ItemIdentifierInventory
		implements ISaveState, Iterable<Pair<ItemIdentifierStack, Integer>>, IItemIdentifierInventory {

	private final Object[] ccTypeHolder = new Object[1];
	private final ItemIdentifierStack[] _contents;
	private final String _name;
	private final int _stackLimit;
	@Nonnull
	private final HashMap<ItemIdentifier, Integer> _contentsMap;
	private final HashSet<ItemIdentifier> _contentsUndamagedSet;
	private final HashSet<ItemIdentifier> _contentsNoNBTSet;
	private final HashSet<ItemIdentifier> _contentsUndamagedNoNBTSet;
	private final boolean isLiquidInventory;

	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<>();

	public final SlotAccess slotAccess = new SlotAccess() {

		@Override
		public void mergeSlots(int intoSlot, int fromSlot) {
			if (_contents[intoSlot] == null) {
				_contents[intoSlot] = _contents[fromSlot];
			} else {
				_contents[intoSlot].setStackSize(_contents[intoSlot].getStackSize() + _contents[fromSlot].getStackSize());
			}
			_contents[fromSlot] = null;
			updateContents();
		}

		@Override
		public boolean canMerge(int intoSlot, int fromSlot) {
			return _contents[intoSlot].getItem().equals(_contents[fromSlot].getItem());
		}

		@Override
		public boolean isSlotEmpty(int idx) {
			return _contents[idx] == null;
		}

	};

	public ItemIdentifierInventory(int size, String name, int stackLimit, boolean liquidInv) {
		_contents = new ItemIdentifierStack[size];
		_name = name;
		_stackLimit = stackLimit;
		_contentsMap = new HashMap<>((int) (size * 1.5));
		_contentsUndamagedSet = new HashSet<>((int) (size * 1.5));
		_contentsNoNBTSet = new HashSet<>((int) (size * 1.5));
		_contentsUndamagedNoNBTSet = new HashSet<>((int) (size * 1.5));
		isLiquidInventory = liquidInv;
	}

	public ItemIdentifierInventory(int size, String name, int stackLimit) {
		this(size, name, stackLimit, false);
	}

	public ItemIdentifierInventory(@Nonnull ItemIdentifierInventory copy) {
		_contents = Arrays.copyOf(copy._contents, copy._contents.length);
		for (int i = 0; i < _contents.length; i++) {
			if (copy._contents[i] != null) _contents[i] = new ItemIdentifierStack(copy._contents[i]);
		}
		_name = copy._name;
		_stackLimit = copy._stackLimit;
		_contentsMap = new HashMap<>(copy._contentsMap);
		_contentsUndamagedSet = new HashSet<>(copy._contentsUndamagedSet);
		_contentsNoNBTSet = new HashSet<>(copy._contentsNoNBTSet);
		_contentsUndamagedNoNBTSet = new HashSet<>(copy._contentsUndamagedNoNBTSet);
		isLiquidInventory = copy.isLiquidInventory;
	}

	public static void dropItems(World world, @Nonnull ItemStack stack, BlockPos pos) {
		dropItems(world, stack, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void dropItems(World world, @Nonnull ItemStack stack, int i, int j, int k) {
		if (stack.isEmpty()) return;
		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.setPickupDelay(10);
		world.spawnEntity(entityitem);
	}

	@Override
	public int getSizeInventory() {
		return _contents.length;
	}

	@Override
	@Deprecated
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		if (_contents[i] == null) {
			return ItemStack.EMPTY;
		}
		return _contents[i].makeNormalStack();
	}

	@Override
	public ItemIdentifierStack getIDStackInSlot(int i) {
		return _contents[i];
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int slot, int count) {
		if (_contents[slot] == null) {
			return ItemStack.EMPTY;
		}
		ItemStack ret = _contents[slot].makeNormalStack();
		if (_contents[slot].getStackSize() > count) {
			ret.setCount(count);
			_contents[slot].setStackSize(_contents[slot].getStackSize() - count);
		} else {
			_contents[slot] = null;
		}
		updateContents();
		return ret;
	}

	@Override
	public void setInventorySlotContents(int i, @Nonnull ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			_contents[i] = null;
		} else {
			if (isInvalidStack(itemstack)) {
				if (LogisticsPipes.isDEBUG()) {
					new UnsupportedOperationException("Not valid for this Inventory: (" + itemstack + ")")
							.printStackTrace();
				}
				return;
			}
			_contents[i] = ItemIdentifierStack.getFromStack(itemstack);
		}
		updateContents();
	}

	@Override
	public void setInventorySlotContents(int i, ItemIdentifierStack itemstack) {
		if (itemstack == null) {
			_contents[i] = null;
		} else {
			if (!isValidStack(itemstack)) {
				if (LogisticsPipes.isDEBUG()) {
					new UnsupportedOperationException("Not valid for this Inventory: (" + itemstack + ")")
							.printStackTrace();
				}
				return;
			}
			_contents[i] = itemstack;
		}
		updateContents();
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
	}

	@Override
	public void markDirty() {
		updateContents();
		for (ISimpleInventoryEventHandler handler : _listener) {
			handler.InventoryChanged(this);
		}
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return true;
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
			if (index < _contents.length) {
				ItemStack stack = ItemStackLoader.loadAndFixItemStackFromNBT(nbttagcompound2);
				ItemIdentifierStack itemstack = ItemIdentifierStack.getFromStack(stack);
				if (isValidStack(itemstack)) {
					_contents[index] = itemstack;
				}
			} else {
				LogisticsPipes.log.fatal("SimpleInventory: java.lang.ArrayIndexOutOfBoundsException: " + index + " of "
						+ _contents.length);
			}
		}
		updateContents();
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		writeToNBT(nbttagcompound, "");
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int j = 0; j < _contents.length; ++j) {
			if (_contents[j] != null && _contents[j].getStackSize() > 0) {
				NBTTagCompound nbttagcompound2 = new NBTTagCompound();
				nbttaglist.appendTag(nbttagcompound2);
				nbttagcompound2.setInteger("index", j);
				_contents[j].unsafeMakeNormalStack().writeToNBT(nbttagcompound2);
			}
		}
		nbttagcompound.setTag(prefix + "items", nbttaglist);
		nbttagcompound.setInteger(prefix + "itemsCount", _contents.length);
	}

	public void dropContents(World world, BlockPos pos) {
		dropContents(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public void dropContents(World world, int posX, int posY, int posZ) {
		if (MainProxy.isServer(world)) {
			for (int i = 0; i < _contents.length; i++) {
				while (_contents[i] != null) {
					ItemStack todrop = decrStackSize(i, _contents[i].getItem().getMaxStackSize());
					ItemIdentifierInventory.dropItems(world, todrop, posX, posY, posZ);
				}
			}
			updateContents();
		}
	}

	@Override
	public void addListener(@Nonnull ISimpleInventoryEventHandler listener) {
		if (!_listener.contains(listener)) {
			_listener.add(listener);
		}
	}

	@Override
	public void removeListener(@Nonnull ISimpleInventoryEventHandler listener) {
		_listener.remove(listener);
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (_contents[i] == null) {
			return ItemStack.EMPTY;
		}

		ItemStack stackToTake = _contents[i].makeNormalStack();
		_contents[i] = null;
		updateContents();
		return stackToTake;
	}

	@Override
	public void handleItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		int i = 0;
		for (ItemIdentifierStack stack : _allItems) {
			if (_contents.length <= i) {
				break;
			}
			_contents[i] = stack;
			i++;
		}
		markDirty();
	}

	private int tryAddToSlot(int i, @Nonnull ItemStack stack, int realstacklimit) {
		if (isInvalidStack(stack)) {
			if (LogisticsPipes.isDEBUG()) {
				new UnsupportedOperationException("Not valid for this Inventory: (" + stack + ")").printStackTrace();
			}
			return 0;
		}
		ItemIdentifierStack slot = _contents[i];

		if (slot == null) {
			_contents[i] = ItemIdentifierStack.getFromStack(stack);
			_contents[i].setStackSize(Math.min(_contents[i].getStackSize(), realstacklimit));
			return _contents[i].getStackSize();
		}

		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		ItemIdentifier slotIdent = slot.getItem();

		if (slotIdent.equals(stackIdent)) {
			slot.setStackSize(slot.getStackSize() + stack.getCount());

			if (slot.getStackSize() > realstacklimit) {
				int ans = stack.getCount() - (slot.getStackSize() - realstacklimit);
				slot.setStackSize(realstacklimit);
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

		if (isInvalidStack(stack)) {
			if (LogisticsPipes.isDEBUG()) {
				new UnsupportedOperationException("Not valid for this Inventory: (" + stack + ")").printStackTrace();
			}
			return stack.getCount();
		}

		stack = stack.copy();

		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		int stacklimit = _stackLimit;

		if (!ignoreMaxStackSize) {
			stacklimit = Math.min(stacklimit, stackIdent.getMaxStackSize());
		}

		for (int i = 0; i < _contents.length; i++) {
			if (stack.getCount() <= 0) break;
			if (_contents[i] == null) continue; //Skip Empty Slots on first attempt.

			int added = tryAddToSlot(i, stack, stacklimit);
			stack.setCount(stack.getCount() - added);
		}

		for (int i = 0; i < _contents.length; i++) {
			if (stack.getCount() <= 0) break;

			int added = tryAddToSlot(i, stack, stacklimit);
			stack.setCount(stack.getCount() - added);
		}

		markDirty();
		return stack.getCount();
	}

	/* InventoryUtil-like functions */

	private void updateContents() {
		_contentsMap.clear();
		_contentsUndamagedSet.clear();
		_contentsNoNBTSet.clear();
		_contentsUndamagedNoNBTSet.clear();
		for (ItemIdentifierStack _content : _contents) {
			if (_content == null) continue;

			ItemIdentifier itemId = _content.getItem();
			_contentsMap.merge(itemId, _content.getStackSize(), Integer::sum);
			_contentsUndamagedSet.add(itemId
					.getUndamaged()); // add is cheaper than check then add; it just returns false if it is already there
			_contentsNoNBTSet.add(itemId
					.getIgnoringNBT()); // add is cheaper than check then add; it just returns false if it is already there
			_contentsUndamagedNoNBTSet.add(itemId.getIgnoringNBT()
					.getUndamaged()); // add is cheaper than check then add; it just returns false if it is already there
		}
	}

	@Override
	public int itemCount(@Nonnull final ItemIdentifier item) {
		return _contentsMap.getOrDefault(item, 0);
	}

	@Override
	@Nonnull
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return _contentsMap;
	}

	@Override
	public boolean containsItem(final ItemIdentifier item) {
		return _contentsMap.containsKey(item);
	}

	@Override
	public boolean containsUndamagedItem(@Nonnull final ItemIdentifier item) {
		return _contentsUndamagedSet.contains(item);
	}

	@Override
	public boolean containsExcludeNBTItem(@Nonnull final ItemIdentifier item) {
		return _contentsNoNBTSet.contains(item);
	}

	@Override
	public boolean containsUndamagedExcludeNBTItem(@Nonnull final ItemIdentifier item) {
		return _contentsUndamagedNoNBTSet.contains(item);
	}

	@Override
	public boolean isEmpty() {
		return _contentsMap.isEmpty();
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

	@Override
	public void clearInventorySlotContents(int i) {
		_contents[i] = null;
		updateContents();
	}

	@Override
	public void recheckStackLimit() {
		for (ItemIdentifierStack _content : _contents) {
			if (_content != null) {
				_content.setStackSize(Math.min(_content.getStackSize(), _stackLimit));
			}
		}
	}

	private boolean isInvalidStack(@Nonnull ItemStack stack) {
		if (isLiquidInventory && !stack.isEmpty()) {
			return FluidIdentifier.get(stack) == null;
		}
		return false;
	}

	private boolean isValidStack(ItemIdentifierStack stack) {
		if (stack == null) return true;
		if (isLiquidInventory) {
			return FluidIdentifier.get(stack.getItem()) != null;
		}
		return true;
	}

	@Override
	@Nonnull
	public Iterator<Pair<ItemIdentifierStack, Integer>> iterator() {
		final Iterator<ItemIdentifierStack> iter = Arrays.asList(_contents).iterator();
		return new Iterator<Pair<ItemIdentifierStack, Integer>>() {

			int pos = -1;

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Pair<ItemIdentifierStack, Integer> next() {
				pos++;
				return new Pair<>(iter.next(), pos);
			}
		};
	}

	public void clearGrid() {
		for (int i = 0; i < getSizeInventory(); i++) {
			_contents[i] = null;
		}
		updateContents();
	}

	@Override
	@Nonnull
	public String getName() {
		return _name;
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	@Nonnull
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	public NonNullList<ItemStack> toNonNullList() {
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(0, Arrays.stream(_contents)
				.filter(Objects::nonNull)
				.map(ItemIdentifierStack::makeNormalStack)
				.collect(Collectors.toList()));
		return list;
	}

	@Override
	public @Nonnull
	List<String> getClientInformation() {
		return Arrays.stream(_contents).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

	@Nonnull
	@Override
	public Iterable<Pair<ItemIdentifierStack, Integer>> contents() {
		return this;
	}

	@Nonnull
	@Override
	public SlotAccess getSlotAccess() {
		return slotAccess;
	}

}
