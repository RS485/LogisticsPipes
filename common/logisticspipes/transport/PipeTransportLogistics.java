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
import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import lombok.AllArgsConstructor;
import lombok.Data;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
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
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.ItemBufferSyncPacket;
import logisticspipes.network.packets.pipe.PipeContentPacket;
import logisticspipes.network.packets.pipe.PipeContentRequest;
import logisticspipes.network.packets.pipe.PipePositionPacket;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.SyncList;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.util.items.ItemStackLoader;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class PipeTransportLogistics {

	@Data
	@AllArgsConstructor
	static class RoutingResult {

		private EnumFacing face;
		private boolean hasRoute;
	}

	private final int _bufferTimeOut = 20 * 2; // 2 Seconds
	public final SyncList<Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>> _itemBuffer = new SyncList<>();
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
			chunk = getWorld().getChunkFromBlockCoords(container.getPos());
			ItemBufferSyncPacket packet = PacketHandler.getPacket(ItemBufferSyncPacket.class);
			packet.setTilePos(container);
			_itemBuffer.setPacketType(packet, getWorld().provider.getDimension(), container.getX(), container.getZ());
		}
	}

	public void markChunkModified(TileEntity tile) {
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
		if (MainProxy.isServer(getWorld())) {
			if (!_itemBuffer.isEmpty()) {
				List<LPTravelingItem> toAdd = new LinkedList<>();
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
					this.injectItem(item, EnumFacing.UP);
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

	public int injectItem(LPTravelingItemServer item, EnumFacing inputOrientation) {
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

	public int injectItem(LPTravelingItem item, EnumFacing inputOrientation) {
		if (item.isCorrupted()) {
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return 0;
		}
		getPipe().triggerDebug();

		int originalCount = item.getItemIdentifierStack().getStackSize();

		item.input = inputOrientation;

		if (MainProxy.isServer(container.getWorld())) {
			readjustSpeed((LPTravelingItemServer) item);
			ItemRoutingInformation info1 = ((LPTravelingItemServer) item).getInfo().clone();
			RoutingResult result = resolveDestination((LPTravelingItemServer) item);
			item.output = result.getFace();
			if (!result.hasRoute) {
				return 0;
			}
			getPipe().debug.log("Injected Item: [" + item.input + ", " + item.output + "] (" + info1);
		} else {
			item.output = null;
		}

		if (item.getPosition() >= getPipeLength()) {
			reachedEnd(item);
		} else {
			items.add(item);

			if (MainProxy.isServer(container.getWorld()) && !getPipe().isOpaque() && item.getItemIdentifierStack().getStackSize() > 0) {
				sendItemPacket((LPTravelingItemServer) item);
			}
		}
		return originalCount - item.getItemIdentifierStack().getStackSize();
	}

	public int injectItem(IRoutedItem item, EnumFacing inputOrientation) {
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
		List<EnumFacing> dirs = new ArrayList<>(Arrays.asList(EnumFacing.VALUES));
		dirs.remove(data.input.getOpposite());
		Iterator<EnumFacing> iter = dirs.iterator();
		while (iter.hasNext()) {
			EnumFacing dir = iter.next();
			DoubleCoordinates pos = CoordinateUtils.add(getPipe().getLPPosition(), dir);
			TileEntity tile = pos.getTileEntity(getWorld());
			if (!SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
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

		EnumFacing blocked = null;

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
			_itemBuffer.add(new Triplet<>(data.getItemIdentifierStack(), new Pair<>(_bufferTimeOut, data.getBufferCounter()), data));
			return new RoutingResult(null, false);
		}

		EnumFacing value;
		if (getRoutedPipe().stillNeedReplace() || getRoutedPipe().initialInit()) {
			data.setDoNotBuffer(false);
			value = null;
		} else {
			value = getRoutedPipe().getRouteLayer().getOrientationForItem(data, blocked);
		}
		if (value == null && MainProxy.isClient(getWorld())) {
			return new RoutingResult(null, true);
		}

		if (value == null && !data.getDoNotBuffer() && data.getBufferCounter() < 5) {
			_itemBuffer.add(new Triplet<>(data.getItemIdentifierStack(), new Pair<>(_bufferTimeOut, data.getBufferCounter()), null));
			return new RoutingResult(null, false);
		}

		if (value != null && !getRoutedPipe().getRouter().isRoutedExit(value)) {
			if (isItemUnwanted(data.getItemIdentifierStack())) {
				return new RoutingResult(null, false);
			}
		}

		data.resetDelay();

		return new RoutingResult(value, true);
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
			_itemBuffer.add(new Triplet<>(ItemIdentifierStack.getFromStack(ItemStackLoader.loadAndFixItemStackFromNBT(nbttagcompound1)), new Pair<>(_bufferTimeOut, 0), null));
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
			float multiplyerSpeed = 1.0F + (0.02F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());
			float multiplyerPower = 1.0F + (0.03F * getRoutedPipe().getUpgradeManager().getSpeedUpgradeCount());

			float add = Math.max(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerPower) - item.getSpeed();
			if (getRoutedPipe().useEnergy((int) (add * 50 + 0.5))) {
				item.setSpeed(Math.min(Math.max(item.getSpeed(), LPConstants.PIPE_NORMAL_SPEED * defaultBoost * multiplyerSpeed), 1.0F));
			}
		}
	}

	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, EnumFacing dir) {
		handleTileReachedServer_internal(arrivingItem, tile, dir);
	}

	protected final void handleTileReachedServer_internal(LPTravelingItemServer arrivingItem, TileEntity tile, EnumFacing dir) {
		if (getPipe() instanceof PipeItemsFluidSupplier) {
			((PipeItemsFluidSupplier) getPipe()).endReached(arrivingItem, tile);
			if (arrivingItem.getItemIdentifierStack().getStackSize() <= 0) {
				return;
			}
		}

		markChunkModified(tile);
		if (arrivingItem.getInfo() != null && arrivingItem.getArrived() && isRouted) {
			getRoutedPipe().notifyOfItemArival(arrivingItem.getInfo());
		}
		if (getPipe() instanceof FluidRoutedPipe) {
			if (((FluidRoutedPipe) getPipe()).endReached(arrivingItem, tile)) {
				return;
			}
		}
		boolean isSpecialConnectionInformationTransition = false;
		if (SimpleServiceLocator.specialtileconnection.needsInformationTransition(tile)) {
			isSpecialConnectionInformationTransition = true;
			SimpleServiceLocator.specialtileconnection.transmit(tile, arrivingItem);
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			if (passToNextPipe(arrivingItem, tile)) {
				return;
			}
		} else {
			IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(tile, dir.getOpposite());
			if (util != null && isRouted) {
				getRoutedPipe().getCacheHolder().trigger(CacheTypes.Inventory);

				// items.scheduleRemoval(arrivingItem);
				// destroy the item on exit if it isn't exitable
				if (!isSpecialConnectionInformationTransition && isItemUnwanted(arrivingItem.getItemIdentifierStack())) {
					return;
				}
				// last chance for chassi to back out
				if (arrivingItem.getTransportMode() != TransportMode.Active && !getRoutedPipe().getTransportLayer().stillWantItem(arrivingItem)) {
					reverseItem(arrivingItem);
					return;
				}
				final ISlotUpgradeManager slotManager;
				{
					ModulePositionType slot = null;
					int positionInt = -1;
					if (arrivingItem.getInfo().targetInfo instanceof ChassiTargetInformation) {
						positionInt = ((ChassiTargetInformation) arrivingItem.getInfo().targetInfo).getModuleSlot();
						slot = ModulePositionType.SLOT;
					} else if (LogisticsPipes.isDEBUG() && container.pipe instanceof PipeLogisticsChassis) {
						System.out.println(arrivingItem);
						new RuntimeException("[ItemInsertion] Information weren't ment for a chassi pipe").printStackTrace();
					}
					slotManager = getRoutedPipe().getUpgradeManager(slot, positionInt);
				}
				if (arrivingItem.getAdditionalTargetInformation() instanceof ITargetSlotInformation) {

					ITargetSlotInformation information = (ITargetSlotInformation) arrivingItem.getAdditionalTargetInformation();
					if (util instanceof ISpecialInsertion) {
						int slot = information.getTargetSlot();
						int amount = information.getAmount();
						if (util.getSizeInventory() > slot) {
							ItemStack content = util.getStackInSlot(slot);
							ItemStack toAdd = arrivingItem.getItemIdentifierStack().makeNormalStack();
							final int amountLeft = Math.max(0, amount - content.getCount());
							toAdd.setCount(Math.min(toAdd.getCount(), amountLeft));
							if (toAdd.getCount() > 0) {
								if (util.getSizeInventory() > slot) {
									int added = ((ISpecialInsertion) util).addToSlot(toAdd, slot);
									arrivingItem.getItemIdentifierStack().lowerStackSize(added);
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
					EnumFacing insertion = arrivingItem.output.getOpposite();
					if (slotManager.hasSneakyUpgrade()) {
						insertion = slotManager.getSneakyOrientation();
					}
					if (insertArrivingItem(arrivingItem, tile, insertion)) return;
				} else {
					EnumFacing[] dirs = getRoutedPipe().getUpgradeManager().getCombinedSneakyOrientation();
					for (EnumFacing insertion : dirs) {
						if (insertion == null) {
							continue;
						}
						if (insertArrivingItem(arrivingItem, tile, insertion)) return;
					}
				}

				if (arrivingItem.getItemIdentifierStack().getStackSize() > 0) {
					reverseItem(arrivingItem);
				}
				return;// the item is handled
			}// end of insert into IInventory
		}
		dropItem(arrivingItem);
	}

	/**
	 * @return true, if every item has been inserted and otherwise false.
	 */
	private boolean insertArrivingItem(LPTravelingItemServer arrivingItem, TileEntity tile, EnumFacing insertion) {
		ItemStack added = InventoryHelper.getTransactorFor(tile, insertion).add(arrivingItem.getItemIdentifierStack().makeNormalStack(), insertion, true);

		if (!added.isEmpty()) {
			arrivingItem.getItemIdentifierStack().lowerStackSize(added.getCount());
			arrivingItem.setBufferCounter(0);
		}

		ItemRoutingInformation info = arrivingItem.getInfo();
		final boolean isSplitStack = arrivingItem.getItemIdentifierStack().getStackSize() > 0;
		if (isSplitStack) {
			// we have some leftovers, we are splitting the stack, we need to clone the info
			info = info.clone();
		}
		if(added.isEmpty()){
			info.getItem().setStackSize(0);
		} else {
			info.getItem().setStackSize(added.getCount());
		}

		inventorySystemConnectorHook(info, tile);

		// back to normal code, break if we've inserted everything, all items disposed of.
		return !isSplitStack;
	}

	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile, EnumFacing dir) {
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			passToNextPipe(arrivingItem, tile);
		}
		// Just ignore any other case
	}

	protected boolean isItemUnwanted(ItemIdentifierStack itemIdentifierStack) {
		if (itemIdentifierStack != null && itemIdentifierStack.makeNormalStack().getItem() instanceof IItemAdvancedExistance) {
			return !((IItemAdvancedExistance) itemIdentifierStack.makeNormalStack().getItem()).canExistInNormalInventory(itemIdentifierStack.makeNormalStack());
		}
		return false;
	}

	protected void inventorySystemConnectorHook(ItemRoutingInformation info, TileEntity tile) {}

	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		return canPipeConnect_internal(tile, side);
	}

	public final boolean canPipeConnect_internal(TileEntity tile, EnumFacing side) {
		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.isHSTube() && !((LogisticsTileGenericPipe) tile).pipe.actAsNormalPipe()) {
				return false;
			}
		}
		if (isRouted) {
			if (tile instanceof ILogisticsPowerProvider || tile instanceof ISubSystemPowerProvider) {
				EnumFacing ori = OrientationsUtil.getOrientationOfTilewithTile(container, tile);
				if (ori != null) {
					return !((tile instanceof LogisticsPowerJunctionTileEntity || tile instanceof ISubSystemPowerProvider) && ori.getAxis() == EnumFacing.Axis.Y);
				}
			}
			if (SimpleServiceLocator.enderIOProxy.isItemConduit(tile, side.getOpposite())
					|| (getPipe().getUpgradeManager().hasRFPowerSupplierUpgrade() && SimpleServiceLocator.powerProxy.isEnergyReceiver(tile, side.getOpposite())) || (getPipe().getUpgradeManager().getIC2PowerLevel() > 0 && SimpleServiceLocator.IC2Proxy.isEnergySink(tile))) {
				return true;
			}
			IInventoryUtil util = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(tile, side.getOpposite());
			if (util != null) {
				return util.getSizeInventory() > 0;
			}
			return isPipeCheck(tile);
		} else {
			return isPipeCheck(tile);
		}
	}

	protected boolean isPipeCheck(TileEntity tile) {
		return SimpleServiceLocator.pipeInformationManager.isItemPipe(tile);
	}

	protected void reachedEnd(LPTravelingItem item) {
		TileEntity tile = container.getTile(item.output);
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

	protected boolean passToNextPipe(LPTravelingItem item, TileEntity tile) {
		IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(tile);
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
	public void injectItem(TravelingItem item, EnumFacing inputOrientation) {
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
	*/
	private void dropItem(LPTravelingItemServer item) {
		if (MainProxy.isClient(container.getWorld())) {
			return;
		}
		item.setSpeed(0.05F);
		item.setContainer(container);
		EntityItem entity = item.toEntityItem();
		if (entity != null) {
			container.getWorld().spawnEntity(entity);
		}
	}

	protected boolean hasReachedEnd(LPTravelingItem item) {
		return item.getPosition() >= ((item.output == null) ? 0.75F : getPipeLength());
	}

	protected void neighborChange() {}

	public NonNullList<ItemStack> dropContents() {
		NonNullList<ItemStack> list = NonNullList.create();
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
		if (MainProxy.isAnyoneWatching(container.getPos(), getWorld().provider.getDimension())) {
			if (!LPTravelingItem.clientSideKnownIDs.get(item.getId())) {
				MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemIdentifierStack()).setTravelId(item.getId())));
				LPTravelingItem.clientSideKnownIDs.set(item.getId());
			}
			MainProxy.sendPacketToAllWatchingChunk(container, (PacketHandler.getPacket(PipePositionPacket.class).setSpeed(item.getSpeed()).setPosition(item.getPosition()).setInput(item.input).setOutput(item.output).setTravelId(item.getId()).setYaw(item.getYaw()).setTilePos(container)));
		}
	}

	public void handleItemPositionPacket(int travelId, EnumFacing input, EnumFacing output, float speed, float position, float yaw) {
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

	public void sendItem(@Nonnull ItemStack stackToSend) {
		this.injectItem((LPTravelingItem) SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend), EnumFacing.UP);
	}

	public World getWorld() {
		return container.getWorld();
	}

	public void onNeighborBlockChange() {}

	public void onBlockPlaced() {}

	public void setTile(LogisticsTileGenericPipe tile) {
		container = tile;
	}

	public CoreUnroutedPipe getNextPipe(EnumFacing output) {
		TileEntity tile = container.getTile(output);
		if (tile instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe) tile).pipe;
		}
		return null;
	}
}
