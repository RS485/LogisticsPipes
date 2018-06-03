package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import org.lwjgl.opengl.GL12;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.PlayerConfig;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.signs.IPipeSignData;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderTESR;
import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.renderer.newpipe.LogisticsNewPipeItemBoxRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsRenderPipe extends TileEntitySpecialRenderer<LogisticsTileGenericPipe> {

	private static final int LIQUID_STAGES = 40;
	private static final int MAX_ITEMS_TO_RENDER = 10;
	private static final ResourceLocation SIGN = new ResourceLocation("textures/entity/sign.png");
	private static final IntHashMap displayFluidLists = new IntHashMap();
	public static LogisticsNewRenderPipe secondRenderer = new LogisticsNewRenderPipe();
	public static LogisticsNewPipeItemBoxRenderer boxRenderer = new LogisticsNewPipeItemBoxRenderer();
	public static PlayerConfig config;
	private static ItemStackRenderer itemRenderer = new ItemStackRenderer(0, 0, 0, false, false);
	private static Map<IPipeSignData, GLRenderList> pipeSignRenderListMap = new HashMap<IPipeSignData, GLRenderList>();
	private static int localItemTestRenderList = -1;
	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };
	private ModelSign modelSign;
	private IBCRenderTESR bcRenderer = SimpleServiceLocator.buildCraftProxy.getBCRenderTESR();

	public LogisticsRenderPipe() {
		super();
		modelSign = new ModelSign();
		modelSign.signStick.showModel = false;

		LogisticsRenderPipe.config = LogisticsPipes.getClientPlayerConfig();
	}

	@Override
	public void render(LogisticsTileGenericPipe tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		boolean inHand = false;
		if(tileentity == null && x == 0 && y == 0 && z == 0) {
			inHand = true;
		} else if (tileentity.pipe == null) {
			return;
		}

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);

		if (destroyStage >= 0)
		{
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4.0F, 4.0F, 1.0F);
			//GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if (destroyStage < 0)
		{
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		}

		if (!inHand) {
			if (tileentity.pipe instanceof CoreRoutedPipe) {
				renderPipeSigns((CoreRoutedPipe) tileentity.pipe, x, y, z, partialTicks);
			}
		}

		double distance = !inHand ? new DoubleCoordinates((TileEntity) tileentity).distanceTo(new DoubleCoordinates(Minecraft.getMinecraft().player)) : 0;

		LogisticsRenderPipe.secondRenderer.renderTileEntityAt(tileentity, x, y, z, partialTicks, distance);

		if(!inHand) {
			bcRenderer.renderWires(tileentity, x, y, z);

			// dynamically render pluggables (like gates)
			bcRenderer.dynamicRenderPluggables(tileentity, x, y, z);

			if (!tileentity.isOpaque()) {
				if (tileentity.pipe.transport instanceof PipeFluidTransportLogistics) {
					//renderFluids(pipe.pipe, x, y, z);
				}
				if (tileentity.pipe.transport != null) {
					renderSolids(tileentity.pipe, x, y, z, partialTicks);
				}
			}
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (destroyStage >= 0)
		{
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
	}

	private void renderSolids(CoreUnroutedPipe pipe, double x, double y, double z, float partialTickTime) {
		GL11.glPushMatrix();

		float light = pipe.container.getWorld().getLightBrightness(pipe.container.getPos());

		int count = 0;
		for (LPTravelingItem item : pipe.transport.items) {
			CoreUnroutedPipe lPipe = pipe;
			double lX = x;
			double lY = y;
			double lZ = z;
			float lItemYaw = item.getYaw();
			if (count >= LogisticsRenderPipe.MAX_ITEMS_TO_RENDER) {
				break;
			}

			if (item.getItemIdentifierStack() == null) {
				continue;
			}
			if (!item.getContainer().getPos().equals(lPipe.container.getPos())) {
				continue;
			}

			if (item.getPosition() > lPipe.transport.getPipeLength() || item.getPosition() < 0) {
				continue;
			}

			float fPos = item.getPosition() + item.getSpeed() * partialTickTime;
			if (fPos > lPipe.transport.getPipeLength() && item.output != null) {
				CoreUnroutedPipe nPipe = lPipe.transport.getNextPipe(item.output);
				if (nPipe != null) {
					fPos -= lPipe.transport.getPipeLength();
					lX -= lPipe.getX() - nPipe.getX();
					lY -= lPipe.getY() - nPipe.getY();
					lZ -= lPipe.getZ() - nPipe.getZ();
					lItemYaw += lPipe.transport.getYawDiff(item);
					lPipe = nPipe;
					item = item.renderCopy();
					item.input = item.output;
					item.output = null;
				} else {
					continue;
				}
			}

			DoubleCoordinates pos = lPipe.getItemRenderPos(fPos, item);
			if (pos == null) {
				continue;
			}
			double boxScale = lPipe.getBoxRenderScale(fPos, item);
			double itemYaw = (lPipe.getItemRenderYaw(fPos, item) - lPipe.getItemRenderYaw(0, item) + lItemYaw) % 360;
			double itemPitch = lPipe.getItemRenderPitch(fPos, item);
			double itemYawForPitch = lPipe.getItemRenderYaw(fPos, item);

			ItemStack itemstack = item.getItemIdentifierStack().makeNormalStack();
			doRenderItem(itemstack, pipe.container.getWorld(), lX + pos.getXCoord(), lY + pos.getYCoord(), lZ + pos.getZCoord(), light, 0.75F, boxScale, itemYaw, itemPitch, itemYawForPitch, partialTickTime);
			count++;
		}

		count = 0;
		double dist = 0.135;
		DoubleCoordinates pos = new DoubleCoordinates(0.5, 0.5, 0.5);
		CoordinateUtils.add(pos, EnumFacing.SOUTH, dist);
		CoordinateUtils.add(pos, EnumFacing.EAST, dist);
		CoordinateUtils.add(pos, EnumFacing.UP, dist);
		for (Pair<ItemIdentifierStack, Pair<Integer, Integer>> item : pipe.transport._itemBuffer) {
			if (item == null || item.getValue1() == null) {
				continue;
			}
			ItemStack itemstack = item.getValue1().makeNormalStack();
			doRenderItem(itemstack, pipe.container.getWorld(), x + pos.getXCoord(), y + pos.getYCoord(), z + pos.getZCoord(), light, 0.25F, 0, 0, 0, 0, partialTickTime);
			count++;
			if (count >= 27) {
				break;
			} else if (count % 9 == 0) {
				CoordinateUtils.add(pos, EnumFacing.SOUTH, dist * 2.0);
				CoordinateUtils.add(pos, EnumFacing.EAST, dist * 2.0);
				CoordinateUtils.add(pos, EnumFacing.DOWN, dist);
			} else if (count % 3 == 0) {
				CoordinateUtils.add(pos, EnumFacing.SOUTH, dist * 2.0);
				CoordinateUtils.add(pos, EnumFacing.WEST, dist);
			} else {
				CoordinateUtils.add(pos, EnumFacing.NORTH, dist);
			}
		}

		GL11.glPopMatrix();
	}

	public void doRenderItem(ItemStack itemstack, World world, double x, double y, double z, float light, float renderScale, double boxScale, double yaw, double pitch, double yawForPitch, float partialTickTime) {
		LogisticsRenderPipe.boxRenderer.doRenderItem(itemstack, light, x, y, z, boxScale, yaw, pitch, yawForPitch);

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glScalef(renderScale, renderScale, renderScale);
		GL11.glRotated(yawForPitch, 0, 1, 0);
		GL11.glRotated(pitch, 1, 0, 0);
		GL11.glRotated(-yawForPitch, 0, 1, 0);
		GL11.glRotated(yaw, 0, 1, 0);
		GL11.glTranslatef(0.0F, -0.35F, 0.0F);
		itemRenderer.setItemstack(itemstack).setWorld(world).setPartialTickTime(partialTickTime);
		itemRenderer.renderInWorld();
		GL11.glPopMatrix();
	}

	private boolean needDistance(List<Pair<EnumFacing, IPipeSign>> list) {
		List<Pair<EnumFacing, IPipeSign>> copy = new ArrayList<>(list);
		Iterator<Pair<EnumFacing, IPipeSign>> iter = copy.iterator();
		boolean north = false, south = false, east = false, west = false;
		while (iter.hasNext()) {
			Pair<EnumFacing, IPipeSign> pair = iter.next();
			if (pair.getValue1() == EnumFacing.UP || pair.getValue1() == EnumFacing.DOWN || pair.getValue1() == null) {
				iter.remove();
			}
			if (pair.getValue1() == EnumFacing.NORTH) {
				north = true;
			}
			if (pair.getValue1() == EnumFacing.SOUTH) {
				south = true;
			}
			if (pair.getValue1() == EnumFacing.EAST) {
				east = true;
			}
			if (pair.getValue1() == EnumFacing.WEST) {
				west = true;
			}
		}
		boolean result = copy.size() > 1;
		if (copy.size() == 2) {
			if (north && south) {
				result = false;
			}
			if (east && west) {
				result = false;
			}
		}
		return result;
	}

	private void renderPipeSigns(CoreRoutedPipe pipe, double x, double y, double z, float partialTickTime) {
		if (!pipe.getPipeSigns().isEmpty()) {
			List<Pair<EnumFacing, IPipeSign>> list = pipe.getPipeSigns();
			for (Pair<EnumFacing, IPipeSign> pair : list) {
				if (pipe.container.renderState.pipeConnectionMatrix.isConnected(pair.getValue1())) {
					continue;
				}
				GL11.glPushMatrix();
				GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
				switch (pair.getValue1()) {
					case UP:
						GL11.glRotatef(90, 1.0F, 0.0F, 0.0F);
						break;
					case DOWN:
						GL11.glRotatef(-90, 1.0F, 0.0F, 0.0F);
						break;
					case NORTH:
						GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
						if (needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case SOUTH:
						GL11.glRotatef(-180, 0.0F, 1.0F, 0.0F);
						if (needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case EAST:
						GL11.glRotatef(-90, 0.0F, 1.0F, 0.0F);
						if (needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case WEST:
						GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
						if (needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					default:
				}
				renderSign(pipe, pair.getValue2(), partialTickTime);
				GL11.glPopMatrix();
			}
		}
	}

	private void renderSign(CoreRoutedPipe pipe, IPipeSign type, float partialTickTime) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		float signScale = 2 / 3.0F;
		GL11.glTranslatef(0.0F, -0.3125F, -0.36F);
		GL11.glRotatef(180, 0.0f, 1.0f, 0.0f);
		Minecraft.getMinecraft().renderEngine.bindTexture(LogisticsRenderPipe.SIGN);

		GL11.glPushMatrix();
		GL11.glScalef(signScale, -signScale, -signScale);
		modelSign.renderSign();
		GL11.glPopMatrix();

		GL11.glTranslatef(-0.32F, 0.5F * signScale + 0.08F, 0.07F * signScale);

		IPipeSignData data = type.getRenderData(pipe);
		GLRenderList renderList = pipeSignRenderListMap.get(data);
		if(data.isListCompatible(this)) {
			if (renderList == null || renderList.isInvalid() || !renderList.isFilled()) {
				renderList = SimpleServiceLocator.renderListHandler.getNewRenderList();
				pipeSignRenderListMap.put(data, renderList);
				renderList.startListCompile();
				type.render(pipe, this);
				renderList.stopCompile();
			}
			renderList.render();
		} else {
			type.render(pipe, this);
		}
	}

	public void renderItemStackOnSign(ItemStack itemstack) {
		if (itemstack == null || itemstack.getItem() == null) {
			return; // Only happens on false configuration
		}

		Item item = itemstack.getItem();

		/*
		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.INVENTORY);

		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GL11.glPushMatrix();

		if (customRenderer != null) {
			if (customRenderer.shouldUseRenderHelper(ItemRenderType.INVENTORY, itemstack, ItemRendererHelper.INVENTORY_BLOCK)) {
				GL11.glScalef(0.20F, -0.20F, -0.01F);

				GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);

				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_LIGHT0);
				GL11.glDisable(GL11.GL_LIGHT1);
				GL11.glDisable(GL11.GL_COLOR_MATERIAL);

				customRenderer.renderItem(ItemRenderType.INVENTORY, itemstack, renderBlocks);

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_LIGHT0);
				GL11.glEnable(GL11.GL_LIGHT1);
				GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			} else {
				GL11.glScalef(0.018F, -0.018F, -0.01F);
				GL11.glTranslatef(-7F, -8F, 0F);

				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_LIGHT0);
				GL11.glDisable(GL11.GL_LIGHT1);
				GL11.glDisable(GL11.GL_COLOR_MATERIAL);

				customRenderer.renderItem(ItemRenderType.INVENTORY, itemstack, renderBlocks);

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_LIGHT0);
				GL11.glEnable(GL11.GL_LIGHT1);
				GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			}
		} else if (item instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType())) {
			GL11.glScalef(0.20F, -0.20F, -0.01F);

			GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);

			renderBlocks.useInventoryTint = false;

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_LIGHT0);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);

			renderBlocks.renderBlockAsItem(Block.getBlockFromItem(item), itemstack.getItemDamage(), 1.0F);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_LIGHT1);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		} else {
			GL11.glScalef(0.02F, -0.02F, -0.01F);

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_LIGHT0);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);

			GL11.glTranslatef(-8F, -8F, 0.0F);

			if (item.requiresMultipleRenderPasses()) {
				for (int var14 = 0; var14 < item.getRenderPasses(itemstack.getItemDamage()); ++var14) {
					TextureAtlasSprite var15 = item.getIconFromDamageForRenderPass(itemstack.getItemDamage(), var14);
					renderItem(var15);
				}
			} else {
				renderItem(item.getIconIndex(itemstack));
			}

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_LIGHT1);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		}

		GL11.glPopMatrix();*/
	}

	/*private void renderItem(TextureAtlasSprite par3Icon) {
		if (par3Icon == null) {
			return;
		}
		int par1 = 0;
		int par2 = 0;
		int par4 = 16;
		int par5 = 16;
		double zLevel = 0;
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(par1 + 0, par2 + par5, zLevel, par3Icon.getMinU(), par3Icon.getMaxV());
		tessellator.addVertexWithUV(par1 + par4, par2 + par5, zLevel, par3Icon.getMaxU(), par3Icon.getMaxV());
		tessellator.addVertexWithUV(par1 + par4, par2 + 0, zLevel, par3Icon.getMaxU(), par3Icon.getMinV());
		tessellator.addVertexWithUV(par1 + 0, par2 + 0, zLevel, par3Icon.getMinU(), par3Icon.getMinV());
		tessellator.draw();
		GL11.glPopMatrix();
	}*/

	public String cut(String name, FontRenderer renderer) {
		if (renderer.getStringWidth(name) < 90) {
			return name;
		}
		StringBuilder sum = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			if (renderer.getStringWidth(sum.toString() + name.charAt(i) + "...") < 90) {
				sum.append(name.charAt(i));
			} else {
				return sum.toString() + "...";
			}
		}
		return sum.toString();
	}

	public boolean isRenderListCompatible(ItemStack stack) {
		GL11.glPushMatrix();
		GL11.glScaled(0, 0, 0);
		try {
			renderItemStackOnSign(stack);
			int i = GL11.glGetError();
			if (i != 0) {
				GL11.glPopMatrix();
				return false;
			}
			if(localItemTestRenderList == -1) {
				localItemTestRenderList = GLAllocation.generateDisplayLists(1);
			}
			GL11.glNewList(localItemTestRenderList, GL11.GL_COMPILE);
			i = GL11.glGetError();
			if (i != 0) {
				GL11.glPopMatrix();
				return false;
			}
			renderItemStackOnSign(stack);
			i = GL11.glGetError();
			if (i != 0) {
				GL11.glPopMatrix();
				return false;
			}
			GL11.glEndList();
			i = GL11.glGetError();
			if (i != 0) {
				GL11.glPopMatrix();
				return false;
			}
			GL11.glCallList(localItemTestRenderList);
			i = GL11.glGetError();
			if (i != 0) {
				GL11.glPopMatrix();
				return false;
			}
			GL11.glPopMatrix();
			return true;
		} catch (Exception e) {
		}
		GL11.glPopMatrix();
		return false;
	}

	private class DisplayFluidList {

		public int[] sideHorizontal = new int[LogisticsRenderPipe.LIQUID_STAGES];
		public int[] sideVertical = new int[LogisticsRenderPipe.LIQUID_STAGES];
		public int[] centerHorizontal = new int[LogisticsRenderPipe.LIQUID_STAGES];
		public int[] centerVertical = new int[LogisticsRenderPipe.LIQUID_STAGES];
	}
}
