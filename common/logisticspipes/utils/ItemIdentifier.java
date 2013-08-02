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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ObfuscationHelper.NAMES;
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
		public final int itemID;
		public final int itemDamage;
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

	private final static ConcurrentHashMap<Integer, ItemIdentifier> _itemIdentifierIdCache = new ConcurrentHashMap< Integer, ItemIdentifier>(4096, 0.5f, 1);

	// for when things differ by NBT tags, and an itemKey isn't enough to get the full object
	private final static ConcurrentHashMap<ItemKey, ConcurrentHashMap<FinalNBTTagCompound,ItemIdentifier>> _itemIdentifierTagCache = new ConcurrentHashMap<ItemKey, ConcurrentHashMap<FinalNBTTagCompound,ItemIdentifier>>(1024, 0.5f, 1);
	
	private final static ConcurrentHashMap<ItemKey, ItemIdentifier> _itemIdentifierCache = new ConcurrentHashMap<ItemKey, ItemIdentifier>(4096, 0.5f, 1);
	
	//array of mod names, used for id -> name, 0 is unknown
	private final static ArrayList<String> _modNameList = new ArrayList<String>();
	//map of mod name -> internal LP modid, first modname gets 1
	private final static Map<String, Integer> _modNameToModIdMap = new HashMap<String, Integer>();
	//map of itemid -> modid
	private final static int _modItemIdMap[] = new int[32768];
	
	private static boolean init = false;
	
	//Hide default constructor
	private ItemIdentifier(int itemID, int itemDamage, FinalNBTTagCompound tag, int uniqueID) {
		this.itemID =  itemID;
		this.itemDamage = itemDamage;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}
	
	public final int itemID;
	public final int itemDamage;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;
	
	private ItemIdentifier _IDIgnoringNBT=null;
	private ItemIdentifier _IDIgnoringDamage=null;

	public static boolean allowNullsForTesting;
	
	public static ItemIdentifier get(int itemID, int itemUndamagableDamage, NBTTagCompound tag)	{
		ItemKey itemKey = new ItemKey(itemID, itemUndamagableDamage);
		if(tag == null) {
			ItemIdentifier unknownItem = _itemIdentifierCache.get(itemKey);
			if(unknownItem != null) {
				return unknownItem;
			}
			int id = getUnusedId();
			unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, null, id);
			_itemIdentifierCache.put(itemKey, unknownItem);
			_itemIdentifierIdCache.put(id, unknownItem);
			return(unknownItem);
		} else {
			ConcurrentHashMap<FinalNBTTagCompound, ItemIdentifier> itemNBTList = _itemIdentifierTagCache.get(itemKey);
			FinalNBTTagCompound tagwithfixedname = new FinalNBTTagCompound(tag);
			if(itemNBTList!=null){
				ItemIdentifier unknownItem = itemNBTList.get(tagwithfixedname);
				if(unknownItem!=null) {
					return unknownItem;
				}
			} else {
				itemNBTList = new ConcurrentHashMap<FinalNBTTagCompound, ItemIdentifier>(16, 0.5f, 1);
				_itemIdentifierTagCache.put(itemKey, itemNBTList);
			}
			FinalNBTTagCompound finaltag = new FinalNBTTagCompound((NBTTagCompound)tag.copy());
			ItemIdentifier unknownItem = new ItemIdentifier(itemID, itemUndamagableDamage, finaltag, getUnusedId());
			checkNBTbadness(unknownItem, tag);
			itemNBTList.put(finaltag,unknownItem);
			_itemIdentifierIdCache.put(unknownItem.uniqueID, unknownItem);
			return(unknownItem);
		}
	}
	
	public static ItemIdentifier get(ItemStack itemStack) {
		if (itemStack == null && allowNullsForTesting){
			return null;
		}
		return get(itemStack.itemID, itemStack.getItemDamage(), itemStack.stackTagCompound);
	}
	
	public static ItemIdentifier getUndamaged(ItemStack itemStack) {
		if (itemStack == null && allowNullsForTesting){
			return null;
		}
		int itemDamage = 0;
		if (!Item.itemsList[itemStack.itemID].isDamageable()) {
			itemDamage = itemStack.getItemDamage();
		}
		return get(itemStack.itemID, itemDamage, itemStack.stackTagCompound);
	}

	public ItemIdentifier getUndamaged() {
		if(_IDIgnoringDamage==null){
			if (!Item.itemsList[this.itemID].isDamageable()) {
				_IDIgnoringDamage = this;
			} else {
				_IDIgnoringDamage = get(this.itemID, 0, this.tag);
			}
		}
		return _IDIgnoringDamage;
	}

	public static ItemIdentifier getIgnoringNBT(ItemStack itemStack) {
		return get(itemStack.itemID, itemStack.getItemDamage(), null);
	}
	public ItemIdentifier getIgnoringNBT() {
		if(this._IDIgnoringNBT==null){
			_IDIgnoringNBT= get(itemID, itemDamage, null);			
		}
		return _IDIgnoringNBT;
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
	/*
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
	*/

	public static void tick() {
		if(init) return;
		synchronized(_modItemIdMap) {
			if(init) return;
			init = true;
			_modNameToModIdMap.put("UNKNOWN", 0);
			_modNameList.add("UNKNOWN");
			NBTTagList list = new NBTTagList();
			GameData.writeItemData(list);
			Set<ItemData> set = GameData.buildWorldItemData(list);
			for(ItemData data:set) {
				String modname = data.getModId();
				if(modname == null)
					continue;
				Integer modid = _modNameToModIdMap.get(modname);
				if(modid==null){
					modid = _modNameList.size();
					_modNameList.add(modname);
					_modNameToModIdMap.put(modname, modid);
				}
				_modItemIdMap[data.getItemId()] = modid;
			}
			_modNameList.ensureCapacity(_modNameToModIdMap.size());
		}
	}
	
	/* Instance Methods */
	
	public String getDebugName() {
		if (Item.itemsList[itemID] != null)	{
			return Item.itemsList[itemID].getUnlocalizedName() + "(ID: " + itemID + ", Damage: " + itemDamage + ")";
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
				name = Item.itemsList[id].getUnlocalizedName(stack);
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = Item.itemsList[id].getUnlocalizedName();
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
			return getName(itemID,this.unsafeMakeNormalStack(1));
		}
		return "<Item name not found>";
	}
	
	public String getFriendlyNameCC() {
		if (Item.itemsList[itemID] != null) {
			return MainProxy.proxy.getName(this);
		}
		return "<Item name not found>";
	}
	
	public int getModId() {
		return _modItemIdMap[this.itemID];
	}

	public String getModName() {
		return _modNameList.get(_modItemIdMap[this.itemID]);
	}
	
	public static int getModIdForName(String modname) {
		Integer m =  _modNameToModIdMap.get(modname);
		if(m==null) {
			return 0;
		}
		return m;
	}

	public ItemIdentifierStack makeStack(int stackSize){
		return new ItemIdentifierStack(this, stackSize);
	}
	
	public ItemStack unsafeMakeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.itemID, stackSize, this.itemDamage);
		stack.setTagCompound(this.tag);
		return stack;
	}

	public ItemStack makeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.itemID, stackSize, this.itemDamage);
		if(this.tag != null) {
			stack.setTagCompound((NBTTagCompound)this.tag.copy());
		}
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
		if(stack.getItemDamage() != this.itemDamage) return false;
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
			Field fList = ObfuscationHelper.getDeclaredField(NAMES.tagList);
			fList.setAccessible(true);
			List internal = (List) fList.get(nbt);
			
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
			Field fMap = ObfuscationHelper.getDeclaredField(NAMES.tagMap);
			fMap.setAccessible(true);
			internal = (HashMap) fMap.get(nbt);
			HashMap<Object, Object> content = new HashMap<Object, Object>();
			HashMap<Integer, Object> keys = new HashMap<Integer, Object>();
			int i = 1;
			for(Object object:internal.entrySet()) {
				Entry e = (Entry)object;
				if(e.getValue() instanceof NBTBase) {
					content.put(e.getKey(), getNBTBaseAsMap((NBTBase)e.getValue()));
					keys.put(i, e.getKey());
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
	
	@Override
	public String toString() {
		return getModName() + "(" + getModId() + "):" + getFriendlyName();
	}

	@Override
	public int compareTo(ItemIdentifier o) {
		/*if(uniqueID==0 || o.uniqueID==0){
			int c= this.itemID - o.itemID;
			if(c!=0) return c;
			c= this.itemDamage - o.itemDamage;
			if(c!=0) return c;
			if(tagsequal(this.tag,o.tag))
				return 0;
			return this.tag.hashCode() - o.tag.hashCode();
		}*/
		if(uniqueID<o.uniqueID)
			return -1;
		if(uniqueID>o.uniqueID)
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object that){
		if (!(that instanceof ItemIdentifier))
			return false;
		ItemIdentifier i = (ItemIdentifier)that;
		return this.uniqueID==i.uniqueID;
		
	}
	
	@Override public int hashCode(){
		return uniqueID;
	}

	public FluidIdentifier getFluidIdentifier() {
		return FluidIdentifier.get(itemID, itemDamage);
	}

	public boolean equalsForCrafting(ItemIdentifier item) {
		return this.itemID == item.itemID && (item.isDamagable() ? true : this.itemDamage == item.itemDamage);
	}

	public boolean equalsWithoutNBT(ItemIdentifier item) {
		return this.itemID == item.itemID && this.itemDamage == item.itemDamage;
	}

	public boolean isDamagable() {
		return this.makeNormalStack(0).getItem().isDamageable();
	}

	private static void checkNBTbadness(ItemIdentifier item, NBTBase nbt) {
		if((item.getMaxStackSize() > 1 || LogisticsPipes.DEBUG) && nbt.getName() == "") {
			LogisticsPipes.log.warning("Bad item " + item.getDebugName() + " : Root NBTTag has no name");
		}
		try {
			String s = checkNBTbadness_recurse(nbt);
			if(s != null) {
				LogisticsPipes.log.warning("Bad item " + item.getDebugName() + " : " + s);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private static String checkNBTbadness_recurse(NBTBase nbt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(nbt instanceof NBTTagList) {
			NBTTagList l = (NBTTagList) nbt;
			for(int i = 0; i < l.tagCount(); i++) {
				NBTBase b = l.tagAt(i);
				if(!b.getName().equals("")) {
					return "NBTTagList containing named tag " + b.getName();
				}
				String ret = checkNBTbadness_recurse(b);
				if(ret != null)
					return ret;
			}
		} else if(nbt instanceof NBTTagCompound) {
			Field fMap = ObfuscationHelper.getDeclaredField(NAMES.tagMap);
			fMap.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, NBTBase> internal = (Map<String, NBTBase>) fMap.get(nbt);
			for(Entry<String, NBTBase> e : internal.entrySet()) {
				String k = e.getKey();
				NBTBase v = e.getValue();
				if(k == null || k.equals("")) {
					return "NBTTagCompound containing empty key";
				}
				if(!k.equals(v.getName())) {
					return "NBTTagCompound key " + k + " doesn't match value name " + v;
				}
				String ret = checkNBTbadness_recurse(v);
				if(ret != null)
					return ret;
			}
		}
		return null;
	}
}
