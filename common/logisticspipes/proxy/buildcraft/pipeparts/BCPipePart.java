package logisticspipes.proxy.buildcraft.pipeparts;

import java.util.List;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.gates.wrapperclasses.PipeWrapper;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Gate;
import buildcraft.transport.PipeTransportStructure;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.GateFactory;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class BCPipePart implements IBCPipePart {

	public LogisticsTileGenericPipe container;
	public int[] signalStrength = new int[]{0, 0, 0, 0};
	public boolean[] wireSet = new boolean[]{false, false, false, false};
	public Gate gate;
	
	public PipeWrapper wrapper;

	public BCPipePart(LogisticsTileGenericPipe tile) {
		this.container = tile;
		wrapper = new PipeWrapper(tile);
	}

	@Override
	public void updateGate() {
		// Update the gate if we have any
		if (gate != null) {
			if (container.getWorldObj().isRemote) {
				// on client, only update the graphical pulse if needed
				gate.updatePulse();
			} else {
				// on server, do the internal gate update
				gate.resolveActions();
				gate.tick();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		// Save gate if any
		if (gate != null) {
			NBTTagCompound gateNBT = new NBTTagCompound();
			gate.writeToNBT(gateNBT);
			data.setTag("Gate", gateNBT);
		}

		for (int i = 0; i < 4; ++i) {
			data.setBoolean("wireSet[" + i + "]", wireSet[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		// Load gate if any
		if (data.hasKey("Gate")) {
			NBTTagCompound gateNBT = data.getCompoundTag("Gate");
			gate = GateFactory.makeGate(wrapper, gateNBT);
		}

		for (int i = 0; i < 4; ++i) {
			wireSet[i] = data.getBoolean("wireSet[" + i + "]");
		}
	}

	@Override
	public boolean hasGate() {
		return gate != null;
	}

	@Override
	public void addItemDrops(List<ItemStack> result) {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			if (wireSet[pipeWire.ordinal()]) {
				result.add(pipeWire.getStack());
			}
		}

		if (hasGate()) {
			result.add(gate.getGateItem());
		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (container.tilePart.hasFacade(direction)) {
				result.add (container.tilePart.getFacade(direction));
			}

			if (container.tilePart.hasPlug(direction)) {
				result.add (SimpleServiceLocator.buildCraftProxy.getPipePlugItemStack());
			}

			if (container.tilePart.hasRobotStation(direction)) {
				result.add (SimpleServiceLocator.buildCraftProxy.getRobotStationItemStack());
			}
		}
	}

	@Override
	public void resetGate() {
		gate.resetGate();
		gate = null;
	}

	@Override
	public boolean isWireConnectedTo(TileEntity tile, PipeWire color) {
		if(tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
			if (!LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
				return false;
			}

			if (!tilePipe.pipe.bcPipePart.getWireSet()[color.ordinal()]) {
				return false;
			}

			return Ut.checkPipesConnections(container, tile);
		}
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}

		TileGenericPipe tilePipe = (TileGenericPipe) tile;

		if (!BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
			return false;
		}

		if (!tilePipe.pipe.wireSet[color.ordinal()]) {
			return false;
		}

		return tilePipe.pipe.transport instanceof PipeTransportStructure || Utils.checkPipesConnections(container, tile);
	}

	@Override
	public boolean isWired(PipeWire color) {
		return wireSet[color.ordinal()];
	}

	@Override
	public int isPoweringTo(int side) {
		if (gate != null && gate.getRedstoneOutput() > 0) {
			ForgeDirection o = ForgeDirection.getOrientation(side).getOpposite();
			TileEntity tile = container.getTile(o);
	
			if (tile instanceof LogisticsTileGenericPipe && container.isPipeConnected(o)) {
				return 0;
			}
	
			return gate.getRedstoneOutput();
		}
		return 0;
	}

	@Override
	public void updateSignalStateForColor(PipeWire wire) {
		if (!wireSet[wire.ordinal()]) {
			return;
		}

		// STEP 1: compute internal signal strength

		if (gate != null && gate.broadcastSignal.get(wire.ordinal())) {
			receiveSignal(255, wire);
		} else {
			readNearbyPipesSignal(wire);
		}

		// STEP 2: transmit signal in nearby blocks

		if (signalStrength[wire.ordinal()] > 1) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = container.getTile(o);
				//TODO handle TileGenericPipe
				if (tile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;

					if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe) && tilePipe.pipe.bcPipePart.getWireSet()[wire.ordinal()]) {
						if (isWireConnectedTo(tile, wire)) {
							tilePipe.pipe.bcPipePart.receiveSignal(signalStrength[wire.ordinal()] - 1, wire);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean receiveSignal(int signal, PipeWire color) {
		if (container.getWorldObj() == null) {
			return false;
		}

		int oldSignal = signalStrength[color.ordinal()];

		if (signal >= signalStrength[color.ordinal()] && signal != 0) {
			signalStrength[color.ordinal()] = signal;
			container.pipe.internalUpdateScheduled = true;

			if (oldSignal == 0) {
				container.scheduleRenderUpdate();
			}

			return true;
		} else {
			return false;
		}
	}

	private void readNearbyPipesSignal(PipeWire color) {
		boolean foundBiggerSignal = false;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(o);

			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;

				if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
					if (isWireConnectedTo(tile, color)) {
						foundBiggerSignal |= receiveSignal(tilePipe.pipe.bcPipePart.getSignalStrength()[color.ordinal()] - 1, color);
					}
				}
			}
		}

		if (!foundBiggerSignal && signalStrength[color.ordinal()] != 0) {
			signalStrength[color.ordinal()] = 0;
			// worldObj.markBlockNeedsUpdate(container.xCoord, container.yCoord, zCoord);
			container.scheduleRenderUpdate();

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = container.getTile(o);
				//TODO handle TileGenericPipe
				if (tile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;

					if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
						tilePipe.pipe.internalUpdateScheduled = true;
					}
				}
			}
		}
	}

	@Override
	public boolean[] getWireSet() {
		return wireSet;
	}

	@Override
	public ItemStack getGateItem() {
		return gate.getGateItem();
	}

	@Override
	public int[] getSignalStrength() {
		return signalStrength;
	}

	@Override
	public void openGateGui(EntityPlayer player) {
		gate.openGui(player);
	}

	@Override
	public boolean isGateActive() {
		return gate != null && gate.isGateActive();
	}

	@Override
	public Object getGate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem) {
		gate = GateFactory.makeGate(wrapper, currentEquippedItem);
	}
}
