package logisticspipes.proxy.buildcraft.subproxies;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import buildcraft.robotics.DockingStationPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.robots.LPRobotConnectionControl;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.ReflectionHelper;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.pluggable.LensPluggable;
import lombok.Getter;
import lombok.SneakyThrows;

public class LPBCTileGenericPipe extends TileGenericPipe implements IBCTilePart {

	private final LPBCPipe bcPipe;
	private final LPBCPluggableState bcPlugState;
	private final LPBCPipeRenderState bcRenderState;
	@Getter
	private final LogisticsTileGenericPipe lpPipe;
	public Map<ForgeDirection, List<StatementSlot>> activeActions = new HashMap<ForgeDirection, List<StatementSlot>>();

	public LPBCTileGenericPipe(LPBCPipe pipe, LogisticsTileGenericPipe lpPipe) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.pipe = bcPipe = pipe;
		bcPipe.setTile(this);
		this.lpPipe = lpPipe;
		bcPlugState = new LPBCPluggableState();
		bcRenderState = new LPBCPipeRenderState();
		ReflectionHelper.setFinalField(TileGenericPipe.class, "pluggableState", this, bcPlugState);
		ReflectionHelper.setFinalField(TileGenericPipe.class, "renderState", this, bcRenderState);
	}

	@Override
	public void writeToNBT_LP(NBTTagCompound nbt) {
		NBTTagCompound bcNBT = new NBTTagCompound();

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			bcNBT.setByte(key, (byte) redstoneInputSide[i]);
		}

		pipe.writeToNBT(bcNBT);

		sideProperties.writeToNBT(bcNBT);
		nbt.setTag("BC_Pipe_NBT", bcNBT);
	}

	@Override
	public void readFromNBT_LP(NBTTagCompound nbt) {
		if (!nbt.hasKey("BC_Pipe_NBT")) {
			redstoneInput = 0;
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				final String key = "redstoneInputSide[" + i + "]";
				if (nbt.hasKey(key)) {
					redstoneInputSide[i] = nbt.getByte(key);

					if (redstoneInputSide[i] > redstoneInput) {
						redstoneInput = redstoneInputSide[i];
					}
				} else {
					redstoneInputSide[i] = 0;
				}
			}
			//Import PipePart
			pipe.readFromNBT(nbt);
			//Import TilePart
			pipeBound = true;
			sideProperties.readFromNBT(nbt);
			attachPluggables = true;
			return;
		}
		NBTTagCompound bcNBT = nbt.getCompoundTag("BC_Pipe_NBT");

		redstoneInput = 0;

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			if (bcNBT.hasKey(key)) {
				redstoneInputSide[i] = bcNBT.getByte(key);

				if (redstoneInputSide[i] > redstoneInput) {
					redstoneInput = redstoneInputSide[i];
				}
			} else {
				redstoneInputSide[i] = 0;
			}
		}

		pipeBound = true;

		pipe.readFromNBT(bcNBT);

		sideProperties.readFromNBT(bcNBT);
		attachPluggables = true;
	}

	@Override
	public void invalidate_LP() {
		invalidate();
	}

	@Override
	public void validate_LP() {
		validate();
	}

	@Override
	@SneakyThrows({ NoSuchFieldException.class, SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class, NoSuchMethodException.class, InvocationTargetException.class })
	public void updateEntity_LP() {
		//Make sure we still have the same TE values
		xCoord = lpPipe.xCoord;
		yCoord = lpPipe.yCoord;
		zCoord = lpPipe.zCoord;

		if (attachPluggables) {
			attachPluggables = false;
			// Attach callback
			PipePluggable[] pluggables = ReflectionHelper.getPrivateField(PipePluggable[].class, SideProperties.class, "pluggables", sideProperties);
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (pluggables[i] != null) {
					pipe.eventBus.registerHandler(pluggables[i]);
					pluggables[i].onAttachedPipe(this, ForgeDirection.getOrientation(i));
				}
			}
			notifyBlockChanged();
		}

		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}

		pipe.updateEntity();

		boolean recheckThisPipe = false;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable p = getPipePluggable(direction);
			if (p != null) {
				p.update(this, direction);

				//Check Gate for ActionChanges
				if (p instanceof GatePluggable && lpPipe.isRoutingPipe()) {
					if (!activeActions.containsKey(direction)) {
						activeActions.put(direction, new ArrayList<StatementSlot>());
					}
					if (!listEquals(activeActions.get(direction), pipe.gates[direction.ordinal()].activeActions)) {
						activeActions.get(direction).clear();
						activeActions.get(direction).addAll(pipe.gates[direction.ordinal()].activeActions);
						lpPipe.getRoutingPipe().triggerConnectionCheck();
						recheckThisPipe = true;
					}
				} else if (activeActions.containsKey(direction)) {
					activeActions.remove(direction);
				}

				if(p instanceof RobotStationPluggable) {
					if(((RobotStationPluggable)p).getStation() != null && ((RobotStationPluggable)p).getStation().robotTaking() != null && ((RobotStationPluggable)p).getStation().robotTaking().getBoard() instanceof LogisticsRoutingBoardRobot) {
						((RobotStationPluggable)p).getStation().robotTaking().getBoard().cycle();
					}
				}
			}
		}
		if (recheckThisPipe) {
			LPRobotConnectionControl.instance.checkAll(worldObj);
		}

		if (worldObj.isRemote) {
			if (resyncGateExpansions) {
				ReflectionHelper.invokePrivateMethod(Object.class, TileGenericPipe.class, this, "syncGateExpansions", new Class[] {}, new Object[] {});
			}

			return;
		}

		if (blockNeighborChange) {
			ReflectionHelper.invokePrivateMethod(Object.class, TileGenericPipe.class, this, "computeConnections", new Class[] {}, new Object[] {});
			pipe.onNeighborBlockChange(0);
			blockNeighborChange = false;
			refreshRenderState = true;
		}

		if (refreshRenderState) {
			refreshRenderState();
			refreshRenderState = false;
		}
	}

	private boolean listEquals(List<StatementSlot> list1, List<StatementSlot> list2) {
		ListIterator<StatementSlot> e1 = list1.listIterator();
		ListIterator<StatementSlot> e2 = list2.listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			StatementSlot o1 = e1.next();
			StatementSlot o2 = e2.next();
			if (!(o1 == null ? o2 == null : statementEquals(o1, o2))) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	private boolean statementEquals(StatementSlot slot1, StatementSlot slot2) {
		if (slot1.statement != slot2.statement || slot1.parameters.length != slot2.parameters.length) {
			return false;
		}
		for (int i = 0; i < slot1.parameters.length; i++) {
			IStatementParameter p1 = slot1.parameters[i];
			IStatementParameter p2 = slot2.parameters[i];
			if ((p1 != null && !(p1.equals(p2))) || (p1 == null && p2 != null)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IBCRenderState getBCRenderState() {
		return bcRenderState;
	}

	@Override
	public IBCPipePart getBCPipePart() {
		return bcPipe;
	}

	@Override
	public IBCPluggableState getBCPlugableState() {
		return bcPlugState;
	}

	@Override
	public void readOldRedStone(NBTTagCompound nbt) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			if (nbt.hasKey(key)) {
				redstoneInputSide[i] = nbt.getByte(key);

				if (redstoneInputSide[i] > redstoneInput) {
					redstoneInput = redstoneInputSide[i];
				}
			} else {
				redstoneInputSide[i] = 0;
			}
		}
	}

	@Override
	public IBCPipePluggable getBCPipePluggable(final ForgeDirection sideHit) {
		final PipePluggable plug = getPipePluggable(sideHit);
		if (plug == null) {
			return null;
		}
		return new IBCPipePluggable() {

			@Override
			public ItemStack[] getDropItems(LogisticsTileGenericPipe container) {
				return plug.getDropItems(container);
			}

			@Override
			public boolean isBlocking() {
				return plug.isBlocking(pipe.container, sideHit);
			}

			@Override
			public Object getOriginal() {
				return plug;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void renderPluggable(RenderBlocks renderblocks, ForgeDirection dir, int renderPass, int x, int y, int z) {
				if (plug.getRenderer() == null) {
					return;
				}
				plug.getRenderer().renderPluggable(renderblocks, bcPipe, dir, plug, FakeBlock.INSTANCE, renderPass, x, y, z);
			}

			@Override
			public boolean isAcceptingItems(LPTravelingItemServer arrivingItem) {
				if (plug instanceof RobotStationPluggable) {
					return true;
				}
				return false;
			}

			@Override
			public LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem) {
				DockingStation station = ((RobotStationPluggable) plug).getStation();
				if (!station.isTaken()) {
					return arrivingItem;
				}
				EntityRobotBase robot = station.robotTaking();
				if (!(robot.getBoard() instanceof LogisticsRoutingBoardRobot)) {
					return arrivingItem;
				}
				if (!((LogisticsRoutingBoardRobot) robot.getBoard()).isAcceptsItems()) {
					return arrivingItem;
				}
				LPPosition robotPos = new LPPosition(robot);
				if (new LPPosition(LPBCTileGenericPipe.this).center().moveForward(sideHit, 0.5).distanceTo(robotPos) > 0.05) {
					return arrivingItem; // Not at station
				}
				return ((LogisticsRoutingBoardRobot) robot.getBoard()).handleItem(arrivingItem);
			}
		};
	}

	@Override
	public void afterStateUpdated() {
		if (worldObj == null) {
			worldObj = lpPipe.getWorldObj();
		}
		this.afterStateUpdated((byte) 2);
	}

	@Override
	public Object getOriginal() {
		xCoord = lpPipe.xCoord;
		yCoord = lpPipe.yCoord;
		zCoord = lpPipe.zCoord;
		return this;
	}

	@Override
	public Block getBlock(ForgeDirection to) {
		return lpPipe.getBlock(to);
	}

	@Override
	public TileEntity getTile(ForgeDirection to) {
		return lpPipe.getTile(to);
	}

	@Override
	public void sendUpdateToClient() {
		super.sendUpdateToClient();
		lpPipe.sendUpdateToClient();
	}

	@Override
	public void setWorldObj_LP(World world) {
		setWorldObj(world);
		xCoord = lpPipe.xCoord;
		yCoord = lpPipe.yCoord;
		zCoord = lpPipe.zCoord;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.ITEM;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return lpPipe.isPipeConnected(with);
	}

	@Override
	public boolean setPluggable(ForgeDirection direction, PipePluggable pluggable, EntityPlayer player) {
		if (pluggable instanceof LensPluggable) {
			// Coloring fundamentally doesn't work on Logistics Pipes
			return false;
		}

		return super.setPluggable(direction, pluggable, player);
	}
}
