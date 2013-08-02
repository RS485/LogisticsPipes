package logisticspipes.blocks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class LogisticsSignBlock extends BlockContainer {

	public static final int SignBlockID = 0;

	public LogisticsSignBlock(int par1) {
		super(par1, Material.iron);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public Icon getIcon(int par1, int par2) {
		return planks.getBlockTextureFromSide(par1);
	}

	/*
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		return true;
	}

	@Override
	public boolean isOpaqueCube() {
		return true;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return null;
    }
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
		TileEntity tile = par1IBlockAccess.getBlockTileEntity(par2, par3, par4);
		if (meta == SignBlockID && tile instanceof LogisticsSignTileEntity) {
			float var6 = 0.28125F;
			float var7 = 0.78125F;
			float var8 = 0.0F;
			float var9 = 1.0F;
			float var10 = 0.125F;
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			PipeItemsCraftingLogistics pipe = ((LogisticsSignTileEntity)tile).getAttachedSignOwnerPipe();
			if (pipe != null) {
				int disX = pipe.getX() - tile.xCoord;
				int disZ = pipe.getZ() - tile.zCoord;
				if (disZ > 0) {
					this.setBlockBounds(var8, var6, 1.0F - var10, var9, var7, 1.0F);
				} else if (disZ < 0) {
					this.setBlockBounds(var8, var6, 0.0F, var9, var7, var10);
				} else if (disX > 0) {
					this.setBlockBounds(1.0F - var10, var6, var8, 1.0F, var7, var9);
				} else if (disX < 0) {
					this.setBlockBounds(0.0F, var6, var8, var10, var7, var9);
				}
			}
		}
	}
*/

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new LogisticsSignTileEntity();
	}
	
	/*
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (meta == SignBlockID && tile instanceof LogisticsSignTileEntity) {
			PipeItemsCraftingLogistics pipe = ((LogisticsSignTileEntity)tile).getAttachedSignOwnerPipe();
			if(pipe != null) {
				pipe.blockActivated(world, x, y, z, player);
				return true;
			}
		}
		return false;
	}
	*/
	
	/*
	@Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
    	int meta = par1World.getBlockMetadata(par2, par3, par4);
		TileEntity tile = par1World.getBlockTileEntity(par2, par3, par4);
		if (meta == SignBlockID && tile instanceof LogisticsSignTileEntity) {
			PipeItemsCraftingLogistics pipe = ((LogisticsSignTileEntity) tile).getAttachedSignOwnerPipe();
			if(pipe != null) {
				pipe.removeRegisteredSign();
			}
		}
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
    */
	
	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}
}
