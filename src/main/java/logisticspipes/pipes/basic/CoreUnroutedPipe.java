package logisticspipes.pipes.basic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.api.ILPPipe;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
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
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public abstract class CoreUnroutedPipe implements IClientState, ILPPipe, ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];
	@Nullable
	public LogisticsTileGenericPipe container;
	public final PipeTransportLogistics transport;
	public final Item item;
	public DebugLogController debug = new DebugLogController(this);

	private boolean initialized = false;

	public CoreUnroutedPipe(PipeTransportLogistics transport, Item item) {
		this.transport = transport;
		this.item = item;
	}

	public void setTile(TileEntity tile) {
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

	/**
	 * Should return the textureindex used by the Pipe Item Renderer, as this is
	 * done client-side the default implementation might not work if your
	 * getTextureIndex(Orienations.Unknown) has logic. Then override this
	 */
	public int getIconIndexForItem() {
		return getIconIndex(null);
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
	 * @param direction - The direction for which the indexed should be rendered.
	 *                  Unknown for pipe center
	 * @return An index valid in the array returned by getTextureIcons()
	 */
	public abstract int getIconIndex(EnumFacing direction);

	public void updateEntity() {
		transport.updateEntity();
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

	public void onBlockRemoval() {}

	@Nullable
	public LogisticsTileGenericPipe getContainer() {
		return container;
	}

	public NonNullList<ItemStack> dropContents() {
		return transport.dropContents();
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
		if (container == null) return null;
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

	public final int getX() {
		return getPos().getX();
	}

	public final int getY() {
		return getPos().getY();
	}

	public final int getZ() {
		return getPos().getZ();
	}

	@Nonnull
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
		if (container == null) {
			return getClass().getName() + "(NO CONTAINER)";
		} else {
			return String.format("%s(%s)", getClass().getName(), container.getPos());
		}
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

	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double travled, double max, List<DoubleCoordinates> visited) {
		double lowest = Integer.MAX_VALUE;
		for (EnumFacing dir : EnumFacing.VALUES) {
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

				lowest = information.getDistanceTo(destinationint, dir.getOpposite(), ident, isActive, travled, Math.min(max, lowest), visited);

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

	public boolean hasSpecialPipeEndAt(EnumFacing dir) {
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

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

	protected void updateAdjacentCache() {}

	/**
	 * Triggers connection checks for routing.
	 */
	protected void triggerConnectionCheck() {}

	/**
	 * Called after reading data from NBT.
	 */
	public void finishInit() {}

	public boolean isPipeBlock() {
		return false;
	}
}
