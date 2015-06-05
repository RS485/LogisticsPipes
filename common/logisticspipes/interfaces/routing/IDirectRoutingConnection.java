package logisticspipes.interfaces.routing;

import logisticspipes.routing.ItemRoutingInformation;

public interface IDirectRoutingConnection {

	public int getConnectionResistance();

	public void addItem(ItemRoutingInformation info);
}
