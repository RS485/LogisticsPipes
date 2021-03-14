package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.bs.ICrateStorageProxy;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.inventory.ProviderMode;

public class CrateInventoryHandler extends SpecialInventoryHandler implements SpecialInventoryHandler.Factory {

	private final ICrateStorageProxy tile;
	private final boolean hideOne;

	private CrateInventoryHandler(TileEntity tile, ProviderMode mode) {
		this.tile = SimpleServiceLocator.betterStorageProxy.getCrateStorageProxy(tile);
		hideOne = mode.getHideOnePerStack() || mode.getHideOnePerType();
	}

	public CrateInventoryHandler() {
		tile = null;
		hideOne = false;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(@Nonnull TileEntity tile, @Nullable EnumFacing dir) {
		return SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile);
	}

	@Nullable
	@Override
	public SpecialInventoryHandler getUtilForTile(@Nonnull TileEntity tile, @Nullable EnumFacing direction, @Nonnull ProviderMode mode) {
		return new CrateInventoryHandler(tile, mode);
	}

	@Override
	@Nonnull
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<>();
		for (ItemStack stack : tile.getContents()) {
			result.add(ItemIdentifier.get(stack));
		}
		return result;
	}

	@Override
	@Nonnull
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}

	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		HashMap<ItemIdentifier, Integer> map = new HashMap<>((int) (tile.getUniqueItems() * 1.5));
		for (ItemStack stack : tile.getContents()) {
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.getCount() - (hideOne ? 1 : 0);
			map.merge(itemId, stackSize, Integer::sum);
		}
		return map;
	}

	@Override
	@Nonnull
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		int count = tile.getItemCount(itemIdent.unsafeMakeNormalStack(1));
		if (count <= (hideOne ? 1 : 0)) {
			return ItemStack.EMPTY;
		}
		return tile.extractItems(itemIdent.makeNormalStack(1), 1);
	}

	@Override
	public boolean containsUndamagedItem(@Nonnull ItemIdentifier itemIdent) {
		if (!itemIdent.isDamageable()) {
			int count = tile.getItemCount(itemIdent.unsafeMakeNormalStack(1));
			return (count > 0);
		}
		for (ItemStack stack : tile.getContents()) {
			ItemIdentifier itemId = ItemIdentifier.get(stack).getUndamaged();
			if (itemId.equals(itemIdent)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int roomForItem(@Nonnull ItemStack stack) {
		return tile.getSpaceForItem(stack);
	}

	@Override
	@Nonnull
	public ItemStack add(@Nonnull ItemStack stack, EnumFacing from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.setCount(0);
		if (doAdd) {
			ItemStack tst = stack.copy();
			ItemStack overflow = tile.insertItems(tst);
			st.setCount(stack.getCount());
			if (!overflow.isEmpty()) {
				st.shrink(overflow.getCount());
			}
		} else {
			st.setCount(Math.max(Math.min(roomForItem(stack), stack.getCount()), 0));
		}
		return st;
	}

	LinkedList<Entry<ItemIdentifier, Integer>> cached;

	@Override
	public int getSizeInventory() {
		if (cached == null) {
			initCache();
		}
		return cached.size();
	}

	public void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<>();
		cached.addAll(map.entrySet());
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		if (entry.getValue() == 0) {
			return ItemStack.EMPTY;
		}
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int i, int j) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		ItemStack stack = entry.getKey().makeNormalStack(j);
		int count = tile.getItemCount(stack);
		if (count <= (hideOne ? 1 : 0)) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = tile.extractItems(stack, 1);
		entry.setValue(entry.getValue() - j);
		return extracted;
	}
}
