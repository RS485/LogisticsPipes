package logisticspipes.ticks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.MapMaker;
import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.changedetection.LPWorldAccess;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class LPTickHandler {

	public static int adjChecksDone = 0;

	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		FluidIdentifier.initFromForge(true);
		SimpleServiceLocator.clientBufferHandler.clientTick(event);
		MainProxy.proxy.tickClient();
		DebugGuiController.instance().execClient();
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		HudUpdateTick.tick();
		SimpleServiceLocator.craftingPermissionManager.tick();
		SimpleServiceLocator.serverBufferHandler.serverTick(event);
		MainProxy.proxy.tickServer();
		LPTickHandler.adjChecksDone = 0;
		DebugGuiController.instance().execServer();
	}

	private static Map<World, LPWorldInfo> worldInfo = new MapMaker().weakKeys().makeMap();

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {
		if (event.phase != Phase.END) {
			return;
		}
		if (event.side != Side.SERVER) {
			return;
		}
		LPWorldInfo info = LPTickHandler.getWorldInfo(event.world);
		info.worldTick++;
	}

	public static LPWorldInfo getWorldInfo(World world) {
		LPWorldInfo info = LPTickHandler.worldInfo.get(world);
		if (info == null) {
			info = new LPWorldInfo();
			LPTickHandler.worldInfo.put(world, info);
			world.addWorldAccess(new LPWorldAccess(world, info));
		}
		return info;
	}

	@Data
	public static class LPWorldInfo {

		@Getter
		@Setter(value = AccessLevel.PRIVATE)
		private long worldTick = 0;
		@Getter
		private Set<LPPosition> updateQueued = new HashSet<LPPosition>();
	}
}
