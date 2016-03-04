package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;

/*
 * Compatibility for Applied Energistics
 * http://www.minecraftforum.net/topic/1625015-151-applied-energistics-rv-10-f-and-rv-9-i/
 */

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler {

	private final ITileStorageMonitorable tile;
	private final boolean hideOnePerStack;
	private final MachineSource source;
	private final ForgeDirection dir;
	public boolean init = false;
	LinkedList<Entry<ItemIdentifier, Integer>> cached;

	private AEInterfaceInventoryHandler(TileEntity tile, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		if (dir.equals(ForgeDirection.UNKNOWN)) {
			throw new IllegalArgumentException("The direction must not be unknown");
		}
		this.tile = (ITileStorageMonitorable) tile;
		this.hideOnePerStack = hideOnePerStack || hideOne;
		source = new MachineSource(new LPActionHost(((IGridHost) tile).getGridNode(dir)));
		this.dir = dir;
	}

	public AEInterfaceInventoryHandler() {
		tile = null;
		hideOnePerStack = false;
		source = null;
		dir = ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean init() {
		init = true;
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof ITileStorageMonitorable && tile instanceof IGridHost;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new AEInterfaceInventoryHandler(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}

	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		Map<ItemIdentifier, Integer> result;
		if (linked) {
			result = new LinkedHashMap<ItemIdentifier, Integer>();
		} else {
			result = new HashMap<ItemIdentifier, Integer>();
		}
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null || tmp.getItemInventory().getStorageList() == null) {
			return result;
		}
		for (IAEItemStack items : tmp.getItemInventory().getStorageList()) {
			ItemIdentifier ident = ItemIdentifier.get(items.getItemStack());
			Integer count = result.get(ident);
			if (count != null) {
				result.put(ident, (int) (count + items.getStackSize() - (hideOnePerStack ? 1 : 0)));
			} else {
				result.put(ident, (int) (items.getStackSize() - (hideOnePerStack ? 1 : 0)));
			}
		}
		return result;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null || tmp.getItemInventory().getStorageList() == null) {
			return result;
		}
		for (IAEItemStack items : tmp.getItemInventory().getStorageList()) {
			ItemIdentifier ident = ItemIdentifier.get(items.getItemStack());
			result.add(ident);
		}
		return result;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null) {
			return null;
		}
		IAEItemStack stack = AEApi.instance().storage().createItemStack(item.makeNormalStack(1));
		IAEItemStack extract = tmp.getItemInventory().extractItems(stack, Actionable.MODULATE, source);
		if (extract == null) {
			return null;
		}
		return extract.getItemStack();
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier item, int count) {
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null) {
			return null;
		}
		IAEItemStack stack = AEApi.instance().storage().createItemStack(item.makeNormalStack(count));
		IAEItemStack extract = tmp.getItemInventory().extractItems(stack, Actionable.MODULATE, source);
		if (extract == null) {
			return null;
		}
		return extract.getItemStack();
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null || tmp.getItemInventory().getStorageList() == null) {
			return false;
		}
		for (IAEItemStack items : tmp.getItemInventory().getStorageList()) {
			ItemIdentifier ident = ItemIdentifier.get(items.getItemStack());
			if (ident.equals(item)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, item.getMaxStackSize());
	}

	@Override
	public int roomForItem(ItemIdentifier item, int count) {
		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null) {
			return 0;
		}
		while (count > 0) {
			IAEItemStack stack = AEApi.instance().storage().createItemStack(item.makeNormalStack(count));
			if (tmp.getItemInventory().canAccept(stack)) {
				return count;
			}
			count--;
		}
		return 0;
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		IAEItemStack tst = AEApi.instance().storage().createItemStack(stack);

		IStorageMonitorable tmp = tile.getMonitorable(dir, source);
		if (tmp == null || tmp.getItemInventory() == null) {
			return st;
		}
		IAEItemStack overflow = tmp.getItemInventory().injectItems(tst, Actionable.MODULATE, source);
		if (overflow != null) {
			st.stackSize -= overflow.getStackSize();
		}
		return st;
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		if (cached == null) {
			initCache();
		}
		return cached.size();
	}

	public void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<Map.Entry<ItemIdentifier, Integer>>();
		for (Entry<ItemIdentifier, Integer> e : map.entrySet()) {
			cached.add(e);
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		if (entry.getValue() == 0) {
			return null;
		}
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		ItemStack extracted = getMultipleItems(entry.getKey(), j);
		entry.setValue(entry.getValue() - j);
		return extracted;
	}

	private class LPActionHost implements IActionHost {

		public IGridNode node;

		public LPActionHost(IGridNode node) {
			this.node = node;
		}

		@Override
		public void securityBreak() {}

		@Override
		public IGridNode getGridNode(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public AECableType getCableConnectionType(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public IGridNode getActionableNode() {
			return node;
		}
	}
}
