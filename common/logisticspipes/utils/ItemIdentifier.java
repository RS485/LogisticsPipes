/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;

import javax.swing.text.html.parser.TagElement;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

/**
 * @author Krapht
 * 
 * I have no bloody clue what different mods use to differate between items except for itemID, 
 * there is metadata, damage, and whatnot. so..... to avoid having to change all my bloody code every 
 * time I need to support a new item flag that would make it a "different" item, I made this cache here
 * 
 *  A ItemIdentifier is immutable, singleton and most importantly UNIQUE!
 */
public final class ItemIdentifier {

	private final static LinkedList<ItemIdentifier> _itemIdentifierCache = new LinkedList<ItemIdentifier>();
	
	//Hide default constructor
	private ItemIdentifier(int itemID, int itemDamage, NBTTagCompound tag) {
		this.itemID =  itemID;
		this.itemDamage = itemDamage;
		this.tag = tag;
	}
	
	public final int itemID;
	public final int itemDamage;
	public final NBTTagCompound tag;
	
	public static boolean allowNullsForTesting;
	
	public static ItemIdentifier get(int itemID, int itemUndamagableDamage, NBTTagCompound tag)	{
		for(ItemIdentifier item : _itemIdentifierCache)	{
			if(item.itemID == itemID && item.itemDamage == itemUndamagableDamage && tagsequal(item.tag, tag)){
				return item;
			}
		}
		ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag); 
		_itemIdentifierCache.add(unknownItem);
		return(unknownItem);
	}
	
	public static ItemIdentifier get(ItemStack itemStack) {
		if (itemStack == null && allowNullsForTesting){
			return null;
		}
		int itemDamage = 0;
		if (!Item.itemsList[itemStack.itemID].isDamageable()) {
			itemDamage = itemStack.getItemDamage();
		}
		return get(itemStack.itemID, itemDamage, itemStack.stackTagCompound);
	}
	
	private static boolean tagsequal(NBTTagCompound tag1, NBTTagCompound tag2) {
		if(tag1 == null && tag2 == null) {
			return true;
		}
		if(tag1 == null) {
			return false;
		}
		if(tag2 == null) {
			return false;
		}
		return tag1.equals(tag2);
	}
	
	public String getDebugName() {
		if (Item.itemsList[itemID] != null)	{
			return Item.itemsList[itemID].getItemName() + "(ID: " + itemID + ", Damage: " + itemDamage + ")";
		}
		return "<item not found>";
	}
	
	private String getName(int id,ItemStack stack) {
		String name = "???";
		try {
			name = Item.itemsList[id].getItemDisplayName(stack);
			if(name == null) {
				throw new Exception();
			}
		} catch(Exception e) {
			try {
				name = Item.itemsList[id].getItemNameIS(stack);
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = Item.itemsList[id].getItemName();
					if(name == null) {
						throw new Exception();
					}
				} catch(Exception e2) {
					name = "???"; 
				}
			}
		}
		return name;

	}
	
	public String getFriendlyName() {
		if (Item.itemsList[itemID] != null) {
			return getName(itemID,this.makeNormalStack(1));
		}
		return "<Item name not found>";
	}
	
	public ItemIdentifierStack makeStack(int stackSize){
		return new ItemIdentifierStack(this, stackSize);
	}
	
	public ItemStack makeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.itemID, stackSize, this.itemDamage);
		stack.setTagCompound(this.tag);
		return stack;
	}
}