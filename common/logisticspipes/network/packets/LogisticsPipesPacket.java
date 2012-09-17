package logisticspipes.network.packets;

import logisticspipes.network.NetworkConstants;
import buildcraft.core.network.BuildCraftPacket;

public abstract class LogisticsPipesPacket extends BuildCraftPacket {

	public LogisticsPipesPacket() {
		channel = NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME;
	}
}
