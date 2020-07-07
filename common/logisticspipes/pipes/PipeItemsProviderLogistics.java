/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDProvider;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.ChestContent;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.modules.ProviderPipeInclude;
import logisticspipes.network.packets.modules.ProviderPipeMode;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class PipeItemsProviderLogistics extends CoreRoutedPipe implements IProvideItems, IHeadUpDisplayRendererProvider, IChestContentReceiver, IChangeListener, IOrderManagerContentReceiver {

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private final Map<ItemIdentifier, Integer> displayMap = new TreeMap<>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<>();

	public final LinkedList<ItemIdentifierStack> oldManagerList = new LinkedList<>();
	public final LinkedList<ItemIdentifierStack> itemListOrderer = new LinkedList<>();
	private final HUDProvider HUD = new HUDProvider(this);

	protected LogisticsItemOrderManager _orderManager = new LogisticsItemOrderManager(this, this);
	private boolean doContentUpdate = true;

	protected ModuleProvider myModule;

	public PipeItemsProviderLogistics(Item item) {
		super(item);
	}

	public PipeItemsProviderLogistics(Item item, LogisticsItemOrderManager logisticsOrderManager) {
		this(item);
		_orderManager = logisticsOrderManager;
		myModule = new ModuleProvider();
		myModule.registerHandler(this, this);
	}

	@Override
	public void onAllowedRemoval() {
		while (_orderManager.hasOrders(ResourceType.PROVIDER)) {
			_orderManager.sendFailed();
		}
	}

	public int getTotalItemCount(ItemIdentifier item) {
		if (!isEnabled()) {
			return 0;
		}

		// check if configurations allow for this item
		if (hasFilter() && ((isExcludeFilter() && itemIsFiltered(item)) || (!isExcludeFilter() && !itemIsFiltered(item)))) {
			return 0;
		}

		return new WorldCoordinatesWrapper(container).connectedTileEntities(ConnectionPipeType.ITEM)
				.filter(adjacent -> !SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.getTileEntity()))
				.map(this::getAdaptedInventoryUtil)
				.filter(Objects::nonNull)
				.map(util -> util.itemCount(item))
				.reduce(Integer::sum).orElse(0);
	}

	protected int neededEnergy() {
		return 1;
	}

	protected int itemsToExtract() {
		return 8;
	}

	protected int stacksToExtract() {
		return 1;
	}

	private int sendStack(ItemIdentifierStack stack, int maxCount, int destination, IAdditionalTargetInformation info) {
		ItemIdentifier item = stack.getItem();

		final Iterator<Pair<IInventoryUtil, EnumFacing>> iterator = new WorldCoordinatesWrapper(container)
				.connectedTileEntities(ConnectionPipeType.ITEM)
				.filter(adjacent -> !SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.getTileEntity()))
				.flatMap(adjacent -> {
					final IInventoryUtil invUtil = getAdaptedInventoryUtil(adjacent);
					return invUtil == null ? Stream.empty() : Stream.of(new Pair<>(invUtil, adjacent.getDirection()));
				})
				.iterator();

		while (iterator.hasNext()) {
			Pair<IInventoryUtil, EnumFacing> next = iterator.next();
			int available = next.getValue1().itemCount(item);
			if (available == 0) {
				continue;
			}

			int wanted = Math.min(available, stack.getStackSize());
			wanted = Math.min(wanted, maxCount);
			wanted = Math.min(wanted, item.getMaxStackSize());
			IRouter dRtr = SimpleServiceLocator.routerManager.getServerRouter(destination);
			if (dRtr == null) {
				_orderManager.sendFailed();
				return 0;
			}
			SinkReply reply = LogisticsManager.canSink(stack.makeNormalStack(), dRtr, null, true, stack.getItem(), null, true, false);
			boolean defersend = false;
			if (reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
				if (reply.maxNumberOfItems < wanted) {
					wanted = reply.maxNumberOfItems;
					if (wanted <= 0) {
						_orderManager.deferSend();
						return 0;
					}
					defersend = true;
				}
			}
			if (!canUseEnergy(wanted * neededEnergy())) {
				return -1;
			}
			ItemStack removed = next.getValue1().getMultipleItems(item, wanted);
			if (removed.isEmpty()) {
				continue;
			}
			int sent = removed.getCount();
			useEnergy(sent * neededEnergy());

			IRoutedItem routedItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(removed);
			routedItem.setDestination(destination);
			routedItem.setTransportMode(TransportMode.Active);
			routedItem.setAdditionalTargetInformation(info);
			super.queueRoutedItem(routedItem, next.getValue2());

			_orderManager.sendSuccessfull(sent, defersend, routedItem);
			return sent;
		}

		_orderManager.sendFailed();
		return 0;
	}

	@Nullable
	private IInventoryUtil getAdaptedInventoryUtil(NeighborTileEntity<TileEntity> adjacent) {
		return CoreRoutedPipe.getInventoryForExtractionMode(getExtractionMode(), adjacent);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_PROVIDER_TEXTURE;
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		if (!isEnabled()) {
			return 0;
		}
		return getTotalItemCount(item) - _orderManager.totalItemsCountInOrders(item);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();

		if (isNthTick(6)) {
			updateInv(null);
		}

		if (doContentUpdate) {
			checkContentUpdate(null);
		}

		if (!_orderManager.hasOrders(ResourceType.PROVIDER) || getWorld().getTotalWorldTime() % 6 != 0) {
			return;
		}

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		LogisticsItemOrder firstOrder = null;
		LogisticsItemOrder order = null;
		while (itemsleft > 0 && stacksleft > 0 && _orderManager.hasOrders(ResourceType.PROVIDER) && (firstOrder == null || firstOrder != order)) {
			if (firstOrder == null) {
				firstOrder = order;
			}
			order = _orderManager.peekAtTopRequest(ResourceType.PROVIDER);
			int sent = sendStack(order.getResource().stack, itemsleft, order.getRouter().getSimpleID(), order.getInformation());
			if (sent < 0) {
				break;
			}
			spawnParticle(Particles.VioletParticle, 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {
		if (!isEnabled()) {
			return;
		}
		if (tree.getRequestType() instanceof ItemResource) {
			ItemIdentifier item = ((ItemResource) tree.getRequestType()).getItem();
			for (IFilter filter : filters) {
				if (filter.isBlocked() == filter.isFilteredItem(item.getUndamaged()) || filter.blockProvider()) {
					return;
				}
			}

			// Check the transaction and see if we have helped already
			int canProvide = getAvailableItemCount(item);
			canProvide -= root.getAllPromissesFor(this, item);
			if (canProvide < 1) {
				return;
			}
			LogisticsPromise promise = new LogisticsPromise(item, Math.min(canProvide, tree.getMissingAmount()), this, ResourceType.PROVIDER);
			tree.addPromise(promise);
		} else if (tree.getRequestType() instanceof DictResource) {
			DictResource dict = (DictResource) tree.getRequestType();
			HashMap<ItemIdentifier, Integer> available = new HashMap<>();
			getAllItems(available, filters);
			for (Entry<ItemIdentifier, Integer> item : available.entrySet()) {
				if (!dict.matches(item.getKey(), IResource.MatchSettings.NORMAL)) {
					continue;
				}
				int canProvide = getAvailableItemCount(item.getKey());
				canProvide -= root.getAllPromissesFor(this, item.getKey());
				if (canProvide < 1) {
					continue;
				}
				LogisticsPromise promise = new LogisticsPromise(item.getKey(), Math.min(canProvide, tree.getMissingAmount()), this, ResourceType.PROVIDER);
				tree.addPromise(promise);
				if (tree.getMissingAmount() <= 0) {
					break;
				}
			}
		}
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		spawnParticle(Particles.WhiteParticle, 2);
		return _orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, ResourceType.PROVIDER, info);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		if (!isEnabled()) {
			return;
		}
		HashMap<ItemIdentifier, Integer> addedItems = new HashMap<>();

		final Iterator<Map<ItemIdentifier, Integer>> iterator = new WorldCoordinatesWrapper(container)
				.connectedTileEntities(ConnectionPipeType.ITEM)
				.filter(adjacent -> !SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.getTileEntity()))
				.map(this::getAdaptedInventoryUtil)
				.filter(Objects::nonNull)
				.map(IInventoryUtil::getItemsAndCount)
				.iterator();

		outer:
		while (iterator.hasNext()) {
			Iterator<Entry<ItemIdentifier, Integer>> entryIterator = iterator.next().entrySet().stream()
					.filter(currentItem -> !items.containsKey(currentItem.getKey()))
					.filter(currentItem -> !hasFilter() || (!isExcludeFilter() || !itemIsFiltered(currentItem.getKey())) && (isExcludeFilter()
							|| itemIsFiltered(currentItem.getKey()))).iterator();

			while (entryIterator.hasNext()) {
				Entry<ItemIdentifier, Integer> next = entryIterator.next();

				for (IFilter filter : filters) {
					if (filter.isBlocked() == filter.isFilteredItem(next.getKey().getUndamaged()) || filter.blockProvider()) {
						continue outer;
					}
				}

				addedItems.merge(next.getKey(), next.getValue(), Integer::sum);
			}
		}

		// reduce what has been reserved, add.
		for (Entry<ItemIdentifier, Integer> item : addedItems.entrySet()) {
			int remaining = item.getValue() - _orderManager.totalItemsCountInOrders(item.getKey());
			if (remaining < 1) {
				continue;
			}

			items.put(item.getKey(), remaining);
		}
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(
				PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1 /*TODO*/).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1 /*TODO*/).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	private void updateInv(EntityPlayer player) {
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
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ChestContent.class).setIdentList(displayList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		} else if (player != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChestContent.class).setIdentList(displayList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
		}
	}

	@Override
	public void listenedChanged() {
		doContentUpdate = true;
	}

	private void checkContentUpdate(EntityPlayer player) {
		doContentUpdate = false;
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList(getWorld());
		if (!oldManagerList.equals(all)) {
			oldManagerList.clear();
			oldManagerList.addAll(all);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(all).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		} else if (player != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(all).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
		}
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 1) {
			localModeWatchers.add(player);
			updateInv(player);
			checkContentUpdate(player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
	}

	@Override
	public void setReceivedChestContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.ensureCapacity(list.size());
		displayList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void setOrderManagerContent(Collection<ItemIdentifierStack> list) {
		itemListOrderer.clear();
		itemListOrderer.addAll(list);
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		new WorldCoordinatesWrapper(container).connectedTileEntities(ConnectionPipeType.ITEM)
				.flatMap(adjacent -> {
					if (!SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.getTileEntity())) {
						final IInventoryUtil adjacentInventoryUtil = this.getAdaptedInventoryUtil(adjacent);
						if (adjacentInventoryUtil != null) {
							return adjacentInventoryUtil.getItems().stream();
						}
					}
					return Stream.empty();
				})
				.forEach(itemidCollection::add);
	}

	@Override
	public double getLoadFactor() {
		return (_orderManager.totalAmountCountInAllOrders() + 63) / 64.0;
	}

	// import from logic
	private ItemIdentifierInventory providingInventory = new ItemIdentifierInventory(9, "", 1);
	private boolean _filterIsExclude;
	private ExtractionMode _extractionMode = ExtractionMode.Normal;

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_ProviderPipe_ID, getWorld(), getX(), getY(), getZ());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderPipeMode.class).setInteger(getExtractionMode().ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), entityplayer);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderPipeInclude.class).setInteger(isExcludeFilter() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), entityplayer);
	}

	/*** GUI ***/
	public ItemIdentifierInventory getprovidingInventory() {
		return providingInventory;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		providingInventory.readFromNBT(nbttagcompound, "");
		_filterIsExclude = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.getMode(nbttagcompound.getInteger("extractionMode"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		providingInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("filterisexclude", _filterIsExclude);
		nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());
	}

	/** INTERFACE TO PIPE **/
	public boolean hasFilter() {
		return !providingInventory.isEmpty();
	}

	public boolean itemIsFiltered(ItemIdentifier item) {
		return providingInventory.containsItem(item);
	}

	public boolean isExcludeFilter() {
		return _filterIsExclude;
	}

	public void setFilterExcluded(boolean isExcluded) {
		_filterIsExclude = isExcluded;
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

}
