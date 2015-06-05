package logisticspipes.proxy.buildcraft;

import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

import buildcraft.transport.pipes.events.PipeEventItem;

public class BCEventHandler {

	public void eventHandler(PipeEventItem.DropItem event) {
		ItemRoutingInformation info = null;

		if (event.item instanceof LPRoutedBCTravelingItem) {
			info = ((LPRoutedBCTravelingItem) event.item).getRoutingInformation();
		} else {
			info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(event.item);
		}

		if (info != null) {
			LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
			lpItem.setContainer(event.pipe.container);
			lpItem.itemWasLost();
		}
	}
}
