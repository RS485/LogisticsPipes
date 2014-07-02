/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.FinalNBTTagCompound;
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
	//a key to look up a ItemIdentifier by ItemID:damage:tag
	private static class ItemKey {
		public final int itemID;
		public final int itemDamage;
		public final FinalNBTTagCompound tag;
		public ItemKey(int i, int d, FinalNBTTagCompound t) {
			this.itemID = i;
			this.itemDamage = d;
			this.tag = t;
		}
		@Override
		public boolean equals(Object that) {
			if (!(that instanceof ItemKey))
				return false;
			ItemKey i = (ItemKey)that;
			return this.itemID == i.itemID && this.itemDamage == i.itemDamage && this.tag.equals(i.tag);
		}
		@Override
		public int hashCode() {
			return ((itemID*0x8241)+itemDamage)^tag.hashCode();
		}
	}

	//remember itemId/damage/tag so we can find GCed ItemIdentifiers
	private static class IDReference extends WeakReference<ItemIdentifier> {
		private final ItemKey key;
		private final int uniqueID;
		IDReference(ItemKey k, int u, ItemIdentifier id) {
			super(id, keyRefQueue);
			key = k;
			uniqueID = u;
		}
	}
	
	//array of ItemIdentifiers for damage=0,tag=null items
	private final static AtomicReferenceArray<ItemIdentifier> simpleIdentifiers = new AtomicReferenceArray<ItemIdentifier>(32768);

	//array of arrays for items with damage>0 and tag==null
	private final static AtomicReferenceArray<AtomicReferenceArray<ItemIdentifier>> damageIdentifiers = new AtomicReferenceArray<AtomicReferenceArray<ItemIdentifier>>(32768);

	//map for id+damage+tag -> ItemIdentifier lookup
	private final static HashMap<ItemKey, IDReference> keyRefMap = new HashMap<ItemKey, IDReference>(1024, 0.5f);
	//for tracking the tagUniqueIDs in use for a given ItemID
	private final static BitSet[] tagIDsets = new BitSet[32768];
	//remember items we already printed a bad tag warning for
	private final static BitSet _badTags = new BitSet(32768);
	//a referenceQueue to collect GCed identifier refs
	private final static ReferenceQueue<ItemIdentifier> keyRefQueue = new ReferenceQueue<ItemIdentifier>();
	//and locks to protect these
	private final static ReadWriteLock keyRefLock = new ReentrantReadWriteLock();
	private final static Lock keyRefRlock = keyRefLock.readLock();
	private final static Lock keyRefWlock = keyRefLock.writeLock();

	//array of mod names, used for id -> name, 0 is unknown
	private final static ArrayList<String> _modNameList = new ArrayList<String>();
	//map of mod name -> internal LP modid, first modname gets 1
	private final static Map<String, Integer> _modNameToModIdMap = new HashMap<String, Integer>();
	//map of itemid -> modid
	private final static int _modItemIdMap[] = new int[32768];
	
	//helper thread to clean up references to GCed ItemIdentifiers
	private static final class ItemIdentifierCleanupThread extends Thread {
		public ItemIdentifierCleanupThread() {
			setName("LogisticsPipes ItemIdentifier Cleanup Thread");
			setDaemon(true);
			start();
		}
		public void run() {
			while(true) {
				IDReference r;
				try {
					r = (IDReference)(keyRefQueue.remove());
				} catch (InterruptedException e) {
					continue;
				}
				keyRefWlock.lock();
				do {
					//value in the map might have been replaced in the meantime
					IDReference current = keyRefMap.get(r.key);
					if(r == current) {
						keyRefMap.remove(r.key);
						tagIDsets[r.key.itemID].clear(r.uniqueID);
					}
					r = (IDReference)(keyRefQueue.poll());
				} while(r != null);
				keyRefWlock.unlock();
			}
		}
	}
	private static final ItemIdentifierCleanupThread cleanupThread = new ItemIdentifierCleanupThread();

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
	
	private int maxStackSize = 0;

	private ItemIdentifier _IDIgnoringNBT=null;
	private ItemIdentifier _IDIgnoringDamage=null;

	public static boolean allowNullsForTesting;
	
	private static ItemIdentifier getOrCreateSimple(int itemID) {
		//no locking here. if 2 threads race and create the same ItemIdentifier, they end up .equal() and one of them ends up in the volatile array
		ItemIdentifier ret = simpleIdentifiers.get(itemID);
		if(ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(itemID, 0, null, 0);
		simpleIdentifiers.set(itemID, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateDamage(int itemID, int damage) {
		//again no locking, we can end up removing or overwriting ItemIdentifiers concurrently added by another thread, but that doesn't affect anything.
		AtomicReferenceArray<ItemIdentifier> damages = damageIdentifiers.get(itemID);
		if(damages == null) {
			//round to nearest superior power of 2
			int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage + 1));
			damages = new AtomicReferenceArray<ItemIdentifier>(newlen);
			damageIdentifiers.set(itemID, damages);
		} else if(damages.length() <= damage) {
			int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage + 1));
			AtomicReferenceArray<ItemIdentifier> newdamages = new AtomicReferenceArray<ItemIdentifier>(newlen);
			for(int i = 0; i < damages.length(); i++)
				newdamages.set(i, damages.get(i));
			damageIdentifiers.set(itemID, newdamages);
			damages = newdamages;
		}
		ItemIdentifier ret = damages.get(damage);
		if(ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(itemID, damage, null, 0);
		damages.set(damage, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateTag(int itemID, int damage, FinalNBTTagCompound tag) {
		ItemKey k = new ItemKey(itemID, damage, tag);
		keyRefRlock.lock();
		IDReference r = keyRefMap.get(k);
		if(r != null) {
			ItemIdentifier ret = r.get();
			if(ret != null) {
				keyRefRlock.unlock();
				return ret;
			}
		}
		keyRefRlock.unlock();
		keyRefWlock.lock();
		r = keyRefMap.get(k);
		if(r != null) {
			ItemIdentifier ret = r.get();
			if(ret != null) {
				keyRefWlock.unlock();
				return ret;
			}
		}
		if(tagIDsets[itemID] == null) {
			tagIDsets[itemID] = new BitSet(16);
		}
		int nextUniqueID;
		if(r == null) {
			nextUniqueID = tagIDsets[itemID].nextClearBit(1);
			tagIDsets[itemID].set(nextUniqueID);
		} else {
			nextUniqueID = r.uniqueID;
		}
		FinalNBTTagCompound finaltag = new FinalNBTTagCompound((NBTTagCompound)tag.copy());
		ItemKey realKey = new ItemKey(itemID, damage, finaltag);
		ItemIdentifier ret = new ItemIdentifier(itemID, damage, finaltag, nextUniqueID);
		keyRefMap.put(realKey, new IDReference(realKey, nextUniqueID, ret));
		checkNBTbadness(ret, finaltag);
		keyRefWlock.unlock();
		return ret;
	}

	public static ItemIdentifier get(int itemID, int itemUndamagableDamage, NBTTagCompound tag)	{
		if(itemID < 0 || itemID > 32767) {
			throw new IllegalArgumentException("Item ID out of range");
		}
		if(itemUndamagableDamage < 0 || itemUndamagableDamage > 32767) {
			throw new IllegalArgumentException("Item Damage out of range");
		}
		if(tag == null && itemUndamagableDamage == 0) {
			//no tag, no damage
			return getOrCreateSimple(itemID);
		} else if(tag == null) {
			//no tag, damage
			return getOrCreateDamage(itemID, itemUndamagableDamage);
		} else {
			//tag
			return getOrCreateTag(itemID, itemUndamagableDamage, new FinalNBTTagCompound(tag));
		}
	}
	
	public static ItemIdentifier get(ItemStack itemStack) {
		if (itemStack == null && allowNullsForTesting){
			return null;
		}
		return get(itemStack.itemID, itemStack.getItemDamage(), itemStack.stackTagCompound);
	}
	
	public static List<ItemIdentifier> getMatchingNBTIdentifier(int itemID, int itemData) {
		//inefficient, we'll have to add another map if this becomes a bottleneck
		ArrayList<ItemIdentifier> resultlist = new ArrayList<ItemIdentifier>(16);
		keyRefRlock.lock();
		for(IDReference r : keyRefMap.values()) {
			ItemIdentifier t = r.get();
			if(t != null && t.itemID == itemID && t.itemDamage == itemData) {
				resultlist.add(t);
			}
		}
		keyRefRlock.unlock();
		return resultlist;
	}

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
	
	public ItemIdentifier getUndamaged() {
		if(_IDIgnoringDamage == null) {
			if(!unsafeMakeNormalStack(0).isItemStackDamageable()) {
				_IDIgnoringDamage = this;
			} else {
				ItemStack tstack = makeNormalStack(0);
				tstack.setItemDamage(0);
				_IDIgnoringDamage = get(tstack);
			}
		}
		return _IDIgnoringDamage;
	}

	public ItemIdentifier getIgnoringNBT() {
		if(_IDIgnoringNBT == null) {
			if(tag == null) {
				_IDIgnoringNBT = this;
			} else {
				_IDIgnoringNBT = get(itemID, itemDamage, null);
			}
		}
		return _IDIgnoringNBT;
	}

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
			return getName(itemID,this.unsafeMakeNormalStack(0));
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
		if(maxStackSize == 0) {
			ItemStack tstack = this.unsafeMakeNormalStack(0);
			int tstacksize = tstack.getMaxStackSize();
			if(tstack.isItemStackDamageable() && tstack.isItemDamaged()) {
				tstacksize = 1;
			}
			tstacksize = Math.max(1, Math.min(64, tstacksize));
			maxStackSize = tstacksize;
		}
		return maxStackSize;
	}
	
	private static Map<Integer, Object> getArrayAsMap(int[] array) {
		HashMap<Integer, Object> map = new HashMap<Integer, Object>();
		int i = 0;
		for(int object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	private static Map<Integer, Object> getArrayAsMap(byte[] array) {
		HashMap<Integer, Object> map = new HashMap<Integer, Object>();
		int i = 1;
		for(byte object: array) {
			map.put(i, object);
			i++;
		}
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	public static Map<Object, Object> getNBTBaseAsMap(NBTBase nbt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
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
			List internal = ((NBTTagList)nbt).tagList;
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
			Map internal = ((NBTTagCompound)nbt).tagMap;
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
		return getModName() + "(" + getModId() + "):" + getFriendlyName() + ", " + itemID + ":" + itemDamage;
	}

	@Override
	public int compareTo(ItemIdentifier o) {
		if(itemID<o.itemID)
			return -1;
		if(itemID>o.itemID)
			return 1;
		if(itemDamage<o.itemDamage)
			return -1;
		if(itemDamage>o.itemDamage)
			return 1;
		if(uniqueID<o.uniqueID)
			return -1;
		if(uniqueID>o.uniqueID)
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object that){
		if (that instanceof ItemIdentifierStack)
			throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
		if (!(that instanceof ItemIdentifier))
			return false;
		ItemIdentifier i = (ItemIdentifier)that;
		return this.equals(i);
		
	}

	public boolean equals(ItemIdentifier that){
		return this.itemID == that.itemID && this.itemDamage == that.itemDamage && this.uniqueID == that.uniqueID;
	}
	
	@Override
	public int hashCode(){
		if(tag == null)
			return (itemID*0x8241)+itemDamage;
		else
			return ((itemID*0x8241)+itemDamage)^tag.hashCode();
	}

	public boolean equalsForCrafting(ItemIdentifier item) {
		return this.itemID == item.itemID && (item.isDamageable() || (this.itemDamage == item.itemDamage));
	}

	public boolean equalsWithoutNBT(ItemStack stack) {
		return this.itemID == stack.itemID && this.itemDamage == stack.getItemDamage();
	}

	public boolean equalsWithoutNBT(ItemIdentifier item) {
		return this.itemID == item.itemID && this.itemDamage == item.itemDamage;
	}

	public boolean isDamageable() {
		return this.getUndamaged() == this;
	}

	private static void checkNBTbadness(ItemIdentifier item, NBTBase nbt) {
		if((item.getMaxStackSize() > 1 || LogisticsPipes.DEBUG) && nbt.getName().isEmpty()) {
			if (!_badTags.get(item.itemID)) {
				_badTags.set(item.itemID);
				LogisticsPipes.log.warning("Bad item " + item.getDebugName()
						+ " : Root NBTTag has no name");
			}
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
			@SuppressWarnings("unchecked")
			Map<String, NBTBase> internal = ((NBTTagCompound)nbt).tagMap;
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

	public boolean isFluidContainer() {
		return Item.itemsList[this.itemID] instanceof LogisticsFluidContainer;
	}
}
