/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestManager;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.TileGenericPipe;

public class LogicLiquidSupplier extends BaseRoutingLogic implements IRequireReliableTransport{
	
	private SimpleInventory dummyInventory = new SimpleInventory(9, "Liquids to keep stocked", 127);
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;
	
	public IChassiePowerProvider _power;

	public LogicLiquidSupplier(){
		throttleTime = 100;
	}
	
	@Override
	public void destroy() {}

	
	@Override
	public void throttledUpdateEntity() {
		if (MainProxy.isClient(worldObj)) return;
		super.throttledUpdateEntity();
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof ITankContainer) || tile.tile instanceof TileGenericPipe) continue;
			ITankContainer container = (ITankContainer) tile.tile;
			if (container.getTanks(ForgeDirection.UNKNOWN) == null || container.getTanks(ForgeDirection.UNKNOWN).length == 0) continue;
			
			//How much do I want?
			Map<ItemIdentifier, Integer> wantContainers = dummyInventory.getItemsAndCount();
			HashMap<LiquidIdentifier, Integer> wantLiquids = new HashMap<LiquidIdentifier, Integer>();
			for (Entry<ItemIdentifier, Integer> item : wantContainers.entrySet()){
				ItemStack wantItem = item.getKey().unsafeMakeNormalStack(1);
				LiquidStack liquidstack = LiquidContainerRegistry.getLiquidForFilledItem(wantItem);
				if (liquidstack == null) continue;
				wantLiquids.put(LiquidIdentifier.get(liquidstack), item.getValue() * liquidstack.amount);
			}

			//How much do I have?
			HashMap<LiquidIdentifier, Integer> haveLiquids = new HashMap<LiquidIdentifier, Integer>();
			
			ILiquidTank[] result = container.getTanks(ForgeDirection.UNKNOWN);
			for (ILiquidTank slot : result){
				if (slot.getLiquid() == null || !wantLiquids.containsKey(LiquidIdentifier.get(slot.getLiquid()))) continue;
				Integer liquidWant = haveLiquids.get(LiquidIdentifier.get(slot.getLiquid()));
				if (liquidWant==null){
					haveLiquids.put(LiquidIdentifier.get(slot.getLiquid()), slot.getLiquid().amount);
				} else {
					haveLiquids.put(LiquidIdentifier.get(slot.getLiquid()), liquidWant +  slot.getLiquid().amount);
				}
			}
			
			//HashMap<Integer, Integer> needLiquids = new HashMap<Integer, Integer>();
			//Reduce what I have and what have been requested already
			for (Entry<LiquidIdentifier, Integer> liquidId: wantLiquids.entrySet()){
				Integer haveCount = haveLiquids.get(liquidId.getKey());
				if (haveCount != null){
					liquidId.setValue(liquidId.getValue() - haveCount);
				}
				for (Entry<ItemIdentifier, Integer> requestedItem : _requestedItems.entrySet()){
					ItemStack wantItem = requestedItem.getKey().unsafeMakeNormalStack(1);
					LiquidStack requestedLiquidId = LiquidContainerRegistry.getLiquidForFilledItem(wantItem);
					if (requestedLiquidId == null) continue;
					liquidId.setValue(liquidId.getValue() - requestedItem.getValue() * requestedLiquidId.amount);
				}
			}
			
			//((PipeItemsLiquidSupplier)this.container.pipe).setRequestFailed(false);
			
			//Make request
			
			for (ItemIdentifier need : wantContainers.keySet()){
				LiquidStack requestedLiquidId = LiquidContainerRegistry.getLiquidForFilledItem(need.unsafeMakeNormalStack(1));
				if (requestedLiquidId == null) continue;
				if (!wantLiquids.containsKey(LiquidIdentifier.get(requestedLiquidId))) continue;
				int countToRequest = wantLiquids.get(LiquidIdentifier.get(requestedLiquidId)) / requestedLiquidId.amount;
				if (countToRequest < 1) continue;
				
				if(!_power.useEnergy(11)) {
					break;
				}
				
				boolean success = false;

				if(_requestPartials) {
					countToRequest = RequestManager.requestPartial(need.makeStack(countToRequest), (IRequestItems) this.container.pipe);
					if(countToRequest > 0) {
						success = true;
					}
				} else {
					success = RequestManager.request(need.makeStack(countToRequest), (IRequestItems) this.container.pipe, null);
				}
				
				if (success){
					Integer currentRequest = _requestedItems.get(need);
					if(currentRequest==null) {
						_requestedItems.put(need, countToRequest);
					} else {
						_requestedItems.put(need, currentRequest + countToRequest);
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
	
	private void decreaseRequested(ItemIdentifierStack item) {
		int remaining = item.stackSize;
		//see if we can get an exact match
		Integer count = _requestedItems.get(item.getItem());
		if (count != null) {
			_requestedItems.put(item.getItem(), Math.max(0, count - remaining));
			remaining -= count;
		}
		if(remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		for(Entry<ItemIdentifier, Integer> e : _requestedItems.entrySet()) {
			if(e.getKey().itemID == item.getItem().itemID && e.getKey().itemDamage == item.getItem().itemDamage) {
				int expected = e.getValue();
				e.setValue(Math.max(0, expected - remaining));
				remaining -= expected;
			}
			if(remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		LogisticsPipes.requestLog.info("liquid supplier got unexpected item " + item.toString());
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		decreaseRequested(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
		decreaseRequested(item);
		delayThrottle();
	}
	
	public boolean isRequestingPartials(){
		return _requestPartials;
	}
	
	public void setRequestingPartials(boolean value){
		_requestPartials = value;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(MainProxy.isServer(entityplayer.worldObj)) {
			//GuiProxy.openGuiLiquidSupplierPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_LiquidSupplier_ID, worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
		return dummyInventory;
	}
}
