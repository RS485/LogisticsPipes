/*
package logisticspipes.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.renderer.newpipe.LogisticsNewSolidBlockWorldRenderer;

public class LogisticsSolidBlockWorldRenderer implements ISimpleBlockRenderingHandler {

	public ClientConfiguration config;
	LogisticsNewSolidBlockWorldRenderer newRenderer = new LogisticsNewSolidBlockWorldRenderer();

	public LogisticsSolidBlockWorldRenderer() {
		config = LogisticsPipes.getClientPlayerConfig();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		int tmpID = LPConstants.solidBlockModel;
		if (config.isUseNewRenderer()) {
			newRenderer.renderInventoryBlock(block, metadata);
		} else {
			LPConstants.solidBlockModel = 0;
			renderer.renderBlockAsItem(block, metadata, 1.0F);
			LPConstants.solidBlockModel = tmpID;
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof LogisticsSolidTileEntity) {
			if (config.isUseNewRenderer()) {
				newRenderer.renderWorldBlock(world, (LogisticsSolidTileEntity) tile, renderer, x, y, z);
				return true;
			} else {
				block.setBlockBounds(0, 0, 0, 1, 1, 1);
				renderer.setRenderBoundsFromBlock(block);
				switch (((LogisticsSolidTileEntity) tile).getRotation()) {
					case 0:
						renderer.uvRotateTop = 2;
						break;
					case 1:
						renderer.uvRotateTop = 1;
						break;
					case 2:
						renderer.uvRotateTop = 0;
						break;
					case 3:
						renderer.uvRotateTop = 3;
						break;
				}
				renderer.renderStandardBlock(block, x, y, z);
				renderer.uvRotateTop = 0;
				return true;
			}
		} else if(world.getBlockMetadata(x, y, z) == LogisticsSolidBlock.LOGISTICS_BLOCK_FRAME) {
			if (config.isUseNewRenderer()) {
				newRenderer.renderWorldBlock(world, null, renderer, x, y, z);
				return true;
			} else {
				block.setBlockBounds(0, 0, 0, 1, 1, 1);
				renderer.setRenderBoundsFromBlock(block);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.uvRotateTop = 0;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return LPConstants.solidBlockModel;
	}
}
*/