/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;

import logisticspipes.proxy.MainProxy;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagByte;
import net.minecraft.src.NBTTagByteArray;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagDouble;
import net.minecraft.src.NBTTagFloat;
import net.minecraft.src.NBTTagInt;
import net.minecraft.src.NBTTagIntArray;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagLong;
import net.minecraft.src.NBTTagShort;
import net.minecraft.src.NBTTagString;

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

	private final static LinkedHashMap<ItemIdentifier, Integer> _itemIdentifierCacheServer = new LinkedHashMap<ItemIdentifier, Integer>();
	private final static LinkedHashMap<ItemIdentifier, Integer> _itemIdentifierCacheClient = new LinkedHashMap<ItemIdentifier, Integer>();
	
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
		for(ItemIdentifier item : MainProxy.isClient() ? _itemIdentifierCacheClient.keySet() : _itemIdentifierCacheServer.keySet()) {
			if(item.itemID == itemID && item.itemDamage == itemUndamagableDamage && tagsequal(item.tag, tag)){
				return item;
			}
		}
		ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag);
		int id = getUnusedId();
		(MainProxy.isClient() ? _itemIdentifierCacheClient : _itemIdentifierCacheServer).put(unknownItem, id);
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
	
	public static ItemIdentifier getForId(int id) {
		for(ItemIdentifier item : MainProxy.isClient() ? _itemIdentifierCacheClient.keySet() : _itemIdentifierCacheServer.keySet()) {
			if(id == (MainProxy.isClient() ? _itemIdentifierCacheClient : _itemIdentifierCacheServer).get(item).intValue()) {
				return item;
			}
		}
		return null;
	}
	
	private static int getUnusedId() {
		int id = new Random().nextInt();
		while(isIdUsed(id)) {
			id = new Random().nextInt();
		}
		return id;
	}
	
	private static boolean isIdUsed(int id) {
		for(Integer value : MainProxy.isClient() ? _itemIdentifierCacheClient.values() : _itemIdentifierCacheServer.values()) {
			if(id == value.intValue()) {
				return true;
			}
		}
		return false;
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
	
	public boolean isValid() {
		return Item.itemsList[itemID] != null;
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
	
	public int getId() {
		return (MainProxy.isClient() ? _itemIdentifierCacheClient : _itemIdentifierCacheServer).get(this);
	}
	
	public String getNBTTagCompoundName() {
		if(tag != null) {
			return tag.getName();
		} else {
			return null;
		}
	}
	
	public Object[] getNBTTagCompoundAsObject() {
		if(tag != null) {
			try {
				return getNBTBaseAsObject(tag);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	private Object[] getNBTBaseAsObject(NBTBase nbt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(nbt == null) {
			return null;
		}
		if(nbt instanceof NBTTagByte) {
			return new Object[]{nbt.getName(), ((NBTTagByte)nbt).data};
		} else if(nbt instanceof NBTTagByteArray) {
			return new Object[]{nbt.getName(), ((NBTTagByteArray)nbt).byteArray};
		} else if(nbt instanceof NBTTagDouble) {
			return new Object[]{nbt.getName(), ((NBTTagDouble)nbt).data};
		} else if(nbt instanceof NBTTagFloat) {
			return new Object[]{nbt.getName(), ((NBTTagFloat)nbt).data};
		} else if(nbt instanceof NBTTagInt) {
			return new Object[]{nbt.getName(), ((NBTTagInt)nbt).data};
		} else if(nbt instanceof NBTTagIntArray) {
			return new Object[]{nbt.getName(), ((NBTTagIntArray)nbt).intArray};
		} else if(nbt instanceof NBTTagList) {
			LinkedList<Object> list = new LinkedList<Object>();
			ArrayList internal = new ArrayList();
			Field fList;
			try {
				fList = NBTTagList.class.getDeclaredField("tagList");
			} catch(Exception e) {
				fList = NBTTagList.class.getDeclaredField("a");
			}
			fList.setAccessible(true);
			internal = (ArrayList) fList.get(nbt);
			list.add("{");
			list.add(nbt.getName());
			for(Object object:internal) {
				if(object instanceof NBTBase) {
					list.add("[");
					list.addAll(Arrays.asList(getNBTBaseAsObject((NBTBase)object)));
					list.add("]");
				}
			}
			list.add("}");
			return list.toArray();
		} else if(nbt instanceof NBTTagCompound) {
			LinkedList<Object> list = new LinkedList<Object>();
			HashMap internal = new HashMap();
			Field fMap;
			try {
				fMap = NBTTagCompound.class.getDeclaredField("tagMap");
			} catch(Exception e) {
				fMap = NBTTagCompound.class.getDeclaredField("a");
			}
			fMap.setAccessible(true);
			internal = (HashMap) fMap.get(nbt);
			list.add("{");
			list.add(nbt.getName());
			for(Object object:internal.values()) {
				if(object instanceof NBTBase) {
					list.add("[");
					list.addAll(Arrays.asList(getNBTBaseAsObject((NBTBase)object)));
					list.add("]");
				}
			}
			list.add("}");
			return list.toArray();
		} else if(nbt instanceof NBTTagLong) {
			return new Object[]{nbt.getName(), ((NBTTagLong)nbt).data};
		} else if(nbt instanceof NBTTagShort) {
			return new Object[]{nbt.getName(), ((NBTTagShort)nbt).data};
		} else if(nbt instanceof NBTTagString) {
			return new Object[]{nbt.getName(), ((NBTTagString)nbt).data};
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
	
	public String toString() {
		return getFriendlyName();
	}
}