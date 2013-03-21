package logisticspipes.utils;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

public class ItemMessage {

	public int id = 0;
	public int data = 0;
	public int amount = 0;
	public NBTTagCompound tag;

	public ItemMessage() {}
	public ItemMessage(int id,int data,int amount, NBTTagCompound tag) {
		this.id = id;
		this.data = data;
		this.amount = amount;
		this.tag = tag;
	}
	
	public ItemMessage(ItemIdentifier selectedItem, int requestCount) {
		this(selectedItem.itemID,selectedItem.itemDamage,requestCount, selectedItem.tag);
	}
	
	public ItemMessage(ItemIdentifierStack selectedItemStack) {
		this(selectedItemStack.getItem(),selectedItemStack.stackSize);
	}
	
	@Override
	public String toString() {
		return amount + " " + ItemIdentifier.get(id,data,tag).getFriendlyName();
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(id, data,tag);
	}
	
	public static void compress(List<ItemMessage> input) {
		for(int i=0;i<input.size();i++) {
			ItemMessage one = input.get(i);
			for(int j=i+1;j<input.size();j++) {
				ItemMessage two = input.get(j);
				if(one.id == two.id && one.data == two.data && one.tag == two.tag) {
					one.amount += two.amount;
					input.remove(j--);
				}
			}
		}
	}
}
