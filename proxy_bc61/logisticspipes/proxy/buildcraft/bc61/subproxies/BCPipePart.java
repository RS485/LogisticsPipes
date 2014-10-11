package logisticspipes.proxy.buildcraft.bc61.subproxies;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.proxy.bc.GateGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BCPipeWireHooks.PipeClassReceiveSignal;
import logisticspipes.proxy.buildcraft.bc61.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.bc61.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.bc61.gates.wrapperclasses.PipeWrapper;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePart;
import logisticspipes.utils.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.ActionState;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.IPipePluggable;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportStructure;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.ActionSlot;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.GuiGateInterface;

public class BCPipePart implements IBCPipePart {
	
	private static Field networkWrappersField;

	public final LogisticsTileGenericPipe container;
	private final BCCoreState coreState;
	public int[] signalStrength = new int[]{0, 0, 0, 0};
	public boolean[] wireSet = new boolean[]{false, false, false, false};
	public final Gate[] gates = new Gate[ForgeDirection.VALID_DIRECTIONS.length];

	private boolean closed = false;

	private ArrayList<ActionState> actionStates = new ArrayList<ActionState>();

	private boolean init;
	
	public PipeWrapper wrapper;

	private boolean resyncGateExpansions = false;
	
	public BCPipePart(LogisticsTileGenericPipe tile) {
		this.container = tile;
		this.coreState = (BCCoreState) tile.bcCoreState.getOriginal();
		try {
			startWrapper();
			wrapper = new PipeWrapper(tile);
			wrapper.wireSet = getWireSet();
			wrapper.signalStrength = getSignalStrength();
			ReflectionHelper.setFinalField(Pipe.class, "gates", wrapper, this.gates); //wrapper.gates = gates;
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
		closed = false;
		actionStates.clear();

		// Update the gate if we have any
		for (Gate gate : gates) {
			if (gate == null) {
				continue;
			}
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
	public void resolveActions() {
		for (Gate gate : gates) {
			if (gate != null) {
				gate.resolveActions();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		for (int i = 0; i < 4; ++i) {
			data.setBoolean("wireSet[" + i + "]", wireSet[i]);
		}
		// Save gate if any
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "Gate[" + i + "]";
			Gate gate = gates[i];
			if (gate != null) {
				NBTTagCompound gateNBT = new NBTTagCompound();
				gate.writeToNBT(gateNBT);
				data.setTag(key, gateNBT);
			} else {
				data.removeTag(key);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		for (int i = 0; i < 4; ++i) {
			wireSet[i] = data.getBoolean("wireSet[" + i + "]");
		}
		
		// Load gate if any
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final String key = "Gate[" + i + "]";
			gates[i] = data.hasKey(key) ? GateFactory.makeGate(wrapper, data.getCompoundTag(key)) : null;
		}

		// Legacy support
		if (data.hasKey("Gate")) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				container.tilePart.setGate(GateFactory.makeGate(wrapper, data.getCompoundTag("Gate")), i);
			}
			data.removeTag("Gate");
		}
	}

	@Override
	public boolean hasGate() {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (hasGate(direction)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasGate(ForgeDirection side) {
		return container.tilePart.hasGate(side);
	}

	@Override
	public void addItemDrops(List<ItemStack> result) {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			if (wireSet[pipeWire.ordinal()]) {
				result.add(pipeWire.getStack());
			}
		}

		for (Gate gate : gates) {
			if (gate != null) {
				result.add(gate.getGateItem());
			}
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
		for (int i = 0; i < gates.length; i++) {
			Gate gate = gates[i];
			if (gate != null) {
				gate.resetGate();
			}
			gates[i] = null;
		}

		this.container.pipe.internalUpdateScheduled = true;
		container.scheduleRenderUpdate();
	}

	@Override
	public boolean isWireConnectedTo(TileEntity tile, Object oColor) {
		PipeWire color = (PipeWire) oColor;
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

	public boolean isWired(PipeWire color) {
		return wireSet[color.ordinal()];
	}

	private int getMaxRedstoneOutput() {
		int max = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Gate gate = gates[dir.ordinal()];

			if (gate != null && gate.getRedstoneOutput() > max) {
				max = gate.getRedstoneOutput();
			}
		}

		return max;
	}

	@Override
	public int isPoweringTo(int side) {
		ForgeDirection o = ForgeDirection.getOrientation(side).getOpposite();

		TileEntity tile = container.getTile(o);

		if (tile instanceof TileGenericPipe && container.isPipeConnected(o)) {
			return 0;
		} else {
			return getMaxRedstoneOutput();
		}
	}

	public void updateSignalStateForColor(PipeWire wire) {
		if (!wireSet[wire.ordinal()]) {
			return;
		}

		// STEP 1: compute internal signal strength
		boolean readNearbySignal = true;
		for (Gate gate : gates) {
			if (gate != null && gate.broadcastSignal.get(wire.ordinal())) {
				receiveSignal(255, wire);
				readNearbySignal = false;
			}
		}

		if (readNearbySignal) {
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
	public boolean receiveSignal(int signal, Object oColor) {
		PipeWire color = (PipeWire) oColor;
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
	public ItemStack getGateItem(int side) {
		Gate gate = gates[side];
		return gate != null ? gate.getGateItem() : null;
	}

	@Override
	public int[] getSignalStrength() {
		return signalStrength;
	}

	@Override
	public void openGateGui(EntityPlayer player, int side) {
		if (!player.worldObj.isRemote) {
			NewGuiHandler.getGui(GateGui.class).setSide(side).setTilePos(container).open(player);
		}
	}

	@Override
	public boolean isGateActive() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getGate(int i) {
		return gates[i];
	}

	@Override
	public void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LinkedList<?> getActions() {
		LinkedList<IAction> result = new LinkedList<IAction>();
		
		if(container.pipe instanceof CoreRoutedPipe) {
			if(BuildCraftProxy.LogisticsDisableAction != null) {
				result.add(BuildCraftProxy.LogisticsDisableAction);
			}
		}
		
		return result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void actionsActivated(Object obj) {
		Collection<ActionSlot> actions = (Collection<ActionSlot>) obj;
		if(!(container.pipe instanceof CoreRoutedPipe)) return;
		((CoreRoutedPipe)container.pipe).setEnabled(true);
		// Activate the actions
		for (ActionSlot slot : actions) {
			if (slot.action instanceof ActionDisableLogistics) {
				((CoreRoutedPipe)container.pipe).setEnabled(false);
			}
		}
	}

	@Override
	public void updateCoreStateGateData() {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			IPipePluggable pluggable = (IPipePluggable) this.container.tilePart.getPluggables(i);
			((BCCoreState)this.container.bcCoreState.getOriginal()).gates[i] = pluggable instanceof ItemGate.GatePluggable ? (ItemGate.GatePluggable) pluggable : null;
		}
	}

	@Override
	public void updateGateFromCoreStateData() {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			final ItemGate.GatePluggable gatePluggable = coreState.gates[i];
			if (gatePluggable == null) {
				wrapper.gates[i] = gates[i] = null;
				continue;
			}
			Gate gate = gates[i];
			if (gate == null || gate.logic != gatePluggable.logic || gate.material != gatePluggable.material) {
				wrapper.gates[i] = gates[i] = GateFactory.makeGate(this.wrapper, gatePluggable.material, gatePluggable.logic, ForgeDirection.getOrientation(i));
			}
		}
		
		syncGateExpansions();
	}

	private void syncGateExpansions() {
		resyncGateExpansions = false;
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			Gate gate = gates[i];
			ItemGate.GatePluggable gatePluggable = coreState.gates[i];
			if (gate != null && gatePluggable.expansions.length > 0) {
				for (IGateExpansion expansion : gatePluggable.expansions) {
					if (expansion != null) {
						if (!gate.expansions.containsKey(expansion)) {
							gate.addGateExpansion(expansion);
						}
					} else {
						resyncGateExpansions = true;
					}
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
	public Container getGateContainer(InventoryPlayer inventory, int side) {
		ContainerGateInterface gui =  new ContainerGateInterface(inventory, wrapper);
		gui.setGate(side);
		return gui;
	}

	@Override
	public Object getClientGui(InventoryPlayer inventory, int side) {
		GuiGateInterface gui = new GuiGateInterface(inventory, wrapper);
		gui.mc = Minecraft.getMinecraft();
		gui.setGate(this.gates[side]);
		((ContainerGateInterface)gui.inventorySlots).setGate(side);
		gui.slots.clear();
		return gui;
	}

	@Override
	public boolean isWired() {
		for (PipeWire color : PipeWire.values()) {
			if (isWired(color)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateSignalState() {
		for (PipeWire c : PipeWire.values()) {
			updateSignalStateForColor(c);
		}
	}

	@Override
	public void refreshRedStoneInput(int redstoneInput) {
		wrapper.container.redstoneInput = redstoneInput;
	}

	@Override
	public Object getGates() {
		return gates;
	}

	@Override
	public Object getWrapped() {
		return wrapper;
	}
}
