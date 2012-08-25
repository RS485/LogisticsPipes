package logisticspipes.logisticspipes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import logisticspipes.routing.IRouter;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.AdjacentTile;


import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
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
	public Orientations itemArrived(IRoutedItem item) {
		if (item.getItemStack() != null){
			_trackStatistics.recievedItem(item.getItemStack().stackSize);
		}
		
		item.setArrived(); //NOT TESTED
		this._router.inboundItemArrived((RoutedEntityItem) item); //NOT TESTED
		
		LinkedList<AdjacentTile> adjacentEntities = _worldAccess.getConnectedEntities();
		LinkedList<Orientations> possibleOrientations = new LinkedList<Orientations>();
		
		// 1st prio, deliver to adjacent IInventories
		
		for (AdjacentTile tile : adjacentEntities){
			if (tile.tile instanceof TileGenericPipe) continue;
			possibleOrientations.add(tile.orientation);
		}
		if (possibleOrientations.size() != 0){
			return possibleOrientations.get(_worldAccess.getRandomInt(possibleOrientations.size()));
		}
		
		// 2nd prio, deliver to non-routed exit
		for (AdjacentTile tile : adjacentEntities){
			if (_router.isRoutedExit(tile.orientation)) continue;
			possibleOrientations.add(tile.orientation);
		}
		// 3rd prio, drop item
		
		if (possibleOrientations.size() == 0){
			return null;
		}
		
		return possibleOrientations.get(_worldAccess.getRandomInt(possibleOrientations.size()));
	}


	//Pipes are dumb and always want the item
	@Override
	public boolean stillWantItem(IRoutedItem item) {
		return true;
	}

}
