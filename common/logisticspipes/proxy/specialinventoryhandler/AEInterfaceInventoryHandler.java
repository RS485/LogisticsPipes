package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.me.tiles.ITileInterfaceApi;

/*
 * Compatibility for Applied Energistics
 * http://www.minecraftforum.net/topic/1625015-151-applied-energistics-rv-10-f-and-rv-9-i/
 */

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler {
	public static boolean init = false;
	private final ITileInterfaceApi _tile;
	private final boolean _hideOnePerStack;

	private AEInterfaceInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = (ITileInterfaceApi)tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public AEInterfaceInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		AEInterfaceInventoryHandler.init=true;
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof ITileInterfaceApi;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new AEInterfaceInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}
	
	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		Map<ItemIdentifier, Integer> result;
		if(linked) {
			result = new LinkedHashMap<ItemIdentifier, Integer>();
		} else {
			result = new HashMap<ItemIdentifier, Integer>();
		}
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.get(items);
			Integer count = result.get(ident);
			if(count != null) {
				result.put(ident, count + items.stackSize - (_hideOnePerStack ? 1:0));
			} else {
				result.put(ident, items.stackSize - (_hideOnePerStack ? 1:0));
			}
		}
		return result;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.get(items);
			result.add(ident);
		}
		return result;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		return _tile.apiExtractNetworkItem(item.makeNormalStack(1), true);
	}

	@Override
	public boolean containsItem(ItemIdentifier item) {
		ItemStack result = _tile.apiExtractNetworkItem(item.unsafeMakeNormalStack(1), false);
		return result != null;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.getUndamaged(items);
			if(ident == item) {
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
		return _tile.apiCurrentAvailableSpace(item.unsafeMakeNormalStack(1), count);
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		ItemStack tst = stack.copy();
		if(tst.stackTagCompound != null && tst.stackTagCompound.getName().equals("")) {
			tst.stackTagCompound.setName("tag");
		}
		
		ItemStack overflow = _tile.apiAddNetworkItem(tst, doAdd);
		if(overflow != null) {
			st.stackSize -= overflow.stackSize;
		}
		return st;
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	LinkedList<Entry<ItemIdentifier, Integer>> cached;

	@Override
	public int getSizeInventory() {
		if(cached == null) initCache();
		return cached.size();
	}
	
	public void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<Map.Entry<ItemIdentifier,Integer>>();
		for(Entry<ItemIdentifier, Integer> e:map.entrySet()) {
			cached.add(e);
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if(cached == null) initCache();
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		if(entry.getValue() == 0) return null;
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(cached == null) initCache();
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		ItemStack stack = entry.getKey().makeNormalStack(j);
		ItemStack extracted = _tile.apiExtractNetworkItem(stack, true);
		entry.setValue(entry.getValue() - j);
		return extracted;
	}
}
