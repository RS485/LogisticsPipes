package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;

import logisticspipes.gui.hud.modules.HUDItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ItemSinkInHand;
import logisticspipes.network.guis.module.inpipe.ItemSinkSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.ItemSinkDefault;
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
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SimpleFilter;
import network.rs485.logisticspipes.property.BitSetProperty;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;

@CCType(name = "ItemSink Module")
public class ModuleItemSink extends LogisticsModule
		implements SimpleFilter, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver,
		ISimpleInventoryEventHandler, IModuleInventoryReceive, Gui {

	public final InventoryProperty filterInventory = new InventoryProperty(
			new ItemIdentifierInventory(9, "Requested items", 1), "");
	public final BooleanProperty defaultRoute = new BooleanProperty(false, "defaultdestination");
	public final BitSetProperty ignoreData = new BitSetProperty(
			new BitSet(filterInventory.getSizeInventory()), "ignoreData");
	public final BitSetProperty ignoreNBT = new BitSetProperty(
			new BitSet(filterInventory.getSizeInventory()), "ignoreNBT");

	private final List<Property<?>> properties = ImmutableList.<Property<?>>builder()
			.add(filterInventory)
			.add(defaultRoute)
			.add(ignoreData)
			.add(ignoreNBT)
			.build();

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final IHUDModuleRenderer HUD = new HUDItemSink(this);
	private SinkReply _sinkReply;
	private SinkReply _sinkReplyDefault;

	public ModuleItemSink() {
		filterInventory.addListener(this);
	}

	public static String getName() {
		return "item_sink";
	}

	@Nonnull
	@Override
	public String getLPName() {
		return getName();
	}

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return properties;
	}

	@Override
	@CCCommand(description = "Returns the FilterInventory of this Module")
	@Nonnull
	public IItemIdentifierInventory getFilterInventory() {
		return filterInventory;
	}

	@CCCommand(description = "Returns true if the module is a default route")
	public boolean isDefaultRoute() {
		return defaultRoute.getValue();
	}

	@CCCommand(description = "Sets the default route status of this module")
	public void setDefaultRoute(Boolean isDefaultRoute) {
		defaultRoute.setValue(isDefaultRoute);
		if (!localModeWatchers.isEmpty()) {
			MainProxy.sendToPlayerList(
					PacketHandler.getPacket(ItemSinkDefault.class).setFlag(isDefaultRoute).setModulePos(this),
					localModeWatchers);
		}
	}

	@Override
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0,
				new ChassiTargetInformation(getPositionInt()));
		_sinkReplyDefault = new SinkReply(FixedPriority.DefaultRoute, 0, true, true, 1, 0,
				new ChassiTargetInformation(getPositionInt()));
	}

	public Stream<ItemIdentifier> getAdjacentInventoriesItems() {
		return Objects.requireNonNull(_service)
				.getAvailableAdjacent()
				.inventories()
				.stream()
				.map(LPNeighborTileEntityKt::getInventoryUtil)
				.filter(Objects::nonNull)
				.flatMap(invUtil -> invUtil.getItems().stream())
				.distinct();
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (defaultRoute.getValue() && !allowDefault) {
			return null;
		}
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final IPipeServiceProvider service = _service;
		if (service == null) return null;
		if (filterInventory.containsUndamagedItem(item.getUndamaged())) {
			if (service.canUseEnergy(1)) {
				return _sinkReply;
			}
			return null;
		}
		final ISlotUpgradeManager upgradeManager = getUpgradeManager();
		if (upgradeManager.isFuzzyUpgrade()) {
			for (Pair<ItemIdentifierStack, Integer> filter : filterInventory.contents()) {
				if (filter == null) {
					continue;
				}
				if (filter.getValue1() == null) {
					continue;
				}
				ItemIdentifier ident1 = item;
				ItemIdentifier ident2 = filter.getValue1().getItem();
				if (ignoreData.get(filter.getValue2())) {
					ident1 = ident1.getIgnoringDamage();
					ident2 = ident2.getIgnoringDamage();
				}
				if (ignoreNBT.get(filter.getValue2())) {
					ident1 = ident1.getIgnoringNBT();
					ident2 = ident2.getIgnoringNBT();
				}
				if (ident1.equals(ident2)) {
					if (service.canUseEnergy(5)) {
						return _sinkReply;
					}
					return null;
				}
			}
		}
		if (defaultRoute.getValue()) {
			if (bestPriority > _sinkReplyDefault.fixedPriority.ordinal() || (
					bestPriority == _sinkReplyDefault.fixedPriority.ordinal()
							&& bestCustomPriority >= _sinkReplyDefault.customPriority)) {
				return null;
			}
			if (service.canUseEnergy(1)) {
				return _sinkReplyDefault;
			}
			return null;
		}
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull
	List<String> getClientInformation() {
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class)
				.setIdentList(ItemIdentifierStack.getListFromInventory(filterInventory)).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(
				PacketHandler.getPacket(ItemSinkDefault.class).setFlag(defaultRoute.getValue()).setModulePos(this), player);
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
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(@Nonnull Collection<ItemIdentifierStack> list) {
		filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return defaultRoute.getValue();
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		if (defaultRoute.getValue()) {
			return;
		}
		Map<ItemIdentifier, Integer> mapIC = filterInventory.getItemsAndCount();
		itemidCollection.addAll(mapIC.keySet());
		mapIC.keySet().stream().map(ItemIdentifier::getUndamaged).forEach(itemidCollection::add);
		if (getUpgradeManager().isFuzzyUpgrade()) {
			for (Pair<ItemIdentifierStack, Integer> stack : filterInventory.contents()) {
				if (stack.getValue1() == null) {
					continue;
				}
				ItemIdentifier ident = stack.getValue1().getItem();
				if (ignoreData.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringDamage());
				}
				if (ignoreNBT.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringNBT());
				}
				if (ignoreData.get(stack.getValue2()) && ignoreNBT.get(stack.getValue2())) {
					itemidCollection.add(ident.getIgnoringDamage().getIgnoringNBT());
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
		this.ignoreData.replaceWith(ignoreData);
	}

	public void setIgnoreNBT(BitSet ignoreNBT) {
		this.ignoreNBT.replaceWith(ignoreNBT);
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkSlot.class).setDefaultRoute(defaultRoute.getValue())
				.setIgnoreData(ignoreData.copyValue()).setIgnoreNBT(ignoreNBT.copyValue())
				.setHasFuzzyUpgrade(getUpgradeManager().isFuzzyUpgrade());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkInHand.class);
	}

}
