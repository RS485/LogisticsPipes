package logisticspipes.proxy.specialconnection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.common.util.ForgeDirection;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection.ConnectionInformation;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;

/**
 * Support for teleport pipes
 **/
public class TeleportPipes implements ISpecialPipedConnection {

	private static Class<?> PipeItemTeleport;
	private static Method teleportPipeMethod;
	private static Object teleportManager;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init() {
		try {
			try {
				TeleportPipes.PipeItemTeleport = Class.forName("buildcraft.additionalpipes.pipes.PipeItemTeleport");
			} catch (Exception e) {
				TeleportPipes.PipeItemTeleport = Class.forName("net.minecraft.src.buildcraft.additionalpipes.pipes.PipeItemTeleport");
			}
			TeleportPipes.teleportPipeMethod = TeleportPipes.PipeItemTeleport.getMethod("getConnectedPipes", boolean.class);
			LogisticsPipes.log.debug("Additional pipes detected, adding compatibility");
			return true;
		} catch (Exception e1) {
			try {
				TeleportPipes.PipeItemTeleport = Class.forName("buildcraft.additionalpipes.pipes.PipeItemsTeleport");
				Class<?> tpmanager = Class.forName("buildcraft.additionalpipes.pipes.TeleportManager");
				TeleportPipes.teleportManager = tpmanager.getField("instance").get(null);
				TeleportPipes.teleportPipeMethod = tpmanager
						.getMethod("getConnectedPipes", Class.forName("buildcraft.additionalpipes.pipes.PipeTeleport"), boolean.class);
				LogisticsPipes.log.debug("Additional pipes detected, adding compatibility");
				return true;
			} catch (Exception e2) {
				LogisticsPipes.log.debug("Additional pipes not detected: " + e2.getMessage());
				return false;
			}
		}
	}

	@Override
	public boolean isType(IPipeInformationProvider tile) {
		if (SimpleServiceLocator.buildCraftProxy.isTileGenericPipe(tile.getTile())
				&& SimpleServiceLocator.buildCraftProxy.getPipeFromTGP(tile.getTile()) != null) {
			if (TeleportPipes.PipeItemTeleport.isAssignableFrom(
					SimpleServiceLocator.buildCraftProxy.getPipeFromTGP(tile.getTile()).getClass())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private LinkedList<?> getConnectedTeleportPipes(Object pipe) throws Exception {
		if (TeleportPipes.teleportManager != null) {
			return (LinkedList<?>) TeleportPipes.teleportPipeMethod.invoke(TeleportPipes.teleportManager, pipe, false);
		}
		return (LinkedList<?>) TeleportPipes.teleportPipeMethod.invoke(pipe, false);
	}

	@Override
	public List<ConnectionInformation> getConnections(IPipeInformationProvider tile, EnumSet<PipeRoutingConnectionType> connection, ForgeDirection side) {
		List<ConnectionInformation> list = new ArrayList<>();
		if (SimpleServiceLocator.buildCraftProxy.isTileGenericPipe(tile.getTile())
				&& SimpleServiceLocator.buildCraftProxy.getPipeFromTGP(tile.getTile()) != null) {
			try {
				LinkedList<?> pipes = getConnectedTeleportPipes(
						SimpleServiceLocator.buildCraftProxy.getPipeFromTGP(tile.getTile()));
				list.addAll(pipes.stream()
						.map(pipe -> new ConnectionInformation(SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(
								SimpleServiceLocator.buildCraftProxy.getTileFromPipe(pipe)), connection, side, ForgeDirection.UNKNOWN, 0))
						.collect(Collectors.toList()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
