package logisticspipes.proxy.buildcraft;

import java.util.List;
import java.util.stream.Stream;

import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.core.CoreConstants;
import buildcraft.core.lib.TileBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeStructureCobblestone;

public class BCPipeInformationProvider implements IPipeInformationProvider {

	private final TileGenericPipe pipe;

	public BCPipeInformationProvider(TileGenericPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public boolean isCorrect(ConnectionPipeType type) {
		if (pipe == null || pipe.pipe == null || !SimpleServiceLocator.buildCraftProxy.isActive()) {
			return false;
		}

		boolean precheck = false;
		if (type == ConnectionPipeType.UNDEFINED) {
			precheck = pipe.pipe.transport instanceof PipeTransportItems || pipe.pipe.transport instanceof PipeTransportFluids;
		} else if (type == ConnectionPipeType.ITEM) {
			precheck = pipe.pipe.transport instanceof PipeTransportItems;
		} else if (type == ConnectionPipeType.FLUID) {
			precheck = pipe.pipe.transport instanceof PipeTransportFluids;
		}
		return precheck;
	}

	@Override
	public int getX() {
		return pipe.xCoord;
	}

	@Override
	public int getY() {
		return pipe.yCoord;
	}

	@Override
	public int getZ() {
		return pipe.zCoord;
	}

	@Override
	public World getWorld() {
		return pipe.getWorld();
	}

	@Override
	public boolean isRouterInitialized() {
		return pipe.initialized;
	}

	@Override
	public boolean isRoutingPipe() {
		return false;
	}

	@Override
	public CoreRoutedPipe getRoutingPipe() {
		throw new RuntimeException("This is no routing pipe");
	}

	@Override
	public TileEntity getNextConnectedTile(EnumFacing direction) {
		return pipe.getTile(direction);
	}

	@Override
	public boolean isFirewallPipe() {
		return false;
	}

	@Override
	public IFilter getFirewallFilter() {
		throw new RuntimeException("This is not a firewall pipe");
	}

	@Override
	public TileEntity getTile() {
		return pipe;
	}

	@Override
	public boolean divideNetwork() {
		//Obsidian seperates networks
		return pipe.pipe instanceof PipeItemsObsidian || pipe.pipe instanceof PipeStructureCobblestone;
	}

	@Override
	public boolean powerOnly() {
		return pipe.pipe instanceof PipeItemsDiamond;
	}

	@Override
	public boolean isOnewayPipe() {
		return pipe.pipe instanceof PipeItemsIron;
	}

	@Override
	public boolean isOutputOpen(EnumFacing direction) {
		return pipe.pipe.outputOpen(direction);
	}

	@Override
	public boolean canConnect(TileEntity to, EnumFacing direction, boolean flag) {
		return SimpleServiceLocator.buildCraftProxy.canPipeConnect(pipe, to, direction);
	}

	@Override
	public double getDistance() {
		return 1;
	}

	@Override
	public boolean isItemPipe() {
		return pipe != null && pipe.pipe != null && pipe.pipe.transport instanceof PipeTransportItems && SimpleServiceLocator.buildCraftProxy.isActive();
	}

	@Override
	public boolean isFluidPipe() {
		return pipe != null && pipe.pipe != null && pipe.pipe.transport instanceof PipeTransportFluids && SimpleServiceLocator.buildCraftProxy.isActive();
	}

	@Override
	public boolean isPowerPipe() {
		return pipe != null && pipe.pipe != null && pipe.pipe.transport instanceof PipeTransportPower && SimpleServiceLocator.buildCraftProxy.isActive();
	}

	@Override
	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double traveled, double max, List<DoubleCoordinates> visited) {
		if (traveled >= max) {
			return Integer.MAX_VALUE;
		}
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (ignore == dir) {
				continue;
			}
			IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(getNextConnectedTile(dir));
			if (information != null) {
				DoubleCoordinates pos = new DoubleCoordinates(information);
				if (visited.contains(pos)) {
					continue;
				}
				visited.add(pos);
				double result = information.getDistanceTo(destinationint, dir.getOpposite(), ident, isActive, traveled + getDistance(), max, visited);
				visited.remove(pos);
				if (result == Integer.MAX_VALUE) {
					return result;
				}
				return result + (int) getDistance();
			}
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, TileEntity from) {
		if (BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
			TravelingItem bcItem;
			if (item instanceof LPTravelingItemServer) {
				LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem();
				lpBCItem.setRoutingInformation(((LPTravelingItemServer) item).getInfo());
				lpBCItem.saveToExtraNBTData();
				bcItem = lpBCItem;
			} else {
				return true;
			}

			DoubleCoordinates p = new DoubleCoordinates(pipe.xCoord + 0.5F, pipe.yCoord + CoreConstants.PIPE_MIN_POS, pipe.zCoord + 0.5F);
			double move;
			if (item.output.getOpposite() == EnumFacing.DOWN) {
				move = 0.24;
			} else if (item.output.getOpposite() == EnumFacing.UP) {
				move = 0.74;
			} else {
				move = 0.49;
			}
			CoordinateUtils.add(p, item.output.getOpposite(), move);

			bcItem.setPosition(p.getXCoord(), p.getYCoord(), p.getZCoord());
			bcItem.setSpeed(item.getSpeed());
			if (item.getItemIdentifierStack() != null) {
				bcItem.setItemStack(item.getItemIdentifierStack().makeNormalStack());
			}
			((PipeTransportItems) pipe.pipe.transport).injectItem(bcItem, item.output);
			return true;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(EnumFacing side) {
		TileBuffer[] cache = pipe.getTileCache();
		if (cache != null) {
			cache[side.ordinal()].refresh();
		}
	}

	@Override
	public boolean isMultiBlock() {
		return false;
	}

	@Override
	public Stream<TileEntity> getPartsOfPipe() {
		return Stream.empty();
	}
}
