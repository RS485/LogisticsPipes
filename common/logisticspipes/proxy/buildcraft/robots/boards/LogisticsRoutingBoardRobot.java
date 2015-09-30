package logisticspipes.proxy.buildcraft.robots.boards;

import java.util.HashSet;
import java.util.Set;

import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.robots.LPRobotConnectionControl;
import logisticspipes.proxy.buildcraft.robots.LPRobotConnectionControl.RobotConnection;
import logisticspipes.proxy.buildcraft.robots.ai.ItemInsertionAIRobot;
import logisticspipes.routing.ExitRoute;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.robotics.ai.AIRobotGotoBlock;
import buildcraft.robotics.ai.AIRobotGotoStation;
import buildcraft.robotics.ai.AIRobotStraightMoveTo;
import buildcraft.transport.TileGenericPipe;
import cofh.api.energy.IEnergyStorage;
import lombok.Getter;

public class LogisticsRoutingBoardRobot extends RedstoneBoardRobot {

	@Getter
	private boolean acceptsItems = true;
	private boolean init = false;
	@Getter
	private Set<LPTravelingItemServer> items = new HashSet<LPTravelingItemServer>();
	private LPPosition targetStationPos;
	private ForgeDirection targetStationSide = ForgeDirection.UNKNOWN;

	private int ticksWithContent = 0;
	@Getter
	private RobotConnection connectionDetails = new RobotConnection();

	@Getter
	private Pair<Double, LogisticsRoutingBoardRobot> currentTarget = null;

	public LogisticsRoutingBoardRobot(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return LogisticsRoutingBoardRobotNBT.instance;
	}

	@Override
	public void start() {
		super.start();
		index();
	}

	private void index() {
		if (init) {
			return;
		}
		DockingStation dock = robot.getLinkedStation();
		if (dock == null) {
			return;
		}
		LPPosition pos = new LPPosition(dock.x(), dock.y(), dock.z());
		LPRobotConnectionControl.instance.addRobot(robot.worldObj, pos, dock.side());
		init = true;
	}

	@Override
	public void update() {
		lpUpdate();
	}

	public void lpUpdate() {
		index();
		if (robot.containsItems()) {
			ticksWithContent++;
			if (ticksWithContent > 20 * 2) {
				startTransport();
				ticksWithContent = 0;
			}
		} else {
			ticksWithContent = 0;
			acceptsItems = true;
			if (currentTarget != null) {
				startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
				currentTarget = null;
				refreshRoutingTable();
			} else if (robot.getDockingStation() == null) {
				startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
			}
		}
		IEnergyStorage bat = robot.getBattery();
		int need = bat.getMaxEnergyStored() - bat.getEnergyStored();
		if (need <= 1000) {
			return;
		}
		need = bat.receiveEnergy(need, true);
		TileEntity tile = getLinkedStationPosition().getTileEntity(robot.worldObj);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isRoutingPipe()) {
			CoreRoutedPipe pipe = ((LogisticsTileGenericPipe) tile).getRoutingPipe();
			boolean energyUsed = false;
			int count = 0;
			while (!energyUsed) {
				if (pipe.useEnergy((int) (need * 1.5D * LogisticsPowerJunctionTileEntity.RFDivisor))) {
					energyUsed = true;
				}
				if (count++ > 5) {
					break;
				}
			}
			if (energyUsed) {
				bat.receiveEnergy(need, false);
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		super.delegateAIEnded(ai);
		if (ai instanceof AIRobotGotoBlock) {
			if (!ai.success()) {
				dropAndClear();
				startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
			} else {
				startDelegateAI(new AIRobotStraightMoveTo(robot, (float) targetStationPos.getXD() + 0.5F + targetStationSide.offsetX * 0.5F, (float) targetStationPos.getYD() + 0.5F + targetStationSide.offsetY * 0.5F, (float) targetStationPos.getZD() + 0.5F + targetStationSide.offsetZ * 0.5F));
			}
		} else if (ai instanceof AIRobotStraightMoveTo) {
			if (!ai.success()) {
				dropAndClear();
				startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
			} else {
				insertIntoPipe();
			}
		} else if (ai instanceof ItemInsertionAIRobot) {
			for (int i = 0; i < robot.getSizeInventory(); i++) {
				robot.setInventorySlotContents(i, null);
			}
			if (!ai.success()) {
				dropAndClear();
			} else {
				items.clear();
			}
			startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
			targetStationPos = null;
		}
	}

	private void insertIntoPipe() {
		TileEntity tile = targetStationPos.getTileEntity(robot.worldObj);
		if (tile instanceof LogisticsTileGenericPipe) {
			startDelegateAI(new ItemInsertionAIRobot(robot, (LogisticsTileGenericPipe) tile, this, targetStationSide.getOpposite()));
		} else {
			dropAndClear();
			startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
			targetStationPos = null;
		}
	}

	private Pair<Double, LogisticsRoutingBoardRobot> findTarget() {
		Pair<Double, LogisticsRoutingBoardRobot> result = null;
		LPPosition robotPos = new LPPosition(robot);
		for (Pair<LPPosition, ForgeDirection> canidatePos : connectionDetails.localConnectedRobots) {
			if (robot.getLinkedStation() == null) {
				continue;
			}
			if (canidatePos.getValue1().equals(new LPPosition(robot.getLinkedStation().x(), robot.getLinkedStation().y(), robot.getLinkedStation().z()))) {
				continue;
			}
			double distance = canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(robotPos);
			if (result == null || result.getValue1() > distance) {
				TileEntity connectedPipeTile = canidatePos.getValue1().getTileEntity(robot.worldObj);
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
				Double mindis = Double.NaN;
				for (LPTravelingItemServer item : items) {
					item.checkIDFromUUID();
					if (item.getInfo().destinationint < 0) {
						continue;
					}
					ExitRoute route = connectedPipe.getRoutingPipe().getRouter().getExitFor(item.getInfo().destinationint, item.getInfo()._transportMode == TransportMode.Active, item.getItemIdentifierStack().getItem());
					if (route == null) {
						continue;
					}
					if (mindis.isNaN()) {
						mindis = route.distanceToDestination;
					}
					mindis = Math.min(mindis, route.distanceToDestination);
				}
				if (mindis.isNaN()) {
					continue;
				}
				double distanceToItem = ((distance * 3) + 21) + mindis;
				if (result == null || result.getValue1() > distanceToItem) {
					result = new Pair<Double, LogisticsRoutingBoardRobot>(distanceToItem, (LogisticsRoutingBoardRobot) connectedRobot.getBoard());
				}
			}
		}
		return result;
	}

	private void startTransport() {
		if (currentTarget == null) {
			currentTarget = findTarget();
		}
		if (currentTarget != null) {
			DockingStation station1 = robot.getDockingStation();
			DockingStation station2 = currentTarget.getValue2().robot.getDockingStation();
			if (station1 == null) {
				station1 = robot.getLinkedStation();
			}
			if (station2 == null) {
				station2 = currentTarget.getValue2().robot.getLinkedStation();
			}
			startTransport(currentTarget.getValue2(), station2);
			currentTarget.getValue2().startTransport(this, station1);
		} else {
			dropAndClear();
		}
	}

	private void dropAndClear() {
		for (LPTravelingItemServer item : items) {
			item.itemWasLost();
			robot.worldObj.spawnEntityInWorld(item.getItemIdentifierStack().makeEntityItem(robot.worldObj, robot.posX, robot.posY, robot.posZ));
		}
		items.clear();
		for (int i = 0; i < robot.getSizeInventory(); i++) {
			robot.setInventorySlotContents(i, null);
		}
	}

	private void startTransport(LogisticsRoutingBoardRobot target, DockingStation station) {
		acceptsItems = false;
		targetStationPos = new LPPosition(station.x(), station.y(), station.z());
		targetStationSide = station.side();
		startDelegateAI(new AIRobotGotoBlock(robot, station.x() + station.side().offsetX, station.y() + station.side().offsetY, station.z() + station.side().offsetZ));
	}

	public LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem) {
		if (robot.isDead) {
			return arrivingItem;
		}
		ITransactor trans = InventoryHelper.getTransactorFor(robot, ForgeDirection.UNKNOWN);
		ItemStack inserted = trans.add(arrivingItem.getItemIdentifierStack().makeNormalStack(), ForgeDirection.UNKNOWN, false);
		if (inserted.stackSize != arrivingItem.getItemIdentifierStack().getStackSize()) {
			acceptsItems = false;
			startTransport();
			return arrivingItem;
		}
		inserted = trans.add(arrivingItem.getItemIdentifierStack().makeNormalStack(), ForgeDirection.UNKNOWN, true);
		if (inserted.stackSize != arrivingItem.getItemIdentifierStack().getStackSize()) {
			throw new UnsupportedOperationException("" + trans);
		}
		items.add(arrivingItem);
		if (currentTarget == null) {
			currentTarget = findTarget();
			refreshRoutingTable();
		}
		ticksWithContent = 0;
		return null;
	}

	private void refreshRoutingTable() {
		TileEntity tile = getLinkedStationPosition().getTileEntity(robot.worldObj);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isRoutingPipe()) {
			CoreRoutedPipe pipe = ((LogisticsTileGenericPipe) tile).getRoutingPipe();
			pipe.getRouter().update(true, pipe);
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);
		nbt.setInteger("LP_Item_Size", items.size());
		int count = 0;
		for (LPTravelingItemServer stack : items) {
			NBTTagCompound nbt_Sub = new NBTTagCompound();
			stack.writeToNBT(nbt_Sub);
			nbt.setTag("LP_Item_" + count++, nbt_Sub);
		}
		if (targetStationPos != null) {
			targetStationPos.writeToNBT("targetStationPos_", nbt);
		}
		nbt.setByte("targetStationSide", (byte) targetStationSide.ordinal());
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);
		items.clear();
		for (int i = 0; i < nbt.getInteger("LP_Item_Size"); i++) {
			if (nbt.hasKey("LP_Item_" + i)) {
				items.add(new LPTravelingItemServer(nbt.getCompoundTag("LP_Item_" + i)));
			}
		}
		targetStationPos = LPPosition.readFromNBT("targetStationPos_", nbt);
		targetStationSide = ForgeDirection.getOrientation(nbt.getByte("targetStationSide"));
	}

	public LPPosition getLinkedStationPosition() {
		return new LPPosition(robot.getLinkedStation().x(), robot.getLinkedStation().y(), robot.getLinkedStation().z());
	}
}
