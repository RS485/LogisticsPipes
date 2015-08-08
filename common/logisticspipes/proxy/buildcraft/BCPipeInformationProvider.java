package logisticspipes.proxy.buildcraft;

import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

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
	public boolean isCorrect() {
		return pipe != null && pipe.pipe != null && pipe.pipe != null && SimpleServiceLocator.buildCraftProxy.isActive();
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
	public TileEntity getTile(ForgeDirection direction) {
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
		if (pipe.pipe instanceof PipeItemsObsidian) { //Obsidian seperates networks
			return true;
		}
		if (pipe.pipe instanceof PipeStructureCobblestone) { //don't recurse onto structure pipes.
			return true;
		}
		return false;
	}

	@Override
	public boolean powerOnly() {
		if (pipe.pipe instanceof PipeItemsDiamond) { //Diamond only allows power through
			return true;
		}
		return false;
	}

	@Override
	public boolean isOnewayPipe() {
		if (pipe.pipe instanceof PipeItemsIron) { //Iron requests and power can come from closed sides
			return true;
		}
		return false;
	}

	@Override
	public boolean isOutputOpen(ForgeDirection direction) {
		return pipe.pipe.outputOpen(direction);
	}

	@Override
	public boolean canConnect(TileEntity to, ForgeDirection direction, boolean flag) {
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
	public double getDistanceTo(int destinationint, ForgeDirection ignore, ItemIdentifier ident, boolean isActive, double traveled, double max, List<LPPosition> visited) {
		if (traveled >= max) {
			return Integer.MAX_VALUE;
		}
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (ignore == dir) {
				continue;
			}
			IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(getTile(dir));
			if (information != null) {
				LPPosition pos = new LPPosition(information);
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
			TravelingItem bcItem = null;
			if (item instanceof LPTravelingItemServer) {
				LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem();
				lpBCItem.setRoutingInformation(((LPTravelingItemServer) item).getInfo());
				lpBCItem.saveToExtraNBTData();
				bcItem = lpBCItem;
			} else {
				return true;
			}
			LPPosition p = new LPPosition(pipe.xCoord + 0.5F, pipe.yCoord + CoreConstants.PIPE_MIN_POS, pipe.zCoord + 0.5F);
			if (item.output.getOpposite() == ForgeDirection.DOWN) {
				p.moveForward(item.output.getOpposite(), 0.24F);
			} else if (item.output.getOpposite() == ForgeDirection.UP) {
				p.moveForward(item.output.getOpposite(), 0.74F);
			} else {
				p.moveForward(item.output.getOpposite(), 0.49F);
			}
			bcItem.setPosition(p.getXD(), p.getYD(), p.getZD());
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
	public void refreshTileCacheOnSide(ForgeDirection side) {
		TileBuffer[] cache = pipe.getTileCache();
		if (cache != null) {
			cache[side.ordinal()].refresh();
		}
	}
}
