/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.IRequireReliableTransport;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsBuilderSupplierLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsLiquidSupplier;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.InventoryUtilFactory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.WorldUtil;

public class LogicLiquidSupplier extends BaseRoutingLogic implements IRequireReliableTransport{
	
	private SimpleInventory dummyInventory = new SimpleInventory(9, "Liquids to keep stocked", 127);
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;

	public boolean pause = false;
	
	public LogicLiquidSupplier(){
		throttleTime = 100;
	}
	
	@Override
	public void destroy() {}

	
	@Override
	public void throttledUpdateEntity() {
		if (pause) return;
		super.throttledUpdateEntity();
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities()){
			if (!(tile.tile instanceof ILiquidContainer)) continue;
			ILiquidContainer container = (ILiquidContainer) tile.tile;
			if (container.getLiquidSlots().length == 0) continue;
			
			//How much do I want?
			InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(dummyInventory);
			HashMap<ItemIdentifier, Integer> wantContainers = invUtil.getItemsAndCount();
			HashMap<Integer, Integer> wantLiquids = new HashMap<Integer, Integer>();
			for (ItemIdentifier item : wantContainers.keySet()){
				ItemStack wantItem = item.makeNormalStack(1);
				int liquidId = BuildCraftAPI.getLiquidForFilledItem(wantItem);
				if (liquidId == 0) continue;
				wantLiquids.put(liquidId, wantContainers.get(item) * BuildCraftAPI.BUCKET_VOLUME);
			}

			//How much do I have?
			HashMap<Integer, Integer> haveLiquids = new HashMap<Integer, Integer>();
			
			LiquidSlot[] result = container.getLiquidSlots();
			for (LiquidSlot slot : result){
				if (!wantLiquids.containsKey(slot.getLiquidId())) continue;
				if (!haveLiquids.containsKey(slot.getLiquidId())){
					haveLiquids.put(slot.getLiquidId(), slot.getLiquidQty());
				} else {
					haveLiquids.put(slot.getLiquidId(), haveLiquids.get(slot.getLiquidId()) + slot.getLiquidQty());
				}
			}
			
			//HashMap<Integer, Integer> needLiquids = new HashMap<Integer, Integer>();
			//Reduce what I have
			for (Integer liquidId: wantLiquids.keySet()){
				if (haveLiquids.containsKey(liquidId)){
					wantLiquids.put(liquidId, wantLiquids.get(liquidId) - haveLiquids.get(liquidId));
				}
			}
			
			//Reduce what have been requested already
			for (Integer liquidId : wantLiquids.keySet()){
				for (ItemIdentifier requestedItem : _requestedItems.keySet()){
					ItemStack wantItem = requestedItem.makeNormalStack(1);
					int requestedLiquidId = BuildCraftAPI.getLiquidForFilledItem(wantItem);
					if (requestedLiquidId == 0) continue;
					wantLiquids.put(liquidId, wantLiquids.get(liquidId) - _requestedItems.get(requestedItem) * BuildCraftAPI.BUCKET_VOLUME);
				}
			}
			
			//((PipeItemsLiquidSupplier)this.container.pipe).setRequestFailed(false);
			
			//Make request
			
			HashMap<ItemIdentifier, Integer> allNeededContainers = invUtil.getItemsAndCount();
			for (ItemIdentifier need : allNeededContainers.keySet()){
				int requestedLiquidId = BuildCraftAPI.getLiquidForFilledItem(need.makeNormalStack(1));
				if (requestedLiquidId == 0) continue;
				if (!wantLiquids.containsKey(requestedLiquidId)) continue;
				int countToRequest = wantLiquids.get(requestedLiquidId) / BuildCraftAPI.BUCKET_VOLUME;
				if (countToRequest < 1) continue;
				boolean success = false;
				do{ 
					success = LogisticsManager.Request(new LogisticsRequest(need, countToRequest, (IRequestItems) this.container.pipe), getRouter().getRoutersByCost(), null);
					if (success || countToRequest == 1){
						break;
					}
					countToRequest = countToRequest / 2;
				} while (_requestPartials && !success);
				
				if (success){
					if (!_requestedItems.containsKey(need)){
						_requestedItems.put(need, countToRequest);
					}else
					{
						_requestedItems.put(need, _requestedItems.get(need) + countToRequest);
					}
				} else{
					//((PipeItemsLiquidSupplier)this.container.pipe).setRequestFailed(true);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		dummyInventory.readFromNBT(nbttagcompound, "");
		_requestPartials = nbttagcompound.getBoolean("requestpartials");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("requestpartials", _requestPartials);
    }
	
	@Override
	public void itemLost(ItemIdentifier item) {
		if (_requestedItems.containsKey(item)){
			_requestedItems.put(item, _requestedItems.get(item) - 1);
		}
	}

	@Override
	public void itemArrived(ItemIdentifier item) {
		super.resetThrottle();
		if (_requestedItems.containsKey(item)){
			_requestedItems.put(item, _requestedItems.get(item) - 1);
		}
		
	}
	
	public boolean isRequestingPartials(){
		return _requestPartials;
	}
	
	public void setRequestingPartials(boolean value){
		_requestPartials = value;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		//pause = true; //Pause until GUI is closed //TODO Find a way to handle this
		if(!APIProxy.isClient(entityplayer.worldObj)) {
			//GuiProxy.openGuiLiquidSupplierPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_LiquidSupplier_ID, worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
		return dummyInventory;
	}
}
