package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class CrateInventoryHandler extends SpecialInventoryHandler {

	private static Class <? extends Object> crateClass;
	private static Method getPileData;
	private static Class <? extends Object> cratePileDataClass;
	private static Method getNumItems;
	private static Method removeItems;
	private static Method getItemStack;
	private static Method getItemCount;
	private static Method spaceForItem;
	private static Method addItems;

	private final TileEntity _tile;
	private final boolean _hideOnePerStack;

	private CrateInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public CrateInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		try {
			crateClass = Class.forName("net.mcft.copy.betterstorage.block.crate.TileEntityCrate");
			getPileData = crateClass.getDeclaredMethod("getPileData", new Class[]{});
			cratePileDataClass = Class.forName("net.mcft.copy.betterstorage.block.crate.CratePileData");
			getNumItems = cratePileDataClass.getDeclaredMethod("getNumItems", new Class[]{});
			removeItems = cratePileDataClass.getDeclaredMethod("removeItems", new Class[]{ItemStack.class, int.class});
			getItemStack = cratePileDataClass.getDeclaredMethod("getItemStack", new Class[]{int.class});
			getItemCount = cratePileDataClass.getDeclaredMethod("getItemCount", new Class[]{ItemStack.class});
			spaceForItem = cratePileDataClass.getDeclaredMethod("spaceForItem", new Class[]{ItemStack.class});
			addItems = cratePileDataClass.getDeclaredMethod("addItems", new Class[]{ItemStack.class});
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
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new CrateInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				result.add(ItemIdentifier.get(itemStack));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}
		
	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>((int)(numitems * 1.5));
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.get(itemStack);
				int stackSize = itemStack.stackSize - (_hideOnePerStack?1:0);
				Integer m = map.get(itemId);
				if (m==null){
					map.put(itemId, stackSize);
				} else {
					map.put(itemId, m + stackSize);
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
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.unsafeMakeNormalStack(1)});
			if (count <= (_hideOnePerStack?1:0)) return null;
			return (ItemStack) removeItems.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1), 1});
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
	public boolean containsItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.unsafeMakeNormalStack(1)});
			return (count > 0);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			if (!Item.itemsList[itemIdent.itemID].isDamageable()) {
				int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.unsafeMakeNormalStack(1)});
				return (count > 0);
			}
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.getUndamaged(itemStack);
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

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int space = (Integer) spaceForItem.invoke(cratePileData, new Object[]{itemIdent.unsafeMakeNormalStack(1)});
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
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.stackSize = 0;
		if(doAdd) {
			ItemStack tst = stack.copy();
			if(tst.stackTagCompound != null && tst.stackTagCompound.getName().equals("")) {
				tst.stackTagCompound.setName("tag");
			}
			try {
				Object cratePileData = getPileData.invoke(_tile, new Object[]{});
				ItemStack overflow = (ItemStack) addItems.invoke(cratePileData, new Object[]{tst});
				st.stackSize = stack.stackSize;
				if(overflow != null) {
					st.stackSize -= overflow.stackSize;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			int space = roomForItem(ItemIdentifier.get(stack), 0);
			st.stackSize = Math.max(Math.min(space, stack.stackSize), 0);
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
		ItemStack extracted = null;
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{stack});
			if (count <= (_hideOnePerStack?1:0)) return null;
			extracted = (ItemStack) removeItems.invoke(cratePileData, new Object[]{stack, 1});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		entry.setValue(entry.getValue() - j);
		return extracted;
	}
}
