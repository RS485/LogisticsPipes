package logisticspipes.proxy;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.Direction;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import lombok.Getter;

import logisticspipes.LPItems;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.routing.pathfinder.PipeInformationManager;

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
		IPipeInformationProvider fromInfo = PipeInformationManager.INSTANCE.getInformationProviderFor(from);
		IPipeInformationProvider toInfo = PipeInformationManager.INSTANCE.getInformationProviderFor(to);
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
