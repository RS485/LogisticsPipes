package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
//TODO: ae related fixme
//import appeng.api.me.tiles.ITileInterfaceApi;
/*
 * Compatibility for Applied Energistics
 * http://www.minecraftforum.net/topic/1625015-
 */

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler {
	public static boolean init = false;
	//private final ITileInterfaceApi _tile;
	private final boolean _hideOnePerStack;

	private AEInterfaceInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		//_tile = (ITileInterfaceApi)tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public AEInterfaceInventoryHandler() {
		//_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		AEInterfaceInventoryHandler.init=true;
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		//TODO ae related fixme
		//return tile instanceof ITileInterfaceApi;
		return false;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new AEInterfaceInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		//TODO: fixme
		/*HashMap<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.get(items);
			Integer count = result.get(ident);
			if(count != null) {
				result.put(ident, count + items.stackSize - (_hideOnePerStack ? 1:0));
			} else {
				result.put(ident, items.stackSize - (_hideOnePerStack ? 1:0));
			}
		}
		return result;*/
		return null;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		//TODO: fixme
		/*
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.get(items);
			result.add(ident);
		}
		return result;
		*/
		return null;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		//TODO ae related fixme
		//return _tile.apiExtractNetworkItem(item.makeNormalStack(1), true);
		return null;
	}

	@Override
	public boolean containsItem(ItemIdentifier item) {
		//TODO ae related fixme
		//ItemStack result = _tile.apiExtractNetworkItem(item.unsafeMakeNormalStack(1), false);
		//return result != null;
		return false;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		//TODO: fixme
				/*
		for(ItemStack items: _tile.apiGetNetworkContents()) {
			ItemIdentifier ident = ItemIdentifier.getUndamaged(items);
			if(ident == item) {
				return true;
			}
		}
		return false;*/
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, item.getMaxStackSize());
	}

	@Override
	public int roomForItem(ItemIdentifier item, int count) {
		//TODO ae related fixme
		//return _tile.apiCurrentAvailableSpace(item.unsafeMakeNormalStack(1), count);
		return 0;
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		ItemStack tst = stack.copy();
		if(tst.stackTagCompound != null && tst.stackTagCompound.getName().equals("")) {
			tst.stackTagCompound.setName("tag");
		}
		//TODO ae related fixme
		/*ItemStack overflow = _tile.apiAddNetworkItem(tst, doAdd);
		if(overflow != null) {
			st.stackSize -= overflow.stackSize;
		}
		return st;*/
		return null;
	}
}
