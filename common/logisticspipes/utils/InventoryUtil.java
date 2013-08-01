/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.IInventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUtil implements IInventoryUtil {

	protected final IInventory _inventory;
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
		int count = 0;
		boolean first = true;
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null || ItemIdentifier.get(stack) != item) continue;
			if(first){
				count = stack.stackSize - ((_hideOne||_hideOnePerStack)?1:0);
				first = false;
			}
			else
				count +=stack.stackSize - (_hideOnePerStack?1:0);
		}
		return count;
	}
	
	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		Map<ItemIdentifier, Integer> items = new LinkedHashMap<ItemIdentifier, Integer>();
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.stackSize - (_hideOnePerStack?1:0);
			Integer currentSize = items.get(itemId);
			if (currentSize==null){
				items.put(itemId, stackSize - (_hideOne?1:0));
			} else {
				items.put(itemId, currentSize + stackSize);
			}
		}
		return items;
	}
	
	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> items = new TreeSet<ItemIdentifier>();
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) continue;
			items.add(ItemIdentifier.get(stack));
		}
		return items;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		return getMultipleItems(item,1);
	}
	
	@Override
	public ItemStack getMultipleItems(ItemIdentifier item, int count){
		if (itemCount(item) < count) return null;
		ItemStack outputStack = null;
		boolean first=true;
		
		for (int i = _cropStart; i < _inventory.getSizeInventory() - _cropEnd && count>0; i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null || (stack.stackSize == 1 && _hideOnePerStack) || ItemIdentifier.get(stack) != item) continue;
			int itemsToSplit = Math.min(count,stack.stackSize-((first && _hideOne || _hideOnePerStack)?1:0));
			first = false;
			if(itemsToSplit ==0 ) continue;
			ItemStack removed = null;
			if (stack.stackSize > itemsToSplit){ // then we only want part of the stack
				removed = stack.splitStack(itemsToSplit);
				_inventory.setInventorySlotContents(i,  stack);				
			} else {
				removed = stack;
				_inventory.setInventorySlotContents(i,  null);				
			}
			if(outputStack == null)
				outputStack = removed;
			else
				outputStack.stackSize += removed.stackSize;
			count-=removed.stackSize;
		}
		return outputStack;		
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
		return roomForItem(item, Integer.MAX_VALUE);
	}

	@Override
	public int roomForItem(ItemIdentifier item, int count){
		int totalRoom = 0;
		int stackLimit = _inventory.getInventoryStackLimit();
		for (int i = 0; i < _inventory.getSizeInventory() && count > totalRoom; i++){
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null){
				if(_inventory.isItemValidForSlot(i, item.unsafeMakeNormalStack(1))) {
					totalRoom += Math.min(stackLimit, item.getMaxStackSize());
				}
				continue;
			}
			if (ItemIdentifier.get(stack) != item) continue;
			
			totalRoom += (Math.min(stackLimit, item.getMaxStackSize()) - stack.stackSize);
		}
		return totalRoom;
	}

	@Override
	public boolean isSpecialInventory() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return _inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _inventory.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _inventory.decrStackSize(i, j);
	}
}
