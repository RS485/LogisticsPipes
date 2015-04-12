package logisticspipes.renderer.newpipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import logisticspipes.LPConstants;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePluggable;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewSolidBlockWorldRenderer.BlockRotation;
import logisticspipes.renderer.newpipe.LogisticsNewSolidBlockWorldRenderer.CoverSides;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class LogisticsNewPipeWorldRenderer implements ISimpleBlockRenderingHandler {
	
	private Map<BlockRotation, CCModel> requestBlock = null;
	
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
			if(requestBlock == null || true) {
				requestBlock = new HashMap<BlockRotation, CCModel>();
				for(BlockRotation rot:BlockRotation.values()) {
					requestBlock.put(rot, LogisticsNewSolidBlockWorldRenderer.block.get(rot).copy().apply(new Scale(0.999)).apply(new Translation(0.0005, 0.0005, 0.0005)));
				}
			}
			
			renderState.currentTexture = icons.getIcon(renderState.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			((LogisticsBlockGenericPipe)block).setRenderAllSides();
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			
			CCRenderState.reset();
			CCRenderState.useNormals = true;
			CCRenderState.alphaOverride = 0xff;
			
			BlockRotation rotation = BlockRotation.getRotation(((PipeBlockRequestTable)pipeTile.pipe).getRotation());
			
			int brightness = new LPPosition(x, y, z).getBlock(world).getMixedBrightnessForBlock(world, x, y, z);
			
			tess.setColorOpaque_F(1F, 1F, 1F);
			tess.setBrightness(brightness);
			
			IconTransformation icon = new IconTransformation(Textures.LOGISTICS_REQUEST_TABLE_NEW);
			
			requestBlock.get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
			
			for(CoverSides side:CoverSides.values()) {
				if(!pipeTile.renderState.pipeConnectionMatrix.isConnected(side.getDir(rotation))) {
					LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
					LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side).get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
				}
			}
			
			return true;
		}
		
		
		tess.addTranslation(0.00002F, 0.00002F, 0.00002F);
		renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		SimpleServiceLocator.buildCraftProxy.pipeFacadeRenderer(renderer, (LogisticsBlockGenericPipe) block, pipeTile, x, y, z, LogisticsPipeWorldRenderer.renderPass);
		
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (pipeTile.tilePart.hasPipePluggable(dir)) {
				IBCPipePluggable p = pipeTile.tilePart.getBCPipePluggable(dir);
				p.renderPluggable(renderer, dir, LogisticsPipeWorldRenderer.renderPass, x, y, z);
			}
		}
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
		
		block.setBlockBounds(0, 0, 0, 1, 1, 1);
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