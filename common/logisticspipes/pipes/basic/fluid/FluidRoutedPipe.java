package logisticspipes.pipes.basic.fluid;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeFluidUtil;
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
import logisticspipes.utils.FluidSinkReply;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

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
		if (SimpleServiceLocator.enderIOProxy.isBundledPipe(tile)) {
			return SimpleServiceLocator.enderIOProxy.isFluidConduit(tile, dir.getOpposite());
		}

		ITankUtil tank = PipeFluidUtil.INSTANCE.getTankUtilForTE(tile, dir.getOpposite());
		return (tank != null && tank.containsTanks()) || tile instanceof LogisticsTileGenericPipe;
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
		return getAvailableAdjacent().fluidTanks().stream()
				.filter(neighbor -> neighbor.getDirection() == connection)
				.findFirst()
				.map(neighbor -> {
					final ITankUtil tankUtil = LPNeighborTileEntityKt.getTankUtil(neighbor);
					return tankUtil != null && tankUtil.containsTanks();
				})
				.orElse(false);
	}

	@Override
	public @Nullable LogisticsModule getLogisticsModule() {
		return null;
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
		boolean fluidTile = false;
		if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir)) {
			IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir);
			if (fluidHandler != null) {
				fluidTile = true;
			}
		}
		if (tile instanceof IFluidHandler) {
			fluidTile = true;
		}
		if (!fluidTile) {
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
			final List<Pair<NeighborTileEntity<TileEntity>, ITankUtil>> list =
					PipeFluidUtil.INSTANCE.getAdjacentTanks(this, true);
			for (Pair<NeighborTileEntity<TileEntity>, ITankUtil> pair : list) {
				if (pair.getValue2() instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) pair.getValue2()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank internalTank = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue1().getDirection().ordinal()];
				validDirections++;
				if (internalTank.getFluid() == null) {
					continue;
				}
				int filled = pair.getValue2().fill(FluidIdentifierStack.getFromStack(internalTank.getFluid()), true);
				if (filled == 0) {
					continue;
				}
				FluidStack drain = internalTank.drain(filled, true);
				if (drain == null || filled != drain.amount) {
					if (LogisticsPipes.isDEBUG()) {
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
			for (Pair<NeighborTileEntity<TileEntity>, ITankUtil> pair : list) {
				if (pair.getValue1().isLogisticsPipe()) {
					if (((LogisticsTileGenericPipe) pair.getValue1().getTileEntity()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank tankSide = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue1().getDirection().ordinal()];
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
					if (LogisticsPipes.isDEBUG()) {
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
			int filled;
			FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(arrivingItem.getItemIdentifierStack());
			if (isConnectableTank(tile, arrivingItem.output, false)) {
				//Try to put liquid into all adjacent tanks.
				for (Pair<NeighborTileEntity<TileEntity>, ITankUtil> util : PipeFluidUtil.INSTANCE.getAdjacentTanks(this, false)) {
					filled = util.getValue2().fill(liquid, true);
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
			Pair<Integer, FluidSinkReply> replies = SimpleServiceLocator.logisticsFluidManager.getBestReply(liquid, getRouter(), routedItem.getJamList());
			if (replies == null) {
				// clear destination without marking item as lost
				routedItem.setDestination(0);
			} else {
				int dest = replies.getValue1();
				routedItem.setDestination(dest);
			}
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
	public boolean isOnSameContainer(CoreRoutedPipe other) {
		if (!(other instanceof FluidRoutedPipe)) {
			return false;
		}
		List<TileEntity> theirs = PipeFluidUtil.INSTANCE.getAllTankTiles((FluidRoutedPipe) other);
		for (TileEntity tile : PipeFluidUtil.INSTANCE.getAllTankTiles(this)) {
			if (theirs.contains(tile)) {
				return true;
			}
		}
		return false;
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
