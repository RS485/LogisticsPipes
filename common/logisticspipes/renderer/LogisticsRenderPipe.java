package logisticspipes.renderer;

import java.util.*;

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
import logisticspipes.renderer.CustomBlockRenderer.RenderInfo;
import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.renderer.newpipe.LogisticsNewPipeItemBoxRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class LogisticsRenderPipe extends TileEntitySpecialRenderer {

	private static final int LIQUID_STAGES = 40;
	private static final int MAX_ITEMS_TO_RENDER = 10;
	private static final ResourceLocation SIGN = new ResourceLocation("textures/entity/sign.png");

	public static LogisticsNewRenderPipe secondRenderer = new LogisticsNewRenderPipe();
	public static LogisticsNewPipeItemBoxRenderer boxRenderer = new LogisticsNewPipeItemBoxRenderer();
	public static PlayerConfig config;
	private static ItemStackRenderer itemRenderer = new ItemStackRenderer(0, 0, 0, false, false, false);
	private static Map<IPipeSignData, GLRenderList> pipeSignRenderListMap = new HashMap<IPipeSignData, GLRenderList>();
	private static int localItemTestRenderList = -1;

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };
	private static final IntHashMap displayFluidLists = new IntHashMap();
	private ModelSign modelSign;
	private RenderBlocks renderBlocks = new RenderBlocks();
	private IBCRenderTESR bcRenderer = SimpleServiceLocator.buildCraftProxy.getBCRenderTESR();

	public LogisticsRenderPipe() {
		super();
		modelSign = new ModelSign();
		modelSign.signStick.showModel = false;

		LogisticsRenderPipe.config = LogisticsPipes.getClientPlayerConfig();
		RenderItem customRenderItem = new RenderItem() {

			@Override
			public boolean shouldBob() {
				return false;
			}

			@Override
			public boolean shouldSpreadItems() {
				return false;
			}
		};
		customRenderItem.setRenderManager(RenderManager.instance);
		itemRenderer.setRenderItem(customRenderItem);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime) {
		if (!(tileentity instanceof LogisticsTileGenericPipe)) {
			return;
		}
		LogisticsTileGenericPipe pipe = ((LogisticsTileGenericPipe) tileentity);
		if (pipe.pipe == null) {
			return;
		}

		if (pipe.pipe instanceof CoreRoutedPipe) {
			renderPipeSigns((CoreRoutedPipe) pipe.pipe, x, y, z, partialTickTime);
		}

		double distance = Math.pow(Minecraft.getMinecraft().thePlayer.lastTickPosX - tileentity.xCoord, 2) + Math.pow(Minecraft.getMinecraft().thePlayer.lastTickPosY - tileentity.yCoord, 2) + Math.pow(Minecraft.getMinecraft().thePlayer.lastTickPosZ - tileentity.zCoord, 2);
		if (LogisticsRenderPipe.config.isUseNewRenderer()) {
			LogisticsRenderPipe.secondRenderer.renderTileEntityAt((LogisticsTileGenericPipe) tileentity, x, y, z, partialTickTime, distance);
		}
		if (LogisticsRenderPipe.config.getRenderPipeContentDistance() * LogisticsRenderPipe.config.getRenderPipeContentDistance() < distance) {
			return;
		}

		bcRenderer.renderWires(pipe, x, y, z);

		// dynamically render pluggables (like gates)
		bcRenderer.dynamicRenderPluggables(pipe, x, y, z);

		if (!pipe.isOpaque()) {
			if (pipe.pipe.transport instanceof PipeFluidTransportLogistics) {
				renderFluids(pipe.pipe, x, y, z);
			}
			if (pipe.pipe.transport instanceof PipeTransportLogistics) {
				renderSolids(pipe.pipe, x, y, z, partialTickTime);
			}
		}
	}

	private void renderSolids(CoreUnroutedPipe pipe, double x, double y, double z, float partialTickTime) {
		GL11.glPushMatrix();

		float light = pipe.container.getWorldObj().getLightBrightness(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

		int count = 0;
		for (LPTravelingItem item : pipe.transport.items) {
			if (count >= LogisticsRenderPipe.MAX_ITEMS_TO_RENDER) {
				break;
			}

			LPPosition pos = new LPPosition(0.5D, 0.5D, 0.5D);

			if (item.getPosition() > 1 || item.getPosition() < 0) {
				continue;
			}

			float fPos = item.getPosition() + item.getSpeed() * partialTickTime;
			double boxScale = 1;

			if (fPos < 0.5) {
				if (item.input == ForgeDirection.UNKNOWN) {
					continue;
				}
				if (!pipe.container.renderState.pipeConnectionMatrix.isConnected(item.input.getOpposite())) {
					continue;
				}
				pos.moveForward(item.input.getOpposite(), 0.5F - fPos);
			} else {
				if (item.output == ForgeDirection.UNKNOWN) {
					continue;
				}
				if (!pipe.container.renderState.pipeConnectionMatrix.isConnected(item.output)) {
					continue;
				}
				pos.moveForward(item.output, fPos - 0.5F);
			}
			if (pipe.container.renderState.pipeConnectionMatrix.isTDConnected(item.input.getOpposite())) {
				boxScale = (fPos * (1 - 0.65)) + 0.65;
			}
			if (pipe.container.renderState.pipeConnectionMatrix.isTDConnected(item.output)) {
				boxScale = ((1 - fPos) * (1 - 0.65)) + 0.65;
			}
			if (pipe.container.renderState.pipeConnectionMatrix.isTDConnected(item.input.getOpposite()) && pipe.container.renderState.pipeConnectionMatrix.isTDConnected(item.output)) {
				boxScale = 0.65;
			}

			if (item.getItemIdentifierStack() == null) {
				continue;
			}
			if (item.getContainer().xCoord != pipe.container.xCoord || item.getContainer().yCoord != pipe.container.yCoord || item.getContainer().zCoord != pipe.container.zCoord) {
				continue;
			}
			ItemStack itemstack = item.getItemIdentifierStack().makeNormalStack();
			doRenderItem(itemstack, pipe.container.getWorldObj(), x + pos.getXD(), y + pos.getYD(), z + pos.getZD(), light, 0.75F, boxScale, partialTickTime);
			count++;
		}
		count = 0;
		float dist = 0.135F;
		LPPosition pos = new LPPosition(0.5D, 0.5D, 0.5D);
		pos.moveForward(ForgeDirection.SOUTH, dist);
		pos.moveForward(ForgeDirection.EAST, dist);
		pos.moveForward(ForgeDirection.UP, dist);
		for (Pair<ItemIdentifierStack, Pair<Integer, Integer>> item : pipe.transport._itemBuffer) {
			if (item == null || item.getValue1() == null) {
				continue;
			}
			ItemStack itemstack = item.getValue1().makeNormalStack();
			doRenderItem(itemstack, pipe.container.getWorldObj(), x + pos.getXD(), y + pos.getYD(), z + pos.getZD(), light, 0.25F, 0, partialTickTime);
			count++;
			if (count >= 27) {
				break;
			} else if (count % 9 == 0) {
				pos.moveForward(ForgeDirection.SOUTH, dist * 2);
				pos.moveForward(ForgeDirection.EAST, dist * 2);
				pos.moveForward(ForgeDirection.DOWN, dist);
			} else if (count % 3 == 0) {
				pos.moveForward(ForgeDirection.SOUTH, dist * 2);
				pos.moveForward(ForgeDirection.WEST, dist);
			} else {
				pos.moveForward(ForgeDirection.NORTH, dist);
			}
		}

		GL11.glPopMatrix();
	}

	public void doRenderItem(ItemStack itemstack, World worldObj, double x, double y, double z, float light, float renderScale, double boxScale, float partialTickTime) {
		if (LogisticsRenderPipe.config.isUseNewRenderer() && boxScale != 0) {
			LogisticsRenderPipe.boxRenderer.doRenderItem(itemstack, light, x, y, z, boxScale);
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glScalef(renderScale, renderScale, renderScale);
		GL11.glTranslatef(0.0F, -0.1F, 0.0F);
		itemRenderer.setItemstack(itemstack).setWorldObj(worldObj).setPartialTickTime(partialTickTime);
		itemRenderer.renderInWorld();
		GL11.glPopMatrix();
	}

	private boolean needDistance(List<Pair<ForgeDirection, IPipeSign>> list) {
		List<Pair<ForgeDirection, IPipeSign>> copy = new ArrayList<Pair<ForgeDirection, IPipeSign>>(list);
		Iterator<Pair<ForgeDirection, IPipeSign>> iter = copy.iterator();
		boolean north = false, south = false, east = false, west = false;
		while (iter.hasNext()) {
			Pair<ForgeDirection, IPipeSign> pair = iter.next();
			if (pair.getValue1() == ForgeDirection.UP || pair.getValue1() == ForgeDirection.DOWN || pair.getValue1() == ForgeDirection.UNKNOWN) {
				iter.remove();
			}
			if (pair.getValue1() == ForgeDirection.NORTH) {
				north = true;
			}
			if (pair.getValue1() == ForgeDirection.SOUTH) {
				south = true;
			}
			if (pair.getValue1() == ForgeDirection.EAST) {
				east = true;
			}
			if (pair.getValue1() == ForgeDirection.WEST) {
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
			List<Pair<ForgeDirection, IPipeSign>> list = pipe.getPipeSigns();
			for (Pair<ForgeDirection, IPipeSign> pair : list) {
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
						;
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

		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.INVENTORY);

		Minecraft.getMinecraft().renderEngine.bindTexture(itemstack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);

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
					IIcon var15 = item.getIconFromDamageForRenderPass(itemstack.getItemDamage(), var14);
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

		GL11.glPopMatrix();
	}

	private void renderItem(IIcon par3Icon) {
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
	}

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

	// BC copy, except where marked with XXX
	private void renderFluids(CoreUnroutedPipe pipe, double x, double y, double z) {
		// XXX PipeTransportFluids trans = pipe.transport;
		PipeFluidTransportLogistics trans = (PipeFluidTransportLogistics) (pipe.transport);

		boolean needsRender = false;
		for (int i = 0; i < 7; ++i) {
			FluidStack fluidStack = trans.renderCache[i];
			if (fluidStack != null && fluidStack.amount > 0) {
				needsRender = true;
				break;
			}
		}

		if (!needsRender) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		int skylight = pipe.container.getWorld().getSkyBlockTypeBrightness(EnumSkyBlock.Sky, pipe.getX(), pipe.getY(), pipe.getZ());
		int blocklight = pipe.container.getWorld().getSkyBlockTypeBrightness(EnumSkyBlock.Block, pipe.getX(), pipe.getY(), pipe.getZ());

		// sides

		boolean sides = false, above = false;

		for (int i = 0; i < 6; ++i) {
			FluidStack fluidStack = trans.renderCache[i];

			if (fluidStack != null && fluidStack.amount > 0) {
				DisplayFluidList d = getListFromBuffer(fluidStack, skylight, blocklight, fluidStack.getFluid().getLuminosity(fluidStack), pipe.container.getWorldObj());

				if (d == null) {
					continue;
				}

				// XXX int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));
				int stage = (int) ((float) fluidStack.amount / (float) (trans.getSideCapacity()) * (LogisticsRenderPipe.LIQUID_STAGES - 1));

				if(stage >= LogisticsRenderPipe.LIQUID_STAGES) {
					stage = LogisticsRenderPipe.LIQUID_STAGES - 1;
				}
				if(stage < 0) {
					stage = 0;
				}

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
						// Yes, this is kind of ugly, but was easier than transform the coordinates above.
						GL11.glTranslatef(0.5F, 0.0F, 0.5F);
						GL11.glRotatef(angleY[i], 0, 1, 0);
						GL11.glRotatef(angleZ[i], 0, 0, 1);
						GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
						list = d.sideHorizontal[stage];
						break;
					default:
				}
				bindTexture(TextureMap.locationBlocksTexture);
				FluidRenderer.setColorForFluidStack(fluidStack);
				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
		FluidStack fluidStack = trans.renderCache[ForgeDirection.UNKNOWN.ordinal()];

		if (fluidStack != null && fluidStack.amount > 0) {
			DisplayFluidList d = getListFromBuffer(fluidStack, skylight, blocklight, fluidStack.getFluid().getLuminosity(fluidStack), pipe.container.getWorldObj());

			if (d != null) {
				// XXX int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));
				int stage = (int) ((float) fluidStack.amount / (float) (trans.getInnerCapacity()) * (LogisticsRenderPipe.LIQUID_STAGES - 1));

				bindTexture(TextureMap.locationBlocksTexture);
				FluidRenderer.setColorForFluidStack(fluidStack);

				if (above) {
					GL11.glCallList(d.centerVertical[stage]);
				}

				if (!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);
				}
			}

		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	// BC copy
	private DisplayFluidList getListFromBuffer(FluidStack stack, int skylight, int blocklight, int flags, World world) {

		int liquidId = stack.getFluidID();

		if (liquidId == 0) {
			return null;
		}

		return getDisplayFluidLists(liquidId, skylight, blocklight, flags, world);
	}

	// BC copy
	private DisplayFluidList getDisplayFluidLists(int liquidId, int skylight, int blocklight, int flags, World world) {
		int finalBlockLight = Math.max(flags & 31, blocklight);
		int listId = (liquidId & 0x3FFFF) << 13 | (flags & 0xE0 | finalBlockLight) << 5 | (skylight & 31);

		if (displayFluidLists.containsItem(listId)) {
			return (DisplayFluidList) displayFluidLists.lookup(listId);
		}

		Fluid fluid = FluidRegistry.getFluid(liquidId);

		if (fluid == null) {
			return null;
		}

		DisplayFluidList d = new DisplayFluidList();
		displayFluidLists.addKey(listId, d);

		RenderInfo block = new RenderInfo();

		if (fluid.getBlock() != null) {
			block.baseBlock = fluid.getBlock();
		} else {
			block.baseBlock = Blocks.water;
		}

		block.texture = fluid.getStillIcon();
		block.brightness = skylight << 16 | finalBlockLight;

		float size = LPConstants.BC_PIPE_MAX_POS - LPConstants.BC_PIPE_MIN_POS;

		// render size

		for (int s = 0; s < LogisticsRenderPipe.LIQUID_STAGES; ++s) {
			float ratio = (float) s / (float) LogisticsRenderPipe.LIQUID_STAGES;

			// SIDE HORIZONTAL

			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], GL11.GL_COMPILE);

			block.minX = 0.0F;
			block.minZ = LPConstants.BC_PIPE_MIN_POS + 0.01F;

			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;

			block.minY = LPConstants.BC_PIPE_MIN_POS + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			CustomBlockRenderer.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// SIDE VERTICAL

			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], GL11.GL_COMPILE);

			block.minY = LPConstants.BC_PIPE_MAX_POS - 0.01;
			block.maxY = 1;

			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;

			CustomBlockRenderer.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER HORIZONTAL

			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], GL11.GL_COMPILE);

			block.minX = LPConstants.BC_PIPE_MIN_POS + 0.01;
			block.minZ = LPConstants.BC_PIPE_MIN_POS + 0.01;

			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;

			block.minY = LPConstants.BC_PIPE_MIN_POS + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			CustomBlockRenderer.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER VERTICAL

			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], GL11.GL_COMPILE);

			block.minY = LPConstants.BC_PIPE_MIN_POS + 0.01;
			block.maxY = LPConstants.BC_PIPE_MAX_POS - 0.01;

			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;

			CustomBlockRenderer.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

		}

		return d;
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
