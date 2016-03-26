package logisticspipes.pipes.basic.fluid;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LPConstants;
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
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

public abstract class FluidRoutedPipe extends CoreRoutedPipe {

	private WorldUtil worldUtil;
	private LogisticsFluidOrderManager _orderFluidManager;

	public FluidRoutedPipe(Item item) {
		super(new PipeFluidTransportLogistics(), item);
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
	}

	@Override
	public boolean logisitcsIsPipeConnected(TileEntity tile, ForgeDirection dir) {
		if (tile instanceof IFluidHandler) {
			IFluidHandler liq = (IFluidHandler) tile;

			if (liq.getTankInfo(dir.getOpposite()) != null && liq.getTankInfo(dir.getOpposite()).length > 0) {
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
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (isFluidSidedTexture(connection)) {
			return Textures.LOGISTICSPIPE_LIQUID_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	private boolean isFluidSidedTexture(ForgeDirection connection) {
		WorldUtil util = new WorldUtil(getWorld(), getX(), getY(), getZ());
		TileEntity tile = util.getAdjacentTileEntitie(connection);
		if (tile instanceof IFluidHandler) {
			IFluidHandler liq = (IFluidHandler) tile;

			if (liq.getTankInfo(connection.getOpposite()) != null && liq.getTankInfo(connection.getOpposite()).length > 0) {
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

	public final List<Pair<TileEntity, ForgeDirection>> getAdjacentTanks(boolean flag) {
		if (worldUtil == null) {
			worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		}
		List<Pair<TileEntity, ForgeDirection>> tileList = new ArrayList<Pair<TileEntity, ForgeDirection>>();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldUtil.getAdjacentTileEntitie(dir);
			if (!isConnectableTank(tile, dir, flag)) {
				continue;
			}
			tileList.add(new Pair<TileEntity, ForgeDirection>(tile, dir));
		}
		return tileList;
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

	public final boolean isConnectableTank(TileEntity tile, ForgeDirection dir, boolean flag) {
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
			List<Pair<TileEntity, ForgeDirection>> list = getAdjacentTanks(true);
			for (Pair<TileEntity, ForgeDirection> pair : list) {
				if (pair.getValue1() instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) pair.getValue1()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank tank = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue2().ordinal()];
				validDirections++;
				if (tank.getFluid() == null) {
					continue;
				}
				int filled = ((IFluidHandler) pair.getValue1()).fill(pair.getValue2().getOpposite(), tank.getFluid().copy(), true);
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
			if (validDirections == 0) {
				return;
			}
			FluidTank tank = ((PipeFluidTransportLogistics) transport).internalTank;
			FluidStack stack = tank.getFluid();
			if (stack == null) {
				return;
			}
			for (Pair<TileEntity, ForgeDirection> pair : list) {
				if (pair.getValue1() instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) pair.getValue1()).pipe instanceof CoreRoutedPipe) {
						continue;
					}
				}
				FluidTank tankSide = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue2().ordinal()];
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
				FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item);
				if (FluidIdentifier.get(liquid).equals(ident)) {
					amount += liquid.amount;
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
			FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(arrivingItem.getItemIdentifierStack());
			if (isConnectableTank(tile, arrivingItem.output, false)) {
				List<Pair<TileEntity, ForgeDirection>> adjTanks = getAdjacentTanks(false);
				//Try to put liquid into all adjacent tanks.
				for (int i = 0; i < adjTanks.size(); i++) {
					Pair<TileEntity, ForgeDirection> pair = adjTanks.get(i);
					IFluidHandler tank = (IFluidHandler) pair.getValue1();
					ForgeDirection dir = pair.getValue2();
					filled = tank.fill(dir.getOpposite(), liquid.copy(), true);
					liquid.amount -= filled;
					if (liquid.amount != 0) {
						continue;
					}
					return true;
				}
				//Try inserting the liquid into the pipe side tank
				filled = ((PipeFluidTransportLogistics) transport).sideTanks[arrivingItem.output.ordinal()].fill(liquid, true);
				if (filled == liquid.amount) {
					return true;
				}
				liquid.amount -= filled;
			}
			//Try inserting the liquid into the pipe internal tank
			filled = ((PipeFluidTransportLogistics) transport).internalTank.fill(liquid, true);
			if (filled == liquid.amount) {
				return true;
			}
			//If liquids still exist,
			liquid.amount -= filled;

			//TODO: FIX THIS
			if (this instanceof IRequireReliableFluidTransport) {
				((IRequireReliableFluidTransport) this).liquidNotInserted(FluidIdentifier.get(liquid), liquid.amount);
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
		List<TileEntity> list = new ArrayList<TileEntity>();
		for (Pair<TileEntity, ForgeDirection> pair : getAdjacentTanks(false)) {
			list.addAll(SimpleServiceLocator.specialTankHandler.getBaseTileFor(pair.getValue1()));
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
