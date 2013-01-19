/** 
 * Copyright (c) Krapht, 2012
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.UUID;

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
		
		//If items have no destination, see if we can get one (unless it has a source, then drop it)
		if (item.getDestination() == null){
			if (item.getSource() != null) return ForgeDirection.UNKNOWN;
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getId(), true);
		}
		
		//If the destination is unknown / unroutable		
		if (item.getDestination() != null && !_router.hasRoute(item.getDestination())){
			if(!item.isItemRelayed()) {
				item = SimpleServiceLocator.logisticsManager.destinationUnreachable(item, _router.getId());
			} else {
				UUID destination = item.getDestination();
				for(SearchNode node:_router.getIRoutersByCost()) {
					if(node.node instanceof IFilteringRouter) {
						if(((IFilteringRouter)node.node).idIdforOtherSide(destination)) {
							item.replaceRelayID(node.node.getId());
							break;
						}
					}
				}
			}
		}
		
		//If we still have no destination or client side unroutable, drop it
		if (item.getDestination() == null) { 
			return ForgeDirection.UNKNOWN;
		}
		
		//Is the destination ourself? Deliver it
		if (item.getDestination().equals(_router.getId()) || (_router instanceof IFilteringRouter && ((IFilteringRouter)_router).idIdforOtherSide(item.getDestination()))){
			
			if(item.isItemRelayed()) {
				item.itemRelayed();
			} else {
				
				if (!_transport.stillWantItem(item)){
					return getOrientationForItem(SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getId(), true));
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
