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
import javax.annotation.Nullable;

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
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;

import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.inventory.ProviderMode;

public class AEInterfaceInventoryHandler extends SpecialInventoryHandler implements SpecialInventoryHandler.Factory {

	public boolean init = false;
	private final LPActionSource source;
	private IStorageMonitorableAccessor acc;
	IGridHost host;
	public IGridNode node;
	private LinkedList<Entry<ItemIdentifier, Integer>> cached;
	private final boolean hideOne;

	private AEInterfaceInventoryHandler(TileEntity tile, EnumFacing dir, ProviderMode mode) {
		hideOne = mode.getHideOnePerStack() || mode.getHideOnePerType();
		this.acc = tile.getCapability(LPStorageMonitorableAccessor.STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, dir);
		node = ((IGridHost) tile).getGridNode(AEPartLocation.fromFacing(dir));
		host = node.getMachine();
		source = new LPActionSource(this);
	}

	public AEInterfaceInventoryHandler() {
		source = null;
		hideOne = false;
	}

	@Override
	public boolean isType(@Nonnull TileEntity tile, @Nullable EnumFacing dir) {
		if (tile instanceof IGridHost && tile.hasCapability(LPStorageMonitorableAccessor.STORAGE_MONITORABLE_ACCESSOR_CAPABILITY, dir)) {
			// for some reason when AE loads (5 ticks) this is null
			return ((IGridHost) tile).getGridNode(AEPartLocation.fromFacing(dir)) != null;
		}
		return false;
	}

	@Override
	public boolean init() {
		init = true;
		return true;
	}

	@Nonnull
	@Override
	public SpecialInventoryHandler getUtilForTile(@Nonnull TileEntity tile, @Nullable EnumFacing direction, @Nonnull ProviderMode mode) {
		return new AEInterfaceInventoryHandler(tile, direction, mode);
	}

	@Override
	@Nonnull
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
		if (tmp == null || tmp.getInventory(channel) == null || tmp.getInventory(channel).getStorageList() == null) {
			return result;
		}

		IItemList<IAEItemStack> items = tmp.getInventory(channel).getStorageList();
		for (IAEItemStack item : items) {
			ItemIdentifier ident = ItemIdentifier.get(item.createItemStack());
			Integer count = result.get(ident);
			if (count != null) {
				result.put(ident, (int) (count + item.getStackSize() - (hideOne ? 1 : 0)));
			} else {
				result.put(ident, (int) (item.getStackSize() - (hideOne ? 1 : 0)));
			}
		}
		return result;
	}

	@Override
	@Nonnull
	public ItemStack getSingleItem(ItemIdentifier item) {
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
	@Nonnull
	public ItemStack getMultipleItems(@Nonnull ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) {
			return ItemStack.EMPTY;
		}
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null) {
			return ItemStack.EMPTY;
		}
		IAEItemStack stack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(itemIdent.makeNormalStack(count));
		IAEItemStack extract = tmp.getInventory(channel).extractItems(stack, Actionable.MODULATE, source);
		if (extract == null) {
			return ItemStack.EMPTY;
		}
		return extract.createItemStack();
	}

	@Override
	public boolean containsUndamagedItem(@Nonnull ItemIdentifier itemIdent) {
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
	public int roomForItem(@Nonnull ItemStack itemStack) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageMonitorable tmp = acc.getInventory(source);
		if (tmp == null || tmp.getInventory(channel) == null) {
			return 0;
		}
		IAEItemStack stack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(itemStack);
		if (stack == null) return 0;
		while (stack.getStackSize() > 0) {
			if (tmp.getInventory(channel).canAccept(stack)) {
				return stack.getStackSize() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) stack.getStackSize();
			}
			stack.decStackSize(1);
		}
		return 0;
	}

	@Override
	@Nonnull
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

		// allow LP putting items into AE
		return cached.size() + 1;
	}

	private void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<>();
		cached.addAll(map.entrySet());
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		if (cached == null) {
			initCache();
		}
		if (slot >= cached.size()) {
			return ItemStack.EMPTY;
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(slot);
		if (entry.getValue() == 0) {
			return ItemStack.EMPTY;
		}
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int slot, int amount) {
		if (cached == null) {
			initCache();
		}
		if (slot >= cached.size()) {
			return ItemStack.EMPTY;
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(slot);
		return getMultipleItems(entry.getKey(), amount);
	}

	@Override
	@Nonnull
	public ItemStack add(@Nonnull ItemStack stack, EnumFacing from, boolean doAdd) {
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
	public static Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR_CAPABILITY;

	private final IStorageMonitorableAccessor instance = STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.getDefaultInstance();

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == STORAGE_MONITORABLE_ACCESSOR_CAPABILITY;
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		return capability == STORAGE_MONITORABLE_ACCESSOR_CAPABILITY ? STORAGE_MONITORABLE_ACCESSOR_CAPABILITY.cast(this.instance) : null;
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

