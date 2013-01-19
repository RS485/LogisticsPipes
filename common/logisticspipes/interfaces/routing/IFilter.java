package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.utils.ItemIdentifier;

public interface IFilter extends IRelayItem {
	boolean isBlocked();
	List<ItemIdentifier> getFilteredItems();
	boolean blockProvider();
	boolean blockCrafting();
}
