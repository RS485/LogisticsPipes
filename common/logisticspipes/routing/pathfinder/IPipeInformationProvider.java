package logisticspipes.routing.pathfinder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface IPipeInformationProvider {

	enum ConnectionPipeType {
		ITEM,
		FLUID,
		MULTI,
		UNDEFINED
	}

	boolean isCorrect(ConnectionPipeType type);

	BlockPos getPos();

	World getWorld();

	boolean isRouterInitialized();

	boolean isRoutingPipe();

	CoreRoutedPipe getRoutingPipe();

	BlockEntity getNextConnectedTile(Direction direction);

	boolean isFirewallPipe();

	IFilter getFirewallFilter();

	BlockEntity getTile();

	boolean divideNetwork();

	boolean powerOnly();

	boolean isOneWayPipe();

	boolean isOutputOpen(Direction direction);

	boolean canConnect(BlockEntity to, Direction direction, boolean flag);

	double getDistance();

	double getDistanceWeight();

	boolean isItemPipe();

	boolean isFluidPipe();

	boolean isPowerPipe();

	double getDistanceTo(UUID destination, Direction ignore, ItemVariant item, boolean isActive, double traveled, double max, List<BlockPos> visited);

	boolean acceptItem(LPTravelingItem item, BlockEntity from);

	void refreshTileCacheOnSide(Direction side);

	boolean isMultiBlock();

	Stream<BlockEntity> getPartsOfPipe();
}
