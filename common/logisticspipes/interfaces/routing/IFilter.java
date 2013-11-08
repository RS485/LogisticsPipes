package logisticspipes.interfaces.routing;

import logisticspipes.utils.item.ItemIdentifier;

public interface IFilter {
	boolean isBlocked();
	boolean isFilteredItem(ItemIdentifier item);
	boolean blockProvider();
	boolean blockCrafting();
	boolean blockRouting();
	boolean blockPower();
}
