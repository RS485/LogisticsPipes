package logisticspipes.pipes.basic;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import static logisticspipes.pipes.basic.LogisticsBlockGenericPipe.PIPE_CONN_BB;
import lombok.Getter;
import org.apache.logging.log4j.Level;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILPPipe;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.LogisticsSolidTileEntity;
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
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.asm.BaseWrapperClass;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.LogisticsTileRenderController;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.ReflectionHelper;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.StackTraceUtil;
import logisticspipes.utils.StackTraceUtil.Info;
import logisticspipes.utils.TileBuffer;
import network.rs485.logisticspipes.connection.PipeInventoryConnectionChecker;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class LogisticsTileGenericPipe extends BlockEntity
		implements Tickable, ILPPipeTile, IPipeInformationProvider, ILogicControllerTile {

	public static PipeInventoryConnectionChecker pipeInventoryConnectionChecker = new PipeInventoryConnectionChecker();

	public int statePacketId = 0;
	public final PipeRenderState renderState;
	public final CoreState coreState = new CoreState();
	public Set<BlockPos> subMultiBlock = new HashSet<>();
	public boolean[] turtleConnect = new boolean[7];
	public LogicController logicController = new LogicController();
	public boolean[] pipeConnectionsBuffer = new boolean[6];
	public boolean[] pipeBCConnectionsBuffer = new boolean[6];
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
	private Box renderBox;
	private EnumMap<Direction, ItemInsertionHandler> itemInsertionHandlers;

	public LogisticsTileGenericPipe(BlockEntityType<?> type) {
		super(type);
		itemInsertionHandlers = new EnumMap<>(Direction.class);
		for (Direction face : Direction.values()) {
			itemInsertionHandlers.put(face, new ItemInsertionHandler(this, face));
		}
		renderState = new PipeRenderState();
	}

	@Override
	public void markRemoved() {
		super.markRemoved();
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
	public void cancelRemoval() {
		super.cancelRemoval();
		initialized = false;
		tileBuffer = null;
		bindPipe();
		if (pipe != null) {
			pipe.validate();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (pipe != null) {
			pipe.onChunkUnload();
		}
		SimpleServiceLocator.openComputersProxy.handleChunkUnload(this);
		tdPart.onChunkUnload();
	}

	@Override
	public void update() {
		imcmpltgpCompanion.update();
		final Info superDebug = StackTraceUtil.addSuperTraceInformation(() -> "Time: " + getWorld().getWorldTime());
		final Info debug = StackTraceUtil.addTraceInformation(() -> "(" + getX() + ", " + getY() + ", " + getZ() + ")", superDebug);
		if (sendInitPacket && !getWorld().isClient()) {
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

			if (MainProxy.isServer(world)) {
				MainProxy.sendPacketToAllWatchingChunk(this, PacketHandler.getPacket(PipeSolidSideCheck.class).setTilePos(this));
			}
		}

		// Sideblocks need to be checked before this
		// Network needs to be after this

		if (refreshRenderState) {
			refreshRenderState();

			if (renderState.isDirty()) {
				renderState.clean();
				sendUpdateToClient();
			}

			refreshRenderState = false;
		}

		if (sendClientUpdate) {
			sendClientUpdate = false;
			statePacketId++;
			MainProxy.sendPacketToAllWatchingChunk(this, getLPDescriptionPacket());
		}

		getRenderController().onUpdate();
		if (!addedToNetwork) {
			addedToNetwork = true;
			SimpleServiceLocator.openComputersProxy.addToNetwork(this);
		}
		debug.end();
	}

	private void refreshRenderState() {
		// Pipe connections;
		for (Direction o : Direction.values()) {
			renderState.pipeConnectionMatrix.setConnected(o, pipeConnectionsBuffer[o.ordinal()]);
			renderState.pipeConnectionMatrix.setBCConnected(o, pipeBCConnectionsBuffer[o.ordinal()]);
			renderState.pipeConnectionMatrix.setTDConnected(o, pipeTDConnectionsBuffer[o.ordinal()]);
		}
		// Pipe Textures
		for (int i = 0; i < 7; i++) {
			Direction o = Direction.getFront(i);
			renderState.textureMatrix.setIconIndex(o, pipe.getIconIndex(o));
		}
		// New Pipe Texture States
		renderState.textureMatrix.refreshStates(pipe);
	}

	@Override
	public boolean isMultipartAllowedInPipe() {
		return !isMultiBlock() && (pipe == null || pipe.isMultipartAllowedInPipe());
	}

	@Override
	public CompoundTag getUpdateTag() {
		sendInitPacket = true;
		CompoundTag nbt = super.getUpdateTag();
		try {
			PacketHandler.addPacketToNBT(getLPDescriptionPacket(), nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nbt;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleUpdateTag(CompoundTag tag) {
		PacketHandler.queueAndRemovePacketFromNBT(tag);
		super.handleUpdateTag(tag);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		CompoundTag nbt = new CompoundTag();
		SPacketUpdateTileEntity superPacket = super.getUpdatePacket();
		if (superPacket != null) {
			nbt.setTag("LogisticsPipes:SuperUpdatePacket", ReflectionHelper.getPrivateField(SPacketUpdateTileEntity.class, superPacket, "nbt", "field_148860_e"));
		}
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
		if (packet.getNbtCompound().hasKey("LogisticsPipes:SuperUpdatePacket")) {
			super.onDataPacket(net, new SPacketUpdateTileEntity(getPos(), 0, packet.getNbtCompound().getCompoundTag("LogisticsPipes:SuperUpdatePacket")));
		}
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
		new WorldCoordinatesWrapper(this).allNeighborTileEntities()
				.filter(adjacent -> SimpleServiceLocator.ccProxy.isTurtle(adjacent.getBlockEntity()))
				.forEach(adjacent -> connected[adjacent.getDirection().ordinal()] = true);
		for (int i = 0; i < 6; i++) {
			if (!connected[i]) {
				turtleConnect[i] = false;
			}
		}
	}

	/* IPipeInformationProvider */

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt = super.writeToNBT(nbt);

		/*
		for (int i = 0; i < Direction.values().length; i++) {
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

		CompoundTag logicNBT = new CompoundTag();
		logicController.writeToNBT(logicNBT);
		nbt.setTag("logicController", logicNBT);
		return nbt;
	}

	@Override
	public void readFromNBT(CompoundTag nbt) {
		if (pipe != null) {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			if (trace.length > 2 && trace[2].getMethodName().equals("handle") && trace[2].getClassName()
					.equals("com.xcompwiz.lookingglass.network.packet.PacketTileEntityNBT")) {
				System.out.println("Prevented false data injection by LookingGlass");
				return;
			}
		}
		super.readFromNBT(nbt);

		if (!nbt.hasKey("pipeId") && MainProxy.isClient(world)) return;

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

	public boolean canPipeConnect(BlockEntity with, Direction side) {
		if (MainProxy.isClient(world)) {
			// XXX why is this ever called client side, its not *used* for anything.
			return false;
		}
		if (with == null) {
			return false;
		}

		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			return false;
		}

		if (SimpleServiceLocator.ccProxy.isTurtle(with) && !turtleConnect[OrientationsUtil.getOrientationOfTileWithTile(this, with).ordinal()]) {
			return false;
		}

		AxisAlignedBB aabb = PIPE_CONN_BB.get(side.getIndex());
		if (SimpleServiceLocator.mcmpProxy.checkIntersectionWith(this, aabb)) {
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

			AxisAlignedBB aabbB = PIPE_CONN_BB.get(side.getOpposite().getIndex());
			if (SimpleServiceLocator.mcmpProxy.checkIntersectionWith((LogisticsTileGenericPipe) with, aabbB)) {
				return false;
			}

		}
		return pipe.canPipeConnect(with, side);
	}

	public ItemStack insertItem(Direction from, ItemStack stack) {
		int used = injectItem(stack, true, from);
		if (used == stack.getCount()) {
			return ItemStack.EMPTY;
		} else {
			stack = stack.copy();
			stack.shrink(used);
			return stack;
		}
	}

	public void addLaser(Direction dir, float length, int color, boolean reverse, boolean renderBall) {
		getRenderController().addLaser(dir, length, color, reverse, renderBall);
	}

	public void removeLaser(Direction dir, int color, boolean isBall) {
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

	public BlockEntity getTile() {
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
	public boolean isOneWayPipe() {
		return false;
	}

	@Override
	public boolean isOutputOpen(Direction direction) {
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
	public boolean canConnect(BlockEntity to, Direction direction, boolean flag) {
		if (pipe == null) {
			return false;
		}
		if (direction != null) {
			AxisAlignedBB aabb = PIPE_CONN_BB.get(direction.getIndex());
			if (SimpleServiceLocator.mcmpProxy.checkIntersectionWith(this, aabb)) {
				return false;
			}
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

	@Override
	public double getDistanceWeight() {
		if (this.pipe != null && this.pipe.transport != null) {
			return this.pipe.transport.getDistanceWeight();
		}
		return 1;
	}

	public int injectItem(ItemStack payload, boolean doAdd, Direction from) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport != null && isPipeConnectedCached(from)) {
			if (doAdd && !getWorld().isClient()) {
				ItemStack leftStack = payload.copy();
				int lastIterLeft;
				do {
					lastIterLeft = leftStack.getCount();
					LPTravelingItem.LPTravelingItemServer travelingItem = RoutedItemHelper.INSTANCE.createNewTravelItem(leftStack);
					leftStack.setCount(pipe.transport.injectItem(travelingItem, from.getOpposite()));
				} while (leftStack.getCount() != lastIterLeft && leftStack.getCount() != 0);
				return payload.getCount() - leftStack.getCount();
			}
		}
		return 0;
	}

	public boolean isPipeConnectedCached(Direction side) {
		if (MainProxy.isClient(this.world)) {
			return renderState.pipeConnectionMatrix.isConnected(side);
		} else {
			return pipeConnectionsBuffer[side.ordinal()];
		}
	}

	public boolean isOpaque() {
		return pipe.isOpaque();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node node() {
		return node;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onConnect(Node node1) {}
	// public int redstoneInput = 0;
	// public int[] redstoneInputSide = new int[Direction.values().length];

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onDisconnect(Node node1) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onMessage(Message message) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Object[] invoke(String s, Context context, Arguments arguments) {
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
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node sidedNode(Direction side) {
		if (this.getTile(side) instanceof LogisticsTileGenericPipe || this.getTile(side) instanceof LogisticsSolidTileEntity) {
			return null;
		} else {
			return node();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public boolean canConnect(Direction side) {
		return !(this.getTile(side) instanceof LogisticsTileGenericPipe) && !(this.getTile(side) instanceof LogisticsSolidTileEntity);
	}

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

		/*
		for (Direction o : Direction.values()) {
			BlockEntity tile = getTile(o);

			if (tile instanceof LogisticsTileGenericPipe) {
				((LogisticsTileGenericPipe) tile).scheduleNeighborChange();
			}
		}*/

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
		packet.setStatePacketId(statePacketId);

		return packet;
	}

	public void afterStateUpdated() {
		if (pipe == null && coreState.pipeId != 0) {
			initialize(LogisticsBlockGenericPipe.createPipe(Item.REGISTRY.getObjectById(coreState.pipeId)));
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

	public void blockCreated(Direction from, Block block, BlockEntity tile) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			cache[from.getOpposite().ordinal()].set(block, tile);
		}
	}

	@Override
	public BlockEntity getNextConnectedTile(Direction to) {
		if (this.pipe.isMultiBlock()) {
			return ((CoreMultiBlockPipe) this.pipe).getConnectedEndTile(to);
		}
		return getTile(to, false);
	}

	public BlockEntity getTile(Direction to) {
		return getTile(to, false);
	}

	public BlockEntity getTile(Direction to, boolean force) {
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

	public Block getBlock(Direction to) {
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

		for (Direction side : Direction.values()) {
			TileBuffer t = cache[side.ordinal()];
			t.refresh();

			pipeConnectionsBuffer[side.ordinal()] = canPipeConnect(t.getTile(), side);
			if (pipeConnectionsBuffer[side.ordinal()]) {
				pipeBCConnectionsBuffer[side.ordinal()] = SimpleServiceLocator.buildCraftProxy.isBuildCraftPipe(t.getTile());
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

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
		if (capability == LogisticsPipes.FLUID_HANDLER_CAPABILITY && LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeFluidTransportLogistics && facing != null) {
			if (((PipeFluidTransportLogistics) pipe.transport).getIFluidHandler(facing) != null) return true;
		}
		if (capability == LogisticsPipes.ITEM_HANDLER_CAPABILITY) {
			if (facing == null) {
				return false;
			}
			BlockEntity tile = getTile(facing);
			if (tile != null) {
				if (pipeInventoryConnectionChecker.shouldLPProvideInventoryTo(tile)) {
					return true;
				}
			}
		}
		if (bcCapProvider.hasCapability(capability, facing)) {
			return true;
		}
		if (imcmpltgpCompanion.hasCapability(capability, facing)) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (capability == LogisticsPipes.FLUID_HANDLER_CAPABILITY && LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport instanceof PipeFluidTransportLogistics && facing != null) {
			return (T) ((PipeFluidTransportLogistics) pipe.transport).getIFluidHandler(facing);
		}
		if (capability == LogisticsPipes.ITEM_HANDLER_CAPABILITY) {
			if (facing == null) {
				return null;
			}
			BlockEntity tile = getTile(facing);
			if (tile != null) {
				if (pipeInventoryConnectionChecker.shouldLPProvideInventoryTo(tile)) {
					return (T) itemInsertionHandlers.get(facing);
				}
			}
		}
		if (bcCapProvider.hasCapability(capability, facing)) {
			return bcCapProvider.getCapability(capability, facing);
		}
		if (imcmpltgpCompanion.hasCapability(capability, facing)) {
			return imcmpltgpCompanion.getCapability(capability, facing);
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

	public Block getBlock() {
		return getBlockType();
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getBlockEntity(pos) == this;
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

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		tdPart.setWorld_LP(world);
	}

	@Nonnull
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
	public double getDistanceTo(UUID destination, Direction ignore, ItemVariant ident, boolean isActive, double traveled, double max,
			List<BlockPos> visited) {
		if (pipe == null || traveled > max) {
			return Integer.MAX_VALUE;
		}
		double result = pipe.getDistanceTo(destination, ignore, ident, isActive, traveled + getDistance(), max, visited);
		if (result == Integer.MAX_VALUE) {
			return result;
		}
		return result + (int) getDistance();
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, BlockEntity from) {
		if (LogisticsBlockGenericPipe.isValid(pipe) && pipe.transport != null) {
			pipe.transport.injectItem(item, item.output);
			return true;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(Direction side) {
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
	public Stream<BlockEntity> getPartsOfPipe() {
		return this.subMultiBlock.stream().map(pos -> getWorld().getBlockEntity(pos));
	}

	public static class CoreState implements IClientState {

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
