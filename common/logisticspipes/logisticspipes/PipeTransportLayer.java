package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.AdjacentTile;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;

/**
 * This class is responsible for handling incoming items for standard pipes
 * @author Krapht
 *
 */
public class PipeTransportLayer extends TransportLayer{

	private final IAdjacentWorldAccess _worldAccess;
	private final ITrackStatistics _trackStatistics;
	private final IRouter _router;
	
	public PipeTransportLayer(IAdjacentWorldAccess worldAccess, ITrackStatistics trackStatistics, IRouter router){
		this._worldAccess = worldAccess;
		this._trackStatistics = trackStatistics;
		this._router = router;
	}
	
	
	@Override
	public ForgeDirection itemArrived(IRoutedItem item, ForgeDirection denyed) {
		if (item.getItemStack() != null){
			_trackStatistics.recievedItem(item.getItemStack().stackSize);
		}
		
		item.setArrived(true);
		this._router.inboundItemArrived((RoutedEntityItem) item); //NOT TESTED
		
		LinkedList<AdjacentTile> adjacentEntities = _worldAccess.getConnectedEntities();
		LinkedList<ForgeDirection> possibleForgeDirection = new LinkedList<ForgeDirection>();
		
		// 1st prio, deliver to adjacent IInventories
		
		for (AdjacentTile tile : adjacentEntities){
			if (tile.tile instanceof TileGenericPipe) continue;
			if(denyed != null && denyed.equals(tile.orientation)) continue;
			
			CoreRoutedPipe pipe = _router.getPipe();
			if(pipe != null) {
				if(pipe.isLockedExit(tile.orientation)) {
					continue;
				}
			}
			
			possibleForgeDirection.add(tile.orientation);
		}
		if (possibleForgeDirection.size() != 0){
			return possibleForgeDirection.get(_worldAccess.getRandomInt(possibleForgeDirection.size()));
		}
		
		// 2nd prio, deliver to non-routed exit
		for (AdjacentTile tile : adjacentEntities){
			if (_router.isRoutedExit(tile.orientation)) continue;
			CoreRoutedPipe pipe = _router.getPipe();
			
			if(pipe != null) {
				if(pipe.isLockedExit(tile.orientation)) {
					continue;
				}
			}
			
			possibleForgeDirection.add(tile.orientation);
		}
		// 3rd prio, drop item
		
		if (possibleForgeDirection.size() == 0){
			return null;
		}
		
		return possibleForgeDirection.get(_worldAccess.getRandomInt(possibleForgeDirection.size()));
	}


	//Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
