package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import logisticspipes.utils.item.ItemIdentifier;

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler {

	public boolean init = false;
	private final boolean hideOnePerStack;
	private final TileEntity tile;
	private final EnumFacing dir;
	private final LPActionSource source;
	private IStorageMonitorableAccessor acc = null;
	public IGridHost host;
	LinkedList<Entry<ItemIdentifier, Integer>> cached;

	private AEInterfaceInventoryHandler(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		this.tile = tile;
		this.hideOnePerStack = hideOnePerStack || hideOne;
		this.acc = tile.getCapability(LPStorageMonitorableAccessor.STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, dir);
		host = (IGridHost) tile;
		source = new LPActionSource(this);
		this.dir = dir;
	}

	public AEInterfaceInventoryHandler() {
		tile = null;
		hideOnePerStack = false;
		source = null;
		dir = null;
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return tile instanceof IGridHost && tile.hasCapability(LPStorageMonitorableAccessor.STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, dir);
	}

	@Override
	public boolean init() {
		init = true;
		return true;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new AEInterfaceInventoryHandler(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}

	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		Map<ItemIdentifier, Integer> result;
		if (linked) {
			result = new LinkedHashMap<>();
		} else {
			result = new HashMap<>();
		}

		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if ((tmp == null) || (tmp.getInventory(channel) == null) || (tmp.getInventory(channel).getStorageList() == null)) {
			return result;
		}

		IItemList<IAEItemStack> items = tmp.getInventory(channel).getStorageList();
		for (IAEItemStack item : items) {
			ItemIdentifier ident = ItemIdentifier.get(item.createItemStack());
			Integer count = result.get(ident);
			if (count != null) {
				result.put(ident, (int) (count + item.getStackSize() - (hideOnePerStack ? 1 : 0)));
			} else {
				result.put(ident, (int) (item.getStackSize() - (hideOnePerStack ? 1 : 0)));
			}
		}
		return result;
	}

	@Override
	public @Nonnull ItemStack getSingleItem(ItemIdentifier item) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null) {
			return ItemStack.EMPTY;
		}
		IAEItemStack stack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(item.makeNormalStack(1));
		IAEItemStack extract = tmp.getInventory(channel).extractItems(stack, Actionable.MODULATE, source);
		if (extract == null) {
			return ItemStack.EMPTY;
		}
		return extract.createItemStack();
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null || tmp.getInventory(channel).getStorageList() == null) {
			return false;
		}
		IItemList<IAEItemStack> items = tmp.getInventory(channel).getStorageList();
		for (IAEItemStack item : items) {
			ItemIdentifier ident = ItemIdentifier.get(item.createItemStack());
			if (ident.equals(itemIdent)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, item.getMaxStackSize());
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null) {
			return 0;
		}
		while (count > 0) {
			IAEItemStack stack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(itemIdent.makeNormalStack(count));
			if (tmp.getInventory(channel).canAccept(stack)) {
				return count;
			}
			count--;
		}
		return 0;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		Set<ItemIdentifier> result = new TreeSet<>();
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null || tmp.getInventory(channel).getStorageList() == null) {
			return result;
		}
		IItemList<IAEItemStack> items = tmp.getInventory(channel).getStorageList();
		for (IAEItemStack item : items) {
			ItemIdentifier ident = ItemIdentifier.get(item.createItemStack());
			result.add(ident);
		}
		return result;
	}

	@Override
	public int getSizeInventory() {
		if (cached == null) {
			initCache();
		}
		return cached.size();
	}

	public void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<>();
		cached.addAll(map.entrySet());
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int i) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		if (entry.getValue() == 0) {
			return ItemStack.EMPTY;
		}
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int i, int j) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		ItemStack extracted = getMultipleItems(entry.getKey(), j);
		entry.setValue(entry.getValue() - j);
		return extracted;
	}

	@Override
	public ItemStack add(ItemStack stack, EnumFacing from, boolean doAdd) {
		ItemStack st = stack.copy();
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IAEItemStack tst = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null) {
			return st;
		}
		IAEItemStack overflow = tmp.getInventory(channel).injectItems(tst, Actionable.MODULATE, source);
		if (overflow != null) {
			st.setCount((int) (st.getCount() - overflow.getStackSize()));
		}
		return st;
	}
}

class LPStorageMonitorableAccessor implements ICapabilitySerializable<NBTBase> {

	@CapabilityInject(IStorageMonitorableAccessor.class)
	public static final Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR_CAPABILITY = null;

	private final IStorageMonitorableAccessor instance = STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.getDefaultInstance();

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == STORAGE_MONITORABLE_ACCESSOR_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == STORAGE_MONITORABLE_ACCESSOR_CAPABILITY ? STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.getStorage().writeNBT(STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.getStorage().readNBT(STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, this.instance, null, nbt);
	}
}

class LPActionSource implements IActionSource {

	final IGridHost host;

	public LPActionSource(AEInterfaceInventoryHandler invh) {
		host = invh.host;
	}

	@Nonnull
	@Override
	public Optional<EntityPlayer> player() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Optional<IActionHost> machine() {
		return Optional.ofNullable((IActionHost) this.host);
	}

	@Nonnull
	@Override
	public <T> Optional<T> context(@Nonnull Class<T> key) {
		return Optional.empty();
	}
}

