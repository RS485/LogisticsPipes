package logisticspipes.interfaces.routing;

import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;

public interface IFilter {
	boolean isBlocked();
	boolean isFilteredItem(ItemIdentifier item);
	boolean blockProvider();
	boolean blockCrafting();
	boolean blockRouting();
	boolean blockPower();
	LPPosition getLPPosition();
}
