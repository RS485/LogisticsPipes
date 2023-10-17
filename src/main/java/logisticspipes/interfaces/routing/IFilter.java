package logisticspipes.interfaces.routing;

import logisticspipes.request.resources.IResource;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface IFilter {

	boolean isBlocked();

	boolean isFilteredItem(ItemIdentifier item);

	boolean isFilteredItem(IResource resultItem);

	boolean blockProvider();

	boolean blockCrafting();

	boolean blockRouting();

	boolean blockPower();

	DoubleCoordinates getLPPosition();
}
