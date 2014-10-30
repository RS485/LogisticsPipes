package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILPPipe;
import logisticspipes.interfaces.IClientState;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePart;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.transport.PipeTransportLogistics;
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
	
	public IBCPipePart bcPipePart;

	public boolean internalUpdateScheduled = false;
	private boolean initialized = false;

	public CoreUnroutedPipe(PipeTransportLogistics transport, Item item) {
		this.transport = transport;
		this.item = item;
	}

	public void setTile(TileEntity tile) {
		this.container = (LogisticsTileGenericPipe) tile;
		transport.setTile((LogisticsTileGenericPipe) tile);
		bcPipePart = SimpleServiceLocator.buildCraftProxy.getBCPipePart(this.container);
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		return false;
	}

	public void onBlockPlaced() {
		transport.onBlockPlaced();
	}

	public void onBlockPlacedBy(EntityLivingBase placer) {
	}

	public void onNeighborBlockChange(int blockId) {
		transport.onNeighborBlockChange(blockId);

		updateSignalState();
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
	public abstract IIconProvider getIconProvider();

	/**
	 * Should return the index in the array returned by GetTextureIcons() for a
	 * specified direction
	 *
	 * @param direction - The direction for which the indexed should be
	 * rendered. Unknown for pipe center
	 *
	 * @return An index valid in the array returned by getTextureIcons()
	 */
	public abstract int getIconIndex(ForgeDirection direction);

	public void updateEntity() {
		bcPipePart.updateEntity();
		transport.updateEntity();

		if (internalUpdateScheduled) {
			internalUpdate();
			internalUpdateScheduled = false;
		}
		bcPipePart.updateGate();
	}

	private void internalUpdate() {
		updateSignalState();
	}

	public void writeToNBT(NBTTagCompound data) {
		transport.writeToNBT(data);
		bcPipePart.writeToNBT(data);
	}

	public void readFromNBT(NBTTagCompound data) {
		transport.readFromNBT(data);
		bcPipePart.readFromNBT(data);
	}

	public boolean needsInit() {
		return !initialized;
	}

	public void initialize() {
		transport.initialize();
		updateSignalState();
		initialized = true;
	}

	public void updateSignalState() {
		bcPipePart.updateSignalState();
	}

	public boolean canConnectRedstone() {
		for (ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if (this.bcPipePart.hasGate(dir)) {
				return true;
			}
		}
		return false;
	}

	public int isPoweringTo(int side) {
		return bcPipePart.isPoweringTo(side);
	}

	public int isIndirectlyPoweringTo(int l) {
		return isPoweringTo(l);
	}

	public void randomDisplayTick(Random random) {
	}

	public boolean isWired() {
		return bcPipePart.isWired();
	}

	@Deprecated
	public boolean hasGate() {
		return bcPipePart.hasGate();
	}

	public boolean hasGate(ForgeDirection side) {
		return container.tilePart.hasGate(side);
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
	
	public void resetGate() {
		bcPipePart.resetGate();
		container.scheduleRenderUpdate();
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
	public void invalidate() {
	}

	/**
	 * Called when TileGenericPipe.validate() is called
	 */
	public void validate() {
	}

	/**
	 * Called when TileGenericPipe.onChunkUnload is called
	 */
	public void onChunkUnload() {
	}

	public World getWorld() {
		return container.getWorldObj();
	}

	public void onEntityCollidedWithBlock(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	public boolean canPipeConnect(TileEntity tile, ForgeDirection direction, boolean flag) {
		return canPipeConnect(tile, direction);
	}

	public final int getX() {
		return this.container.xCoord;
	}

	public final int getY() {
		return this.container.yCoord;
	}

	public final int getZ() {
		return this.container.zCoord;
	}

	public boolean canBeDestroyed() {
		return false;
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

	@Override
	public void setCCType(Object type) {
		ccType = type;
	}

	@Override
	public Object getCCType() {
		return ccType;
	}
	
	public abstract int getTextureIndex();
}
