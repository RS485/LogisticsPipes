/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public final class ItemIdentifierStack implements Comparable<ItemIdentifierStack>{
	public static class orderedComparitor implements Comparator<ItemIdentifierStack>{
		@Override
		public int compare(ItemIdentifierStack o1, ItemIdentifierStack o2) {
			int c=o1._item.itemID-o2._item.itemID;
			if(c!=0) return c;
			c=o1._item.itemDamage-o2._item.itemDamage;
			if(c!=0) return c;
			c=o1._item.uniqueID-o2._item.uniqueID;
			if(c!=0) return c;

			return o2.stackSize-o1.stackSize;
		}		
	}
	public static class itemComparitor implements Comparator<ItemIdentifierStack>{

		@Override
		public int compare(ItemIdentifierStack o1, ItemIdentifierStack o2) {
			int c=o1._item.itemID-o2._item.itemID;
			if(c!=0) return c;
			c=o1._item.itemDamage-o2._item.itemDamage;
			if(c!=0) return c;
			return o1._item.uniqueID-o2._item.uniqueID;
		}
		
	}
	public static class simpleItemComparitor implements Comparator<ItemIdentifierStack>{

		@Override
		public int compare(ItemIdentifierStack o1, ItemIdentifierStack o2) {
			int c=o1._item.itemID-o2._item.itemID;
			if(c!=0) return c;
			return o1._item.itemDamage-o2._item.itemDamage;
		}
		
	}
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
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof ItemIdentifierStack) {
			ItemIdentifierStack stack = (ItemIdentifierStack)object;
			return stack._item.equals(this._item) && stack.stackSize == this.stackSize;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return _item.hashCode() ^ (1023 * this.stackSize);
	}

	@Override
	public String toString() {
		return new StringBuilder(Integer.toString(stackSize)).append("x").append(_item.toString()).toString();
	}
	
	@Override
	public ItemIdentifierStack clone() {
		return new ItemIdentifierStack(_item, stackSize);
	}
	
	public String getFriendlyName() {
		return stackSize + " " + _item.getFriendlyName();
	}
	
	public void write(DataOutputStream data) throws IOException {
		data.writeInt(_item.itemID);
		data.writeInt(stackSize);
		data.writeInt(_item.itemDamage);
		SendNBTTagCompound.writeNBTTagCompound(_item.tag, data);
	}
	
	public static ItemIdentifierStack read(DataInputStream data) throws IOException {
		int itemID = data.readInt();
		int stacksize = data.readInt();
		int damage = data.readInt();
		NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
		return new ItemIdentifierStack(ItemIdentifier.get(itemID, damage, tag), stacksize);
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
