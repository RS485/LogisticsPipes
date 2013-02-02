/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.HashMap;

import logisticspipes.interfaces.IInventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUtil implements IInventoryUtil {

	private final IInventory _inventory;
	private final boolean _hideOnePerStack;
	private final boolean _hideOne;
	private final int _cropStart;
	private final int _cropEnd;
	
	public InventoryUtil(IInventory inventory, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_inventory = inventory;
		_hideOnePerStack = hideOnePerStack;
		_hideOne = hideOne;
		_cropStart = cropStart;
		_cropEnd = cropEnd;
	}
	
	@Override
	public int itemCount(ItemIdentifier item) {
		HashMap<ItemIdentifier, Integer> map = getItemsAndCount();
		if(map.containsKey(item)) {
			return map.get(item);
		}
		return 0;
	}
	
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> items = new HashMap<ItemIdentifier, Integer>();
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.stackSize - (_hideOnePerStack?1:0);
			if (!items.containsKey(itemId)){
				items.put(itemId, stackSize - (_hideOne?1:0));
			} else {
				items.put(itemId, items.get(itemId) + stackSize);
			}
		}
		return items;
	}
	
	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		//XXX this doesn't handle _hideOne ... does it have to?
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			if (stack.stackSize <= (_hideOnePerStack?1:0)) continue;
			if (ItemIdentifier.get(stack) == item) {
				ItemStack removed = stack.splitStack(1);
				if (stack.stackSize == 0) {
					_inventory.setInventorySlotContents(i,  null);
				} else {
					_inventory.setInventorySlotContents(i,  stack);
				}
				return removed;
			}
		}
		return null;
	}
	
	@Override
	public ItemStack getMultipleItems(ItemIdentifier item, int count){
		if (itemCount(item) < count) return null;
		ItemStack stack = null;
		for (int i = 0; i < count; i++){
			if(stack == null){
				stack = getSingleItem(item);
			}
			else{
				stack.stackSize += getSingleItem(item).stackSize;
			}
		}
		return stack;
	}
	
	//Ignores slot/item hiding
	@Override
	public boolean containsItem(ItemIdentifier item){
		for (int i = 0; i < _inventory.getSizeInventory(); i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			if (ItemIdentifier.get(stack) == item) return true;
		}
		return false;
	}
	
	//Ignores slot/item hiding
	@Override
	public boolean containsUndamagedItem(ItemIdentifier item){
		for (int i = 0; i < _inventory.getSizeInventory(); i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			if (ItemIdentifier.getUndamaged(stack) == item) return true;
		}
		return false;
	}

	//Ignores slot/item hiding
	@Override
	public int roomForItem(ItemIdentifier item){
		int totalRoom = 0;
		for (int i = 0; i < _inventory.getSizeInventory(); i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null){
				totalRoom += Math.min(_inventory.getInventoryStackLimit(), item.makeNormalStack(1).getMaxStackSize());
				continue;
			}
			if (ItemIdentifier.get(stack) != item) continue;
			
			totalRoom += (Math.min(_inventory.getInventoryStackLimit(), item.makeNormalStack(1).getMaxStackSize()) - stack.stackSize);
		}
		return totalRoom;
	}

	//Ignores slot/item hiding
	@Override
	public boolean hasRoomForItem(ItemIdentifier item) {
		return roomForItem(item) > 0;
	}
}
