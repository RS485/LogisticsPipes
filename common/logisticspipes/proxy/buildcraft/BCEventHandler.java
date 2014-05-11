package logisticspipes.proxy.buildcraft;

import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEvent;
import buildcraft.transport.pipes.events.PipeEventItem.DropItem;

public class BCEventHandler {
	/*
	 * Called trough ASM from Pipe.handlePipeEvent();
	 */
	public static void handle(PipeEvent event, Pipe<?> pipe) {
		if(event instanceof DropItem) {
			if(pipe != null) {
				if(pipe.transport instanceof PipeTransportItems) {
					ItemRoutingInformation info = null;
					if(((DropItem)event).item instanceof LPRoutedBCTravelingItem) {
						info = ((LPRoutedBCTravelingItem)((DropItem)event).item).getRoutingInformation();
					} else {
						info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(((DropItem)event).item);
					}
					if(info != null) {
						LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
						lpItem.setContainer(pipe.container);
						lpItem.itemWasLost();
					}
				}
			}
		}
	}
}
