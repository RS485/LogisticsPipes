package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import logisticspipes.gui.hud.modules.HUDSimpleFilterModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
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
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SimpleFilter;
import network.rs485.logisticspipes.property.ItemIdentifierInventoryProperty;
import network.rs485.logisticspipes.property.Property;

@CCType(name = "Terminus Module")
public class ModuleTerminus extends LogisticsModule
		implements SimpleFilter, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver,
		ISimpleInventoryEventHandler, IModuleInventoryReceive, Gui {

	public final ItemIdentifierInventoryProperty filterInventory = new ItemIdentifierInventoryProperty(
			new ItemIdentifierInventory(9, "Terminated items", 1), "");

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final IHUDModuleRenderer HUD = new HUDSimpleFilterModule(this);
	private SinkReply _sinkReply;

	public ModuleTerminus() {
		filterInventory.addListener(this);
	}

	public static String getName() {
		return "terminus";
	}

	@Nonnull
	@Override
	public String getLPName() { return getName(); }

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(filterInventory);
	}

	@Override
	@CCCommand(description = "Returns the FilterInventory of this Module")
	@Nonnull
	public IItemIdentifierInventory getFilterInventory() {
		return filterInventory;
	}

	@Override
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.Terminus, 0, true, false, 2, 0,
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
		if (filterInventory.containsUndamagedItem(item.getUndamaged())) {
			if (service.canUseEnergy(2)) {
				return _sinkReply;
			}
		}

		return null;
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull
	List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Terminated: ");
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
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class)
						.setIdentList(ItemIdentifierStack.getListFromInventory(filterInventory)).setModulePos(this),
				localModeWatchers);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		MainProxy.runOnServer(getWorld(), () -> () ->
				MainProxy.sendToPlayerList(
						PacketHandler.getPacket(ModuleInventory.class)
								.setIdentList(ItemIdentifierStack.getListFromInventory(inventory))
								.setModulePos(this),
						localModeWatchers
				)
		);
	}

	@Override
	public void handleInvContent(@Nonnull Collection<ItemIdentifierStack> list) {
		filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemIdentifiers) {
		Set<ItemIdentifier> filterItemIds = filterInventory.getItemsAndCount().keySet();
		itemIdentifiers.addAll(filterItemIds);
		filterItemIds.stream().map(ItemIdentifier::getUndamaged).forEach(itemIdentifiers::add);
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
	public boolean receivePassive() {
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
