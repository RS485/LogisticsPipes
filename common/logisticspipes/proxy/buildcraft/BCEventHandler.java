package logisticspipes.proxy.buildcraft;

import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

import net.minecraft.tileentity.TileEntity;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEvent;
import buildcraft.transport.pipes.events.PipeEventItem.DropItem;

public class BCEventHandler {

	/*
	 * Called trough ASM from Pipe.handlePipeEvent();
	 */
	public static void handle(PipeEvent event) {
		if (event instanceof DropItem) {
			DropItem drop = (DropItem) event;
			TileEntity tile = drop.item.getContainer();
			if (tile instanceof IPipeTile) {
				IPipe pipe = ((IPipeTile) tile).getPipe();
				if (pipe instanceof Pipe<?>) {
					if (((Pipe<?>) pipe).transport instanceof PipeTransportItems) {
						ItemRoutingInformation info = null;
						if (((DropItem) event).item instanceof LPRoutedBCTravelingItem) {
							info = ((LPRoutedBCTravelingItem) ((DropItem) event).item).getRoutingInformation();
						} else {
							info = LPRoutedBCTravelingItem.restoreFromExtraNBTData(((DropItem) event).item);
						}
						if (info != null) {
							LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
							lpItem.setContainer(((Pipe<?>) pipe).container);
							lpItem.itemWasLost();
						}
					}
				}
			}
		}
	}
}
