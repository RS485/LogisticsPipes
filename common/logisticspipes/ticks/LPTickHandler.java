package logisticspipes.ticks;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class LPTickHandler {
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		ItemIdentifier.tick();
		FluidIdentifier.initFromForge(true);
		if(LogisticsPipes.WATCHDOG) {
			Watchdog.tickClient();
		}
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		HudUpdateTick.tick();
		SimpleServiceLocator.craftingPermissionManager.tick();
		if(LogisticsPipes.WATCHDOG) {
			Watchdog.tickServer();
		}
	}
}
