package logisticspipes.routing.debug;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import logisticspipes.interfaces.IDebugHUDProvider;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.network.packets.routingdebug.RoutingUpdateCanidatePipe;
import logisticspipes.network.packets.routingdebug.RoutingUpdateDebugCanidateList;
import logisticspipes.network.packets.routingdebug.RoutingUpdateDebugClosedSet;
import logisticspipes.network.packets.routingdebug.RoutingUpdateDebugFilters;
import logisticspipes.network.packets.routingdebug.RoutingUpdateDoneDebug;
import logisticspipes.network.packets.routingdebug.RoutingUpdateInitDebug;
import logisticspipes.network.packets.routingdebug.RoutingUpdateSourcePipe;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class ClientViewController implements IDebugHUDProvider {

	private static ClientViewController instance;

	private ClientViewController() {}

	private DoubleCoordinates mainPipe = null;
	private int tick = 0;
	private final List<DoubleCoordinates> canidates = new ArrayList<>();
	private DebugWindow debugWindow;

	private List<IHeadUpDisplayRendererProvider> listHUD = new ArrayList<>();
	private HashMap<DoubleCoordinates, DebugInformation> HUDPositions = new HashMap<>();

	public static class DebugInformation {

		public boolean isNew = false;
		public int newIndex = -1;
		public List<Integer> positions = new ArrayList<>();
		public List<ExitRoute> routes = new ArrayList<>();
		public EnumSet<PipeRoutingConnectionType> closedSet;
		public EnumMap<PipeRoutingConnectionType, List<List<DoubleCoordinates>>> filters;
		public EnumSet<PipeRoutingConnectionType> nextFlags;
	}

	public static ClientViewController instance() {
		if (ClientViewController.instance == null) {
			ClientViewController.instance = new ClientViewController();
		}
		return ClientViewController.instance;
	}

	private DebugInformation getDebugInformation(DoubleCoordinates pos) {
		DebugInformation info = HUDPositions.get(pos);
		if (info == null) {
			info = new DebugInformation();
			HUDPositions.put(pos, info);
		}
		return info;
	}

	public void tick() {
		if (tick++ % 5 != 0) {
			return;
		}
		if (mainPipe != null) {
			PipeFXRenderHandler.spawnGenericParticle(Particles.WhiteParticle, mainPipe.getXInt(), mainPipe.getYInt(), mainPipe.getZInt(), 1);
		}
		for (DoubleCoordinates pos : canidates) {
			PipeFXRenderHandler.spawnGenericParticle(Particles.OrangeParticle, pos.getXInt(), pos.getYInt(), pos.getZInt(), 1);
		}
	}

	public void clear() {
		mainPipe = null;
		canidates.clear();
		listHUD.clear();
		HUDPositions.clear();
	}

	public void handlePacket(RoutingUpdateSourcePipe routingUpdateSourcePipe) {
		mainPipe = routingUpdateSourcePipe.getExitRoute().destination.getLPPosition();
		getDebugInformation(mainPipe).nextFlags = routingUpdateSourcePipe.getExitRoute().getFlags();
	}

	public void handlePacket(RoutingUpdateCanidatePipe routingUpdateCanidatePipe) {
		DoubleCoordinates pos = routingUpdateCanidatePipe.getExitRoute().destination.getLPPosition();
		canidates.add(routingUpdateCanidatePipe.getExitRoute().destination.getLPPosition());
		//listHUD.add(new HUDRoutingTableDebugProvider(new HUDRoutingTableNewCandateUntrace(routingUpdateCanidatePipe.getExitRoute()), pos));
		getDebugInformation(pos).isNew = true;
		getDebugInformation(pos).newIndex = routingUpdateCanidatePipe.getExitRoute().debug.index;
	}

	public void init(RoutingUpdateInitDebug routingUpdateInitDebug) {
		debugWindow = new DebugWindow("Debug Code", 500, 250);
		LogisticsHUDRenderer.instance().debugHUD = this;
	}

	public void done(RoutingUpdateDoneDebug routingUpdateDoneDebug) {
		if (debugWindow != null) {
			debugWindow.setVisible(false);
			debugWindow = null;
		}
		LogisticsHUDRenderer.instance().debugHUD = null;
		listHUD.clear();
		HUDPositions.clear();
	}

	public void handlePacket(RoutingUpdateDebugClosedSet routingUpdateDebugClosedSet) {
		getDebugInformation(routingUpdateDebugClosedSet.getPos()).closedSet = routingUpdateDebugClosedSet.getSet();
	}

	public void handlePacket(RoutingUpdateDebugFilters routingUpdateDebugFilters) {
		getDebugInformation(routingUpdateDebugFilters.getPos()).filters = routingUpdateDebugFilters.getFilterPositions();
	}

	public void updateList(RoutingUpdateDebugCanidateList routingUpdateDebugCanidateList) {
		debugWindow.clear();
		int i = 0;
		for (ExitRoute exit : routingUpdateDebugCanidateList.getExitRoutes()) {
			i++;
			Color color = Color.BLACK;
			if (exit.debug.isNewlyAddedCanidate) {
				color = Color.BLUE;
			}
			debugWindow.showInfo(exit.destination.toString(), color);
			debugWindow.showInfo("\n", color);
			for (int j = 0; j < 2; j++) {
				debugWindow.showInfo("\t", color);
			}
			debugWindow.showInfo(exit.debug.toStringNetwork, color);
			debugWindow.showInfo("\n", color);
			DoubleCoordinates pos = exit.destination.getLPPosition();
			getDebugInformation(pos).routes.add(exit);
			getDebugInformation(pos).positions.add(i);
		}
		listHUD.addAll(HUDPositions.entrySet().stream()
				.map(entry -> new HUDRoutingTableDebugProvider(new HUDRoutingTableGeneralInfo(entry.getValue()), entry.getKey()))
				.collect(Collectors.toList()));
	}

	@Override
	public List<IHeadUpDisplayRendererProvider> getHUDs() {
		return listHUD;
	}
}
