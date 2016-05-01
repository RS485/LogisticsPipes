package logisticspipes.pipes.basic;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILPPipe;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logic.LogicController;
import logisticspipes.logic.interfaces.ILogicControllerTile;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.pipe.PipeTileStatePacket;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPluggableState;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.IOCTile;
import logisticspipes.proxy.opencomputers.asm.BaseWrapperClass;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.LogisticsTileRenderController;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.StackTraceUtil;
import logisticspipes.utils.StackTraceUtil.Info;
import logisticspipes.utils.TileBuffer;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumColor;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.TileGenericPipe;
import cofh.api.transport.IItemDuct;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import lombok.Getter;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;
import org.apache.logging.log4j.Level;

public abstract class LogisticsTileGenericPipe extends TileEntity implements ITickable, IPipeInformationProvider, ILogicControllerTile, IFluidHandler, IOCTile, ILPPipeTile {

	public Object OPENPERIPHERAL_IGNORE; //Tell OpenPeripheral to ignore this class

	public Set<DoubleCoordinates> subMultiBlock = new HashSet<>();

	public boolean[] turtleConnect = new boolean[7];

	private LogisticsTileRenderController renderController;

	private boolean addedToNetwork = false;

	private boolean sendInitPacket = true;

	public LogicController logicController = new LogicController();

	public final PipeRenderState renderState;
	public final CoreState coreState = new CoreState();
	public final IBCTilePart tilePart;
	public final IBCPluggableState bcPlugableState;
	public final ITDPart tdPart;

	public LogisticsTileGenericPipe() {
		preInit();
		SimpleServiceLocator.openComputersProxy.initLogisticsTileGenericPipe(this);
		tilePart = SimpleServiceLocator.buildCraftProxy.getBCTilePart(this);
		tdPart = SimpleServiceLocator.thermalDynamicsProxy.getTDPart(this);
		renderState = new PipeRenderState(tilePart);
		bcPlugableState = tilePart.getBCPlugableState();
	}

	protected void preInit() {}

	@Override
	public void invalidate() {
		if (pipe == null) {
			tileEntityInvalid = true;
			initialized = false;
			tileBuffer = null;
			super.invalidate();
		} else if (!pipe.preventRemove()) {
			tileEntityInvalid = true;
			initialized = false;
			tileBuffer = null;
			pipe.invalidate();
			super.invalidate();
			SimpleServiceLocator.openComputersProxy.handleInvalidate(this);
			tilePart.invalidate_LP();
			tdPart.invalidate();
		}
	}

	@Override
	public void validate() {
		super.validate();
		initialized = false;
		tileBuffer = null;
		bindPipe();
		if (pipe != null) {
			pipe.validate();
		}
		tilePart.validate_LP();
	}

	@Override
	public void onChunkUnload() {
		if (pipe != null) {
			pipe.onChunkUnload();
		}
		SimpleServiceLocator.openComputersProxy.handleChunkUnload(this);
		tdPart.onChunkUnload();
	}

	@Override
	public void update() {
		Info superDebug = StackTraceUtil.addSuperTraceInformation("Time: " + getWorld().getWorldTime());
		Info debug = StackTraceUtil.addTraceInformation("(" + getX() + ", " + getY() + ", " + getZ() + ")", superDebug);
		if (sendInitPacket && MainProxy.isServer(getWorld())) {
			sendInitPacket = false;
			getRenderController().sendInit();
		}
		if (!worldObj.isRemote) {
			if (deletePipe) {
				worldObj.setBlockToAir(getPos());
			}

			if (pipe == null) {
				debug.end();
				return;
			}

			if (!initialized) {
				initialize(pipe);
			}
		}

		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			debug.end();
			return;
		}

		pipe.updateEntity();

		if (worldObj.isRemote) {
			debug.end();
			return;
		}

		if (blockNeighborChange) {
			computeConnections();
			pipe.onNeighborBlockChange(0);
			blockNeighborChange = false;
			refreshRenderState = true;
		}

		//Sideblocks need to be checked before this
		tilePart.updateEntity_LP();
		//Network needs to be after this

		if (refreshRenderState) {
			// Pipe connections;
			for (EnumFacing o : EnumFacing.VALUES) {
				renderState.pipeConnectionMatrix.setConnected(o, pipeConnectionsBuffer[o.ordinal()]);
				renderState.pipeConnectionMatrix.setBCConnected(o, pipeBCConnectionsBuffer[o.ordinal()]);
				renderState.pipeConnectionMatrix.setTDConnected(o, pipeTDConnectionsBuffer[o.ordinal()]);
			}
			// Pipe Textures
			for (int i = 0; i < 7; i++) {
				EnumFacing o = EnumFacing.getFront(i);
				renderState.textureMatrix.setIconIndex(o, pipe.getIconIndex(o));
			}
			//New Pipe Texture States
			renderState.textureMatrix.refreshStates(pipe);

			if (renderState.isDirty()) {
				renderState.clean();
				sendUpdateToClient();
			}

			refreshRenderState = false;
		}

		if (bcPlugableState.isDirty(true)) {
			sendUpdateToClient();
		}

		if (sendClientUpdate) {
			sendClientUpdate = false;

			MainProxy.sendPacketToAllWatchingChunk(this, getLPDescriptionPacket());
		}
		getRenderController().onUpdate();
		if (!addedToNetwork) {
			addedToNetwork = true;
			SimpleServiceLocator.openComputersProxy.addToNetwork(this);
		}
		debug.end();
	}

	@Override
	public Packet getDescriptionPacket() {
		sendInitPacket = true;
		try {
			return PacketHandler.toFMLPacket(getLPDescriptionPacket());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void addInfoToCrashReport(CrashReportCategory par1CrashReportCategory) {
		try {
			super.addInfoToCrashReport(par1CrashReportCategory);
		} catch (Exception e) {
			if (LPConstants.DEBUG) {
				e.printStackTrace();
			}
		}
		par1CrashReportCategory.addCrashSection("LP-Version", LPConstants.VERSION);
		if (pipe != null) {
			par1CrashReportCategory.addCrashSection("Pipe", pipe.getClass().getCanonicalName());
			if (pipe.transport != null) {
				par1CrashReportCategory.addCrashSection("Transport", pipe.transport.getClass().getCanonicalName());
			} else {
				par1CrashReportCategory.addCrashSection("Transport", "null");
			}

			if (pipe instanceof CoreRoutedPipe) {
				try {
					((CoreRoutedPipe) pipe).addCrashReport(par1CrashReportCategory);
				} catch (Exception e) {
					par1CrashReportCategory.addCrashSectionThrowable("Internal LogisticsPipes Error", e);
				}
			}
		}
	}

	public void scheduleNeighborChange() {
		tilePart.scheduleNeighborChange();
		tdPart.scheduleNeighborChange();
		blockNeighborChange = true;
		boolean[] connected = new boolean[6];
		new WorldCoordinatesWrapper(this).getAdjacentTileEntities()
				.filter(adjacent -> SimpleServiceLocator.ccProxy.isTurtle(adjacent.tileEntity))
				.forEach(adjacent -> connected[adjacent.direction.ordinal()] = true);
		for (int i = 0; i < 6; i++) {
			if (!connected[i]) {
				turtleConnect[i] = false;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		/*
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			nbt.setByte(key, (byte) redstoneInputSide[i]);
		}
		 */

		if (pipe != null) {
			nbt.setInteger("pipeId", Item.itemRegistry.getIDForObject(pipe.item));
			pipe.writeToNBT(nbt);
		} else {
			nbt.setInteger("pipeId", coreState.pipeId);
		}

		tilePart.writeToNBT_LP(nbt);
		for (int i = 0; i < turtleConnect.length; i++) {
			nbt.setBoolean("turtleConnect_" + i, turtleConnect[i]);
		}
		SimpleServiceLocator.openComputersProxy.handleWriteToNBT(this, nbt);

		NBTTagCompound logicNBT = new NBTTagCompound();
		logicController.writeToNBT(logicNBT);
		nbt.setTag("logicController", logicNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(pipe != null) {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			if (trace.length > 2 && trace[2].getMethodName().equals("handle") && trace[2].getClassName().equals("com.xcompwiz.lookingglass.network.packet.PacketTileEntityNBT")) {
				System.out.println("Prevented false data injection by LookingGlass");
				return;
			}
		}
		super.readFromNBT(nbt);

		if (nbt.hasKey("redstoneInputSide[0]")) {
			tilePart.readOldRedStone(nbt);
		}

		coreState.pipeId = nbt.getInteger("pipeId");
		pipe = LogisticsBlockGenericPipe.createPipe(Item.getItemById(coreState.pipeId));
		bindPipe();

		if (pipe != null) {
			pipe.readFromNBT(nbt);
		} else {
			LogisticsPipes.log.log(Level.WARN, "Pipe failed to load from NBT at {0},{1},{2}", new Object[] { pos.getX(), pos.getY(), pos.getZ() });
			deletePipe = true;
		}

		tilePart.readFromNBT_LP(nbt);
		for (int i = 0; i < turtleConnect.length; i++) {
			turtleConnect[i] = nbt.getBoolean("turtleConnect_" + i);
		}
		SimpleServiceLocator.openComputersProxy.handleReadFromNBT(this, nbt);

		logicController.readFromNBT(nbt.getCompoundTag("logicController"));
	}

	public boolean canPipeConnect(TileEntity with, EnumFacing side) {
		if (MainProxy.isClient(worldObj)) {
			//XXX why is this ever called client side, its not *used* for anything.
			return false;
		}
		if (with == null) {
			return false;
		}

		if (tilePart.hasBlockingPluggable(side)) {
			return false;
		}

		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			return false;
		}

		if (SimpleServiceLocator.ccProxy.isTurtle(with) && !turtleConnect[OrientationsUtil.getOrientationOfTilewithTile(this, with).ordinal()]) {
			return false;
		}

		IConnectionOverrideResult result = SimpleServiceLocator.buildCraftProxy.checkConnectionOverride(with, side, this);
		if (result.forceDisconnect()) {
			return false;
		}
		if (result.forceConnect()) {
			return true;
		}

		if (!SimpleServiceLocator.buildCraftProxy.checkForPipeConnection(with, side, this)) {
			return false;
		}
		if (SimpleServiceLocator.thermalDynamicsProxy.isBlockedSide(with, side.getOpposite())) {
			return false;
		}
		if (with instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) with).tilePart.hasBlockingPluggable(side.getOpposite())) {
				return false;
			}
			CoreUnroutedPipe otherPipe = ((LogisticsTileGenericPipe) with).pipe;

			if (!(LogisticsBlockGenericPipe.isValid(otherPipe))) {
				return false;
			}

			if (!(otherPipe.canPipeConnect(this, side.getOpposite()))) {
				return false;
			}
		}
		return pipe.canPipeConnect(with, side);
	}

	public void queueEvent(String event, Object[] arguments) {
		SimpleServiceLocator.ccProxy.queueEvent(event, arguments, this);
	}

	public void handleMesssage(int computerId, Object message, int sourceId) {
		SimpleServiceLocator.ccProxy.handleMesssage(computerId, message, this, sourceId);
	}

	public void setTurtleConnect(boolean flag) {
		SimpleServiceLocator.ccProxy.setTurtleConnect(flag, this);
	}

	public boolean getTurtleConnect() {
		return SimpleServiceLocator.ccProxy.getTurtleConnect(this);
	}

	public int getLastCCID() {
		return SimpleServiceLocator.ccProxy.getLastCCID(this);
	}

	public ItemStack insertItem(EnumFacing dir, ItemStack stack) {
		int used = injectItem(stack, true, dir);
		if (used == stack.stackSize) {
			return null;
		} else {
			stack = stack.copy();
			stack.stackSize -= used;
			return stack;
		}
	}

	public void addLaser(EnumFacing dir, float length, int color, boolean reverse, boolean renderBall) {
		getRenderController().addLaser(dir, length, color, reverse, renderBall);
	}

	public void removeLaser(EnumFacing dir, int color, boolean isBall) {
		getRenderController().removeLaser(dir, color, isBall);
	}

	public LogisticsTileRenderController getRenderController() {
		if (renderController == null) {
			renderController = new LogisticsTileRenderController(this);
		}
		return renderController;
	}

	/* IPipeInformationProvider */

	@Override
	public boolean isCorrect(ConnectionPipeType type) {
		return true;
	}

	@Override
	public int getX() {
		return getPos().getX();
	}

	@Override
	public int getY() {
		return getPos().getY();
	}

	@Override
	public int getZ() {
		return getPos().getZ();
	}

	@Override
	public boolean isRouterInitialized() {
		return isInitialized() && (!isRoutingPipe() || !getRoutingPipe().stillNeedReplace());
	}

	@Override
	public boolean isRoutingPipe() {
		return pipe instanceof CoreRoutedPipe;
	}

	@Override
	public CoreRoutedPipe getRoutingPipe() {
		if (pipe instanceof CoreRoutedPipe) {
			return (CoreRoutedPipe) pipe;
		}
		throw new RuntimeException("This is no routing pipe");
	}

	@Override
	public boolean isFirewallPipe() {
		return pipe instanceof PipeItemsFirewall;
	}

	@Override
	public IFilter getFirewallFilter() {
		if (pipe instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall) pipe).getFilter();
		}
		throw new RuntimeException("This is no firewall pipe");
	}

	public TileEntity getTile() {
		return this;
	}

	@Override
	public boolean divideNetwork() {
		return false;
	}

	@Override
	public boolean powerOnly() {
		return false;
	}

	@Override
	public boolean isOnewayPipe() {
		return false;
	}

	@Override
	public boolean isOutputOpen(EnumFacing direction) {
		return true;
	}

	@Override
	public boolean isItemPipe() {
		return true;
	}

	@Override
	public boolean isFluidPipe() {
		return pipe != null && pipe.isFluidPipe();
	}

	@Override
	public boolean isPowerPipe() {
		return false;
	}

	@Override
	public boolean canConnect(TileEntity to, EnumFacing direction, boolean flag) {
		if (pipe == null) {
			return false;
		}
		return pipe.canPipeConnect(to, direction, flag);
	}

	@Override
	public double getDistance() {
		if(this.pipe != null && this.pipe.transport != null) {
			return this.pipe.transport.getPipeLength();
		}
		return 1;
	}

	public int injectItem(ItemStack payload, boolean doAdd, EnumFacing from) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport != null && isPipeConnected(from)) {
			if (doAdd && MainProxy.isServer(getWorld())) {
				ItemStack leftStack = payload.copy();
				int lastIterLeft;
				do {
					lastIterLeft = leftStack.stackSize;
					LPTravelingItem.LPTravelingItemServer travelingItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(leftStack);
					leftStack.stackSize = pipe.transport.injectItem(travelingItem, from.getOpposite());
				} while (leftStack.stackSize != lastIterLeft && leftStack.stackSize != 0);
				return payload.stackSize - leftStack.stackSize;
			}
		}
		return 0;
	}

	public boolean isOpaque() {
		return pipe.isOpaque();
	}

	@Getter
	private boolean initialized = false;

	public boolean[] pipeConnectionsBuffer = new boolean[6];
	public boolean[] pipeBCConnectionsBuffer = new boolean[6];
	public boolean[] pipeTDConnectionsBuffer = new boolean[6];

	public CoreUnroutedPipe pipe;
	//public int redstoneInput = 0;
	//public int[] redstoneInputSide = new int[EnumFacing.VALUES.length];

	private boolean deletePipe = false;
	private TileBuffer[] tileBuffer;
	private boolean sendClientUpdate = false;
	private boolean blockNeighborChange = false;
	private boolean refreshRenderState = false;
	private boolean pipeBound = false;

	public class CoreState implements IClientState {

		public int pipeId = -1;

		@Override
		public void writeData(LPDataOutputStream data) throws IOException {
			data.writeInt(pipeId);

		}

		@Override
		public void readData(LPDataInputStream data) throws IOException {
			pipeId = data.readInt();

		}
	}

	public void initialize(CoreUnroutedPipe pipe) {
		blockType = getBlockType();

		if (pipe == null) {
			LogisticsPipes.log.log(Level.WARN, "Pipe failed to initialize at {0},{1},{2}, deleting", new Object[] { pos.getX(), pos.getY(), pos.getY() });
			worldObj.setBlockToAir(getPos());
			return;
		}

		this.pipe = pipe;

		for (EnumFacing o : EnumFacing.VALUES) {
			TileEntity tile = getTile(o);

			if (tile instanceof LogisticsTileGenericPipe) {
				((LogisticsTileGenericPipe) tile).scheduleNeighborChange();
			}

			SimpleServiceLocator.buildCraftProxy.notifyOfChange(this, tile, o);
		}

		bindPipe();

		computeConnections();
		scheduleRenderUpdate();

		if (pipe.needsInit()) {
			pipe.initialize();
		}

		initialized = true;
	}

	private void bindPipe() {
		if (!pipeBound && pipe != null) {
			pipe.setTile(this);
			coreState.pipeId = Item.getIdFromItem(pipe.item);
			pipeBound = true;
		}
	}

	/* SMP */

	public ModernPacket getLPDescriptionPacket() {
		bindPipe();

		PipeTileStatePacket packet = PacketHandler.getPacket(PipeTileStatePacket.class);

		packet.setTilePos(this);

		packet.setCoreState(coreState);
		packet.setRenderState(renderState);
		packet.setBcPluggableState(bcPlugableState);
		packet.setPipe(pipe);

		return packet;
	}

	public void afterStateUpdated() {
		if (pipe == null && coreState.pipeId != 0) {
			initialize(LogisticsBlockGenericPipe.createPipe((Item) Item.itemRegistry.getObjectById(coreState.pipeId)));
		}

		if (pipe == null) {
			return;
		}

		worldObj.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

		if (renderState.needsRenderUpdate()) {
			worldObj.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
			renderState.clean();
		}
		tilePart.afterStateUpdated();
	}

	public void sendUpdateToClient() {
		sendClientUpdate = true;
	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null && pipe != null) {
			tileBuffer = TileBuffer.makeBuffer(worldObj, pos.getX(), pos.getY(), pos.getZ(), pipe.transport.delveIntoUnloadedChunks());
		}
		return tileBuffer;
	}

	public void blockCreated(EnumFacing from, Block block, TileEntity tile) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			cache[from.getOpposite().ordinal()].set(block, tile);
		}
	}

	@Override
	public TileEntity getNextConnectedTile(EnumFacing to) {
		if(this.pipe.isMultiBlock()) {
			return ((CoreMultiBlockPipe)this.pipe).getConnectedEndTile(to);
		}
		return getTile(to, false);
	}

	public TileEntity getTile(EnumFacing to) {
		return getTile(to, false);
	}

	public TileEntity getTile(EnumFacing to, boolean force) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			if (force) {
				cache[to.ordinal()].refresh();
			}
			return cache[to.ordinal()].getTile();
		} else {
			return null;
		}
	}

	public Block getBlock(EnumFacing to) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getBlock();
		} else {
			return null;
		}
	}

	private void computeConnections() {
		TileBuffer[] cache = getTileCache();
		if (cache == null) {
			return;
		}

		boolean[] pipeTDConnectionsBufferOld = pipeTDConnectionsBuffer.clone();

		for (EnumFacing side : EnumFacing.VALUES) {
			TileBuffer t = cache[side.ordinal()];
			t.refresh();

			pipeConnectionsBuffer[side.ordinal()] = canPipeConnect(t.getTile(), side);
			if (pipeConnectionsBuffer[side.ordinal()]) {
				pipeBCConnectionsBuffer[side.ordinal()] = SimpleServiceLocator.buildCraftProxy.isTileGenericPipe(t.getTile());
				pipeTDConnectionsBuffer[side.ordinal()] = SimpleServiceLocator.thermalDynamicsProxy.isItemDuct(t.getTile());
			} else {
				pipeBCConnectionsBuffer[side.ordinal()] = false;
				pipeTDConnectionsBuffer[side.ordinal()] = false;
			}
		}
		if (!Arrays.equals(pipeTDConnectionsBufferOld, pipeTDConnectionsBuffer)) {
			tdPart.connectionsChanged();
		}
	}

	/**
	 * ITankContainer implementation *
	 */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !tilePart.hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).fill(from, resource, doFill);
		} else {
			return 0;
		}
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !tilePart.hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, maxDrain, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !tilePart.hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).drain(from, resource, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !tilePart.hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).canFill(from, fluid);
		} else {
			return false;
		}
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof IFluidHandler && !tilePart.hasBlockingPluggable(from)) {
			return ((IFluidHandler) pipe.transport).canDrain(from, fluid);
		} else {
			return false;
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return null;
	}

	public void scheduleRenderUpdate() {
		refreshRenderState = true;
	}

	@SideOnly(Side.CLIENT)
	public IIconProvider getPipeIcons() {
		if (pipe == null) {
			return null;
		}
		return pipe.getIconProvider();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 64 * 4 * 64 * 4;
	}

	public boolean isSolidOnSide(EnumFacing side) {
		return tilePart.isSolidOnSide(side);
	}

	public Block getBlock() {
		return getBlockType();
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(pos) == this;
	}

	@Override
	public boolean isInvalid() {
		if (pipe != null && pipe.preventRemove()) {
			return false;
		}
		return super.isInvalid();
	}

	@Override
	public LogicController getLogicController() {
		return logicController;
	}

	@Override
	public ILPPipe getLPPipe() {
		return pipe;
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		tilePart.setWorldObj_LP(world);
		tdPart.setWorldObj_LP(world);
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderBox;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (renderBox != null) {
			return renderBox;
		}
		if (pipe == null) {
			return new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
		}
		if (!pipe.isMultiBlock()) {
			renderBox = new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
		} else {
			LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> set = ((CoreMultiBlockPipe) pipe).getRotatedSubBlocks();
			set.addToAll(pipe.getLPPosition());
			set.add(new DoubleCoordinatesType<>(getPos(), CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
			set.add(new DoubleCoordinatesType<>(getPos().add(1, 1, 1), CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
			renderBox = AxisAlignedBB.fromBounds(set.getMinXD() - 1, set.getMinYD() - 1, set.getMinZD() - 1, set.getMaxXD() + 1, set.getMaxYD() + 1, set.getMaxZD() + 1);
		}
		return renderBox;
	}

	@Override
	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double traveled, double max, List<DoubleCoordinates> visited) {
		if (pipe == null || traveled > max) {
			return Integer.MAX_VALUE;
		}
		double result = pipe.getDistanceTo(destinationint, ignore, ident, isActive, traveled + getDistance(), max, visited);
		if (result == Integer.MAX_VALUE) {
			return result;
		}
		return result + (int) getDistance();
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, TileEntity from) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport != null) {
			pipe.transport.injectItem(item, item.output);
			return true;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(EnumFacing side) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			cache[side.ordinal()].refresh();
		}
	}

	@Override
	public boolean isMultiBlock() {
		return pipe != null && pipe.isMultiBlock();
	}

	@Override
	public Stream<TileEntity> getPartsOfPipe() {
		return this.subMultiBlock.stream().map(pos -> pos.getTileEntity(worldObj));
	}

	public boolean isPipeConnected(EnumFacing with) {
		if (worldObj.isRemote) {
			return renderState.pipeConnectionMatrix.isConnected(with);
		}
		return pipeConnectionsBuffer[with.ordinal()];
	}

	public PipePluggable getPipePluggable(EnumFacing direction) {
		if (tilePart.getBCPipePluggable(direction) == null) {
			return null;
		}
		return (PipePluggable) tilePart.getBCPipePluggable(direction).getOriginal();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		LogisticsPipeWorldRenderer.renderPass = pass;
		return super.shouldRenderInPass(pass);
	}
}
