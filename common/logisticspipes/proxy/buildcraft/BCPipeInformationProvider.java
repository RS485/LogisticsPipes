package logisticspipes.proxy.buildcraft;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import logisticspipes.LPConstants;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.ReflectionHelper;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamondItem;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDirectional;
import buildcraft.transport.pipe.behaviour.PipeBehaviourIron;
import buildcraft.transport.pipe.behaviour.PipeBehaviourObsidian;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;
import buildcraft.transport.tile.TilePipeHolder;
import sun.plugin.dom.core.CoreConstants;

public class BCPipeInformationProvider implements IPipeInformationProvider {

	private final TilePipeHolder pipe;

	public BCPipeInformationProvider(TilePipeHolder pipe) {
		this.pipe = pipe;
	}

	@Override
	public boolean isCorrect(ConnectionPipeType type) {
		if (pipe == null || pipe.getPipe() == null || !SimpleServiceLocator.buildCraftProxy.isActive()) {
			return false;
		}

		boolean precheck = false;
		if (type == ConnectionPipeType.UNDEFINED) {
			precheck = pipe.getPipe().getDefinition().flowType == PipeApi.flowItems || pipe.getPipe().getDefinition().flowType == PipeApi.flowFluids;
		} else if (type == ConnectionPipeType.ITEM) {
			precheck = pipe.getPipe().getDefinition().flowType == PipeApi.flowItems;
		} else if (type == ConnectionPipeType.FLUID) {
			precheck = pipe.getPipe().getDefinition().flowType == PipeApi.flowFluids;
		}
		return precheck;
	}

	@Override
	public int getX() {
		return pipe.getPipePos().getX();
	}

	@Override
	public int getY() {
		return pipe.getPipePos().getY();
	}

	@Override
	public int getZ() {
		return pipe.getPipePos().getZ();
	}

	@Override
	public World getWorld() {
		return pipe.getWorld();
	}

	@Override
	public boolean isRouterInitialized() {
		return pipe.getPipe() != null;
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
		return pipe.getNeighbourTile(direction);
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
		return (pipe.getPipe().getDefinition().flowType == PipeApi.flowItems && pipe.getPipe().getBehaviour() instanceof PipeBehaviourObsidian) || pipe.getPipe().getDefinition().flowType == PipeApi.flowStructure;
	}

	@Override
	public boolean powerOnly() {
		return (pipe.getPipe().getDefinition().flowType == PipeApi.flowItems && pipe.getPipe().getBehaviour() instanceof PipeBehaviourDiamondItem);
	}

	@Override
	public boolean isOnewayPipe() {
		return (pipe.getPipe().getDefinition().flowType == PipeApi.flowItems && pipe.getPipe().getBehaviour() instanceof PipeBehaviourIron);
	}

	@Override
	public boolean isOutputOpen(EnumFacing direction) {
		EnumFacing point = ReflectionHelper.invokePrivateMethod(PipeBehaviourDirectional.class, pipe.getPipe().getBehaviour(), "getCurrentDir", "getCurrentDir", new Class[0], new Object[0]);
		return point == direction;
	}

	@Override
	public boolean canConnect(TileEntity to, EnumFacing direction, boolean targeted) {
		return pipe.getPipe().isConnected(direction);
	}

	@Override
	public double getDistance() {
		return 1;
	}

	@Override
	public double getDistanceWeight() {
		return 1;
	}

	@Override
	public boolean isItemPipe() {
		return pipe != null && pipe.getPipe() != null && pipe.getPipe().getDefinition().flowType == PipeApi.flowItems && SimpleServiceLocator.buildCraftProxy.isActive();
	}

	@Override
	public boolean isFluidPipe() {
		return pipe != null && pipe.getPipe() != null && pipe.getPipe().getDefinition().flowType == PipeApi.flowFluids && SimpleServiceLocator.buildCraftProxy.isActive();
	}

	@Override
	public boolean isPowerPipe() {
		return pipe != null && pipe.getPipe() != null && pipe.getPipe().getDefinition().flowType == PipeApi.flowPower && SimpleServiceLocator.buildCraftProxy.isActive();
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
		if (pipe != null && pipe.getPipe() != null && pipe.getPipe().getDefinition().flowType == PipeApi.flowItems) {
			TravellingItem bcItem;
			if (item instanceof LPTravelingItemServer) {
				LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem(item.getItemIdentifierStack().makeNormalStack());
				lpBCItem.setRoutingInformation(((LPTravelingItemServer) item).getInfo());
				//lpBCItem.saveToExtraNBTData();
				bcItem = lpBCItem;
			} else {
				return true;
			}

			DoubleCoordinates p = new DoubleCoordinates(pipe.getPos().getX() + 0.5F, pipe.getPos().getY() + LPConstants.PIPE_MIN_POS, pipe.getPos().getZ() + 0.5F);
			double move;
			if (item.output.getOpposite() == EnumFacing.DOWN) {
				move = 0.24;
			} else if (item.output.getOpposite() == EnumFacing.UP) {
				move = 0.74;
			} else {
				move = 0.49;
			}
			CoordinateUtils.add(p, item.output.getOpposite(), move);

			ReflectionHelper.setPrivateField(TravellingItem.class, bcItem, "side", "side", item.output.getOpposite());
			ReflectionHelper.setPrivateField(TravellingItem.class, bcItem, "toCenter", "toCenter", true);
			ReflectionHelper.setPrivateField(TravellingItem.class, bcItem, "speed", "speed", item.getSpeed());
//			bcItem.colour = onInsert.colour;
			ReflectionHelper.invokePrivateMethod(TravellingItem.class, bcItem, "genTimings", "genTimings", new Class[]{int.class, int.class}, new Object[]{from.getWorld().getTotalWorldTime(), 0});
			EnumSet<EnumFacing> tried = ReflectionHelper.getPrivateField(TravellingItem.class, bcItem, "tried", "tried");
			tried.add(item.output.getOpposite());

			ReflectionHelper.invokePrivateMethod(PipeFlowItems.class, pipe.getPipe().getFlow(), "addItemTryMerge", "addItemTryMerge", new Class[]{TravellingItem.class}, new Object[]{bcItem});
			return true;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(EnumFacing side) {

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