package logisticspipes.blocks;

import logisticspipes.blocks.LogisticsBlock;
import net.minecraft.src.BlockLog;
import net.minecraft.src.ModelSign;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.TileEntitySpecialRenderer;

import org.lwjgl.opengl.GL11;

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
