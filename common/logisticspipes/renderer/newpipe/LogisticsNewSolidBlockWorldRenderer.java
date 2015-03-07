package logisticspipes.renderer.newpipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.LPPosition;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.render.uv.UVScale;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;

public class LogisticsNewSolidBlockWorldRenderer {

	enum CoverSides {
		DOWN(ForgeDirection.DOWN, "D"),
		NORTH(ForgeDirection.NORTH, "N"),
		SOUTH(ForgeDirection.SOUTH, "S"),
		WEST(ForgeDirection.WEST, "W"),
		EAST(ForgeDirection.EAST, "E");
		private ForgeDirection dir;
		@Getter
		private String letter;
		CoverSides(ForgeDirection dir, String letter) {
			this.dir = dir;
			this.letter = letter;
		}
		public ForgeDirection getDir(BlockRotation rot) {
			ForgeDirection result = dir;
			switch(rot.getInteger()) {
				case 0:
					result = result.getRotation(ForgeDirection.UP);
				case 3:
					result = result.getRotation(ForgeDirection.UP);
				case 1:
					result = result.getRotation(ForgeDirection.UP);
				case 2:
			}
			return result;
		}
	}
	
	enum BlockRotation {
		ZERO(0),ONE(1),TWO(2),THREE(3);
		@Getter
		private int integer;
		BlockRotation(int rot) {
			integer = rot;
		}
		
		static BlockRotation getRotation(int from) {
			for(BlockRotation rot:values()) {
				if(rot.getInteger() == from) {
					return rot;
				}
			}
			return null;
		}
	}
	
	static Map<BlockRotation, CCModel> block = new HashMap<BlockRotation, CCModel>();
	static Map<CoverSides, Map<BlockRotation, CCModel>> texturePlate_Inner = new HashMap<CoverSides, Map<BlockRotation, CCModel>>();
	static Map<CoverSides, Map<BlockRotation, CCModel>> texturePlate_Outer = new HashMap<CoverSides, Map<BlockRotation, CCModel>>();
	
	static {
		loadModels();
	}
	
	public static void loadModels() {
		try {
			Map<String, CCModel> blockPartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/BlockModel_result.obj"), 7, new Scale(1/100f));
			
			block = null;
			for(Entry<String, CCModel> entry:blockPartModels.entrySet()) {
				if(entry.getKey().contains(" Block ")) {
					if(block != null) throw new UnsupportedOperationException();
					block = computeRotated(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)));
				}
			}

			texturePlate_Outer.clear();
			texturePlate_Inner.clear();
			for(CoverSides side:CoverSides.values()) {
				String grp_Outer = "OutSide_" + side.getLetter();
				String grp_Inside = "Inside_" + side.getLetter();
				for(Entry<String, CCModel> entry:blockPartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp_Outer + " ")) {
						texturePlate_Outer.put(side, computeRotated(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
					if(entry.getKey().contains(" " + grp_Inside + " ")) {
						texturePlate_Inner.put(side, computeRotated(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(texturePlate_Outer.get(side) == null) throw new RuntimeException("Couldn't load OutSide " + side.name() + " (" + grp_Outer + ").");
				if(texturePlate_Inner.get(side) == null) throw new RuntimeException("Couldn't load OutSide " + side.name() + " (" + grp_Outer + ").");
			}
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<BlockRotation, CCModel> computeRotated(CCModel m) {
		m.apply(new UVScale(1, 0.75));
		Map<BlockRotation, CCModel> map = new HashMap<BlockRotation, CCModel>();
		for(BlockRotation rot:BlockRotation.values()) {
			CCModel model = m.copy();
			switch(rot.getInteger()) {
				case 0:
					model.apply(Rotation.sideOrientation(0, 3));
					model.apply(new Translation(0, 0, 1));
					break;
				case 1:
					model.apply(Rotation.sideOrientation(0, 1));
					model.apply(new Translation(1, 0, 0));
					break;
				case 2:
					break;
				case 3:
					model.apply(Rotation.sideOrientation(0, 2));
					model.apply(new Translation(1, 0, 1));
					break;
			}
			model.computeNormals();
			model.computeLighting(LightModel.standardLightModel);
			map.put(rot, model);
		}
		return map;
	}
	
	public void renderWorldBlock(LogisticsSolidTileEntity blockTile, RenderBlocks renderer, int x, int y, int z) {
		Tessellator tess = Tessellator.instance;
		CCRenderState.reset();
		CCRenderState.useNormals = true;
		CCRenderState.alphaOverride = 0xff;
		
		BlockRotation rotation = BlockRotation.getRotation(blockTile.getRotation());
		
		int brightness = new LPPosition((TileEntity)blockTile).getBlock(blockTile.getWorldObj()).getMixedBrightnessForBlock(blockTile.getWorldObj(), blockTile.xCoord, blockTile.yCoord, blockTile.zCoord);
		
		tess.setColorOpaque_F(1F, 1F, 1F);
		tess.setBrightness(brightness);
		
		IconTransformation icon = new IconTransformation(LogisticsSolidBlock.getNewIcon(blockTile.getWorldObj(), blockTile.xCoord, blockTile.yCoord, blockTile.zCoord));
		
		//Draw
		block.get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
		LPPosition pos = new LPPosition(blockTile);
		for(CoverSides side:CoverSides.values()) {
			boolean render = true;
			LPPosition newPos = pos.copy();
			newPos.moveForward(side.getDir(rotation));
			TileEntity sideTile = newPos.getTileEntity(blockTile.getWorldObj());
			if(sideTile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) sideTile;
				if(tilePipe.renderState.pipeConnectionMatrix.isConnected(side.getDir(rotation).getOpposite())) {
					render = false;
				}
			}
			if(render) {
				texturePlate_Outer.get(side).get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
				texturePlate_Inner.get(side).get(rotation).render(new IVertexOperation[]{new Translation(x, y, z), icon});
			}
		}
		
	}

	public void renderInventoryBlock(Block block2, int metadata) {
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT); //don't break other mods' guis when holding a pipe
		//force transparency
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		Block block = LogisticsPipes.LogisticsPipeBlock;
		Tessellator tess = Tessellator.instance;
		
		BlockRotation rotation = BlockRotation.ZERO;
		
		tess.startDrawingQuads();
		
		IconTransformation icon = new IconTransformation(LogisticsSolidBlock.getNewIcon(metadata));
		
		//Draw
		LogisticsNewSolidBlockWorldRenderer.block.get(rotation).render(new IVertexOperation[]{icon});
		for(CoverSides side:CoverSides.values()) {
			LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(new IVertexOperation[]{icon});
		}
		tess.draw();
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}
	
}
