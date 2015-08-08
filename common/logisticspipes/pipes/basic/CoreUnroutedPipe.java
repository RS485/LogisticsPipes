package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;

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
import logisticspipes.renderer.IIconProvider;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.textures.Textures;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class CoreUnroutedPipe implements IClientState, ILPPipe, ILPCCTypeHolder {

	private Object ccType;

	public LogisticsTileGenericPipe container;
	public final PipeTransportLogistics transport;
	public final Item item;
	public DebugLogController debug = new DebugLogController(this);

	public IBCPipePart bcPipePart;

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

	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		CoreUnroutedPipe otherPipe;

		if (tile instanceof LogisticsTileGenericPipe) {
			otherPipe = ((LogisticsTileGenericPipe) tile).pipe;
			if (!LogisticsBlockGenericPipe.isFullyDefined(otherPipe)) {
				return false;
			}
		}

		return transport.canPipeConnect(tile, side);
	}

	/**
	 * Should return the textureindex used by the Pipe Item Renderer, as this is
	 * done client-side the default implementation might not work if your
	 * getTextureIndex(Orienations.Unknown) has logic. Then override this
	 */
	public int getIconIndexForItem() {
		return getIconIndex(ForgeDirection.UNKNOWN);
	}

	/**
	 * Should return the IIconProvider that provides icons for this pipe
	 *
	 * @return An array of icons
	 */
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return Textures.LPpipeIconProvider;
	}

	/**
	 * Should return the index in the array returned by GetTextureIcons() for a
	 * specified direction
	 *
	 * @param direction
	 *            - The direction for which the indexed should be rendered.
	 *            Unknown for pipe center
	 * @return An index valid in the array returned by getTextureIcons()
	 */
	public abstract int getIconIndex(ForgeDirection direction);

	public void updateEntity() {
		transport.updateEntity();

		if (MainProxy.isClient(getWorld())) {
			if (oldRendererState != (LogisticsPipes.getClientPlayerConfig().isUseNewRenderer() && !container.renderState.forceRenderOldPipe)) {
				oldRendererState = (LogisticsPipes.getClientPlayerConfig().isUseNewRenderer() && !container.renderState.forceRenderOldPipe);
				getWorld().markBlockForUpdate(getX(), getY(), getZ());
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

	protected void notifyBlockOfNeighborChange(ForgeDirection side) {
		container.getWorldObj().notifyBlockOfNeighborChange(container.xCoord + side.offsetX, container.yCoord + side.offsetY, container.zCoord + side.offsetZ, LogisticsPipes.LogisticsPipeBlock);
	}

	public void updateNeighbors(boolean needSelf) {
		if (needSelf) {
			container.getWorldObj().notifyBlockOfNeighborChange(container.xCoord, container.yCoord, container.zCoord, LogisticsPipes.LogisticsPipeBlock);
		}
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			notifyBlockOfNeighborChange(side);
		}
	}

	public void dropItem(ItemStack stack) {
		MainProxy.dropItems(container.getWorldObj(), stack, container.xCoord, container.yCoord, container.zCoord);
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
	public ForgeDirection getOpenOrientation() {
		int connectionsNum = 0;

		ForgeDirection targetOrientation = ForgeDirection.UNKNOWN;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (container.isPipeConnected(o)) {

				connectionsNum++;

				if (connectionsNum == 1) {
					targetOrientation = o;
				}
			}
		}

		if (connectionsNum > 1 || connectionsNum == 0) {
			return ForgeDirection.UNKNOWN;
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
		return container.getWorldObj();
	}

	public void onEntityCollidedWithBlock(Entity entity) {

	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection direction, boolean flag) {
		return canPipeConnect(tile, direction);
	}

	public boolean isSideBlocked(ForgeDirection side, boolean ignoreSystemDisconnection) {
		return false;
	}

	public final int getX() {
		return container.xCoord;
	}

	public final int getY() {
		return container.yCoord;
	}

	public final int getZ() {
		return container.zCoord;
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
		return super.toString() + " (" + getX() + ", " + getY() + ", " + getZ() + ")";
	}

	public LPPosition getLPPosition() {
		return new LPPosition(this);
	}

	public WorldUtil getWorldUtil() {
		return new WorldUtil(getWorld(), getX(), getY(), getZ());
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
			public boolean isSideDisconnected(ForgeDirection side) {
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
			public ForgeDirection[] getCombinedSneakyOrientation() {
				return null;
			}
		};
	}

	public double getDistanceTo(int destinationint, ForgeDirection ignore, ItemIdentifier ident, boolean isActive, double travled, double max, List<LPPosition> visited) {
		double lowest = Integer.MAX_VALUE;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
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
