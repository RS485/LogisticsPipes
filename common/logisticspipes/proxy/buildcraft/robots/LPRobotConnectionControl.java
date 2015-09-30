package logisticspipes.proxy.buildcraft.robots;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection.ConnectionInformation;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.transport.TileGenericPipe;

public class LPRobotConnectionControl implements ISpecialPipedConnection {

	public static class RobotConnection {

		public final Set<Pair<LPPosition, ForgeDirection>> localConnectedRobots = new HashSet<Pair<LPPosition, ForgeDirection>>();
	}

	private final Map<World, Set<Pair<LPPosition, ForgeDirection>>> globalAvailableRobots = new WeakHashMap<World, Set<Pair<LPPosition, ForgeDirection>>>();

	public void addRobot(World world, LPPosition pos, ForgeDirection dir) {
		if (globalAvailableRobots.get(world) == null) {
			globalAvailableRobots.put(world, new HashSet<Pair<LPPosition, ForgeDirection>>());
		}
		globalAvailableRobots.get(world).add(new Pair<LPPosition, ForgeDirection>(pos, dir));
		checkAll(world);
	}

	//TODO: Call this somewhere...
	public void removeRobot(World world, LPPosition pos, ForgeDirection dir) {
		if (globalAvailableRobots.containsKey(world)) {
			globalAvailableRobots.get(world).remove(new Pair<LPPosition, ForgeDirection>(pos, dir));
		}
		checkAll(world);
	}

	public void checkAll(World world) {
		if (!globalAvailableRobots.containsKey(world)) {
			return;
		}

		for (Pair<LPPosition, ForgeDirection> canidatePos : globalAvailableRobots.get(world)) {
			TileEntity connectedPipeTile = canidatePos.getValue1().getTileEntity(world);
			if (!(connectedPipeTile instanceof LogisticsTileGenericPipe)) {
				continue;
			}
			LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
			if (!connectedPipe.isRoutingPipe()) {
				continue;
			}
			PipePluggable connectedPluggable = ((TileGenericPipe) connectedPipe.tilePart.getOriginal()).getPipePluggable(canidatePos.getValue2());
			if (!(connectedPluggable instanceof RobotStationPluggable)) {
				continue;
			}
			DockingStation connectedStation = ((RobotStationPluggable) connectedPluggable).getStation();
			if (!connectedStation.isTaken()) {
				continue;
			}
			EntityRobotBase connectedRobot = connectedStation.robotTaking();
			if (connectedRobot == null) {
				continue;
			}
			if (!(connectedRobot.getBoard() instanceof LogisticsRoutingBoardRobot)) {
				continue;
			}
			LogisticsRoutingBoardRobot lpBoard = ((LogisticsRoutingBoardRobot) connectedRobot.getBoard());
			if (isModified(lpBoard)) {
				connectedPipe.getRoutingPipe().triggerConnectionCheck();
			}
		}
	}

	public boolean isModified(LogisticsRoutingBoardRobot board) {
		Set<Pair<LPPosition, ForgeDirection>> localConnectedRobots = new HashSet<Pair<LPPosition, ForgeDirection>>();
		LPPosition sourceRobotPosition = board.getLinkedStationPosition().center().moveForward(board.robot.getLinkedStation().side(), 0.5);
		IZone zone = board.robot.getZoneToWork();
		for (Pair<LPPosition, ForgeDirection> canidatePos : globalAvailableRobots.get(board.robot.worldObj)) {
			LPPosition canidateRobotPosition = canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5);
			double distance = canidateRobotPosition.distanceTo(sourceRobotPosition);
			boolean isPartOfZone;
			if (zone != null) {
				isPartOfZone = zone.contains(canidateRobotPosition.getXD(), canidateRobotPosition.getYD(), canidateRobotPosition.getZD());
			} else {
				isPartOfZone = distance < Configs.MAX_ROBOT_DISTANCE;
			}
			if (isPartOfZone) {
				localConnectedRobots.add(canidatePos);
			}
		}
		if (board.getConnectionDetails().localConnectedRobots.equals(localConnectedRobots)) {
			return false;
		} else {
			board.getConnectionDetails().localConnectedRobots.clear();
			board.getConnectionDetails().localConnectedRobots.addAll(localConnectedRobots);
			return true;
		}
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(IPipeInformationProvider startPipe) {
		if (!(startPipe instanceof LogisticsTileGenericPipe)) {
			return false;
		}
		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) startPipe;
		return pipe.isRoutingPipe();
	}

	@Override
	public List<ConnectionInformation> getConnections(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connection, ForgeDirection side) {
		List<ConnectionInformation> list = new ArrayList<ConnectionInformation>();
		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) startPipe;
		if (pipe == null || pipe.tilePart.getOriginal() == null) {
			return list; // Proxy got disabled
		}
		LPPosition pos = new LPPosition(startPipe);
		pos.center();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable pluggable = ((TileGenericPipe) pipe.tilePart.getOriginal()).getPipePluggable(dir);
			if (!(pluggable instanceof RobotStationPluggable)) {
				continue;
			}
			DockingStation station = ((RobotStationPluggable) pluggable).getStation();
			if (!station.isTaken()) {
				continue;
			}
			EntityRobotBase robot = station.robotTaking();
			if (robot == null) {
				continue;
			}
			if (!(robot.getBoard() instanceof LogisticsRoutingBoardRobot)) {
				continue;
			}
			if (robot.isDead) {
				continue;
			}
			if (!((LogisticsRoutingBoardRobot) robot.getBoard()).isAcceptsItems()) {
				continue;
			}
			LPPosition robotPos = new LPPosition(robot);
			if (((LogisticsRoutingBoardRobot) robot.getBoard()).getCurrentTarget() != null) {
				Pair<Double, LogisticsRoutingBoardRobot> currentTarget = ((LogisticsRoutingBoardRobot) robot.getBoard()).getCurrentTarget();
				LPPosition pipePos = currentTarget.getValue2().getLinkedStationPosition();
				TileEntity connectedPipeTile = pipePos.getTileEntity(pipe.getWorldObj());
				if (!(connectedPipeTile instanceof LogisticsTileGenericPipe)) {
					continue;
				}
				LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
				if (!connectedPipe.isRoutingPipe()) {
					continue;
				}
				IPipeInformationProvider connectedInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(connectedPipe);
				EntityRobotBase connectedRobot = currentTarget.getValue2().robot;
				if (connectedRobot == null) {
					continue;
				}
				if (!(connectedRobot.getBoard() instanceof LogisticsRoutingBoardRobot)) {
					continue;
				}
				if (connectedRobot.isDead) {
					continue;
				}
				if (connectedRobot.getZoneToWork() != null && !connectedRobot.getZoneToWork().contains(robotPos.getXD(), robotPos.getYD(), robotPos.getZD())) {
					continue;
				}
				if (!((LogisticsRoutingBoardRobot) connectedRobot.getBoard()).isAcceptsItems()) {
					continue;
				}
				LPPosition connectedRobotPos = new LPPosition(connectedRobot);
				if (pipePos.copy().center().moveForward(currentTarget.getValue2().robot.getLinkedStation().side(), 0.5).distanceTo(connectedRobotPos) > 0.05) {
					continue; // Not at station
				}
				EnumSet<PipeRoutingConnectionType> newCon = connection.clone();
				newCon.removeAll(EnumSet.of(PipeRoutingConnectionType.canPowerFrom, PipeRoutingConnectionType.canPowerSubSystemFrom));
				double distance = currentTarget.getValue2().getLinkedStationPosition().copy().center().moveForward(currentTarget.getValue2().robot.getLinkedStation().side(), 0.5).distanceTo(robotPos);
				list.add(new ConnectionInformation(connectedInfo, newCon, currentTarget.getValue2().robot.getLinkedStation().side().getOpposite(), dir, (distance * 3) + 21));
			} else {
				if (pos.copy().moveForward(dir, 0.5).distanceTo(robotPos) > 0.05) {
					continue; // Not at station
				}
				for (Pair<LPPosition, ForgeDirection> canidatePos : ((LogisticsRoutingBoardRobot) robot.getBoard()).getConnectionDetails().localConnectedRobots) {
					if (canidatePos.getValue1().equals(new LPPosition(startPipe))) {
						continue;
					}
					double distance = canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(robotPos);
					TileEntity connectedPipeTile = canidatePos.getValue1().getTileEntity(pipe.getWorldObj());
					if (!(connectedPipeTile instanceof LogisticsTileGenericPipe)) {
						continue;
					}
					LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
					if (!connectedPipe.isRoutingPipe()) {
						continue;
					}
					IPipeInformationProvider connectedInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(connectedPipe);
					PipePluggable connectedPluggable = ((TileGenericPipe) connectedPipe.tilePart.getOriginal()).getPipePluggable(canidatePos.getValue2());
					if (!(connectedPluggable instanceof RobotStationPluggable)) {
						continue;
					}
					DockingStation connectedStation = ((RobotStationPluggable) connectedPluggable).getStation();
					if (!connectedStation.isTaken()) {
						continue;
					}
					EntityRobotBase connectedRobot = connectedStation.robotTaking();
					if (connectedRobot == null) {
						continue;
					}
					if (!(connectedRobot.getBoard() instanceof LogisticsRoutingBoardRobot)) {
						continue;
					}
					if (connectedRobot.isDead) {
						continue;
					}
					if (connectedRobot.getZoneToWork() != null && !connectedRobot.getZoneToWork().contains(robotPos.getXD(), robotPos.getYD(), robotPos.getZD())) {
						continue;
					}
					if (!((LogisticsRoutingBoardRobot) connectedRobot.getBoard()).isAcceptsItems()) {
						continue;
					}
					if (((LogisticsRoutingBoardRobot) connectedRobot.getBoard()).getCurrentTarget() != null && ((LogisticsRoutingBoardRobot) connectedRobot.getBoard()).getCurrentTarget().getValue2() != robot.getBoard()) {
						continue;
					}
					LPPosition connectedRobotPos = new LPPosition(connectedRobot);
					if (canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(connectedRobotPos) > 0.05) {
						continue; // Not at station
					}
					EnumSet<PipeRoutingConnectionType> newCon = connection.clone();
					newCon.removeAll(EnumSet.of(PipeRoutingConnectionType.canPowerFrom, PipeRoutingConnectionType.canPowerSubSystemFrom));
					list.add(new ConnectionInformation(connectedInfo, newCon, canidatePos.getValue2().getOpposite(), dir, (distance * 3) + 21));
				}
			}
		}
		return list;
	}

	public void cleanup() {
		globalAvailableRobots.clear();
	}

	public static final LPRobotConnectionControl instance = new LPRobotConnectionControl();

	private LPRobotConnectionControl() {}

}
