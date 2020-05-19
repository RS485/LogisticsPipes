package logisticspipes.network.packets.pipe;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import lombok.AllArgsConstructor;
import lombok.Data;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class RequestRoutingLasersPacket extends CoordinatesPacket {

	private abstract static class Log {

		abstract void log(String log);
	}

	@Data
	@AllArgsConstructor
	private static class DataEntry {

		final LogisticsTileGenericPipe pipe;
		final EnumFacing dir;
		final ArrayList<ExitRoute> connectedRouters;
		final List<LaserData> lasers;
		final EnumSet<PipeRoutingConnectionType> connectionType;
		final Log log;
	}

	private boolean firstPipe = false;

	public RequestRoutingLasersPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.world);
		if (tile == null) {
			return;
		}
		if (tile.pipe instanceof CoreRoutedPipe) {
			IRouter router = ((CoreRoutedPipe) tile.pipe).getRouter();

			//this is here to allow players to manually trigger a network-wide LSA update
			router.forceLsaUpdate();

			List<List<ExitRoute>> exits = router.getRouteTable();
			HashMap<EnumFacing, ArrayList<ExitRoute>> routers = new HashMap<>();
			for (List<ExitRoute> exit : exits) {
				if (exit == null) {
					continue;
				}
				for (ExitRoute e : exit) {
					if (!routers.containsKey(e.exitOrientation)) {
						routers.put(e.exitOrientation, new ArrayList<>());
					}
					if (!routers.get(e.exitOrientation).contains(e)) {
						routers.get(e.exitOrientation).add(e);
					}
				}
			}
			ArrayList<LaserData> lasers = new ArrayList<>();
			firstPipe = true;
			for (final EnumFacing dir : routers.keySet()) {
				if (dir == null) {
					continue;
				}
				handleRouteInDirection(tile, dir, routers.get(dir), lasers, EnumSet.allOf(PipeRoutingConnectionType.class), new Log() {

					@Override
					void log(String log) {
						if (LogisticsPipes.isDEBUG()) {
							System.out.println(dir.name() + ": " + log);
						}
					}
				});
			}
			lasers = compressLasers(lasers);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RoutingLaserPacket.class).setLasers(lasers), player);
		}
	}

	private void handleRouteInDirection(final LogisticsTileGenericPipe pipeIn, EnumFacing dirIn, ArrayList<ExitRoute> connectedRoutersIn, final List<LaserData> lasersIn, EnumSet<PipeRoutingConnectionType> connectionTypeIn, final Log logIn) {
		List<DataEntry> worklist = new LinkedList<>();
		worklist.add(new DataEntry(pipeIn, dirIn, connectedRoutersIn, lasersIn, connectionTypeIn, logIn));
		while (!worklist.isEmpty()) {
			final DataEntry entry = worklist.remove(0);
			final LogisticsTileGenericPipe pipe = entry.pipe;
			final EnumFacing dir = entry.dir;
			final ArrayList<ExitRoute> connectedRouters = entry.connectedRouters;
			final List<LaserData> lasers = entry.lasers;
			final EnumSet<PipeRoutingConnectionType> connectionType = entry.connectionType;
			final Log log = entry.log;
			if (LogisticsPipes.isDEBUG()) {
				log.log("Size: " + connectedRouters.size());
			}
			lasers.add(new LaserData(pipe.getX(), pipe.getY(), pipe.getZ(), dir, connectionType).setStartPipe(firstPipe));
			firstPipe = false;
			HashMap<CoreRoutedPipe, ExitRoute> map = PathFinder.paintAndgetConnectedRoutingPipes(pipe, dir, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, (world, laser) -> {
				if (pipe.getWorld() == world) {
					lasers.add(laser);
				}
			}, connectionType);
			for (CoreRoutedPipe connectedPipe : map.keySet()) {
				IRouter newRouter = connectedPipe.getRouter();
				Iterator<ExitRoute> iRoutes = connectedRouters.iterator();
				while (iRoutes.hasNext()) {
					ExitRoute route = iRoutes.next();
					if (route.destination == newRouter) {
						iRoutes.remove();
					}
				}
			}
			Map<CoreRoutedPipe, ArrayList<ExitRoute>> sort = new HashMap<>();
			for (ExitRoute routeTo : connectedRouters) {
				ExitRoute result = null;
				CoreRoutedPipe resultPipe = null;
				for (Entry<CoreRoutedPipe, ExitRoute> routeCanidate : map.entrySet()) {
					List<ExitRoute> distances = routeCanidate.getValue().destination.getDistanceTo(routeTo.destination);
					for (ExitRoute distance : distances) {
						if (distance.isSameWay(routeTo)) {
							if (result == null || result.distanceToDestination > distance.distanceToDestination) {
								result = distance;
								resultPipe = routeCanidate.getKey();
							}
						}
					}
				}
				if (result == null) {
					continue;
				}
				if (!sort.containsKey(resultPipe)) {
					sort.put(resultPipe, new ArrayList<>());
				}
				if (!sort.get(resultPipe).contains(result)) {
					sort.get(resultPipe).add(result);
				}
			}

			for (Entry<CoreRoutedPipe, ArrayList<ExitRoute>> connectedPipe : sort.entrySet()) {
				HashMap<EnumFacing, ArrayList<ExitRoute>> routers = new HashMap<>();
				for (ExitRoute exit : connectedPipe.getValue()) {
					if (!routers.containsKey(exit.exitOrientation)) {
						routers.put(exit.exitOrientation, new ArrayList<>());
					}
					if (!routers.get(exit.exitOrientation).contains(exit)) {
						routers.get(exit.exitOrientation).add(exit);
					}
				}
				for (final EnumFacing exitDir : routers.keySet()) {
					if (exitDir == null) {
						continue;
					}
					worklist.add(new DataEntry(connectedPipe.getKey().container, exitDir, routers.get(exitDir), lasers, map.get(connectedPipe.getKey()).connectionDetails, new Log() {

						@Override
						void log(String logString) {
							if (LogisticsPipes.isDEBUG()) {
								log.log(exitDir.name() + ": " + logString);
							}
						}
					}));
				}
			}
		}
	}

	private ArrayList<LaserData> compressLasers(ArrayList<LaserData> lasers) {
		ArrayList<LaserData> options = new ArrayList<>(lasers);
		Iterator<LaserData> iLasers = lasers.iterator();
		while (iLasers.hasNext()) {
			boolean compressed = false;
			LaserData data = iLasers.next();
			BlockPos next = new BlockPos(data.getPosX(), data.getPosY(), data.getPosZ()).offset(data.getDir(), data.getLength());
			boolean found;
			do {
				found = false;
				Iterator<LaserData> iOptions = options.iterator();
				while (iOptions.hasNext()) {
					LaserData d = iOptions.next();
					if (d.getPosX() == next.getX() && d.getPosY() == next.getY() && d.getPosZ() == next.getZ()) {
						if (data.getDir().equals(d.getDir()) && data.getConnectionType().equals(d.getConnectionType())) {
							data.setLength(data.getLength() + d.getLength());
							next = next.offset(data.getDir(), data.getLength());
							found = true;
							iOptions.remove();
							lasers.remove(d);
							compressed = true;
						} else if (data.getDir().equals(d.getDir())) {
							data.setFinalPipe(false);
						}
					}
				}
			} while (found);
			if (compressed) {
				iLasers = lasers.iterator();
			}
		}
		return lasers;
	}

	@Override
	public ModernPacket template() {
		return new RequestRoutingLasersPacket(getId());
	}
}
