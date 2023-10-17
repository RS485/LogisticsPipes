package logisticspipes.interfaces.routing;

import net.minecraft.world.World;

public interface IChannelManagerProvider {

	IChannelManager getChannelManager(World world);
}
