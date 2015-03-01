package logisticspipes.proxy.buildcraft;

import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.render.FacadeBlockAccess;

public class LPFacadeBlockAccess extends FacadeBlockAccess {
	private final IBlockAccess world;
	private final ForgeDirection side;

	public LPFacadeBlockAccess(IBlockAccess world, ForgeDirection side) {
		super(world, side);
		this.world = world;
		this.side = side;
	}

	@Override
	public Block getBlock(int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof LogisticsTileGenericPipe) {
			PipePluggable p = ((LogisticsTileGenericPipe) tile).getPipePluggable(side);
			if (p instanceof IFacadePluggable) {
				return ((IFacadePluggable) p).getCurrentBlock();
			}
		}
		return super.getBlock(x, y, z);
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof LogisticsTileGenericPipe) {
			PipePluggable p = ((LogisticsTileGenericPipe) tile).getPipePluggable(side);
			if (p instanceof IFacadePluggable) {
				return ((IFacadePluggable) p).getCurrentMetadata();
			}
		}
		return super.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		if(world.getBlock(x, y, z) instanceof LogisticsBlockGenericPipe) {
			return false;
		}
		return super.isAirBlock(x, y, z);
	}
}
