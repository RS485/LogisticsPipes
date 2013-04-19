package logisticspipes.renderer;

import java.util.HashMap;

import logisticspipes.transport.PipeLiquidTransportLogistics;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.RenderPipe;

public class LogisticsRenderPipe extends RenderPipe {

	final static private int LIQUID_STAGES = 40;

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };
	
	private HashMap<Integer, HashMap<Integer, DisplayLiquidList>> displayLiquidLists = new HashMap<Integer, HashMap<Integer, DisplayLiquidList>>();
	
	private class DisplayLiquidList {

		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		super.renderTileEntityAt(tileentity, x, y, z, f);
		if (BuildCraftCore.render == RenderMode.NoDynamic) return;
		TileGenericPipe pipe = ((TileGenericPipe) tileentity);
		if (pipe.pipe == null) return;
		if (pipe.pipe.transport instanceof PipeLiquidTransportLogistics) {
			renderLiquids(pipe.pipe, x, y, z);
		}
	}

	private void renderLiquids(Pipe pipe, double x, double y, double z) {
		PipeLiquidTransportLogistics liq = (PipeLiquidTransportLogistics) pipe.transport;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		// sides

		boolean sides = false, above = false;

		for (int i = 0; i < 6; ++i) {
			// ILiquidTank tank = liq.getTanks()[i];
			// LiquidStack liquid = tank.getLiquid();
			LiquidStack liquid = liq.renderCache[i];
			// int amount = liquid != null ? liquid.amount : 0;
			// int amount = liquid != null ? liq.renderAmmount[i] : 0;

			if (liquid != null && liquid.amount > 0) {
				DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

				if (d == null) {
					continue;
				}

				int stage = (int) ((float) liquid.amount / (float) (liq.getSideCapacity()) * (LIQUID_STAGES - 1));

				GL11.glPushMatrix();
				int list = 0;

				switch (ForgeDirection.VALID_DIRECTIONS[i]) {
				case UP:
					above = true;
					list = d.sideVertical[stage];
					break;
				case DOWN:
					GL11.glTranslatef(0, -0.75F, 0);
					list = d.sideVertical[stage];
					break;
				case EAST:
				case WEST:
				case SOUTH:
				case NORTH:
					sides = true;
					GL11.glRotatef(angleY[i], 0, 1, 0);
					GL11.glRotatef(angleZ[i], 0, 0, 1);
					list = d.sideHorizontal[stage];
					break;
				default:
				}

				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
		// ILiquidTank tank = liq.getTanks()[ForgeDirection.Unknown.ordinal()];
		// LiquidStack liquid = tank.getLiquid();
		LiquidStack liquid = liq.renderCache[ForgeDirection.UNKNOWN.ordinal()];

		// int amount = liquid != null ? liquid.amount : 0;
		// int amount = liquid != null ? liq.renderAmmount[ForgeDirection.Unknown.ordinal()] : 0;
		if (liquid != null && liquid.amount > 0) {
			// DisplayLiquidList d = getListFromBuffer(liq.getTanks()[ForgeDirection.Unknown.ordinal()].getLiquid(), pipe.worldObj);
			DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

			if (d != null) {
				int stage = (int) ((float) liquid.amount / (float) (liq.getInnerCapacity()) * (LIQUID_STAGES - 1));

				if (above) {
					GL11.glCallList(d.centerVertical[stage]);
				}

				if (!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);
				}
			}

		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	private DisplayLiquidList getListFromBuffer(LiquidStack stack, World world) {

		int liquidId = stack.itemID;

		if (liquidId == 0)
			return null;
		//@TODO: fixme
		/*if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			Minecraft.getMinecraft().renderEngine.bindTexture(Block.blocksList[liquidId].getIcon(par1, par2)(), 0);
		} else if (Item.itemsList[liquidId] != null) {
			Minecraft.getMinecraft().renderEngine.bindTexture(Item.itemsList[liquidId].getIconFromDamage(stack.itemMeta));
		} else
			return null;*/
		return getDisplayLiquidLists(liquidId, stack.itemMeta, world);
	}

	private DisplayLiquidList getDisplayLiquidLists(int liquidId, int meta, World world) {
		HashMap<Integer, DisplayLiquidList> list = displayLiquidLists.get(liquidId);
		if (list!=null) {
			HashMap<Integer, DisplayLiquidList> x = displayLiquidLists.get(liquidId);
			DisplayLiquidList liquidList = x.get(meta);
			if (liquidList!=null)
				return liquidList;
		} else {
			list = new HashMap<Integer, DisplayLiquidList>();
			displayLiquidLists.put(liquidId, list);
		}

		DisplayLiquidList d = new DisplayLiquidList();
		list.put(meta, d);

		BlockInterface block = new BlockInterface();
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			block.texture = Block.blocksList[liquidId].getIcon(0, meta);
		} else {
			block.texture = Item.itemsList[liquidId].getIconFromDamage(meta);
		}

		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		// render size

		for (int s = 0; s < LIQUID_STAGES; ++s) {
			float ratio = (float) s / (float) LIQUID_STAGES;

			// SIDE HORIZONTAL

			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = 0.0F;
			block.minZ = Utils.pipeMinPos + 0.01F;

			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;

			block.minY = Utils.pipeMinPos + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// SIDE VERTICAL

			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMaxPos - 0.01;
			block.maxY = 1;

			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER HORIZONTAL

			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = Utils.pipeMinPos + 0.01;
			block.minZ = Utils.pipeMinPos + 0.01;

			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER VERTICAL

			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = Utils.pipeMaxPos - 0.01;

			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

		}

		return d;
	}
}
