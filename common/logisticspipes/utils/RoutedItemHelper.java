package logisticspipes.utils;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifierStack;

public class RoutedItemHelper {

	public LPTravelingItemServer createNewTravelItem(@Nonnull ItemStack item) {
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
