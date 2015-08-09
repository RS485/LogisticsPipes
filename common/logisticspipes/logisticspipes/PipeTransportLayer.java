package logisticspipes.logisticspipes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper.AdjacentTileEntity;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * This class is responsible for handling incoming items for standard pipes
 *
 * @author Krapht
 */
public class PipeTransportLayer extends TransportLayer {

	private final CoreRoutedPipe routedPipe;
	private final ITrackStatistics _trackStatistics;
	private final IRouter _router;

	public PipeTransportLayer(CoreRoutedPipe routedPipe, ITrackStatistics trackStatistics, IRouter router) {
		this.routedPipe = routedPipe;
		_trackStatistics = trackStatistics;
		_router = router;
	}

	@Override
	public ForgeDirection itemArrived(IRoutedItem item, ForgeDirection denyed) {
		if (item.getItemIdentifierStack() != null) {
			_trackStatistics.recievedItem(item.getItemIdentifierStack().getStackSize());
		}

		List<AdjacentTileEntity> adjacentEntities = new WorldCoordinatesWrapper(routedPipe.container)
				.getConnectedAdjacentTileEntities(IPipeInformationProvider.ConnectionPipeType.ITEM).collect(Collectors.toList());
		LinkedList<ForgeDirection> possibleForgeDirection = new LinkedList<ForgeDirection>();

		// 1st prio, deliver to adjacent IInventories

		for (AdjacentTileEntity adjacent : adjacentEntities) {
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.tileEntity)) {
				continue;
			}
			if (_router.isRoutedExit(adjacent.direction)) {
				continue;
			}
			if (denyed != null && denyed.equals(adjacent.direction)) {
				continue;
			}

			CoreRoutedPipe pipe = _router.getPipe();
			if (pipe != null) {
				if (pipe.isLockedExit(adjacent.direction)) {
					continue;
				}
			}

			possibleForgeDirection.add(adjacent.direction);
		}
		if (possibleForgeDirection.size() != 0) {
			return possibleForgeDirection.get(routedPipe.getWorld().rand.nextInt(possibleForgeDirection.size()));
		}

		// 2nd prio, deliver to non-routed exit
		for (AdjacentTileEntity adjacent : adjacentEntities) {
			if (_router.isRoutedExit(adjacent.direction)) {
				continue;
			}
			CoreRoutedPipe pipe = _router.getPipe();

			if (pipe != null) {
				if (pipe.isLockedExit(adjacent.direction)) {
					continue;
				}
			}

			possibleForgeDirection.add(adjacent.direction);
		}
		// 3rd prio, drop item

		if (possibleForgeDirection.size() == 0) {
			return null;
		}

		return possibleForgeDirection.get(routedPipe.getWorld().rand.nextInt(possibleForgeDirection.size()));
	}

	//Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
