package logisticspipes.renderer;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.PipeRendererWorld;

public class LogisticsPipeWorldRenderer extends PipeRendererWorld {

	@Override
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, BlockGenericPipe block, TileGenericPipe tile, int x, int y, int z) {
		PipeRenderState renderState = tile.renderState;
		if(tile instanceof LogisticsTileGenericPipe && tile.pipe instanceof PipeBlockRequestTable) {
			IIconProvider icons = tile.getPipeIcons();
			if (icons == null) return;
			renderState.currentTexture = icons.getIcon(renderState.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
			return;
		}
		super.renderPipe(renderblocks, iblockaccess, block, tile, x, y, z);
	}
}
