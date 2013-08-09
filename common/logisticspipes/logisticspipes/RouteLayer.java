/** 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.RoutedEntityItem;
import net.minecraftforge.common.ForgeDirection;

/**
 * @author Krapht
 * 
 * This class is responsible for resolving where incoming items should go.
 */
public class RouteLayer {

	protected final IRouter _router;
	private final TransportLayer _transport;
	
	public RouteLayer(IRouter router, TransportLayer transportLayer) {
		_router = router;
		_transport = transportLayer;
	}
	
	public ForgeDirection getOrientationForItem(IRoutedItem item, ForgeDirection blocked){
		
		item.checkIDFromUUID();
		//If a item has no destination, find one
		if (item.getDestination() < 0) {
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), false);
		}
		
		//If the destination is unknown / unroutable or it already arrived at its destination and somehow looped back		
		if (item.getDestination() >= 0 && (!_router.hasRoute(item.getDestination()) || item.getArrived())){
			if(!item.isItemRelayed()) {
				item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), false);
			} else {
				int destination = item.getDestination();
				for(ExitRoute node:_router.getIRoutersByCost()) {
					if(node.destination instanceof IFilteringRouter) {
						if(((IFilteringRouter)node.destination).isIdforOtherSide(destination)) {
							item.replaceRelayID(node.destination.getSimpleID());
							break;
						}
					}
				}
			}
		}
		
		item.checkIDFromUUID();
		//If we still have no destination or client side unroutable, drop it
		if (item.getDestination() < 0) { 
			return ForgeDirection.UNKNOWN;
		}
		
		//Is the destination ourself? Deliver it
		if (item.getDestinationUUID().equals(_router.getId())){
			
			if (!_transport.stillWantItem(item)){
				_router.inboundItemArrived((RoutedEntityItem) item);
				return getOrientationForItem(SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), true), null);
			}
			
			item.setDoNotBuffer(true);
			ForgeDirection o =_transport.itemArrived(item, blocked);
			return o != null?o:ForgeDirection.UNKNOWN;
		}
		
		//Do we now know the destination?
		if (!_router.hasRoute(item.getDestination())){
			return ForgeDirection.UNKNOWN;
		}
		//Which direction should we send it
		return _router.getExitFor(item.getDestination());
	}
}
