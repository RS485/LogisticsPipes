package logisticspipes.pipes.basic;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILPPipe;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePart;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.UtilEnumFacing;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreUnroutedPipe implements IClientState, ILPPipe, ILPCCTypeHolder {

	private Object ccType;

	public LogisticsTileGenericPipe container;
	public final PipeTransportLogistics transport;
	public final Item item;
	public DebugLogController debug = new DebugLogController(this);
	@Getter
	public BlockPos pos;

	public IBCPipePart bcPipePart;
	@Getter
	public int xCoord = pos.getX();
	@Getter
	public int yCoord = pos.getY();
	@Getter
	public int zCoord = pos.getZ();
	public Block block;
	public IBlockAccess iBlockAccess;





	private boolean initialized = false;

	private boolean oldRendererState;

	public CoreUnroutedPipe(PipeTransportLogistics transport, Item item) {
		this.transport = transport;
		this.item = item;
	}

	public void setTile(TileEntity tile) {
		container = (LogisticsTileGenericPipe) tile;
		transport.setTile((LogisticsTileGenericPipe) tile);
		bcPipePart = ((LogisticsTileGenericPipe) tile).tilePart.getBCPipePart();
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		return false;
	}

	public void onBlockPlaced() {
		transport.onBlockPlaced();
	}

	public void onBlockPlacedBy(EntityLivingBase placer) {}

	public void onNeighborBlockChange(int blockId) {
		transport.onNeighborBlockChange(blockId);
	}

	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
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

		if (MainProxy.isClient(getWorld())) {
			if (oldRendererState != (LogisticsPipes.getClientPlayerConfig().isUseNewRenderer() && !container.renderState.forceRenderOldPipe)) {
				oldRendererState = (LogisticsPipes.getClientPlayerConfig().isUseNewRenderer() && !container.renderState.forceRenderOldPipe);
				getWorld().markBlockForUpdate(BlockPos.ORIGIN);
			}
		}
	}

	public void writeToNBT(NBTTagCompound data) {
		transport.writeToNBT(data);
	}

	public void readFromNBT(NBTTagCompound data) {
		transport.readFromNBT(data);
	}

	public boolean needsInit() {
		return !initialized;
	}

	public void initialize() {
		transport.initialize();
		initialized = true;
	}

	protected void notifyBlockOfNeighborChange(EnumFacing side) {
		container.getWorld().notifyBlockOfStateChange(container.pos ,LogisticsPipes.LogisticsPipeBlock);
	}

	public void updateNeighbors(boolean needSelf) {
		if (needSelf) {
			container.getWorld().notifyBlockOfStateChange(container.pos, LogisticsPipes.LogisticsPipeBlock);
		}
		for (EnumFacing side : UtilEnumFacing.VALID_DIRECTIONS) {
			notifyBlockOfNeighborChange(side);
		}
	}

	public void dropItem(ItemStack stack) {
		MainProxy.dropItems(container.getWorld(), stack, container.pos);
	}

	public void onBlockRemoval() {
		if (getWorld().getWorldInfo().getGameType() != GameType.CREATIVE) {
			for (ItemStack stack : computeItemDrop()) {
				dropItem(stack);
			}
		}
	}

	public ArrayList<ItemStack> computeItemDrop() {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();
		bcPipePart.addItemDrops(result);
		return result;
	}

	public LogisticsTileGenericPipe getContainer() {
		return container;
	}

	public List<ItemStack> dropContents() {
		return transport.dropContents();
	}

	/**
	 * If this pipe is open on one side, return it.
	 */
	public EnumFacing getOpenOrientation() {
		int connectionsNum = 0;

		EnumFacing targetOrientation = UtilEnumFacing.UNKNOWN;

		for (EnumFacing o : UtilEnumFacing.VALID_DIRECTIONS) {
			if (container.isPipeConnected(o)) {

				connectionsNum++;

				if (connectionsNum == 1) {
					targetOrientation = o;
				}
			}
		}

		if (connectionsNum > 1 || connectionsNum == 0) {
			return UtilEnumFacing.UNKNOWN;
		}

		return targetOrientation.getOpposite();
	}

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

	public boolean canPipeConnect(TileEntity tile, EnumFacing direction, boolean flag) {
		return canPipeConnect(tile, direction);
	}

	public boolean isSideBlocked(EnumFacing side, boolean ignoreSystemDisconnection) {
		return false;
	}

	public final BlockPos getblockpos() {
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
		return Configs.OPAQUE;
	}

	@Override
	public String toString() {
		return super.toString() + pos.toString() ;
	}

	public LPPosition getLPPosition() {
		return new LPPosition(this);
	}

	public WorldUtil getWorldUtil() {
		return new WorldUtil(getWorld(), getblockpos());
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
			public int getIC2PowerLevel() {
				return 0;
			}

			@Override
			public int getSpeedUpgradeCount() {
				return 0;
			}

			@Override
			public boolean isSideDisconnected(EnumFacing side) {
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
			public EnumFacing[] getCombinedSneakyOrientation() {
				return null;
			}
		};
	}

	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double travled, double max, List<LPPosition> visited) {
		double lowest = Integer.MAX_VALUE;
		for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
			if (ignore == dir) {
				continue;
			}
			IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(container.getTile(dir));
			if (information != null) {
				LPPosition pos = new LPPosition(information);
				if (visited.contains(pos)) {
					continue;
				}
				visited.add(pos);

				lowest = information.getDistanceTo(destinationint, dir.getOpposite(), ident, isActive, travled, Math.min(max, lowest), visited);

				visited.remove(pos);
			}
		}
		return lowest;
	}

	public boolean canHoldBCParts() {
		return true;
	}

	public boolean isInitialized() {
		return container != null;
	}
}
