package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import logisticspipes.gui.hud.modules.HUDSimpleFilterModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;
import network.rs485.logisticspipes.module.PropertyModule;
import network.rs485.logisticspipes.module.SimpleFilter;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;

public class ModulePassiveSupplier extends PropertyModule
		implements Gui, SimpleFilter, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver,
		IModuleInventoryReceive, ISimpleInventoryEventHandler {

	private final InventoryProperty filterInventory = new InventoryProperty(
			new ItemIdentifierInventory(9, "Requested items", 64), "");

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final IHUDModuleRenderer HUD = new HUDSimpleFilterModule(this);
	private SinkReply _sinkReply;

	public ModulePassiveSupplier() {
		filterInventory.addListener(this);
	}

	public static String getName() {
		return "passive_supplier";
	}

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(filterInventory);
	}

	@Override
	@Nonnull
	public IInventory getFilterInventory() {
		return filterInventory;
	}

	@Override
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.PassiveSupplier, 0, true, false, 2, 0,
				new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}

		final IPipeServiceProvider service = _service;
		if (service == null) return null;
		final ISlotUpgradeManager upgradeManager = service.getUpgradeManager(slot, positionInt);
		IInventoryUtil targetUtil = PipeServiceProviderUtilKt.availableSneakyInventories(service, upgradeManager)
				.stream().findFirst().orElse(null);
		if (targetUtil == null) {
			return null;
		}

		if (!filterInventory.containsItem(item)) {
			return null;
		}

		int targetCount = filterInventory.itemCount(item);
		int haveCount = targetUtil.itemCount(item);
		if (targetCount <= haveCount) {
			return null;
		}

		if (service.canUseEnergy(2)) {
			return new SinkReply(_sinkReply, targetCount - haveCount);
		}
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull
	List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Supplied: ");
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class)
				.setIdentList(ItemIdentifierStack.getListFromInventory(filterInventory)).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(@Nonnull Collection<ItemIdentifierStack> list) {
		filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		MainProxy.runOnServer(getWorld(), () -> () ->
				MainProxy.sendToPlayerList(
						PacketHandler.getPacket(ModuleInventory.class)
								.setIdentList(ItemIdentifierStack.getListFromInventory(filterInventory))
								.setModulePos(this),
						localModeWatchers
				)
		);
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		itemidCollection.addAll(filterInventory.getItemsAndCount().keySet());
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return SimpleFilter.getPipeGuiProvider();
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return SimpleFilter.getInHandGuiProvider();
	}

}
