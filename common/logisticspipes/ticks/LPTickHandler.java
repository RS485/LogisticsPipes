package logisticspipes.ticks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.World;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.MapMaker;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.changedetection.LPWorldAccess;
import logisticspipes.utils.FluidIdentifier;
import network.rs485.grow.ServerTickDispatcher;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPTickHandler {

	public static int adjChecksDone = 0;

	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			FluidIdentifier.initFromForge(true);
			SimpleServiceLocator.clientBufferHandler.clientTick();
			MainProxy.proxy.tickClient();
			DebugGuiController.instance().execClient();
		}
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			HudUpdateTick.tick();
			SimpleServiceLocator.serverBufferHandler.serverTick();
			MainProxy.proxy.tickServer();
			LPTickHandler.adjChecksDone = 0;
			DebugGuiController.instance().execServer();
			ServerTickDispatcher.INSTANCE.tick();
		}
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
			world.addEventListener(new LPWorldAccess(world, info));
		}
		return info;
	}

	@Data
	public static class LPWorldInfo {

		@Getter
		@Setter(value = AccessLevel.PRIVATE)
		private long worldTick = 0;
		@Getter
		private Set<DoubleCoordinates> updateQueued = new HashSet<>();

		@Getter
		@Setter
		private boolean skipBlockUpdateForWorld = false;
	}
}
