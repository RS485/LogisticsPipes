/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class RoutedEntityItem extends EntityPassiveItem implements IRoutedItem{

	public UUID sourceUUID;
	public UUID destinationUUID;
	
	//public UUID lastHop;
	//private boolean _isDefaultRouted;
	//private boolean _isPassive;
	private boolean _doNotBuffer;
	//private float _speedBoost;
	public boolean arrived;
	
	private TransportMode _transportMode = TransportMode.Unknown;
	
	private List<EntityPlayer> knownBy = new ArrayList<EntityPlayer>();
	
	public RoutedEntityItem(World world, IPipedItem entityItem) {
		super(world, entityItem.getEntityId());
		container = entityItem.getContainer();
		deterministicRandomization = entityItem.getDeterministicRandomization();
		position = entityItem.getPosition();
		speed = entityItem.getSpeed();
		synchroTracker = entityItem.getSynchroTracker();
		item = entityItem.getItemStack(); 
	}
	
	@Override
	public EntityItem toEntityItem(Orientations dir) {
		return super.toEntityItem(dir);
	}
	
	public void addKnownPlayer(EntityPlayer player) {
		knownBy.add(player);
	}
	
	public boolean isKnownBy(EntityPlayer player) {
		return knownBy.contains(player);
	}
	
//	public boolean isDefaultRouted(){
//		return _isDefaultRouted;
//	}
	
//	public void setDefaultRouted(boolean isDefault){
//		_isDefaultRouted = isDefault;
//		refreshSpeed();
//	}
	
//	public void refreshSpeed(){
//		speed = Math.max(speed, Utils.pipeNormalSpeed * (_isDefaultRouted?core_LogisticsPipes.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER : core_LogisticsPipes.LOGISTICS_ROUTED_SPEED_MULTIPLIER));
//	}
	
	public void changeDestination(UUID newDestination){
		if (destinationUUID != null && SimpleServiceLocator.routerManager.isRouter(destinationUUID)){
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationUUID);

			destinationRouter.itemDropped(this);
			
			if (!arrived && destinationRouter .getPipe() != null && destinationRouter.getPipe().logic instanceof IRequireReliableTransport){
				((IRequireReliableTransport)destinationRouter.getPipe().logic).itemLost(ItemIdentifier.get(item));
			}
		}
		destinationUUID = newDestination;
	}
	
	@Override
	public void remove() {
		
		if (sourceUUID != null && SimpleServiceLocator.routerManager.isRouter(sourceUUID)) {
			SimpleServiceLocator.routerManager.getRouter(sourceUUID).itemDropped(this);
		}
		
		if (destinationUUID != null && SimpleServiceLocator.routerManager.isRouter(destinationUUID)){
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationUUID); 
			destinationRouter.itemDropped(this);
			if (!arrived && destinationRouter.getPipe() != null && destinationRouter.getPipe().logic instanceof IRequireReliableTransport){
				((IRequireReliableTransport)destinationRouter.getPipe().logic).itemLost(ItemIdentifier.get(item));
			}
		}
		super.remove();
	}

	@Override
	public UUID getDestination() {
		return this.destinationUUID;
	}

	@Override
	public ItemStack getItemStack() {
		return this.item;
	}

	@Override
	public void setDestination(UUID destination) {
		this.destinationUUID = destination;
		
	}

	@Override
	public UUID getSource() {
		return this.sourceUUID;
	}

	@Override
	public void setSource(UUID source) {
		this.sourceUUID = source;
	}

//	@Override
//	public boolean isPassive() {
//		return this._isPassive;
//	}

//	@Override
//	public void setPassive(boolean isPassive) {
//		this._isPassive = isPassive;
//	}

//	@Override
//	public boolean isDefault() {
//		return this._isDefaultRouted;
//	}

//	@Override
//	public void setDefault(boolean isDefault) {
//		this._isDefaultRouted = isDefault;
//	}

	@Override
	public void setDoNotBuffer(boolean isBuffered) {
		_doNotBuffer = isBuffered;
	}

	@Override
	public boolean getDoNotBuffer() {
		return _doNotBuffer;
	}

	@Override
	public EntityPassiveItem getEntityPassiveItem() {
		return this;
	}

	@Override
	@Deprecated
	public void setArrived() {
		this.arrived = true;
	}

	@Override
	public IRoutedItem split(World worldObj, int itemsToTake, Orientations orientation) {
		EntityPassiveItem newItem = new EntityPassiveItem(worldObj);
		newItem.setPosition(position.x, position.y, position.z);
		newItem.setSpeed(this.speed);
		newItem.setItemStack(this.item.splitStack(itemsToTake));
		
		if (this.container instanceof TileGenericPipe && ((TileGenericPipe)this.container).pipe.transport instanceof PipeTransportItems){
			if (((TileGenericPipe)this.container).pipe instanceof PipeLogisticsChassi){
				PipeLogisticsChassi chassi = (PipeLogisticsChassi) ((TileGenericPipe)this.container).pipe;
				chassi.queueRoutedItem(SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(worldObj, newItem), orientation.reverse());
				
			}
			//((PipeTransportItems)((TileGenericPipe)this.container).pipe.transport).entityEntering(newItem, orientation);
				
		}
		
		return SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(worldObj, newItem);
		
	}

	@Override
	public void SetPosition(double x, double y, double z) {
		this.position = new Position(x,y,z);
	}

	@Override
	public void setTransportMode(TransportMode transportMode) {
		this._transportMode = transportMode;
		
	}

	@Override
	public TransportMode getTransportMode() {
		return this._transportMode;
	}
	
	public boolean hasContributions() {
		//prevent groupEntities()
		try {
			@SuppressWarnings("restriction")
			final Class<?> caller = sun.reflect.Reflection.getCallerClass(3);
			if(caller.equals(PipeTransportItems.class)) {
				return true;
			}
			return super.hasContributions();
		} catch(Exception e) {
			return true;
		}
	}
}
