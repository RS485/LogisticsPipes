package logisticspipes.logisticspipes;

import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.routing.IRouter;
import net.minecraftforge.common.ForgeDirection;

public class RouteLayerFirewall extends RouteLayer{
	public RouteLayerFirewall(IRouter iRouter, TransportLayer transportLayer) {
		super(iRouter, transportLayer);
	}
	
	@Override
	public ForgeDirection getOrientationForItem(IRoutedItem item, ForgeDirection blocked){
		
		item.checkIDFromUUID();
		//If a item has no destination, find one
		if (item.getDestination() < 0) {
			return super.getOrientationForItem(item, blocked);
		}
		int destination = item.getDestination();
		// if the item isRelayed and has arrived at us, flip the destination to ourselves, so it can continue on its way.
		if(item.isItemRelayed()){
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				IFilteringRouter node =(IFilteringRouter)(_router.getCachedPipe().getRouter(dir));
				if(node.getSimpleID()==destination) {
					// check to see if the next step is valid here...
					item.itemRelayed();
					break;
				}
			}
		}
		return super.getOrientationForItem(item, blocked);
	}	
}
