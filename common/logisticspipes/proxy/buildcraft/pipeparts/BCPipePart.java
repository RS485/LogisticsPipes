package logisticspipes.proxy.buildcraft.pipeparts;

import java.util.List;

import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import buildcraft.api.transport.PipeWire;
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

	public BCPipePart(LogisticsTileGenericPipe tile) {
		this.container = tile;
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
			gate = GateFactory.makeGate(this, gateNBT);
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
			if (container.hasFacade(direction)) {
				result.add (container.getFacade(direction));
			}

			if (container.hasPlug(direction)) {
				result.add (new ItemStack(BuildCraftTransport.plugItem));
			}

			if (container.hasRobotStation(direction)) {
				result.add (new ItemStack(BuildCraftTransport.robotStationItem));
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

		return tilePipe.pipe.transport instanceof PipeTransportStructure || transport instanceof PipeTransportStructure || Utils.checkPipesConnections(container, tile);
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

				if (tile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;

					if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe) && tilePipe.pipe.wireSet[wire.ordinal()]) {
						if (isWireConnectedTo(tile, wire)) {
							tilePipe.pipe.receiveSignal(signalStrength[wire.ordinal()] - 1, wire);
						}
					}
				}
			}
		}
	}

	private boolean receiveSignal(int signal, PipeWire color) {
		if (container.getWorldObj() == null) {
			return false;
		}

		int oldSignal = signalStrength[color.ordinal()];

		if (signal >= signalStrength[color.ordinal()] && signal != 0) {
			signalStrength[color.ordinal()] = signal;
			internalUpdateScheduled = true;

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
						foundBiggerSignal |= receiveSignal(tilePipe.pipe.signalStrength[color.ordinal()] - 1, color);
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
}
