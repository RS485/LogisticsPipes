package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.LPBlocks;
import logisticspipes.api.ILPPipe;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.textures.Textures;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public abstract class CoreUnroutedPipe implements IClientState, ILPPipe, ILPCCTypeHolder {

	private Object ccType;

	public LogisticsTileGenericPipe container;
	public final PipeTransportLogistics transport;
	public final Item item;
	public DebugLogController debug = new DebugLogController(this);

	private boolean initialized = false;

	private boolean oldRendererState;

	public CoreUnroutedPipe(PipeTransportLogistics transport, Item item) {
		this.transport = transport;
		this.item = item;
	}

	public void setTile(BlockEntity tile) {
		container = (LogisticsTileGenericPipe) tile;
		transport.setTile((LogisticsTileGenericPipe) tile);
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		return false;
	}

	public void onBlockPlaced() {
		transport.onBlockPlaced();
	}

	public void onBlockPlacedBy(EntityLivingBase placer) {}

	public void onNeighborBlockChange() {
		transport.onNeighborBlockChange();
	}

	public boolean canPipeConnect(BlockEntity tile, Direction side) {
		CoreUnroutedPipe otherPipe;

		if (tile instanceof LogisticsTileGenericPipe) {
			otherPipe = ((LogisticsTileGenericPipe) tile).pipe;
			if (!LogisticsBlockGenericPipe.isFullyDefined(otherPipe)) {
				return false;
			}
		}

		return transport.canPipeConnect(tile, side);
	}


	public void updateEntity() {
		transport.updateEntity();
	}

	public void writeToNBT(CompoundTag data) {
		transport.writeToNBT(data);
	}

	public void readFromNBT(CompoundTag data) {
		transport.readFromNBT(data);
	}

	public boolean needsInit() {
		return !initialized;
	}

	public void initialize() {
		transport.initialize();
		initialized = true;
	}

	protected void notifyBlockOfNeighborChange(Direction side) {
		container.getWorld().notifyNeighborsOfStateChange(CoordinateUtils.add(new DoubleCoordinates(container.getPos()), side).getBlockPos(), LPBlocks.pipe, true);
	}

	public void updateNeighbors(boolean needSelf) {
		if (needSelf) {
			container.getWorld().notifyNeighborsOfStateChange(container.getPos(), LPBlocks.pipe, true);
		}
		for (Direction side : Direction.values()) {
			notifyBlockOfNeighborChange(side);
		}
	}

	public void dropItem(ItemStack stack) {
		MainProxy.dropItems(container.getWorld(), stack, getX(), getY(), getZ());
	}

	public void onBlockRemoval() {
		if (getWorld().getWorldInfo().getGameType() != GameType.CREATIVE) {
			computeItemDrop().forEach(this::dropItem);
		}
	}

	public ArrayList<ItemStack> computeItemDrop() {
		return new ArrayList<>();
	}

	public LogisticsTileGenericPipe getContainer() {
		return container;
	}

	public List<ItemStack> dropContents() {
		return transport.dropContents();
	}

	/**
	 * If this pipe is open on one side, return it.
	 * /
	 public Direction getOpenOrientation() {
	 int connectionsNum = 0;

	 Direction targetOrientation = null;

	 for (Direction o : Direction.values()) {
	 if (container.isPipeConnectedCached(o)) {

	 connectionsNum++;

	 if (connectionsNum == 1) {
	 targetOrientation = o;
	 }
	 }
	 }

	 if (connectionsNum > 1 || connectionsNum == 0) {
	 return null;
	 }

	 return targetOrientation.getOpposite();
	 } */

	/**
	 * Called when TileGenericPipe.invalidate() is called
	 */
	public void invalidate() {}

	/**
	 * Called when TileGenericPipe.validate() is called
	 */
	public void validate() {}

	/**
	 * Called when TileGenericPipe.onChunkUnload is called
	 */
	public void onChunkUnload() {}

	public World getWorld() {
		return container.getWorld();
	}

	public void onEntityCollidedWithBlock(Entity entity) {

	}

	public boolean canPipeConnect(BlockEntity tile, Direction direction, boolean flag) {
		return canPipeConnect(tile, direction);
	}

	public boolean isSideBlocked(Direction side, boolean ignoreSystemDisconnection) {
		return false;
	}

	public final BlockPos getPos() {
		return container.getPos();
	}

	public boolean canBeDestroyed() {
		return true;
	}

	public boolean destroyByPlayer() {
		return false;
	}

	public void setPreventRemove(boolean flag) {}

	public boolean preventRemove() {
		return false;
	}

	@Override
	public boolean isRoutedPipe() {
		return false;
	}

	public boolean isFluidPipe() {
		return false;
	}

	@Override
	public void setCCType(Object type) {
		ccType = type;
	}

	@Override
	public Object getCCType() {
		return ccType;
	}

	public abstract int getTextureIndex();

	public void triggerDebug() {
		if (debug.debugThisPipe) {
			System.out.print("");
		}
	}

	public void addStatusInformation(List<StatusEntry> status) {}

	public boolean isOpaque() {
		return LPConfiguration.INSTANCE.getOpaquePipes();
	}

	@Override
	public String toString() {
		return String.format("%s (%d, %d, %d)", super.toString(), getPos().getX(), getPos().getY(), getPos().getZ());
	}

	public DoubleCoordinates getLPPosition() {
		return new DoubleCoordinates(this);
	}

	public IPipeUpgradeManager getUpgradeManager() {
		return new IPipeUpgradeManager() {

			@Override
			public boolean hasPowerPassUpgrade() {
				return false;
			}

			@Override
			public boolean hasRFPowerSupplierUpgrade() {
				return false;
			}

			@Override
			public boolean hasBCPowerSupplierUpgrade() {
				return false;
			}

			@Override
			public int getIC2PowerLevel() {
				return 0;
			}

			@Override
			public int getSpeedUpgradeCount() {
				return 0;
			}

			@Override
			public boolean isSideDisconnected(Direction side) {
				return false;
			}

			@Override
			public boolean hasCCRemoteControlUpgrade() {
				return false;
			}

			@Override
			public boolean hasCraftingMonitoringUpgrade() {
				return false;
			}

			@Override
			public boolean isOpaque() {
				return false;
			}

			@Override
			public boolean hasUpgradeModuleUpgrade() {
				return false;
			}

			@Override
			public boolean hasCombinedSneakyUpgrade() {
				return false;
			}

			@Override
			public Direction[] getCombinedSneakyOrientation() {
				return null;
			}
		};
	}

	public double getDistanceTo(UUID destination, Direction ignore, ItemVariant ident, boolean isActive, double traveled, double max, List<BlockPos> visited) {
		double lowest = Integer.MAX_VALUE;
		for (Direction dir : Direction.values()) {
			if (ignore == dir) {
				continue;
			}
			IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(container.getNextConnectedTile(dir));
			if (information != null) {
				DoubleCoordinates pos = new DoubleCoordinates(information);
				if (visited.contains(pos)) {
					continue;
				}
				visited.add(pos);

				lowest = information.getDistanceTo(destination, dir.getOpposite(), ident, isActive, traveled, Math.min(max, lowest), visited);

				visited.remove(pos);
			}
		}
		return lowest;
	}

	public boolean isMultiBlock() {
		return false;
	}

	public boolean actAsNormalPipe() {
		return true;
	}

	public boolean isHSTube() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public ISpecialPipeRenderer getSpecialRenderer() {
		return null;
	}

	public boolean hasSpecialPipeEndAt(Direction dir) {
		return false;
	}

	public DoubleCoordinates getItemRenderPos(float fPos, LPTravelingItem travelItem) {
		DoubleCoordinates pos = new DoubleCoordinates(0.5, 0.5, 0.5);
		if (fPos < 0.5) {
			if (travelItem.input == null) {
				return null;
			}
			if (!container.renderState.pipeConnectionMatrix.isConnected(travelItem.input.getOpposite())) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.input.getOpposite(), 0.5 - fPos);
		} else {
			if (travelItem.output == null) {
				return null;
			}
			if (!container.renderState.pipeConnectionMatrix.isConnected(travelItem.output)) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.output, fPos - 0.5);
		}
		return pos;
	}

	public double getBoxRenderScale(float fPos, LPTravelingItem travelItem) {
		double boxScale = 1;
		if (container.renderState.pipeConnectionMatrix.isTDConnected(travelItem.input.getOpposite())) {
			boxScale = (fPos * (1 - 0.65)) + 0.65;
		}
		if (container.renderState.pipeConnectionMatrix.isTDConnected(travelItem.output)) {
			boxScale = ((1 - fPos) * (1 - 0.65)) + 0.65;
		}
		if (container.renderState.pipeConnectionMatrix.isTDConnected(travelItem.input.getOpposite()) && container.renderState.pipeConnectionMatrix.isTDConnected(travelItem.output)) {
			boxScale = 0.65;
		}
		return boxScale;
	}

	public double getItemRenderPitch(float fPos, LPTravelingItem travelItem) {
		return 0;
	}

	public double getItemRenderYaw(float fPos, LPTravelingItem travelItem) {
		return 0;
	}

	public boolean isInitialized() {
		return container != null;
	}

	public abstract IHighlightPlacementRenderer getHighlightRenderer();

	public World getWorldForHUD() {
		return getWorld();
	}

	public boolean isMultipartAllowedInPipe() {
		return true;
	}
}
