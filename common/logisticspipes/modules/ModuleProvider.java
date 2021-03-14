package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.gui.hud.modules.HUDProviderModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logistics.LogisticsManager;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.inventory.ProviderMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ProviderModuleInHand;
import logisticspipes.network.guis.module.inpipe.ProviderModuleGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.SneakyModuleDirectionUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SneakyDirection;

@CCType(name = "Provider Module")
public class ModuleProvider extends LogisticsModule implements SneakyDirection, ILegacyActiveModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive, Gui {

	public final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Items to provide (or empty for all)", 1);
	private EnumFacing _sneakyDirection = null;

	private boolean isActive = false;

	private final Map<ItemIdentifier, Integer> displayMap = new TreeMap<>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<>();
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public boolean isExcludeFilter = false;
	public ProviderMode providerMode = ProviderMode.DEFAULT;
	private final IHUDModuleRenderer HUD = new HUDProviderModule(this);

	public ModuleProvider() {}

	public static String getName() {
		return "provider";
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		if (nbttagcompound.hasKey("isActive")) {
			isActive = nbttagcompound.getBoolean("isActive");
		}
		isExcludeFilter = nbttagcompound.getBoolean("filterisexclude");
		providerMode = ProviderMode.modeFromIntSafe(nbttagcompound.getInteger("extractionMode"));
		_sneakyDirection = SneakyDirection.readSneakyDirection(nbttagcompound);
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("isActive", isActive);
		nbttagcompound.setBoolean("filterisexclude", isExcludeFilter);
		nbttagcompound.setInteger("extractionMode", providerMode.ordinal());
		SneakyDirection.writeSneakyDirection(_sneakyDirection, nbttagcompound);
	}

	@Override
	public EnumFacing getSneakyDirection() {
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(EnumFacing sneakyDirection) {
		_sneakyDirection = sneakyDirection;
		if (MainProxy.isServer(this._world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(SneakyModuleDirectionUpdate.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
		}
	}

	protected int neededEnergy() {
		return (int) (1 * Math.pow(1.1, getUpgradeManager().getItemExtractionUpgrade()) * Math.pow(1.2, getUpgradeManager().getItemStackExtractionUpgrade()));
	}

	protected int itemsToExtract() {
		return 8 * (int) Math.pow(2, getUpgradeManager().getItemExtractionUpgrade());
	}

	protected int stacksToExtract() {
		return 1 + getUpgradeManager().getItemStackExtractionUpgrade();
	}

	public ItemSendMode itemSendMode() {
		return getUpgradeManager().getItemExtractionUpgrade() > 0 ? ItemSendMode.Fast : ItemSendMode.Normal;
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		return null;
	}

	@Override
	public void tick() {
		if (_service.isNthTick(6)) {
			checkUpdate(null);
		}
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		LogisticsItemOrder firstOrder = null;
		LogisticsItemOrder order = null;
		while (itemsleft > 0 && stacksleft > 0 && _service.getItemOrderManager().hasOrders(ResourceType.PROVIDER) && (firstOrder == null || firstOrder != order)) {
			if (firstOrder == null) {
				firstOrder = order;
			}
			order = _service.getItemOrderManager().peekAtTopRequest(ResourceType.PROVIDER);
			int sent = sendStack(order.getResource().stack, itemsleft, order.getDestination().getRouter().getSimpleID(), order.getInformation());
			if (sent < 0) {
				break;
			}
			_service.spawnParticle(Particles.VioletParticle, 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	public boolean filterBlocksItem(ItemIdentifier item) {
		if (!hasFilter()) {
			return false;
		}
		boolean isFiltered = itemIsFiltered(item);
		return isExcludeFilter == isFiltered;
	}

	@Override
	public void onBlockRemoval() {
		while (_service.getItemOrderManager().hasOrders(ResourceType.PROVIDER)) {
			_service.getItemOrderManager().sendFailed();
		}
	}

	@Nonnull
	public Stream<IInventoryUtil> inventoriesWithMode() {
		return _service.getAvailableAdjacent().inventories().stream()
				.map(this::getInventoryUtilWithMode)
				.filter(Objects::nonNull);
	}

	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {
		List<ItemIdentifier> possible = new ArrayList<>();
		if (tree.getRequestType() instanceof ItemResource) {
			possible.add(((ItemResource) tree.getRequestType()).getItem());
		} else if (tree.getRequestType() instanceof DictResource) {
			possible.addAll(
					inventoriesWithMode()
							.map(IInventoryUtil::getItemsAndCount)
							.flatMap(inventory -> inventory.keySet().stream())
							.filter(item -> tree.getRequestType().matches(item, IResource.MatchSettings.NORMAL))
							.collect(Collectors.toList())
			);
		}
		for (ItemIdentifier item : possible) {
			int canProvide = getAvailableItemCount(item);
			canProvide -= root.getAllPromissesFor((IProvideItems) _service, item);
			canProvide = Math.min(canProvide, tree.getMissingAmount());
			if (canProvide < 1) {
				return;
			}
			LogisticsPromise promise = new LogisticsPromise(item, canProvide, (IProvideItems) _service, ResourceType.PROVIDER);
			tree.addPromise(promise);
		}
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		_service.spawnParticle(Particles.WhiteParticle, 2);
		return _service.getItemOrderManager().addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, ResourceType.PROVIDER, info);
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		return getTotalItemCount(item) - _service.getItemOrderManager().totalItemsCountInOrders(item);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		items.putAll(
				inventoriesWithMode()
						.map(IInventoryUtil::getItemsAndCount)
						.flatMap(inventory -> inventory.entrySet().stream())
						.filter(item -> {
							if (items.containsKey(item.getKey())) return false; // already provided by any previous module. No comparison of the amount
							if (filterBlocksItem(item.getKey())) return false; // skip provider-filtered items
							final boolean blockedInFilters = filters.stream().anyMatch(
									filter -> filter.isBlocked() == filter.isFilteredItem(item.getKey().getUndamaged()) || filter.blockProvider()
							);
							return !blockedInFilters; // skip filters-parameter-filtered items
						})
						.map(item -> new Pair<>(item.getKey(), item.getValue() - _service.getItemOrderManager().totalItemsCountInOrders(item.getKey())))
						.filter(itemIdentAndRemaining -> itemIdentAndRemaining.getValue2() > 0) // reduce what has been reserved
						.collect(Pair.toMap(Integer::sum)) // sum up the provided amount by the inventories
		);
	}

	// returns -1 on permanently failed, don't try another stack this tick
	// returns 0 on "unable to do this delivery"
	public int sendStack(ItemIdentifierStack stack, int maxCount, int destination, IAdditionalTargetInformation info) {
		ItemIdentifier item = stack.getItem();

		Iterator<Pair<IInventoryUtil, EnumFacing>> iterator = _service.getAvailableAdjacent().inventories().stream().flatMap(neighbor -> {
			final IInventoryUtil invUtil = getInventoryUtilWithMode(neighbor);
			if (invUtil == null) return Stream.empty();
			return Stream.of(new Pair<>(invUtil, neighbor.getDirection()));
		}).iterator();

		while (iterator.hasNext()) {
			final Pair<IInventoryUtil, EnumFacing> current = iterator.next();
			int available = current.getValue1().itemCount(item);
			if (available == 0) {
				continue;
			}

			int wanted = Math.min(available, stack.getStackSize());
			wanted = Math.min(wanted, maxCount);
			wanted = Math.min(wanted, item.getMaxStackSize());
			IRouter dRtr = SimpleServiceLocator.routerManager.getServerRouter(destination);
			if (dRtr == null) {
				_service.getItemOrderManager().sendFailed();
				return 0;
			}
			SinkReply reply = LogisticsManager.canSink(stack.makeNormalStack(), dRtr, null, true, stack.getItem(), null, true, false);
			boolean defersend = false;
			if (reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
				if (reply.maxNumberOfItems < wanted) {
					wanted = reply.maxNumberOfItems;
					if (wanted <= 0) {
						_service.getItemOrderManager().deferSend();
						return 0;
					}
					defersend = true;
				}
			}
			if (!_service.canUseEnergy(wanted * neededEnergy())) {
				return -1;
			}
			ItemStack removed = current.getValue1().getMultipleItems(item, wanted);
			if (removed.isEmpty()) {
				continue;
			}
			int sent = removed.getCount();
			_service.useEnergy(sent * neededEnergy());

			final IRoutedItem routedItem = _service.sendStack(removed, destination, itemSendMode(), info, current.getValue2());
			_service.getItemOrderManager().sendSuccessfull(sent, defersend, routedItem);
			return sent;
		}

		_service.getItemOrderManager().sendFailed();
		return 0;
	}

	public int getTotalItemCount(final ItemIdentifier item) {
		if (filterBlocksItem(item)) return 0;
		return inventoriesWithMode().map(invUtil -> invUtil.itemCount(item)).reduce(Integer::sum).orElse(0);
	}

	public boolean hasFilter() {
		return !_filterInventory.isEmpty();
	}

	private boolean itemIsFiltered(ItemIdentifier item) {
		return _filterInventory.containsItem(item);
	}

	/*** GUI STUFF ***/

	@CCCommand(description = "Returns the FilterInventory of this Module")
	public IInventory getFilterInventory() {
		return _filterInventory;
	}

	public boolean isExcludeFilter() {
		return isExcludeFilter;
	}

	public void setFilterExcluded(boolean isExcludeFilter) {
		this.isExcludeFilter = isExcludeFilter;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public ProviderMode getExtractionMode() {
		return providerMode;
	}

	public void setExtractionMode(int id) {
		providerMode = ProviderMode.modeFromIntSafe(id);
	}

	public void nextExtractionMode() {
		providerMode = ProviderMode.modeFromIntSafe(providerMode.ordinal() + 1);
	}

	@Override
	public @Nonnull List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add(!isExcludeFilter ? "Included" : "Excluded");
		list.add("Mode: " + providerMode.name());
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	private void checkUpdate(EntityPlayer player) {
		if (localModeWatchers.size() == 0 && player == null) {
			return;
		}
		displayList.clear();
		displayMap.clear();
		getAllItems(displayMap, new ArrayList<>(0));
		displayList.ensureCapacity(displayMap.size());
		displayList.addAll(displayMap.entrySet().stream()
				.map(item -> new ItemIdentifierStack(item.getKey(), item.getValue()))
				.collect(Collectors.toList()));
		if (!oldList.equals(displayList)) {
			oldList.clear();
			oldList.ensureCapacity(displayList.size());
			oldList.addAll(displayList);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(displayList).setModulePos(this).setCompressable(true), localModeWatchers);
		} else if (player != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(displayList).setModulePos(this).setCompressable(true), player);
		}
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
		checkUpdate(player);
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
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		//when filter is empty or in exclude mode, this is interested in attached inventory already
		if (!isExcludeFilter && !_filterInventory.isEmpty()) {
			// when items included this is only interested in items in the filter
			itemidCollection.addAll(_filterInventory.getItemsAndCount().keySet());
		}
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return isExcludeFilter || _filterInventory.isEmpty(); // when items included this is only interested in items in the filter
		// when items not included, we can only serve those items in the filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleGuiProvider.class).setExtractorMode(getExtractionMode().ordinal()).setExclude(isExcludeFilter);
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleInHand.class);
	}

	private IInventoryUtil getInventoryUtilWithMode(NeighborTileEntity<TileEntity> neighbor) {
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(neighbor.getTileEntity(), neighbor.getOurDirection(), providerMode);
	}
}
