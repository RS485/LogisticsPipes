package logisticspipes.proxy.specialinventoryhandler;

import gregtechmod.api.interfaces.IDigitalChest;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class DigitalChestHandler extends SpecialInventoryHandler {

	private final IDigitalChest _tile;
	private final boolean _hideOnePerStack;
	private static boolean apiIsBroken = false;

	private DigitalChestHandler(IDigitalChest tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public DigitalChestHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		if(apiIsBroken) return false;
		try {
			return (tile instanceof IDigitalChest) && ((IDigitalChest)tile).isDigitalChest() && (((IInventory)tile).getSizeInventory() == 3);
		} catch (Throwable e) {
			LogisticsPipes.log.info("Looks like greg broke his API again, disabling Digital/Quantum chest support.");
			apiIsBroken = true;
			return false;
		}
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new DigitalChestHandler((IDigitalChest)tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		ItemIdentifierStack content = getContents();
		if(content == null) return 0;
		if(content.getItem() != itemIdent) return 0;
		return content.stackSize - (_hideOnePerStack?1:0);
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		ItemIdentifierStack content = getContents();
		if(content != null) {
			result.add(content.getItem());
		}
		return result;
	}
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		ItemIdentifierStack content = getContents();
		if(content != null) {
			map.put(content.getItem(), content.stackSize - (_hideOnePerStack?1:0));
		}
		return map;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return getMultipleItems(itemIdent,1);
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		ItemIdentifierStack content = getContents();
		if(content == null) return false;
		return itemIdent == content.getItem();
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		ItemIdentifierStack content = getContents();
		if(content == null) return false;
		return itemIdent.getUndamaged() == content.getItem().getUndamaged();
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		ItemIdentifierStack content = getContents();
		if(content == null) return 0;
		if(content.getItem() != itemIdent) return 0;
		return _tile.getMaxItemCount() + 3 * itemIdent.getMaxStackSize() - content.stackSize;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		//check that we can actually get what we want
		ItemIdentifierStack content = getContents();
		if(content == null) return null;
		if(content.getItem() != itemIdent) return null;
		if(content.stackSize < count) return null;

		//set up the finished stack to return
		ItemStack resultstack = content.makeNormalStack();
		resultstack.stackSize = count;

		//empty input slots first
		for(int i = 1; count > 0 && i < 3; i++) {
			ItemStack stack = ((IInventory)_tile).getStackInSlot(i);
			if(stack != null && ItemIdentifier.get(stack) == itemIdent) {
				int wanted = Math.min(count, stack.stackSize);
				stack.stackSize -= wanted;
				count -= wanted;
			}
		}
		if(count == 0) return resultstack;

		//now take from internal storage
		ItemStack[] data = _tile.getStoredItemData();
		int wanted = Math.min(count, data[0].stackSize);
		_tile.setItemCount(data[0].stackSize - wanted);
		count -= wanted;
		if(count == 0) return resultstack;

		//and finally use the output slot
		ItemStack stack = ((IInventory)_tile).getStackInSlot(0);
		if(stack != null && ItemIdentifier.get(stack) == itemIdent) {
			wanted = Math.min(count, stack.stackSize - (_hideOnePerStack?1:0));
			stack.stackSize -= wanted;
			count -= wanted;
		}
		return resultstack;
	}

	private ItemIdentifierStack getContents(){
		ItemStack[] data = _tile.getStoredItemData();
		if(data == null || data.length < 1 || data[0] == null || data[0].itemID < 1) return null;
		ItemIdentifierStack dataIdent = ItemIdentifierStack.GetFromStack(data[0]);
		for(int i = 0; i < 3; i++) {
			ItemStack stack = ((IInventory)_tile).getStackInSlot(i);
			if(stack != null && ItemIdentifier.get(stack) == dataIdent.getItem()) {
				dataIdent.stackSize += stack.stackSize;
			}
		}
		return dataIdent;
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemIdentifier itemIdent = ItemIdentifier.get(stack);
		ItemStack st = stack.copy();
		st.stackSize = 0;
		ItemIdentifierStack content = getContents();
		if(content == null) return st;
		if(content.getItem() != itemIdent) return st;

		if(!doAdd) {
			int space = _tile.getMaxItemCount() + 3 * itemIdent.getMaxStackSize() - content.stackSize;
			st.stackSize = Math.max(Math.min(space, stack.stackSize), 0);
			return st;
		}

		//add to output slot first
		ItemStack slot = ((IInventory)_tile).getStackInSlot(0);
		if(slot == null) {
			slot = st.copy();
			slot.stackSize = 0;
		}
		if(ItemIdentifier.get(slot) == itemIdent) {
			int toadd = Math.min(slot.getMaxStackSize() - slot.stackSize, stack.stackSize - st.stackSize);
			if(toadd > 0) {
				st.stackSize += toadd;
				slot.stackSize += toadd;
				((IInventory)_tile).setInventorySlotContents(0, slot);
			}
		}
		if(stack.stackSize - st.stackSize == 0) return st;

		//now internal storage
		ItemStack[] data = _tile.getStoredItemData();
		int toadd = Math.min(_tile.getMaxItemCount() - data[0].stackSize, stack.stackSize - st.stackSize);
		if(toadd > 0) {
			st.stackSize += toadd;
			_tile.setItemCount(data[0].stackSize + toadd);
		}
		if(stack.stackSize - st.stackSize == 0) return st;

		//and finally the input slots
		for(int i = 1; i < 3; i++) {
			slot = ((IInventory)_tile).getStackInSlot(i);
			if(slot == null) {
				slot = st.copy();
				slot.stackSize = 0;
			}
			if(ItemIdentifier.get(slot) == itemIdent) {
				toadd = Math.min(slot.getMaxStackSize() - slot.stackSize, stack.stackSize - st.stackSize);
				if(toadd > 0) {
					st.stackSize += toadd;
					slot.stackSize += toadd;
					((IInventory)_tile).setInventorySlotContents(i, slot);
				}
			}
			if(stack.stackSize - st.stackSize == 0) return st;
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
		return getContents().makeNormalStack();
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(i != 0) return null;
		return getMultipleItems(getContents().getItem(), j);
	}
}
