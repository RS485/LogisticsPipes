package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import logisticspipes.utils.item.ItemIdentifier;

public class DSUInventoryHandler extends SpecialInventoryHandler {

	private final IDeepStorageUnit _tile;
	private final boolean _hideOnePerStack;

	private DSUInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = (IDeepStorageUnit) tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public DSUInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return tile instanceof IDeepStorageUnit;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new DSUInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		ItemStack items = _tile.getStoredItemType();
		if (!items.isEmpty() && ItemIdentifier.get(items).equals(itemIdent)) {
			return items.getCount() - (_hideOnePerStack ? 1 : 0);
		}
		return 0;
	}

	@Override
	public @Nonnull
	ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		ItemStack items = _tile.getStoredItemType();
		if (items.isEmpty() || !ItemIdentifier.get(items).equals(itemIdent)) {
			return ItemStack.EMPTY;
		}
		if (_hideOnePerStack) {
			items.shrink(1);
		}
		if (count >= items.getCount()) {
			_tile.setStoredItemCount((_hideOnePerStack ? 1 : 0));
			return items;
		}
		ItemStack newItems = items.splitStack(count);
		_tile.setStoredItemCount(items.getCount() + (_hideOnePerStack ? 1 : 0));
		return newItems;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<>();
		ItemStack items = _tile.getStoredItemType();
		if (!items.isEmpty()) {
			result.add(ItemIdentifier.get(items));
		}
		return result;
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> result = new HashMap<>();
		ItemStack items = _tile.getStoredItemType();
		if (!items.isEmpty()) {
			result.put(ItemIdentifier.get(items), items.getCount() - (_hideOnePerStack ? 1 : 0));
		}
		return result;
	}

	@Override
	public @Nonnull
	ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return getMultipleItems(itemIdent, 1);
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		ItemStack items = _tile.getStoredItemType();
		return !items.isEmpty() && ItemIdentifier.get(items).getUndamaged().equals(itemIdent);
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		if (itemIdent.tag != null) {
			return 0;
		}
		ItemStack items = _tile.getStoredItemType();
		if (items.isEmpty()) {
			return _tile.getMaxStoredCount();
		}
		if (ItemIdentifier.get(items).equals(itemIdent)) {
			return _tile.getMaxStoredCount() - items.getCount();
		}
		return 0;
	}

	@Override
	public ItemStack add(ItemStack stack, EnumFacing from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.setCount(0);
		if (stack.getTagCompound() != null) {
			return st;
		}
		ItemStack items = _tile.getStoredItemType();
		if (items.isEmpty()) {
			if (stack.getCount() <= _tile.getMaxStoredCount()) {
				_tile.setStoredItemType(stack, stack.getCount());
				st.setCount(stack.getCount());
				return st;
			} else {
				_tile.setStoredItemType(stack, _tile.getMaxStoredCount());
				st.setCount(_tile.getMaxStoredCount());
				return st;
			}
		}
		if (!items.isItemEqual(stack)) {
			return st;
		}
		if (stack.getCount() <= _tile.getMaxStoredCount() - items.getCount()) {
			_tile.setStoredItemCount(items.getCount() + stack.getCount());
			st.setCount(stack.getCount());
			return st;
		} else {
			_tile.setStoredItemCount(_tile.getMaxStoredCount());
			st.setCount(_tile.getMaxStoredCount() - items.getCount());
			return st;
		}
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public @Nonnull
	ItemStack getStackInSlot(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		return _tile.getStoredItemType();
	}

	@Override
	public @Nonnull
	ItemStack decrStackSize(int slot, int amount) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		return getMultipleItems(ItemIdentifier.get(_tile.getStoredItemType()), amount);
	}
}
