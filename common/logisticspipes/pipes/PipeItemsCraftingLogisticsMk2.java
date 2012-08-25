/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.main.LogisticsRequest;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.utils.AdjacentTile;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.Utils;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsCraftingLogisticsMk2 extends PipeItemsCraftingLogistics{
	
	public PipeItemsCraftingLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if ((!_orderManager.hasOrders() && _extras < 1) || worldObj.getWorldTime() % 6 != 0) return;
		
		LinkedList<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1 ) {
			_orderManager.sendFailed();
			return;
		}
		
		for(int i = 0; i < 64; i++) {
			if ((!_orderManager.hasOrders() && _extras < 1)) break;
			//if(!(!_orderManager.hasOrders() && _extras < 1))
			for (AdjacentTile tile : locateCrafters()){
				ItemStack extracted = null; 
				if (tile.tile instanceof ISpecialInventory){
					extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile);
				} else if (tile.tile instanceof IInventory) {
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
						if(LogisticsPipes.DisplayRequests)System.out.println("Extra dropped, " + _extras + " remaining");
						Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, p.orientation.reverse());
						entityPos.moveForwards(0.5);
						EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stackToSend);
						entityItem.setSpeed(Utils.pipeNormalSpeed * LogisticsPipes.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER);
						((PipeTransportItems) transport).entityEntering(entityItem, entityPos.orientation);
					}
				}
			}
			if(_orderManager.hasOrders()) {
				break;
			}
		}
	}

	@Override
	public int getCenterTexture() {
		return LogisticsPipes.LOGISTICSPIPE_CRAFTERMK2_TEXTURE;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}
