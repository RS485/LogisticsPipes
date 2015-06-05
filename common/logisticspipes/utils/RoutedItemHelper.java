package logisticspipes.utils;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.item.ItemStack;

public class RoutedItemHelper {

	public LPTravelingItemServer createNewTravelItem(ItemStack item) {
		return createNewTravelItem(ItemIdentifierStack.getFromStack(item));
	}

	public LPTravelingItemServer createNewTravelItem(ItemIdentifierStack item) {
		return new LPTravelingItemServer(item);
	}

	public LPTravelingItemServer createNewTravelItem(ItemRoutingInformation info) {
		return new LPTravelingItemServer(info);
	}

	public LPTravelingItemServer getServerTravelingItem(IRoutedItem item) {
		return (LPTravelingItemServer) item;
	}
}
