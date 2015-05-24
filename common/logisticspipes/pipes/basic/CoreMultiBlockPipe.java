package logisticspipes.pipes.basic;

import java.util.List;

import codechicken.lib.render.CCRenderState;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.LPPositionSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class CoreMultiBlockPipe extends CoreUnroutedPipe {

	public CoreMultiBlockPipe(PipeMultiBlockTransportLogistics transport, Item item) {
		super(transport, item);
	}

	@Override
	public boolean isMultiBlock() {
		return true;
	}
	
	/**
	 * North Orientated
	 * @return Relative Positions
	 */
	public abstract LPPositionSet getSubBlocks();
	public abstract LPPositionSet getRotatedSubBlocks();

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("rawtypes")
	public abstract void addCollisionBoxesToList(List arraylist, AxisAlignedBB axisalignedbb);

	@SideOnly(Side.CLIENT)
	public abstract AxisAlignedBB getCompleteBox();

	public abstract ITubeOrientation getTubeOrientation(EntityPlayer player, int xPos, int zPos);

	public abstract float getPipeLength();

	public abstract ForgeDirection getExitForInput(ForgeDirection commingFrom);

	public abstract TileEntity getConnectedEndTile(ForgeDirection output);

	public abstract boolean renderNormalPipe();

	@SideOnly(Side.CLIENT)
	public abstract ISpecialPipeRenderer getSpecialRenderer();

	public abstract IHighlightPlacementRenderer getHighlightRenderer();
}
