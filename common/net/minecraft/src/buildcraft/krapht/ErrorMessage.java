package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.krapht.ItemIdentifier;

public class ErrorMessage {

	public int id = 0;
	public int data = 0;
	public int amount = 0;
	public NBTTagCompound tag;

	public ErrorMessage() {}
	public ErrorMessage(int id,int data,int amount, NBTTagCompound tag) {
		this.id = id;
		this.data = data;
		this.amount = amount;
		this.tag = tag;
	}
	
	public String toString() {
		return amount + " " + ItemIdentifier.get(id,data,tag).getFriendlyName();
	}
	
	public ItemIdentifier getItemIdentifier() {
		return ItemIdentifier.get(id, data,tag);
	}
}
