package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class BarrelModInventoryHandler extends SpecialInventoryHandler {

	private static Class <? extends Object> barrelClass;
	private static Method getStackLimit;
	private static Field item;
	private static Method getModeForSide;
	private static Method inventoryChanged;

	private final TileEntity _tile;
	private final boolean _hideOnePerStack;

	private BarrelModInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public BarrelModInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		try {
			barrelClass = Class.forName("need4speed402.mods.barrels.TileEntityBarrel");
			getStackLimit = barrelClass.getDeclaredMethod("getStackLimit", new Class[]{});
			item = barrelClass.getDeclaredField("item");
			item.setAccessible(true);
			getModeForSide = barrelClass.getDeclaredMethod("getModeForSide", new Class[]{int.class});
			inventoryChanged = barrelClass.getDeclaredMethod("inventoryChanged");
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
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new BarrelModInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}


	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) == itemIdent) {
					int value = itemStack.stackSize;
					return value - (_hideOnePerStack?1:0);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
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
				int value = itemStack.stackSize;
				if(value - (_hideOnePerStack?1:0) < count) return null;
				itemStack.stackSize = value - count;
				inventoryChanged.invoke(_tile);
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
				int value = itemStack.stackSize;
				map.put(ItemIdentifier.get(itemStack), value - (_hideOnePerStack?1:0));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
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
				int value = itemStack.stackSize;
				if(value > (_hideOnePerStack?1:0)) {
					itemStack.stackSize = value - 1;
					inventoryChanged.invoke(_tile);
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
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}
	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			int max = (Integer) getStackLimit.invoke(_tile, new Object[]{});
			if(itemStack != null) {
				if(ItemIdentifier.get(itemStack) != itemIdent) return 0;
				max *= itemStack.getMaxStackSize();
				int value = itemStack.stackSize;
				return max - value;
			} else {
				max *= itemIdent.makeNormalStack(1).getMaxStackSize();
				return max;
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
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.stackSize = 0;
		if(from == ForgeDirection.UNKNOWN) return st;
		try {
			byte mode = (Byte) getModeForSide.invoke(_tile, from.ordinal());
			if(mode != 2 && mode != 3) return st;
			ItemStack itemStack = (ItemStack) item.get(_tile);
			if(itemStack == null) {
				st.stackSize = stack.stackSize;
				if(doAdd) {
					ItemStack tst = stack.copy();
					if(tst.stackTagCompound != null && tst.stackTagCompound.getName().equals("")) {
						tst.stackTagCompound.setName("tag");
					}
					((IInventory)_tile).setInventorySlotContents(0, tst);
				}
			} else {
				if(ItemIdentifier.get(itemStack) != ItemIdentifier.get(stack)) return st;
				int max = (Integer) getStackLimit.invoke(_tile, new Object[]{});
				max *= itemStack.getMaxStackSize();
				int value = itemStack.stackSize;
				int room = max - value;
				st.stackSize = Math.max(Math.min(room, stack.stackSize), 0);
				if(doAdd && st.stackSize > 0) {
					itemStack.stackSize = value + st.stackSize;
					inventoryChanged.invoke(_tile);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
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
		if(i != 0) return null;
		try {
			return (ItemStack) item.get(_tile);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		try {
			ItemStack itemStack = (ItemStack) item.get(_tile);
			int value = itemStack.stackSize;
			if(value > (_hideOnePerStack?1:0)) {
				itemStack.stackSize = value - 1;
				ItemStack ret = itemStack.copy();
				ret.stackSize = 1;
				return ret;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
}
