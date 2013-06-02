package logisticspipes.network.packets.abstracts;

import logisticspipes.network.NetworkConstants;
import buildcraft.core.network.BuildCraftPacket;

public abstract class ModernPacket extends BuildCraftPacket implements
		Comparable<ModernPacket> {

	private final int id;

	public ModernPacket(int id) {
		channel = NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME;
		this.id = id;
	}

	public abstract ModernPacket template();

	@Override
	public int getID() {
		return id;
	}

	@Override
	public int compareTo(ModernPacket o) {
		return this.getClass().getSimpleName()
				.compareTo(o.getClass().getSimpleName());
	}
}