package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class BarrelInventoryHandler extends SpecialInventoryHandler {

	private static Class <? extends Object> barrelClass;
	private static Method getItemCount;
	private static Method setItemCount;
	private static Method getMaxSize;
	private static Field item;

	private final TileEntity _tile;
	private final boolean _hideOnePerStack;

	private BarrelInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public BarrelInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		try {
			barrelClass = Class.forName("factorization.common.TileEntityBarrel");
			getItemCount = barrelClass.getDeclaredMethod("getItemCount", new Class[]{});
			setItemCount = barrelClass.getDeclaredMethod("setItemCount", new Class[]{int.class});
			getMaxSize = barrelClass.getDeclaredMethod("getMaxSize", new Class[]{});
			item = barrelClass.getDeclaredField("item");
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return barrelClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new BarrelInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}


	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) == itemIdent) {
					int value = (Integer) getItemCount.invoke(_tile, new Object[]{});
					return value - (_hideOnePerStack?1:0);
				}
			}
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
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return null;
				int value = (Integer) getItemCount.invoke(_tile, new Object[]{});
				if(value - (_hideOnePerStack?1:0) < count) return null;
				setItemCount.invoke(_tile, new Object[]{value - count});
				ItemStack ret = itemStack.copy();
				ret.stackSize = count;
				return ret;
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
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				result.add(ItemIdentifier.get(itemStack));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		return result;
	}
	
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				int value = (Integer) getItemCount.invoke(_tile, new Object[]{});
				map.put(ItemIdentifier.get(itemStack), value - (_hideOnePerStack?1:0));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return null;
				int value = (Integer) getItemCount.invoke(_tile, new Object[]{});
				if(value > (_hideOnePerStack?1:0)) {
					setItemCount.invoke(_tile, new Object[]{value - 1});
					ItemStack ret = itemStack.copy();
					ret.stackSize = 1;
					return ret;
				}
			}
			return null;
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
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				return ItemIdentifier.get(itemStack) == itemIdent;
			}
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				return ItemIdentifier.getUndamaged(itemStack) == itemIdent;
			}
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			int max = (Integer) getMaxSize.invoke(_tile, new Object[]{});
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return 0;
				int value = (Integer) getItemCount.invoke(_tile, new Object[]{});
				return max - value;
			}
			return max;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
