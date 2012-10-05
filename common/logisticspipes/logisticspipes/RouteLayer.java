/** 
 * Copyright (c) Krapht, 2012
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;

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
	
	public Orientations getOrientationForItem(IRoutedItem item, World world){

		if(item.getDestination() == null && MainProxy.isClient(world)) {
			return null;
		}
		
		//If items have no destination, see if we can get one (unless it has a source, then drop it)
		if (item.getDestination() == null){
			if (item.getSource() != null) return Orientations.Unknown;
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getId(), false);
		}
		
		//If the destination is unknown / unroutable		
		if (item.getDestination() != null && !_router.hasRoute(item.getDestination())){
				item = SimpleServiceLocator.logisticsManager.destinationUnreachable(item, _router.getId());
		}
		
		//If we still have no destination, drop it
		if (item.getDestination() == null){ 
			return Orientations.Unknown;
		}
		
		//Is the destination ourself? Deliver it
		if (item.getDestination().equals(_router.getId())){
			
			if (!_transport.stillWantItem(item)){
				return getOrientationForItem(SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getId(), true), world);
			}
			
			item.setDoNotBuffer(true);
			Orientations o =_transport.itemArrived(item);
			return o != null?o:Orientations.Unknown;
		}
		
		//Do we now know the destination?
		if (!_router.hasRoute(item.getDestination())){
			return Orientations.Unknown;
		}
		//Which direction should we send it
		return _router.getExitFor(item.getDestination());
	}
}
