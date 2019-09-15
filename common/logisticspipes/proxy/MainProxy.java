package logisticspipes.proxy;

import java.util.Map;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.collect.Maps;
import lombok.Getter;

import logisticspipes.LPItems;
import logisticspipes.entity.FakePlayerLP;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.OrientationsUtil;

public class MainProxy {

	private MainProxy() {}

	@Getter
	private static int globalTick;

	public static void addTick() {
		MainProxy.globalTick++;
	}

	public static boolean checkPipesConnections(BlockEntity from, BlockEntity to, Direction way) {
		return MainProxy.checkPipesConnections(from, to, way, false);
	}

	public static boolean checkPipesConnections(BlockEntity from, BlockEntity to, Direction way, boolean ignoreSystemDisconnection) {
		if (from == null || to == null) {
			return false;
		}
		IPipeInformationProvider fromInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(from);
		IPipeInformationProvider toInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(to);
		if (fromInfo == null && toInfo == null) {
			return false;
		}
		if (fromInfo != null) {
			if (!fromInfo.canConnect(to, way, ignoreSystemDisconnection)) {
				return false;
			}
		}
		if (toInfo != null) {
			if (!toInfo.canConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPipeControllerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() == LPItems.pipeController;
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		fakePlayers.entrySet().removeIf(entry -> entry.getValue().world == event.getWorld());
	}
}
