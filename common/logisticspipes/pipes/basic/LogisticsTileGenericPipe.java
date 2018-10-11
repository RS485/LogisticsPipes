package logisticspipes.pipes.basic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.network.Node;
import lombok.Getter;
import org.apache.logging.log4j.Level;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILPPipe;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logic.LogicController;
import logisticspipes.logic.interfaces.ILogicControllerTile;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.block.PipeSolidSideCheck;
import logisticspipes.network.packets.pipe.PipeTileStatePacket;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.proxy.opencomputers.IOCTile;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.LogisticsTileRenderController;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.StackTraceUtil;
import logisticspipes.utils.StackTraceUtil.Info;
import logisticspipes.utils.TileBuffer;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

@ModDependentInterface(modId = { LPConstants.cofhCoreModID, LPConstants.openComputersModID, LPConstants.openComputersModID, LPConstants.openComputersModID},
		interfacePath = { "cofh.api.transport.IItemDuct", "li.cil.oc.api.network.ManagedPeripheral",
		"li.cil.oc.api.network.Environment", "li.cil.oc.api.network.SidedEnvironment", })
public class LogisticsTileGenericPipe extends TileEntity
		implements ITickable, IOCTile, ILPPipeTile, IPipeInformationProvider, /*IItemDuct, ManagedPeripheral, Environment, SidedEnvironment, */
		ILogicControllerTile {

	public int statePacketId = 0;
	public final PipeRenderState renderState;
	public final CoreState coreState = new CoreState();
	public final ITDPart tdPart;
	public final IBCPipeCapabilityProvider bcCapProvider;
	public Object OPENPERIPHERAL_IGNORE; //Tell OpenPeripheral to ignore this class
	public Set<DoubleCoordinates> subMultiBlock = new HashSet<>();
	public boolean[] turtleConnect = new boolean[7];
	@ModDependentField(modId = LPConstants.computerCraftModID)
	public HashMap<IComputerAccess, EnumFacing> connections;
	@ModDependentField(modId = LPConstants.computerCraftModID)
	public IComputerAccess currentPC;
	@ModDependentField(modId = LPConstants.openComputersModID)
	public Node node;
	public LogicController logicController = new LogicController();
	public boolean[] pipeConnectionsBuffer = new boolean[6];
	public boolean[] pipeTDConnectionsBuffer = new boolean[6];
	public CoreUnroutedPipe pipe;
	private LogisticsTileRenderController renderController;
	private boolean addedToNetwork = false;
	private boolean sendInitPacket = true;
	@Getter
	private boolean initialized = false;
	private boolean deletePipe = false;
	private TileBuffer[] tileBuffer;
	private boolean sendClientUpdate = false;
	private boolean blockNeighborChange = false;
	private boolean refreshRenderState = false;
	private boolean pipeBound = false;
	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderBox;

	public LogisticsTileGenericPipe() {
		preInit();
		SimpleServiceLocator.openComputersProxy.initLogisticsTileGenericPipe(this);
		tdPart = SimpleServiceLocator.thermalDynamicsProxy.getTDPart(this);
		bcCapProvider = SimpleServiceLocator.buildCraftProxy.getIBCPipeCapabilityProvider(this);
		renderState = new PipeRenderState();
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
		if (!world.isRemote) {
			if (deletePipe) {
				world.setBlockToAir(getPos());
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

		if (world.isRemote) {
			debug.end();
			return;
		}

		if (blockNeighborChange) {
			computeConnections();
			pipe.onNeighborBlockChange();
			blockNeighborChange = false;
			refreshRenderState = true;

			if(MainProxy.isServer(world)) {
				MainProxy.sendPacketToAllWatchingChunk(this, PacketHandler.getPacket(PipeSolidSideCheck.class).setTilePos(this));
			}
		}

		//Sideblocks need to be checked before this
		//Network needs to be after this

		if (refreshRenderState) {
			// Pipe connections;
			for (EnumFacing o : EnumFacing.VALUES) {
				renderState.pipeConnectionMatrix.setConnected(o, pipeConnectionsBuffer[o.ordinal()]);
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
	public NBTTagCompound getUpdateTag() {
		sendInitPacket = true;
		NBTTagCompound nbt = super.getUpdateTag();
		try {
			PacketHandler.addPacketToNBT(getLPDescriptionPacket(), nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nbt;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleUpdateTag(NBTTagCompound tag) {
		PacketHandler.queueAndRemovePacketFromNBT(tag);
		super.handleUpdateTag(tag);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		try {
			PacketHandler.addPacketToNBT(getLPDescriptionPacket(), nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SPacketUpdateTileEntity(getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		PacketHandler.queueAndRemovePacketFromNBT(packet.getNbtCompound());
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

	/* IPipeInformationProvider */

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);

		/*
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			final String key = "redstoneInputSide[" + i + "]";
			nbt.setByte(key, (byte) redstoneInputSide[i]);
		}
		 */

		if (pipe != null) {
			nbt.setInteger("pipeId", Item.REGISTRY.getIDForObject(pipe.item));
			pipe.writeToNBT(nbt);
		} else {
			nbt.setInteger("pipeId", coreState.pipeId);
		}

		for (int i = 0; i < turtleConnect.length; i++) {
			nbt.setBoolean("turtleConnect_" + i, turtleConnect[i]);
		}
		SimpleServiceLocator.openComputersProxy.handleWriteToNBT(this, nbt);

		NBTTagCompound logicNBT = new NBTTagCompound();
		logicController.writeToNBT(logicNBT);
		nbt.setTag("logicController", logicNBT);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (pipe != null) {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			if (trace.length > 2 && trace[2].getMethodName().equals("handle") && trace[2].getClassName()
					.equals("com.xcompwiz.lookingglass.network.packet.PacketTileEntityNBT")) {
				System.out.println("Prevented false data injection by LookingGlass");
				return;
			}
		}
		super.readFromNBT(nbt);

		if(!nbt.hasKey("pipeId") && MainProxy.isClient(world)) return;

		coreState.pipeId = nbt.getInteger("pipeId");
		pipe = LogisticsBlockGenericPipe.createPipe(Item.getItemById(coreState.pipeId));
		bindPipe();

		if (pipe != null) {
			pipe.readFromNBT(nbt);
		} else {
			LogisticsPipes.log.log(Level.WARN, "Pipe failed to load from NBT at {0},{1},{2}", getPos().getX(), getPos().getY(), getPos().getZ());
			deletePipe = true;
		}

		for (int i = 0; i < turtleConnect.length; i++) {
			turtleConnect[i] = nbt.getBoolean("turtleConnect_" + i);
		}
		SimpleServiceLocator.openComputersProxy.handleReadFromNBT(this, nbt);

		logicController.readFromNBT(nbt.getCompoundTag("logicController"));
	}

	public boolean canPipeConnect(TileEntity with, EnumFacing side) {
		if (MainProxy.isClient(world)) {
			//XXX why is this ever called client side, its not *used* for anything.
			return false;
		}
		if (with == null) {
			return false;
		}

		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			return false;
		}

		if (SimpleServiceLocator.ccProxy.isTurtle(with) && !turtleConnect[OrientationsUtil.getOrientationOfTilewithTile(this, with).ordinal()]) {
			return false;
		}

		if (SimpleServiceLocator.thermalDynamicsProxy.isBlockedSide(with, side.getOpposite())) {
			return false;
		}
		if (with instanceof LogisticsTileGenericPipe) {
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

	public boolean getTurtleConnect() {
		return SimpleServiceLocator.ccProxy.getTurtleConnect(this);
	}

	public void setTurtleConnect(boolean flag) {
		SimpleServiceLocator.ccProxy.setTurtleConnect(flag, this);
	}

	public int getLastCCID() {
		return SimpleServiceLocator.ccProxy.getLastCCID(this);
	}

	public ItemStack insertItem(EnumFacing dir, ItemStack stack) {
		int used = injectItem(stack, true, dir);
		if (used == stack.getCount()) {
			return ItemStack.EMPTY;
		} else {
			stack = stack.copy();
			stack.shrink(used);
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
		if (this.pipe != null && this.pipe.transport != null) {
			return this.pipe.transport.getPipeLength();
		}
		return 1;
	}

	public int injectItem(ItemStack payload, boolean doAdd, EnumFacing from) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport != null && isPipeConnectedCached(from)) {
			if (doAdd && MainProxy.isServer(getWorld())) {
				ItemStack leftStack = payload.copy();
				int lastIterLeft;
				do {
					lastIterLeft = leftStack.getCount();
					LPTravelingItem.LPTravelingItemServer travelingItem = SimpleServiceLocator.routedItemHelper.createNewTravelItem(leftStack);
					leftStack.setCount(pipe.transport.injectItem(travelingItem, from.getOpposite()));
				} while (leftStack.getCount() != lastIterLeft && leftStack.getCount() != 0);
				return payload.getCount() - leftStack.getCount();
			}
		}
		return 0;
	}

	public boolean isPipeConnectedCached(EnumFacing side) {
		if(MainProxy.isClient(this.world)) {
			return renderState.pipeConnectionMatrix.isConnected(side);
		} else {
			return pipeConnectionsBuffer[side.ordinal()];
		}
	}

	public boolean isOpaque() {
		return pipe.isOpaque();
	}

	/*
	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node node() {
		return node;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onConnect(Node node1) {}
	//public int redstoneInput = 0;
	//public int[] redstoneInputSide = new int[EnumFacing.VALUES.length];

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onDisconnect(Node node1) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onMessage(Message message) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Object[] invoke(String s, Context context, Arguments arguments) throws Exception {
		BaseWrapperClass object = (BaseWrapperClass) CCObjectWrapper.getWrappedObject(pipe, BaseWrapperClass.WRAPPER);
		object.isDirectCall = true;
		return CCObjectWrapper.createArray(object);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public String[] methods() {
		return new String[] { "getPipe" };
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public boolean canConnect(EnumFacing dir) {
		return !(this.getTile(dir) instanceof LogisticsTileGenericPipe) && !(this.getTile(dir) instanceof LogisticsSolidTileEntity);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node sidedNode(EnumFacing dir) {
		if (this.getTile(dir) instanceof LogisticsTileGenericPipe || this.getTile(dir) instanceof LogisticsSolidTileEntity) {
			return null;
		} else {
			return node();
		}
	}
	*/

	@Override
	public Object getOCNode() {
		return node;
	}

	public void initialize(CoreUnroutedPipe pipe) {
		blockType = getBlockType();

		if (pipe == null) {
			LogisticsPipes.log.log(Level.WARN, "Pipe failed to initialize at {0},{1},{2}, deleting", getPos().getX(), getPos().getY(), getPos().getZ());
			world.setBlockToAir(getPos());
			return;
		}

		this.pipe = pipe;

		for (EnumFacing o : EnumFacing.VALUES) {
			TileEntity tile = getTile(o);

			if (tile instanceof LogisticsTileGenericPipe) {
				((LogisticsTileGenericPipe) tile).scheduleNeighborChange();
			}
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
		packet.setPipe(pipe);
		packet.setStatePacketId(++statePacketId);

		return packet;
	}

	public void afterStateUpdated() {
		if (pipe == null && coreState.pipeId != 0) {
			initialize(LogisticsBlockGenericPipe.createPipe((Item) Item.REGISTRY.getObjectById(coreState.pipeId)));
		}

		if (pipe == null) {
			return;
		}

		world.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

		if (renderState.needsRenderUpdate()) {
			world.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
			renderState.clean();
		}
	}

	public void sendUpdateToClient() {
		sendClientUpdate = true;
	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null && pipe != null) {
			tileBuffer = TileBuffer.makeBuffer(world, pos, pipe.transport.delveIntoUnloadedChunks());
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
		if (this.pipe.isMultiBlock()) {
			return ((CoreMultiBlockPipe) this.pipe).getConnectedEndTile(to);
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
				pipeTDConnectionsBuffer[side.ordinal()] = SimpleServiceLocator.thermalDynamicsProxy.isItemDuct(t.getTile());
			} else {
				pipeTDConnectionsBuffer[side.ordinal()] = false;
			}
		}
		if (!Arrays.equals(pipeTDConnectionsBufferOld, pipeTDConnectionsBuffer)) {
			tdPart.connectionsChanged();
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == LogisticsPipes.FLUID_HANDLER_CAPABILITY && LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeFluidTransportLogistics && facing != null) {
			if(((PipeFluidTransportLogistics) pipe.transport).getIFluidHandler(facing) != null) return true;
		}
		if (bcCapProvider.hasCapability(capability, facing)) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == LogisticsPipes.FLUID_HANDLER_CAPABILITY && LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeFluidTransportLogistics && facing != null) {
			return (T)((PipeFluidTransportLogistics) pipe.transport).getIFluidHandler(facing);
		}
		if(bcCapProvider.hasCapability(capability, facing)) {
			return bcCapProvider.getCapability(capability, facing);
		}
		return super.getCapability(capability, facing);
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
		return false;
	}

	public Block getBlock() {
		return getBlockType();
	}


	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getTileEntity(pos) == this;
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
	public BlockPos getBlockPos() {
		return getPos();
	}

	/*
		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public IPipe getPipe() {
			return (IPipe) tilePart.getBCPipePart().getOriginal();
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public boolean canInjectItems(EnumFacing from) {
			return isPipeConnectedCached(from);
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public int x() {
			return xCoord;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public int y() {
			return yCoord;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public int z() {
			return zCoord;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public Block getNeighborBlock(EnumFacing dir) {
			return getBlock(dir);
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public TileEntity getNeighborTile(EnumFacing dir) {
			return getTile(dir);
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public IPipe getNeighborPipe(EnumFacing dir) {
			if (getTile(dir) instanceof IPipeTile) {
				return ((IPipeTile) getTile(dir)).getPipe();
			}
			return null;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public int getPipeColor() {
			return 0;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public PipePluggable getPipePluggable(EnumFacing direction) {
			if (tilePart.getBCPipePluggable(direction) == null) {
				return null;
			}
			return (PipePluggable) tilePart.getBCPipePluggable(direction).getOriginal();
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public boolean hasPipePluggable(EnumFacing direction) {
			return tilePart.getBCPipePluggable(direction) != null;
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public boolean hasBlockingPluggable(EnumFacing direction) {
			if (tilePart.getBCPipePluggable(direction) == null) {
				return false;
			}
			return tilePart.getBCPipePluggable(direction).isBlocking();
		}

		@Override
		@ModDependentMethod(modId = "BuildCraft|Transport")
		public ConnectOverride overridePipeConnection(PipeType pipeType, EnumFacing forgeDirection) {
			if (this.pipe != null && this.pipe.isFluidPipe()) {
				if (pipeType == PipeType.FLUID) {
					return ConnectOverride.CONNECT;
				}
			}
			return ConnectOverride.DEFAULT;
		}
	*/
	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		tdPart.setWorld_LP(world);
	}

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
			set.add(new DoubleCoordinatesType<>(getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1, CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
			renderBox = new AxisAlignedBB(set.getMinXD() - 1, set.getMinYD() - 1, set.getMinZD() - 1, set.getMaxXD() + 1, set.getMaxYD() + 1, set.getMaxZD() + 1);
		}
		return renderBox;
	}

	@Override
	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double traveled, double max,
			List<DoubleCoordinates> visited) {
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

	public boolean nonNull() {
		return Objects.nonNull(pipe);
	}

	@Override
	public boolean isMultiBlock() {
		return nonNull() && pipe.isMultiBlock();
	}

	@Override
	public Stream<TileEntity> getPartsOfPipe() {
		return this.subMultiBlock.stream().map(pos -> pos.getTileEntity(world));
	}

	public class CoreState implements IClientState {

		public int pipeId = -1;

		@Override
		public void writeData(LPDataOutput output) {
			output.writeInt(pipeId);

		}

		@Override
		public void readData(LPDataInput input) {
			pipeId = input.readInt();

		}
	}
}
