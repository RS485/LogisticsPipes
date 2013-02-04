/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public final class ItemIdentifierStack implements Comparable<ItemIdentifierStack>{
	private final ItemIdentifier _item;
	public int stackSize;
	
	public static ItemIdentifierStack GetFromStack(ItemStack stack){
		return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.stackSize);
	}
	
	public ItemIdentifierStack(ItemIdentifier item, int stackSize){
		_item = item;
		this.stackSize = stackSize;
	}
	
	public ItemIdentifier getItem(){
		return _item;
	}

	public ItemStack unsafeMakeNormalStack(){
		ItemStack stack = new ItemStack(_item.itemID, this.stackSize, _item.itemDamage);
		stack.setTagCompound(_item.tag);
		return stack;
	}

	public ItemStack makeNormalStack(){
		ItemStack stack = new ItemStack(_item.itemID, this.stackSize, _item.itemDamage);
		if(_item.tag != null) {
			stack.setTagCompound((NBTTagCompound)_item.tag.copy());
		}
		return stack;
	}
	
	public boolean equals(Object object) {
		if(object instanceof ItemIdentifierStack) {
			ItemIdentifierStack stack = (ItemIdentifierStack)object;
			return stack._item.equals(this._item) && stack.stackSize == this.stackSize;
		}
		return false;
	}
	
	public String toString() {
		return new StringBuilder(Integer.toString(stackSize)).append("x").append(_item.toString()).toString();
	}
	
	public ItemIdentifierStack clone() {
		return new ItemIdentifierStack(_item, stackSize);
	}
	
	public static LinkedList<ItemIdentifierStack> getListFromInventory(IInventory inv) {
		return getListFromInventory(inv, false);
	}
		
	public static LinkedList<ItemIdentifierStack> getListFromInventory(IInventory inv, boolean removeNull) {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for(int i=0;i<inv.getSizeInventory();i++) {
			if(inv.getStackInSlot(i) == null) {
				if(!removeNull) {
					list.add(null);
				}
			} else {
				list.add(ItemIdentifierStack.GetFromStack(inv.getStackInSlot(i)));
			}
		}
		return list;
	}

	public static LinkedList<ItemIdentifierStack> getListSendQueue(LinkedList<Pair3<IRoutedItem, ForgeDirection, ItemSendMode>> _sendQueue) {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for(Pair3<IRoutedItem, ForgeDirection, ItemSendMode> part:_sendQueue) {
			if(part == null) {
				list.add(null);
			} else {
				boolean added = false;
				for(ItemIdentifierStack stack:list) {
					if(stack.getItem().equals(ItemIdentifierStack.GetFromStack(part.getValue1().getItemStack()).getItem())) {
						stack.stackSize += part.getValue1().getItemStack().stackSize;
						added = true;
						break;
					}
				}
				if(!added) {
					list.add(ItemIdentifierStack.GetFromStack(part.getValue1().getItemStack()));
				}
			}
		}
		return list;
	}

	@Override
	public int compareTo(ItemIdentifierStack o) {
		int c= _item.compareTo(o._item);
		if(c==0)
			return stackSize-o.stackSize;
		return c;
	}
}
