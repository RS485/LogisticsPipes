package logisticspipes.network.packets.abstracts;

import logisticspipes.network.NetworkConstants;
import buildcraft.core.network.BuildCraftPacket;

public abstract class ModernPacket<T extends ModernPacket<T>> extends BuildCraftPacket implements
		Comparable<ModernPacket<T>> {

	private final int id;

	public ModernPacket(int id) {
		channel = NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME;
		this.id = id;
	}

	public abstract T template();

	@Override
	public int getID() {
		return id;
	}

	@Override
	public int compareTo(ModernPacket<T> o) {
		return this.getClass().getSimpleName()
				.compareTo(o.getClass().getSimpleName());
	}
}