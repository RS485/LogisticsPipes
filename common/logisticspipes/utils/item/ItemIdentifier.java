/*
 * Copyright (c) Krapht, 2011
 * <p>
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import lombok.AllArgsConstructor;

import logisticspipes.asm.addinfo.IAddInfo;
import logisticspipes.asm.addinfo.IAddInfoProvider;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.utils.FinalNBTTagCompound;
import logisticspipes.utils.ReflectionHelper;

/**
 * @author Krapht I have no bloody clue what different mods use to differate
 * between items except for itemID, there is metadata, damage, and
 * whatnot. so..... to avoid having to change all my bloody code every
 * time I need to support a new item targeted that would make it a
 * "different" item, I made this cache here A ItemIdentifier is
 * immutable, singleton and most importantly UNIQUE!
 */
public final class ItemIdentifier implements Comparable<ItemIdentifier>, ILPCCTypeHolder {

	//a key to look up a ItemIdentifier by Item:damage:tag
	private static class ItemKey {

		public final Item item;
		public final int itemDamage;
		public final FinalNBTTagCompound tag;

		public ItemKey(Item i, int d, FinalNBTTagCompound t) {
			item = i;
			itemDamage = d;
			tag = t;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof ItemKey)) {
				return false;
			}
			ItemKey i = (ItemKey) that;
			return item == i.item && itemDamage == i.itemDamage && tag.equals(i.tag);
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
			super(id, ItemIdentifier.keyRefQueue);
			key = k;
			uniqueID = u;
		}
	}

	private interface IDamagedIdentifierHolder {

		ItemIdentifier get(int damage);

		void set(int damage, ItemIdentifier ret);

		void ensureCapacity(int damage);
	}

	private static class MapDamagedItentifierHolder implements IDamagedIdentifierHolder {

		private final ConcurrentHashMap<Integer, ItemIdentifier> holder;

		public MapDamagedItentifierHolder() {
			holder = new ConcurrentHashMap<>(4096, 0.5f, 1);
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
			int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage));
			holder = new AtomicReferenceArray<>(newlen);
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
			if (holder.length() <= damage) {
				int newlen = 1 << (32 - Integer.numberOfLeadingZeros(damage));
				AtomicReferenceArray<ItemIdentifier> newdamages = new AtomicReferenceArray<>(newlen);
				for (int i = 0; i < holder.length(); i++) {
					newdamages.set(i, holder.get(i));
				}
				holder = newdamages;
			}
		}
	}

	private final Object[] ccTypeHolder = new Object[1];

	//array of ItemIdentifiers for damage=0,tag=null items
	private final static ConcurrentHashMap<Item, ItemIdentifier> simpleIdentifiers = new ConcurrentHashMap<>(4096, 0.5f, 1);

	//array of arrays for items with damage>0 and tag==null
	private final static ConcurrentHashMap<Item, IDamagedIdentifierHolder> damageIdentifiers = new ConcurrentHashMap<>(4096, 0.5f, 1);

	//map for id+damage+tag -> ItemIdentifier lookup
	private final static HashMap<ItemKey, IDReference> keyRefMap = new HashMap<>(1024, 0.5f);
	//for tracking the tagUniqueIDs in use for a given Item
	private final static HashMap<Item, BitSet> tagIDsets = new HashMap<>(1024, 0.5f);
	//a referenceQueue to collect GCed identifier refs
	private final static ReferenceQueue<ItemIdentifier> keyRefQueue = new ReferenceQueue<>();
	//and locks to protect these
	private final static ReadWriteLock keyRefLock = new ReentrantReadWriteLock();
	private final static Lock keyRefRlock = ItemIdentifier.keyRefLock.readLock();
	private final static Lock keyRefWlock = ItemIdentifier.keyRefLock.writeLock();

	//helper thread to clean up references to GCed ItemIdentifiers
	private static final class ItemIdentifierCleanupThread extends Thread {

		public ItemIdentifierCleanupThread() {
			setName("LogisticsPipes ItemIdentifier Cleanup Thread");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				IDReference r;
				try {
					r = (IDReference) (ItemIdentifier.keyRefQueue.remove());
				} catch (InterruptedException e) {
					continue;
				}
				ItemIdentifier.keyRefWlock.lock();
				do {
					//value in the map might have been replaced in the meantime
					IDReference current = ItemIdentifier.keyRefMap.get(r.key);
					if (r == current) {
						ItemIdentifier.keyRefMap.remove(r.key);
						ItemIdentifier.tagIDsets.get(r.key.item).clear(r.uniqueID);
					}
					r = (IDReference) (ItemIdentifier.keyRefQueue.poll());
				} while (r != null);
				ItemIdentifier.keyRefWlock.unlock();
			}
		}
	}

	private static final ItemIdentifierCleanupThread cleanupThread = new ItemIdentifierCleanupThread();

	//Hide default constructor
	private ItemIdentifier(Item item, int itemDamage, FinalNBTTagCompound tag, int uniqueID) {
		this.item = item;
		this.itemDamage = itemDamage;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}

	public final Item item;
	public final int itemDamage;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;

	private int maxStackSize = 0;

	private ItemIdentifier _IDIgnoringNBT = null;
	private ItemIdentifier _IDIgnoringDamage = null;
	private ItemIdentifier _IDIgnoringData = null;
	private DictItemIdentifier _dict;
	private boolean canHaveDict = true;
	private String modName;
	private String creativeTabName;

	private static ItemIdentifier getOrCreateSimple(Item item, ItemIdentifier proposal) {
		if (proposal != null) {
			if (proposal.item == item && proposal.itemDamage == 0 && proposal.tag == null) {
				return proposal;
			}
		}
		//no locking here. if 2 threads race and create the same ItemIdentifier, they end up .equal() and one of them ends up in the map
		ItemIdentifier ret = ItemIdentifier.simpleIdentifiers.get(item);
		if (ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, 0, null, 0);
		ItemIdentifier.simpleIdentifiers.put(item, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateDamage(Item item, int damage, ItemIdentifier proposal) {
		if (proposal != null) {
			if (proposal.item == item && proposal.itemDamage == damage && proposal.tag == null) {
				return proposal;
			}
		}
		//again no locking, we can end up removing or overwriting ItemIdentifiers concurrently added by another thread, but that doesn't affect anything.
		IDamagedIdentifierHolder damages = ItemIdentifier.damageIdentifiers.get(item);
		if (damages == null) {
			if (item.getMaxDamage() < 32767) {
				damages = new ArrayDamagedItentifierHolder(damage);
			} else {
				damages = new MapDamagedItentifierHolder();
			}
			ItemIdentifier.damageIdentifiers.put(item, damages);
		} else {
			damages.ensureCapacity(damage);
		}
		ItemIdentifier ret = damages.get(damage);
		if (ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, damage, null, 0);
		damages.set(damage, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateTag(Item item, int damage, FinalNBTTagCompound tag) {
		ItemKey k = new ItemKey(item, damage, tag);
		ItemIdentifier.keyRefRlock.lock();
		IDReference r = ItemIdentifier.keyRefMap.get(k);
		if (r != null) {
			ItemIdentifier ret = r.get();
			if (ret != null) {
				ItemIdentifier.keyRefRlock.unlock();
				return ret;
			}
		}
		ItemIdentifier.keyRefRlock.unlock();
		ItemIdentifier.keyRefWlock.lock();
		r = ItemIdentifier.keyRefMap.get(k);
		if (r != null) {
			ItemIdentifier ret = r.get();
			if (ret != null) {
				ItemIdentifier.keyRefWlock.unlock();
				return ret;
			}
		}
		if (ItemIdentifier.tagIDsets.get(item) == null) {
			ItemIdentifier.tagIDsets.put(item, new BitSet(16));
		}
		int nextUniqueID;
		if (r == null) {
			nextUniqueID = ItemIdentifier.tagIDsets.get(item).nextClearBit(1);
			ItemIdentifier.tagIDsets.get(item).set(nextUniqueID);
		} else {
			nextUniqueID = r.uniqueID;
		}
		FinalNBTTagCompound finaltag = new FinalNBTTagCompound(tag);
		ItemKey realKey = new ItemKey(item, damage, finaltag);
		ItemIdentifier ret = new ItemIdentifier(item, damage, finaltag, nextUniqueID);
		ItemIdentifier.keyRefMap.put(realKey, new IDReference(realKey, nextUniqueID, ret));
		ItemIdentifier.keyRefWlock.unlock();
		return ret;
	}

	public static ItemIdentifier get(Item item, int itemUndamagableDamage, NBTTagCompound tag) {
		return get(item, itemUndamagableDamage, tag, null);
	}

	private static ItemIdentifier get(Item item, int itemUndamagableDamage, NBTTagCompound tag, ItemIdentifier proposal) {
		if (itemUndamagableDamage < 0) {
			throw new IllegalArgumentException("Item Damage out of range");
		}
		if (tag == null && itemUndamagableDamage == 0) {
			//no tag, no damage
			return ItemIdentifier.getOrCreateSimple(item, proposal);
		} else if (tag == null) {
			//no tag, damage
			return ItemIdentifier.getOrCreateDamage(item, itemUndamagableDamage, proposal);
		} else {
			//tag
			return ItemIdentifier.getOrCreateTag(item, itemUndamagableDamage, new FinalNBTTagCompound(tag));
		}
	}

	@AllArgsConstructor
	public static class ItemStackAddInfo implements IAddInfo {

		private final ItemIdentifier ident;
	}

	@SuppressWarnings("ConstantConditions")
	@Nonnull
	public static ItemIdentifier get(@Nonnull ItemStack itemStack) {
		ItemIdentifier proposal = null;
		IAddInfoProvider prov = null;
		if (((Object) itemStack) instanceof IAddInfoProvider && !itemStack.hasTagCompound()) {
			prov = (IAddInfoProvider) (Object) itemStack;
			ItemStackAddInfo info = prov.getLogisticsPipesAddInfo(ItemStackAddInfo.class);
			if (info != null) {
				proposal = info.ident;
			}
		}
		ItemIdentifier ident = ItemIdentifier.get(itemStack.getItem(), itemStack.getItemDamage(), itemStack.getTagCompound(), proposal);
		if (ident != proposal && prov != null && !itemStack.hasTagCompound()) {
			prov.setLogisticsPipesAddInfo(new ItemStackAddInfo(ident));
		}
		return ident;
	}

	public static List<ItemIdentifier> getMatchingNBTIdentifier(Item item, int itemData) {
		//inefficient, we'll have to add another map if this becomes a bottleneck
		ArrayList<ItemIdentifier> resultlist = new ArrayList<>(16);
		ItemIdentifier.keyRefRlock.lock();
		for (IDReference r : ItemIdentifier.keyRefMap.values()) {
			ItemIdentifier t = r.get();
			if (t != null && t.item == item && t.itemDamage == itemData) {
				resultlist.add(t);
			}
		}
		ItemIdentifier.keyRefRlock.unlock();
		return resultlist;
	}

	/* Instance Methods */

	public ItemIdentifier getUndamaged() {
		if (_IDIgnoringDamage == null) {
			if (!unsafeMakeNormalStack(1).isItemStackDamageable()) {
				_IDIgnoringDamage = this;
			} else {
				ItemStack tstack = makeNormalStack(1);
				tstack.setItemDamage(0);
				_IDIgnoringDamage = ItemIdentifier.get(tstack);
			}
		}
		return _IDIgnoringDamage;
	}

	public ItemIdentifier getIgnoringNBT() {
		if (_IDIgnoringNBT == null) {
			if (tag == null) {
				_IDIgnoringNBT = this;
			} else {
				_IDIgnoringNBT = ItemIdentifier.get(item, itemDamage, null, null);
			}
		}
		return _IDIgnoringNBT;
	}

	public ItemIdentifier getIgnoringData() {
		if (_IDIgnoringData == null) {
			if (itemDamage == 0) {
				_IDIgnoringData = this;
			} else {
				_IDIgnoringData = ItemIdentifier.get(item, 0, tag, null);
			}
		}
		return _IDIgnoringData;
	}

	public String getDebugName() {
		return item.getUnlocalizedName() + "(ID: " + Item.getIdFromItem(item) + ", Damage: " + itemDamage + ")";
	}

	@Nonnull
	private String getName(@Nonnull ItemStack stack) {
		return item.getItemStackDisplayName(stack);
	}

	@Nonnull
	public String getFriendlyName() {
		return getName(unsafeMakeNormalStack(1));
	}

	public String getFriendlyNameCC() {
		return MainProxy.proxy.getName(this);
	}

	public String getModName() {
		if (modName == null) {
			ResourceLocation rl = item.getRegistryName();
			assert rl != null;
			Map<String, ModContainer> modList = Loader.instance().getIndexedModList();
			ModContainer mc = modList.get(rl.getResourceDomain());
			if (mc == null) {
				// get mod that really registered this item
				Map<ResourceLocation, String> map = ReflectionHelper.invokePrivateMethod(ForgeRegistry.class, ForgeRegistries.ITEMS, "getOverrideOwners", "getOverrideOwners", new Class[0], new Object[0]);

				final String key = map.get(rl);
				if (key != null)
					mc = modList.get(key);
			}

			modName = mc != null ? mc.getName() : "UNKNOWN";
		}
		return modName;
	}

	public String getCreativeTabName() {
		if (creativeTabName == null) {
			CreativeTabs tab = item.getCreativeTab();

			if (tab == null && item instanceof ItemBlock) {
				Block block = Block.getBlockFromItem(item);
				if (block != Blocks.AIR) {
					tab = block.getCreativeTabToDisplayOn();
				}
			}

			if (tab != null) {
				creativeTabName = tab.tabLabel;
			}
		}
		return creativeTabName;
	}

	@Nonnull
	public ItemIdentifierStack makeStack(int stackSize) {
		return new ItemIdentifierStack(this, stackSize);
	}

	@Nonnull
	public ItemStack unsafeMakeNormalStack(int stackSize) {
		ItemStack stack = new ItemStack(item, stackSize, itemDamage);
		stack.setTagCompound(tag);
		return stack;
	}

	@Nonnull
	public ItemStack makeNormalStack(int stackSize) {
		ItemStack stack = new ItemStack(item, stackSize, itemDamage);
		if (tag != null) {
			stack.setTagCompound(tag.copy());
		}
		return stack;
	}

	@Nonnull
	public EntityItem makeEntityItem(int stackSize, World world, double x, double y, double z) {
		return new EntityItem(world, x, y, z, makeNormalStack(stackSize));
	}

	public int getMaxStackSize() {
		if (maxStackSize == 0) {
			ItemStack tstack = unsafeMakeNormalStack(1);
			int tstacksize = tstack.getMaxStackSize();
			if (tstack.isItemStackDamageable() && tstack.isItemDamaged()) {
				tstacksize = 1;
			}
			tstacksize = Math.max(1, Math.min(64, tstacksize));
			maxStackSize = tstacksize;
		}
		return maxStackSize;
	}

	private static Map<Integer, Object> getArrayAsMap(int[] array) {
		HashMap<Integer, Object> map = new HashMap<>();
		int i = 0;
		for (int object : array) {
			map.put(i, object);
			i++;
		}
		return map;
	}

	private static Map<Integer, Object> getArrayAsMap(byte[] array) {
		HashMap<Integer, Object> map = new HashMap<>();
		int i = 1;
		for (byte object : array) {
			map.put(i, object);
			i++;
		}
		return map;
	}

	public static Map<Object, Object> getNBTBaseAsMap(NBTBase nbt) throws SecurityException, IllegalArgumentException {
		if (nbt == null) return null;

		if (nbt instanceof NBTTagByte) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagByte");
			map.put("value", ((NBTTagByte) nbt).getByte());
			return map;
		} else if (nbt instanceof NBTTagByteArray) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagByteArray");
			map.put("value", ItemIdentifier.getArrayAsMap(((NBTTagByteArray) nbt).getByteArray()));
			return map;
		} else if (nbt instanceof NBTTagDouble) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagDouble");
			map.put("value", ((NBTTagDouble) nbt).getDouble());
			return map;
		} else if (nbt instanceof NBTTagFloat) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagFloat");
			map.put("value", ((NBTTagFloat) nbt).getFloat());
			return map;
		} else if (nbt instanceof NBTTagInt) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagInt");
			map.put("value", ((NBTTagInt) nbt).getInt());
			return map;
		} else if (nbt instanceof NBTTagIntArray) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagIntArray");
			map.put("value", ItemIdentifier.getArrayAsMap(((NBTTagIntArray) nbt).getIntArray()));
			return map;
		} else if (nbt instanceof NBTTagList) {
			HashMap<Integer, Object> content = new HashMap<>();
			int i = 1;
			for (Object object : ((NBTTagList) nbt)) {
				if (object instanceof NBTBase) {
					content.put(i, ItemIdentifier.getNBTBaseAsMap((NBTBase) object));
				}
				i++;
			}
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagList");
			map.put("value", content);
			return map;
		} else if (nbt instanceof NBTTagCompound) {
			HashMap<Object, Object> content = new HashMap<>();
			HashMap<Integer, Object> keys = new HashMap<>();
			int i = 1;
			for (String key : ((NBTTagCompound) nbt).getKeySet()) {
				NBTBase value = ((NBTTagCompound) nbt).getTag(key);
				content.put(key, ItemIdentifier.getNBTBaseAsMap(value));
				keys.put(i, key);
				i++;
			}
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagCompound");
			map.put("value", content);
			map.put("keys", keys);
			return map;
		} else if (nbt instanceof NBTTagLong) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagLong");
			map.put("value", ((NBTTagLong) nbt).getLong());
			return map;
		} else if (nbt instanceof NBTTagShort) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagShort");
			map.put("value", ((NBTTagShort) nbt).getShort());
			return map;
		} else if (nbt instanceof NBTTagString) {
			HashMap<Object, Object> map = new HashMap<>();
			map.put("type", "NBTTagString");
			map.put("value", ((NBTTagString) nbt).getString());
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
		int c = Item.getIdFromItem(item) - Item.getIdFromItem(o.item);
		if (c != 0) {
			return c;
		}
		c = itemDamage - o.itemDamage;
		if (c != 0) {
			return c;
		}
		c = uniqueID - o.uniqueID;
		return c;
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof ItemIdentifierStack) {
			throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
		}
		if (!(that instanceof ItemIdentifier)) {
			return false;
		}
		ItemIdentifier i = (ItemIdentifier) that;
		return this.equals(i);

	}

	public boolean equals(ItemIdentifier that) {
		if (that == null) return false;
		return item == that.item && itemDamage == that.itemDamage && uniqueID == that.uniqueID;
	}

	@Override
	public int hashCode() {
		if (tag == null) {
			return item.hashCode() + itemDamage;
		} else {
			return (item.hashCode() + itemDamage) ^ tag.hashCode();
		}
	}

	public boolean equalsForCrafting(ItemIdentifier item) {
		return this.item == item.item && (item.isDamageable() || (itemDamage == item.itemDamage));
	}

	public boolean equalsWithoutNBT(@Nonnull ItemStack stack) {
		return item == stack.getItem() && itemDamage == stack.getItemDamage();
	}

	public boolean equalsWithoutNBT(ItemIdentifier item) {
		return this.item == item.item && itemDamage == item.itemDamage;
	}

	public boolean isDamageable() {
		return unsafeMakeNormalStack(1).isItemStackDamageable();
	}

	public boolean isFluidContainer() {
		return item instanceof LogisticsFluidContainer;
	}

	@Nullable
	public DictItemIdentifier getDictIdentifiers() {
		if (_dict == null && canHaveDict) {
			_dict = DictItemIdentifier.getDictItemIdentifier(this);
			canHaveDict = false;
		}
		return _dict;
	}

	public void debugDumpData(boolean isClient) {
		System.out.println((isClient ? "Client" : "Server") + " Item: " + Item.getIdFromItem(item) + ":" + itemDamage + " uniqueID " + uniqueID);
		StringBuilder sb = new StringBuilder();
		sb.append("Tag: ");
		debugDumpTag(tag, sb);
		System.out.println(sb.toString());
		System.out.println("Damageable: " + isDamageable());
		System.out.println("MaxStackSize: " + getMaxStackSize());
		if (getUndamaged() == this) {
			System.out.println("Undamaged: this");
		} else {
			System.out.println("Undamaged:");
			getUndamaged().debugDumpData(isClient);
		}
		System.out.println("Mod: " + getModName());
		System.out.println("CreativeTab: " + getCreativeTabName());
		if (getDictIdentifiers() != null) {
			getDictIdentifiers().debugDumpData(isClient);
		}
	}

	private void debugDumpTag(NBTBase nbt, StringBuilder sb) {
		if (nbt == null) {
			sb.append("null");
			return;
		}
		if (nbt instanceof NBTTagByte) {
			sb.append("TagByte(data=").append(((NBTTagByte) nbt).getByte()).append(")");
		} else if (nbt instanceof NBTTagShort) {
			sb.append("TagShort(data=").append(((NBTTagShort) nbt).getShort()).append(")");
		} else if (nbt instanceof NBTTagInt) {
			sb.append("TagInt(data=").append(((NBTTagInt) nbt).getInt()).append(")");
		} else if (nbt instanceof NBTTagLong) {
			sb.append("TagLong(data=").append(((NBTTagLong) nbt).getLong()).append(")");
		} else if (nbt instanceof NBTTagFloat) {
			sb.append("TagFloat(data=").append(((NBTTagFloat) nbt).getFloat()).append(")");
		} else if (nbt instanceof NBTTagDouble) {
			sb.append("TagDouble(data=").append(((NBTTagDouble) nbt).getDouble()).append(")");
		} else if (nbt instanceof NBTTagString) {
			sb.append("TagString(data=\"").append(((NBTTagString) nbt).getString()).append("\")");
		} else if (nbt instanceof NBTTagByteArray) {
			sb.append("TagByteArray(data=");
			for (int i = 0; i < ((NBTTagByteArray) nbt).getByteArray().length; i++) {
				sb.append(((NBTTagByteArray) nbt).getByteArray()[i]);
				if (i < ((NBTTagByteArray) nbt).getByteArray().length - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
		} else if (nbt instanceof NBTTagIntArray) {
			sb.append("TagIntArray(data=");
			for (int i = 0; i < ((NBTTagIntArray) nbt).getIntArray().length; i++) {
				sb.append(((NBTTagIntArray) nbt).getIntArray()[i]);
				if (i < ((NBTTagIntArray) nbt).getIntArray().length - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
		} else if (nbt instanceof NBTTagList) {
			sb.append("TagList(data=");
			for (int i = 0; i < ((NBTTagList) nbt).tagCount(); i++) {
				debugDumpTag((((NBTTagList) nbt).get(i)), sb);
				if (i < ((NBTTagList) nbt).tagCount() - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
		} else if (nbt instanceof NBTTagCompound) {
			sb.append("TagCompound(data=");
			for (Iterator<String> iter = ((NBTTagCompound) nbt).getKeySet().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				NBTBase value = ((NBTTagCompound) nbt).getTag(key);
				sb.append("\"").append(key).append("\"=");
				debugDumpTag((value), sb);
				if (iter.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(")");
		} else {
			sb.append(nbt.getClass().getName()).append("(?)");
		}
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}
}
