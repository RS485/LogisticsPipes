package logisticspipes.interfaces.routing;

import logisticspipes.utils.ItemIdentifier;

public interface IFilter extends IRelayItem {
	boolean isBlocked();
	boolean isFilteredItem(ItemIdentifier item);
	boolean blockProvider();
	boolean blockCrafting();
	boolean blockRouting();
	boolean blockPower();
}
