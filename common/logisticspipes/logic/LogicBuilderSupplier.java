/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import buildcraft.builders.TileBuilder;

public class LogicBuilderSupplier extends BaseRoutingLogic implements IRequireReliableTransport {
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;

	public IRoutedPowerProvider _power;
	
	public boolean pause = false;
	
	
	public LogicBuilderSupplier() {
		throttleTime = 100;
	}
	
	@Override
	public void destroy() {}

	
	@Override
	public void throttledUpdateEntity() {
		if (pause) return;
		super.throttledUpdateEntity();
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof TileBuilder)) continue;
			TileBuilder builder = (TileBuilder) tile.tile;
			
			IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil((IInventory) tile.tile);
			
			//TODO: don't double get
			//How many do I want?
			Collection<ItemStack> neededItems = builder.getNeededItems();
			HashMap<ItemIdentifier, Integer> needed = new HashMap<ItemIdentifier, Integer>();
			if (neededItems == null) return;
			for(ItemStack stack : neededItems){
				ItemIdentifier item = ItemIdentifier.get(stack);
				Integer neededCount = needed.get(item);
				if (neededCount == null){
					needed.put(item, stack.stackSize);
				} else {
					needed.put(item, neededCount + stack.stackSize);
				}
			}
			
			//How many do I have?
			HashMap<ItemIdentifier, Integer> have = invUtil.getItemsAndCount();
			
			//Reduce what I have and what have been requested already
			for (Entry<ItemIdentifier, Integer> item : needed.entrySet()){
				Integer haveCount = have.get(item.getKey());
				if (haveCount != null){
					item.setValue(item.getValue() - haveCount);
				}
				Integer requestedCount =  _requestedItems.get(item.getKey());
				if (requestedCount!=null){
					item.setValue(item.getValue() - requestedCount);
				}
			}
			
			((PipeItemsBuilderSupplierLogistics)this.container.pipe).setRequestFailed(false);
			
			//Make request
			for (Entry<ItemIdentifier, Integer> need : needed.entrySet()){
				Integer amountRequested = need.getValue();
				if (amountRequested==null || amountRequested < 1) continue;
				int neededCount = amountRequested;
				
				if(!_power.useEnergy(15)) {
					break;
				}
				
				boolean success = false;

				ItemIdentifierStack wanted = need.getKey().makeStack(neededCount);
				if(_requestPartials) {
					neededCount = RequestTree.requestPartial(wanted, (IRequestItems) container.pipe);
					if(neededCount > 0) {
						success = true;
					}
				} else {
					success = RequestTree.request(wanted, (IRequestItems) container.pipe, null)>0;
				}
				
				if (success){
					Integer currentRequest = _requestedItems.get(need.getKey());
					if(currentRequest == null) {
						_requestedItems.put(need.getKey(), neededCount);
					} else {
						_requestedItems.put(need.getKey(), currentRequest + neededCount);
					}
				} else {
					((PipeItemsBuilderSupplierLogistics)this.container.pipe).setRequestFailed(true);
				}
				
			}
		}
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
		LogisticsPipes.requestLog.info("builder supplier got unexpected item " + item.toString());
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

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		
	}
}
