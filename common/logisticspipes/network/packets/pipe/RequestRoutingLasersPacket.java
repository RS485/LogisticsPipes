package logisticspipes.network.packets.pipe;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LPConstants;
import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IPaintPath;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.AllArgsConstructor;
import lombok.Data;

public class RequestRoutingLasersPacket extends CoordinatesPacket {

	private abstract class Log {

		abstract void log(String log);
	}

	@Data
	@AllArgsConstructor
	private class DataEntry {

		final LogisticsTileGenericPipe pipe;
		final ForgeDirection dir;
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
		LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if (tile == null) {
			return;
		}
		if (tile.pipe instanceof CoreRoutedPipe) {
			IRouter router = ((CoreRoutedPipe) tile.pipe).getRouter();

			//this is here to allow players to manually trigger a network-wide LSA update
			router.forceLsaUpdate();

			List<List<ExitRoute>> exits = router.getRouteTable();
			HashMap<ForgeDirection, ArrayList<ExitRoute>> routers = new HashMap<ForgeDirection, ArrayList<ExitRoute>>();
			for (List<ExitRoute> exit : exits) {
				if (exit == null) {
					continue;
				}
				for (ExitRoute e : exit) {
					if (!routers.containsKey(e.exitOrientation)) {
						routers.put(e.exitOrientation, new ArrayList<ExitRoute>());
					}
					if (!routers.get(e.exitOrientation).contains(e)) {
						routers.get(e.exitOrientation).add(e);
					}
				}
			}
			ArrayList<LaserData> lasers = new ArrayList<LaserData>();
			firstPipe = true;
			for (final ForgeDirection dir : routers.keySet()) {
				if (dir == ForgeDirection.UNKNOWN) {
					continue;
				}
				handleRouteInDirection(tile, dir, routers.get(dir), lasers, EnumSet.allOf(PipeRoutingConnectionType.class), new Log() {

					@Override
					void log(String log) {
						if (LPConstants.DEBUG) {
							System.out.println(dir.name() + ": " + log);
						}
					}
				});
			}
			lasers = compressLasers(lasers);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RoutingLaserPacket.class).setLasers(lasers), player);
		}
	}

	private void handleRouteInDirection(final LogisticsTileGenericPipe pipeIn, ForgeDirection dirIn, ArrayList<ExitRoute> connectedRoutersIn, final List<LaserData> lasersIn, EnumSet<PipeRoutingConnectionType> connectionTypeIn, final Log logIn) {
		List<DataEntry> worklist = new LinkedList<DataEntry>();
		worklist.add(new DataEntry(pipeIn, dirIn, connectedRoutersIn, lasersIn, connectionTypeIn, logIn));
		while (!worklist.isEmpty()) {
			final DataEntry entry = worklist.remove(0);
			final LogisticsTileGenericPipe pipe = entry.pipe;
			final ForgeDirection dir = entry.dir;
			final ArrayList<ExitRoute> connectedRouters = entry.connectedRouters;
			final List<LaserData> lasers = entry.lasers;
			final EnumSet<PipeRoutingConnectionType> connectionType = entry.connectionType;
			final Log log = entry.log;
			if (LPConstants.DEBUG) {
				log.log("Size: " + connectedRouters.size());
			}
			lasers.add(new LaserData(pipe.xCoord, pipe.yCoord, pipe.zCoord, dir, connectionType).setStartPipe(firstPipe));
			firstPipe = false;
			HashMap<CoreRoutedPipe, ExitRoute> map = PathFinder.paintAndgetConnectedRoutingPipes(pipe, dir, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, new IPaintPath() {

				@Override
				public void addLaser(World worldObj, LaserData laser) {
					if (pipe.getWorld() == worldObj) {
						lasers.add(laser);
					}
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
			Map<CoreRoutedPipe, ArrayList<ExitRoute>> sort = new HashMap<CoreRoutedPipe, ArrayList<ExitRoute>>();
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
					sort.put(resultPipe, new ArrayList<ExitRoute>());
				}
				if (!sort.get(resultPipe).contains(result)) {
					sort.get(resultPipe).add(result);
				}
			}

			for (Entry<CoreRoutedPipe, ArrayList<ExitRoute>> connectedPipe : sort.entrySet()) {
				HashMap<ForgeDirection, ArrayList<ExitRoute>> routers = new HashMap<ForgeDirection, ArrayList<ExitRoute>>();
				for (ExitRoute exit : connectedPipe.getValue()) {
					if (!routers.containsKey(exit.exitOrientation)) {
						routers.put(exit.exitOrientation, new ArrayList<ExitRoute>());
					}
					if (!routers.get(exit.exitOrientation).contains(exit)) {
						routers.get(exit.exitOrientation).add(exit);
					}
				}
				for (final ForgeDirection exitDir : routers.keySet()) {
					if (exitDir == ForgeDirection.UNKNOWN) {
						continue;
					}
					worklist.add(new DataEntry(connectedPipe.getKey().container, exitDir, routers.get(exitDir), lasers, map.get(connectedPipe.getKey()).connectionDetails, new Log() {

						@Override
						void log(String logString) {
							if (LPConstants.DEBUG) {
								log.log(exitDir.name() + ": " + logString);
							}
						}
					}));
				}
			}
		}
	}

	private ArrayList<LaserData> compressLasers(ArrayList<LaserData> lasers) {
		ArrayList<LaserData> options = new ArrayList<LaserData>();
		options.addAll(lasers);
		Iterator<LaserData> iLasers = lasers.iterator();
		while (iLasers.hasNext()) {
			boolean compressed = false;
			LaserData data = iLasers.next();
			LPPosition next = new LPPosition(data.getPosX(), data.getPosY(), data.getPosZ());
			next.moveForward(data.getDir(), data.getLength());
			boolean found = false;
			do {
				found = false;
				Iterator<LaserData> iOptions = options.iterator();
				while (iOptions.hasNext()) {
					LaserData d = iOptions.next();
					if (d.getPosX() == next.getX() && d.getPosY() == next.getY() && d.getPosZ() == next.getZ()) {
						if (data.getDir().equals(d.getDir()) && data.getConnectionType().equals(d.getConnectionType())) {
							data.setLength(data.getLength() + d.getLength());
							next.moveForward(data.getDir(), d.getLength());
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
