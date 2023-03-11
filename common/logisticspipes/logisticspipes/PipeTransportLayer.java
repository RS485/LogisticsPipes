package logisticspipes.logisticspipes;

import java.util.LinkedList;
import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
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
	public EnumFacing itemArrived(IRoutedItem item, EnumFacing denied) {
		if (item.getItemIdentifierStack() != null) {
			_trackStatistics.receivedItem(item.getItemIdentifierStack().getStackSize());
		}

		// 1st priority, deliver to adjacent inventories
		LinkedList<EnumFacing> possibleEnumFacing = new LinkedList<>();
		for (NeighborTileEntity<TileEntity> adjacent : routedPipe.getAvailableAdjacent().inventories()) {
			if (_router.isRoutedExit(adjacent.getDirection())) {
				continue;
			}
			if (denied != null && denied.equals(adjacent.getDirection())) {
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

		// 2nd priority, deliver to non-routed exit
		new WorldCoordinatesWrapper(routedPipe.container).connectedTileEntities().stream()
				.filter(neighbor -> {
					if (_router.isRoutedExit(neighbor.getDirection())) return false;
					final CoreRoutedPipe routerPipe = _router.getPipe();
					return routerPipe == null || !routerPipe.isLockedExit(neighbor.getDirection());
				})
				.forEach(neighbor -> possibleEnumFacing.add(neighbor.getDirection()));

		if (possibleEnumFacing.size() == 0) {
			// last resort, drop item
			return null;
		} else {
			return possibleEnumFacing.get(routedPipe.getWorld().rand.nextInt(possibleEnumFacing.size()));
		}
	}

	@Override
	public boolean stillWantItem(IRoutedItem item) {
		// pipes are dumb and always want the item
		return true;
	}

}
