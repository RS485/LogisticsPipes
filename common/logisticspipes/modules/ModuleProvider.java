package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import logisticspipes.gui.hud.modules.HUDProviderModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.ItemRequestProvider;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.logistics.LogisticsManagerImpl;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ProviderModuleInHand;
import logisticspipes.network.guis.module.inpipe.ProviderModuleGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;

@CCType(name = "Provider Module")
public class ModuleProvider extends LogisticsSneakyDirectionModule implements ILegacyActiveModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive {

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Items to provide (or empty for all)", 1);
	private Direction _sneakyDirection = null;

	private boolean isActive = false;

	protected final int ticksToActiveAction = 6;
	protected final int ticksToPassiveAction = 100;
	private final Map<ItemIdentifier, Integer> displayMap = new TreeMap<>();
	public final ArrayList<ItemStack> displayList = new ArrayList<>();
	private final ArrayList<ItemStack> oldList = new ArrayList<>();
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	protected int currentTick = 0;
	protected boolean isExcludeFilter = false;
	protected ExtractionMode _extractionMode = ExtractionMode.Normal;
	private IHUDModuleRenderer HUD = new HUDProviderModule(this);

	public ModuleProvider() {}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		isActive = nbttagcompound.getBoolean("isActive");
		isExcludeFilter = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.getMode(nbttagcompound.getInteger("extractionMode"));
		if (nbttagcompound.hasKey("sneakydirection")) {
			_sneakyDirection = Direction.values()[nbttagcompound.getInteger("sneakydirection")];
		} else if (nbttagcompound.hasKey("sneakyorientation")) {
			// convert sneakyorientation to sneakydirection
			int t = nbttagcompound.getInteger("sneakyorientation");
			switch (t) {
				default:
				case 0:
					_sneakyDirection = null;
					break;
				case 1:
					_sneakyDirection = Direction.UP;
					break;
				case 2:
					_sneakyDirection = Direction.SOUTH;
					break;
				case 3:
					_sneakyDirection = Direction.DOWN;
					break;
			}
		}

	}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("isActive", isActive);
		nbttagcompound.setBoolean("filterisexclude", isExcludeFilter);
		nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());
		if (_sneakyDirection != null) {
			nbttagcompound.setInteger("sneakydirection", _sneakyDirection.ordinal());
		}
	}

	@Override
	public Direction getSneakyDirection() {
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(Direction sneakyDirection) {
		_sneakyDirection = sneakyDirection;
		if (MainProxy.isServer(this.world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ExtractorModuleMode.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleGuiProvider.class).setExtractorMode(getExtractionMode().ordinal()).setExclude(isExcludeFilter);
		// .setIsActive(isActive)
		// .setSneakyDirection(_sneakyDirection);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleInHand.class);
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

	protected ItemSendMode itemSendMode() {
		return getUpgradeManager().getItemExtractionUpgrade() > 0 ? ItemSendMode.Fast : ItemSendMode.Normal;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		return null;
	}

	@Override
	public void tick() {
		currentTick = 0;
		checkUpdate(null);
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		LogisticsItemOrder firstOrder = null;
		LogisticsItemOrder order = null;
		while (itemsleft > 0 && stacksleft > 0 && service.getItemOrderManager().hasOrders(ResourceType.PROVIDER) && (firstOrder == null || firstOrder != order)) {
			if (firstOrder == null) {
				firstOrder = order;
			}
			order = service.getItemOrderManager().peekAtTopRequest(ResourceType.PROVIDER);
			int sent = sendStack(order.getResource().stack, itemsleft, order.getDestination().getRouter().getSimpleId(), order.getInformation());
			if (sent < 0) {
				break;
			}
			service.spawnParticle(Particles.VioletParticle, 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	public boolean filterAllowsItem(ItemIdentifier item) {
		if (!hasFilter()) {
			return true;
		}
		boolean isFiltered = itemIsFiltered(item);
		return isExcludeFilter ^ isFiltered;
	}

	@Override
	public void onBlockRemoval() {
		while (service.getItemOrderManager().hasOrders(ResourceType.PROVIDER)) {
			service.getItemOrderManager().sendFailed();
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {
		List<ItemIdentifier> possible = new ArrayList<>();
		if (tree.getRequestType() instanceof ItemResource) {
			possible.add(((ItemResource) tree.getRequestType()).getItem());
		} else if (tree.getRequestType() instanceof Resource.Dict) {
			WrappedInventory inv = service.getPointedInventory(_extractionMode);
			if (inv != null) {
				Map<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
				possible.addAll(currentInv.keySet().stream()
						.filter(item -> tree.getRequestType().matches(item, Resource.MatchSettings.NORMAL))
						.collect(Collectors.toList()));
			}
		}
		for (ItemIdentifier item : possible) {
			int canProvide = getAvailableItemCount(item);
			canProvide -= root.getAllPromissesFor((ItemRequestProvider) service, item);
			canProvide = Math.min(canProvide, tree.getMissingAmount());
			if (canProvide < 1) {
				return;
			}
			LogisticsPromise promise = new LogisticsPromise(item, canProvide, (ItemRequestProvider) service, ResourceType.PROVIDER);
			tree.addPromise(promise);
		}
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, ItemRequester destination, IAdditionalTargetInformation info) {
		return service.getItemOrderManager().addOrder(new ItemStack(promise.item, promise.numberOfItems), destination, ResourceType.PROVIDER, info);
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		return getTotalItemCount(item) - service.getItemOrderManager().totalItemsCountInOrders(item);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		WrappedInventory inv = service.getPointedInventory(_extractionMode);
		if (inv == null) {
			return;
		}

		Map<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();

		// Skip already added items from this provider, skip filtered items, Reduce what has been reserved, add.
		outer:
		for (Entry<ItemIdentifier, Integer> currItem : currentInv.entrySet()) {
			if (items.containsKey(currItem.getKey())) {
				continue; // Already provided by the previous module
			}

			if (!filterAllowsItem(currItem.getKey())) {
				continue;
			}

			for (IFilter filter : filters) {
				if (filter.isBlocked() == filter.isFilteredItem(currItem.getKey().getUndamaged()) || filter.blockProvider()) {
					continue outer;
				}
			}

			int remaining = currItem.getValue() - service.getItemOrderManager().totalItemsCountInOrders(currItem.getKey());
			if (remaining < 1) {
				continue;
			}

			items.put(currItem.getKey(), remaining);
		}
	}

	// returns -1 on permanently failed, don't try another stack this tick
	// returns 0 on "unable to do this delivery"
	private int sendStack(ItemStack stack, int maxCount, int destination, IAdditionalTargetInformation info) {
		ItemIdentifier item = stack.getItem();
		WrappedInventory inv = service.getPointedInventory(_extractionMode);
		if (inv == null) {
			service.getItemOrderManager().sendFailed();
			return 0;
		}

		int available = inv.itemCount(item);
		if (available == 0) {
			service.getItemOrderManager().sendFailed();
			return 0;
		}
		int wanted = Math.min(available, stack.getCount());
		wanted = Math.min(wanted, maxCount);
		wanted = Math.min(wanted, item.getMaxStackSize());
		Router dRtr = RouterManager.getInstance().getRouterUnsafe(destination, false);
		if (dRtr == null) {
			service.getItemOrderManager().sendFailed();
			return 0;
		}
		SinkReply reply = LogisticsManagerImpl.canSink(dRtr, null, true, stack.getItem(), null, true, false);
		boolean defersend = false;
		if (reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
			if (reply.maxNumberOfItems < wanted) {
				wanted = reply.maxNumberOfItems;
				if (wanted <= 0) {
					service.getItemOrderManager().deferSend();
					return 0;
				}
				defersend = true;
			}
		}
		if (!service.canUseEnergy(wanted * neededEnergy())) {
			return -1;
		}

		ItemStack removed = inv.getMultipleItems(item, wanted);
		if (removed.isEmpty()) {
			service.getItemOrderManager().sendFailed();
			return 0;
		}
		int sent = removed.getCount();
		service.useEnergy(sent * neededEnergy());

		IRoutedItem sendedItem = service.sendStack(removed, destination, itemSendMode(), info);
		service.getItemOrderManager().sendSuccessfull(sent, defersend, sendedItem);
		return sent;
	}

	private int getTotalItemCount(ItemIdentifier item) {

		WrappedInventory inv = service.getPointedInventory(_extractionMode);
		if (inv == null) {
			return 0;
		}

		if (!filterAllowsItem(item)) {
			return 0;
		}

		return inv.itemCount(item);
	}

	private boolean hasFilter() {
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

	public ExtractionMode getExtractionMode() {
		return _extractionMode;
	}

	public void setExtractionMode(int id) {
		_extractionMode = ExtractionMode.getMode(id);
	}

	public void nextExtractionMode() {
		_extractionMode = _extractionMode.next();
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add(!isExcludeFilter ? "Included" : "Excluded");
		list.add("Mode: " + _extractionMode.getExtractionModeString());
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
				.map(item -> new ItemStack(item.getKey(), item.getValue()))
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
	public void handleInvContent(Collection<ItemStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		// when filter is empty or in exclude mode, this is interested in attached inventory already
		if (isExcludeFilter || _filterInventory.isEmpty()) {
			return null;
		}
		// when items included this is only interested in items in the filter
		Map<ItemIdentifier, Integer> mapIC = _filterInventory.getItemsAndCount();
		List<ItemIdentifier> li = new ArrayList<>(mapIC.size());
		li.addAll(mapIC.keySet());
		return li;
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
	public boolean receivePassive() {
		return false;
	}

}
