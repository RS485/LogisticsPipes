package logisticspipes.renderer;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.render.PipeRendererWorld;

public class LogisticsPipeWorldRenderer extends PipeRendererWorld {

	@Override
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, BlockGenericPipe block, IPipeRenderState renderState, int x, int y, int z) {
		if(renderState instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)renderState).pipe instanceof PipeBlockRequestTable) {
			PipeRenderState state = renderState.getRenderState();
			IIconProvider icons = renderState.getPipeIcons();
			if (icons == null) return;
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
			return;
		}
		super.renderPipe(renderblocks, iblockaccess, block, renderState, x, y, z);
	}
	
}
