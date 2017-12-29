package logisticspipes.ticks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.PacketInboundHandler;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.changedetection.LPWorldAccess;
import logisticspipes.utils.FluidIdentifier;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

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
		if(MainProxy.getClientMainWorld() != null) {
			for(PacketInboundHandler handler: LPTickHandler.packetHandler) {
				handler.tick(MainProxy.getClientMainWorld());
			}
		}
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

	private static List<PacketInboundHandler> packetHandler = Lists.newCopyOnWriteArrayList();
	private static Map<World, LPWorldInfo> worldInfo = new MapMaker().weakKeys().makeMap();

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {
		if (event.phase != Phase.END) {
			return;
		}
		if (event.side != Side.SERVER) {
			return;
		}
		for(PacketInboundHandler handler: LPTickHandler.packetHandler) {
			handler.tick(event.world);
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

	public static void registerPacketHandler(PacketInboundHandler handler) {
		packetHandler.add(handler);
	}

	@Data
	public static class LPWorldInfo {

		@Getter
		@Setter(value = AccessLevel.PRIVATE)
		private long worldTick = 0;
		@Getter
		private Set<DoubleCoordinates> updateQueued = new HashSet<>();
	}
}
