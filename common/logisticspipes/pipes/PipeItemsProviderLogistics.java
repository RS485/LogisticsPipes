/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.gui.hud.HUDProvider;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class PipeItemsProviderLogistics extends RoutedPipe implements IProvideItems, IHeadUpDisplayRendererProvider, IChestContentReceiver, IChangeListener, IOrderManagerContentReceiver {

	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
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
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
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
		
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			
			IInventoryUtil inv = getAdaptedInventoryUtil(tile);
			int available = inv.itemCount(item);
			if (available == 0) continue;
			
			int wanted = Math.min(available, stack.stackSize);
			wanted = Math.min(wanted, maxCount);
			wanted = Math.min(wanted, item.getMaxStackSize());
			
			if(!useEnergy(wanted * neededEnergy())) {
				return 0;
			}
			ItemStack removed = inv.getMultipleItems(item, wanted);
			if(removed == null) continue;
			int sent = removed.stackSize;

			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(removed, this.worldObj);
			routedItem.setDestination(destination);
			routedItem.setTransportMode(TransportMode.Active);
			routedItem.addRelayPoints(relays);
			super.queueRoutedItem(routedItem, tile.orientation);
			
			_orderManager.sendSuccessfull(sent);
			return sent;
		}
		_orderManager.sendFailed();
		return 0;
	}
	
	private IInventoryUtil getAdaptedInventoryUtil(AdjacentTile tile){
		IInventory base = (IInventory) tile.tile;
		if (base instanceof ISidedInventory) {
			base = new SidedInventoryAdapter((ISidedInventory) base, tile.orientation.getOpposite());
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
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 0, 0);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_PROVIDER_TEXTURE;
	}

	@Override
	public int getAvailableItemCount(ItemIdentifier item) {
		if (!isEnabled()){
			return 0;
		}
		return getTotalItemCount(item) - _orderManager.totalItemsCountInOrders(item); 
	}
	
	@Override
	public void enabledUpdateEntity() {
		
		if(worldObj.getWorldTime() % 6 == 0) {
			updateInv();
		}
		
		if (doContentUpdate) {
			checkContentUpdate();
		}
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && _orderManager.hasOrders()) {
			Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>> order = _orderManager.getNextRequest();
			int sent = sendStack(order.getValue1(), itemsleft, order.getValue2().getRouter().getSimpleID(), order.getValue3());
			if (sent == 0)
				break;
			MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, zCoord, this.worldObj, 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses, List<IFilter> filters) {
		
		if (!isEnabled()){
			return;
		}
		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.getFilteredItems().contains(tree.getStack().getItem()) || filter.blockProvider()) return;
		}
		
		// Check the transaction and see if we have helped already
		int canProvide = getAvailableItemCount(tree.getStack().getItem());
		if (donePromisses.containsKey(tree.getStack().getItem())){
			canProvide -= donePromisses.get(tree.getStack().getItem());
		}
		if (canProvide < 1) return;
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = tree.getStack().getItem();
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
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, xCoord, yCoord, zCoord, this.worldObj, 2);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		LogicProvider providerLogic = (LogicProvider) logic;
		HashMap<ItemIdentifier, Integer> addedItems = new HashMap<ItemIdentifier, Integer>(); 
		
		if (!isEnabled()){
			return;
		}
		
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			IInventoryUtil inv = this.getAdaptedInventoryUtil(tile);
			
			HashMap<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
			for (ItemIdentifier currItem : currentInv.keySet()) {
				if(items.containsKey(currItem)) continue;
				
				if(providerLogic.hasFilter()  && ((providerLogic.isExcludeFilter() && providerLogic.itemIsFiltered(currItem))  || (!providerLogic.isExcludeFilter() && !providerLogic.itemIsFiltered(currItem)))) continue;
				
				for(IFilter filter:filters) {
					if(filter.isBlocked() == filter.getFilteredItems().contains(currItem) || filter.blockProvider()) continue;
				}
				
				if (!addedItems.containsKey(currItem)) {
					addedItems.put(currItem, currentInv.get(currItem));
				} else {
					addedItems.put(currItem, addedItems.get(currItem) + currentInv.get(currItem));
				}
			}
		}
		
		//Reduce what has been reserved.
		Iterator<ItemIdentifier> iterator = addedItems.keySet().iterator();
		while(iterator.hasNext()){
			ItemIdentifier item = iterator.next();
		
			int remaining = addedItems.get(item) - _orderManager.totalItemsCountInOrders(item);
			if (remaining < 1){
				iterator.remove();
			} else {
				addedItems.put(item, remaining);	
			}
		}
		for(ItemIdentifier item: addedItems.keySet()) {
			if (!items.containsKey(item)) {
				items.put(item, addedItems.get(item));
			} else {
				items.put(item, addedItems.get(item) + items.get(item));
			}
		}
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, xCoord, yCoord, zCoord, 1 /*TODO*/).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, xCoord, yCoord, zCoord, 1 /*TODO*/).getPacket());
	}
	
	private void updateInv() {
		itemList.clear();
		Map<ItemIdentifier, Integer> list = new HashMap<ItemIdentifier, Integer>();
		getAllItems(list, new ArrayList<IFilter>(0));
		for(ItemIdentifier item :list.keySet()) {
			itemList.add(new ItemIdentifierStack(item, list.get(item)));
		}
		if(!itemList.equals(oldList)) {
			oldList.clear();
			oldList.addAll(itemList);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, xCoord, yCoord, zCoord, itemList).getPacket(), localModeWatchers);
		}
	}

	@Override
	public void listenedChanged() {
		doContentUpdate = true;
	}

	private void checkContentUpdate() {
		doContentUpdate = false;
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList();
		if(!oldManagerList.equals(all)) {
			oldManagerList.clear();
			oldManagerList.addAll(all);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, all).getPacket(), localModeWatchers);
		}
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, xCoord, yCoord, zCoord, oldList).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, oldManagerList).getPacket(), (Player)player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStartWatching(player, mode);
		localModeWatchers.remove(player);
	}

	@Override
	public void setReceivedChestContent(LinkedList<ItemIdentifierStack> list) {
		itemList.clear();
		itemList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void setOrderManagerContent(LinkedList<ItemIdentifierStack> list) {
		itemListOrderer.clear();
		itemListOrderer.addAll(list);
	}
}
