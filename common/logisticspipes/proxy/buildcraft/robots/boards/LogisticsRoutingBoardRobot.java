package logisticspipes.proxy.buildcraft.robots.boards;

import java.util.HashSet;
import java.util.Set;

import cofh.api.energy.IEnergyStorage;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.robots.DockingStation;
import buildcraft.robots.RobotStationPluggable;
import buildcraft.robots.ai.AIRobotGotoBlock;
import buildcraft.robots.ai.AIRobotGotoStation;
import buildcraft.robots.ai.AIRobotStraightMoveTo;
import buildcraft.transport.TileGenericPipe;

public class LogisticsRoutingBoardRobot extends RedstoneBoardRobot {
	
	@Getter
	private boolean acceptsItems = true;
	private boolean init = false;
	private Set<LPTravelingItemServer> items = new HashSet<LPTravelingItemServer>();
	private IDockingStation targetStation;
	private int ticksWithContent = 0;
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
		if(init) return;
		init = true;
		IDockingStation dock = robot.getDockingStation();
		LPPosition pos = new LPPosition(dock.x(), dock.y(), dock.z());
		if(BuildCraftProxy.availableRobots.get(this.robot.worldObj) == null) {
			BuildCraftProxy.availableRobots.put(this.robot.worldObj, new HashSet<Pair<LPPosition,ForgeDirection>>());
		}
		BuildCraftProxy.availableRobots.get(this.robot.worldObj).add(new Pair<LPPosition, ForgeDirection>(pos, dock.side()));
	}
	
	@Override
	public void update() {
		index();
		if(robot.containsItems()) {
			ticksWithContent++;
			if(ticksWithContent > 20 * 2) {
				startTransport();
				ticksWithContent = 0;
			}
		} else {
			ticksWithContent = 0;
			acceptsItems = true;
			if(currentTarget != null) {
				startDelegateAI(new AIRobotGotoStation(robot, this.robot.getLinkedStation()));
				currentTarget = null;
				refreshRoutingTable();
			}
		}
		IEnergyStorage bat = this.robot.getBattery();
		int need = bat.getMaxEnergyStored() - bat.getEnergyStored();
		if(need <= 1000) return;
		need = bat.receiveEnergy(need, true);
		TileEntity tile = this.getLinkedStationPosition().getTileEntity(robot.worldObj);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).isRoutingPipe()) {
			CoreRoutedPipe pipe = ((LogisticsTileGenericPipe)tile).getRoutingPipe();
			boolean energyUsed = false;
			int count = 0;
			while(!energyUsed) {
				if(pipe.useEnergy((int)(need * 1.5D * LogisticsPowerJunctionTileEntity.RFDivisor))) {
					energyUsed = true;
				}
				if(count++ > 5) {
					break;
				}
			}
			if(energyUsed) {
				bat.receiveEnergy(need, false);
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		super.delegateAIEnded(ai);
		if(ai instanceof AIRobotGotoBlock) {
			if(!ai.success()) {
				dropAndClear();
				startDelegateAI(new AIRobotGotoStation(robot, this.robot.getLinkedStation()));
			} else {
				BlockIndex stationIndex = targetStation.index();
				ForgeDirection stationSide = targetStation.side();
				startDelegateAI(new AIRobotStraightMoveTo(robot,
						stationIndex.x + 0.5F + stationSide.offsetX * 0.5F,
						stationIndex.y + 0.5F + stationSide.offsetY * 0.5F,
						stationIndex.z + 0.5F + stationSide.offsetZ * 0.5F));
			}
		} else if(ai instanceof AIRobotStraightMoveTo) {
			if(!ai.success()) {
				dropAndClear();
				startDelegateAI(new AIRobotGotoStation(robot, this.robot.getLinkedStation()));
			} else {
				insertIntoPipe();
			}
		}
	}

	private Pair<Double, LogisticsRoutingBoardRobot> findTarget() {
		if(BuildCraftProxy.availableRobots.get(robot.worldObj) == null) return null;
		Pair<Double, LogisticsRoutingBoardRobot> result = null;
		LPPosition robotPos = new LPPosition(robot);
		for(Pair<LPPosition, ForgeDirection> canidatePos: BuildCraftProxy.availableRobots.get(robot.worldObj)) {
			if(this.robot.getLinkedStation() == null) continue;
			if(canidatePos.getValue1().equals(new LPPosition(this.robot.getLinkedStation().x(), this.robot.getLinkedStation().y(), this.robot.getLinkedStation().z()))) continue;
			double distance = canidatePos.getValue1().copy().center().moveForward(canidatePos.getValue2(), 0.5).distanceTo(robotPos);
			if(distance < 64 && (result == null || result.getValue1() > distance)) {
				TileEntity connectedPipeTile = canidatePos.getValue1().getTileEntity(robot.worldObj);
				if(!(connectedPipeTile instanceof LogisticsTileGenericPipe)) continue;
				LogisticsTileGenericPipe connectedPipe = (LogisticsTileGenericPipe) connectedPipeTile;
				if(!connectedPipe.isRoutingPipe()) continue;
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
				Double mindis = Double.NaN;
				for(LPTravelingItemServer item: items) {
					item.checkIDFromUUID();
					if(item.getInfo().destinationint < 0) continue;
					ExitRoute route = connectedPipe.getRoutingPipe().getRouter().getExitFor(item.getInfo().destinationint, item.getInfo()._transportMode == TransportMode.Active, item.getItemIdentifierStack().getItem());
					if(route == null) continue;
					if(mindis.isNaN()) {
						mindis = route.distanceToDestination;
					}
					mindis = Math.min(mindis, route.distanceToDestination);
				}
				if(mindis.isNaN()) continue;
				double distanceToItem = ((distance * 3) + 21) + mindis;
				if(result == null || result.getValue1() > distanceToItem) {
					result = new Pair<Double, LogisticsRoutingBoardRobot>(distanceToItem, (LogisticsRoutingBoardRobot)connectedRobot.getBoard());
				}
			}
		}
		return result;
	}
	
	private void startTransport() {
		if(currentTarget == null) {
			currentTarget = findTarget();
		}
		if(currentTarget != null) {
			IDockingStation station1 = this.robot.getDockingStation();
			IDockingStation station2 = currentTarget.getValue2().robot.getDockingStation();
			if(station1 == null) station1 = this.robot.getLinkedStation();
			if(station2 == null) station2 = currentTarget.getValue2().robot.getLinkedStation();
			startTransport(currentTarget.getValue2(), station2);
			currentTarget.getValue2().startTransport(this, station1);
		} else {
			dropAndClear();
		}
	}
	
	private void dropAndClear() {
		for(LPTravelingItemServer item: items) {
			item.itemWasLost();
			this.robot.worldObj.spawnEntityInWorld(item.getItemIdentifierStack().makeEntityItem(this.robot.worldObj, this.robot.posX, this.robot.posY, this.robot.posZ));
		}
		items.clear();
		for(int i=0;i<this.robot.getSizeInventory();i++) {
			this.robot.setInventorySlotContents(i, null);
		}
	}

	private void startTransport(LogisticsRoutingBoardRobot target, IDockingStation station) {
		acceptsItems = false;
		targetStation = station;
		startDelegateAI(new AIRobotGotoBlock(robot,
				station.x() + station.side().offsetX,
				station.y() + station.side().offsetY,
				station.z() + station.side().offsetZ));
	}

	private void insertIntoPipe() {
		LPPosition pos = new LPPosition(targetStation.x(), targetStation.y(), targetStation.z());
		TileEntity tile = pos.getTileEntity(this.robot.worldObj);
		if(tile instanceof LogisticsTileGenericPipe) {
			for(LPTravelingItemServer item: items) {
				LPTravelingItem.clientSideKnownIDs.set(item.getId(), false);
				((LogisticsTileGenericPipe)tile).pipe.transport.injectItem(item, targetStation.side().getOpposite());
			}
			items.clear();
			for(int i=0;i<this.robot.getSizeInventory();i++) {
				this.robot.setInventorySlotContents(i, null);
			}
		} else {
			dropAndClear();
		}
		startDelegateAI(new AIRobotGotoStation(robot, this.robot.getLinkedStation()));
		targetStation = null;
	}
	
	public LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem) {
		if(this.robot.isDead) {
			return arrivingItem;
		}
		ITransactor trans = InventoryHelper.getTransactorFor(robot, ForgeDirection.UNKNOWN);
		ItemStack inserted = trans.add(arrivingItem.getItemIdentifierStack().makeNormalStack(), ForgeDirection.UNKNOWN, false);
		if(inserted.stackSize != arrivingItem.getItemIdentifierStack().getStackSize()) {
			//TODO: Split item up
			this.acceptsItems = false;
			startTransport();
			return arrivingItem;
		}
		inserted = trans.add(arrivingItem.getItemIdentifierStack().makeNormalStack(), ForgeDirection.UNKNOWN, true);
		if(inserted.stackSize != arrivingItem.getItemIdentifierStack().getStackSize()) throw new UnsupportedOperationException("" + trans);
		items.add(arrivingItem);
		if(currentTarget == null) {
			currentTarget = findTarget();
			refreshRoutingTable();
		}
		return null;
	}

	private void refreshRoutingTable() {
		TileEntity tile = this.getLinkedStationPosition().getTileEntity(robot.worldObj);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).isRoutingPipe()) {
			CoreRoutedPipe pipe = ((LogisticsTileGenericPipe)tile).getRoutingPipe();
			pipe.getRouter().update(true, pipe);
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);
		nbt.setInteger("LP_Item_Size", items.size());
		int count = 0;
		for(LPTravelingItemServer stack:items) {
			NBTTagCompound nbt_Sub = new NBTTagCompound();
			stack.writeToNBT(nbt_Sub);
			nbt.setTag("LP_Item_" + count++, nbt_Sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);
		items.clear();
		for(int i=0;i<nbt.getInteger("LP_Item_Size");i++) {
			if(nbt.hasKey("LP_Item_" + i)) {
				items.add(new LPTravelingItemServer(nbt.getCompoundTag("LP_Item_" + i)));
			}
		}
	}
	
	public LPPosition getLinkedStationPosition() {
		return new LPPosition(this.robot.getLinkedStation().x(), this.robot.getLinkedStation().y(), this.robot.getLinkedStation().z());
	}
}
