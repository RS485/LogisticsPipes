package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public class BarrelInventoryHandler implements ISpecialInventoryHandler {

	private Class <? extends Object> barrelClass;
	private Method getItemCount;
	private Method setItemCount;
	private Method getMaxSize;
	private Field item;
	
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
	public HashMap<ItemIdentifier, Integer> getItemsAndCount(TileEntity tile) {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		try {
			ItemStack itemStack = (ItemStack) item.get(tile);
			if(itemStack != null) {
				int value = (Integer) getItemCount.invoke(tile, new Object[]{});
				map.put(ItemIdentifier.get(itemStack), value);
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
	public int roomForItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(tile);
			int max = (Integer) getMaxSize.invoke(tile, new Object[]{});
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return 0;
				int value = (Integer) getItemCount.invoke(tile, new Object[]{});
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

	@Override
	public ItemStack getSingleItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(tile);
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return null;
				int value = (Integer) getItemCount.invoke(tile, new Object[]{});
				if(value > 0) {
					setItemCount.invoke(tile, new Object[]{value - 1});
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
	public boolean containsItem(TileEntity tile, ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(tile);
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
}
