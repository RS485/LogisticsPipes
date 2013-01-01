package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class CrateInventoryHandler implements ISpecialInventoryHandler {

	private Class <? extends Object> crateClass;
	private Method getPileData;
	private Class <? extends Object> cratePileDataClass;
	private Method getNumItems;
	private Method removeItems;
	private Method getItemStack;
	private Method getItemCount;
	private Method spaceForItem;

	@Override
	public boolean init() {
		try {
			crateClass = Class.forName("net.mcft.copy.betterstorage.blocks.TileEntityCrate");
			getPileData = crateClass.getDeclaredMethod("getPileData", new Class[]{});
			cratePileDataClass = Class.forName("net.mcft.copy.betterstorage.blocks.CratePileData");
			getNumItems = cratePileDataClass.getDeclaredMethod("getNumItems", new Class[]{});
			removeItems = cratePileDataClass.getDeclaredMethod("removeItems", new Class[]{ItemStack.class, int.class});
			getItemStack = cratePileDataClass.getDeclaredMethod("getItemStack", new Class[]{int.class});
			getItemCount = cratePileDataClass.getDeclaredMethod("getItemCount", new Class[]{ItemStack.class});
			spaceForItem = cratePileDataClass.getDeclaredMethod("spaceForItem", new Class[]{ItemStack.class});
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return crateClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount(TileEntity tile) {
		try {
			Object cratePileData = getPileData.invoke(tile, new Object[]{});
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>((int)(numitems * 1.5));
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.get(itemStack);
				if (!map.containsKey(itemId)){
					map.put(itemId, itemStack.stackSize);
				} else {
					map.put(itemId, map.get(itemId) + itemStack.stackSize);
				}
			}
			return map;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return new HashMap<ItemIdentifier, Integer>();
	}

	@Override
	public int roomForItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(tile, new Object[]{});
			int space = (Integer) spaceForItem.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1)});
			return space;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public ItemStack getSingleItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(tile, new Object[]{});
			if (!Item.itemsList[itemIdent.itemID].isDamageable()) {
				return (ItemStack) removeItems.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1), 1});
			}
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.get(itemStack);
				if(itemId == itemIdent) {
					return (ItemStack) removeItems.invoke(cratePileData, new Object[]{itemStack, 1});
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean containsItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(tile, new Object[]{});
			if (!Item.itemsList[itemIdent.itemID].isDamageable()) {
				int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1)});
				return (count > 0);
			}
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.get(itemStack);
				if(itemId == itemIdent) {
					return true;
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
}
