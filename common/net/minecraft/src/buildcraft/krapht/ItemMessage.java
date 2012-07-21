package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.krapht.ItemIdentifier;

public class ItemMessage {

	public int id = 0;
	public int data = 0;
	public int amount = 0;

	public ItemMessage() {}
	public ItemMessage(int id,int data,int amount) {
		this.id = id;
		this.data = data;
		this.amount = amount;
	}
	
	public String toString() {
		return amount + " " + ItemIdentifier.get(id,data).getFriendlyName();
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(id, data);
	}
}
