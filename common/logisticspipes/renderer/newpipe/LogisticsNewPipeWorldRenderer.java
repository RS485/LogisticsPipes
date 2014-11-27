package logisticspipes.renderer.newpipe;

import java.util.Arrays;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class LogisticsNewPipeWorldRenderer implements ISimpleBlockRenderingHandler {
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Tessellator tess = Tessellator.instance;
		TileEntity tile = world.getTileEntity(x, y, z);
		LogisticsTileGenericPipe pipeTile = (LogisticsTileGenericPipe) tile;
		PipeRenderState renderState = pipeTile.renderState;
		
		if(pipeTile.pipe instanceof PipeBlockRequestTable) {
			if(LogisticsPipeWorldRenderer.renderPass != 0) return false;
			IIconProvider icons = pipeTile.getPipeIcons();
			if (icons == null) return false;
			renderState.currentTexture = icons.getIcon(renderState.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			((LogisticsBlockGenericPipe)block).setRenderAllSides();
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			return true;
		}
		
		
		tess.addTranslation(0.00002F, 0.00002F, 0.00002F);
		SimpleServiceLocator.buildCraftProxy.pipeFacadeRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		SimpleServiceLocator.buildCraftProxy.pipePlugRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		SimpleServiceLocator.buildCraftProxy.pipeRobotStationRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		tess.addTranslation(-0.00002F, -0.00002F, -0.00002F);
		
		boolean solidSides[] = new boolean[6];
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LPPosition pos = new LPPosition((TileEntity)pipeTile);
			pos.moveForward(dir);
			Block blockSide = pos.getBlock(pipeTile.getWorldObj());
			if(blockSide == null || !blockSide.isSideSolid(pipeTile.getWorldObj(), pos.getX(), pos.getY(), pos.getZ(), dir.getOpposite()) || renderState.pipeConnectionMatrix.isConnected(dir)) {
			} else {
				solidSides[dir.ordinal()] = true;
			}
		}
		if(!Arrays.equals(solidSides, renderState.solidSidesCache)) {
			renderState.solidSidesCache = solidSides.clone();
			renderState.cachedRenderer = null;
		}
		
		block.setBlockBounds(0, 0, 0, 0, 0, 0);
		renderer.setRenderBoundsFromBlock(block);
		renderer.renderStandardBlock(block, x, y, z);
		
		return true;
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