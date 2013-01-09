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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import logisticspipes.proxy.MainProxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.ItemData;

/**
 * @author Krapht
 * 
 * I have no bloody clue what different mods use to differate between items except for itemID, 
 * there is metadata, damage, and whatnot. so..... to avoid having to change all my bloody code every 
 * time I need to support a new item flag that would make it a "different" item, I made this cache here
 * 
 *  A ItemIdentifier is immutable, singleton and most importantly UNIQUE!
 */
public final class ItemIdentifier implements Comparable<ItemIdentifier> {
	
	private static class ItemKey implements Comparable<ItemKey>{
		public ItemKey(int id, int d){ itemID=id;itemDamage=d;}
		public int itemID;
		public int itemDamage;
		@Override 
		public boolean equals(Object that){
			if (!(that instanceof ItemKey))
				return false;
			ItemKey i = (ItemKey)that;
			return this.itemID== i.itemID && this.itemDamage == i.itemDamage;
			
		}
		
		@Override public int hashCode(){
			//1000001 chosen because 1048576 is 2^20, moving the bits for the item ID to the top of the integer
			// not exactly 2^20 was chosen so that when the has is used mod power 2, there arn't repeated collisions on things with the same damage id.
			return ((itemID)*1000001)+itemDamage;
		}
		@Override
		public int compareTo(ItemKey o) {
			if(itemID==o.itemID)
				return itemDamage-o.itemDamage;
			return itemID-o.itemID;
		}
	}

	private final static ConcurrentHashMap<Integer, ItemIdentifier> _itemIdentifierIdCache = new ConcurrentHashMap< Integer, ItemIdentifier>();

	private final static ConcurrentSkipListSet<ItemIdentifier> _itemIdentifierTagCache = new ConcurrentSkipListSet<ItemIdentifier>();
	
	private final static ConcurrentHashMap<ItemKey, ItemIdentifier> _itemIdentifierCache = new ConcurrentHashMap<ItemKey, ItemIdentifier>();
	
	private final static ConcurrentHashMap<Integer, String> _modItemIdMap = new ConcurrentHashMap<Integer, String>();
	private final static ConcurrentSkipListSet<String> _modList = new ConcurrentSkipListSet<String>();
	
	private static boolean init = false;
	
	//Hide default constructor
	private ItemIdentifier(int itemID, int itemDamage, NBTTagCompound tag, int uniqueID) {
		this.itemID =  itemID;
		this.itemDamage = itemDamage;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}
	
	public final int itemID;
	public final int itemDamage;
	public final NBTTagCompound tag;
	public final int uniqueID;
	
	public static boolean allowNullsForTesting;
	
	public static ItemIdentifier get(int itemID, int itemUndamagableDamage, NBTTagCompound tag)	{
		if(tag == null) {
			ItemKey itemKey = new ItemKey(itemID, itemUndamagableDamage);
			ItemIdentifier unknownItem = _itemIdentifierCache.get(itemKey);
			if(unknownItem != null) {
				return unknownItem;
			}
			int id = getUnusedId();
			unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag, id);
			_itemIdentifierCache.put(itemKey, unknownItem);
			_itemIdentifierIdCache.put(id, unknownItem);
			return(unknownItem);
		} else {
			for(ItemIdentifier item : _itemIdentifierTagCache) {
				if(item.itemID == itemID && item.itemDamage == itemUndamagableDamage && tagsequal(item.tag, tag)){
					return item;
				}
			}
			int id = getUnusedId();
			ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag, id);
			_itemIdentifierTagCache.add(unknownItem);
			_itemIdentifierIdCache.put(id, unknownItem);
			return(unknownItem);
		}
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
		return _itemIdentifierIdCache.get(id);
	}
	
	private static int getUnusedId() {
		int id = new Random().nextInt();
		while(isIdUsed(id)) {
			id = new Random().nextInt();
		}
		return id;
	}
	
	private static boolean isIdUsed(int id) {
		return _itemIdentifierIdCache.containsKey(id);
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

	public static void tick() {
		if(init) return;
		init = true;
		NBTTagList list = new NBTTagList();
		GameData.writeItemData(list);
		Set<ItemData> set = GameData.buildWorldItemData(list);
		for(ItemData data:set) {
			_modItemIdMap.put(data.getItemId(), data.getModId());
			if(!_modList.contains(data.getModId())) {
				_modList.add(data.getModId());
			}
		}
	}
	
	/* Instance Methods */
	
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
	
	public String getFriendlyNameCC() {
		if (Item.itemsList[itemID] != null) {
			return MainProxy.proxy.getName(this);
		}
		return "<Item name not found>";
	}
	
	public String getModId() {
		String name = "UNKNOWN";
		if(_modItemIdMap.containsKey(this.itemID)) {
			name = _modItemIdMap.get(this.itemID);
		}
		return name;
	}
	
	public ItemIdentifierStack makeStack(int stackSize){
		return new ItemIdentifierStack(this, stackSize);
	}
	
	public ItemStack makeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.itemID, stackSize, this.itemDamage);
		stack.setTagCompound(this.tag);
		return stack;
	}
	
	public int getMaxStackSize() {
		if(Item.itemsList[this.itemID].isDamageable() && this.itemDamage > 0) {
			return 1;
		}
		int limit = Item.itemsList[this.itemID].getItemStackLimit();
		return limit < 64 ? limit : 64;
	}
	
	public boolean fuzzyMatch(ItemStack stack) {
		if(stack.itemID != this.itemID) return false;
		if(!Item.itemsList[this.itemID].isDamageable()) {
			if(this.itemDamage != stack.getItemDamage()) return false;
		}
		return true;
	}
	
	public int getId() {
		return uniqueID;
	}
	
	public String getNBTTagCompoundName() {
		if(tag != null) {
			return tag.getName();
		} else {
			return null;
		}
	}
	
	public Map<Object, Object> getNBTTagCompoundAsMap() {
		if(tag != null) {
			try {
				return getNBTBaseAsMap(tag);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	private Map<Integer, Object> getArrayAsMap(int[] array) {
		HashMap<Integer, Object> map = new HashMap<Integer, Object>();
		int i = 0;
		for(int object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	private Map<Integer, Object> getArrayAsMap(byte[] array) {
		HashMap<Integer, Object> map = new HashMap<Integer, Object>();
		int i = 1;
		for(byte object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	@SuppressWarnings("unused")
	private <T> Map<Integer, T> getListAsMap(List<T> array) {
		HashMap<Integer, T> map = new HashMap<Integer, T>();
		int i = 1;
		for(T object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private Map<Object, Object> getNBTBaseAsMap(NBTBase nbt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(nbt == null) {
			return null;
		}
		if(nbt instanceof NBTTagByte) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagByte");
			map.put("value", ((NBTTagByte)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagByteArray) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagByteArray");
			map.put("value", getArrayAsMap(((NBTTagByteArray)nbt).byteArray));
			return map;
		} else if(nbt instanceof NBTTagDouble) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagDouble");
			map.put("value", ((NBTTagDouble)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagFloat) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagFloat");
			map.put("value", ((NBTTagFloat)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagInt) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagInt");
			map.put("value", ((NBTTagInt)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagIntArray) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagIntArray");
			map.put("value", getArrayAsMap(((NBTTagIntArray)nbt).intArray));
			return map;
		} else if(nbt instanceof NBTTagList) {
			ArrayList internal = new ArrayList();
			Field fList;
			try {
				fList = NBTTagList.class.getDeclaredField("tagList");
			} catch(Exception e) {
				fList = NBTTagList.class.getDeclaredField("a");
			}
			fList.setAccessible(true);
			internal = (ArrayList) fList.get(nbt);
			
			HashMap<Integer, Object> content = new HashMap<Integer, Object>();
			int i = 1;
			for(Object object:internal) {
				if(object instanceof NBTBase) {
					content.put(i, getNBTBaseAsMap((NBTBase)object));
				}
				i++;
			}
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagList");
			map.put("value", content);
			return map;
		} else if(nbt instanceof NBTTagCompound) {
			HashMap internal = new HashMap();
			Field fMap;
			try {
				fMap = NBTTagCompound.class.getDeclaredField("tagMap");
			} catch(Exception e) {
				fMap = NBTTagCompound.class.getDeclaredField("a");
			}
			fMap.setAccessible(true);
			internal = (HashMap) fMap.get(nbt);
			HashMap<Object, Object> content = new HashMap<Object, Object>();
			HashMap<Integer, Object> keys = new HashMap<Integer, Object>();
			int i = 1;
			for(Object object:internal.keySet()) {
				if(internal.get(object) instanceof NBTBase) {
					content.put(object, getNBTBaseAsMap((NBTBase)internal.get(object)));
					keys.put(i, object);
				}
				i++;
			}
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagCompound");
			map.put("value", content);
			map.put("keys", keys);
			return map;
		} else if(nbt instanceof NBTTagLong) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagLong");
			map.put("value", ((NBTTagLong)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagShort) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("name", nbt.getName());
			map.put("type", "NBTTagShort");
			map.put("value", ((NBTTagShort)nbt).data);
			return map;
		} else if(nbt instanceof NBTTagString) {
			HashMap<Object, Object> map = new HashMap<Object, Object>(); 
			map.put("name", nbt.getName());
			map.put("type", "NBTTagString");
			map.put("value", ((NBTTagString)nbt).data);
			return map;
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
	
	public String toString() {
		return getModId() + ":" + getFriendlyName();
	}

	@Override
	public int compareTo(ItemIdentifier o) {
		if(uniqueID<o.uniqueID)
			return -1;
		if(uniqueID>o.uniqueID)
			return 1;
		return 0;
	}
	
	public boolean equals(Object that){
		if (!(that instanceof ItemIdentifier))
			return false;
		ItemIdentifier i = (ItemIdentifier)that;
		return this.uniqueID==i.uniqueID;
		
	}
	
	@Override public int hashCode(){
		return uniqueID;
	}
}
