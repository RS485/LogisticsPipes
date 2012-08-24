/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.krapht;

import java.util.HashMap;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class InventoryUtil {
	
	private final IInventory _inventory;
	private final boolean _hideOne;
	
	public InventoryUtil(IInventory inventory, boolean hideOne) {
		_inventory = inventory;
		_hideOne = hideOne;
	}
	
	public int itemCount(final ItemIdentifier item){
		int count = 0;
		for (int i = 0; i < _inventory.getSizeInventory(); i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			if (ItemIdentifier.get(stack) == item) {
				count += stack.stackSize - (_hideOne?1:0);
			}
		}
		return count;
	}
	
	public HashMap<ItemIdentifier, Integer> getItemsAndCount(){
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
	
	public int getItemCount(ItemIdentifier item){
		HashMap<ItemIdentifier, Integer> itemsAndCount = getItemsAndCount();
		if (!itemsAndCount.containsKey(item)){
			return 0;
		}
		return itemsAndCount.get(item);
	}
	
	public ItemStack getSingleItem(ItemIdentifier item){
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
		for (int i = 0; i < _inventory.getSizeInventory(); i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			if (ItemIdentifier.get(stack) == item) return true;
		}
		return false;
	}
	
	//Will not hide 1 item;
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

	public boolean hasRoomForItem(ItemIdentifier item) {
		return roomForItem(item) > 0;
	}
}
