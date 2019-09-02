package logisticspipes.interfaces;

import logisticspipes.routing.channels.ChannelInformation;

public interface IGUIChannelInformationReceiver {

	void handleChannelInformation(ChannelInformation channel, boolean flag);
}
