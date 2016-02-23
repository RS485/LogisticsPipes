/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
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
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PipeItemsProviderLogistics extends CoreRoutedPipe implements IProvideItems, IHeadUpDisplayRendererProvider, IChestContentReceiver, IChangeListener, IOrderManagerContentReceiver {

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private final Map<ItemIdentifier, Integer> displayMap = new TreeMap<ItemIdentifier, Integer>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<ItemIdentifierStack>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<ItemIdentifierStack>();

	public final LinkedList<ItemIdentifierStack> oldManagerList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> itemListOrderer = new LinkedList<ItemIdentifierStack>();
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

		//Check if configurations allow for this item
		if (hasFilter() && ((isExcludeFilter() && itemIsFiltered(item)) || (!isExcludeFilter() && !itemIsFiltered(item)))) {
			return 0;
		}

		int count = 0;
		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile.tile)) {
				continue;
			}
			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			count += inv.itemCount(item);
		}
		return count;
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

		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile.tile)) {
				continue;
			}

			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			int available = inv.itemCount(item);
			if (available == 0) {
				continue;
			}

			int wanted = Math.min(available, stack.getStackSize());
			wanted = Math.min(wanted, maxCount);
			wanted = Math.min(wanted, item.getMaxStackSize());
			IRouter dRtr = SimpleServiceLocator.routerManager.getRouterUnsafe(destination, false);
			if (dRtr == null) {
				_orderManager.sendFailed();
				return 0;
			}
			SinkReply reply = LogisticsManager.canSink(dRtr, null, true, stack.getItem(), null, true, false);
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
			ItemStack removed = inv.getMultipleItems(item, wanted);
			if (removed == null || removed.stackSize == 0) {
				continue;
			}
			int sent = removed.stackSize;
			useEnergy(sent * neededEnergy());

			IRoutedItem routedItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(removed);
			routedItem.setDestination(destination);
			routedItem.setTransportMode(TransportMode.Active);
			routedItem.setAdditionalTargetInformation(info);
			super.queueRoutedItem(routedItem, tile.orientation);

			_orderManager.sendSuccessfull(sent, defersend, routedItem);
			return sent;
		}
		_orderManager.sendFailed();
		return 0;
	}

	private IInventoryUtil getAdaptedInventoryUtil(AdjacentTile tile) {
		IInventory base = (IInventory) tile.tile;
		if (base instanceof net.minecraft.inventory.ISidedInventory) {
			base = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) base, tile.orientation.getOpposite(), true);
		}
		ExtractionMode mode = getExtractionMode();
		switch (mode) {
			case LeaveFirst:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), false, false, 1, 0);
			case LeaveLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), false, false, 0, 1);
			case LeaveFirstAndLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), false, false, 1, 1);
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), true, false, 0, 0);
			case Leave1PerType:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), false, true, 0, 0);
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, tile.orientation.getOpposite(), false, false, 0, 0);
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
			HashMap<ItemIdentifier, Integer> available = new HashMap<ItemIdentifier, Integer>();
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
		HashMap<ItemIdentifier, Integer> addedItems = new HashMap<ItemIdentifier, Integer>();

		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile.tile)) {
				continue;
			}
			IInventoryUtil inv = getAdaptedInventoryUtil(tile);

			Map<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
			outer:
				for (Entry<ItemIdentifier, Integer> currItem : currentInv.entrySet()) {
					if (items.containsKey(currItem.getKey())) {
						continue;
					}

				if (hasFilter() && ((isExcludeFilter() && itemIsFiltered(currItem.getKey())) || (!isExcludeFilter() && !itemIsFiltered(currItem.getKey())))) {
						continue;
					}

				for (IFilter filter : filters) {
						if (filter.isBlocked() == filter.isFilteredItem(currItem.getKey().getUndamaged()) || filter.blockProvider()) {
							continue outer;
						}
					}

				Integer addedAmount = addedItems.get(currItem.getKey());
					if (addedAmount == null) {
						addedItems.put(currItem.getKey(), currItem.getValue());
					} else {
						addedItems.put(currItem.getKey(), addedAmount + currItem.getValue());
					}
				}
		}

		//Reduce what has been reserved, add.
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
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1 /*TODO*/).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
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
		getAllItems(displayMap, new ArrayList<IFilter>(0));
		displayList.ensureCapacity(displayMap.size());
		for (Entry<ItemIdentifier, Integer> item : displayMap.entrySet()) {
			displayList.add(new ItemIdentifierStack(item.getKey(), item.getValue()));
		}
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
	//work in progress, currently not active code.
	public Set<ItemIdentifier> getSpecificInterests() {
		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		Set<ItemIdentifier> l1 = null;
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile.tile)) {
				continue;
			}

			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			Set<ItemIdentifier> items = inv.getItems();
			if (l1 == null) {
				l1 = items;
			} else {
				l1.addAll(items);
			}
		}
		return l1;
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
