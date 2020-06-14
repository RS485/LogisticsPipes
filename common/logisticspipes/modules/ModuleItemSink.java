package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.gui.hud.modules.HUDItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.module.inhand.ItemSinkInHand;
import logisticspipes.network.guis.module.inpipe.ItemSinkSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.ItemSinkDefault;
import logisticspipes.network.packets.modules.ItemSinkFuzzy;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SimpleFilter;

@CCType(name = "ItemSink Module")
public class ModuleItemSink extends LogisticsModule implements SimpleFilter, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive, Gui {

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Requested items", 1);
	private boolean _isDefaultRoute;

	private BitSet ignoreData = new BitSet(_filterInventory.getSizeInventory());
	private BitSet ignoreNBT = new BitSet(_filterInventory.getSizeInventory());

	private IHUDModuleRenderer HUD = new HUDItemSink(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleItemSink() {
		_filterInventory.addListener(this);
	}

	public static String getName() {
		return "item_sink";
	}

	@Override
	@CCCommand(description = "Returns the FilterInventory of this Module")
	@Nonnull
	public ItemIdentifierInventory getFilterInventory() {
		return _filterInventory;
	}

	@CCCommand(description = "Returns true if the module is a default route")
	public boolean isDefaultRoute() {
		return _isDefaultRoute;
	}

	@CCCommand(description = "Sets the default route status of this module")
	public void setDefaultRoute(Boolean isDefaultRoute) {
		_isDefaultRoute = isDefaultRoute;
		if (!localModeWatchers.isEmpty()) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ItemSinkDefault.class).setFlag(_isDefaultRoute).setModulePos(this), localModeWatchers);
		}
	}

	private SinkReply _sinkReply;
	private SinkReply _sinkReplyDefault;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
		_sinkReplyDefault = new SinkReply(FixedPriority.DefaultRoute, 0, true, true, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (_isDefaultRoute && !allowDefault) {
			return null;
		}
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		if (_filterInventory.containsUndamagedItem(item.getUndamaged())) {
			if (_service.canUseEnergy(1)) {
				return _sinkReply;
			}
			return null;
		}
		final ISlotUpgradeManager upgradeManager = getUpgradeManager();
		if (upgradeManager != null && upgradeManager.isFuzzyUpgrade()) {
			for (Pair<ItemIdentifierStack, Integer> filter : _filterInventory) {
				if (filter == null) {
					continue;
				}
				if (filter.getValue1() == null) {
					continue;
				}
				ItemIdentifier ident1 = item;
				ItemIdentifier ident2 = filter.getValue1().getItem();
				if (ignoreData.get(filter.getValue2())) {
					ident1 = ident1.getIgnoringData();
					ident2 = ident2.getIgnoringData();
				}
				if (ignoreNBT.get(filter.getValue2())) {
					ident1 = ident1.getIgnoringNBT();
					ident2 = ident2.getIgnoringNBT();
				}
				if (ident1.equals(ident2)) {
					if (_service.canUseEnergy(5)) {
						return _sinkReply;
					}
					return null;
				}
			}
		}
		if (_isDefaultRoute) {
			if (bestPriority > _sinkReplyDefault.fixedPriority.ordinal() || (bestPriority == _sinkReplyDefault.fixedPriority.ordinal() && bestCustomPriority >= _sinkReplyDefault.customPriority)) {
				return null;
			}
			if (_service.canUseEnergy(1)) {
				return _sinkReplyDefault;
			}
			return null;
		}
		return null;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		_isDefaultRoute = nbttagcompound.getBoolean("defaultdestination");
		if (nbttagcompound.hasKey("ignoreData")) {
			ignoreData = BitSet.valueOf(nbttagcompound.getByteArray("ignoreData"));
			ignoreNBT = BitSet.valueOf(nbttagcompound.getByteArray("ignoreNBT"));
		}
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("defaultdestination", isDefaultRoute());
		nbttagcompound.setByteArray("ignoreData", ignoreData.toByteArray());
		nbttagcompound.setByteArray("ignoreNBT", ignoreNBT.toByteArray());
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Default: " + (isDefaultRoute() ? "Yes" : "No"));
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ItemSinkDefault.class).setFlag(_isDefaultRoute).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (MainProxy.isServer(_world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return _isDefaultRoute;
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		if (_isDefaultRoute) {
			return;
		}
		Map<ItemIdentifier, Integer> mapIC = _filterInventory.getItemsAndCount();
		itemidCollection.addAll(mapIC.keySet());
		mapIC.keySet().stream().map(ItemIdentifier::getUndamaged).forEach(itemidCollection::add);
		if (getUpgradeManager() != null && getUpgradeManager().isFuzzyUpgrade()) {
			for (Pair<ItemIdentifierStack, Integer> stack : _filterInventory) {
				if (stack.getValue1() == null) {
					continue;
				}
				ItemIdentifier ident = stack.getValue1().getItem();
				if (ignoreData.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringData());
				}
				if (ignoreNBT.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringNBT());
				}
				if (ignoreData.get(stack.getValue2()) && ignoreNBT.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringData().getIgnoringNBT());
				}
			}
		}
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	public void setIgnoreData(BitSet ignoreData) {
		this.ignoreData = ignoreData;
	}

	public void setIgnoreNBT(BitSet ignoreNBT) {
		this.ignoreNBT = ignoreNBT;
	}

	public boolean isIgnoreData(int pos) {
		return ignoreData.get(pos);
	}

	public boolean isIgnoreNBT(int pos) {
		return ignoreNBT.get(pos);
	}

	public void setIgnoreData(int slot, EntityPlayer player) {
		if (slot < 0 || slot >= 9) {
			return;
		}
		if (MainProxy.isClient(_world.getWorld())) {
			if (player == null) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkFuzzy.class).setPos(slot).setNBT(false).setModulePos(this));
			}
		} else {
			sendIgnoreUpdate(slot, player, ignoreData);
		}
	}

	public void setIgnoreNBT(int slot, EntityPlayer player) {
		if (slot < 0 || slot >= 9) {
			return;
		}
		if (MainProxy.isClient(_world.getWorld())) {
			if (player == null) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkFuzzy.class).setPos(slot).setNBT(true).setModulePos(this));
			}
		} else {
			sendIgnoreUpdate(slot, player, ignoreNBT);
		}
	}

	public void sendIgnoreUpdate(int slot, @Nullable EntityPlayer player, @Nonnull BitSet ignoreNBT) {
		ignoreNBT.set(slot, !ignoreNBT.get(slot));
		ModernPacket pak = PacketHandler.getPacket(ItemSinkFuzzy.class).setIgnoreData(ignoreData).setIgnoreNBT(ignoreNBT).setModulePos(this);
		if (player != null) {
			MainProxy.sendPacketToPlayer(pak, player);
		}
		MainProxy.sendPacketToAllWatchingChunk(this, pak);
	}

	public void importFromInventory() {
		if (_service == null) {
			return;
		}
		IInventoryUtil inv = _service.getPointedInventory();
		if (inv == null) {
			return;
		}
		int count = 0;
		for (ItemIdentifier item : inv.getItems()) {
			_filterInventory.setInventorySlotContents(count, item.makeStack(1));
			count++;
			if (count >= _filterInventory.getSizeInventory()) {
				break;
			}
		}
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkSlot.class).setDefaultRoute(_isDefaultRoute).setIgnoreData(ignoreData).setIgnoreNBT(ignoreNBT).setHasFuzzyUpgrade(getUpgradeManager().isFuzzyUpgrade());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkInHand.class);
	}

}
