/*
 * Copyright (c) Krapht, 2011
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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.fabricmc.fabric.api.util.NbtType;

import logisticspipes.LPConstants;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.ItemAdvancedExistence;
import logisticspipes.interfaces.SlotUpgradeManager;
import logisticspipes.interfaces.SpecialInsertion;
import logisticspipes.interfaces.WrappedInventory;
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
import logisticspipes.proxy.specialconnection.SpecialTileConnectionRegistry;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.SyncList;
import logisticspipes.utils.tuples.Tuple2;
import logisticspipes.utils.tuples.Tuple3;

public class PipeTransportLogistics {

	private final int _bufferTimeOut = 20 * 2; // 2 Seconds
	public final SyncList<Tuple3<ItemStack, Tuple2<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>> _itemBuffer = new SyncList<>();
	private Chunk chunk;
	public LPItemList items = new LPItemList(this);
	public LogisticsTileGenericPipe container;
	public final boolean isRouted;
	public final int MAX_DESTINATION_UNREACHABLE_BUFFER = 30;

	public PipeTransportLogistics(boolean isRouted) {
		this.isRouted = isRouted;
	}

	public void initialize() {
		if (!getWorld().isClient()) {
			// cache chunk for marking dirty
			chunk = getWorld().getChunkFromBlockCoords(container.getPos());
			ItemBufferSyncPacket packet = PacketHandler.getPacket(ItemBufferSyncPacket.class);
			packet.setTilePos(container);
			_itemBuffer.setPacketType(packet, getWorld().provider.getDimension(), container.getX(), container.getZ());
		}
	}

	public void markChunkModified(BlockEntity tile) {
		if (tile != null && chunk != null) {
			// items are crossing a chunk boundary, mark both chunks modified
			if (container.getPos().getX() >> 4 != tile.getPos().getX() >> 4 || container.getPos().getZ() >> 4 != tile.getPos().getZ() >> 4) {
				chunk.markDirty();
				if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.transport != null && ((LogisticsTileGenericPipe) tile).pipe.transport.chunk != null) {
					((LogisticsTileGenericPipe) tile).pipe.transport.chunk.markDirty();
				} else {
					getWorld().getChunkFromBlockCoords(tile.getPos()).markDirty();
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
		if (!getWorld().isClient()) {
			if (!_itemBuffer.isEmpty()) {
				List<LPTravelingItem> toAdd = new LinkedList<>();
				Iterator<Tuple3<ItemStack, Tuple2<Integer, Integer>, LPTravelingItemServer>> iterator = _itemBuffer.iterator();
				while (iterator.hasNext()) {
					Tuple3<ItemStack, Tuple2<Integer, Integer>, LPTravelingItemServer> next = iterator.next();
					int currentTimeOut = next.getValue2().getValue1();
					if (currentTimeOut > 0) {
						next.getValue2().setValue1(currentTimeOut - 1);
					} else if (next.getValue3() != null) {
						if (getRoutedPipe().getRouter().hasRoute(next.getValue3().getDestination(), next.getValue3().getTransportMode() == TransportMode.Active, next.getValue3().getItemStack().getItem()) || next.getValue2().getValue2() > MAX_DESTINATION_UNREACHABLE_BUFFER) {
							next.getValue3().setBufferCounter(next.getValue2().getValue2() + 1);
							toAdd.add(next.getValue3());
							iterator.remove();
						} else {
							next.getValue2().setValue2(next.getValue2().getValue2() + 1);
							next.getValue2().setValue1(_bufferTimeOut);
						}
					} else {
						LPTravelingItemServer item = RoutedItemHelper.INSTANCE.createNewTravelItem(next.getValue1());
						item.setDoNotBuffer(true);
						item.setBufferCounter(next.getValue2().getValue2() + 1);
						toAdd.add(item);
						iterator.remove();
					}
				}
				for (LPTravelingItem item : toAdd) {
					this.injectItem(item, Direction.UP);
				}
			}
			_itemBuffer.sendUpdateToWaters();
		}
	}

	public void dropBuffer() {
		Iterator<Tuple3<ItemStack, Tuple2<Integer, Integer>, LPTravelingItemServer>> iterator = _itemBuffer.iterator();
		while (iterator.hasNext()) {
			ItemStack next = iterator.next().getValue1();
			MainProxy.dropItems(getWorld(), next.makeNormalStack(), getPipe().getX(), getPipe().getY(), getPipe().getZ());
			iterator.remove();
		}
	}

	public int injectItem(LPTravelingItemServer item, Direction inputOrientation) {
		return injectItem((LPTravelingItem) item, inputOrientation);
	}

	public float getPipeLength() {
		return 1.0F;
	}

	public double getDistanceWeight() {
		return 1.0D;
	}

	public float getYawDiff(LPTravelingItem item) {
		return 0.0F;
	}

	public int injectItem(LPTravelingItem item, Direction inputOrientation) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return 0;
		}
		getPipe().triggerDebug();

		int originalCount = item.getStack().getCount();

		item.input = inputOrientation;

		if (!container.getWorld().isClient()) {
			readjustSpeed((LPTravelingItemServer) item);
			RoutingResult result = resolveDestination((LPTravelingItemServer) item);
			item.output = result.getFace();
			if (!result.hasRoute) {
				return 0;
			}
			getPipe().debug.log(String.format("Injected Item: [%s, %s] (%s)", item.input, item.output, ((LPTravelingItemServer) item).getInfo()));
		} else {
			item.output = null;
		}

		if (item.getPosition() >= getPipeLength()) {
			reachedEnd(item);
		} else {
			items.add(item);

			if (MainProxy.isServer(container.getWorld()) && !getPipe().isOpaque()) {
				sendItemPacket((LPTravelingItemServer) item);
			}
		}
		return originalCount - item.getStack().getCount();
	}

	public int injectItem(IRoutedItem item, Direction inputOrientation) {
		return injectItem((LPTravelingItem) RoutedItemHelper.INSTANCE.getServerTravelingItem(item), inputOrientation);
	}

	/**
	 * emit the supplied item. This function assumes ownershop of the item, and
	 * you may assume that it is now either buffered by the pipe or moving
	 * through the pipe.
	 *
	 * @param item the item that just bounced off an inventory. In the case of a
	 *             pipe with a buffer, this function will alter item.
	 */
	protected void reverseItem(LPTravelingItemServer item) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;
		}

		if (getPipe() instanceof IBufferItems) {
			item.getStack().setCount(((IBufferItems) getPipe()).addToBuffer(item.getStack(), item.getAdditionalTargetInformation()));
			if (item.getStack().getCount() <= 0) {
				return;
			}
		}

		// Assign new ID to update ItemStack content
		item.id = item.getNextId();

		if (item.getPosition() >= getPipeLength()) {
			item.setPosition(item.getPosition() - getPipeLength());
		}

		item.input = item.output.getOpposite();

		readjustSpeed(item);
		RoutingResult result = resolveDestination(item);
		item.output = result.getFace();
		if (!result.hasRoute) {
			return;
		} else if (item.output == null) {
			dropItem(item);
			return;
		}

		items.unscheduleRemoval(item);
		if (!getPipe().isOpaque()) {
			sendItemPacket(item);
		}
	}

	public RoutingResult resolveDestination(LPTravelingItemServer data) {
		if (isRouted) {
			return resolveRoutedDestination(data);
		} else {
			return resolveUnroutedDestination(data);
		}
	}

	public RoutingResult resolveUnroutedDestination(LPTravelingItemServer data) {
		List<Direction> dirs = new ArrayList<>(Arrays.asList(Direction.values()));
		dirs.remove(data.input.getOpposite());
		Iterator<Direction> iter = dirs.iterator();
		while (iter.hasNext()) {
			Direction dir = iter.next();
			BlockPos pos = getPipe().getPos().offset(dir);
			BlockEntity tile = getWorld().getBlockEntity(pos);
			if (!PipeInformationManager.INSTANCE.isItemPipe(tile)) {
				iter.remove();
			} else if (!canPipeConnect(tile, dir)) {
				iter.remove();
			} else if (tile instanceof LogisticsTileGenericPipe && !((LogisticsTileGenericPipe) tile).canConnect(container, dir.getOpposite(), false)) {
				iter.remove();
			}
		}
		if (dirs.isEmpty()) {
			return new RoutingResult(null, false);
		}
		int num = new Random().nextInt(dirs.size());
		return new RoutingResult(dirs.get(num), true);
	}

	public RoutingResult resolveRoutedDestination(LPTravelingItemServer data) {

		Direction blocked = null;

		if (data.getDestinationUUID() == null) {
			ItemStack stack = data.getStack();
			ItemRoutingInformation result = getRoutedPipe().getQueuedForItemStack(stack);
			if (result != null) {
				data.setInformation(result);
				data.getInfo().setItem(stack);
				blocked = data.input.getOpposite();
			}
		}

		if (data.getStack() != null) {
			getRoutedPipe().relayedItem(data.getStack().getCount());
		}

		if (data.getDestination() >= 0 && !getRoutedPipe().getRouter().hasRoute(data.getDestinationUUID(), data.getTransportMode() == TransportMode.Active, data.getStack()) && data.getBufferCounter() < MAX_DESTINATION_UNREACHABLE_BUFFER) {
			_itemBuffer.add(new Tuple3<>(data.getStack(), new Tuple2<>(_bufferTimeOut, data.getBufferCounter()), data));
			return new RoutingResult(null, false);
		}

		Direction value;
		if (getRoutedPipe().stillNeedReplace() || getRoutedPipe().initialInit()) {
			data.setDoNotBuffer(false);
			value = null;
		} else {
			value = getRoutedPipe().getRouteLayer().getOrientationForItem(data, blocked);
		}
		if (value == null && getWorld().isClient()) {
			return new RoutingResult(null, true);
		}

		if (value == null && !data.getDoNotBuffer() && data.getBufferCounter() < 5) {
			_itemBuffer.add(new Tuple3<>(data.getStack(), new Tuple2<>(_bufferTimeOut, data.getBufferCounter()), null));
			return new RoutingResult(null, false);
		}

		if (value != null && !getRoutedPipe().getRouter().isRoutedExit(value)) {
			if (!isItemExitable(data.getStack())) {
				return new RoutingResult(null, false);
			}
		}

		data.resetDelay();

		return new RoutingResult(value, true);
	}

	public void readFromNBT(CompoundTag nbt) {

		ListTag listTag = nbt.getList("travelingEntities", NbtType.COMPOUND);

		for (int j = 0; j < listTag.size(); ++j) {
			try {
				CompoundTag dataTag = listTag.getCompoundTag(j);

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

		ListTag nbttaglist2 = nbt.getList("buffercontents", NbtType.COMPOUND);
		for (int i = 0; i < nbttaglist2.size(); i++) {
			CompoundTag stackTag = nbttaglist2.getCompoundTag(i);
			_itemBuffer.add(new Tuple3<>(ItemStack.fromTag(stackTag), new Tuple2<>(_bufferTimeOut, 0), null));
		}

	}

	public void writeToNBT(CompoundTag nbt) {

		{
			ListTag listTag = new ListTag();

			for (LPTravelingItem item : items) {
				if (item instanceof LPTravelingItemServer) {
					CompoundTag dataTag = new CompoundTag();
					listTag.appendTag(dataTag);
					((LPTravelingItemServer) item).writeToNBT(dataTag);
				}
			}

			nbt.setTag("travelingEntities", listTag);
		}

		ListTag nbttaglist2 = new ListTag();

		for (Tuple2<ItemStack, Tuple2<Integer, Integer>> stack : _itemBuffer) {
			CompoundTag nbttagcompound1 = new CompoundTag();
			stack.getValue1().makeNormalStack().writeToNBT(nbttagcompound1);
			nbttaglist2.appendTag(nbttagcompound1);
		}
		nbt.setTag("buffercontents", nbttaglist2);

	}

	public void readjustSpeed(LPTravelingItemServer item) {
		float defaultBoost;

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
			float multiplierSpeed = 1.0F + (0.02F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());
			float multiplierPower = 1.0F + (0.03F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());

			float add = Math.max(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplierPower) - item.getSpeed();
			if (getRoutedPipe().useEnergy((int) (add * 50 + 0.5))) {
				item.setSpeed(MathHelper.clamp(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplierSpeed, 1f));
			}
		}
	}

	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, BlockEntity tile, Direction dir) {
		handleTileReachedServerInternal(arrivingItem, tile, dir);
	}

	protected final void handleTileReachedServerInternal(LPTravelingItemServer arrivingItem, BlockEntity tile, Direction dir) {
		if (getPipe() instanceof PipeItemsFluidSupplier) {
			((PipeItemsFluidSupplier) getPipe()).endReached(arrivingItem, tile);
			if (arrivingItem.getStack().getCount() <= 0) {
				return;
			}
		}

		markChunkModified(tile);
		if (getWorld().isClient() && arrivingItem.getInfo() != null && arrivingItem.getArrived() && isRouted) {
			getRoutedPipe().notifyOfItemArival(arrivingItem.getInfo());
		}
		if (getPipe() instanceof FluidRoutedPipe) {
			if (((FluidRoutedPipe) getPipe()).endReached(arrivingItem, tile)) {
				return;
			}
		}
		boolean isSpecialConnectionInformationTransition = false;
		if (!getWorld().isClient()) {
			if (SpecialTileConnectionRegistry.INSTANCE.needsInformationTransition(tile)) {
				isSpecialConnectionInformationTransition = true;
				SpecialTileConnectionRegistry.INSTANCE.transmit(tile, arrivingItem);
			}
		}
		if (PipeInformationManager.INSTANCE.isItemPipe(tile)) {
			if (passToNextPipe(arrivingItem, tile)) {
				return;
			}
		} else {
			WrappedInventory util = InventoryUtilFactory.INSTANCE.getInventoryUtil(tile, dir.getOpposite());
			if (util != null && isRouted) {
				getRoutedPipe().getCacheHolder().trigger(CacheTypes.Inventory);

				// items.scheduleRemoval(arrivingItem);
				if (!getWorld().isClient()) {
					// destroy the item on exit if it isn't exitable
					if (!isSpecialConnectionInformationTransition && !isItemExitable(arrivingItem.getItemStack())) {
						return;
					}
					// last chance for chassi to back out
					if (arrivingItem != null) {
						if (arrivingItem.getTransportMode() != TransportMode.Active && !getRoutedPipe().getTransportLayer().stillWantItem(arrivingItem)) {
							reverseItem(arrivingItem);
							return;
						}
					}
					SlotUpgradeManager slotManager;
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
						if (util instanceof SpecialInsertion) {
							int slot = information.getTargetSlot();
							int amount = information.getAmount();
							if (util.getSlotCount() > slot) {
								ItemStack content = util.getInvStack(slot);
								ItemStack toAdd = arrivingItem.getItemStack().makeNormalStack();
								final int amountLeft = Math.max(0, amount - content.getCount());
								toAdd.setCount(Math.min(toAdd.getCount(), amountLeft));
								if (toAdd.getCount() > 0) {
									if (util.getSlotCount() > slot) {
										int added = ((SpecialInsertion) util).addToSlot(toAdd, slot);
										arrivingItem.getItemStack().lowerStackSize(added);
										if (added > 0) {
										}
									}
								}
							}
							if (information.isLimited()) {
								if (arrivingItem.getItemStack().getCount() > 0) {
									reverseItem(arrivingItem);
								}
								return;
							}
						}
					}
					// sneaky insertion
					if (!getRoutedPipe().getUpgradeManager().hasCombinedSneakyUpgrade() || slotManager.hasOwnSneakyUpgrade()) {
						Direction insertion = arrivingItem.output.getOpposite();
						if (slotManager.hasSneakyUpgrade()) {
							insertion = slotManager.getSneakyOrientation();
						}
						ItemStack added = InventoryHelper.getTransactorFor(tile, insertion).add(arrivingItem.getItemStack().makeNormalStack(), insertion, true);

						arrivingItem.getItemStack().lowerStackSize(added.getCount());

						if (added.getCount() > 0 && arrivingItem instanceof IRoutedItem) {
							arrivingItem.setBufferCounter(0);
						}

						ItemRoutingInformation info;

						if (arrivingItem.getItemStack().getCount() > 0) {
							// we have some leftovers, we are splitting the stack, we need to clone the info
							info = arrivingItem.getInfo().clone();
							// For InvSysCon
							info.getItem().setStackSize(added.getCount());
							insertedItemStack(info, tile);
						} else {
							info = arrivingItem.getInfo();
							info.getItem().setStackSize(added.getCount());
							// For InvSysCon
							insertedItemStack(info, tile);

							// back to normal code, break if we've inserted everything, all items disposed of.
							return; // every item has been inserted.
						}
					} else {
						Direction[] dirs = getRoutedPipe().getUpgradeManager().getCombinedSneakyOrientation();
						for (Direction insertion : dirs) {
							if (insertion == null) {
								continue;
							}
							ItemStack added = InventoryHelper.getTransactorFor(tile, insertion).add(arrivingItem.getItemStack().makeNormalStack(), insertion, true);

							arrivingItem.getItemStack().lowerStackSize(added.getCount());
							if (added.getCount() > 0) {
								arrivingItem.setBufferCounter(0);
							}
							ItemRoutingInformation info;

							if (arrivingItem.getItemStack().getCount() > 0) {
								// we have some leftovers, we are splitting the stack, we need to clone the info
								info = arrivingItem.getInfo().clone();
								// For InvSysCon
								info.getItem().setStackSize(added.getCount());
								insertedItemStack(info, tile);
							} else {
								info = arrivingItem.getInfo();
								info.getItem().setStackSize(added.getCount());
								// For InvSysCon
								insertedItemStack(info, tile);
								// back to normal code, break if we've inserted everything, all items disposed of.
								return;// every item has been inserted.
							}
						}
					}

					if (arrivingItem.getItemStack().getCount() > 0) {
						reverseItem(arrivingItem);
					}
				}
				return;// the item is handled
			}// end of insert into IInventory
		}
		dropItem(arrivingItem);
	}

	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, BlockEntity tile, Direction dir) {
		if (PipeInformationManager.INSTANCE.isItemPipe(tile)) {
			passToNextPipe(arrivingItem, tile);
		}
		// Just ignore any other case
	}

	protected boolean isItemExitable(ItemStack itemIdentifierStack) {
		if (itemIdentifierStack != null && itemIdentifierStack.makeNormalStack().getItem() instanceof ItemAdvancedExistence) {
			return ((ItemAdvancedExistence) itemIdentifierStack.makeNormalStack().getItem()).canExistInNormalInventory(itemIdentifierStack.makeNormalStack());
		}
		return true;
	}

	protected void insertedItemStack(ItemRoutingInformation info, BlockEntity tile) {}

	public boolean canPipeConnect(BlockEntity tile, Direction side) {
		return canPipeConnect_internal(tile, side);
	}

	public final boolean canPipeConnect_internal(BlockEntity tile, Direction side) {
		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.isHSTube() && !((LogisticsTileGenericPipe) tile).pipe.actAsNormalPipe()) {
				return false;
			}
		}
		if (isRouted) {
			if (tile instanceof ILogisticsPowerProvider || tile instanceof ISubSystemPowerProvider) {
				Direction ori = OrientationsUtil.getOrientationOfTileWithTile(container, tile);
				if (ori != null) {
					if ((tile instanceof LogisticsPowerJunctionTileEntity || tile instanceof ISubSystemPowerProvider) && !OrientationsUtil.isSide(ori)) {
						return false;
					}
					return true;
				}
			}
			if (SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile) || SimpleServiceLocator.factorizationProxy.isBarral(tile)
					|| SimpleServiceLocator.enderIOProxy.isItemConduit(tile, side.getOpposite())
					|| (getPipe().getUpgradeManager().hasRFPowerSupplierUpgrade() && SimpleServiceLocator.powerProxy.isEnergyReceiver(tile, side.getOpposite())) || (getPipe().getUpgradeManager().getIC2PowerLevel() > 0 && SimpleServiceLocator.IC2Proxy.isEnergySink(tile))) {
				return true;
			}
			WrappedInventory util = InventoryUtilFactory.INSTANCE.getInventoryUtil(tile, side.getOpposite());
			if (util != null) {
				return util.getSlotCount() > 0;
			}
			return isPipeCheck(tile);
		} else {
			return isPipeCheck(tile);
		}
	}

	protected boolean isPipeCheck(BlockEntity tile) {
		return PipeInformationManager.INSTANCE.isItemPipe(tile);
	}

	protected void reachedEnd(LPTravelingItem item) {
		BlockEntity tile = container.getTile(item.output);
		if (items.scheduleRemoval(item)) {
			if (MainProxy.isServer(container.getWorld())) {
				handleTileReachedServer((LPTravelingItemServer) item, tile, item.output);
			} else {
				handleTileReachedClient((LPTravelingItemClient) item, tile, item.output);
			}
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
			if (hasReachedEnd(item)) {
				if (item.output == null) {
					if (MainProxy.isServer(container.getWorld())) {
						dropItem((LPTravelingItemServer) item);
					}
					items.scheduleRemoval(item);
					continue;
				}
				reachedEnd(item);
			}
		}
		items.removeScheduledItems();
		items.addScheduledItems();
	}

	protected boolean passToNextPipe(LPTravelingItem item, BlockEntity tile) {
		IPipeInformationProvider information = PipeInformationManager.INSTANCE.getInformationProviderFor(tile);
		if (information != null) {
			item.setPosition(item.getPosition() - getPipeLength());
			item.setYaw(item.getYaw() + (getYawDiff(item)));
			return information.acceptItem(item, container);
		}
		return false;
	}

	/**
	 * Accept items from BC
	 */ /*
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public void injectItem(TravelingItem item, Direction inputOrientation) {
		if (!getWorld().isClient()) {
			if (item instanceof LPRoutedBCTravelingItem) {
				ItemRoutingInformation info = ((LPRoutedBCTravelingItem) item).getRoutingInformation();
				info.setItem(ItemStack.getFromStack(item.getItemStack()));
				LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
				lpItem.setSpeed(item.getSpeed());
				this.injectItem(lpItem, inputOrientation);
			} else {
				ItemRoutingInformation info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(item);
				if (info != null) {
					info.setItem(ItemStack.getFromStack(item.getItemStack()));
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
	*/
	private void dropItem(LPTravelingItemServer item) {
		if (MainProxy.isClient(container.getWorld())) {
			return;
		}
		item.setSpeed(0.05F);
		item.setContainer(container);
		ItemEntity entity = item.toEntityItem();
		if (entity != null) {
			container.getWorld().spawnEntity(entity);
		}
	}

	protected boolean hasReachedEnd(LPTravelingItem item) {
		return item.getPosition() >= ((item.output == null) ? 0.75F : getPipeLength());
	}

	protected void neighborChange() {}

	public List<ItemStack> dropContents() {
		List<ItemStack> list = new ArrayList<>();
		if (!getWorld().isClient()) {
			for (LPTravelingItem item : items) {
				list.add(item.getItemStack().makeNormalStack());
			}
		}
		return list;
	}

	public boolean delveIntoUnloadedChunks() {
		return true;
	}

	private void sendItemPacket(LPTravelingItemServer item) {
		if (MainProxy.isAnyoneWatching(container.getPos(), getWorld().provider.getDimension())) {
			if (!LPTravelingItem.clientSideKnownIDs.get(item.getId())) {
				MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemStack()).setTravelId(item.getId())));
				LPTravelingItem.clientSideKnownIDs.set(item.getId());
			}
			MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipePositionPacket.class).setSpeed(item.getSpeed()).setPosition(item.getPosition()).setInput(item.input).setOutput(item.output).setTravelId(item.getId()).setYaw(item.getYaw()).setTilePos(container)));
		}
	}

	public void handleItemPositionPacket(int travelId, Direction input, Direction output, float speed, float position, float yaw) {
		WeakReference<LPTravelingItemClient> ref = LPTravelingItem.clientList.get(travelId);
		LPTravelingItemClient item = null;
		if (ref != null) {
			item = ref.get();
		}
		if (item == null) {
			sendItemContentRequest(travelId);
			item = new LPTravelingItemClient(travelId, position, input, output, yaw);
			item.setSpeed(speed);
			LPTravelingItem.clientList.put(travelId, new WeakReference<>(item));
		} else {
			if (item.getContainer() instanceof LogisticsTileGenericPipe) {
				((LogisticsTileGenericPipe) item.getContainer()).pipe.transport.items.scheduleRemoval(item);
				((LogisticsTileGenericPipe) item.getContainer()).pipe.transport.items.removeScheduledItems();
			}
			item.updateInformation(input, output, speed, position, yaw);
		}
		// update lastTicked so we don't double-move items
		item.lastTicked = MainProxy.getGlobalTick();
		if (items.get(travelId) == null) {
			items.add(item);
		}
		// getPipe().spawnParticle(Particles.OrangeParticle, 1);
	}

	private void sendItemContentRequest(int travelId) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeContentRequest.class).setInteger(travelId));
	}

	public void sendItem(ItemStack stackToSend) {
		this.injectItem((LPTravelingItem) RoutedItemHelper.INSTANCE.createNewTravelItem(stackToSend), Direction.UP);
	}

	public World getWorld() {
		return container.getWorld();
	}

	public void onNeighborBlockChange() {}

	public void onBlockPlaced() {}

	public void setTile(LogisticsTileGenericPipe tile) {
		container = tile;
	}

	public CoreUnroutedPipe getNextPipe(Direction output) {
		BlockEntity tile = container.getTile(output);
		if (tile instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe) tile).pipe;
		}
		return null;
	}

	@Data
	@AllArgsConstructor
	public static class RoutingResult {

		private Direction face;
		private boolean hasRoute;

	}

}
