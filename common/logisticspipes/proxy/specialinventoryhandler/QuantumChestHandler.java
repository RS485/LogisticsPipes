package logisticspipes.proxy.specialinventoryhandler;

import gregtechmod.api.IQuantumChest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class QuantumChestHandler extends SpecialInventoryHandler {

	private final IQuantumChest _tile;

	private QuantumChestHandler(IQuantumChest tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
			_tile = (IQuantumChest)tile;
	}

	public QuantumChestHandler() {
		_tile = null;
	}
	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof IQuantumChest;
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new QuantumChestHandler((IQuantumChest)tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		ItemIdentifierStack content = getContents();
		if(content !=null)
			result.add(content.getItem());		
		return result;
	}
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		ItemIdentifierStack content = getContents();
		if(content !=null)
			map.put(content.getItem(), content.stackSize - 1);		
		return map;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return getMultipleItems(itemIdent,1);
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		ItemIdentifierStack contains = getContents();
		return itemIdent == contains.getItem();
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		ItemIdentifierStack contains = getContents();
		return itemIdent.getUndamaged() == contains.getItem().getUndamaged();
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		ItemIdentifierStack contains = getContents();
		return Integer.MAX_VALUE - 128 - (contains != null?contains.stackSize:0);
	}
	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) return null;
		int slot = ((ISidedInventory)_tile).getStartInventorySide(ForgeDirection.UP);
		System.out.print(slot);
		ItemStack stack = ((IInventory)_tile).decrStackSize(slot, Math.min(itemIdent.getMaxStackSize(),count));
		count -= stack.stackSize;
		while (count>0) {
			ItemStack newStack = ((IInventory)_tile).decrStackSize(slot, Math.min(itemIdent.getMaxStackSize(),count));
			if(newStack == null || newStack.stackSize == 0) {
				break;
			} else {
				stack.stackSize += newStack.stackSize;
				count -= newStack.stackSize;
			}
		}
		return stack;
	}	
	private ItemIdentifierStack getContents(){
		ItemStack[] data = _tile.getStoredItemData();
		if(data.length < 1 || data[0] == null || data[0].itemID < 1) return null;
		ItemStack stack = ((IInventory)_tile).getStackInSlot(1);
		if(stack == null || stack.itemID < 1) return null;
		ItemIdentifierStack dataIdent = ItemIdentifierStack.GetFromStack(data[0]);
		dataIdent.stackSize+=stack.stackSize;
		return dataIdent;
	}
}
