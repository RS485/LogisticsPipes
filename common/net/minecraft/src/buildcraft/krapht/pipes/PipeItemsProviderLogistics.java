/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.krapht.IProvideItems;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsOrderManager;
import net.minecraft.src.buildcraft.krapht.LogisticsPromise;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.logisticspipes.ExtractionMode;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.krapht.CroppedInventory;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;

public class PipeItemsProviderLogistics extends RoutedPipe implements IProvideItems{

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	//private InventoryUtilFactory _inventoryUtilFactory = new InventoryUtilFactory();
		
	public PipeItemsProviderLogistics(int itemID) {
		super(new LogicProvider(), itemID);
	}
	
	public PipeItemsProviderLogistics(int itemID, 
										//InventoryUtilFactory inventoryUtilFactory,
										LogisticsOrderManager logisticsOrderManager) {
		this(itemID);		
		//_inventoryUtilFactory = inventoryUtilFactory;
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
		for (Orientations o : Orientations.values()){
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
		for (Orientations o : Orientations.values()){
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
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_PROVIDER_TEXTURE;
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
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		LogisticsRequest order = _orderManager.getNextRequest();
		int sent = sendItem(order.getItem(), order.numberLeft(), order.getDestination().getRouter().getId());
		if (sent > 0){
			_orderManager.sendSuccessfull(sent);
		}
		else {
			_orderManager.sendFailed();
		}
	}

	@Override
	public void canProvide(LogisticsTransaction transaction) {
		
		if (!isEnabled()){
			return;
		}
		
		// Check the transaction and see if we have helped already
		HashMap<ItemIdentifier, Integer> commited = transaction.getTotalPromised(this);
		for (LogisticsRequest request : transaction.getRemainingRequests()){
			int canProvide = getAvailableItemCount(request.getItem());
			if (commited.containsKey(request.getItem())){
				canProvide -= commited.get(request.getItem());
			}
			if (canProvide < 1) continue;
			LogisticsPromise promise = new LogisticsPromise();
			promise.item = request.getItem();
			promise.numberOfItems = Math.min(canProvide, request.notYetAllocated());
			promise.sender = this;
			request.addPromise(promise);
			commited = transaction.getTotalPromised(this);
		}
	}
	
	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new LogisticsRequest(promise.item, promise.numberOfItems, destination));
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		LogicProvider providerLogic = (LogicProvider) logic;
		HashMap<ItemIdentifier, Integer> allItems = new HashMap<ItemIdentifier, Integer>(); 
	
		if (!isEnabled()){
			return allItems;
		}
		
		for (Orientations o : Orientations.values()){
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
}
