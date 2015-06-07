package logisticspipes.renderer.newpipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IIconTransformation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.operation.LPRotation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.object3d.operation.LPUVScale;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import org.lwjgl.opengl.GL11;

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
			switch (rot.getInteger()) {
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
		ZERO(0),
		ONE(1),
		TWO(2),
		THREE(3);

		@Getter
		private int integer;

		BlockRotation(int rot) {
			integer = rot;
		}

		static BlockRotation getRotation(int from) {
			for (BlockRotation rot : BlockRotation.values()) {
				if (rot.getInteger() == from) {
					return rot;
				}
			}
			return null;
		}
	}

	static Map<BlockRotation, IModel3D> block = new HashMap<BlockRotation, IModel3D>();
	static Map<CoverSides, Map<BlockRotation, IModel3D>> texturePlate_Inner = new HashMap<CoverSides, Map<BlockRotation, IModel3D>>();
	static Map<CoverSides, Map<BlockRotation, IModel3D>> texturePlate_Outer = new HashMap<CoverSides, Map<BlockRotation, IModel3D>>();

	static {
		LogisticsNewSolidBlockWorldRenderer.loadModels();
	}

	public static void loadModels() {
		if(!SimpleServiceLocator.cclProxy.isActivated()) return;
		try {
			Map<String, IModel3D> blockPartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/BlockModel_result.obj"), 7, new LPScale(1 / 100f));

			LogisticsNewSolidBlockWorldRenderer.block = null;
			for (Entry<String, IModel3D> entry : blockPartModels.entrySet()) {
				if (entry.getKey().contains(" Block ")) {
					if (LogisticsNewSolidBlockWorldRenderer.block != null) {
						throw new UnsupportedOperationException();
					}
					LogisticsNewSolidBlockWorldRenderer.block = LogisticsNewSolidBlockWorldRenderer.computeRotated(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)));
				}
			}

			LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.clear();
			LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.clear();
			for (CoverSides side : CoverSides.values()) {
				String grp_Outer = "OutSide_" + side.getLetter();
				String grp_Inside = "Inside_" + side.getLetter();
				for (Entry<String, IModel3D> entry : blockPartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp_Outer + " ")) {
						LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.put(side, LogisticsNewSolidBlockWorldRenderer.computeRotated(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
					if (entry.getKey().contains(" " + grp_Inside + " ")) {
						LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.put(side, LogisticsNewSolidBlockWorldRenderer.computeRotated(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side) == null) {
					throw new RuntimeException("Couldn't load OutSide " + side.name() + " (" + grp_Outer + ").");
				}
				if (LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side) == null) {
					throw new RuntimeException("Couldn't load OutSide " + side.name() + " (" + grp_Outer + ").");
				}
			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<BlockRotation, IModel3D> computeRotated(IModel3D m) {
		m.apply(new LPUVScale(1, 0.75));
		Map<BlockRotation, IModel3D> map = new HashMap<BlockRotation, IModel3D>();
		for (BlockRotation rot : BlockRotation.values()) {
			IModel3D model = m.copy();
			switch (rot.getInteger()) {
				case 0:
					model.apply(LPRotation.sideOrientation(0, 3));
					model.apply(new LPTranslation(0, 0, 1));
					break;
				case 1:
					model.apply(LPRotation.sideOrientation(0, 1));
					model.apply(new LPTranslation(1, 0, 0));
					break;
				case 2:
					break;
				case 3:
					model.apply(LPRotation.sideOrientation(0, 2));
					model.apply(new LPTranslation(1, 0, 1));
					break;
			}
			model.computeNormals();
			model.computeStandardLighting();
			map.put(rot, model);
		}
		return map;
	}

	public void renderWorldBlock(LogisticsSolidTileEntity blockTile, RenderBlocks renderer, int x, int y, int z) {
		Tessellator tess = Tessellator.instance;
		SimpleServiceLocator.cclProxy.getRenderState().reset();
		SimpleServiceLocator.cclProxy.getRenderState().setUseNormals(true);
		SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

		BlockRotation rotation = BlockRotation.getRotation(blockTile.getRotation());

		int brightness = new LPPosition(blockTile).getBlock(blockTile.getWorldObj()).getMixedBrightnessForBlock(blockTile.getWorldObj(), blockTile.xCoord, blockTile.yCoord, blockTile.zCoord);

		tess.setColorOpaque_F(1F, 1F, 1F);
		tess.setBrightness(brightness);

		IIconTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(blockTile.getWorldObj(), blockTile.xCoord, blockTile.yCoord, blockTile.zCoord));

		//Draw
		LogisticsNewSolidBlockWorldRenderer.block.get(rotation).render(new I3DOperation[] { new LPTranslation(x, y, z), icon });
		LPPosition pos = new LPPosition(blockTile);
		for (CoverSides side : CoverSides.values()) {
			boolean render = true;
			LPPosition newPos = pos.copy();
			newPos.moveForward(side.getDir(rotation));
			TileEntity sideTile = newPos.getTileEntity(blockTile.getWorldObj());
			if (sideTile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) sideTile;
				if (tilePipe.renderState.pipeConnectionMatrix.isConnected(side.getDir(rotation).getOpposite())) {
					render = false;
				}
			}
			if (render) {
				LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(new I3DOperation[] { new LPTranslation(x, y, z), icon });
				LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side).get(rotation).render(new I3DOperation[] { new LPTranslation(x, y, z), icon });
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

		IIconTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(metadata));

		//Draw
		LogisticsNewSolidBlockWorldRenderer.block.get(rotation).render(new I3DOperation[] { icon });
		for (CoverSides side : CoverSides.values()) {
			LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(new I3DOperation[] { icon });
		}
		tess.draw();
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}

}
