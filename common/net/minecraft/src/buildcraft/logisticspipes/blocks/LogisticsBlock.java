package net.minecraft.src.buildcraft.logisticspipes.blocks;

import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;

public class LogisticsBlock extends BlockContainer {

	public LogisticsBlock(int par1) {
		super(par1, Material.iron);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	//public int getBlockTexture(IBlockAccess par1IBlockAccess, int par2,int par3, int par4, int par5) {
	//	return this.getBlockTextureFromSideAndMetadata(par5, par1IBlockAccess.getBlockMetadata(par2, par3, par4));
	//}

	public int getBlockTextureFromSideAndMetadata(int par1, int par2) {
		return par2 == 0 ? 4 /* SIGN */: 0 /* NONE */;
	}

	public int getBlockTextureFromSide(int par1) {
		return 0 /* NONE */;
	}

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        return true;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

	public int getRenderType()
    {
        return -1;
    }
	
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		float var6 = 0.28125F;
		float var7 = 0.78125F;
		float var8 = 0.0F;
		float var9 = 1.0F;
		float var10 = 0.125F;
		if(APIProxy.isServerSide()) {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		} else {
			//Individual
			this.setBlockBounds(var8, var6, 1.0F - var10, var9, var7, 1.0F);
		}
	}

	@Override
	public TileEntity getBlockEntity() {
		return new LogisticsTileEntiy();
	}
}
