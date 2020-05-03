/**
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;

/**
 * @author Krapht This class is responsible for resolving where incoming items
 * should go.
 */
public class RouteLayer {

	protected final @Nonnull IRouter _router;
	private final TransportLayer _transport;
	private final CoreRoutedPipe _pipe;

	public RouteLayer(@Nonnull IRouter router, TransportLayer transportLayer, CoreRoutedPipe pipe) {
		_router = router;
		_transport = transportLayer;
		_pipe = pipe;
	}

	public EnumFacing getOrientationForItem(IRoutedItem item, EnumFacing blocked) {

		item.checkIDFromUUID();
		//If a item has no destination, find one
		if (item.getDestination() < 0) {
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), false);
			_pipe.debug.log("No Destination, assigned new destination: (" + item.getInfo());
		}

		//If the destination is unknown / unroutable or it already arrived at its destination and somehow looped back
		if (item.getDestination() >= 0 && (!_router.hasRoute(item.getDestination(), item.getTransportMode() == TransportMode.Active, item.getItemIdentifierStack().getItem()) || item.getArrived())) {
			item = SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), false);
			_pipe.debug.log("Unreachable Destination, sssigned new destination: (" + item.getInfo());
		}

		item.checkIDFromUUID();
		//If we still have no destination or client side unroutable, drop it
		if (item.getDestination() < 0) {
			return null;
		}

		//Is the destination ourself? Deliver it
		if (item.getDestinationUUID().equals(_router.getId())) {

			_transport.handleItem(item);

			if (item.getDistanceTracker() != null) {
				item.getDistanceTracker().setCurrentDistanceToTarget(0);
				item.getDistanceTracker().setDestinationReached();
			}

			if (item.getTransportMode() != TransportMode.Active && !_transport.stillWantItem(item)) {
				return getOrientationForItem(SimpleServiceLocator.logisticsManager.assignDestinationFor(item, _router.getSimpleID(), true), null);
			}

			item.setDoNotBuffer(true);
			item.setArrived(true);
			return _transport.itemArrived(item, blocked);
		}

		//Do we now know the destination?
		if (!_router.hasRoute(item.getDestination(), item.getTransportMode() == TransportMode.Active, item.getItemIdentifierStack().getItem())) {
			return null;
		}

		//Which direction should we send it
		ExitRoute exit = _router.getExitFor(item.getDestination(), item.getTransportMode() == TransportMode.Active, item.getItemIdentifierStack().getItem());
		if (exit == null) {
			return null;
		}

		if (item.getDistanceTracker() != null) {
			item.getDistanceTracker().setCurrentDistanceToTarget(exit.blockDistance);
		}

		return exit.exitOrientation;
	}
}
