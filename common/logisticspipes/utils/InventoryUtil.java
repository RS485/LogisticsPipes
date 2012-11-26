/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.HashMap;

import logisticspipes.proxy.SimpleServiceLocator;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class InventoryUtil {
	
	private final IInventory _inventory;
	private final boolean _hideOne;
	
	public InventoryUtil(IInventory inventory, boolean hideOne) {
		_inventory = inventory;
		_hideOne = hideOne;
	}
	
	public int itemCount(final ItemIdentifier item) {
		HashMap<ItemIdentifier, Integer> map = getItemsAndCount();
		if(map.containsKey(item)) {
			return map.get(item);
		}
		return 0;
	}
	
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		if(SimpleServiceLocator.specialinventory.isSpecialType(_inventory)) {
			return SimpleServiceLocator.specialinventory.getItemsAndCount(_inventory);
		} else {
			HashMap<ItemIdentifier, Integer> items = new HashMap<ItemIdentifier, Integer>();
			for (int i = 0; i < _inventory.getSizeInventory(); i++){
				ItemStack stack = _inventory.getStackInSlot(i);
				if (stack == null) continue;
				ItemIdentifier itemId = ItemIdentifier.get(stack);
				int stackSize = stack.stackSize - (_hideOne?1:0);
				if (!items.containsKey(itemId)){
					items.put(itemId, stackSize);
				} else {
					items.put(itemId, items.get(itemId) + stackSize);
				}
			}	
			return items;
		}
	}
	
	public ItemStack getSingleItem(ItemIdentifier item) {
		if(SimpleServiceLocator.specialinventory.isSpecialType(_inventory)) {
			return SimpleServiceLocator.specialinventory.getSingleItem(_inventory, item);
		} else {
			for (int i = 0; i < _inventory.getSizeInventory(); i++){
				ItemStack stack = _inventory.getStackInSlot(i);
				if (stack == null) continue;
				if (_hideOne && stack.stackSize == 1) continue;
				if (ItemIdentifier.get(stack) == item) {
					ItemStack removed = stack.splitStack(1);
					if (stack.stackSize == 0){
						_inventory.setInventorySlotContents(i,  null);
					}
					return removed;
				}
			}
			return null;
		}
	}
	
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
	
	//Will not hide 1 item;
	public boolean containsItem(ItemIdentifier item){
		if(SimpleServiceLocator.specialinventory.isSpecialType(_inventory)) {
			return SimpleServiceLocator.specialinventory.containsItem(_inventory, item);
		} else {
			for (int i = 0; i < _inventory.getSizeInventory(); i++){
				ItemStack stack = _inventory.getStackInSlot(i);
				if (stack == null) continue;
				if (ItemIdentifier.get(stack) == item) return true;
			}
			return false;
		}
	}
	
	//Will not hide 1 item;
	public int roomForItem(ItemIdentifier item){
		if(SimpleServiceLocator.specialinventory.isSpecialType(_inventory)) {
			return SimpleServiceLocator.specialinventory.roomForItem(_inventory, item);
		} else {
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
	}

	public boolean hasRoomForItem(ItemIdentifier item) {
		return roomForItem(item) > 0;
	}
}
