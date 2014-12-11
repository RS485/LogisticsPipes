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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
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
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * @author Krapht
 * 
 * I have no bloody clue what different mods use to differate between items except for itemID, 
 * there is metadata, damage, and whatnot. so..... to avoid having to change all my bloody code every 
 * time I need to support a new item flag that would make it a "different" item, I made this cache here
 * 
 *  A ItemIdentifier is immutable, singleton and most importantly UNIQUE!
 */
public final class ItemIdentifier implements Comparable<ItemIdentifier>, ILPCCTypeHolder {
	//a key to look up a ItemIdentifier by Item:damage:tag
	private static class ItemKey {
		public final Item item;
		public final int itemDamage;
		public final FinalNBTTagCompound tag;
		public ItemKey(Item i, int d, FinalNBTTagCompound t) {
			this.item = i;
			this.itemDamage = d;
			this.tag = t;
		}
		@Override
		public boolean equals(Object that) {
			if (!(that instanceof ItemKey))
				return false;
			ItemKey i = (ItemKey)that;
			return this.item == i.item && this.itemDamage == i.itemDamage && this.tag.equals(i.tag);
		}
		@Override
		public int hashCode() {
			return item.hashCode() ^ itemDamage ^ tag.hashCode();
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
	
	private static interface IDamagedIdentifierHolder {
		ItemIdentifier get(int damage);
		void set(int damage, ItemIdentifier ret);
		void ensureCapacity(int damage);
	}
	
	private static class MapDamagedItentifierHolder implements IDamagedIdentifierHolder {
		private ConcurrentHashMap<Integer, ItemIdentifier> holder;

		public MapDamagedItentifierHolder() {
			holder = new ConcurrentHashMap<Integer, ItemIdentifier>(4096, 0.5f, 1);
		}

		@Override
		public ItemIdentifier get(int damage) {
			return holder.get(damage);
		}

		@Override
		public void set(int damage, ItemIdentifier item) {
			holder.put(damage, item);
		}

		@Override
		public void ensureCapacity(int damage) {}
	}
	
	private static class ArrayDamagedItentifierHolder implements IDamagedIdentifierHolder {
		private AtomicReferenceArray<ItemIdentifier> holder;

		public ArrayDamagedItentifierHolder(int damage) {
			//round to nearest superior power of 2
			int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage + 1));
			holder = new AtomicReferenceArray<ItemIdentifier>(newlen);
		}

		@Override
		public ItemIdentifier get(int damage) {
			return holder.get(damage);
		}

		@Override
		public void set(int damage, ItemIdentifier ident) {
			holder.set(damage, ident);
		}

		@Override
		public void ensureCapacity(int damage) {
			int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage + 1));
			AtomicReferenceArray<ItemIdentifier> newdamages = new AtomicReferenceArray<ItemIdentifier>(newlen);
			for(int i = 0; i < holder.length(); i++) {
				newdamages.set(i, holder.get(i));
			}
			holder = newdamages;
		}
	}
	
	//array of ItemIdentifiers for damage=0,tag=null items
	private final static ConcurrentHashMap<Item, ItemIdentifier> simpleIdentifiers = new ConcurrentHashMap<Item, ItemIdentifier>(4096, 0.5f, 1);

	//array of arrays for items with damage>0 and tag==null
	private final static ConcurrentHashMap<Item, IDamagedIdentifierHolder> damageIdentifiers = new ConcurrentHashMap<Item, IDamagedIdentifierHolder>(4096, 0.5f, 1);

	//map for id+damage+tag -> ItemIdentifier lookup
	private final static HashMap<ItemKey, IDReference> keyRefMap = new HashMap<ItemKey, IDReference>(1024, 0.5f);
	//for tracking the tagUniqueIDs in use for a given Item
	private final static HashMap<Item, BitSet> tagIDsets = new HashMap<Item, BitSet>(1024, 0.5f);
	//a referenceQueue to collect GCed identifier refs
	private final static ReferenceQueue<ItemIdentifier> keyRefQueue = new ReferenceQueue<ItemIdentifier>();
	//and locks to protect these
	private final static ReadWriteLock keyRefLock = new ReentrantReadWriteLock();
	private final static Lock keyRefRlock = keyRefLock.readLock();
	private final static Lock keyRefWlock = keyRefLock.writeLock();

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
						tagIDsets.get(r.key.item).clear(r.uniqueID);
					}
					r = (IDReference)(keyRefQueue.poll());
				} while(r != null);
				keyRefWlock.unlock();
			}
		}
	}
	private static final ItemIdentifierCleanupThread cleanupThread = new ItemIdentifierCleanupThread();

	//Hide default constructor
	private ItemIdentifier(Item item, int itemDamage, FinalNBTTagCompound tag, int uniqueID) {
		this.item =  item;
		this.itemDamage = itemDamage;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}
	
	private Object ccType;
	
	public final Item item;
	public final int itemDamage;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;
	
	private int maxStackSize = 0;

	private ItemIdentifier _IDIgnoringNBT=null;
	private ItemIdentifier _IDIgnoringDamage=null;

	public static boolean allowNullsForTesting;
	
	private static ItemIdentifier getOrCreateSimple(Item item) {
		//no locking here. if 2 threads race and create the same ItemIdentifier, they end up .equal() and one of them ends up in the map
		ItemIdentifier ret = simpleIdentifiers.get(item);
		if(ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, 0, null, 0);
		simpleIdentifiers.put(item, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateDamage(Item item, int damage) {
		//again no locking, we can end up removing or overwriting ItemIdentifiers concurrently added by another thread, but that doesn't affect anything.
		IDamagedIdentifierHolder damages = damageIdentifiers.get(item);
		if(damages == null) {
			if(item.getMaxDamage() < 32767) {
				damages = new ArrayDamagedItentifierHolder(damage);
			} else {
				damages = new MapDamagedItentifierHolder();
			}
			damageIdentifiers.put(item, damages);
		} else {
			damages.ensureCapacity(damage);
		}
		ItemIdentifier ret = damages.get(damage);
		if(ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, damage, null, 0);
		damages.set(damage, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateTag(Item item, int damage, FinalNBTTagCompound tag) {
		ItemKey k = new ItemKey(item, damage, tag);
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
		if(tagIDsets.get(item) == null) {
			tagIDsets.put(item, new BitSet(16));
		}
		int nextUniqueID;
		if(r == null) {
			nextUniqueID = tagIDsets.get(item).nextClearBit(1);
			tagIDsets.get(item).set(nextUniqueID);
		} else {
			nextUniqueID = r.uniqueID;
		}
		FinalNBTTagCompound finaltag = new FinalNBTTagCompound((NBTTagCompound)tag.copy());
		ItemKey realKey = new ItemKey(item, damage, finaltag);
		ItemIdentifier ret = new ItemIdentifier(item, damage, finaltag, nextUniqueID);
		keyRefMap.put(realKey, new IDReference(realKey, nextUniqueID, ret));
		keyRefWlock.unlock();
		return ret;
	}

	public static ItemIdentifier get(Item item, int itemUndamagableDamage, NBTTagCompound tag)	{
		if(itemUndamagableDamage < 0) {
			throw new IllegalArgumentException("Item Damage out of range");
		}
		if(tag == null && itemUndamagableDamage == 0) {
			//no tag, no damage
			return getOrCreateSimple(item);
		} else if(tag == null) {
			//no tag, damage
			return getOrCreateDamage(item, itemUndamagableDamage);
		} else {
			//tag
			return getOrCreateTag(item, itemUndamagableDamage, new FinalNBTTagCompound(tag));
		}
	}
	
	public static ItemIdentifier get(ItemStack itemStack) {
		if (itemStack == null && allowNullsForTesting){
			return null;
		}
		return get(itemStack.getItem(), itemStack.getItemDamage(), itemStack.stackTagCompound);
	}
	
	public static List<ItemIdentifier> getMatchingNBTIdentifier(Item item, int itemData) {
		//inefficient, we'll have to add another map if this becomes a bottleneck
		ArrayList<ItemIdentifier> resultlist = new ArrayList<ItemIdentifier>(16);
		keyRefRlock.lock();
		for(IDReference r : keyRefMap.values()) {
			ItemIdentifier t = r.get();
			if(t != null && t.item == item && t.itemDamage == itemData) {
				resultlist.add(t);
			}
		}
		keyRefRlock.unlock();
		return resultlist;
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
				_IDIgnoringNBT = get(item, itemDamage, null);
			}
		}
		return _IDIgnoringNBT;
	}

	public String getDebugName() {
		return item.getUnlocalizedName() + "(ID: " + Item.getIdFromItem(item) + ", Damage: " + itemDamage + ")";
	}
	
	private String getName(ItemStack stack) {
		String name = "???";
		try {
			name = item.getItemStackDisplayName(stack);
			if(name == null) {
				throw new Exception();
			}
		} catch(Exception e) {
			try {
				name = item.getUnlocalizedName(stack);
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = item.getUnlocalizedName();
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
		return getName(this.unsafeMakeNormalStack(0));
	}
	
	public String getFriendlyNameCC() {
		return MainProxy.proxy.getName(this);
	}
	
	public String getModName() {
		UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(this.item);
		if(ui == null) return "UNKNOWN";
		return ui.modId;
	}
	
	public ItemIdentifierStack makeStack(int stackSize){
		return new ItemIdentifierStack(this, stackSize);
	}
	
	public ItemStack unsafeMakeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.item, stackSize, this.itemDamage);
		stack.setTagCompound(this.tag);
		return stack;
	}

	public ItemStack makeNormalStack(int stackSize){
		ItemStack stack = new ItemStack(this.item, stackSize, this.itemDamage);
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
			map.put("type", "NBTTagByte");
			map.put("value", ((NBTTagByte)nbt).func_150290_f());
			return map;
		} else if(nbt instanceof NBTTagByteArray) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagByteArray");
			map.put("value", getArrayAsMap(((NBTTagByteArray)nbt).func_150292_c()));
			return map;
		} else if(nbt instanceof NBTTagDouble) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagDouble");
			map.put("value", ((NBTTagDouble)nbt).func_150286_g());
			return map;
		} else if(nbt instanceof NBTTagFloat) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagFloat");
			map.put("value", ((NBTTagFloat)nbt).func_150288_h());
			return map;
		} else if(nbt instanceof NBTTagInt) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagInt");
			map.put("value", ((NBTTagInt)nbt).func_150287_d());
			return map;
		} else if(nbt instanceof NBTTagIntArray) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagIntArray");
			map.put("value", getArrayAsMap(((NBTTagIntArray)nbt).func_150302_c()));
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
			map.put("type", "NBTTagCompound");
			map.put("value", content);
			map.put("keys", keys);
			return map;
		} else if(nbt instanceof NBTTagLong) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagLong");
			map.put("value", ((NBTTagLong)nbt).func_150291_c());
			return map;
		} else if(nbt instanceof NBTTagShort) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put("type", "NBTTagShort");
			map.put("value", ((NBTTagShort)nbt).func_150287_d());
			return map;
		} else if(nbt instanceof NBTTagString) {
			HashMap<Object, Object> map = new HashMap<Object, Object>(); 
			map.put("type", "NBTTagString");
			map.put("value", ((NBTTagString)nbt).func_150285_a_());
			return map;
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
	
	@Override
	public String toString() {
		return getModName() + ":" + getFriendlyName() + ", " + Item.getIdFromItem(item) + ":" + itemDamage;
	}

	@Override
	public int compareTo(ItemIdentifier o) {
		int c=Item.getIdFromItem(item) - Item.getIdFromItem(o.item);
		if(c!=0) return c;
		c=itemDamage-o.itemDamage;
		if(c!=0) return c;
		c=uniqueID-o.uniqueID;
		return c;
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
		return this.item == that.item && this.itemDamage == that.itemDamage && this.uniqueID == that.uniqueID;
	}
	
	@Override
	public int hashCode(){
		if(tag == null)
			return item.hashCode()+itemDamage;
		else
			return (item.hashCode()+itemDamage)^tag.hashCode();
	}

	public boolean equalsForCrafting(ItemIdentifier item) {
		return this.item == item.item && (item.isDamageable() || (this.itemDamage == item.itemDamage));
	}

	public boolean equalsWithoutNBT(ItemStack stack) {
		return this.item == stack.getItem() && this.itemDamage == stack.getItemDamage();
	}

	public boolean equalsWithoutNBT(ItemIdentifier item) {
		return this.item == item.item && this.itemDamage == item.itemDamage;
	}

	public boolean isDamageable() {
		return this.unsafeMakeNormalStack(0).isItemStackDamageable();
	}

	public boolean isFluidContainer() {
		return this.item instanceof LogisticsFluidContainer;
	}
	
	public void debugDumpData(boolean isClient) {
		System.out.println((isClient?"Client":"Server") + " Item: " + Item.getIdFromItem(item) + ":" + itemDamage + " uniqueID " + uniqueID);
		StringBuilder sb = new StringBuilder();
		sb.append("Tag: ");
		debugDumpTag(tag, sb);
		System.out.println(sb.toString());
		System.out.println("Damageable: " + isDamageable());
		System.out.println("MaxStackSize: " + getMaxStackSize());
		if(this.getUndamaged() == this) {
			System.out.println("Undamaged: this");
		} else {
			System.out.println("Undamaged:");
			this.getUndamaged().debugDumpData(isClient);
		}
	}
	
	private void debugDumpTag(NBTBase nbt, StringBuilder sb) {
		if(nbt == null) {
			sb.append("null");
			return;
		}
		if(nbt instanceof NBTTagByte) {
			sb.append("TagByte(data=" + ((NBTTagByte)nbt).func_150290_f() + ")");
		} else if(nbt instanceof NBTTagShort) {
			sb.append("TagShort(data=" + ((NBTTagShort)nbt).func_150289_e() + ")");
		} else if(nbt instanceof NBTTagInt) {
			sb.append("TagInt(data=" + ((NBTTagInt)nbt).func_150287_d() + ")");
		} else if(nbt instanceof NBTTagLong) {
			sb.append("TagLong(data=" + ((NBTTagLong)nbt).func_150291_c() + ")");
		} else if(nbt instanceof NBTTagFloat) {
			sb.append("TagFloat(data=" + ((NBTTagFloat)nbt).func_150288_h() + ")");
		} else if(nbt instanceof NBTTagDouble) {
			sb.append("TagDouble(data=" + ((NBTTagDouble)nbt).func_150286_g() + ")");
		} else if(nbt instanceof NBTTagString) {
			sb.append("TagString(data=\"" + ((NBTTagString)nbt).func_150285_a_() + "\")");
		} else if(nbt instanceof NBTTagByteArray) {
			sb.append("TagByteArray(data=");
			for(int i = 0; i < ((NBTTagByteArray)nbt).func_150292_c().length; i++) {
				sb.append(((NBTTagByteArray)nbt).func_150292_c()[i]);
				if(i < ((NBTTagByteArray)nbt).func_150292_c().length - 1)
					sb.append(",");
			}
			sb.append(")");
		} else if(nbt instanceof NBTTagIntArray) {
			sb.append("TagIntArray(data=");
			for(int i = 0; i < ((NBTTagIntArray)nbt).func_150302_c().length; i++) {
				sb.append(((NBTTagIntArray)nbt).func_150302_c()[i]);
				if(i < ((NBTTagIntArray)nbt).func_150302_c().length - 1)
					sb.append(",");
			}
			sb.append(")");
		} else if(nbt instanceof NBTTagList) {
			sb.append("TagList(data=");
			for(int i = 0; i < ((NBTTagList)nbt).tagList.size(); i++) {
				debugDumpTag((NBTBase)(((NBTTagList)nbt).tagList.get(i)), sb);
				if(i < ((NBTTagList)nbt).tagList.size() - 1)
					sb.append(",");
			}
			sb.append(")");
		} else if(nbt instanceof NBTTagCompound) {
			sb.append("TagCompound(data=");
			Object[] oe = ((NBTTagCompound)nbt).tagMap.entrySet().toArray();
			for(int i = 0; i < oe.length; i++) {
				Entry<String, NBTBase> e = (Entry<String, NBTBase>)(oe[i]);
				sb.append("\"" + e.getKey() + "\"=");
				debugDumpTag((NBTBase)(e.getValue()), sb);
				if(i < oe.length - 1)
					sb.append(",");
			}
			sb.append(")");
		} else {
			sb.append(nbt.getClass().getName() + "(?)");
		}
	}

	@Override
	public void setCCType(Object type) {
		ccType = type;
	}

	@Override
	public Object getCCType() {
		return ccType;
	}
}
