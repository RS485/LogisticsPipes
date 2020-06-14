package logisticspipes.routing.channels;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.interfaces.routing.IChannelManagerProvider;

public class ChannelManagerProvider implements IChannelManagerProvider {

	private WeakReference<World> worldWeakReference = null;
	private ChannelManager channelManager = null;

	public ChannelManagerProvider() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public IChannelManager getChannelManager(@Nonnull World world) {
		if (worldWeakReference == null || worldWeakReference.get() == null || channelManager == null) {
			worldWeakReference = new WeakReference<>(world);
			if (channelManager != null) {
				channelManager.markDirty();
			}
			channelManager = new ChannelManager(world);
		}
		return channelManager;
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload worldEvent) {
		if (worldWeakReference != null) {
			if (worldWeakReference.get() == null || worldWeakReference.get() == worldEvent.getWorld()) {
				channelManager = null;
				worldWeakReference = null;
			}
		}
	}
}
