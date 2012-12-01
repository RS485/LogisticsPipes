package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public class QuantumChestHandler implements ISpecialInventoryHandler {
	
	Class<?> GT_TileEntity_Quantumchest;
	Field mItemCount;
	Method getStoredItemData;
	
	@Override
	public boolean init() {
		try {
			GT_TileEntity_Quantumchest = Class.forName("gregtechmod.common.tileentities.GT_TileEntity_Quantumchest");
			mItemCount = GT_TileEntity_Quantumchest.getDeclaredField("mItemCount");
			mItemCount.setAccessible(true);
			getStoredItemData = GT_TileEntity_Quantumchest.getDeclaredMethod("getStoredItemData", new Class[]{});
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return GT_TileEntity_Quantumchest.isAssignableFrom(tile.getClass());
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount(TileEntity tile) {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		ItemStack stack = ((IInventory)tile).getStackInSlot(1);
		if(data.length < 1 || data[0] == null) return map;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(dataIdent != stackIdent) {
			if(data[0].stackSize != 0) map.put(dataIdent, data[0].stackSize);
			if(stack.stackSize != 0) map.put(stackIdent, stack.stackSize);
		} else {
			map.put(dataIdent, data[0].stackSize + stack.stackSize - 1);
		}
		return map;
	}

	@Override
	public int roomForItem(TileEntity tile, ItemIdentifier item) {
		int result = Integer.MAX_VALUE - 128;
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null) return result;
		ItemStack stack = ((IInventory)tile).getStackInSlot(1);
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(item == dataIdent || item == stackIdent) {
			return result - (data[0].stackSize + stack.stackSize);
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack getSingleItem(TileEntity tile, ItemIdentifier item) {
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null) return null;
		ItemStack stack = ((IInventory)tile).getStackInSlot(1);
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(stackIdent == item && stack.stackSize > 1) {
			stack.stackSize--;
			return stackIdent.makeNormalStack(1);
		}
		if(dataIdent == item && data[0].stackSize > 0) {
			try {
				mItemCount.set(tile, data[0].stackSize - 1);
				return dataIdent.makeNormalStack(1);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if(stackIdent == item && stack.stackSize > 0) {
			stack.stackSize--;
			return stackIdent.makeNormalStack(1);
		}
		return null;
	}

	@Override
	public boolean containsItem(TileEntity tile, ItemIdentifier item) {
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null) return false;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		return item == dataIdent;
	}

}
