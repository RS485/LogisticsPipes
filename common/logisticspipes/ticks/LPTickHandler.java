package logisticspipes.ticks;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class LPTickHandler {
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		FluidIdentifier.initFromForge(true);
		SimpleServiceLocator.clientBufferHandler.clientTick(event);
		MainProxy.proxy.tickClient();
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		HudUpdateTick.tick();
		SimpleServiceLocator.craftingPermissionManager.tick();
		SimpleServiceLocator.serverBufferHandler.serverTick(event);
		MainProxy.proxy.tickServer();
	}
}
