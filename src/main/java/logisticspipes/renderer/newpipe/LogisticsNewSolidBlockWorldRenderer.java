package logisticspipes.renderer.newpipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.util.EnumFacing;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.operation.LPRotation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.object3d.operation.LPUVScale;

public class LogisticsNewSolidBlockWorldRenderer {

	enum CoverSides {
		DOWN(EnumFacing.DOWN, "D"),
		NORTH(EnumFacing.NORTH, "N"),
		SOUTH(EnumFacing.SOUTH, "S"),
		WEST(EnumFacing.WEST, "W"),
		EAST(EnumFacing.EAST, "E");

		private EnumFacing dir;
		@Getter
		private String letter;

		CoverSides(EnumFacing dir, String letter) {
			this.dir = dir;
			this.letter = letter;
		}

		public EnumFacing getDir(BlockRotation rot) {
			EnumFacing result = dir;
			if (result != EnumFacing.DOWN) {
				switch (rot.getInteger()) {
					case 0:
						result = result.rotateY();
					case 3:
						result = result.rotateY();
					case 1:
						result = result.rotateY();
					case 2:
				}
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

	static Map<BlockRotation, IModel3D> block = new HashMap<>();
	static Map<CoverSides, Map<BlockRotation, IModel3D>> texturePlate_Inner = new HashMap<>();
	static Map<CoverSides, Map<BlockRotation, IModel3D>> texturePlate_Outer = new HashMap<>();

	public static void loadModels() {
		if (!SimpleServiceLocator.cclProxy.isActivated()) return;
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
		Map<BlockRotation, IModel3D> map = new HashMap<>();
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
/*
	public void renderWorldBlock(IBlockAccess world, LogisticsSolidTileEntity blockTile, RenderBlocks renderer, int x, int y, int z) {
		Tessellator tess = Tessellator.instance;
		SimpleServiceLocator.cclProxy.getRenderState().reset();
		SimpleServiceLocator.cclProxy.getRenderState().setUseNormals(true);
		SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

		BlockRotation rotation = BlockRotation.ZERO;
		int brightness = 0;
		IIconTransformation icon;
		if(blockTile != null) {
			BlockRotation.getRotation(blockTile.getRotation());
			brightness = new DoubleCoordinates(blockTile).getBlock(world).getMixedBrightnessForBlock(world, blockTile.xCoord, blockTile.yCoord, blockTile.zCoord);
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(world, blockTile.xCoord, blockTile.yCoord, blockTile.zCoord));
		} else {
			brightness = LogisticsPipes.LogisticsSolidBlock.getMixedBrightnessForBlock(world, x, y, z);
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(world, x, y, z));
		}


		tess.setColorOpaque_F(1F, 1F, 1F);
		tess.setBrightness(brightness);

		//Draw
		LogisticsNewSolidBlockWorldRenderer.block.get(rotation).render(new LPTranslation(x, y, z), icon);
		if(blockTile != null) {
			DoubleCoordinates pos = new DoubleCoordinates(blockTile);
			for (CoverSides side : CoverSides.values()) {
				boolean render = true;
				DoubleCoordinates newPos = CoordinateUtils.sum(pos, side.getDir(rotation));
				TileEntity sideTile = newPos.getTileEntity(blockTile.getworld());
				if (sideTile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) sideTile;
					if (tilePipe.renderState.pipeConnectionMatrix.isConnected(side.getDir(rotation).getOpposite())) {
						render = false;
					}
				}
				if (render) {
					LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(new LPTranslation(x, y, z), icon);
					LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side).get(rotation).render(new LPTranslation(x, y, z), icon);
				}
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

		TextureTransformation icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(metadata));

		//Draw
		LogisticsNewSolidBlockWorldRenderer.block.get(rotation).render(icon);
		if(metadata != LogisticsSolidBlock.LOGISTICS_BLOCK_FRAME) {
			for (CoverSides side : CoverSides.values()) {
				LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation).render(icon);
			}
		}
		tess.draw();
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}*/

}
