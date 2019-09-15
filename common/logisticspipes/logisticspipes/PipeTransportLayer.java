package logisticspipes.logisticspipes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.Router;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

/**
 * This class is responsible for handling incoming items for standard pipes
 *
 * @author Krapht
 */
public class PipeTransportLayer extends TransportLayer {

	private final CoreRoutedPipe routedPipe;
	private final ITrackStatistics _trackStatistics;
	private final Router _router;

	public PipeTransportLayer(CoreRoutedPipe routedPipe, ITrackStatistics trackStatistics, Router router) {
		this.routedPipe = routedPipe;
		_trackStatistics = trackStatistics;
		_router = router;
	}

	@Override
	public Direction itemArrived(IRoutedItem item, Direction denyed) {
		if (item.getItemStack() != null) {
			_trackStatistics.recievedItem(item.getItemStack().getCount());
		}

		final List<NeighborBlockEntity<BlockEntity>> adjacentEntities = new WorldCoordinatesWrapper(routedPipe.container)
				.connectedTileEntities(IPipeInformationProvider.ConnectionPipeType.ITEM)
				.collect(Collectors.toList());
		LinkedList<Direction> possibleEnumFacing = new LinkedList<>();

		// 1st prio, deliver to adjacent IInventories

		for (NeighborBlockEntity<BlockEntity> adjacent : adjacentEntities) {
			if (PipeInformationManager.INSTANCE.isItemPipe(adjacent.getBlockEntity())) {
				continue;
			}
			if (_router.isRoutedExit(adjacent.getDirection())) {
				continue;
			}
			if (denyed != null && denyed.equals(adjacent.getDirection())) {
				continue;
			}

			CoreRoutedPipe pipe = _router.getPipe();
			if (pipe != null) {
				if (pipe.isLockedExit(adjacent.getDirection())) {
					continue;
				}
			}

			possibleEnumFacing.add(adjacent.getDirection());
		}
		if (possibleEnumFacing.size() != 0) {
			return possibleEnumFacing.get(routedPipe.getWorld().rand.nextInt(possibleEnumFacing.size()));
		}

		// 2nd prio, deliver to non-routed exit
		for (NeighborBlockEntity<BlockEntity> adjacent : adjacentEntities) {
			if (_router.isRoutedExit(adjacent.getDirection())) {
				continue;
			}
			CoreRoutedPipe pipe = _router.getPipe();

			if (pipe != null) {
				if (pipe.isLockedExit(adjacent.getDirection())) {
					continue;
				}
			}

			possibleEnumFacing.add(adjacent.getDirection());
		}
		// 3rd prio, drop item

		if (possibleEnumFacing.size() == 0) {
			return null;
		}

		return possibleEnumFacing.get(routedPipe.getWorld().rand.nextInt(possibleEnumFacing.size()));
	}

	// Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
