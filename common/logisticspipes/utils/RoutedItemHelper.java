package logisticspipes.utils;

import net.minecraft.item.ItemStack;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemStack;

public class RoutedItemHelper {

	public static final RoutedItemHelper INSTANCE = new RoutedItemHelper();

	private RoutedItemHelper() {}

	public LPTravelingItemServer createNewTravelItem(ItemStack item) {
		return new LPTravelingItemServer(item);
	}

	public LPTravelingItemServer createNewTravelItem(ItemRoutingInformation info) {
		return new LPTravelingItemServer(info);
	}

	public LPTravelingItemServer getServerTravelingItem(IRoutedItem item) {
		return (LPTravelingItemServer) item;
	}
}
