/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.transport;

import java.util.HashMap;
import java.util.Iterator;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.network.packets.PacketPipeLogisticsContent;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.network.PacketPipeTransportContent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PipeTransportLogistics extends PipeTransportItems {

	private final int _bufferTimeOut = 20 * 2; //2 Seconds
	
//	private class ResolvedRoute{
//		UUID bestRouter;
//		boolean isDefault;
//	}
	
	private RoutedPipe _pipe = null;
	
	private final HashMap<ItemStack,Integer> _itemBuffer = new HashMap<ItemStack, Integer>(); 
	
	public PipeTransportLogistics() {
		allowBouncing = true;
		travelHook = new LogisticsItemTravelingHook(worldObj, xCoord, yCoord, zCoord, this);
	}

//	private ResolvedRoute resolveRoute(EntityData data){
//		ResolvedRoute route = new ResolvedRoute();
//		
//		ResolvedDestination p = SimpleServiceLocator.logisticsManager.getPassiveDestinationFor(ItemIdentifier.get(data.item.item), _pipe.getRouter().getId());
//		
//		if (p != null){
//			route.bestRouter = p.bestRouter;
//			route.isDefault = p.isDefault;
//		} else {
//			route.bestRouter = core_LogisticsPipes.logisticsManager.getDestinationFor(ItemIdentifier.get(data.item.item), _pipe.getRouter().getRouteTable().keySet());
//			if (route.bestRouter == null){
//				route.bestRouter = core_LogisticsPipes.logisticsManager.getDestinationFor(null, _pipe.getRouter().getRouteTable().keySet());
//				route.isDefault = true;
//			}
//		}
//		return route;
//	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!_itemBuffer.isEmpty()){
			Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
			while (iterator.hasNext()){
				ItemStack next = iterator.next();
				int currentTimeOut = _itemBuffer.get(next);
				if (currentTimeOut > 0){
					_itemBuffer.put(next, currentTimeOut - 1 );
				} else {
					EntityPassiveItem item = new EntityPassiveItem(container.pipe.worldObj, this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(next) - 0.1, this.zCoord + 0.5, next);
					IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(container.pipe.worldObj, item);
					routedItem.setDoNotBuffer(true);
					this.entityEntering(routedItem.getEntityPassiveItem(), Orientations.YPos);
					iterator.remove();
				}
			}
		}
	}
	
	public void dropBuffer(){
		Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
		while (iterator.hasNext()){
			ItemStack next = iterator.next();
			SimpleServiceLocator.buildCraftProxy.dropItems(this.container.worldObj, next, this.xCoord, this.yCoord, this.zCoord);
			iterator.remove();
		}
	}
	
	@Override
	public void entityEntering(IPipedItem item, Orientations orientation) {
		if(MainProxy.isServer()) {
			EntityData data = travelingEntities.get(item.getEntityId());
			if(data != null && item instanceof RoutedEntityItem) {
				RoutedEntityItem routed = (RoutedEntityItem) item;
				for(EntityPlayer player:MainProxy.getPlayerArround(worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE)) {
					if(!routed.isKnownBy(player)) {
						PacketDispatcher.sendPacketToPlayer(createItemPacket(data), (Player)player);
						if(routed.getDestination() != null) { 
							routed.addKnownPlayer(player);
						}
					}
				}
			}
		}
		super.entityEntering(item, orientation);
	}
	
	@Override
	public void unscheduleRemoval(IPipedItem item) {
		super.unscheduleRemoval(item);
		if(item instanceof IRoutedItem) {
			IRoutedItem routed = (IRoutedItem)item;
			routed.changeDestination(null);
			EntityData data = travelingEntities.get(item.getEntityId());
			data.item = routed.getNewEntityPassiveItem();
		}
	}

	@Override
	public Orientations resolveDestination(EntityData data) {
		
		if (_pipe == null){
			_pipe = (RoutedPipe) container.pipe;
		}
		
		if(data.item != null && data.item.getItemStack() != null) {
			_pipe.relayedItem(data.item.getItemStack().stackSize);
		}
		
		boolean forcePacket = false;
		if(!(data.item instanceof IRoutedItem)) {
			forcePacket = true;
		} else if(((IRoutedItem)data.item).getDestination() == null) {
			forcePacket = true;
		}
		
		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(_pipe.worldObj, data);
		Orientations value =_pipe.getRouteLayer().getOrientationForItem(routedItem);
		if (value == null && MainProxy.isClient()) {
			routedItem.getItemStack().stackSize = 0;
			scheduleRemoval(data.item);
			return Orientations.Unknown;
		} else if (value == null) {
			System.out.println("THIS IS NOT SUPPOSED TO HAPPEN!");
			return Orientations.Unknown;
		}
		if (value == Orientations.Unknown && !routedItem.getDoNotBuffer()){
			if(MainProxy.isServer()) {
				PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, worldObj.getWorldInfo().getDimension(), createItemPacket(data));
			}
			_itemBuffer.put(routedItem.getItemStack().copy(), 20 * 2);
			//routedItem.getItemStack().stackSize = 0;	//Hack to make the item disappear
			scheduleRemoval(data.item);			
			return Orientations.XNeg;
		}
		
		readjustSpeed(routedItem.getEntityPassiveItem());
		
		if(MainProxy.isServer()) {
			if(routedItem instanceof RoutedEntityItem) {
				RoutedEntityItem routed = (RoutedEntityItem) routedItem;
				for(EntityPlayer player:MainProxy.getPlayerArround(worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE)) {
					if(!routed.isKnownBy(player) || forcePacket) {
						PacketDispatcher.sendPacketToPlayer(createItemPacket(data), (Player)player);
						if(!forcePacket) {
							routed.addKnownPlayer(player);
						}
					}
				}
			}
		}
		
		if (value == Orientations.Unknown ){ 
			//Reduce the speed of items being dropped so they don't go all over the place
			data.item.setSpeed(Math.min(data.item.getSpeed(), Utils.pipeNormalSpeed * 5F));
		}
		
		_pipe.queueEvent("item_direction", new Object[]{ItemIdentifier.get(data.item.getItemStack()).getId(), data.item.getItemStack().stackSize, value.ordinal()});
		
		return value;
		
//		if (!(data.item instanceof RoutedEntityItem )){
//			ResolvedRoute bestRoute = resolveRoute(data);
//			
//			if (bestRoute.bestRouter == null){
//				//No idea where to send this. Drop it
//				return Orientations.Unknown;
//			}
//			
//			RoutedEntityItem newItem = new RoutedEntityItem(this.worldObj, data.item);
//			newItem.setDefaultRouted(bestRoute.isDefault);
//			newItem.sourceUUID = _pipe.getRouter().getId();
//			newItem.destinationUUID = bestRoute.bestRouter;
//			_pipe.getRouter().startTrackingRoutedItem(newItem);
//			SimpleServiceLocator.routerManager.getRouter(bestRoute.bestRouter).startTrackingInboundItem(newItem);
//			data.item = newItem;
//			
//			if (bestRoute.bestRouter != _pipe.getRouter().getId()){
//				return _pipe.getRouter().getExitFor(bestRoute.bestRouter);
//			}
//			
//			//If the best destination is the current router, continue
//		}
//		RoutedEntityItem item = (RoutedEntityItem)data.item;
//		//a routed item with destination stripped. Check if we can get new route		
//		if (item.destinationUUID == null)	{
//			ResolvedRoute bestRoute = resolveRoute(data);
//			
//			if (bestRoute.bestRouter == null){
//				//Unable to find a new destination, drop it
//				return Orientations.Unknown;
//			}
//			item.setDefaultRouted(bestRoute.isDefault);
//			item.destinationUUID = bestRoute.bestRouter;
//			SimpleServiceLocator.routerManager.getRouter(bestRoute.bestRouter).startTrackingInboundItem(item);
//		}
//		
//		if (item.destinationUUID == _pipe.getRouter().getId()){
//			return destinationReached(item);
//		}
//
//		Orientations exit = _pipe.getRouter().getExitFor(item.destinationUUID); 
//		if (exit != null) {
//			item.refreshSpeed();
//			if (item.sourceUUID != _pipe.getRouter().getId()){
//				_pipe.stat_lifetime_relayed++;
//				_pipe.stat_session_relayed++;
//			}
//			return(exit);
//		}
//		
//		//No route, attempt to reroute!
//		ResolvedRoute bestRoute = resolveRoute(data);
//		if (bestRoute.bestRouter == null){
//			//Failed, drop package
//			return Orientations.Unknown;
//		}
//		item.changeDestination(bestRoute.bestRouter);
//		item.setDefaultRouted(bestRoute.isDefault);
//		if (item.destinationUUID == _pipe.getRouter().getId()){
//			return destinationReached(item);
//		}
//		return _pipe.getRouter().getExitFor(item.destinationUUID);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		_itemBuffer.clear();
		
        NBTTagList nbttaglist = nbttagcompound.getTagList("buffercontents");
        for(int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            _itemBuffer.put(ItemStack.loadItemStackFromNBT(nbttagcompound1), _bufferTimeOut);
        }
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		NBTTagList nbttaglist = new NBTTagList();
        //ItemStack[] offspring = spawn.toArray(new ItemStack[spawn.size()]);
		for (ItemStack stack : _itemBuffer.keySet()){
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            stack.writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
		}
        nbttagcompound.setTag("buffercontents", nbttaglist);
		
	}
	
	@Override
	public void readjustSpeed(IPipedItem item) {	
		if (SimpleServiceLocator.buildCraftProxy.isRoutedItem(item)){
			
			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(item); 
			float defaultBoost = 1F;
			
			switch (routedItem.getTransportMode()){
			case Default:
				defaultBoost = 10F;
				break;
			case Passive:
				defaultBoost = 20F;
				break;
			case Active:
				defaultBoost = 30F;
				break;
			
			}
			float add = Math.max(item.getSpeed(), Utils.pipeNormalSpeed * defaultBoost) - item.getSpeed();
			if (_pipe == null){
				_pipe = (RoutedPipe) container.pipe;
			}
			if(_pipe.useEnergy(Math.round(add * 25))) {
				item.setSpeed(Math.max(item.getSpeed(), Utils.pipeNormalSpeed * defaultBoost));
			}
		}
	}

	/**
	 * Handles a packet describing a stack of items inside a pipe.
	 * 
	 * @param packet
	 */
	@Override
	public void handleItemPacket(PacketPipeTransportContent packet) {
		if (packet.getID() != PacketIds.PIPE_CONTENTS)
			return;
		
		if(!PacketPipeLogisticsContent.isPacket(packet)) {
			super.handleItemPacket(packet);
			return;
		}
		
		IPipedItem item = EntityPassiveItem.getOrCreate(worldObj, packet.getEntityId());

		item.setItemStack(new ItemStack(packet.getItemId(), packet.getStackSize(), packet.getItemDamage()));

		item.setPosition(packet.getPosX(), packet.getPosY(), packet.getPosZ());
		item.setSpeed(packet.getSpeed());
		
		if(SimpleServiceLocator.buildCraftProxy.isRoutedItem(item)) {
			if (item.getContainer() != this.container || !travelingEntities.containsKey(item.getEntityId())) {
				if (item.getContainer() != null) {
					((PipeTransportItems) ((TileGenericPipe) item.getContainer()).pipe.transport).scheduleRemoval(item);
				}
				EntityData entity = new EntityData(item, packet.getInputOrientation());
				entity.output = packet.getOutputOrientation();
				travelingEntities.put(new Integer(item.getEntityId()), entity);
				item.setContainer(container);
			} else {
				travelingEntities.get(new Integer(item.getEntityId())).item = item;
				travelingEntities.get(new Integer(item.getEntityId())).input = packet.getInputOrientation();
				travelingEntities.get(new Integer(item.getEntityId())).output = packet.getOutputOrientation();
			}
			PacketPipeLogisticsContent newpacket = new PacketPipeLogisticsContent(packet);
			IRoutedItem routed = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(item);
			routed.setSource(newpacket.getSourceUUID(this.worldObj));
			routed.setDestination(newpacket.getDestUUID(this.worldObj));
			routed.setTransportMode(newpacket.getTransportMode());
			travelingEntities.get(new Integer(item.getEntityId())).item = routed.getEntityPassiveItem();
			return;
		}
		PacketPipeLogisticsContent newpacket = new PacketPipeLogisticsContent(packet);
		IRoutedItem routed = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.worldObj,item);
		routed.setSource(newpacket.getSourceUUID(this.worldObj));
		routed.setDestination(newpacket.getDestUUID(this.worldObj));
		routed.setTransportMode(newpacket.getTransportMode());
		item = routed.getEntityPassiveItem();
		if (item.getContainer() != this.container || !travelingEntities.containsKey(item.getEntityId())) {
			if (item.getContainer() != null) {
				((PipeTransportItems) ((TileGenericPipe) item.getContainer()).pipe.transport).scheduleRemoval(item);
			}
			EntityData entity = new EntityData(item, packet.getInputOrientation());
			entity.output = packet.getOutputOrientation();
			travelingEntities.put(new Integer(item.getEntityId()), entity);
			item.setContainer(container);
		} else {
			travelingEntities.get(new Integer(item.getEntityId())).item = item;
			travelingEntities.get(new Integer(item.getEntityId())).input = packet.getInputOrientation();
			travelingEntities.get(new Integer(item.getEntityId())).output = packet.getOutputOrientation();
		}
	}

	/**
	 * Creates a packet describing a stack of items inside a pipe.
	 * 
	 * @param item
	 * @param orientation
	 * @return
	 */
	@Override
	public Packet createItemPacket(EntityData data) {
		if(data.item instanceof RoutedEntityItem) {
			PacketPipeLogisticsContent packet = new PacketPipeLogisticsContent(container.xCoord, container.yCoord, container.zCoord, data);

			return packet.getPacket();
		} else {
			return super.createItemPacket(data);
		}
	}

	/*	private Orientations destinationReached(RoutedEntityItem item) {
//		item.arrived = true;
//		if (item.sourceUUID != null && SimpleServiceLocator.routerManager.getRouter(item.sourceUUID) != null){
//			SimpleServiceLocator.routerManager.getRouter(item.sourceUUID).outboundItemArrived(item);
//		}
//		if  (item.destinationUUID != null && SimpleServiceLocator.routerManager.getRouter(item.destinationUUID) != null){
//			SimpleServiceLocator.routerManager.getRouter(item.destinationUUID).inboundItemArrived(item);
//		}
//		
//		_pipe.stat_lifetime_recieved++;
//		_pipe.stat_session_recieved++;
//		
//		item.destinationUUID = null;
//		
//		//0) Deliver according to chassi orientation
//		if (this._pipe instanceof PipeLogisticsChassi){
//			
//			return ((PipeLogisticsChassi)this._pipe).getPointedOrientation();
//		}
//		
//		//1) Deliver to attached chest/non-pipe
//		LinkedList<Orientations> possible = getPossibleMovements(getPosition(), item);
//		Iterator<Orientations> iterator = possible.iterator();
//		while (iterator.hasNext())	{
//			Position p = new Position(_pipe.xCoord, _pipe.yCoord, _pipe.zCoord, iterator.next());
//			p.moveForwards(1);
//			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
//			if (tile instanceof TileGenericPipe){
//				iterator.remove();
//			}
//		}
//		if (possible.size() > 0){
//			return possible.get(worldObj.rand.nextInt(possible.size()));
//		}
//		
//		//2) deliver to attached pipe that does not have a route
//		LinkedList<Orientations> nonRoutes = _pipe.getRouter().GetNonRoutedExits();
//		iterator = nonRoutes.iterator();
//		while(iterator.hasNext()) {
//			Position p = new Position(_pipe.xCoord, _pipe.yCoord, _pipe.zCoord, iterator.next());
//			p.moveForwards(1);
//			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z); 
//			if (tile == null || !( tile instanceof TileGenericPipe) || !((TileGenericPipe)tile).acceptItems()){
//				iterator.remove();
//			}
//		}
//
//		if (nonRoutes.size() != 0){
//			return nonRoutes.get(worldObj.rand.nextInt(nonRoutes.size()));
//		}
//		
//		//3) Eject				
//		return Orientations.Unknown;
//	}
 */
}
