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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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

	private final static HashMap<ItemIdentifier, Integer> _itemIdentifierIdCache = new HashMap<ItemIdentifier, Integer>();

	private final static ArrayList<ItemIdentifier> _itemIdentifierTagCache = new ArrayList<ItemIdentifier>();
	
	private final static Table<Integer, Integer, ItemIdentifier> _itemIdentifierCache = HashBasedTable.create();
	
	private static ReadWriteLock lock = new ReentrantReadWriteLock();
	private static Lock readLock = lock.readLock();
	private static Lock writeLock = lock.writeLock();
	
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
		if(tag == null) {
			readLock.lock();
			if(_itemIdentifierCache.contains(itemID, itemUndamagableDamage)) {
				ItemIdentifier ident = _itemIdentifierCache.get(itemID, itemUndamagableDamage);
				readLock.unlock();
				return ident;
			}
			readLock.unlock();
			ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag);
			int id = getUnusedId();
			writeLock.lock();
			_itemIdentifierCache.put(itemID, itemUndamagableDamage, unknownItem);
			_itemIdentifierIdCache.put(unknownItem, id);
			writeLock.unlock();
			return(unknownItem);
		} else {
			readLock.lock();
			for(ItemIdentifier item : _itemIdentifierTagCache) {
				if(item.itemID == itemID && item.itemDamage == itemUndamagableDamage && tagsequal(item.tag, tag)){
					readLock.unlock();
					return item;
				}
			}
			readLock.unlock();
			ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, tag);
			int id = getUnusedId();
			writeLock.lock();
			_itemIdentifierTagCache.add(unknownItem);
			_itemIdentifierIdCache.put(unknownItem, id);
			writeLock.unlock();
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
		readLock.lock();
		for(ItemIdentifier item :_itemIdentifierIdCache.keySet()) {
			if(id == _itemIdentifierIdCache.get(item).intValue()) {
				readLock.unlock();
				return item;
			}
		}
		readLock.unlock();
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
		readLock.lock();
		for(Integer value : _itemIdentifierIdCache.values()) {
			if(id == value.intValue()) {
				readLock.unlock();
				return true;
			}
		}
		readLock.unlock();
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
		readLock.lock();
		int id = _itemIdentifierIdCache.get(this);
		readLock.unlock();
		return id;
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
		int i = 0;
		for(byte object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	private <T> Map<Integer, T> getListAsMap(List<T> array) {
		HashMap<Integer, T> map = new HashMap<Integer, T>();
		int i = 0;
		for(T object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
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
			int i = 0;
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
			int i = 0;
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
		return getFriendlyName();
	}
}