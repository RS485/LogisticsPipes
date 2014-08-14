package logisticspipes.proxy.buildcraft.pipeparts;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.proxy.bc.GateGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BCPipeWireHooks;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.BCPipeWireHooks.PipeClassReceiveSignal;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.wrapperclasses.PipeWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportStructure;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.GuiGateInterface;

public class BCPipePart implements IBCPipePart {
	
	private static Field networkWrappersField;

	public LogisticsTileGenericPipe container;
	public int[] signalStrength = new int[]{0, 0, 0, 0};
	public boolean[] wireSet = new boolean[]{false, false, false, false};
	public Gate gate;
	
	private boolean init;
	
	public PipeWrapper wrapper;

	private boolean resyncGateExpansions = false;
	
	public BCPipePart(LogisticsTileGenericPipe tile) {
		this.container = tile;
		try {
			startWrapper();
			wrapper = new PipeWrapper(tile);
			wrapper.wireSet = getWireSet();
			wrapper.signalStrength = getSignalStrength();
			wrapper.gate = gate;
			stopWrapper();
		} catch(IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch(NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch(SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateEntity() {
		if(!init) {
			wrapper.updateWorld();
			init = true;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void startWrapper() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		if(networkWrappersField == null) {
			networkWrappersField = Pipe.class.getDeclaredField("networkWrappers");
			networkWrappersField.setAccessible(true);
		}
		Map<Class, TilePacketWrapper> networkWrappers = (Map<Class, TilePacketWrapper>) networkWrappersField.get(null);
		networkWrappers.put(PipeWrapper.class, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void stopWrapper() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Map<Class, TilePacketWrapper> networkWrappers = (Map<Class, TilePacketWrapper>) networkWrappersField.get(null);
		networkWrappers.remove(PipeWrapper.class);
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
		for (int i = 0; i < 4; ++i) {
			data.setBoolean("wireSet[" + i + "]", wireSet[i]);
		}
		// Save gate if any
		if (gate != null) {
			NBTTagCompound gateNBT = new NBTTagCompound();
			gate.writeToNBT(gateNBT);
			data.setTag("Gate", gateNBT);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		for (int i = 0; i < 4; ++i) {
			wireSet[i] = data.getBoolean("wireSet[" + i + "]");
		}
		
		// Load gate if any
		if (data.hasKey("Gate")) {
			NBTTagCompound gateNBT = data.getCompoundTag("Gate");
			wrapper.gate = gate = GateFactory.makeGate(wrapper, gateNBT);
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
		wrapper.gate = gate = null;
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

			return MainProxy.checkPipesConnections(container, tile);
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
				if (tile instanceof TileGenericPipe) {
					TileGenericPipe tilePipe = (TileGenericPipe) tile;

					if (BlockGenericPipe.isFullyDefined(tilePipe.pipe) && tilePipe.pipe.wireSet[wire.ordinal()]) {
						if (isWireConnectedTo(tile, wire)) {
							((PipeClassReceiveSignal)tilePipe.pipe).receiveSignal(signalStrength[wire.ordinal()] - 1, wire);
						}
					}
				}
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
			if (tile instanceof TileGenericPipe) {
				TileGenericPipe tilePipe = (TileGenericPipe) tile;

				if (BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
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
				if (tile instanceof TileGenericPipe) {
					TileGenericPipe tilePipe = (TileGenericPipe) tile;

					if (BlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
						((PipeClassReceiveSignal)tilePipe.pipe).triggerInternalUpdateScheduled();
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
		if (!player.worldObj.isRemote) {
			NewGuiHandler.getGui(GateGui.class).setTilePos(container).open(player);
		}
	}

	@Override
	public boolean isGateActive() {
		return gate != null && gate.isGateActive();
	}

	@Override
	public Object getGate() {
		return gate;
	}

	@Override
	public void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem) {
		wrapper.gate = gate = GateFactory.makeGate(wrapper, currentEquippedItem);
	}

	@Override
	public LinkedList<?> getActions() {
		LinkedList<IAction> result = new LinkedList<IAction>();

		if (hasGate()) {
			gate.addActions(result);
		}
		
		if(container.pipe instanceof CoreRoutedPipe) {
			if(BuildCraftProxy.LogisticsDisableAction != null) {
				result.add(BuildCraftProxy.LogisticsDisableAction);
			}
		}
		
		return result;
	}
	
	@Override
	public void actionsActivated(Object obj) {
		Map<IAction, Boolean> actions = (Map<IAction, Boolean>) obj;
		if(!(container.pipe instanceof CoreRoutedPipe)) return;
		((CoreRoutedPipe)container.pipe).setEnabled(true);
		// Activate the actions
		for (Entry<IAction, Boolean> i : actions.entrySet()) {
			if (i.getValue()) {
				if (i.getKey() instanceof ActionDisableLogistics){
					((CoreRoutedPipe)container.pipe).setEnabled(false);
				}
			}
		}
	}

	@Override
	public void updateCoreStateGateData() {
		container.coreState.expansions.clear();
		if (gate != null) {
			container.coreState.gateMaterial = gate.material.ordinal();
			container.coreState.gateLogic = gate.logic.ordinal();
			for (IGateExpansion ex : gate.expansions.keySet()) {
				container.coreState.expansions.add(GateExpansions.getServerExpansionID(ex.getUniqueIdentifier()));
			}
		} else {
			container.coreState.gateMaterial = -1;
			container.coreState.gateLogic = -1;
		}
	}

	@Override
	public void updateGateFromCoreStateData() {
		if (container.coreState.gateMaterial == -1) {
			wrapper.gate = gate = null;
		} else if (gate == null) {
			wrapper.gate = gate = GateFactory.makeGate(this.wrapper, GateDefinition.GateMaterial.fromOrdinal(container.coreState.gateMaterial), GateDefinition.GateLogic.fromOrdinal(container.coreState.gateLogic));
		}

		syncGateExpansions();
	}

	private void syncGateExpansions() {
		resyncGateExpansions = false;
		if (gate != null && !container.coreState.expansions.isEmpty()) {
			for (byte id : container.coreState.expansions) {
				IGateExpansion ex = GateExpansions.getExpansionClient(id);
				if (ex != null) {
					if (!gate.expansions.containsKey(ex)) {
						gate.addGateExpansion(ex);
					}
				} else {
					resyncGateExpansions = true;
				}
			}
		}
	}

	@Override
	public void checkResyncGate() {
		if (resyncGateExpansions) {
			syncGateExpansions();
		}
	}

	@Override
	public Container getGateContainer(InventoryPlayer inventory) {
		return new ContainerGateInterface(inventory, wrapper);
	}

	@Override
	public Object getClientGui(InventoryPlayer inventory) {
		return new GuiGateInterface(inventory, wrapper);
	}
}
