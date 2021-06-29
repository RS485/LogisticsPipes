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
 * I have no bloody clue what different mods use to differentiate
 * between items except for itemID, there is metadata, damage, and
 * whatnot. so..... to avoid having to change all my bloody code every
 * time I need to support a new item targeted that would make it a
 * "different" item, I made this cache here. An ItemIdentifier is
 * immutable, singleton and most importantly UNIQUE!
 *
 * @author Krapht
 */
public final class ItemIdentifier implements Comparable<ItemIdentifier>, ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];

	// Map of ItemIdentifiers for damage = 0 and tag == null Items
	private static final ConcurrentHashMap<Item, ItemIdentifier> simpleIdentifiers = new ConcurrentHashMap<>(4096, 0.5f, 1);

	// Map of ItemIdentifiers for damage > 0 and tag == null Items
	private static final ConcurrentHashMap<Item, IDamagedItemIdentifierHolder> damageIdentifiers = new ConcurrentHashMap<>(4096, 0.5f, 1);

	private static final HashMap<ItemKey, IDReference> keyRefMap = new HashMap<>(1024, 0.5f);

	private static final HashMap<Item, BitSet> idSetsMap = new HashMap<>(1024, 0.5f);

	// ReferenceQueue for GCed ItemIdentifier references
	private static final ReferenceQueue<ItemIdentifier> keyRefQueue = new ReferenceQueue<>();

	private static final ReadWriteLock keyRefLock = new ReentrantReadWriteLock();
	private static final Lock keyRefRLock = ItemIdentifier.keyRefLock.readLock();
	private static final Lock keyRefWLock = ItemIdentifier.keyRefLock.writeLock();

	// Thread to clean up internal references by observing the ReferenceQueue
	private static final ItemIdentifierCleanupThread cleanupThread = new ItemIdentifierCleanupThread();

	public final Item item;
	public final int itemDamage;
	public final FinalNBTTagCompound tag;
	public final int uniqueID;

	private int maxStackSize = 0;
	private ItemIdentifier identifierIgnoringNBT;
	private ItemIdentifier identifierUnmanageable;
	private ItemIdentifier identifierIgnoringDamage;
	private DictItemIdentifier identifierDictionary;
	private boolean canHaveDictionary = true;
	private String modName;
	private String creativeTabName;

	// Hide default constructor
	private ItemIdentifier(Item item, int itemDamage, FinalNBTTagCompound tag, int uniqueID) {
		this.item = item;
		this.itemDamage = itemDamage;
		this.tag = tag;
		this.uniqueID = uniqueID;
	}

	private static ItemIdentifier getOrCreateSimple(Item item, ItemIdentifier proposal) {
		if (proposal != null
				&& proposal.item == item
				&& proposal.itemDamage == 0
				&& proposal.tag == null) {
			return proposal;
		}

		// No locking here. If 2 threads race and create the same ItemIdentifier, they end up .equal()
		ItemIdentifier ret = ItemIdentifier.simpleIdentifiers.get(item);
		if (ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, 0, null, 0);
		ItemIdentifier.simpleIdentifiers.put(item, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateDamage(Item item, int damage, ItemIdentifier proposal) {
		if (proposal != null
				&& proposal.item == item
				&& proposal.itemDamage == damage
				&& proposal.tag == null) {
			return proposal;
		}

		// No locking here. We can end up removing or overwriting ItemIdentifiers concurrently added
		// by other threads, but that doesn't affect anything.
		IDamagedItemIdentifierHolder damaged = ItemIdentifier.damageIdentifiers.get(item);
		if (damaged == null) {
			if (item.getMaxDamage(new ItemStack(item)) < 32767) {
				damaged = new ArrayDamagedItemIdentifierHolder(damage);
			} else {
				damaged = new MapDamagedItemIdentifierHolder();
			}
			ItemIdentifier.damageIdentifiers.put(item, damaged);
		} else {
			damaged.ensureCapacity(damage);
		}

		ItemIdentifier ret = damaged.get(damage);
		if (ret != null) {
			return ret;
		}
		ret = new ItemIdentifier(item, damage, null, 0);
		damaged.set(damage, ret);
		return ret;
	}

	private static ItemIdentifier getOrCreateTag(Item item, int damage, FinalNBTTagCompound tag) {
		ItemKey key = new ItemKey(item, damage, tag);

		ItemIdentifier.keyRefRLock.lock();
		IDReference idRef = ItemIdentifier.keyRefMap.get(key);
		if (idRef != null) {
			ItemIdentifier ret = idRef.get();
			if (ret != null) {
				ItemIdentifier.keyRefRLock.unlock();
				return ret;
			}
		}
		ItemIdentifier.keyRefRLock.unlock();

		ItemIdentifier.keyRefWLock.lock();
		idRef = ItemIdentifier.keyRefMap.get(key);
		if (idRef != null) {
			ItemIdentifier ret = idRef.get();
			if (ret != null) {
				ItemIdentifier.keyRefWLock.unlock();
				return ret;
			}
		}

		if (ItemIdentifier.idSetsMap.get(item) == null) {
			ItemIdentifier.idSetsMap.put(item, new BitSet(16));
		}
		int nextUniqueID;
		if (idRef == null) {
			nextUniqueID = ItemIdentifier.idSetsMap.get(item).nextClearBit(1);
			ItemIdentifier.idSetsMap.get(item).set(nextUniqueID);
		} else {
			nextUniqueID = idRef.uniqueID;
		}

		FinalNBTTagCompound finalTag = new FinalNBTTagCompound(tag);
		ItemKey finalKey = new ItemKey(item, damage, finalTag);
		ItemIdentifier ret = new ItemIdentifier(item, damage, finalTag, nextUniqueID);
		ItemIdentifier.keyRefMap.put(finalKey, new IDReference(finalKey, nextUniqueID, ret));
		ItemIdentifier.keyRefWLock.unlock();
		return ret;
	}

	public static ItemIdentifier get(Item item, int itemDamage, NBTTagCompound tag) {
		return get(item, itemDamage, tag, null);
	}

	private static ItemIdentifier get(Item item, int itemDamage, NBTTagCompound tag, ItemIdentifier proposal) {
		if (itemDamage < 0) {
			throw new IllegalArgumentException("Item Damage out of range: " + itemDamage + " <- must be non-negative");
		}
		if (tag == null && itemDamage == 0) {
			return getOrCreateSimple(item, proposal);
		} else if (tag == null) {
			return getOrCreateDamage(item, itemDamage, proposal);
		} else {
			return getOrCreateTag(item, itemDamage, new FinalNBTTagCompound(tag));
		}
	}

	/**
	 * The cast operation will not throw ClassCastException because of ASM
	 * byte-code is injected with the interface to persist
	 * data in {@link ItemStack} outside of LP. This way the item can be
	 * identified again when it returns to the network.
	 *
	 * @param itemStack the item to identify
	 * @return the transformed {@link ItemStack} possibly with additional data
	 * obtained from the injected {@link ArrayList} hidden behind the interface
	 */
	@SuppressWarnings("ConstantConditions")
	@Nonnull
	public static ItemIdentifier get(@Nonnull ItemStack itemStack) {
		ItemIdentifier proposal = null;
		IAddInfoProvider provider = null;
		if (!itemStack.hasTagCompound()) {
			provider = (IAddInfoProvider) (Object) itemStack;
			ItemStackAddInfo info = provider.getLogisticsPipesAddInfo(ItemStackAddInfo.class);
			if (info != null) {
				proposal = info.identifier;
			}
		}
		ItemIdentifier ident = ItemIdentifier.get(itemStack.getItem(), itemStack.getItemDamage(), itemStack.getTagCompound(), proposal);
		if (ident != proposal && provider != null && !itemStack.hasTagCompound()) {
			provider.setLogisticsPipesAddInfo(new ItemStackAddInfo(ident));
		}
		return ident;
	}

	public static List<ItemIdentifier> getMatchingNBTIdentifier(Item item, int itemData) {
		// TODO inefficient, we'll have to add another map if this becomes a bottleneck
		ArrayList<ItemIdentifier> ret = new ArrayList<>(16);
		ItemIdentifier.keyRefRLock.lock();
		for (IDReference idRef : ItemIdentifier.keyRefMap.values()) {
			ItemIdentifier identifier = idRef.get();
			if (identifier != null && identifier.item == item && identifier.itemDamage == itemData) {
				ret.add(identifier);
			}
		}
		ItemIdentifier.keyRefRLock.unlock();
		return ret;
	}

	/* Instance Methods */

	public ItemIdentifier getUndamaged() {
		if (identifierUnmanageable == null) {
			if (!unsafeMakeNormalStack(1).isItemStackDamageable()) {
				identifierUnmanageable = this;
			} else {
				ItemStack stack = makeNormalStack(1);
				stack.setItemDamage(0);
				identifierUnmanageable = ItemIdentifier.get(stack);
			}
		}
		return identifierUnmanageable;
	}

	public ItemIdentifier getIgnoringNBT() {
		if (identifierIgnoringNBT == null) {
			if (tag == null) {
				identifierIgnoringNBT = this;
			} else {
				identifierIgnoringNBT = ItemIdentifier.get(item, itemDamage, null, null);
			}
		}
		return identifierIgnoringNBT;
	}

	public ItemIdentifier getIgnoringDamage() {
		if (identifierIgnoringDamage == null) {
			if (itemDamage == 0) {
				identifierIgnoringDamage = this;
			} else {
				identifierIgnoringDamage = ItemIdentifier.get(item, 0, tag, null);
			}
		}
		return identifierIgnoringDamage;
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
			ResourceLocation resLocation = item.getRegistryName();
			assert resLocation != null;

			Map<String, ModContainer> modList = Loader.instance().getIndexedModList();
			ModContainer modContainer = modList.get(resLocation.getResourceDomain());
			if (modContainer == null) {
				// Get mod that really registered this item
				Map<ResourceLocation, String> map = ReflectionHelper
						.invokePrivateMethod(ForgeRegistry.class, ForgeRegistries.ITEMS, "getOverrideOwners", "getOverrideOwners", new Class[0], new Object[0]);

				String key = map.get(resLocation);
				if (key != null) {
					modContainer = modList.get(key);
				}
			}
			modName = modContainer != null ? modContainer.getName() : "UNKNOWN";
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
			ItemStack stack = unsafeMakeNormalStack(1);
			int stackSize = stack.getMaxStackSize();
			if (stack.isItemStackDamageable() && stack.isItemDamaged()) {
				stackSize = 1;
			}
			stackSize = Math.max(1, Math.min(64, stackSize));
			maxStackSize = stackSize;
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
	public int compareTo(ItemIdentifier other) {
		int diff = Item.getIdFromItem(item) - Item.getIdFromItem(other.item);
		if (diff != 0) {
			return diff;
		}
		diff = itemDamage - other.itemDamage;
		if (diff != 0) {
			return diff;
		}
		diff = uniqueID - other.uniqueID;
		return diff;
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
		if (identifierDictionary == null && canHaveDictionary) {
			identifierDictionary = DictItemIdentifier.getDictItemIdentifier(this);
			canHaveDictionary = false;
		}
		return identifierDictionary;
	}

	@Override
	public int hashCode() {
		if (tag == null) {
			return item.hashCode() + itemDamage;
		} else {
			return (item.hashCode() + itemDamage) ^ tag.hashCode();
		}
	}

	/**
	 * Key to look up an {@link ItemIdentifier} by {@link Item}:Damage:{@link FinalNBTTagCompound}
	 */
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

	private static class IDReference extends WeakReference<ItemIdentifier> {

		private final ItemKey key;
		private final int uniqueID;

		IDReference(ItemKey k, int u, ItemIdentifier id) {
			super(id, ItemIdentifier.keyRefQueue);
			key = k;
			uniqueID = u;
		}
	}

	private interface IDamagedItemIdentifierHolder {

		ItemIdentifier get(int damage);

		void set(int damage, ItemIdentifier ret);

		void ensureCapacity(int damage);
	}

	private static class MapDamagedItemIdentifierHolder implements IDamagedItemIdentifierHolder {

		private final ConcurrentHashMap<Integer, ItemIdentifier> holder;

		public MapDamagedItemIdentifierHolder() {
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
		public void ensureCapacity(int damage) { }
	}

	private static class ArrayDamagedItemIdentifierHolder implements IDamagedItemIdentifierHolder {

		private final int INT_SIZE = 32;

		private AtomicReferenceArray<ItemIdentifier> holder;

		public ArrayDamagedItemIdentifierHolder(int damage) {
			// Ceil to nearest power of 2
			int length = 1 << (INT_SIZE - Integer.numberOfLeadingZeros(damage));
			holder = new AtomicReferenceArray<>(length);
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
				int newLength = 1 << (INT_SIZE - Integer.numberOfLeadingZeros(damage));
				AtomicReferenceArray<ItemIdentifier> newDamages = new AtomicReferenceArray<>(newLength);
				for (int i = 0; i < holder.length(); i++) {
					newDamages.set(i, holder.get(i));
				}
				holder = newDamages;
			}
		}
	}

	/**
	 * Helper thread to clean up references to GCed {@link ItemIdentifier}s
	 */
	private static final class ItemIdentifierCleanupThread extends Thread {

		public ItemIdentifierCleanupThread() {
			setName("LogisticsPipes ItemIdentifier Cleanup Thread");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				IDReference idRef;
				try {
					idRef = (IDReference) ItemIdentifier.keyRefQueue.remove();
				} catch (InterruptedException ignored) {
					continue;
				}
				ItemIdentifier.keyRefWLock.lock();
				do {
					// Value in the map might have been replaced in the meantime
					IDReference current = ItemIdentifier.keyRefMap.get(idRef.key);
					if (idRef == current) {
						ItemIdentifier.keyRefMap.remove(idRef.key);
						ItemIdentifier.idSetsMap.get(idRef.key.item).clear(idRef.uniqueID);
					}
					idRef = (IDReference) (ItemIdentifier.keyRefQueue.poll());
				} while (idRef != null);
				ItemIdentifier.keyRefWLock.unlock();
			}
		}
	}

	@AllArgsConstructor
	private static class ItemStackAddInfo implements IAddInfo {

		private final ItemIdentifier identifier;
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

	// TODO should not this be LogisticsPipes.log.*() instead of sout?
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
}
