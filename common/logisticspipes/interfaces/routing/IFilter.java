package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.utils.ItemIdentifier;

public interface IFilter {
	boolean isBlocked();
	List<ItemIdentifier> getFilteredItems();
}
