/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestManager;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.utils.Utils;
import buildcraft.energy.EngineWood;
import buildcraft.energy.TileEngine;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class LogicSupplier extends BaseRoutingLogic implements IRequireReliableTransport{
	
	private SimpleInventory dummyInventory = new SimpleInventory(9, "Items to keep stocked", 127);
	
	private final InventoryUtilFactory _invUtilFactory;
	private final InventoryUtil _dummyInvUtil;
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;

	public boolean pause = false;
	
	public IChassiePowerProvider _power;
	
	public LogicSupplier() {
		this(new InventoryUtilFactory());
	}
	
	public LogicSupplier(InventoryUtilFactory inventoryUtilFactory){
		_invUtilFactory = inventoryUtilFactory;
		_dummyInvUtil = _invUtilFactory.getInventoryUtil(dummyInventory);
		throttleTime = 100;
	}
	
	@Override
	public void destroy() {}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		//pause = true; //Pause until GUI is closed //TODO Find a way to handle this
		if(MainProxy.isServer(entityplayer.worldObj)) {
			//GuiProxy.openGuiSupplierPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SupplierPipe_ID, worldObj, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, xCoord, yCoord, zCoord, isRequestingPartials() ? 1 : 0).getPacket(), (Player)entityplayer);
		}
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
		return dummyInventory;
	}

	@Override
	public void throttledUpdateEntity() {
		
		if (!((CoreRoutedPipe)this.container.pipe).isEnabled()){
			return;
		}
		
		if(MainProxy.isClient(this.worldObj)) return;
		if (pause) return;
		super.throttledUpdateEntity();
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			
			//Do not attempt to supply redstone engines
			if (tile.tile instanceof TileEngine && ((TileEngine)tile.tile).engine instanceof EngineWood) continue;
			
			IInventory inv = Utils.getInventory((IInventory) tile.tile);
			if (inv.getSizeInventory() < 1) continue;
			InventoryUtil invUtil = _invUtilFactory.getInventoryUtil(inv);
			
			//How many do I want?
			HashMap<ItemIdentifier, Integer> needed = _dummyInvUtil.getItemsAndCount();
			
			//How many do I have?
			HashMap<ItemIdentifier, Integer> have = invUtil.getItemsAndCount();
			
			//Reduce what I have
			for (ItemIdentifier item : needed.keySet()){
				if (have.containsKey(item)){
					needed.put(item, needed.get(item) - have.get(item));
				}
			}
			
			//Reduce what have been requested already
			for (ItemIdentifier item : needed.keySet()){
				if (_requestedItems.containsKey(item)){
					needed.put(item, needed.get(item) - _requestedItems.get(item));
				}
			}
			
			((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(false);
			
			LinkedList<IRouter> valid = getRouter().getIRoutersByCost();
			
			/*
			//TODO Double Chests, Simplyfication
			// Filter out providers attached to this inventory so that we don't get stuck in an
			// endless supply/provide loop on this inventory.

			WorldUtil invWU = new WorldUtil(tile.tile.worldObj, tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord);
			ArrayList<IProvideItems> invProviders = new ArrayList<IProvideItems>();

			for (AdjacentTile atile : invWU.getAdjacentTileEntities()) {
				if ((atile.tile instanceof TileGenericPipe)) {
					Pipe p = ((TileGenericPipe) atile.tile).pipe;
					if ((p instanceof IProvideItems)) {
						invProviders.add((IProvideItems) p);
					}
				}
			}

			for (IRouter r : valid) {
				CoreRoutedPipe cp = r.getPipe();
				if (((cp instanceof IProvideItems)) && (invProviders.contains((IProvideItems) cp))) {
					valid.remove(r);
				}
			}
			*/
			
			//Make request
			for (ItemIdentifier need : needed.keySet()){
				if (needed.get(need) < 1) continue;
				int neededCount = needed.get(need);
				
				if(!_power.useEnergy(10)) {
					break;
				}
				
				boolean success = false;
				do{ 
					success = RequestManager.request(need.makeStack(neededCount),  (IRequestItems) container.pipe, valid, null);
					if (success || neededCount == 1){
						break;
					}
					neededCount = neededCount / 2;
				} while (_requestPartials && !success);
				
				if (success){
					if (!_requestedItems.containsKey(need)){
						_requestedItems.put(need, neededCount);
					}else
					{
						_requestedItems.put(need, _requestedItems.get(need) + neededCount);
					}
				} else{
					((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(true);
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
}
