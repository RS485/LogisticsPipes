package logisticspipes.logisticspipes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

/**
 * This class is responsible for handling incoming items for standard pipes
 *
 * @author Krapht
 */
public class PipeTransportLayer extends TransportLayer {

	private final CoreRoutedPipe routedPipe;
	private final ITrackStatistics _trackStatistics;
	private final @Nonnull IRouter _router;

	public PipeTransportLayer(CoreRoutedPipe routedPipe, ITrackStatistics trackStatistics, @Nonnull IRouter router) {
		this.routedPipe = routedPipe;
		_trackStatistics = trackStatistics;
		_router = router;
	}

	@Override
	public EnumFacing itemArrived(IRoutedItem item, EnumFacing denyed) {
		if (item.getItemIdentifierStack() != null) {
			_trackStatistics.recievedItem(item.getItemIdentifierStack().getStackSize());
		}

		final List<NeighborTileEntity<TileEntity>> adjacentEntities = new WorldCoordinatesWrapper(routedPipe.container)
				.connectedTileEntities(IPipeInformationProvider.ConnectionPipeType.ITEM)
				.collect(Collectors.toList());
		LinkedList<EnumFacing> possibleEnumFacing = new LinkedList<>();

		// 1st prio, deliver to adjacent IInventories

		for (NeighborTileEntity<TileEntity> adjacent : adjacentEntities) {
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(adjacent.getTileEntity())) {
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
		for (NeighborTileEntity<TileEntity> adjacent : adjacentEntities) {
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

	//Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
