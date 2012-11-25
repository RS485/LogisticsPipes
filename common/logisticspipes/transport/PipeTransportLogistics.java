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
import java.util.LinkedList;
import java.util.List;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.Pair;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.common.network.Player;

public class PipeTransportLogistics extends PipeTransportItems {

	private final int _bufferTimeOut = 20 * 2; //2 Seconds
	
//	private class ResolvedRoute{
//		UUID bestRouter;
//		boolean isDefault;
//	}
	
	private RoutedPipe _pipe = null;
	
	private final HashMap<ItemStack,Pair<Integer /* Time */, Integer /* BufferCounter */>> _itemBuffer = new HashMap<ItemStack, Pair<Integer, Integer>>(); 
	
	public PipeTransportLogistics() {
		allowBouncing = true;
		travelHook = new LogisticsItemTravelingHook(worldObj, xCoord, yCoord, zCoord, this);
	}
	
	private RoutedPipe getPipe() {
		if (_pipe == null){
			_pipe = (RoutedPipe) container.pipe;
		}
		return _pipe;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!_itemBuffer.isEmpty()){
			List<IRoutedItem> toAdd = new LinkedList<IRoutedItem>();
			Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
			while (iterator.hasNext()){
				ItemStack next = iterator.next();
				int currentTimeOut = _itemBuffer.get(next).getValue1();
				if (currentTimeOut > 0){
					_itemBuffer.get(next).setValue1(currentTimeOut - 1);
				} else {
					EntityPassiveItem item = new EntityPassiveItem(container.pipe.worldObj, this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(next) - 0.1, this.zCoord + 0.5, next);
					IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(container.pipe.worldObj, item);
					routedItem.setDoNotBuffer(true);
					routedItem.setBufferCounter(_itemBuffer.get(next).getValue2() + 1);
					toAdd.add(routedItem);
					iterator.remove();
				}
			}
			for(IRoutedItem item:toAdd) {
				this.entityEntering(item.getEntityPassiveItem(), ForgeDirection.UP);
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
	public void entityEntering(IPipedItem item, ForgeDirection orientation) {
		if(MainProxy.isServer()) {
			EntityData data = travelingEntities.get(item.getEntityId());
			if(data != null && item instanceof RoutedEntityItem) {
				RoutedEntityItem routed = (RoutedEntityItem) item;
				for(EntityPlayer player:MainProxy.getPlayerArround(worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE)) {
					if(!routed.isKnownBy(player)) {
						MainProxy.sendPacketToPlayer(createItemPacket(data), (Player)player);
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
			IRoutedItem newRoute = routed.getNewUnRoutedItem();
			data.item = newRoute.getEntityPassiveItem();
			newRoute.setReRoute(true);
			newRoute.addToJamList(getPipe().getRouter());
		}
	}
	
	@Override
	public ForgeDirection resolveDestination(EntityData data) {
		
		if(data.item != null && data.item.getItemStack() != null) {
			getPipe().relayedItem(data.item.getItemStack().stackSize);
		}
		
		boolean forcePacket = false;
		if(!(data.item instanceof IRoutedItem)) {
			forcePacket = true;
		} else if(((IRoutedItem)data.item).getDestination() == null || ((IRoutedItem)data.item).isUnRouted()) {
			forcePacket = true;
		}
		
		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(getPipe().worldObj, data);
		ForgeDirection value = getPipe().getRouteLayer().getOrientationForItem(routedItem);
		routedItem.setReRoute(false);
		if (value == null && MainProxy.isClient()) {
			routedItem.getItemStack().stackSize = 0;
			scheduleRemoval(data.item);
			return ForgeDirection.UNKNOWN;
		} else if (value == null) {
			System.out.println("THIS IS NOT SUPPOSED TO HAPPEN!");
			return ForgeDirection.UNKNOWN;
		}
		if (value == ForgeDirection.UNKNOWN && !routedItem.getDoNotBuffer()){
			if(MainProxy.isServer()) {
				MainProxy.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, worldObj.getWorldInfo().getDimension(), createItemPacket(data));
			}
			_itemBuffer.put(routedItem.getItemStack().copy(), new Pair<Integer,Integer>(20 * 2, routedItem.getBufferCounter()));
			routedItem.getItemStack().stackSize = 0;	//Hack to make the item disappear
			scheduleRemoval(data.item);			
			return ForgeDirection.WEST;
		}
		
		readjustSpeed(routedItem.getEntityPassiveItem());
		
		if(MainProxy.isServer()) {
			if(routedItem instanceof RoutedEntityItem) {
				RoutedEntityItem routed = (RoutedEntityItem) routedItem;
				for(EntityPlayer player:MainProxy.getPlayerArround(worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE)) {
					if(!routed.isKnownBy(player) || forcePacket) {
						MainProxy.sendPacketToPlayer(createItemPacket(data), (Player)player);
						if(!forcePacket) {
							routed.addKnownPlayer(player);
						}
					}
				}
			}
		}
		
		if (value == ForgeDirection.UNKNOWN ){ 
			//Reduce the speed of items being dropped so they don't go all over the place
			data.item.setSpeed(Math.min(data.item.getSpeed(), Utils.pipeNormalSpeed * 5F));
		}
		
		return value;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);


		_itemBuffer.clear();
		
        NBTTagList nbttaglist = nbttagcompound.getTagList("buffercontents");
        for(int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            _itemBuffer.put(ItemStack.loadItemStackFromNBT(nbttagcompound1), new Pair<Integer, Integer>(_bufferTimeOut, 0));
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
			if(getPipe().useEnergy(Math.round(add * 25))) {
				item.setSpeed(Math.max(item.getSpeed(), Utils.pipeNormalSpeed * defaultBoost));
			}
		}
	}
}
