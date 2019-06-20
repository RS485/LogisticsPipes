package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.factorization.FactorizationProxy;
import logisticspipes.utils.item.ItemIdentifier;

public class BarrelInventoryHandler extends SpecialInventoryHandler {

	private static Class<?> barrelClass;
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
			BarrelInventoryHandler.barrelClass = Class.forName(FactorizationProxy.barelClassPath);
			BarrelInventoryHandler.getItemCount = BarrelInventoryHandler.barrelClass.getDeclaredMethod("getItemCount"); // ()I
			BarrelInventoryHandler.setItemCount = BarrelInventoryHandler.barrelClass.getDeclaredMethod("setItemCount", int.class); // (I)V
			BarrelInventoryHandler.getMaxSize = BarrelInventoryHandler.barrelClass.getDeclaredMethod("getMaxSize"); // ()I
			BarrelInventoryHandler.item = BarrelInventoryHandler.barrelClass.getDeclaredField("item");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return SimpleServiceLocator.factorizationProxy.isBarral(tile);
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new BarrelInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				if (ItemIdentifier.get(itemStack).equals(itemIdent)) {
					int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
					return value - (_hideOnePerStack ? 1 : 0);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				if (!ItemIdentifier.get(itemStack).equals(itemIdent)) {
					return ItemStack.EMPTY;
				}
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
				if (value - (_hideOnePerStack ? 1 : 0) < count) {
					return ItemStack.EMPTY;
				}
				BarrelInventoryHandler.setItemCount.invoke(_tile, value - count);
				ItemStack ret = itemStack.copy();
				ret.setCount(count);
				return ret;
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<>();
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				result.add(ItemIdentifier.get(itemStack));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> map = new HashMap<>();
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
				map.put(ItemIdentifier.get(itemStack), value - (_hideOnePerStack ? 1 : 0));
			}
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				if (!ItemIdentifier.get(itemStack).equals(itemIdent)) {
					return ItemStack.EMPTY;
				}
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
				if (value > (_hideOnePerStack ? 1 : 0)) {
					BarrelInventoryHandler.setItemCount.invoke(_tile, value - 1);
					ItemStack ret = itemStack.copy();
					ret.setCount(1);
					return ret;
				}
			}
			return null;
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				return ItemIdentifier.get(itemStack).getUndamaged().equals(itemIdent);
			}
			return false;
		} catch (IllegalArgumentException | IllegalAccessException e) {
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
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			int max = (Integer) BarrelInventoryHandler.getMaxSize.invoke(_tile, new Object[] {});
			if (itemStack != null && !itemStack.isEmpty()) {
				if (!ItemIdentifier.get(itemStack).equals(itemIdent)) {
					return 0;
				}
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
				return max - value;
			}
			return max;
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public ItemStack add(ItemStack stack, EnumFacing from, boolean doAdd) {
		assert _tile != null;

		ItemStack st = stack.copy();
		st.setCount(0);
		if (from != EnumFacing.UP) {
			return st;
		}
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack == null) {
				st.setCount(stack.getCount());
				if (doAdd) {
					ItemStack tst = stack.copy();
					((IInventory) _tile).setInventorySlotContents(0, tst);
				}
			} else {
				if (!ItemIdentifier.get(itemStack).equals(ItemIdentifier.get(stack))) {
					return st;
				}
				int max = (Integer) BarrelInventoryHandler.getMaxSize.invoke(_tile);
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile);
				int room = max - value;
				st.setCount(Math.max(Math.min(room, stack.getCount()), 0));
				if (doAdd && st.getCount() > 0) {
					BarrelInventoryHandler.setItemCount.invoke(_tile, value + st.getCount());
				}
			}
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return st;
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i != 0) {
			return null;
		}
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			if (itemStack != null && !itemStack.isEmpty()) {
				int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile);
				value -= _hideOnePerStack ? 1 : 0;
				if (value > 0) {
					ItemStack ret = itemStack.copy();
					ret.setCount(value);
					return ret;
				}
			}
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		try {
			ItemStack itemStack = (ItemStack) BarrelInventoryHandler.item.get(_tile);
			int value = (Integer) BarrelInventoryHandler.getItemCount.invoke(_tile, new Object[] {});
			j = Math.min(j, value - (_hideOnePerStack ? 1 : 0));
			if (j > 0) {
				BarrelInventoryHandler.setItemCount.invoke(_tile, value - j);
				ItemStack ret = itemStack.copy();
				ret.setCount(j);
				return ret;
			}
		} catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
}
