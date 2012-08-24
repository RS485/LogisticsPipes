package logisticspipes.buildcraft.krapht;

import logisticspipes.krapht.ItemIdentifier;
import logisticspipes.krapht.ItemIdentifierStack;
import net.minecraft.src.NBTTagCompound;

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
	
	public String toString() {
		return amount + " " + ItemIdentifier.get(id,data,tag).getFriendlyName();
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(id, data,tag);
	}
}
