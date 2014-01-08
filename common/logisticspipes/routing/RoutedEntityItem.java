/*
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;

public class RoutedEntityItem extends TravelingItem implements IRoutedItem {

	int destinationint = -1;
	UUID destinationUUID;
	ItemIdentifierStack thisItem;
	
	boolean _doNotBuffer;
	
	int bufferCounter = 0;
	
	boolean arrived;
	
	TransportMode _transportMode = TransportMode.Unknown;
	
	List<Integer> jamlist = new ArrayList<Integer>();
	
	private static InsertionHandler LP_INSERTIONHANDLER = new InsertionHandler() {
		@Override
		public boolean canInsertItem(TravelingItem item, IInventory inv) {
			if(item.getItemStack() != null && item.getItemStack().getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance)item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) return false;
			return true;
		}
	};
	
	public RoutedEntityItem(TravelingItem entityItem) {
		super(entityItem.id);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		entityItem.writeToNBT(nbt);
		readFromNBT(nbt);
		thisItem = ItemIdentifierStack.getFromStack(entityItem.getItemStack());
		container = entityItem.getContainer();
		
		if(container != null && container.worldObj != null) {
			delay = 10*20 + container.worldObj.getTotalWorldTime(); //10 seconds, it should be delivered by then
		} else {
			delay = 62; //64-2 ticks (assume destination consumes items at 1/tick) *20ms ; that way another stack gets sent 64 ticks after the first.
		}
		
		RoutedEntityItemSaveHandler handler = new RoutedEntityItemSaveHandler(this);
		NBTTagCompound extraData = entityItem.getExtraData();
		if(!extraData.hasKey("routingInformation")) {
			NBTTagCompound newTags = extraData.getCompoundTag("routingInformation");
			handler.writeToNBT(newTags);
		} else {
			NBTTagCompound tags = extraData.getCompoundTag("routingInformation");
			handler.readFromNBT(tags);

			/* clear destination on load for activerouted items
			 * we'll have to keep this until active requesters are smarter about remebering things over unload/reload
			 */
			if(handler.transportMode != TransportMode.Active) {
				this.destinationUUID=handler.destinationUUID;
				this.checkIDFromUUID();
			}

			bufferCounter = handler.bufferCounter;
			arrived = handler.arrived;
			_transportMode = handler.transportMode;
			handler.writeToNBT(tags);
		}
		this.setInsetionHandler(LP_INSERTIONHANDLER);
	}
	
	@Override
	public EntityItem toEntityItem() {
		World worldObj = container.getWorldObj();
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (getItemStack().stackSize <= 0) {
				return null;
			}

			if(getItemStack().getItem() instanceof LogisticsFluidContainer) {
				remove();
				return null;
			}

			if(container != null) {
				//detect items spawning in the center of pipes and move them to the exit side
				if(xCoord == container.xCoord + 0.5 && yCoord == container.yCoord + 0.25 && zCoord == container.zCoord + 0.5) {
					//N, W and down need to move a tiny bit beyond the block end because vanilla uses floor(coord) to determine block x/y/z
					if(output == ForgeDirection.DOWN) {
						//position.moveForwards(0.251);
						yCoord -= 0.251;
					} else if(output == ForgeDirection.UP) {
						//position.moveForwards(0.75);
						yCoord += 0.75;
					} else if(output == ForgeDirection.NORTH) {
						//position.moveForwards(0.501);
						zCoord -= 0.501;
					} else if(output == ForgeDirection.WEST) {
						//position.moveForwards(0.501);
						xCoord -= 0.501;
					} else if(output == ForgeDirection.SOUTH) {
						//position.moveForwards(0.5);
						zCoord += 0.5;
					} else if(output == ForgeDirection.EAST) {
						//position.moveForwards(0.5);
						xCoord += 0.5;
					}
				}
			}

			Position motion = new Position(0, 0, 0, output);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			EntityItem entityitem = new EntityItem(worldObj, xCoord, yCoord, zCoord, getItemStack());

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = worldObj.rand.nextFloat() * 0.01F - 0.02F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3 + motion.z;
			//worldObj.spawnEntityInWorld(entityitem);
			remove();

			return entityitem;
		} else {
			return null;
		}
	}
	
	@Override
	public void clearDestination() {
		if (destinationint >= 0) {
			if (SimpleServiceLocator.routerManager.isRouter(destinationint)){
				IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationint); 
				if (destinationRouter.getPipe() != null && destinationRouter.getPipe() instanceof IRequireReliableTransport){
					((IRequireReliableTransport)destinationRouter.getPipe()).itemLost(ItemIdentifierStack.getFromStack(item));
				}
				if (destinationRouter.getPipe() != null && destinationRouter.getPipe() instanceof IRequireReliableFluidTransport) {
					if(item.getItem() instanceof LogisticsFluidContainer) {
						FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item);
						((IRequireReliableFluidTransport)destinationRouter.getPipe()).liquidLost(FluidIdentifier.get(liquid), liquid.amount);
					}
				}
			}
			jamlist.add(destinationint);
		}
		//keep buffercounter and jamlist
		destinationint = -1;
		destinationUUID = null;
		_doNotBuffer = false;
		arrived = false;
		_transportMode = TransportMode.Unknown;
	}
	
	@Override
	public void remove() {
		if(MainProxy.isClient(this.container.getWorldObj())) return;
		if (destinationint >= 0 && SimpleServiceLocator.routerManager.isRouter(destinationint)){
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(destinationint); 
			if (!arrived && destinationRouter.getPipe() != null && destinationRouter.getPipe() instanceof IRequireReliableTransport){
				((IRequireReliableTransport)destinationRouter.getPipe()).itemLost(ItemIdentifierStack.getFromStack(item));
			}
			if (!arrived && destinationRouter.getPipe() != null && destinationRouter.getPipe() instanceof IRequireReliableFluidTransport) {
				if(item.getItem() instanceof LogisticsFluidContainer) {
					FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item);
					((IRequireReliableFluidTransport)destinationRouter.getPipe()).liquidLost(FluidIdentifier.get(liquid), liquid.amount);
				}
			}
		}
	}

	@Override
	public int getDestination() {
		return this.destinationint;
	}

	@Override
	public ItemIdentifierStack getIDStack(){
		return this.thisItem;
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
		IRouter router = SimpleServiceLocator.routerManager.getRouter(destination);
		if(router != null) {
			this.destinationUUID = router.getId();
		} else {
			this.destinationUUID = null;
		}
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
	public TravelingItem getTravelingItem() {
		return this;
	}

	@Override
	public void setArrived(boolean flag) {
		this.arrived = flag;
	}

	@Override
	public boolean getArrived() {
		return this.arrived;
	}

	@Override
	public void split(int itemsToTake, ForgeDirection orientation) {
		if(getItemStack().getItem() instanceof LogisticsFluidContainer) {
			throw new UnsupportedOperationException("Can't split up a FluidContainer");
		}
		TravelingItem newItem = new TravelingItem();
		newItem.setPosition(xCoord, yCoord, zCoord);
		newItem.setSpeed(this.speed);
		newItem.setItemStack(this.item.splitStack(this.item.stackSize - itemsToTake));
		
		if (this.container instanceof TileGenericPipe && ((TileGenericPipe)this.container).pipe.transport instanceof PipeTransportItems){
			if (((TileGenericPipe)this.container).pipe instanceof PipeLogisticsChassi){
				PipeLogisticsChassi chassi = (PipeLogisticsChassi) ((TileGenericPipe)this.container).pipe;
				chassi.queueRoutedItem(SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(newItem), orientation, ItemSendMode.Fast);
			} else {
				//this should never happen
				PipeEventItem.DropItem event = new PipeEventItem.DropItem(newItem, newItem.toEntityItem());
				if(container instanceof TileGenericPipe) {
					((TileGenericPipe)container).pipe.handlePipeEvent(event);
				}
				if (event.entity == null)
					return;
				container.worldObj.spawnEntityInWorld(event.entity);
			}
		}
	}

	@Override
	public void SetPosition(double x, double y, double z) {
		this.xCoord=x;
		this.yCoord=y;
		this.zCoord=z;
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
	public TravelingItem getNewTravelingItem() {
		if(getItemStack().getItem() instanceof LogisticsFluidContainer) {
			throw new UnsupportedOperationException("Can't change FluidContainer to TravelingItem");
		}
		TravelingItem newItem = new TravelingItem();
		newItem.setItemStack(getItemStack());
		newItem.setContainer(container);
		newItem.setPosition(xCoord, yCoord, zCoord);
		newItem.setSpeed(speed);
		newItem.setItemStack(item);
		return newItem;
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
	public int getBufferCounter() {
		return bufferCounter;
	}

	@Override
	public void setBufferCounter(int counter) {
		bufferCounter = counter;
	}

	@Override
	public IRoutedItem getCopy() {
		TravelingItem Entityitem = new TravelingItem();
		Entityitem.setContainer(container);
		Entityitem.setPosition(xCoord, yCoord, zCoord);
		Entityitem.setSpeed(speed);
		Entityitem.setItemStack(item.copy());
		RoutedEntityItem routed = new RoutedEntityItem(Entityitem);
		routed.destinationint = destinationint;
		routed._doNotBuffer = _doNotBuffer;
		routed.bufferCounter = bufferCounter;
		routed.arrived = arrived;
		routed._transportMode = _transportMode;
		routed.jamlist.addAll(jamlist);
		routed.thisItem = this.thisItem;
		return routed;
	}

	@Override
	public UUID getDestinationUUID() {
		return this.destinationUUID;
	}

	@Override
	public void checkIDFromUUID() {	
		IRouterManager rm = SimpleServiceLocator.routerManager;
		IRouter router = rm.getRouter(destinationint);
		if(router==null || destinationUUID!=router.getId()) {
			destinationint=rm.getIDforUUID(destinationUUID);
		}		
	}
	
	public void useInformationFrom(RoutedEntityItem result) {
		destinationint = result.destinationint;
		destinationUUID = result.destinationUUID;
		thisItem = result.thisItem;
		_doNotBuffer = result._doNotBuffer;
		arrived = result.arrived;
		_transportMode = result._transportMode;
		jamlist = result.jamlist;
	}

	// Delayed
    private final long delay;

	@Override
	public long getTimeOut() {
		return delay;
	}

	@Override
	public long getTickToTimeOut() {
		if(this.container == null || this.container.worldObj == null)
			return 0; 
		return delay-this.container.worldObj.getTotalWorldTime();
	}

}
