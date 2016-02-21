package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.AdjacentTile;

import net.minecraft.util.EnumFacing;

/**
 * This class is responsible for handling incoming items for standard pipes
 * 
 * @author Krapht
 */
public class PipeTransportLayer extends TransportLayer {

	private final IAdjacentWorldAccess _worldAccess;
	private final ITrackStatistics _trackStatistics;
	private final IRouter _router;

	public PipeTransportLayer(IAdjacentWorldAccess worldAccess, ITrackStatistics trackStatistics, IRouter router) {
		_worldAccess = worldAccess;
		_trackStatistics = trackStatistics;
		_router = router;
	}

	@Override
	public EnumFacing itemArrived(IRoutedItem item, EnumFacing denyed) {
		if (item.getItemIdentifierStack() != null) {
			_trackStatistics.recievedItem(item.getItemIdentifierStack().getStackSize());
		}

		LinkedList<AdjacentTile> adjacentEntities = _worldAccess.getConnectedEntities();
		LinkedList<EnumFacing> possibleEnumFacing = new LinkedList<EnumFacing>();

		// 1st prio, deliver to adjacent IInventories

		for (AdjacentTile tile : adjacentEntities) {
			if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile.tile)) {
				continue;
			}
			if (_router.isRoutedExit(tile.orientation)) {
				continue;
			}
			if (denyed != null && denyed.equals(tile.orientation)) {
				continue;
			}

			CoreRoutedPipe pipe = _router.getPipe();
			if (pipe != null) {
				if (pipe.isLockedExit(tile.orientation)) {
					continue;
				}
			}

			possibleEnumFacing.add(tile.orientation);
		}
		if (possibleEnumFacing.size() != 0) {
			return possibleEnumFacing.get(_worldAccess.getRandomInt(possibleEnumFacing.size()));
		}

		// 2nd prio, deliver to non-routed exit
		for (AdjacentTile tile : adjacentEntities) {
			if (_router.isRoutedExit(tile.orientation)) {
				continue;
			}
			CoreRoutedPipe pipe = _router.getPipe();

			if (pipe != null) {
				if (pipe.isLockedExit(tile.orientation)) {
					continue;
				}
			}

			possibleEnumFacing.add(tile.orientation);
		}
		// 3rd prio, drop item

		if (possibleEnumFacing.size() == 0) {
			return null;
		}

		return possibleEnumFacing.get(_worldAccess.getRandomInt(possibleEnumFacing.size()));
	}

	//Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
