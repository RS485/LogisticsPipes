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
import java.util.UUID;

import logisticspipes.gui.hud.HUDProvider;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.CroppedInventory;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsProviderLogistics extends RoutedPipe implements IProvideItems, IHeadUpDisplayRendererProvider, IChestContentReceiver, IChangeListener, IOrderManagerContentReceiver {

	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldManagerList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> itemListOrderer = new LinkedList<ItemIdentifierStack>();
	private final HUDProvider HUD = new HUDProvider(this);
	
	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);
	//private InventoryUtilFactory _inventoryUtilFactory = new InventoryUtilFactory();
		
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
		for (ForgeDirection o : ForgeDirection.values()){
			Position p = new Position(xCoord, yCoord, zCoord, o);
			p.moveForwards(1);
			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
			if (!(tile instanceof IInventory)) continue;
			if (tile instanceof TileGenericPipe) continue;
			InventoryUtil inv = this.getAdaptedInventoryUtil((IInventory) tile); 
					//_inventoryUtilFactory.getInventoryUtil(Utils.getInventory((IInventory) tile));
			count += inv.itemCount(item);
		}
		return count;
	}

	protected int sendItem(ItemIdentifier item, int maxCount, UUID destination) {
		int sent = 0;
		for (ForgeDirection o : ForgeDirection.values()){
			Position p = new Position(xCoord, yCoord, zCoord, o);
			p.moveForwards(1);
			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
			if (!(tile instanceof IInventory)) continue;
			if (tile instanceof TileGenericPipe) continue;
			
			InventoryUtil inv = getAdaptedInventoryUtil((IInventory) tile); 
					//new InventoryUtil(Utils.getInventory((IInventory) tile));
			
			if (inv.itemCount(item)> 0){
				ItemStack removed = inv.getSingleItem(item);
				IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(removed, this.worldObj);
				routedItem.setSource(this.getRouter().getId());
				routedItem.setDestination(destination);
				routedItem.setTransportMode(TransportMode.Active);
				super.queueRoutedItem(routedItem, p.orientation);
				//super.sendRoutedItem(removed, destination, p);
				sent++;
				maxCount--;
				if (maxCount < 1) break;
			}			
		}
		updateInv(false);
		return sent;
	}
	
	private InventoryUtil getAdaptedInventoryUtil(IInventory base){
		ExtractionMode mode = ((LogicProvider)logic).getExtractionMode();
		switch(mode){
			case LeaveFirst:
				base = new CroppedInventory(base, 1, 0);
				break;
			case LeaveLast:
				base = new CroppedInventory(base, 0, 1);
				break;
			case LeaveFirstAndLast:
				base = new CroppedInventory(base, 1, 1);
				break;
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getOneHiddenInventoryUtil(base);
		}
		
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(base);
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
	public void updateEntity() {
		super.updateEntity();
		
		if(MainProxy.isClient()) return;
		
		if(worldObj.getWorldTime() % 6 == 0) {
			updateInv(false);
		}
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		
		if(!this.getClass().equals(PipeItemsProviderLogistics.class)) return;
		
		if(!useEnergy(1)) return;
		
		Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
		int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
		MainProxy.proxy.spawnGenericParticle("VioletParticle", this.xCoord, this.yCoord, this.zCoord, 3);
		if (sent > 0){
			_orderManager.sendSuccessfull(sent);
		}
		else {
			_orderManager.sendFailed();
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses) {
		
		if (!isEnabled()){
			return;
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
		tree.addPromise(promise);
	}
	
	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination);
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		LogicProvider providerLogic = (LogicProvider) logic;
		HashMap<ItemIdentifier, Integer> allItems = new HashMap<ItemIdentifier, Integer>(); 
	
		if (!isEnabled()){
			return allItems;
		}
		
		for (ForgeDirection o : ForgeDirection.values()){
			Position p = new Position(xCoord, yCoord, zCoord, o);
			p.moveForwards(1);
			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
			if (!(tile instanceof IInventory)) continue;
			if (tile instanceof TileGenericPipe) continue;
			InventoryUtil inv = this.getAdaptedInventoryUtil((IInventory) tile); 
					//_inventoryUtilFactory.getInventoryUtil(Utils.getInventory((IInventory) tile));
			HashMap<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
			for (ItemIdentifier currItem : currentInv.keySet()){
				if (providerLogic.hasFilter() 
						&& ((providerLogic.isExcludeFilter() && providerLogic.itemIsFiltered(currItem)) 
								|| (!providerLogic.isExcludeFilter() && !providerLogic.itemIsFiltered(currItem)))) continue;

				if (!allItems.containsKey(currItem)){
					allItems.put(currItem, currentInv.get(currItem));
				}else {
					allItems.put(currItem, allItems.get(currItem) + currentInv.get(currItem));
				}
			}
		}
		
		//Reduce what has been reserved.
		Iterator<ItemIdentifier> iterator = allItems.keySet().iterator();
		while(iterator.hasNext()){
			ItemIdentifier item = iterator.next();
		
			int remaining = allItems.get(item) - _orderManager.totalItemsCountInOrders(item);
			if (remaining < 1){
				iterator.remove();
			} else {
				allItems.put(item, remaining);	
			}
		}
		
		return allItems;
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
	
	private IInventory getRawInventory(ForgeDirection ori) {
		Position pos = new Position(this.xCoord, this.yCoord, this.zCoord, ori);
		pos.moveForwards(1);
		TileEntity tile = this.worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
		if (tile instanceof TileGenericPipe) return null;
		if (!(tile instanceof IInventory)) return null;
		return Utils.getInventory((IInventory) tile);
	}
	
	private IInventory getInventory(ForgeDirection ori) {
		IInventory rawInventory = getRawInventory(ori);
		if (rawInventory instanceof ISidedInventory) return new SidedInventoryAdapter((ISidedInventory) rawInventory, ori.getOpposite());
		return rawInventory;
	}
	
	private void addToList(ItemIdentifierStack stack) {
		for(ItemIdentifierStack ident:itemList) {
			if(ident.getItem().equals(stack.getItem())) {
				ident.stackSize += stack.stackSize;
				return;
			}
		}
		itemList.addLast(stack);
	}
	
	private void updateInv(boolean force) {
		itemList.clear();
		for(ForgeDirection ori:ForgeDirection.values()) {
			LogicProvider providerLogic = (LogicProvider) logic;
			IInventory inv = getInventory(ori);
			if(inv != null) {
				for(int i=0;i<inv.getSizeInventory();i++) {
					if(inv.getStackInSlot(i) != null) {
						//Filter
						if (providerLogic.hasFilter() 
								&& ((providerLogic.isExcludeFilter() && providerLogic.itemIsFiltered(ItemIdentifier.get(inv.getStackInSlot(i)))) 
										|| (!providerLogic.isExcludeFilter() && !providerLogic.itemIsFiltered(ItemIdentifier.get(inv.getStackInSlot(i)))))) continue;

						addToList(ItemIdentifierStack.GetFromStack(inv.getStackInSlot(i)));
					}
				}
			}
		}
		if(!itemList.equals(oldList) || force) {
			oldList.clear();
			oldList.addAll(itemList);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, xCoord, yCoord, zCoord, itemList).getPacket(), localModeWatchers);
		}
	}

	@Override
	public void listenedChanged() {
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
			updateInv(true);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, _orderManager.getContentList()).getPacket(), localModeWatchers);
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
