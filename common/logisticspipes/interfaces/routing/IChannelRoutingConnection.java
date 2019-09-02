package logisticspipes.interfaces.routing;

import logisticspipes.routing.ItemRoutingInformation;

public interface IChannelRoutingConnection {

	int getConnectionResistance();

	void addItem(ItemRoutingInformation info);
}
