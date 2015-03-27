package logisticspipes.proxy.buildcraft.robots;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection.ConnectionInformation;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.robots.DockingStation;
import buildcraft.robots.RobotStationPluggable;
import buildcraft.transport.TileGenericPipe;

public class LPRobotConnectionControl implements ISpecialPipedConnection {
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	public boolean isType(IPipeInformationProvider startPipe) {
		if(!(startPipe instanceof LogisticsTileGenericPipe)) return false;
		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) startPipe;
		
		return true;
	}

	@Override
	public List<ConnectionInformation> getConnections(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connection, ForgeDirection side) {
		List<ConnectionInformation> list = new ArrayList<ConnectionInformation>();
		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) startPipe;
		if(BuildCraftProxy.availableRobots.get(pipe.getWorldObj()) == null) return list;
		LPPosition pos = new LPPosition(startPipe);
		pos.center();
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable pluggable = ((TileGenericPipe)pipe.tilePart.getOriginal()).getPipePluggable(dir);
			if(!(pluggable instanceof RobotStationPluggable)) continue;
			DockingStation station = ((RobotStationPluggable)pluggable).getStation();
			if(!station.isTaken()) continue;
			EntityRobotBase robot = station.robotTaking();
			if(robot == null) continue;
			if(!(robot.getBoard() instanceof LogisticsRoutingBoardRobot)) continue;
			if(robot.isDead) continue;
			if(!((LogisticsRoutingBoardRobot)robot.getBoard()).isAcceptsItems()) continue;
			LPPosition robotPos = new LPPosition(robot);
			if(((LogisticsRoutingBoardRobot)robot.getBoard()).getCurrentTarget() != null) {
				Pair<Double, LogisticsRoutingBoardRobot> currentTarget = ((LogisticsRoutingBoardRobot)robot.getBoard()).getCurrentTarget();
				LPPosition pipePos = currentTarget.getValue2().getLinkedStationPosition();
				TileEntity connectedPipeTile = pipePos.getTileEntity(pipe.getWorldObj());
				if(!(connectedPipeTile instanceof LogisticsTileGenericPipe)) continue;
				LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
				if(!connectedPipe.isRoutingPipe()) continue;
				IPipeInformationProvider connectedInfo = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(connectedPipe);
				EntityRobotBase connectedRobot = currentTarget.getValue2().robot;
				if(connectedRobot == null) continue;
				if(!(connectedRobot.getBoard() instanceof LogisticsRoutingBoardRobot)) continue;
				if(connectedRobot.isDead) continue;
				if(!((LogisticsRoutingBoardRobot)connectedRobot.getBoard()).isAcceptsItems()) continue;
				LPPosition connectedRobotPos = new LPPosition(connectedRobot);
				if(pipePos.copy().center().moveForward(currentTarget.getValue2().robot.getLinkedStation().side(), 0.5).distanceTo(connectedRobotPos) > 0.05) continue; // Not at station
				EnumSet<PipeRoutingConnectionType> newCon = connection.clone();
				newCon.removeAll(EnumSet.of(PipeRoutingConnectionType.canPowerFrom, PipeRoutingConnectionType.canPowerSubSystemFrom));
				double distance = currentTarget.getValue2().getLinkedStationPosition().copy().center().moveForward(currentTarget.getValue2().robot.getLinkedStation().side(), 0.5).distanceTo(robotPos);
				list.add(new ConnectionInformation(connectedInfo, newCon, currentTarget.getValue2().robot.getLinkedStation().side().getOpposite(), dir, (distance * 3) + 21));
			} else {
				if(pos.copy().moveForward(dir, 0.5).distanceTo(robotPos) > 0.05) continue; // Not at station
				for(Pair<LPPosition, ForgeDirection> canidatePos: BuildCraftProxy.availableRobots.get(pipe.getWorldObj())) {
					if(canidatePos.getValue1().equals(new LPPosition(startPipe))) continue;
					double distance = canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(robotPos);
					if(distance < 64) {
						TileEntity connectedPipeTile = canidatePos.getValue1().getTileEntity(pipe.getWorldObj());
						if(!(connectedPipeTile instanceof LogisticsTileGenericPipe)) continue;
						LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
						if(!connectedPipe.isRoutingPipe()) continue;
						IPipeInformationProvider connectedInfo = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(connectedPipe);
						PipePluggable connectedPluggable = ((TileGenericPipe)connectedPipe.tilePart.getOriginal()).getPipePluggable(canidatePos.getValue2());
						if(!(connectedPluggable instanceof RobotStationPluggable)) continue;
						DockingStation connectedStation = ((RobotStationPluggable)connectedPluggable).getStation();
						if(!connectedStation.isTaken()) continue;
						EntityRobotBase connectedRobot = connectedStation.robotTaking();
						if(connectedRobot == null) continue;
						if(!(connectedRobot.getBoard() instanceof LogisticsRoutingBoardRobot)) continue;
						if(connectedRobot.isDead) continue;
						if(!((LogisticsRoutingBoardRobot)connectedRobot.getBoard()).isAcceptsItems()) continue;
						if(((LogisticsRoutingBoardRobot)connectedRobot.getBoard()).getCurrentTarget() != null && ((LogisticsRoutingBoardRobot)connectedRobot.getBoard()).getCurrentTarget().getValue2() != robot.getBoard()) continue;
						LPPosition connectedRobotPos = new LPPosition(connectedRobot);
						if(canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(connectedRobotPos) > 0.05) continue; // Not at station
						EnumSet<PipeRoutingConnectionType> newCon = connection.clone();
						newCon.removeAll(EnumSet.of(PipeRoutingConnectionType.canPowerFrom, PipeRoutingConnectionType.canPowerSubSystemFrom));
						list.add(new ConnectionInformation(connectedInfo, newCon, canidatePos.getValue2().getOpposite(), dir, (distance * 3) + 21));
					}
				}
			}
		}
		return list;
	}
	
}
