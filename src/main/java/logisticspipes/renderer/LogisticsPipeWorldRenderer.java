/*
package logisticspipes.renderer;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePluggable;
import logisticspipes.renderer.newpipe.LogisticsNewPipeWorldRenderer;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;

public class LogisticsPipeWorldRenderer implements ISimpleBlockRenderingHandler {

	public static int renderPass = -1;

	private ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
	private LogisticsNewPipeWorldRenderer newRenderer = new LogisticsNewPipeWorldRenderer();

	public static boolean renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, LogisticsBlockGenericPipe block, LogisticsTileGenericPipe pipe, int x, int y, int z) {
		if (pipe.pipe instanceof PipeBlockRequestTable) {
			if (LogisticsPipeWorldRenderer.renderPass != 0) {
				return false;
			}
			PipeRenderState state = pipe.renderState;
			IIconProvider icons = pipe.getPipeIcons();
			if (icons == null) {
				return false;
			}
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(null));
			block.setRenderAllSides();
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
			return true;
		}

		// Here to prevent Minecraft from crashing when nothing renders on render pass zero
		// This is likely a bug, and has been submitted as an issue to the Forge team
		renderblocks.setRenderBounds(0, 0, 0, 0, 0, 0);
		renderblocks.renderStandardBlock(Blocks.stone, x, y, z);
		renderblocks.setRenderBoundsFromBlock(block);

		PipeRenderState state = pipe.renderState;
		IIconProvider icons = pipe.getPipeIcons();

		if (icons == null) {
			return false;
		}

		if (LogisticsPipeWorldRenderer.renderPass == 0) {
			int connectivity = state.pipeConnectionMatrix.getMask();
			float[] dim = new float[6];

			if (!pipe.isOpaque()) {
				// render the unconnected pipe faces of the center block (if any)
				if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
					LogisticsPipeWorldRenderer.resetToCenterDimensions(dim);
					state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(null));
					LogisticsPipeWorldRenderer.renderTwoWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);
				}

				// render the connecting pipe faces
				for (int dir = 0; dir < 6; dir++) {
					int mask = 1 << dir;
					if ((connectivity & mask) == 0) {
						continue; // no connection towards dir
					}

					// center piece offsets
					LogisticsPipeWorldRenderer.resetToCenterDimensions(dim);

					// extend block towards dir as it's connected to there
					dim[dir / 2] = dir % 2 == 0 ? 0 : LPConstants.BC_PIPE_MAX_POS;
					dim[dir / 2 + 3] = dir % 2 == 0 ? LPConstants.BC_PIPE_MIN_POS : 1;

					// the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5 -> 001111
					int renderMask = (3 << (dir / 2 * 2)) ^ 0x3f;

					//workaround for 1.6 texture weirdness, rotate texture for N/S/E/W connections
					renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = (dir < 2) ? 0 : 1;

					// render sub block
					state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(EnumFacing.VALUES[dir]));

					LogisticsPipeWorldRenderer.renderTwoWayBlock(renderblocks, block, x, y, z, dim, renderMask);
					renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = 0;
				}
			} else {
				// render the unconnected pipe faces of the center block (if any)
				if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
					LogisticsPipeWorldRenderer.resetToCenterDimensions(dim);

					//Render opaque Layer
					state.currentTexture = icons.getIcon(Textures.LOGISTICSPIPE_OPAQUE_TEXTURE.normal);
					LogisticsPipeWorldRenderer.renderOneWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);

					//Render Pipe Texture
					state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(null));
					LogisticsPipeWorldRenderer.renderOneWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);
				}

				// render the connecting pipe faces
				for (int dir = 0; dir < 6; dir++) {
					int mask = 1 << dir;
					if ((connectivity & mask) == 0) {
						continue; // no connection towards dir
					}

					// center piece offsets
					LogisticsPipeWorldRenderer.resetToCenterDimensions(dim);

					// extend block towards dir as it's connected to there
					dim[dir / 2] = dir % 2 == 0 ? 0 : LPConstants.BC_PIPE_MAX_POS;
					dim[dir / 2 + 3] = dir % 2 == 0 ? LPConstants.BC_PIPE_MIN_POS : 1;

					// the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5 -> 001111
					int renderMask = (3 << (dir / 2 * 2)) ^ 0x3f;

					//workaround for 1.6 texture weirdness, rotate texture for N/S/E/W connections
					renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = (dir < 2) ? 0 : 1;

					//Render opaque Layer
					state.currentTexture = icons.getIcon(Textures.LOGISTICSPIPE_OPAQUE_TEXTURE.normal);
					LogisticsPipeWorldRenderer.renderOneWayBlock(renderblocks, block, x, y, z, dim, 0x3f);

					// render sub block
					state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(EnumFacing.VALUES[dir]));
					LogisticsPipeWorldRenderer.renderOneWayBlock(renderblocks, block, x, y, z, dim, renderMask);
					renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = 0;
				}
			}
		}

		renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		for (EnumFacing dir : EnumFacing.VALUES) {
			if (pipe.tilePart.hasPipePluggable(dir)) {
				IBCPipePluggable p = pipe.tilePart.getBCPipePluggable(dir);
				p.renderPluggable(renderblocks, dir, LogisticsPipeWorldRenderer.renderPass, x, y, z);
			}
		}
		return true;
	}

	private static void resetToCenterDimensions(float[] dim) {
		for (int i = 0; i < 3; i++) {
			dim[i] = LPConstants.BC_PIPE_MIN_POS;
		}
		for (int i = 3; i < 6; i++) {
			dim[i] = LPConstants.BC_PIPE_MAX_POS;
		}
	}

	/**
	 * Render a block with normal and inverted vertex order so back face culling
	 * doesn't have any effect.
	 * /
	private static void renderOneWayBlock(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;

		block.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlock(block, x, y, z);
	}

	/**
	 * Render a block with normal and inverted vertex order so back face culling
	 * doesn't have any effect.
	 * /
	private static void renderTwoWayBlock(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;

		block.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlock(block, x, y, z);
		//flip back side texture
		renderblocks.flipTexture = true;
		block.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
		renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
		renderblocks.renderStandardBlock(block, x, y, z);
		renderblocks.flipTexture = false;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe pipeTile = (LogisticsTileGenericPipe) tile;
			SimpleServiceLocator.thermalDynamicsProxy.renderPipeConnections(pipeTile, renderer);
			if (config.isUseNewRenderer() && !pipeTile.renderState.forceRenderOldPipe) {
				return newRenderer.renderWorldBlock(world, x, y, z, block, modelId, renderer);
			}
			return LogisticsPipeWorldRenderer.renderPipe(renderer, world, (LogisticsBlockGenericPipe) block, pipeTile, x, y, z);
		} else if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			renderer.setRenderBounds(0, 0, 0, 0, 0, 0);
			renderer.renderStandardBlock(Blocks.stone, x, y, z);
			renderer.setRenderBoundsFromBlock(block);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getRenderId() {
		return LPConstants.pipeModel;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}
}
*/