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

import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class RoutedEntityItem extends EntityPassiveItem implements IRoutedItem{

	int sourceint = -1;
	int destinationint = -1;
	
	boolean _doNotBuffer;
	
	int bufferCounter = 0;
	
	boolean arrived;
	boolean reRoute;
	boolean isUnrouted;
	
	TransportMode _transportMode = TransportMode.Unknown;
	
	List<Integer> jamlist = new ArrayList<Integer>();
	
	public RoutedEntityItem(World world, IPipedItem entityItem) {
		super(world, entityItem.getEntityId());
		container = entityItem.getContainer();
		position = entityItem.getPosition();
		speed = entityItem.getSpeed();
		item = entityItem.getItemStack();
		if(entityItem.getContribution("routingInformation") == null) {
			this.addContribution("routingInformation", new RoutedEntityItemSaveHandler(this));
		} else {
			RoutedEntityItemSaveHandler settings = (RoutedEntityItemSaveHandler) entityItem.getContribution("routingInformation");
			sourceint = settings.sourceint;
			destinationint = settings.destinationint;
			bufferCounter = settings.bufferCounter;
			arrived = settings.arrived;
			_transportMode = settings.transportMode;
			this.addContribution("routingInformation", new RoutedEntityItemSaveHandler(this));
		}
	}
	
	@Override
	public EntityItem toEntityItem(ForgeDirection dir) {
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (getItemStack().stackSize <= 0) {
				return null;
			}
			
			if(getItemStack().getItem() instanceof LogisticsLiquidContainer) {
				remove();
				return null;
			}

			Position motion = new Position(0, 0, 0, dir);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			EntityItem entityitem = new EntityItem(worldObj, position.x, position.y, position.z, getItemStack());

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.00F + worldObj.rand.nextFloat() * 0.01F - 0.02F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3 + motion.z;
			worldObj.spawnEntityInWorld(entityitem);
			remove();

			return entityitem;
		} else {
			return null;
		}
	}
	
	@Override
	public void changeDestination(int newDestination){
		if (destinationint >= 0 && SimpleServiceLocator.routerManager.isRouter(destinationint)){
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationint);

			destinationRouter.itemDropped(this);
			
			if (destinationRouter.getPipe() != null && destinationRouter.getPipe().logic instanceof IRequireReliableTransport){
				((IRequireReliableTransport)destinationRouter.getPipe().logic).itemLost(ItemIdentifierStack.GetFromStack(item));
			}
		}
		destinationint = newDestination;
		if(newDestination >= 0) {
			isUnrouted = false;
		}
	}
	
	@Override
	public void remove() {
		if(MainProxy.isClient()) return;
		if (sourceint >= 0 && SimpleServiceLocator.routerManager.isRouter(sourceint)) {
			SimpleServiceLocator.routerManager.getRouter(sourceint).itemDropped(this);
		}
		
		if (destinationint >= 0 && SimpleServiceLocator.routerManager.isRouter(destinationint)){
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationint); 
			destinationRouter.itemDropped(this);
			if (!arrived && destinationRouter.getPipe() != null && destinationRouter.getPipe().logic instanceof IRequireReliableTransport){
				((IRequireReliableTransport)destinationRouter.getPipe().logic).itemLost(ItemIdentifierStack.GetFromStack(item));
			}
		}
		super.remove();
	}

	@Override
	public int getDestination() {
		return this.destinationint;
	}

	@Override
	public ItemStack getItemStack() {
		return this.item;
	}

	@Override
	public void setItemStack(ItemStack item) {
		this.item = item;
	}

	@Override
	public void setDestination(int destination) {
		this.destinationint = destination;
		if(destination >=0) {
			isUnrouted = false;
		}
	}

	@Override
	public int getSource() {
		return this.sourceint;
	}

	@Override
	public void setSource(int source) {
		this.sourceint = source;
	}

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
	public void setArrived(boolean flag) {
		this.arrived = flag;
	}

	@Override
	public boolean getArrived() {
		return this.arrived;
	}

	@Override
	public IRoutedItem split(World worldObj, int itemsToTake, ForgeDirection orientation) {
		if(getItemStack().getItem() instanceof LogisticsLiquidContainer) {
			throw new UnsupportedOperationException("Can't split up a LiquidContainer");
		}
		EntityPassiveItem newItem = new EntityPassiveItem(worldObj);
		newItem.setPosition(position.x, position.y, position.z);
		newItem.setSpeed(this.speed);
		newItem.setItemStack(this.item.splitStack(itemsToTake));
		
		if (this.container instanceof TileGenericPipe && ((TileGenericPipe)this.container).pipe.transport instanceof PipeTransportItems){
			if (((TileGenericPipe)this.container).pipe instanceof PipeLogisticsChassi){
				PipeLogisticsChassi chassi = (PipeLogisticsChassi) ((TileGenericPipe)this.container).pipe;
				chassi.queueRoutedItem(SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(worldObj, newItem), orientation.getOpposite());
			} else {
				((PipeTransportItems)((TileGenericPipe)this.container).pipe.transport).entityEntering(newItem, orientation);
			}
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

	@Override
	public IRoutedItem getNewUnRoutedItem() {
		if(getItemStack().getItem() instanceof LogisticsLiquidContainer) {
			throw new UnsupportedOperationException("Can't change LiquidContainer to UnRoutedItem");
		}
		EntityPassiveItem Entityitem = new EntityPassiveItem(worldObj, entityId);
		Entityitem.setContainer(container);
		Entityitem.setPosition(position.x, position.y, position.z);
		Entityitem.setSpeed(speed);
		Entityitem.setItemStack(item);
		RoutedEntityItem routed = new RoutedEntityItem(worldObj, Entityitem);
		routed.isUnrouted = true;
		routed.jamlist.addAll(jamlist);
		return routed;
	}

	@Override
	public EntityPassiveItem getNewEntityPassiveItem() {
		if(getItemStack().getItem() instanceof LogisticsLiquidContainer) {
			throw new UnsupportedOperationException("Can't change LiquidContainer to EntityPassiveItem");
		}
		EntityPassiveItem Entityitem = new EntityPassiveItem(worldObj, entityId);
		Entityitem.setContainer(container);
		Entityitem.setPosition(position.x, position.y, position.z);
		Entityitem.setSpeed(speed);
		Entityitem.setItemStack(item);
		return Entityitem;
	}

	@Override
	public boolean isReRoute() {
		return reRoute;
	}

	@Override
	public void setReRoute(boolean flag) {
		reRoute = flag;
	}

	@Override
	public void addToJamList(IRouter router) {
		jamlist.add(router.getSimpleID());
	}

	@Override
	public List<Integer> getJamList() {
		return jamlist;
	}

	@Override
	public boolean isUnRouted() {
		return isUnrouted;
	}

	@Override
	public int getBufferCounter() {
		return bufferCounter;
	}

	@Override
	public void setBufferCounter(int counter) {
		bufferCounter = counter;
	}

	@Override
	public IRoutedItem getCopy() {
		EntityPassiveItem Entityitem = new EntityPassiveItem(worldObj, entityId);
		Entityitem.setContainer(container);
		Entityitem.setPosition(position.x, position.y, position.z);
		Entityitem.setSpeed(speed);
		Entityitem.setItemStack(item.copy());
		RoutedEntityItem routed = new RoutedEntityItem(worldObj, Entityitem);
		routed.sourceint = sourceint;
		routed.destinationint = destinationint;
		routed._doNotBuffer = _doNotBuffer;
		routed.bufferCounter = bufferCounter;
		routed.arrived = arrived;
		routed.reRoute = reRoute;
		routed.isUnrouted = isUnrouted;
		routed._transportMode = _transportMode;
		routed.jamlist.addAll(jamlist);
		return routed;
	}
}
