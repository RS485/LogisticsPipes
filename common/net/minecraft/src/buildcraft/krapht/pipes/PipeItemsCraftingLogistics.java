/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.krapht.CraftingTemplate;
import net.minecraft.src.buildcraft.krapht.ICraftItems;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsOrderManager;
import net.minecraft.src.buildcraft.krapht.LogisticsPromise;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;
import net.minecraft.src.krapht.WorldUtil;

public class PipeItemsCraftingLogistics extends RoutedPipe implements ICraftItems{

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	
	protected int _extras;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(new LogicCrafting(), itemID);
	}
	
	protected LinkedList<AdjacentTile> locateCrafters()	{
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> crafters = new LinkedList<AdjacentTile>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities()){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			crafters.add(tile);
		}
		return crafters;
	}
	
	protected ItemStack extractFromAutoWorkbench(TileAutoWorkbench workbench){
		return workbench.extractItem(true, Orientations.Unknown);
	}
	
	protected ItemStack extractFromIInventory(IInventory inv){
		
		InventoryUtil invUtil = new InventoryUtil(inv, false);
		LogicCrafting craftingLogic = (LogicCrafting) this.logic;
		ItemStack itemstack = craftingLogic.getCraftedItem();
		if (itemstack == null) return null;
		
		ItemIdentifierStack targetItemStack = ItemIdentifierStack.GetFromStack(itemstack);
		//return invUtil.getMultipleItems(targetItemStack.getItem(), targetItemStack.stackSize);
		return invUtil.getSingleItem(targetItemStack.getItem());
	}
	
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if ((!_orderManager.hasOrders() && _extras < 1) || worldObj.getWorldTime() % 6 != 0) return;
		
		LinkedList<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1 ){
			_orderManager.sendFailed();
			return;
		}
		
		for (AdjacentTile tile : locateCrafters()){
			ItemStack extracted = null; 
			if (tile.tile instanceof TileAutoWorkbench){
				extracted = extractFromAutoWorkbench((TileAutoWorkbench) tile.tile);
			}else if (tile.tile instanceof IInventory) {
				extracted = extractFromIInventory((IInventory)tile.tile);
			}
			if (extracted == null) continue;
			while (extracted.stackSize > 0){
				ItemStack stackToSend = extracted.splitStack(1);
				Position p = new Position(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, tile.orientation);
				if (_orderManager.hasOrders()){
					LogisticsRequest order = _orderManager.getNextRequest();
					IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stackToSend, worldObj);
					item.setSource(this.getRouter().getId());
					item.setDestination(order.getDestination().getRouter().getId());
					item.setTransportMode(TransportMode.Active);
					super.queueRoutedItem(item, tile.orientation);
					//super.sendRoutedItem(stackToSend, order.getDestination().getRouter().getId(), p);
					_orderManager.sendSuccessfull(1);
				}else{
					_extras--;
					if(mod_LogisticsPipes.DisplayRequests)System.out.println("Extra dropped, " + _extras + " remaining");
					Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, p.orientation.reverse());
					entityPos.moveForwards(0.5);
					EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stackToSend);
					entityItem.speed = Utils.pipeNormalSpeed * core_LogisticsPipes.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER;
					((PipeTransportItems) transport).entityEntering(entityItem, entityPos.orientation);
				}
			}
		}
	}
	
	private ItemIdentifier providedItem(){
		LogicCrafting craftingLogic = (LogicCrafting) this.logic;
		ItemStack stack = craftingLogic.getCraftedItem(); 
		if ( stack == null) return null;
		return ItemIdentifier.get(stack);
	}
	

	@Override
	public int getCenterTexture() {
		// TODO Auto-generated method stub
		return core_LogisticsPipes.LOGISTICSPIPE_CRAFTER_TEXTURE;
	}
	
	public void canProvide(LogisticsTransaction transaction){
		
		if (!isEnabled()){
			return;
		}
		
		if (_extras < 1) return;
		for (LogisticsRequest request : transaction.getRemainingRequests()){
			ItemIdentifier providedItem = providedItem();
			if (request.getItem() != providedItem) continue;
			HashMap<ItemIdentifier, Integer> promised = transaction.getTotalPromised(this);
			int alreadyPromised = promised.containsKey(providedItem) ? promised.get(providedItem) : 0; 
			if (alreadyPromised >= _extras) continue;
			int remaining = _extras - alreadyPromised;
			LogisticsPromise promise = new LogisticsPromise();
			promise.item = providedItem;
			promise.numberOfItems = Math.min(remaining, request.notYetAllocated());
			promise.sender = this;
			promise.extra = true;
			request.addPromise(promise);
		}
	}

	@Override
	public void canCraft(LogisticsTransaction transaction) {
		
		if (!isEnabled()){
			return;
		}
		
		LogicCrafting craftingLogic = (LogicCrafting) this.logic;
		ItemStack stack = craftingLogic.getCraftedItem(); 
		if ( stack == null) return;
		
		CraftingTemplate template = new CraftingTemplate(ItemIdentifierStack.GetFromStack(stack), this);

		//Check all materials
		boolean hasSatellite = craftingLogic.isSatelliteConnected(); 
		for (int i = 0; i < 9; i++){
			ItemStack resourceStack = craftingLogic.getMaterials(i);
			if (resourceStack == null || resourceStack.stackSize == 0) continue;
			if (i < 6 || !hasSatellite){
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), this);
			}
			else{
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), (IRequestItems)craftingLogic.getSatelliteRouter().getPipe());
			}
				
		}
		transaction.addCraftingTemplate(template);
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise.extra){
			_extras -= promise.numberOfItems;
		}
		_orderManager.addOrder(new LogisticsRequest(promise.item, promise.numberOfItems, destination));
	}

	@Override
	public int getAvailableItemCount(ItemIdentifier item) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public Router getRouter() {
//		return router;
//	}

	@Override
	public void registerExtras(int count) {
		_extras += count;
		if(mod_LogisticsPipes.DisplayRequests)System.out.println(count + " extras registered");
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		return new HashMap<ItemIdentifier, Integer>();
	}

	@Override
	public ItemIdentifier getCraftedItem() {
		if (!isEnabled()){
			return null;
		}
		return providedItem();
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		// TODO Auto-generated method stub
		return null;
	}
}
