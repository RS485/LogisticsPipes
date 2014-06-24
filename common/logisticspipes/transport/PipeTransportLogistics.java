/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.PipeContentPacket;
import logisticspipes.network.packets.pipe.PipeContentRequest;
import logisticspipes.network.packets.pipe.PipePositionPacket;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.LPRoutedBCTravelingItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;

public class PipeTransportLogistics extends PipeTransport {
	
	private final int																					_bufferTimeOut	= 20 * 2;														// 2 Seconds
	private final HashMap<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>>	_itemBuffer		= new HashMap<ItemIdentifierStack, Pair<Integer, Integer>>();
	private Chunk																						chunk;
	public LPItemList																					items = new LPItemList(this);
	
	@Override
	public void initialize() {
		super.initialize();
		if(MainProxy.isServer(getWorld())) {
			// cache chunk for marking dirty
			chunk = getWorld().getChunkFromBlockCoords(container.xCoord, container.zCoord);
		}
	}
	
	public void markChunkModified(TileEntity tile) {
		if(tile != null && chunk != null) {
			// items are crossing a chunk boundary, mark both chunks modified
			if(container.xCoord >> 4 != tile.xCoord >> 4 || container.zCoord >> 4 != tile.zCoord >> 4) {
				chunk.isModified = true;
				if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe != null && ((TileGenericPipe)tile).pipe.transport instanceof PipeTransportLogistics && ((PipeTransportLogistics)((TileGenericPipe)tile).pipe.transport).chunk != null) {
					((PipeTransportLogistics)((TileGenericPipe)tile).pipe.transport).chunk.isModified = true;
				} else {
					getWorld().getChunkFromChunkCoords(tile.xCoord, tile.zCoord).isModified = true;
				}
			}
		}
	}
	
	protected CoreRoutedPipe getPipe() {
		return (CoreRoutedPipe)container.pipe;
	}
	
	@Override
	public void updateEntity() {
		moveSolids();
		if(!_itemBuffer.isEmpty()) {
			List<LPTravelingItem> toAdd = new LinkedList<LPTravelingItem>();
			Iterator<Entry<ItemIdentifierStack, Pair<Integer, Integer>>> iterator = _itemBuffer.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<ItemIdentifierStack, Pair<Integer, Integer>> next = iterator.next();
				int currentTimeOut = next.getValue().getValue1();
				if(currentTimeOut > 0) {
					next.getValue().setValue1(currentTimeOut - 1);
				} else {
					LPTravelingItemServer item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(next.getKey());
					item.setDoNotBuffer(true);
					item.setBufferCounter(next.getValue().getValue2() + 1);
					toAdd.add(item);
					iterator.remove();
				}
			}
			for(LPTravelingItem item: toAdd) {
				this.injectItem(item, ForgeDirection.UP);
			}
		}
	}
	
	public void dropBuffer() {
		Iterator<ItemIdentifierStack> iterator = _itemBuffer.keySet().iterator();
		while(iterator.hasNext()) {
			ItemIdentifierStack next = iterator.next();
			SimpleServiceLocator.buildCraftProxy.dropItems(getWorld(), next.makeNormalStack(), this.getPipe().getX(), this.getPipe().getY(), this.getPipe().getZ());
			iterator.remove();
		}
	}
	
	public void injectItem(LPTravelingItemServer item, ForgeDirection inputOrientation) {
		injectItem((LPTravelingItem)item, inputOrientation);
	}
	
	public void injectItem(LPTravelingItem item, ForgeDirection inputOrientation) {
		if(item.isCorrupted())
		// Safe guard - if for any reason the item is corrupted at this
		// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		getPipe().triggerDebug();
		
		item.input = inputOrientation;
		
		while(item.getPosition() >= 1.0F) {
			item.setPosition(item.getPosition() - 1.0F);
		}
		
		if(MainProxy.isServer(container.worldObj)) {
			readjustSpeed((LPTravelingItemServer)item);
			item.output = resolveDestination((LPTravelingItemServer)item);
			if(item.output == null) {
				return; // don't do anything
			}
			getPipe().debug.log("Injected Item: [" + item.input + ", " + item.output + "] (" + ((LPTravelingItemServer)item).getInfo());
		} else {
			item.output = ForgeDirection.UNKNOWN;
		}
		
		items.add(item);
		
		if(MainProxy.isServer(container.worldObj) && !getPipe().isOpaque()) {
			sendItemPacket((LPTravelingItemServer)item);
		}
	}
	
	public void injectItem(IRoutedItem item, ForgeDirection inputOrientation) {
		injectItem((LPTravelingItem)SimpleServiceLocator.routedItemHelper.getServerTravelingItem(item), inputOrientation);
	}
	
	protected void reverseItem(LPTravelingItemServer item, ItemIdentifierStack stack) {
		if(item.isCorrupted())
		// Safe guard - if for any reason the item is corrupted at this
		// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		
		if(getPipe() instanceof IBufferItems) {
			stack.setStackSize(((IBufferItems)getPipe()).addToBuffer(stack, item.getAdditionalTargetInformation()));
			if(stack.getStackSize() <= 0) return;
		}
		
		// Assign new ID to update ItemStack content
		item.id = item.getNextId();
		
		item.getInfo().setItem(stack);

		if(item.getPosition() >= 1.0F) {
			item.setPosition(item.getPosition() - 1.0F);
		}
		
		item.input = item.output.getOpposite();
		
		readjustSpeed((LPTravelingItemServer)item);
		item.output = resolveDestination((LPTravelingItemServer)item);
		if(item.output == null) {
			return; // don't do anything
		} else if(item.output == ForgeDirection.UNKNOWN) {
			dropItem((LPTravelingItemServer)item);
			return;
		}
		
		items.unscheduleRemoval(item);
		if(!getPipe().isOpaque()) {
			sendItemPacket((LPTravelingItemServer)item);
		}
	}
	
	public ForgeDirection resolveDestination(LPTravelingItemServer data) {
		
		if(data != null && data.getItemIdentifierStack() != null) {
			getPipe().relayedItem(data.getItemIdentifierStack().getStackSize());
		}
		
		ForgeDirection blocked = null;
		
		if(data.getDestinationUUID() == null) {
			ItemIdentifierStack stack = data.getItemIdentifierStack();
			ItemRoutingInformation result = getPipe().getQueuedForItemStack(stack);
			if(result != null) {
				data.setInformation(result);
				data.getInfo().setItem(stack.clone());
				blocked = data.input.getOpposite();
			}
		}
		
		ForgeDirection value;
		if(this.getPipe().stillNeedReplace() || this.getPipe().initialInit()) {
			data.setDoNotBuffer(false);
			value = ForgeDirection.UNKNOWN;
		} else
			value = getPipe().getRouteLayer().getOrientationForItem(data, blocked);
		if(value == null && MainProxy.isClient(getWorld())) {
			return null;
		} else if(value == null) {
			LogisticsPipes.log.severe("THIS IS NOT SUPPOSED TO HAPPEN!");
			return ForgeDirection.UNKNOWN;
		}
		if(value == ForgeDirection.UNKNOWN && !data.getDoNotBuffer() && data.getBufferCounter() < 5) {
			_itemBuffer.put(data.getItemIdentifierStack(), new Pair<Integer, Integer>(20 * 2, data.getBufferCounter()));
			return null;
		}
		
		if(value != ForgeDirection.UNKNOWN && !getPipe().getRouter().isRoutedExit(value)) {
			if(!isItemExitable(data.getItemIdentifierStack())) { return null; }
		}
		
		data.resetDelay();
		
		return value;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		NBTTagList nbttaglist = nbt.getTagList("travelingEntities");
		
		for(int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound dataTag = (NBTTagCompound)nbttaglist.tagAt(j);
				
				LPTravelingItem item = new LPTravelingItemServer(dataTag);
				
				if(item.isCorrupted()) {
					continue;
				}
				
				items.scheduleLoad(item);
			} catch(Throwable t) {
				// It may be the case that entities cannot be reloaded between
				// two versions - ignore these errors.
			}
		}
		
		_itemBuffer.clear();
		
		NBTTagList nbttaglist2 = nbt.getTagList("buffercontents");
		for(int i = 0; i < nbttaglist2.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist2.tagAt(i);
			_itemBuffer.put(ItemIdentifierStack.getFromStack(ItemStack.loadItemStackFromNBT(nbttagcompound1)), new Pair<Integer, Integer>(_bufferTimeOut, 0));
		}
		
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		{
			NBTTagList nbttaglist = new NBTTagList();
			
			for(LPTravelingItem item: items) {
				if(item instanceof LPTravelingItemServer) {
					NBTTagCompound dataTag = new NBTTagCompound();
					nbttaglist.appendTag(dataTag);
					((LPTravelingItemServer)item).writeToNBT(dataTag);
				}
			}
			
			nbt.setTag("travelingEntities", nbttaglist);
		}
		
		NBTTagList nbttaglist2 = new NBTTagList();
		
		for(ItemIdentifierStack stack: _itemBuffer.keySet()) {
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			stack.makeNormalStack().writeToNBT(nbttagcompound1);
			nbttaglist2.appendTag(nbttagcompound1);
		}
		nbt.setTag("buffercontents", nbttaglist2);
		
	}
	
	public void readjustSpeed(LPTravelingItemServer item) {
		float defaultBoost = 1F;
		
		switch(item.getTransportMode()) {
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
		
		float multiplyerSpeed = 1.0F + (0.02F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
		float multiplyerPower = 1.0F + (0.03F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
		
		float add = Math.max(item.getSpeed(), TransportConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerPower) - item.getSpeed();
		if(getPipe().useEnergy((int)(add * 50 + 0.5))) {
			item.setSpeed(Math.min(Math.max(item.getSpeed(), TransportConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerSpeed), 1.0F));
		}
	}
	
	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile) {
		ItemIdentifierStack itemStack = arrivingItem.getItemIdentifierStack().clone();
		if(getPipe() instanceof PipeItemsFluidSupplier) {
			((PipeItemsFluidSupplier)getPipe()).endReached(arrivingItem, tile);
			if(arrivingItem.getItemIdentifierStack().getStackSize() <= 0) { return; }
		}
		
		this.markChunkModified(tile);
		if(MainProxy.isServer(getWorld()) && arrivingItem.getInfo() != null && arrivingItem.getArrived()) {
			getPipe().notifyOfItemArival(arrivingItem.getInfo());
		}
		if(this.getPipe() instanceof FluidRoutedPipe) {
			if(((FluidRoutedPipe)this.getPipe()).endReached(arrivingItem, tile)) { return; }
		}
		if(SimpleServiceLocator.thermalExpansionProxy.isItemConduit(tile)) {
			if(SimpleServiceLocator.thermalExpansionProxy.insertIntoConduit(arrivingItem, tile, getPipe())) {
				// items.scheduleRemoval(arrivingItem);
				return;
			}
		}
		boolean isSpecialConnectionInformationTransition = false;
		if(MainProxy.isServer(getWorld())) {
			if(SimpleServiceLocator.specialtileconnection.needsInformationTransition(tile)) {
				isSpecialConnectionInformationTransition = true;
				SimpleServiceLocator.specialtileconnection.transmit(tile, arrivingItem);
			}
		}
		if(tile instanceof IPipeTile) {
			if(passToNextPipe(arrivingItem, tile)) return;
		} else if(tile instanceof IInventory) {
			// items.scheduleRemoval(arrivingItem);
			if(MainProxy.isServer(getWorld())) {
				// destroy the item on exit if it isn't exitable
				if(!isSpecialConnectionInformationTransition && !isItemExitable(arrivingItem.getItemIdentifierStack())) { return; }
				// last chance for chassi to back out
				if(arrivingItem instanceof IRoutedItem) {
					IRoutedItem routed = (IRoutedItem)arrivingItem;
					if(routed.getTransportMode() != TransportMode.Active && !getPipe().getTransportLayer().stillWantItem(routed)) {
						reverseItem(arrivingItem, itemStack);
						return;
					}
				}
				UpgradeManager manager = getPipe().getUpgradeManager();
				boolean tookSome = false;
				if(manager.hasPatternUpgrade()) {
					if(getPipe() instanceof PipeItemsSupplierLogistics) {
						IInventory inv = (IInventory)tile;
						if(inv instanceof ISidedInventory) inv = new SidedInventoryMinecraftAdapter((ISidedInventory)inv, ForgeDirection.UNKNOWN, false);
						IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
						if(util instanceof ISpecialInsertion) {
							PipeItemsSupplierLogistics pipe = (PipeItemsSupplierLogistics)getPipe();
							int[] slots = pipe.getSlotsForItemIdentifier(itemStack.getItem());
							for(int i: slots) {
								if(util.getSizeInventory() > pipe.getInvSlotForSlot(i)) {
									ItemStack content = util.getStackInSlot(pipe.getInvSlotForSlot(i));
									ItemStack toAdd = itemStack.makeNormalStack();
									toAdd.stackSize = Math.min(toAdd.stackSize, Math.max(0, pipe.getAmountForSlot(i) - (content != null ? content.stackSize : 0)));
									if(toAdd.stackSize > 0) {
										if(util.getSizeInventory() > pipe.getInvSlotForSlot(i)) {
											int added = ((ISpecialInsertion)util).addToSlot(toAdd, pipe.getInvSlotForSlot(i));
											itemStack.lowerStackSize(added);
											if(added > 0) {
												tookSome = true;
											}
										}
									}
								}
							}
							if(pipe.isLimited()) {
								if(itemStack.getStackSize() > 0) {
									reverseItem(arrivingItem, itemStack);
								}
								return;
							}
						}
					}
				}
				// sneaky insertion
				if(!manager.hasCombinedSneakyUpgrade()) {
					ForgeDirection insertion = arrivingItem.output.getOpposite();
					if(manager.hasSneakyUpgrade()) {
						insertion = manager.getSneakyOrientation();
					}
					ItemStack added = InventoryHelper.getTransactorFor(tile).add(itemStack.makeNormalStack(), insertion, true);
					
					itemStack.lowerStackSize(added.stackSize);
					if(added.stackSize > 0) tookSome = true;
					
					insertedItemStack(ItemIdentifierStack.getFromStack(added), arrivingItem.getInfo(), tile);
				} else {
					ForgeDirection[] dirs = manager.getCombinedSneakyOrientation();
					for(int i = 0; i < dirs.length; i++) {
						ForgeDirection insertion = dirs[i];
						if(insertion == null) continue;
						ItemStack added = InventoryHelper.getTransactorFor(tile).add(itemStack.makeNormalStack(), insertion, true);
						
						itemStack.lowerStackSize(added.stackSize);
						if(added.stackSize > 0) tookSome = true;
						
						// For InvSysCon
						insertedItemStack(ItemIdentifierStack.getFromStack(added), arrivingItem.getInfo(), tile);
						if(itemStack.getStackSize() <= 0) break;
					}
				}
				if(itemStack.getStackSize() > 0 && tookSome && arrivingItem instanceof IRoutedItem) {
					((IRoutedItem)arrivingItem).setBufferCounter(0);
				}
				
				if(itemStack.getStackSize() > 0) {
					reverseItem(arrivingItem, itemStack);
				}
			}
			return;// the item is handled
		}
		dropItem(arrivingItem);
	}
	
	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile) {
		if(tile instanceof IPipeTile) {
			passToNextPipe(arrivingItem, tile);
		}
		// Just ignore any other case
	}
	
	protected boolean isItemExitable(ItemIdentifierStack itemIdentifierStack) {
		if(itemIdentifierStack != null && itemIdentifierStack.makeNormalStack().getItem() instanceof IItemAdvancedExistance) { return ((IItemAdvancedExistance)itemIdentifierStack.makeNormalStack().getItem()).canExistInNormalInventory(itemIdentifierStack.makeNormalStack()); }
		return true;
	}
	
	protected void insertedItemStack(ItemIdentifierStack item, ItemRoutingInformation info, TileEntity tile) {}
	
	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if(tile instanceof ILogisticsPowerProvider || tile instanceof ISubSystemPowerProvider) {
			ForgeDirection ori = OrientationsUtil.getOrientationOfTilewithPipe(this, tile);
			if(ori != null && ori != ForgeDirection.UNKNOWN) {
				if((tile instanceof LogisticsPowerJunctionTileEntity || tile instanceof ISubSystemPowerProvider) && !OrientationsUtil.isSide(ori)) { return false; }
				return true;
			}
		}
		if(SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile) || SimpleServiceLocator.factorizationProxy.isBarral(tile)
				|| (Configs.TE_PIPE_SUPPORT && SimpleServiceLocator.thermalExpansionProxy.isItemConduit(tile) && SimpleServiceLocator.thermalExpansionProxy.isSideFree(tile, side.getOpposite().ordinal())) || (this.getPipe().getUpgradeManager().hasBCPowerSupplierUpgrade() && tile instanceof IPowerReceptor)
				|| (this.getPipe().getUpgradeManager().hasRFPowerSupplierUpgrade() && SimpleServiceLocator.thermalExpansionProxy.isEnergyHandler(tile)) || (this.getPipe().getUpgradeManager().getIC2PowerLevel() > 0 && SimpleServiceLocator.IC2Proxy.isEnergySink(tile))) { return true; }
		if(tile instanceof TileGenericPipe) {
			Pipe<?> pipe2 = ((TileGenericPipe)tile).pipe;
			if(BlockGenericPipe.isValid(pipe2)) {
				if(!(pipe2.transport instanceof PipeTransportItems) && !(pipe2.transport instanceof PipeTransportLogistics)) return false;
				return true;
			}
		}
		if(tile instanceof ISidedInventory) {
			int[] slots = ((ISidedInventory)tile).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
			return slots != null && slots.length > 0;
		}
		return tile instanceof TileGenericPipe || tile instanceof ISpecialInventory || (tile instanceof IInventory && ((IInventory)tile).getSizeInventory() > 0) || (tile instanceof IMachine && ((IMachine)tile).manageSolids());
	}

	private SecurityManager hackToGetCaller = new SecurityManager() {
		@Override
		public Object getSecurityContext() {
			return this.getClassContext();
		}
	};
	
	@Override
	public PipeType getPipeType() {
		Class<?>[] caller = (Class<?>[]) hackToGetCaller.getSecurityContext();
		if(caller[3].getName().equals("buildcraft.core.utils.Utils")) {
			return PipeType.ITEM;
		}
		if(LogisticsPipes.LogisticsPipeType == null) {
			return PipeType.STRUCTURE;
		}
		return LogisticsPipes.LogisticsPipeType; // Don't let BC render the Pipe content
	}
	
	public void defaultReajustSpeed(TravelingItem item) {
		float speed = item.getSpeed();
		
		if(speed > TransportConstants.PIPE_NORMAL_SPEED) {
			speed -= TransportConstants.PIPE_NORMAL_SPEED;
		}
		
		if(speed < TransportConstants.PIPE_NORMAL_SPEED) {
			speed = TransportConstants.PIPE_NORMAL_SPEED;
		}
		
		item.setSpeed(speed);
	}
	
	public boolean canReceivePipeObjects(ForgeDirection o, TravelingItem item) {
		TileEntity entity = container.getTile(o);
		
		if(!container.isPipeConnected(o)) return false;
		
		if(entity instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe)entity;
			
			return pipe.pipe.transport instanceof PipeTransportItems;
		} else if(entity instanceof IInventory && item.getInsertionHandler().canInsertItem(item, (IInventory)entity)) if(Transactor.getTransactorFor(entity).add(item.getItemStack(), o.getOpposite(), false).stackSize > 0) return true;
		
		return false;
	}
	
	private void moveSolids() {
		items.flush();
		items.scheduleAdd();
		for(LPTravelingItem item: items) {
			if(item.lastTicked >= MainProxy.getGlobalTick()) continue;
			item.lastTicked = MainProxy.getGlobalTick();
			item.addAge();
			item.setPosition(item.getPosition() + item.getSpeed());
			if(endReached(item)) {
				if(item.output == ForgeDirection.UNKNOWN) {
					if(MainProxy.isServer(container.getWorldObj())) {
						dropItem((LPTravelingItemServer)item);
					}
					items.scheduleRemoval(item);
					continue;
				}
				TileEntity tile = container.getTile(item.output);
				if(items.scheduleRemoval(item)) {
					if(MainProxy.isServer(container.getWorldObj())) {
						handleTileReachedServer((LPTravelingItemServer)item, tile);
					} else {
						handleTileReachedClient((LPTravelingItemClient)item, tile);
					}
				}
			}
		}
		items.addScheduledItems();
		items.removeScheduledItems();
	}
	
	private boolean passToNextPipe(LPTravelingItem item, TileEntity tile) {
		if(tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe)tile;
			if(BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportLogistics) {
				((PipeTransportLogistics)pipe.pipe.transport).injectItem(item, item.output);
				return true;
			}
		}
		if(SimpleServiceLocator.buildCraftProxy.insertIntoBuildcraftPipe(tile, item)) { return true; }
		return false;
	}
	
	/**
	 * Accept items from BC
	 */
	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		if(MainProxy.isServer(this.getWorld())) {
			if(item instanceof LPRoutedBCTravelingItem) {
				ItemRoutingInformation info = ((LPRoutedBCTravelingItem)item).getRoutingInformation();
				LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
				this.injectItem(lpItem, inputOrientation);
			} else {
				ItemRoutingInformation info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(item);
				if(info != null) {
					LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
					this.injectItem(lpItem, inputOrientation);
				} else {
					LPTravelingItemServer lpItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(item.getItemStack());
					lpItem.setSpeed(item.getSpeed());
					this.injectItem(lpItem, inputOrientation);
				}
			}
		}
	}
	
	private void dropItem(LPTravelingItemServer item) {
		if(container.worldObj.isRemote) { return; }
		item.setSpeed(0.05F);
		item.setContainer(container);
		EntityItem entity = item.toEntityItem();
		if(entity != null) {
			container.worldObj.spawnEntityInWorld(entity);
		}
	}
	
	protected boolean endReached(LPTravelingItem item) {
		return item.getPosition() >= ((item.output == ForgeDirection.UNKNOWN)?0.75F:1.0F);
	}
	
	protected void neighborChange() {}
	
	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}
	
	@Override
	public void dropContents() {
		for(LPTravelingItem item: items) {
			container.pipe.dropItem(item.getItemIdentifierStack().makeNormalStack());
		}
		items.clear();
	}
	
	@Override
	public boolean delveIntoUnloadedChunks() {
		return true;
	}
	
	private void sendItemPacket(LPTravelingItemServer item) {
		if(!LPTravelingItem.clientSideKnownIDs.get(item.getId())) {
			MainProxy.sendPacketToAllWatchingChunk(this.container.xCoord, this.container.zCoord, MainProxy.getDimensionForWorld(this.getWorld()), (PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemIdentifierStack()).setTravelId(item.getId())));
			LPTravelingItem.clientSideKnownIDs.set(item.getId());
		}
		MainProxy.sendPacketToAllWatchingChunk(this.container.xCoord, this.container.zCoord, MainProxy.getDimensionForWorld(this.getWorld()), (PacketHandler.getPacket(PipePositionPacket.class).setSpeed(item.getSpeed()).setPosition(item.getPosition()).setInput(item.input).setOutput(item.output).setTravelId(item.getId()).setTilePos(container)));
	}
	
	public void handleItemPositionPacket(int travelId, ForgeDirection input, ForgeDirection output, float speed, float position) {
		WeakReference<LPTravelingItemClient> ref = LPTravelingItem.clientList.get(travelId);
		LPTravelingItemClient item = null;
		if(ref != null) item = ref.get();
		if(item == null) {
			sendItemContentRequest(travelId);
			item = new LPTravelingItemClient(travelId, position, input, output);
			item.setSpeed(speed);
			LPTravelingItem.clientList.put(travelId, new WeakReference<LPTravelingItemClient>(item));
		} else {
			if(item.getContainer() instanceof LogisticsTileGenericPipe) {
				((PipeTransportLogistics)((LogisticsTileGenericPipe)item.getContainer()).pipe.transport).items.scheduleRemoval(item);
				((PipeTransportLogistics)((LogisticsTileGenericPipe)item.getContainer()).pipe.transport).items.removeScheduledItems();
			}
			item.updateInformation(input, output, speed, position);
		}
		//update lastTicked so we don't double-move items
		item.lastTicked = MainProxy.getGlobalTick();
		if(items.get(travelId) == null) {
			items.add(item);
		}
		getPipe().spawnParticle(Particles.OrangeParticle, 1);
	}
	
	private void sendItemContentRequest(int travelId) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeContentRequest.class).setInteger(travelId));
	}
	
	public void sendItem(ItemStack stackToSend) {
		this.injectItem((LPTravelingItem)SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend), ForgeDirection.UP);
	}
}
