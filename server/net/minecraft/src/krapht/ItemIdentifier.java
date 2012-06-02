/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht;

import java.util.LinkedList;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

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

	//TODO: Change internal storage to a static matrix [itemid,itemdamage] to optimize search times, or some other hashed collection.
	private final static LinkedList<ItemIdentifier> _itemIdentifierCache = new LinkedList<ItemIdentifier>();
	
	 //Hide default constructor
	private ItemIdentifier(int itemID, int itemDamage) {
		this.itemID =  itemID;
		this.itemDamage = itemDamage;
	}
	
	public final int itemID;
	public final int itemDamage;
	
	public static boolean allowNullsForTesting;
	
	public static ItemIdentifier get(int itemID, int itemUndamagableDamage)	{
		for(ItemIdentifier item : _itemIdentifierCache)	{
			if(item.itemID == itemID && item.itemDamage == itemUndamagableDamage){
				return item;
			}
		}
		ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage); 
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
		return get(itemStack.itemID, itemDamage);
	}
	
	public String getDebugName() {
		if (Item.itemsList[itemID] != null)	{
			return Item.itemsList[itemID].getItemName() + "(ID: " + itemID + ", Damage: " + itemDamage + ")";
		}
		return "<item not found>";
	}
	
	/*public String getFriendlyName() {
		if (Item.itemsList[itemID]!= null) {
			return Item.itemsList[itemID].getItemDisplayName(this.makeNormalStack(1));
			//return Item.itemsList[itemID].func_40397_d(this.makeNormalStack(1));
		}
		return "<Item name not found>";
	}*/
	
	public ItemIdentifierStack makeStack(int stackSize){
		return new ItemIdentifierStack(this, stackSize);
	}
	
	public ItemStack makeNormalStack(int stackSize){
		return new ItemStack(this.itemID, stackSize, this.itemDamage);
	}
}