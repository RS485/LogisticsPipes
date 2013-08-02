package logisticspipes.renderer;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.IPipeRenderState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.render.PipeWorldRenderer;

public class LogisticsPipeWorldRenderer extends PipeWorldRenderer {

	@Override
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, Block block, IPipeRenderState renderState, int x, int y, int z) {
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
