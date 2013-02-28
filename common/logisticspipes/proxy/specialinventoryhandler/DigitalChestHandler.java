package logisticspipes.proxy.specialinventoryhandler;

import gregtechmod.api.IDigitalChest;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class DigitalChestHandler extends SpecialInventoryHandler {

	private final IDigitalChest _tile;

	private DigitalChestHandler(IDigitalChest tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = (IDigitalChest)tile;
	}

	public DigitalChestHandler() {
		_tile = null;
	}
	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return (tile instanceof IDigitalChest) && ((IDigitalChest)tile).isQuantumChest() && (((IInventory)tile).getSizeInventory() == 3);
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new DigitalChestHandler((IDigitalChest)tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		ItemIdentifierStack content = getContents();
		if(content == null) return 0;
		if(content.getItem() != itemIdent) return 0;
		return content.stackSize;
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
			map.put(content.getItem(), content.stackSize);
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
			wanted = Math.min(count, stack.stackSize);
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
}
