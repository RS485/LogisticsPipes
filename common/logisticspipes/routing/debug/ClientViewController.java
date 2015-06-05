package logisticspipes.routing.debug;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import logisticspipes.utils.tuples.LPPosition;

public class ClientViewController implements IDebugHUDProvider {

	private static ClientViewController instance;

	private ClientViewController() {}

	private LPPosition mainPipe = null;
	private int tick = 0;
	private final List<LPPosition> canidates = new ArrayList<LPPosition>();
	private DebugWindow debugWindow;

	private List<IHeadUpDisplayRendererProvider> listHUD = new ArrayList<IHeadUpDisplayRendererProvider>();
	private HashMap<LPPosition, DebugInformation> HUDPositions = new HashMap<LPPosition, DebugInformation>();

	public static class DebugInformation {

		public boolean isNew = false;
		public int newIndex = -1;
		public List<Integer> positions = new ArrayList<Integer>();
		public List<ExitRoute> routes = new ArrayList<ExitRoute>();
		public EnumSet<PipeRoutingConnectionType> closedSet;
		public EnumMap<PipeRoutingConnectionType, List<List<LPPosition>>> filters;
		public EnumSet<PipeRoutingConnectionType> nextFlags;
	}

	public static ClientViewController instance() {
		if (ClientViewController.instance == null) {
			ClientViewController.instance = new ClientViewController();
		}
		return ClientViewController.instance;
	}

	private DebugInformation getDebugInformation(LPPosition pos) {
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
			PipeFXRenderHandler.spawnGenericParticle(Particles.WhiteParticle, mainPipe.getX(), mainPipe.getY(), mainPipe.getZ(), 1);
		}
		for (LPPosition pos : canidates) {
			PipeFXRenderHandler.spawnGenericParticle(Particles.OrangeParticle, pos.getX(), pos.getY(), pos.getZ(), 1);
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
		LPPosition pos = routingUpdateCanidatePipe.getExitRoute().destination.getLPPosition();
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
		ExitRoute[] e = routingUpdateDebugCanidateList.getMsg();
		int i = 0;
		for (ExitRoute exit : e) {
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
			LPPosition pos = exit.destination.getLPPosition();
			getDebugInformation(pos).routes.add(exit);
			getDebugInformation(pos).positions.add(i);
		}
		for (Entry<LPPosition, DebugInformation> entry : HUDPositions.entrySet()) {
			listHUD.add(new HUDRoutingTableDebugProvider(new HUDRoutingTableGeneralInfo(entry.getValue()), entry.getKey()));
		}
	}

	@Override
	public List<IHeadUpDisplayRendererProvider> getHUDs() {
		return listHUD;
	}
}
