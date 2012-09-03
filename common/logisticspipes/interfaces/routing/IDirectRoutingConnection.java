package logisticspipes.interfaces.routing;

import java.util.UUID;

import logisticspipes.utils.ItemIdentifier;

public interface IDirectRoutingConnection {
	public int getConnectionResistance();
	public void addItem(ItemIdentifier item, UUID sourceId, UUID destinationId);
}
