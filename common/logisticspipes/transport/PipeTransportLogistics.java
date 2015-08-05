/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.ISpecialInsertion;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.ITargetSlotInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.ItemBufferSyncPacket;
import logisticspipes.network.packets.pipe.PipeContentPacket;
import logisticspipes.network.packets.pipe.PipeContentRequest;
import logisticspipes.network.packets.pipe.PipePositionPacket;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.LPRoutedBCTravelingItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SyncList;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.transport.TravelingItem;

public class PipeTransportLogistics {

	private final int _bufferTimeOut = 20 * 2; // 2 Seconds
	public final SyncList<Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>> _itemBuffer = new SyncList<Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>>();
	private Chunk chunk;
	public LPItemList items = new LPItemList(this);
	public LogisticsTileGenericPipe container;
	public final boolean isRouted;
	public final int MAX_DESTINATION_UNREACHABLE_BUFFER = 30;

	public PipeTransportLogistics(boolean isRouted) {
		this.isRouted = isRouted;
	}

	public void initialize() {
		if (MainProxy.isServer(getWorld())) {
			// cache chunk for marking dirty
			chunk = getWorld().getChunkFromBlockCoords(container.xCoord, container.zCoord);
			ItemBufferSyncPacket packet = PacketHandler.getPacket(ItemBufferSyncPacket.class);
			packet.setTilePos(container);
			_itemBuffer.setPacketType(packet, MainProxy.getDimensionForWorld(getWorld()), container.xCoord, container.zCoord);
		}
	}

	public void markChunkModified(TileEntity tile) {
		if (tile != null && chunk != null) {
			// items are crossing a chunk boundary, mark both chunks modified
			if (container.xCoord >> 4 != tile.xCoord >> 4 || container.zCoord >> 4 != tile.zCoord >> 4) {
				chunk.isModified = true;
				if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.transport instanceof PipeTransportLogistics && ((LogisticsTileGenericPipe) tile).pipe.transport.chunk != null) {
					((LogisticsTileGenericPipe) tile).pipe.transport.chunk.isModified = true;
				} else {
					getWorld().getChunkFromChunkCoords(tile.xCoord, tile.zCoord).isModified = true;
				}
			}
		}
	}

	protected CoreUnroutedPipe getPipe() {
		return container.pipe;
	}

	protected CoreRoutedPipe getRoutedPipe() {
		if (!isRouted) {
			throw new UnsupportedOperationException("Can't use a Transport pipe as a routing pipe");
		}
		return (CoreRoutedPipe) container.pipe;
	}

	public void updateEntity() {
		moveSolids();
		if (MainProxy.isServer(getWorld())) {
			if (!_itemBuffer.isEmpty()) {
				List<LPTravelingItem> toAdd = new LinkedList<LPTravelingItem>();
				Iterator<Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>> iterator = _itemBuffer.iterator();
				while (iterator.hasNext()) {
					Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer> next = iterator.next();
					int currentTimeOut = next.getValue2().getValue1();
					if (currentTimeOut > 0) {
						next.getValue2().setValue1(currentTimeOut - 1);
					} else if (next.getValue3() != null) {
						if (getRoutedPipe().getRouter().hasRoute(next.getValue3().getDestination(), next.getValue3().getTransportMode() == TransportMode.Active, next.getValue3().getItemIdentifierStack().getItem()) || next.getValue2().getValue2() > MAX_DESTINATION_UNREACHABLE_BUFFER) {
							next.getValue3().setBufferCounter(next.getValue2().getValue2() + 1);
							toAdd.add(next.getValue3());
							iterator.remove();
						} else {
							next.getValue2().setValue2(next.getValue2().getValue2() + 1);
							next.getValue2().setValue1(_bufferTimeOut);
						}
					} else {
						LPTravelingItemServer item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(next.getValue1());
						item.setDoNotBuffer(true);
						item.setBufferCounter(next.getValue2().getValue2() + 1);
						toAdd.add(item);
						iterator.remove();
					}
				}
				for (LPTravelingItem item : toAdd) {
					this.injectItem(item, ForgeDirection.UP);
				}
			}
			_itemBuffer.sendUpdateToWaters();
		}
	}

	public void dropBuffer() {
		Iterator<Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>> iterator = _itemBuffer.iterator();
		while (iterator.hasNext()) {
			ItemIdentifierStack next = iterator.next().getValue1();
			MainProxy.dropItems(getWorld(), next.makeNormalStack(), getPipe().getX(), getPipe().getY(), getPipe().getZ());
			iterator.remove();
		}
	}

	public int injectItem(LPTravelingItemServer item, ForgeDirection inputOrientation) {
		return injectItem((LPTravelingItem) item, inputOrientation);
	}

	public int injectItem(LPTravelingItem item, ForgeDirection inputOrientation) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return 0;
		}
		getPipe().triggerDebug();

		int originalCount = item.getItemIdentifierStack().getStackSize();

		item.input = inputOrientation;

		while (item.getPosition() >= 1.0F) {
			item.setPosition(item.getPosition() - 1.0F);
		}

		if (MainProxy.isServer(container.getWorldObj())) {
			readjustSpeed((LPTravelingItemServer) item);
			item.output = resolveDestination((LPTravelingItemServer) item);
			if (item.output == null) {
				return 0;
			}
			getPipe().debug.log("Injected Item: [" + item.input + ", " + item.output + "] (" + ((LPTravelingItemServer) item).getInfo());
		} else {
			item.output = ForgeDirection.UNKNOWN;
		}

		items.add(item);

		if (MainProxy.isServer(container.getWorldObj()) && !getPipe().isOpaque()) {
			sendItemPacket((LPTravelingItemServer) item);
		}

		return originalCount - item.getItemIdentifierStack().getStackSize();
	}

	public int injectItem(IRoutedItem item, ForgeDirection inputOrientation) {
		return injectItem((LPTravelingItem) SimpleServiceLocator.routedItemHelper.getServerTravelingItem(item), inputOrientation);
	}

	/**
	 * emit the supplied item. This function assumes ownershop of the item, and
	 * you may assume that it is now either buffered by the pipe or moving
	 * through the pipe.
	 * 
	 * @param item
	 *            the item that just bounced off an inventory. In the case of a
	 *            pipe with a buffer, this function will alter item.
	 */
	protected void reverseItem(LPTravelingItemServer item) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		}

		if (getPipe() instanceof IBufferItems) {
			item.getItemIdentifierStack().setStackSize(((IBufferItems) getPipe()).addToBuffer(item.getItemIdentifierStack(), item.getAdditionalTargetInformation()));
			if (item.getItemIdentifierStack().getStackSize() <= 0) {
				return;
			}
		}

		// Assign new ID to update ItemStack content
		item.id = item.getNextId();

		if (item.getPosition() >= 1.0F) {
			item.setPosition(item.getPosition() - 1.0F);
		}

		item.input = item.output.getOpposite();

		readjustSpeed(item);
		item.output = resolveDestination(item);
		if (item.output == null) {
			return; // don't do anything
		} else if (item.output == ForgeDirection.UNKNOWN) {
			dropItem(item);
			return;
		}

		items.unscheduleRemoval(item);
		if (!getPipe().isOpaque()) {
			sendItemPacket(item);
		}
	}

	public ForgeDirection resolveDestination(LPTravelingItemServer data) {
		if (isRouted) {
			return resolveRoutedDestination(data);
		} else {
			return resolveUnroutedDestination(data);
		}
	}

	public ForgeDirection resolveUnroutedDestination(LPTravelingItemServer data) {
		List<ForgeDirection> dirs = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
		dirs.remove(data.input.getOpposite());
		Iterator<ForgeDirection> iter = dirs.iterator();
		while (iter.hasNext()) {
			ForgeDirection dir = iter.next();
			LPPosition pos = getPipe().getLPPosition();
			pos.moveForward(dir);
			TileEntity tile = pos.getTileEntity(getWorld());
			if (!SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
				iter.remove();
				continue;
			}
			if(!this.container.isPipeConnected(dir)) {
				iter.remove();
				continue;
			}
		}
		if (dirs.isEmpty()) {
			return ForgeDirection.UNKNOWN;
		}
		int num = new Random().nextInt(dirs.size());
		return dirs.get(num);
	}

	public ForgeDirection resolveRoutedDestination(LPTravelingItemServer data) {

		ForgeDirection blocked = null;

		if (data.getDestinationUUID() == null) {
			ItemIdentifierStack stack = data.getItemIdentifierStack();
			ItemRoutingInformation result = getRoutedPipe().getQueuedForItemStack(stack);
			if (result != null) {
				data.setInformation(result);
				data.getInfo().setItem(stack);
				blocked = data.input.getOpposite();
			}
		}

		if (data.getItemIdentifierStack() != null) {
			getRoutedPipe().relayedItem(data.getItemIdentifierStack().getStackSize());
		}

		if (data.getDestination() >= 0 && !getRoutedPipe().getRouter().hasRoute(data.getDestination(), data.getTransportMode() == TransportMode.Active, data.getItemIdentifierStack().getItem()) && data.getBufferCounter() < MAX_DESTINATION_UNREACHABLE_BUFFER) {
			_itemBuffer.add(new Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>(data.getItemIdentifierStack(), new Pair<Integer, Integer>(_bufferTimeOut, data.getBufferCounter()), data));
			return null;
		}

		ForgeDirection value;
		if (getRoutedPipe().stillNeedReplace() || getRoutedPipe().initialInit()) {
			data.setDoNotBuffer(false);
			value = ForgeDirection.UNKNOWN;
		} else {
			value = getRoutedPipe().getRouteLayer().getOrientationForItem(data, blocked);
		}
		if (value == null && MainProxy.isClient(getWorld())) {
			return null;
		} else if (value == null) {
			LogisticsPipes.log.fatal("THIS IS NOT SUPPOSED TO HAPPEN!");
			return ForgeDirection.UNKNOWN;
		}
		if (value == ForgeDirection.UNKNOWN && !data.getDoNotBuffer() && data.getBufferCounter() < 5) {
			_itemBuffer.add(new Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>(data.getItemIdentifierStack(), new Pair<Integer, Integer>(_bufferTimeOut, data.getBufferCounter()), null));
			return null;
		}

		if (value != ForgeDirection.UNKNOWN && !getRoutedPipe().getRouter().isRoutedExit(value)) {
			if (!isItemExitable(data.getItemIdentifierStack())) {
				return null;
			}
		}

		data.resetDelay();

		return value;
	}

	public void readFromNBT(NBTTagCompound nbt) {

		NBTTagList nbttaglist = nbt.getTagList("travelingEntities", 10);

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound dataTag = nbttaglist.getCompoundTagAt(j);

				LPTravelingItem item = new LPTravelingItemServer(dataTag);

				if (item.isCorrupted()) {
					continue;
				}

				items.scheduleLoad(item);
			} catch (Throwable t) {
				// It may be the case that entities cannot be reloaded between
				// two versions - ignore these errors.
			}
		}

		_itemBuffer.clear();

		NBTTagList nbttaglist2 = nbt.getTagList("buffercontents", 10);
		for (int i = 0; i < nbttaglist2.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = nbttaglist2.getCompoundTagAt(i);
			_itemBuffer.add(new Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>(ItemIdentifierStack.getFromStack(ItemStack.loadItemStackFromNBT(nbttagcompound1)), new Pair<Integer, Integer>(_bufferTimeOut, 0), null));
		}

	}

	public void writeToNBT(NBTTagCompound nbt) {

		{
			NBTTagList nbttaglist = new NBTTagList();

			for (LPTravelingItem item : items) {
				if (item instanceof LPTravelingItemServer) {
					NBTTagCompound dataTag = new NBTTagCompound();
					nbttaglist.appendTag(dataTag);
					((LPTravelingItemServer) item).writeToNBT(dataTag);
				}
			}

			nbt.setTag("travelingEntities", nbttaglist);
		}

		NBTTagList nbttaglist2 = new NBTTagList();

		for (Pair<ItemIdentifierStack, Pair<Integer, Integer>> stack : _itemBuffer) {
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			stack.getValue1().makeNormalStack().writeToNBT(nbttagcompound1);
			nbttaglist2.appendTag(nbttagcompound1);
		}
		nbt.setTag("buffercontents", nbttaglist2);

	}

	public void readjustSpeed(LPTravelingItemServer item) {
		float defaultBoost = 1F;

		switch (item.getTransportMode()) {
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

		if (isRouted) {
			float multiplyerSpeed = 1.0F + (0.02F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());
			float multiplyerPower = 1.0F + (0.03F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());

			float add = Math.max(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerPower) - item.getSpeed();
			if (getRoutedPipe().useEnergy((int) (add * 50 + 0.5))) {
				item.setSpeed(Math.min(Math.max(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerSpeed), 1.0F));
			}
		}
	}

	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, ForgeDirection dir) {
		if (isRouted && getPipe().container.tilePart.getBCPipePluggable(dir) != null && getPipe().container.tilePart.getBCPipePluggable(dir).isAcceptingItems(arrivingItem)) {
			LPTravelingItemServer remainingItem = getPipe().container.tilePart.getBCPipePluggable(dir).handleItem(arrivingItem);
			if (remainingItem != null) {
				getRoutedPipe().getRouter().update(true, getRoutedPipe());
				this.injectItem(remainingItem, dir);
			}
			return;
		}

		if (getPipe() instanceof PipeItemsFluidSupplier) {
			((PipeItemsFluidSupplier) getPipe()).endReached(arrivingItem, tile);
			if (arrivingItem.getItemIdentifierStack().getStackSize() <= 0) {
				return;
			}
		}

		markChunkModified(tile);
		if (MainProxy.isServer(getWorld()) && arrivingItem.getInfo() != null && arrivingItem.getArrived() && isRouted) {
			getRoutedPipe().notifyOfItemArival(arrivingItem.getInfo());
		}
		if (getPipe() instanceof FluidRoutedPipe) {
			if (((FluidRoutedPipe) getPipe()).endReached(arrivingItem, tile)) {
				return;
			}
		}
		boolean isSpecialConnectionInformationTransition = false;
		if (MainProxy.isServer(getWorld())) {
			if (SimpleServiceLocator.specialtileconnection.needsInformationTransition(tile)) {
				isSpecialConnectionInformationTransition = true;
				SimpleServiceLocator.specialtileconnection.transmit(tile, arrivingItem);
			}
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			if (passToNextPipe(arrivingItem, tile)) {
				return;
			}
		} else if (tile instanceof IInventory && isRouted) {
			getRoutedPipe().getCacheHolder().trigger(CacheTypes.Inventory);

			// items.scheduleRemoval(arrivingItem);
			if (MainProxy.isServer(getWorld())) {
				// destroy the item on exit if it isn't exitable
				if (!isSpecialConnectionInformationTransition && !isItemExitable(arrivingItem.getItemIdentifierStack())) {
					return;
				}
				// last chance for chassi to back out
				if (arrivingItem instanceof IRoutedItem) {
					IRoutedItem routed = arrivingItem;
					if (routed.getTransportMode() != TransportMode.Active && !getRoutedPipe().getTransportLayer().stillWantItem(routed)) {
						reverseItem(arrivingItem);
						return;
					}
				}
				ISlotUpgradeManager slotManager;
				{
					ModulePositionType slot = null;
					int positionInt = -1;
					if (arrivingItem.getInfo().targetInfo instanceof ChassiTargetInformation) {
						positionInt = ((ChassiTargetInformation) arrivingItem.getInfo().targetInfo).getModuleSlot();
						slot = ModulePositionType.SLOT;
					} else if (LPConstants.DEBUG && container.pipe instanceof PipeLogisticsChassi) {
						System.out.println(arrivingItem);
						new RuntimeException("[ItemInsertion] Information weren't ment for a chassi pipe").printStackTrace();
					}
					slotManager = getRoutedPipe().getUpgradeManager(slot, positionInt);
				}
				boolean tookSome = false;
				if (arrivingItem.getAdditionalTargetInformation() instanceof ITargetSlotInformation) {

					ITargetSlotInformation information = (ITargetSlotInformation) arrivingItem.getAdditionalTargetInformation();
					IInventory inv = (IInventory) tile;
					if (inv instanceof ISidedInventory) {
						inv = new SidedInventoryMinecraftAdapter((ISidedInventory) inv, ForgeDirection.UNKNOWN, false);
					}
					IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
					if (util instanceof ISpecialInsertion) {
						int slot = information.getTargetSlot();
						int amount = information.getAmount();
						if (util.getSizeInventory() > slot) {
							ItemStack content = util.getStackInSlot(slot);
							ItemStack toAdd = arrivingItem.getItemIdentifierStack().makeNormalStack();
							toAdd.stackSize = Math.min(toAdd.stackSize, Math.max(0, amount - (content != null ? content.stackSize : 0)));
							if (toAdd.stackSize > 0) {
								if (util.getSizeInventory() > slot) {
									int added = ((ISpecialInsertion) util).addToSlot(toAdd, slot);
									arrivingItem.getItemIdentifierStack().lowerStackSize(added);
									if (added > 0) {
										tookSome = true;
									}
								}
							}
						}
						if (information.isLimited()) {
							if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
								reverseItem(arrivingItem);
							}
							return;
						}
					}
				}
				// sneaky insertion
				if (!getRoutedPipe().getUpgradeManager().hasCombinedSneakyUpgrade() || slotManager.hasOwnSneakyUpgrade()) {
					ForgeDirection insertion = arrivingItem.output.getOpposite();
					if (slotManager.hasSneakyUpgrade()) {
						insertion = slotManager.getSneakyOrientation();
					}
					ItemStack added = InventoryHelper.getTransactorFor(tile, dir.getOpposite()).add(arrivingItem.getItemIdentifierStack().makeNormalStack(), insertion, true);

					arrivingItem.getItemIdentifierStack().lowerStackSize(added.stackSize);

					if (added.stackSize > 0 && arrivingItem instanceof IRoutedItem) {
						tookSome = true;
						((IRoutedItem) arrivingItem).setBufferCounter(0);
					}

					ItemRoutingInformation info;

					if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
						// we have some leftovers, we are splitting the stack, we need to clone the info
						info = arrivingItem.getInfo().clone();
						// For InvSysCon
						info.getItem().setStackSize(added.stackSize);
						insertedItemStack(info, tile);
					} else {
						info = arrivingItem.getInfo();
						info.getItem().setStackSize(added.stackSize);
						// For InvSysCon
						insertedItemStack(info, tile);

						// back to normal code, break if we've inserted everything, all items disposed of.
						return; // every item has been inserted.
					}
				} else {
					ForgeDirection[] dirs = getRoutedPipe().getUpgradeManager().getCombinedSneakyOrientation();
					for (ForgeDirection insertion : dirs) {
						if (insertion == null) {
							continue;
						}
						ItemStack added = InventoryHelper.getTransactorFor(tile, dir.getOpposite()).add(arrivingItem.getItemIdentifierStack().makeNormalStack(), insertion, true);

						arrivingItem.getItemIdentifierStack().lowerStackSize(added.stackSize);
						if (added.stackSize > 0 && arrivingItem instanceof IRoutedItem) {
							tookSome = true;
							((IRoutedItem) arrivingItem).setBufferCounter(0);
						}
						ItemRoutingInformation info;

						if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
							// we have some leftovers, we are splitting the stack, we need to clone the info
							info = arrivingItem.getInfo().clone();
							// For InvSysCon
							info.getItem().setStackSize(added.stackSize);
							insertedItemStack(info, tile);
						} else {
							info = arrivingItem.getInfo();
							info.getItem().setStackSize(added.stackSize);
							// For InvSysCon
							insertedItemStack(info, tile);
							// back to normal code, break if we've inserted everything, all items disposed of.
							return;// every item has been inserted.
						}
					}
				}

				if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
					reverseItem(arrivingItem);
				}
			}
			return;// the item is handled
		}// end of insert into IInventory
		dropItem(arrivingItem);
	}

	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile) {
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			passToNextPipe(arrivingItem, tile);
		}
		// Just ignore any other case
	}

	protected boolean isItemExitable(ItemIdentifierStack itemIdentifierStack) {
		if (itemIdentifierStack != null && itemIdentifierStack.makeNormalStack().getItem() instanceof IItemAdvancedExistance) {
			return ((IItemAdvancedExistance) itemIdentifierStack.makeNormalStack().getItem()).canExistInNormalInventory(itemIdentifierStack.makeNormalStack());
		}
		return true;
	}

	protected void insertedItemStack(ItemRoutingInformation info, TileEntity tile) {}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (isRouted) {
			if (tile instanceof ILogisticsPowerProvider || tile instanceof ISubSystemPowerProvider) {
				ForgeDirection ori = OrientationsUtil.getOrientationOfTilewithTile(container, tile);
				if (ori != null && ori != ForgeDirection.UNKNOWN) {
					if ((tile instanceof LogisticsPowerJunctionTileEntity || tile instanceof ISubSystemPowerProvider) && !OrientationsUtil.isSide(ori)) {
						return false;
					}
					return true;
				}
			}
			if (SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile) || SimpleServiceLocator.factorizationProxy.isBarral(tile)
			//|| (Configs.TE_PIPE_SUPPORT && SimpleServiceLocator.thermalExpansionProxy.isItemConduit(tile) && SimpleServiceLocator.thermalExpansionProxy.isSideFree(tile, side.getOpposite().ordinal()))
					|| (getPipe().getUpgradeManager().hasRFPowerSupplierUpgrade() && SimpleServiceLocator.cofhPowerProxy.isEnergyReceiver(tile)) || (getPipe().getUpgradeManager().getIC2PowerLevel() > 0 && SimpleServiceLocator.IC2Proxy.isEnergySink(tile))) {
				return true;
			}
			if (tile instanceof ISidedInventory) {
				int[] slots = ((ISidedInventory) tile).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
				return slots != null && slots.length > 0;
			}
			return SimpleServiceLocator.pipeInformationManager.isItemPipe(tile) || (getPipe().isFluidPipe() && SimpleServiceLocator.pipeInformationManager.isFluidPipe(tile)) || (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0);
		} else {
			return SimpleServiceLocator.pipeInformationManager.isItemPipe(tile);
		}
	}

	private void moveSolids() {
		items.flush();
		items.scheduleAdd();
		for (LPTravelingItem item : items) {
			if (item.lastTicked >= MainProxy.getGlobalTick()) {
				continue;
			}
			item.lastTicked = MainProxy.getGlobalTick();
			item.addAge();
			item.setPosition(item.getPosition() + item.getSpeed());
			if (endReached(item)) {
				if (item.output == ForgeDirection.UNKNOWN) {
					if (MainProxy.isServer(container.getWorldObj())) {
						dropItem((LPTravelingItemServer) item);
					}
					items.scheduleRemoval(item);
					continue;
				}
				TileEntity tile = container.getTile(item.output);
				if (items.scheduleRemoval(item)) {
					if (MainProxy.isServer(container.getWorldObj())) {
						handleTileReachedServer((LPTravelingItemServer) item, tile, item.output);
					} else {
						handleTileReachedClient((LPTravelingItemClient) item, tile);
					}
				}
			}
		}
		items.removeScheduledItems();
		items.addScheduledItems();
	}

	private boolean passToNextPipe(LPTravelingItem item, TileEntity tile) {
		IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(tile);
		if (information != null) {
			return information.acceptItem(item, container);
		}
		return false;
	}

	/**
	 * Accept items from BC
	 */
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		if (MainProxy.isServer(getWorld())) {
			if (item instanceof LPRoutedBCTravelingItem) {
				ItemRoutingInformation info = ((LPRoutedBCTravelingItem) item).getRoutingInformation();
				info.setItem(ItemIdentifierStack.getFromStack(item.getItemStack()));
				LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
				lpItem.setSpeed(item.getSpeed());
				this.injectItem(lpItem, inputOrientation);
			} else {
				ItemRoutingInformation info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(item);
				if (info != null) {
					info.setItem(ItemIdentifierStack.getFromStack(item.getItemStack()));
					LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
					lpItem.setSpeed(item.getSpeed());
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
		if (container.getWorldObj().isRemote) {
			return;
		}
		item.setSpeed(0.05F);
		item.setContainer(container);
		EntityItem entity = item.toEntityItem();
		if (entity != null) {
			container.getWorldObj().spawnEntityInWorld(entity);
		}
	}

	protected boolean endReached(LPTravelingItem item) {
		return item.getPosition() >= ((item.output == ForgeDirection.UNKNOWN) ? 0.75F : 1.0F);
	}

	protected void neighborChange() {}

	public List<ItemStack> dropContents() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		if (MainProxy.isServer(getWorld())) {
			for (LPTravelingItem item : items) {
				list.add(item.getItemIdentifierStack().makeNormalStack());
			}
		}
		return list;
	}

	public boolean delveIntoUnloadedChunks() {
		return true;
	}

	private void sendItemPacket(LPTravelingItemServer item) {
		if (MainProxy.isAnyoneWatching(container.xCoord, container.zCoord, MainProxy.getDimensionForWorld(getWorld()))) {
			if (!LPTravelingItem.clientSideKnownIDs.get(item.getId())) {
				MainProxy.sendPacketToAllWatchingChunk(container.xCoord, container.zCoord, MainProxy.getDimensionForWorld(getWorld()), (PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemIdentifierStack()).setTravelId(item.getId())));
				LPTravelingItem.clientSideKnownIDs.set(item.getId());
			}
			MainProxy.sendPacketToAllWatchingChunk(container.xCoord, container.zCoord, MainProxy.getDimensionForWorld(getWorld()),
					(PacketHandler.getPacket(PipePositionPacket.class).setSpeed(item.getSpeed()).setPosition(item.getPosition()).setInput(item.input).setOutput(item.output).setTravelId(item.getId()).setTilePos(container)));
		}
	}

	public void handleItemPositionPacket(int travelId, ForgeDirection input, ForgeDirection output, float speed, float position) {
		WeakReference<LPTravelingItemClient> ref = LPTravelingItem.clientList.get(travelId);
		LPTravelingItemClient item = null;
		if (ref != null) {
			item = ref.get();
		}
		if (item == null) {
			sendItemContentRequest(travelId);
			item = new LPTravelingItemClient(travelId, position, input, output);
			item.setSpeed(speed);
			LPTravelingItem.clientList.put(travelId, new WeakReference<LPTravelingItemClient>(item));
		} else {
			if (item.getContainer() instanceof LogisticsTileGenericPipe) {
				((LogisticsTileGenericPipe) item.getContainer()).pipe.transport.items.scheduleRemoval(item);
				((LogisticsTileGenericPipe) item.getContainer()).pipe.transport.items.removeScheduledItems();
			}
			item.updateInformation(input, output, speed, position);
		}
		//update lastTicked so we don't double-move items
		item.lastTicked = MainProxy.getGlobalTick();
		if (items.get(travelId) == null) {
			items.add(item);
		}
		//getPipe().spawnParticle(Particles.OrangeParticle, 1);
	}

	private void sendItemContentRequest(int travelId) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeContentRequest.class).setInteger(travelId));
	}

	public void sendItem(ItemStack stackToSend) {
		this.injectItem((LPTravelingItem) SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend), ForgeDirection.UP);
	}

	public World getWorld() {
		return container.getWorldObj();
	}

	public void onNeighborBlockChange(int blockId) {}

	public void onBlockPlaced() {}

	public void setTile(LogisticsTileGenericPipe tile) {
		container = tile;
	}
}
