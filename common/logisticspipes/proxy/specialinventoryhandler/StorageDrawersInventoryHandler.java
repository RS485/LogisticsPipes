package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroupInteractive;

public class StorageDrawersInventoryHandler extends SpecialInventoryHandler {

	private final IDrawerGroupInteractive _drawer;
	private final boolean _hideOnePerStack;
	private final boolean _hideOnePerType;

	private StorageDrawersInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_drawer = (IDrawerGroupInteractive) tile;
		_hideOnePerStack = hideOnePerStack;
		_hideOnePerType = hideOne;
	}

	public StorageDrawersInventoryHandler() {
		_drawer = null;
		_hideOnePerStack = false;
		_hideOnePerType = false;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof IDrawerGroupInteractive;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new StorageDrawersInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}


	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		int count = 0;
		boolean first = true;
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				if(ItemIdentifier.get(drawer.getStoredItemCopy()).equals(itemIdent)) {
					count += drawer.getStoredItemCount() - (_hideOnePerStack ? 1:0) - (_hideOnePerType && first ? 1:0);
					first = false;
				}
			}
		}
		return count;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		ItemStack stack = null;
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				if(ItemIdentifier.get(drawer.getStoredItemCopy()).equals(itemIdent)) {
					if(stack == null) {
						stack = _drawer.takeItemsFromSlot(i, count);
						count -= stack.stackSize;
						if(count <= 0) break;
					} else {
						ItemStack toAdd = _drawer.takeItemsFromSlot(i, count);
						if(!ItemIdentifier.get(toAdd).equals(itemIdent)) throw new UnsupportedOperationException();
						stack.stackSize += toAdd.stackSize;
						count -= toAdd.stackSize;
						if(count <= 0) break;
					}
				}
			}
		}
		return stack;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				result.add(ItemIdentifier.get(drawer.getStoredItemCopy()));
			}
		}
		return result;
	}
	
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				int count = drawer.getStoredItemCount();
				ItemIdentifier ident = ItemIdentifier.get(drawer.getStoredItemCopy());
				if(result.containsKey(ident)) {
					result.put(ident, result.get(ident) + count);
				} else {
					result.put(ident, count);
				}
			}
		}
		return result;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return getMultipleItems(itemIdent, 1);
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				if(drawer.canItemBeStored(itemIdent.makeNormalStack(1))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() != null) {
				if(ItemIdentifier.get(drawer.getStoredItemCopy()).getUndamaged().equals(itemIdent)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		int room = 0;
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null && drawer.getStoredItemCopy() == null) {
				count += drawer.getMaxCapacity();
			} else if(drawer != null && drawer.canItemBeStored(itemIdent.makeNormalStack(1))) {
				count += drawer.getRemainingCapacity();
			}
		}
		if(count != 0) {
			return Math.min(room, count);
		} else {
			return room;
		}
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.stackSize = 0;
		
		for(int i=0; i < _drawer.getDrawerCount(); i++) {
			IDrawer drawer = _drawer.getDrawer(i);
			if(drawer != null) {
				if(drawer.canItemBeStored(stack)) {
					int used = _drawer.putItemsIntoSlot(i, stack.copy(), stack.stackSize);
					stack.stackSize -= used;
					st.stackSize += used;
				}
			}
		}
		return st;
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return _drawer.getDrawerCount();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _drawer.getDrawer(i) != null ? _drawer.getDrawer(i).getStoredItemCopy() : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _drawer.takeItemsFromSlot(i, j);
	}
}
