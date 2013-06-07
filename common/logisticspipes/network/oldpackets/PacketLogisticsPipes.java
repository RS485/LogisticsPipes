package logisticspipes.network.oldpackets;

import logisticspipes.network.NetworkConstants;
import buildcraft.core.network.BuildCraftPacket;

public abstract class PacketLogisticsPipes extends BuildCraftPacket {

	public PacketLogisticsPipes() {
		channel = NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME;
	}
}
