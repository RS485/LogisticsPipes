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

import logisticspipes.gui.hud.HUDProvider;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.network.oldpackets.PacketPipeInvContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SidedInventoryForgeAdapter;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class PipeItemsProviderLogistics extends CoreRoutedPipe implements IProvideItems, IHeadUpDisplayRendererProvider, IChestContentReceiver, IChangeListener, IOrderManagerContentReceiver {

	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();

	private final Map<ItemIdentifier,Integer> displayMap = new HashMap<ItemIdentifier, Integer>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<ItemIdentifierStack>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<ItemIdentifierStack>();

	public final LinkedList<ItemIdentifierStack> oldManagerList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> itemListOrderer = new LinkedList<ItemIdentifierStack>();
	private final HUDProvider HUD = new HUDProvider(this);
	
	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);
	private boolean doContentUpdate = true;
		
	public PipeItemsProviderLogistics(int itemID) {
		super(new LogicProvider(), itemID);
	}
	
	public PipeItemsProviderLogistics(int itemID, LogisticsOrderManager logisticsOrderManager) {
		this(itemID);
		_orderManager = logisticsOrderManager;
	}
	
	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		while(_orderManager.hasOrders()) {
			_orderManager.sendFailed();
		}
	}

	public int getTotalItemCount(ItemIdentifier item) {
		
		if (!isEnabled()){
			return 0;
		}
		
		//Check if configurations allow for this item
		LogicProvider logicProvider = (LogicProvider) logic;
		if (logicProvider.hasFilter() 
				&& ((logicProvider.isExcludeFilter() && logicProvider.itemIsFiltered(item)) 
						|| (!logicProvider.isExcludeFilter() && !logicProvider.itemIsFiltered(item)))) return 0;
		
		
		int count = 0;
		WorldUtil wUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			IInventoryUtil inv = this.getAdaptedInventoryUtil(tile);
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
	
	private int sendStack(ItemIdentifierStack stack, int maxCount, int destination, List<IRelayItem> relays) {
		ItemIdentifier item = stack.getItem();
		
		WorldUtil wUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			
			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			int available = inv.itemCount(item);
			if (available == 0) continue;
			
			int wanted = Math.min(available, stack.stackSize);
			wanted = Math.min(wanted, maxCount);
			wanted = Math.min(wanted, item.getMaxStackSize());
			IRouter dRtr = SimpleServiceLocator.routerManager.getRouterUnsafe(destination,false);
			if(dRtr == null) {
				_orderManager.sendFailed();
				return 0;
			}
			SinkReply reply = LogisticsManagerV2.canSink(dRtr, null, true, stack.getItem(), null, true,false);
			boolean defersend = false;
			if(reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
				if(reply.maxNumberOfItems < wanted) {
					wanted = reply.maxNumberOfItems;
					if(wanted <= 0) {
						_orderManager.deferSend();
						return 0;
					}
					defersend = true;
				}
			}
			if(!canUseEnergy(wanted * neededEnergy())) {
				return -1;
			}
			ItemStack removed = inv.getMultipleItems(item, wanted);
			if(removed == null) continue;
			int sent = removed.stackSize;
			useEnergy(sent * neededEnergy());

			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(removed, this.worldObj);
			routedItem.setDestination(destination);
			routedItem.setTransportMode(TransportMode.Active);
			routedItem.addRelayPoints(relays);
			super.queueRoutedItem(routedItem, tile.orientation);
			
			_orderManager.sendSuccessfull(sent, defersend);
			return sent;
		}
		_orderManager.sendFailed();
		return 0;
	}
	
	private IInventoryUtil getAdaptedInventoryUtil(AdjacentTile tile){
		IInventory base = (IInventory) tile.tile;
		if(base instanceof net.minecraft.inventory.ISidedInventory) {
			base = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory)base, tile.orientation.getOpposite(),false);
		}
		if(base instanceof net.minecraftforge.common.ISidedInventory) {
			base = new SidedInventoryForgeAdapter((net.minecraftforge.common.ISidedInventory)base, tile.orientation.getOpposite());
		}
		ExtractionMode mode = ((LogicProvider)logic).getExtractionMode();
		switch(mode){
			case LeaveFirst:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 1, 0);
			case LeaveLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 0, 1);
			case LeaveFirstAndLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 1, 1);
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, true, false, 0, 0);
			case Leave1PerType:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, true, 0, 0);
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 0, 0);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_PROVIDER_TEXTURE;
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		if (!isEnabled()){
			return 0;
		}
		return getTotalItemCount(item) - _orderManager.totalItemsCountInOrders(item); 
	}
	
	@Override
	public void enabledUpdateEntity() {
		
		if(worldObj.getWorldTime() % 6 == 0) {
			updateInv(null);
		}
		
		if (doContentUpdate) {
			checkContentUpdate(null);
		}
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>> firstOrder = null;
		Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>> order = null;
		while (itemsleft > 0 && stacksleft > 0 && _orderManager.hasOrders() && (firstOrder == null || firstOrder != order)) {
			if(firstOrder == null)
				firstOrder = order;
			order = _orderManager.peekAtTopRequest();
			int sent = sendStack(order.getValue1(), itemsleft, order.getValue2().getRouter().getSimpleID(), order.getValue3());
			if(sent < 0) break;
			MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, getX(), getY(), getZ(), this.worldObj, 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		
		if (!isEnabled()){
			return;
		}
		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(tree.getStackItem().getUndamaged()) || filter.blockProvider()) return;
		}
		
		// Check the transaction and see if we have helped already
		int canProvide = getAvailableItemCount(tree.getStackItem());
		canProvide -= donePromisses;
		if (canProvide < 1) return;
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = tree.getStackItem();
		promise.numberOfItems = Math.min(canProvide, tree.getMissingItemCount());
		promise.sender = this;
		List<IRelayItem> relays = new LinkedList<IRelayItem>();
		for(IFilter filter:filters) {
			relays.add(filter);
		}
		promise.relayPoints = relays;
		tree.addPromise(promise);
	}
	
	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, getX(), getY(), getZ(), this.worldObj, 2);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		if (!isEnabled()){
			return;
		}
		LogicProvider providerLogic = (LogicProvider) logic;
		HashMap<ItemIdentifier, Integer> addedItems = new HashMap<ItemIdentifier, Integer>();
		
		WorldUtil wUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			IInventoryUtil inv = this.getAdaptedInventoryUtil(tile);
			
			Map<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
outer:
			for (Entry<ItemIdentifier, Integer> currItem : currentInv.entrySet()) {
				if(items.containsKey(currItem.getKey())) continue;
				
				if(providerLogic.hasFilter() && ((providerLogic.isExcludeFilter() && providerLogic.itemIsFiltered(currItem.getKey()))  || (!providerLogic.isExcludeFilter() && !providerLogic.itemIsFiltered(currItem.getKey())))) continue;
				
				for(IFilter filter:filters) {
					if(filter.isBlocked() == filter.isFilteredItem(currItem.getKey().getUndamaged()) || filter.blockProvider()) continue outer;
				}
				
				Integer addedAmount = addedItems.get(currItem.getKey());
				if (addedAmount==null) {
					addedItems.put(currItem.getKey(), currItem.getValue());
				} else {
					addedItems.put(currItem.getKey(), addedAmount + currItem.getValue());
				}
			}
		}
		
		//Reduce what has been reserved, add.
		for(Entry<ItemIdentifier, Integer> item: addedItems.entrySet()) {
			int remaining = item.getValue() - _orderManager.totalItemsCountInOrders(item.getKey());
			if (remaining < 1) continue;

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
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1 /*TODO*/).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1 /*TODO*/).getPacket());
	}
	
	private void updateInv(EntityPlayer player) {
		if(localModeWatchers.size() == 0 && player == null)
			return;
		displayList.clear();
		displayMap.clear();
		getAllItems(displayMap, new ArrayList<IFilter>(0));
		displayList.ensureCapacity(displayMap.size());
		for(Entry <ItemIdentifier, Integer> item :displayMap.entrySet()) {
			displayList.add(new ItemIdentifierStack(item.getKey(), item.getValue()));
		}
		if(!oldList.equals(displayList)) {
			oldList.clear();
			oldList.ensureCapacity(displayList.size());
			oldList.addAll(displayList);
			MainProxy.sendCompressedToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), displayList).getPacket(), localModeWatchers);
		} else if(player != null) {
			MainProxy.sendCompressedPacketToPlayer(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), displayList).getPacket(), (Player)player);
		}
	}

	@Override
	public void listenedChanged() {
		doContentUpdate = true;
	}

	private void checkContentUpdate(EntityPlayer player) {
		doContentUpdate = false;
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList(this.worldObj);
		if(!oldManagerList.equals(all)) {
			oldManagerList.clear();
			oldManagerList.addAll(all);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), all).getPacket(), localModeWatchers);
		} else if(player != null) {
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), all).getPacket(), (Player)player);
		}
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
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

	@Override //work in progress, currently not active code.
	public Set<ItemIdentifier> getSpecificInterests() {
		WorldUtil wUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
		Set<ItemIdentifier> l1 = null;
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			
			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			Set<ItemIdentifier> items = inv.getItems();
			if(l1==null)
				l1=items;
			else
				l1.addAll(items);
		}
		return l1;
	}

	@Override
	public double getLoadFactor() {
		return (_orderManager.totalItemsCountInAllOrders()+63)/64.0;
	}


}
