/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.IRequireReliableTransport;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketCoordinates;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.InventoryUtilFactory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.WorldUtil;

public class LogicCrafting extends BaseRoutingLogic implements IRequireReliableTransport {

	private final SimpleInventory _dummyInventory = new SimpleInventory(10, "Requested items", 127);
	private final InventoryUtilFactory _invUtilFactory;
	private final InventoryUtil _dummyInvUtil;
	
	private final LinkedList<ItemIdentifier> _lostItems = new LinkedList<ItemIdentifier>(); 
	
	public int SatelliteId = 0;
	
	public LogicCrafting() {
		this(new InventoryUtilFactory());
	}
	
	public LogicCrafting(InventoryUtilFactory invUtilFactory){
		_invUtilFactory = invUtilFactory;
		_dummyInvUtil = _invUtilFactory.getInventoryUtil(_dummyInventory);
		throttleTime = 40;
	}
	
	/** SATELLITE CODE **/
	
	private int getNextConnectSatelliteId(boolean prev){
		HashMap<Router, Orientations> routes = getRouter().getRouteTable();
		int closestIdFound = prev?0:Integer.MAX_VALUE;
		for (LogicSatellite satellite : LogicSatellite.AllSatellites){
			if (routes.containsKey(satellite.getRouter())){
				if (!prev && satellite.satelliteId > SatelliteId && satellite.satelliteId < closestIdFound) {
					closestIdFound = satellite.satelliteId;
				} else if (prev && satellite.satelliteId < SatelliteId && satellite.satelliteId > closestIdFound){
					closestIdFound = satellite.satelliteId;
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE)
			return SatelliteId;
		
		return closestIdFound;
		
	}
	
	public void setNextSatellite() {
		SatelliteId = getNextConnectSatelliteId(false);
		
		if(APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}
	
	public void setPrevSatellite() {
		SatelliteId = getNextConnectSatelliteId(true);

		if(APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}

	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId) {
		this.SatelliteId = satelliteId;
	}
	
	public boolean isSatelliteConnected(){
		for (LogicSatellite satellite : LogicSatellite.AllSatellites){
			if (satellite.satelliteId == SatelliteId){
				if (this.getRouter().getRouteTable().containsKey(satellite.getRouter())){
					return true;
				}
			}
		}
		return false;
	}
	
	public IRouter getSatelliteRouter(){
		for (LogicSatellite satellite : LogicSatellite.AllSatellites){
			if (satellite.satelliteId == SatelliteId){
				return satellite.getRouter();
			}
		}
		return null;
	}
	
	public void paintPathToSatellite() {
		IRouter satelliteRouter = getSatelliteRouter();
		if (satelliteRouter == null) return;
		
		this.getRouter().displayRouteTo(satelliteRouter);
		
		// No need for networking here
	}
	
	
	/** OTHER CODE **/
	
	public int RequestsItem(ItemIdentifier item) {
		if (item == null){
			return 0;
		}
		return _dummyInvUtil.getItemCount(item);
	}
	

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
    	SatelliteId = nbttagcompound.getInteger("satelliteid");
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	_dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setInteger("satelliteid", SatelliteId);
    }
	

	@Override
	public void destroy() {	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(!APIProxy.isClient(entityplayer.worldObj)) {
			//GuiProxy.openGuiCraftingPipe(entityplayer, _dummyInventory, this);
			entityplayer.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_CRAFTINGPIPE_ID, worldObj, xCoord, yCoord, zCoord);
		}
	}

	
	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) return;
		System.out.println("Item lost");
		Iterator<ItemIdentifier> iterator = _lostItems.iterator();
		while(iterator.hasNext()){
			LogisticsRequest request = new LogisticsRequest(iterator.next(), 1, (IRequestItems)this.getRoutedPipe());
			if (LogisticsManager.Request(request, ((RoutedPipe)((TileGenericPipe)this.container).pipe).getRouter().getRoutersByCost(), null)){
				iterator.remove();
			}
		}
	}
	
	@Override
	public void itemArrived(ItemIdentifier item) {}

	@Override
	public void itemLost(ItemIdentifier item) {
		_lostItems.add(item);
	}
	
	public void importFromCraftingTable() {
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> crafters = new LinkedList<AdjacentTile>();
		TileAutoWorkbench bench = null;
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities()){
			if (!(tile.tile instanceof TileAutoWorkbench)) continue;
			bench = (TileAutoWorkbench) tile.tile;
			break;
		}
		if (bench == null) return;
		
		//Import 
		for(int i = 0; i < bench.getSizeInventory(); i++){
			if (i >= _dummyInventory.getSizeInventory() - 1) break;
			ItemStack newStack = bench.getStackInSlot(i) == null ? null : bench.getStackInSlot(i).copy(); 
			_dummyInventory.setInventorySlotContents(i, newStack);
		}
		
		//Compact
		for (int i = 0; i < _dummyInventory.getSizeInventory() - 1; i++){
			ItemStack stackInSlot = _dummyInventory.getStackInSlot(i);
			if (stackInSlot == null) continue;
			ItemIdentifier itemInSlot = ItemIdentifier.get(stackInSlot);
			for (int j = i+1; j < _dummyInventory.getSizeInventory() - 1; j++){
				ItemStack stackInOtherSlot = _dummyInventory.getStackInSlot(j);
				if (stackInOtherSlot == null) continue;
				if (itemInSlot == ItemIdentifier.get(stackInOtherSlot)){
					stackInSlot.stackSize += stackInOtherSlot.stackSize;
					_dummyInventory.setInventorySlotContents(j, null);
				}
			}
		}
		for (int i = 0; i < _dummyInventory.getSizeInventory() - 1; i++){
			if (_dummyInventory.getStackInSlot(i) != null) continue;
			for (int j = i+1; j < _dummyInventory.getSizeInventory() - 1; j++){
				if (_dummyInventory.getStackInSlot(j) == null) continue;
				_dummyInventory.setInventorySlotContents(i, _dummyInventory.getStackInSlot(j));
				_dummyInventory.setInventorySlotContents(j, null);
				break;
			}
		}
		
		_dummyInventory.setInventorySlotContents(9, bench.findRecipe());
		
		// Send packet asking for import
		if(APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_IMPORT, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}

	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}
	
	/*** INTERFACE TO PIPE ***/
	public ItemStack getCraftedItem(){
		return _dummyInventory.getStackInSlot(9);
	}
	
	public ItemStack getMaterials(int slotnr){
		return _dummyInventory.getStackInSlot(slotnr);
	}
	
	/*** GUI ***/
	public SimpleInventory get_dummyInventory() {
		return _dummyInventory;
	}
}
