package logisticspipes.blocks;

import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;

public class LogisticsBlockRenderer extends TileEntitySpecialRenderer
{
	CraftingSignRenderer signRenderer = new CraftingSignRenderer();
	
    public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8)
    {
    	if(par1TileEntity.worldObj.getBlockMetadata(par1TileEntity.xCoord, par1TileEntity.yCoord, par1TileEntity.zCoord) == LogisticsBlock.SignBlockID) {
    		signRenderer.renderTileEntityAt(par1TileEntity, par2, par4, par6, par8);
    	}
    }
}
