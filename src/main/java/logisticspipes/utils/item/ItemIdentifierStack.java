/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.utils.tuples.Triplet;
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

			return o2.getStackSize()-o1.getStackSize();
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
	private int stackSize;
	
	public static ItemIdentifierStack getFromStack(ItemStack stack){
		return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.stackSize);
	}
	
	public ItemIdentifierStack(ItemIdentifier item, int stackSize){
		_item = item;
		this.setStackSize(stackSize);
	}
	
	public ItemIdentifier getItem(){
		return _item;
	}

	/**
	 * @return the stackSize
	 */
	public int getStackSize() {
		return stackSize;
	}

	/**
	 * @param stackSize the stackSize to set
	 */
	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	public ItemStack unsafeMakeNormalStack(){
		ItemStack stack = new ItemStack(_item.itemID, this.getStackSize(), _item.itemDamage);
		stack.setTagCompound(_item.tag);
		return stack;
	}

	public ItemStack makeNormalStack(){
		ItemStack stack = new ItemStack(_item.itemID, this.getStackSize(), _item.itemDamage);
		if(_item.tag != null) {
			stack.setTagCompound((NBTTagCompound)_item.tag.copy());
		}
		return stack;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof ItemIdentifierStack) {
			ItemIdentifierStack stack = (ItemIdentifierStack)object;
			return stack._item.equals(this._item) && stack.getStackSize() == this.getStackSize();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return _item.hashCode() ^ (1023 * this.getStackSize());
	}

	@Override
	public String toString() {
		return new StringBuilder(Integer.toString(getStackSize())).append("x ").append(_item.toString()).toString();
	}
	
	@Override
	public ItemIdentifierStack clone() {
		return new ItemIdentifierStack(_item, getStackSize());
	}
	
	public String getFriendlyName() {
		return getStackSize() + " " + _item.getFriendlyName();
	}
	
	public void write(DataOutputStream data) throws IOException {
		data.writeInt(_item.itemID);
		data.writeInt(getStackSize());
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
				list.add(ItemIdentifierStack.getFromStack(inv.getStackInSlot(i)));
			}
		}
		return list;
	}

	public static LinkedList<ItemIdentifierStack> getListSendQueue(LinkedList<Triplet<IRoutedItem, ForgeDirection, ItemSendMode>> _sendQueue) {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for(Triplet<IRoutedItem, ForgeDirection, ItemSendMode> part:_sendQueue) {
			if(part == null) {
				list.add(null);
			} else {
				boolean added = false;
				for(ItemIdentifierStack stack:list) {
					if(stack.getItem().equals(ItemIdentifierStack.getFromStack(part.getValue1().getItemStack()).getItem())) {
						stack.setStackSize(stack.getStackSize() + part.getValue1().getItemStack().stackSize);
						added = true;
						break;
					}
				}
				if(!added) {
					list.add(ItemIdentifierStack.getFromStack(part.getValue1().getItemStack()));
				}
			}
		}
		return list;
	}

	@Override
	public int compareTo(ItemIdentifierStack o) {
		int c= _item.compareTo(o._item);
		if(c==0)
			return getStackSize()-o.getStackSize();
		return c;
	}
}
