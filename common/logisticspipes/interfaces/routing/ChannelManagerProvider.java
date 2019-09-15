package logisticspipes.interfaces.routing;

import net.minecraft.world.World;

import logisticspipes.routing.channels.ChannelManagerProviderImpl;

public interface ChannelManagerProvider {

	static ChannelManagerProvider getInstance() {
		return ChannelManagerProviderImpl.INSTANCE;
	}

	IChannelManager getChannelManager(World world);
}
