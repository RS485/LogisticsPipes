package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
/*
 * Compatibility for Applied Energistics
 * http://www.minecraftforum.net/topic/1625015-
 */

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler {

	private static Class <? extends Object> interfaceClass;
	private static Method apiGetNetworkContents;
	private static Method apiExtractNetworkItem;
	private static Method apiCurrentAvailableSpace;
	
	private final TileEntity _tile;
	private final boolean _hideOnePerStack;

	private AEInterfaceInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public AEInterfaceInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		try {
			for(ItemStack items: ((List<ItemStack>)apiGetNetworkContents.invoke(_tile))) {
				ItemIdentifier ident = ItemIdentifier.get(items);
				if(result.containsKey(ident)) {
					result.put(ident, result.get(ident) + items.stackSize - (_hideOnePerStack ? 1:0));
				} else {
					result.put(ident, items.stackSize - (_hideOnePerStack ? 1:0));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		try {
			return (ItemStack) apiExtractNetworkItem.invoke(_tile, new Object[]{item.makeNormalStack(1), true});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean containsItem(ItemIdentifier item) {
		try {
			ItemStack result = (ItemStack) apiExtractNetworkItem.invoke(_tile, new Object[]{item.unsafeMakeNormalStack(1), false});
			return result != null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		try {
			for(ItemStack items: ((List<ItemStack>)apiGetNetworkContents.invoke(_tile))) {
				ItemIdentifier ident = ItemIdentifier.getUndamaged(items);
				if(ident == item) {
					return true;
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		try {
			return (Integer) apiCurrentAvailableSpace.invoke(_tile, new Object[]{item.unsafeMakeNormalStack(1), item.getMaxStackSize()});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean init() {
		try {
			interfaceClass = Class.forName("appeng.me.tile.TileInterfaceBase");
			apiGetNetworkContents = interfaceClass.getDeclaredMethod("apiGetNetworkContents");
			apiExtractNetworkItem = interfaceClass.getDeclaredMethod("apiExtractNetworkItem", new Class[]{ItemStack.class, boolean.class});
			apiCurrentAvailableSpace = interfaceClass.getDeclaredMethod("apiCurrentAvailableSpace", new Class[]{ItemStack.class, int.class});
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return interfaceClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new AEInterfaceInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}
}
