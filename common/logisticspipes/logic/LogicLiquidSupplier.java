/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.HashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestManager;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryUtil;
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
			InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(dummyInventory);
			HashMap<ItemIdentifier, Integer> wantContainers = invUtil.getItemsAndCount();
			HashMap<LiquidIdentifier, Integer> wantLiquids = new HashMap<LiquidIdentifier, Integer>();
			for (ItemIdentifier item : wantContainers.keySet()){
				ItemStack wantItem = item.makeNormalStack(1);
				LiquidStack liquidstack = LiquidContainerRegistry.getLiquidForFilledItem(wantItem);
				if (liquidstack == null) continue;
				wantLiquids.put(LiquidIdentifier.get(liquidstack), wantContainers.get(item) * liquidstack.amount);
			}

			//How much do I have?
			HashMap<LiquidIdentifier, Integer> haveLiquids = new HashMap<LiquidIdentifier, Integer>();
			
			ILiquidTank[] result = container.getTanks(ForgeDirection.UNKNOWN);
			for (ILiquidTank slot : result){
				if (slot.getLiquid() == null || !wantLiquids.containsKey(LiquidIdentifier.get(slot.getLiquid()))) continue;
				if (!haveLiquids.containsKey(LiquidIdentifier.get(slot.getLiquid()))){
					haveLiquids.put(LiquidIdentifier.get(slot.getLiquid()), slot.getLiquid().amount);
				} else {
					haveLiquids.put(LiquidIdentifier.get(slot.getLiquid()), haveLiquids.get(LiquidIdentifier.get(slot.getLiquid())) +  slot.getLiquid().amount);
				}
			}
			
			//HashMap<Integer, Integer> needLiquids = new HashMap<Integer, Integer>();
			//Reduce what I have
			for (LiquidIdentifier liquidId: wantLiquids.keySet()){
				if (haveLiquids.containsKey(liquidId)){
					wantLiquids.put(liquidId, wantLiquids.get(liquidId) - haveLiquids.get(liquidId));
				}
			}
			
			//Reduce what have been requested already
			for (LiquidIdentifier liquidId : wantLiquids.keySet()){
				for (ItemIdentifier requestedItem : _requestedItems.keySet()){
					ItemStack wantItem = requestedItem.makeNormalStack(1);
					LiquidStack requestedLiquidId = LiquidContainerRegistry.getLiquidForFilledItem(wantItem);
					if (requestedLiquidId == null) continue;
					wantLiquids.put(liquidId, wantLiquids.get(liquidId) - _requestedItems.get(requestedItem) * LiquidContainerRegistry.getLiquidForFilledItem(requestedItem.makeNormalStack(1)).amount);
				}
			}
			
			//((PipeItemsLiquidSupplier)this.container.pipe).setRequestFailed(false);
			
			//Make request
			
			HashMap<ItemIdentifier, Integer> allNeededContainers = invUtil.getItemsAndCount();
			for (ItemIdentifier need : allNeededContainers.keySet()){
				LiquidStack requestedLiquidId = LiquidContainerRegistry.getLiquidForFilledItem(need.makeNormalStack(1));
				if (requestedLiquidId == null) continue;
				if (!wantLiquids.containsKey(LiquidIdentifier.get(requestedLiquidId))) continue;
				int countToRequest = wantLiquids.get(LiquidIdentifier.get(requestedLiquidId)) / LiquidContainerRegistry.getLiquidForFilledItem(need.makeNormalStack(1)).amount;
				if (countToRequest < 1) continue;
				
				if(!_power.useEnergy(11)) {
					break;
				}
				
				boolean success = false;
				do{ 
					success = RequestManager.request(need.makeStack(countToRequest),  (IRequestItems) this.container.pipe, getRouter().getIRoutersByCost(), null);
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
	public void itemLost(ItemIdentifierStack item) {
		if (_requestedItems.containsKey(item.getItem())){
			_requestedItems.put(item.getItem(), Math.max(0, _requestedItems.get(item.getItem()) - item.stackSize));
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
		super.resetThrottle();
		if (_requestedItems.containsKey(item.getItem())){
			_requestedItems.put(item.getItem(), Math.max(0, _requestedItems.get(item.getItem()) - item.stackSize));
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
