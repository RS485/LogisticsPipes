package logisticspipes.interfaces.routing;

import logisticspipes.routing.ItemRoutingInformation;

public interface IChannelRoutingConnection {

	public int getConnectionResistance();

	public void addItem(ItemRoutingInformation info);
}
