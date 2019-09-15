package logisticspipes.interfaces.routing;

import net.minecraft.world.World;

public interface ChannelManagerProvider {

	IChannelManager getChannelManager(World world);
}
