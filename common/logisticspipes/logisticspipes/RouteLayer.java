/** 
 * Copyright (c) Krapht, 2012
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import net.minecraftforge.common.ForgeDirection;

/**
 * @author Krapht
 * 
 * This class is responsible for resolving where incoming items should go.
 */
public class RouteLayer {

	private final IRouter _router;
	private final TransportLayer _transport;
	
	public RouteLayer(IRouter router, TransportLayer transport) {
		_router = router;
		_transport = transport;
	}
	
	public ForgeDirection getOrientationForItem(IRoutedItem item){
		
		item.checkIDFromUUID();
		//If items have no destination, see if we can get one (unless it has a source, then drop it)
		if (item.getDestination() < 0){
			if (item.getSource() >= 0) return ForgeDirection.UNKNOWN;
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), true);
		}
		
		//If the destination is unknown / unroutable		
		if (item.getDestination() >= 0 && !_router.hasRoute(item.getDestination())){
			if(!item.isItemRelayed()) {
				item = SimpleServiceLocator.logisticsManager.destinationUnreachable(item, _router.getSimpleID());
			} else {
				int destination = item.getDestination();
				for(SearchNode node:_router.getIRoutersByCost()) {
					if(node.node instanceof IFilteringRouter) {
						if(((IFilteringRouter)node.node).idIdforOtherSide(destination)) {
							item.replaceRelayID(node.node.getSimpleID());
							break;
						}
					}
				}
			}
		}
		
		//If we still have no destination or client side unroutable, drop it
		if (item.getDestination() < 0) { 
			return ForgeDirection.UNKNOWN;
		}
		
		//Is the destination ourself? Deliver it
		if (item.getDestinationUUID().equals(_router.getId())){
			
			if(item.isItemRelayed()) {
				item.itemRelayed();
			} else {
				
				if (!_transport.stillWantItem(item)){
					return getOrientationForItem(SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), true));
				}
				
				item.setDoNotBuffer(true);
				ForgeDirection o =_transport.itemArrived(item);
				return o != null?o:ForgeDirection.UNKNOWN;
			}
		}
		
		//Do we now know the destination?
		if (!_router.hasRoute(item.getDestination())){
			return ForgeDirection.UNKNOWN;
		}
		//Which direction should we send it
		return _router.getExitFor(item.getDestination());
	}
}
