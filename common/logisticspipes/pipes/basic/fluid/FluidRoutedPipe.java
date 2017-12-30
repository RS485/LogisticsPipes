package logisticspipes.pipes.basic.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import logisticspipes.LPConstants;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.LogisticsFluidOrderManager;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public abstract class FluidRoutedPipe extends CoreRoutedPipe {

	private LogisticsFluidOrderManager _orderFluidManager;

	public FluidRoutedPipe(Item item) {
		super(new PipeFluidTransportLogistics(), item);
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
	}

	@Override
	public boolean logisitcsIsPipeConnected(TileEntity tile, EnumFacing dir) {
		if(SimpleServiceLocator.enderIOProxy.isBundledPipe(tile)) {
			return SimpleServiceLocator.enderIOProxy.isFluidConduit(tile, dir.getOpposite());
		}

		ITankUtil liq = SimpleServiceLocator.tankUtilFactory.getTankUtilForTE(tile, dir.getOpposite());
		if(liq != null) {
			if(liq.containsTanks()) {
				return true;
			}
		}

		return tile instanceof LogisticsTileGenericPipe;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public TextureType getNonRoutedTexture(EnumFacing connection) {
		if (isFluidSidedTexture(connection)) {
			return Textures.LOGISTICSPIPE_LIQUID_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	private boolean isFluidSidedTexture(EnumFacing connection) {
		TileEntity tileEntity = new WorldCoordinatesWrapper(container).getAdjacentFromDirection(connection).tileEntity;
		ITankUtil liq = SimpleServiceLocator.tankUtilFactory.getTankUtilForTE(tileEntity, connection.getOpposite());
		if(liq != null) {
			if(liq.containsTanks()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	/***
	 * @param flag
	 *            Weather to list a Nearby Pipe or not
	 */

	public final List<ITankUtil> getAdjacentTanks(boolean flag) {
		return new WorldCoordinatesWrapper(container).getAdjacentTileEntities()
				.filter(adjacent -> isConnectableTank(adjacent.tileEntity, adjacent.direction, flag))
				.map(adjacent -> SimpleServiceLocator.tankUtilFactory.getTankUtilForTE(adjacent.tileEntity, adjacent.direction))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/***
	 * @param flag
	 *            Weather to list a Nearby Pipe or not
	 */

	public final List<Triplet<ITankUtil, TileEntity, EnumFacing>> getAdjacentTanksAdvanced(boolean flag) {
		return new WorldCoordinatesWrapper(container).getAdjacentTileEntities()
				.filter(adjacent -> isConnectableTank(adjacent.tileEntity, adjacent.direction, flag))
				.map(adjacent -> new Triplet<>(SimpleServiceLocator.tankUtilFactory.getTankUtilForTE(adjacent.tileEntity, adjacent.direction), adjacent.tileEntity, adjacent.direction))
				.filter(triplet -> triplet.getValue1() != null)
				.collect(Collectors.toList());
	}

	/***
	 * @param tile
	 *            The connected TileEntity
	 * @param dir
	 *            The direction the TileEntity is in relative to the currect
	 *            pipe
	 * @param flag
	 *            Weather to list a Nearby Pipe or not
	 */

	public final boolean isConnectableTank(TileEntity tile, EnumFacing dir, boolean flag) {
		if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(tile)) {
			return true;
		}
		if (!(tile instanceof IFluidHandler)) {
			return false;
		}
		if (!this.canPipeConnect(tile, dir)) {
			return false;
		}
		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe instanceof FluidRoutedPipe) {
				return false;
			}
			if (!flag) {
				return false;
			}
			if (((LogisticsTileGenericPipe) tile).pipe == null || !(((LogisticsTileGenericPipe) tile).pipe.transport instanceof IFluidHandler)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (canInsertFromSideToTanks()) {
			int validDirections = 0;
			List<Triplet<ITankUtil, TileEntity, EnumFacing>> list = getAdjacentTanksAdvanced(true);
			for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : list) {
				if (pair.getValue2() instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) pair.getValue2()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank internalTank = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue3().ordinal()];
				validDirections++;
				if (internalTank.getFluid() == null) {
					continue;
				}
				ITankUtil externalTank = pair.getValue1();
				int filled = externalTank.fill(FluidIdentifierStack.getFromStack(internalTank.getFluid()), true);
				if (filled == 0) {
					continue;
				}
				FluidStack drain = internalTank.drain(filled, true);
				if (drain == null || filled != drain.amount) {
					if (LPConstants.DEBUG) {
						throw new UnsupportedOperationException("Fluid Multiplication");
					}
				}
			}
			if (validDirections == 0) {
				return;
			}
			FluidTank tank = ((PipeFluidTransportLogistics) transport).internalTank;
			FluidStack stack = tank.getFluid();
			if (stack == null) {
				return;
			}
			for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : list) {
				if (pair.getValue2() instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) pair.getValue2()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank tankSide = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue3().ordinal()];
				stack = tank.getFluid();
				if (stack == null) {
					continue;
				}
				stack = stack.copy();
				int filled = tankSide.fill(stack, true);
				if (filled == 0) {
					continue;
				}
				FluidStack drain = tank.drain(filled, true);
				if (drain == null || filled != drain.amount) {
					if (LPConstants.DEBUG) {
						throw new UnsupportedOperationException("Fluid Multiplication");
					}
				}
			}
		}
	}

	public int countOnRoute(FluidIdentifier ident) {
		int amount = 0;
		for (ItemRoutingInformation next : _inTransitToMe) {
			ItemIdentifierStack item = next.getItem();
			if (item.getItem().isFluidContainer()) {
				FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item);
				if (liquid.getFluid().equals(ident)) {
					amount += liquid.getAmount();
				}
			}
		}
		return amount;
	}

	public abstract boolean canInsertFromSideToTanks();

	public abstract boolean canInsertToTanks();

	public abstract boolean canReceiveFluid();

	public boolean endReached(LPTravelingItemServer arrivingItem, TileEntity tile) {
		if (canInsertToTanks() && MainProxy.isServer(getWorld())) {
			getCacheHolder().trigger(CacheTypes.Inventory);
			if (arrivingItem.getItemIdentifierStack() == null || !(arrivingItem.getItemIdentifierStack().getItem().isFluidContainer())) {
				return false;
			}
			if (getRouter().getSimpleID() != arrivingItem.getDestination()) {
				return false;
			}
			int filled = 0;
			FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(arrivingItem.getItemIdentifierStack());
			if (isConnectableTank(tile, arrivingItem.output, false)) {
				List<ITankUtil> adjTanks = getAdjacentTanks(false);
				//Try to put liquid into all adjacent tanks.
				for (ITankUtil util : adjTanks) {
					filled = util.fill(liquid, true);
					liquid.lowerAmount(filled);
					if (liquid.getAmount() != 0) {
						continue;
					}
					return true;
				}
				//Try inserting the liquid into the pipe side tank
				filled = ((PipeFluidTransportLogistics) transport).sideTanks[arrivingItem.output.ordinal()].fill(liquid.makeFluidStack(), true);
				if (filled == liquid.getAmount()) {
					return true;
				}
				liquid.lowerAmount(filled);
			}
			//Try inserting the liquid into the pipe internal tank
			filled = ((PipeFluidTransportLogistics) transport).internalTank.fill(liquid.makeFluidStack(), true);
			if (filled == liquid.getAmount()) {
				return true;
			}
			//If liquids still exist,
			liquid.lowerAmount(filled);

			//TODO: FIX THIS
			if (this instanceof IRequireReliableFluidTransport) {
				((IRequireReliableFluidTransport) this).liquidNotInserted(liquid.getFluid(), liquid.getAmount());
			}

			IRoutedItem routedItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(SimpleServiceLocator.logisticsFluidManager.getFluidContainer(liquid));
			Pair<Integer, Integer> replies = SimpleServiceLocator.logisticsFluidManager.getBestReply(liquid, getRouter(), routedItem.getJamList());
			int dest = replies.getValue1();
			routedItem.setDestination(dest);
			routedItem.setTransportMode(TransportMode.Passive);
			this.queueRoutedItem(routedItem, arrivingItem.output.getOpposite());
			return true;
		}
		return false;
	}

	@Override
	public boolean isFluidPipe() {
		return true;
	}

	@Override
	public boolean sharesInterestWith(CoreRoutedPipe other) {
		if (!(other instanceof FluidRoutedPipe)) {
			return false;
		}
		List<TileEntity> theirs = ((FluidRoutedPipe) other).getAllTankTiles();
		for (TileEntity tile : getAllTankTiles()) {
			if (theirs.contains(tile)) {
				return true;
			}
		}
		return false;
	}

	public List<TileEntity> getAllTankTiles() {
		List<TileEntity> list = new ArrayList<>();
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(false)) {
			list.addAll(SimpleServiceLocator.specialTankHandler.getBaseTileFor(pair.getValue2()));
		}
		return list;
	}

	public LogisticsFluidOrderManager getFluidOrderManager() {
		_orderFluidManager = _orderFluidManager != null ? _orderFluidManager : new LogisticsFluidOrderManager(this);
		return _orderFluidManager;
	}

	@Override
	public LogisticsOrderManager<?, ?> getOrderManager() {
		return getFluidOrderManager();
	}
}
