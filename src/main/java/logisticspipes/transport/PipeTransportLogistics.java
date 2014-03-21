/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.transport;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.CoreConstants;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelerSet;
import buildcraft.transport.TravelingItem;

public class PipeTransportLogistics extends PipeTransportItems implements IItemTravelingHook {

	private final int _bufferTimeOut = 20 * 2; //2 Seconds
//	private CoreRoutedPipe _pipe = null;
	private final HashMap<ItemStack,Pair<Integer /* Time */, Integer /* BufferCounter */>> _itemBuffer = new HashMap<ItemStack, Pair<Integer, Integer>>(); 
//	private Chunk chunk;
	
	private static Field toLoad;
	
	public PipeTransportLogistics() {
		allowBouncing = true;
		travelHook = this;
	}
/*
	@Override
	public void initialize() {
		super.initialize();
		if(MainProxy.isServer(getWorld())) {
			//cache chunk for marking dirty
//			chunk = getWorld().getChunkFromBlockCoords(xCoord, zCoord);
		}
	}
/*
	public void markChunkModified(TileEntity tile) {
		if(tile != null && chunk != null) {
			//items are crossing a chunk boundary, mark both chunks modified
			if(xCoord >> 4 != tile.xCoord >> 4 || zCoord >> 4 != tile.zCoord >> 4) {
				chunk.isModified = true;
				if(tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe != null && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportLogistics && ((PipeTransportLogistics)((TileGenericPipe) tile).pipe.transport).chunk != null) {
					((PipeTransportLogistics)((TileGenericPipe) tile).pipe.transport).chunk.isModified = true;
				} else {
					getWorld().updateTileEntityChunkAndDoNothing(tile.xCoord, tile.yCoord, tile.zCoord, tile);
				}
			}
		}
	}
	
*/
	//TODO revert this change only if that cast shows up on a profile. otherwise this decreases memory use
	private CoreRoutedPipe getPipe() {
/*		if (_pipe == null){
			_pipe = (CoreRoutedPipe) container.pipe;
		}
		return _pipe;*/
		return (CoreRoutedPipe)container.pipe;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!_itemBuffer.isEmpty()){
			List<IRoutedItem> toAdd = new LinkedList<IRoutedItem>();
			Iterator<Entry<ItemStack, Pair<Integer, Integer>>> iterator = _itemBuffer.entrySet().iterator();
			while (iterator.hasNext()){
				Entry<ItemStack, Pair<Integer, Integer>> next = iterator.next();
				int currentTimeOut = next.getValue().getValue1();
				if (currentTimeOut > 0){
					next.getValue().setValue1(currentTimeOut - 1);
				} else {
					TravelingItem item = new TravelingItem(this.getPipe().getX()+ 0.5F, this.getPipe().getY() + CoreConstants.PIPE_MIN_POS - 0.1, this.getPipe().getZ() + 0.5, next.getKey());
					IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(item);
					routedItem.setDoNotBuffer(true);
					routedItem.setBufferCounter(next.getValue().getValue2() + 1);
					toAdd.add(routedItem);
					iterator.remove();
				}
			}
			for(IRoutedItem item:toAdd) {
				this.injectItem(item.getTravelingItem(), ForgeDirection.UP);
			}
		}
	}
	
	public void dropBuffer(){
		Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
		while (iterator.hasNext()){
			ItemStack next = iterator.next();
			SimpleServiceLocator.buildCraftProxy.dropItems(getWorld(), next, this.getPipe().getX(), this.getPipe().getY(), this.getPipe().getZ());
			iterator.remove();
		}
	}

	@Override
	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		super.injectItem(SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(item), inputOrientation);
	}

	@Override
	protected void reverseItem(TravelingItem item) {
		super.reverseItem(SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(item));
	}

	@Override
	public ForgeDirection resolveDestination(TravelingItem data) {
		
		if(data != null && data.getItemStack() != null) {
			getPipe().relayedItem(data.getItemStack().stackSize);
		}
		
		ForgeDirection blocked = null;

		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(data);
		
		if(routedItem.getDestinationUUID() == null) {
			TravelingItem result = getPipe().getQueuedForItemStack(data.getItemStack());
			if(result != null) {
				if(routedItem instanceof RoutedEntityItem && result instanceof RoutedEntityItem) {
					((RoutedEntityItem)routedItem).useInformationFrom((RoutedEntityItem)result);
					blocked = data.input.getOpposite();
				} else {
					LogisticsPipes.log.warning("Unable to transfer information from ont Item to another. (" + routedItem.getClass().getName() + ", " + result.getClass().getName() + ")");
				}
			}
		}
		
		ForgeDirection value;
		if(this.getPipe().stillNeedReplace()){
			routedItem.setDoNotBuffer(false);
			value = ForgeDirection.UNKNOWN;
		} else
			value = getPipe().getRouteLayer().getOrientationForItem(routedItem, blocked);
		if (value == null && MainProxy.isClient(getWorld())) {
			routedItem.getItemStack().stackSize = 0;
			items.scheduleRemoval(data);
			return ForgeDirection.UNKNOWN;
		} else if (value == null) {
			LogisticsPipes.log.severe("THIS IS NOT SUPPOSED TO HAPPEN!");
			return ForgeDirection.UNKNOWN;
		}
		if (value == ForgeDirection.UNKNOWN && !routedItem.getDoNotBuffer() && routedItem.getBufferCounter() < 5) {
			_itemBuffer.put(routedItem.getItemStack().copy(), new Pair<Integer,Integer>(20 * 2, routedItem.getBufferCounter()));
			routedItem.getItemStack().stackSize = 0;	//Hack to make the item disappear
			items.scheduleRemoval(data);
			return ForgeDirection.UNKNOWN;
		}
		
		if(value != ForgeDirection.UNKNOWN && !getPipe().getRouter().isRoutedExit(value)) {
			if(!isItemExitable(routedItem.getItemStack())) {
				routedItem.getItemStack().stackSize = 0;	//Hack to make the item disappear
				items.scheduleRemoval(data);
				return ForgeDirection.UNKNOWN;
			}
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
	public void readjustSpeed(TravelingItem item) {	
		if (SimpleServiceLocator.buildCraftProxy.isRoutedItem(item)){
			
			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(item); 
			float defaultBoost = 1F;
			
			switch (routedItem.getTransportMode()){
			case Default:
				defaultBoost = 20F;
				break;
			case Passive:
				defaultBoost = 25F;
				break;
			case Active:
				defaultBoost = 30F;
				break;
			case Unknown:
				defaultBoost = 20F;
				break;
			default:
				defaultBoost = 20F;
				break;
			
			}

			float multiplyerSpeed = 1.0F + (0.2F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
			float multiplyerPower = 1.0F + (0.3F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
			
			float add = Math.max(item.getSpeed(), TransportConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerPower) - item.getSpeed();
			if(getPipe().useEnergy((int)(add * 50 + 0.5))) {
				item.setSpeed(Math.min(Math.max(item.getSpeed(), TransportConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerSpeed), 1.0F));
			}
		}
		if (MainProxy.isClient(getWorld())) {
			MainProxy.spawnParticle(Particles.GoldParticle, getPipe().getX(), getPipe().getY(), getPipe().getZ(), 1);
		}
	}
	
	//called from endReached, return false to let BC transport handle the item.
	protected boolean handleTileReached(TravelingItem arrivingItem, TileEntity tile) {
		//((PipeTransportLogistics)pipe).markChunkModified(tile);
		if (MainProxy.isServer(getWorld()) && (arrivingItem instanceof RoutedEntityItem) && ((RoutedEntityItem)arrivingItem).getArrived()) {
			getPipe().notifyOfItemArival((RoutedEntityItem) arrivingItem);
		}
		boolean isSpecialConnectionInformationTransition = false;
		if (!CoreProxy.proxy.isRenderWorld(getWorld())) {
			if(SimpleServiceLocator.specialtileconnection.needsInformationTransition(tile)) {
				isSpecialConnectionInformationTransition = true;
				SimpleServiceLocator.specialtileconnection.transmit(tile, arrivingItem);
			}
		}
		if (tile instanceof IPipeTile){
			return false; // let the normal BC pipe passing mechanism run
		} else if (tile instanceof IInventory) {
			items.scheduleRemoval(arrivingItem);
			if (!CoreProxy.proxy.isRenderWorld(getWorld())) {
				//LogisticsPipes start
				
				// destroy the item on exit if it isn't exitable
				if(!isSpecialConnectionInformationTransition && !isItemExitable(arrivingItem.getItemStack())) {
					return true;
				}
				//last chance for chassi to back out
				if(arrivingItem instanceof IRoutedItem) {
					IRoutedItem routed = (IRoutedItem) arrivingItem;
					if (!getPipe().getTransportLayer().stillWantItem(routed)) {
						reverseItem(arrivingItem);
						return true;
					}
				}
				UpgradeManager manager = getPipe().getUpgradeManager();
				boolean tookSome = false;
				if(manager.hasPatternUpgrade()) {
					if(getPipe() instanceof PipeItemsSupplierLogistics) {
						IInventory inv = (IInventory) tile;
						if (inv instanceof ISidedInventory) inv = new SidedInventoryMinecraftAdapter((ISidedInventory) inv, ForgeDirection.UNKNOWN, false);
						IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
						if(util instanceof ISpecialInsertion) {
							PipeItemsSupplierLogistics pipe = (PipeItemsSupplierLogistics) getPipe();
							ItemIdentifierStack stack = ItemIdentifierStack.getFromStack(arrivingItem.getItemStack());
							int[] slots = pipe.getSlotsForItemIdentifier(stack.getItem());
							for(int i:slots) {
								if(util.getSizeInventory() > pipe.getInvSlotForSlot(i)) {
									ItemStack content = util.getStackInSlot(pipe.getInvSlotForSlot(i));
									ItemStack toAdd = arrivingItem.getItemStack().copy();
									toAdd.stackSize = Math.min(toAdd.stackSize, Math.max(0, pipe.getAmountForSlot(i) - (content != null ? content.stackSize : 0)));
									if(toAdd.stackSize > 0) {
										if(util.getSizeInventory() > pipe.getInvSlotForSlot(i)) {
											int added = ((ISpecialInsertion) util).addToSlot(toAdd, pipe.getInvSlotForSlot(i));
											arrivingItem.getItemStack().stackSize -= added;
											if(added > 0) {
												tookSome = true;
											}
										}
									}
								}
							}
							if(pipe.isLimited()) {
								if(arrivingItem.getItemStack().stackSize > 0) {
									reverseItem(arrivingItem);
								}
								return true;
							}
						}
					}
				}
				//sneaky insertion
				if(!manager.hasCombinedSneakyUpgrade()) {
					ForgeDirection insertion = arrivingItem.output.getOpposite();
					if(manager.hasSneakyUpgrade()) {
						insertion = manager.getSneakyOrientation();
					}
					ItemStack added = InventoryHelper.getTransactorFor(tile).add(arrivingItem.getItemStack(), insertion, true);
					
					arrivingItem.getItemStack().stackSize -= added.stackSize;
					if(added.stackSize > 0)
						tookSome = true;
					
					if(arrivingItem instanceof IRoutedItem) {
						IRoutedItem routed = (IRoutedItem) arrivingItem;
						TravelingItem newItem = (TravelingItem) routed.getCopy();
						newItem.setItemStack(added);
						insertedItemStack(newItem, tile);
					}
				} else {
					ForgeDirection[] dirs = manager.getCombinedSneakyOrientation();
					for(int i=0;i<dirs.length;i++) {
						ForgeDirection insertion = dirs[i];
						if(insertion == null) continue;
						ItemStack added = InventoryHelper.getTransactorFor(tile).add(arrivingItem.getItemStack(), insertion, true);
						
						arrivingItem.getItemStack().stackSize -= added.stackSize;
						if(added.stackSize > 0)
							tookSome = true;
						
						//For InvSysCon
						if(arrivingItem instanceof IRoutedItem) {
							IRoutedItem routed = (IRoutedItem) arrivingItem;
							TravelingItem newItem = (TravelingItem) routed.getCopy();
							newItem.setItemStack(added);
							insertedItemStack(newItem, tile);
						}
						if(arrivingItem.getItemStack().stackSize <= 0) break;
					}
				}
				if(arrivingItem.getItemStack().stackSize > 0 && tookSome && arrivingItem instanceof IRoutedItem) {
					((IRoutedItem)arrivingItem).setBufferCounter(0);
				}
				
				//LogisticsPipes end

				if(arrivingItem.getItemStack().stackSize > 0) {
					reverseItem(arrivingItem);
				}
			}
			return true;// the item is handled
		} else {
			return false; //bounce, drop, according to normal rules
		}
	}
	//BC copy end
	
	protected boolean isItemExitable(ItemStack stack) {
		if(stack != null && stack.getItem() instanceof IItemAdvancedExistance) {
			return ((IItemAdvancedExistance)stack.getItem()).canExistInNormalInventory(stack);
		}
		return true;
	}
	
	protected void insertedItemStack(TravelingItem data, TileEntity tile) {}
	
	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return super.canPipeConnect(tile, side)
				|| SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile)
				|| SimpleServiceLocator.factorizationProxy.isBarral(tile);
	}

	/* --- IItemTravelHook --- */
	@Override
	public boolean endReached(PipeTransportItems pipe, TravelingItem data, TileEntity tile) {
		return handleTileReached(data, tile);
	}

	@Override
	public void drop(PipeTransportItems pipe, TravelingItem data) {
		data.setSpeed(0.0F);
	}

	@Override
	public void centerReached(PipeTransportItems pipe, TravelingItem data) {}
}
