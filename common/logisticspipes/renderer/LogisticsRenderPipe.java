package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.MatrixTranformations;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
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

import buildcraft.api.transport.PipeWire;


public class LogisticsRenderPipe extends TileEntitySpecialRenderer {

	final static private int LIQUID_STAGES = 40;
	final static private int MAX_ITEMS_TO_RENDER = 10;
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };
	
	private HashMap<Integer, DisplayFluidList> displayFluidLists = new HashMap<Integer, DisplayFluidList>();

    private ModelSign modelSign = new ModelSign();

	private RenderBlocks renderBlocks = new RenderBlocks();
	
	private class DisplayFluidList {
		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}
	
	public LogisticsRenderPipe() {
		super();
		customRenderItem = new RenderItem() {
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
		dummyEntityItem.age = 0;
		dummyEntityItem.hoverStart = 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		if(!(tileentity instanceof LogisticsTileGenericPipe)) return;
		LogisticsTileGenericPipe pipe = ((LogisticsTileGenericPipe)tileentity);
		if(pipe.pipe == null) return;
		renderGatesWires(pipe, x, y, z);
		if(!pipe.isOpaque()) {
			if(pipe.pipe.transport instanceof PipeFluidTransportLogistics) {
				renderFluids(pipe.pipe, x, y, z);
			}
			if(pipe.pipe.transport instanceof PipeTransportLogistics) {
				renderSolids(pipe.pipe, x, y, z, f);
			}
		}
		if(pipe.pipe instanceof CoreRoutedPipe) {
			renderPipePipe((CoreRoutedPipe)pipe.pipe, x, y, z);
		}
	}



	private void renderGatesWires(LogisticsTileGenericPipe pipe, double x, double y, double z) {
		PipeRenderState state = pipe.renderState;

		if (state.wireMatrix.hasWire(PipeWire.RED)) {
			pipeWireRender(pipe, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MIN_POS, PipeWire.RED, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.BLUE)) {
			pipeWireRender(pipe, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, PipeWire.BLUE, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.GREEN)) {
			pipeWireRender(pipe, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, PipeWire.GREEN, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.YELLOW)) {
			pipeWireRender(pipe, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, PipeWire.YELLOW, x, y, z);
		}

		if (pipe.pipe.gate != null) {
			pipeGateRender(pipe, x, y, z);
		}
	}

	private void pipeWireRender(LogisticsTileGenericPipe pipe, float cx, float cy, float cz, PipeWire color, double x, double y, double z) {

		PipeRenderState state = pipe.renderState;

		float minX = LPConstants.PIPE_MIN_POS;
		float minY = LPConstants.PIPE_MIN_POS;
		float minZ = LPConstants.PIPE_MIN_POS;

		float maxX = LPConstants.PIPE_MAX_POS;
		float maxY = LPConstants.PIPE_MAX_POS;
		float maxZ = LPConstants.PIPE_MAX_POS;

		boolean foundX = false, foundY = false, foundZ = false;

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.WEST)) {
			minX = 0;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.EAST)) {
			maxX = 1;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.DOWN)) {
			minY = 0;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.UP)) {
			maxY = 1;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.NORTH)) {
			minZ = 0;
			foundZ = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.SOUTH)) {
			maxZ = 1;
			foundZ = true;
		}

		boolean center = false;

		if (minX == 0 && maxX != 1 && (foundY || foundZ)) {
			if (cx == LPConstants.PIPE_MIN_POS) {
				maxX = LPConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minX != 0 && maxX == 1 && (foundY || foundZ)) {
			if (cx == LPConstants.PIPE_MAX_POS) {
				minX = LPConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		if (minY == 0 && maxY != 1 && (foundX || foundZ)) {
			if (cy == LPConstants.PIPE_MIN_POS) {
				maxY = LPConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minY != 0 && maxY == 1 && (foundX || foundZ)) {
			if (cy == LPConstants.PIPE_MAX_POS) {
				minY = LPConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		if (minZ == 0 && maxZ != 1 && (foundX || foundY)) {
			if (cz == LPConstants.PIPE_MIN_POS) {
				maxZ = LPConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minZ != 0 && maxZ == 1 && (foundX || foundY)) {
			if (cz == LPConstants.PIPE_MAX_POS) {
				minZ = LPConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		boolean found = foundX || foundY || foundZ;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		RenderHelper.disableStandardItemLighting();

		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef((float) x, (float) y, (float) z);

		float scale = 1.001f;
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glScalef(scale, scale, scale);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);


		bindTexture(TextureMap.locationBlocksTexture);

		RenderInfo renderBox = new RenderInfo();
		renderBox.texture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(color));

		// Z render

		if (minZ != LPConstants.PIPE_MIN_POS || maxZ != LPConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(cx == LPConstants.PIPE_MIN_POS ? cx - 0.05F : cx, cy == LPConstants.PIPE_MIN_POS ? cy - 0.05F : cy, minZ, cx == LPConstants.PIPE_MIN_POS ? cx
					: cx + 0.05F, cy == LPConstants.PIPE_MIN_POS ? cy : cy + 0.05F, maxZ);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox, pipe.getWorldObj(), 0, 0, 0, pipe.xCoord, pipe.yCoord, pipe.zCoord, true, true);
		}

		// X render

		if (minX != LPConstants.PIPE_MIN_POS || maxX != LPConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(minX, cy == LPConstants.PIPE_MIN_POS ? cy - 0.05F : cy, cz == LPConstants.PIPE_MIN_POS ? cz - 0.05F : cz, maxX, cy == LPConstants.PIPE_MIN_POS ? cy
					: cy + 0.05F, cz == LPConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox, pipe.getWorldObj(), 0, 0, 0, pipe.xCoord, pipe.yCoord, pipe.zCoord, true, true);
		}

		// Y render

		if (minY != LPConstants.PIPE_MIN_POS || maxY != LPConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(cx == LPConstants.PIPE_MIN_POS ? cx - 0.05F : cx, minY, cz == LPConstants.PIPE_MIN_POS ? cz - 0.05F : cz, cx == LPConstants.PIPE_MIN_POS ? cx
					: cx + 0.05F, maxY, cz == LPConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox, pipe.getWorldObj(), 0, 0, 0, pipe.xCoord, pipe.yCoord, pipe.zCoord, true, true);
		}

		if (center || !found) {
			renderBox.setBounds(cx == LPConstants.PIPE_MIN_POS ? cx - 0.05F : cx, cy == LPConstants.PIPE_MIN_POS ? cy - 0.05F : cy, cz == LPConstants.PIPE_MIN_POS ? cz - 0.05F : cz,
					cx == LPConstants.PIPE_MIN_POS ? cx : cx + 0.05F, cy == LPConstants.PIPE_MIN_POS ? cy : cy + 0.05F, cz == LPConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox, pipe.getWorldObj(), 0, 0, 0, pipe.xCoord, pipe.yCoord, pipe.zCoord, true, true);
		}

		RenderHelper.enableStandardItemLighting();

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private void pipeGateRender(LogisticsTileGenericPipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
//		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderHelper.disableStandardItemLighting();

		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef((float) x, (float) y, (float) z);

		bindTexture(TextureMap.locationBlocksTexture);

		IIcon iconLogic;
		if (pipe.renderState.isGateLit()) {
			iconLogic = pipe.pipe.gate.logic.getIconLit();
		} else {
			iconLogic = pipe.pipe.gate.logic.getIconDark();
		}

		float translateCenter = 0;

		// Render base gate
		renderGate(pipe, iconLogic, 0, 0.1F, 0, 0);

		float pulseStage = pipe.pipe.gate.getPulseStage() * 2F;

		if (pipe.renderState.isGatePulsing() || pulseStage != 0) {
			// Render pulsing gate
			float amplitude = 0.10F;
			float start = 0.01F;

			if (pulseStage < 1) {
				translateCenter = (pulseStage * amplitude) + start;
			} else {
				translateCenter = amplitude - ((pulseStage - 1F) * amplitude) + start;
			}

			renderGate(pipe, iconLogic, 0, 0.13F, translateCenter, translateCenter);
		}

		IIcon materialIcon = pipe.pipe.gate.material.getIconBlock();
		if (materialIcon != null) {
			renderGate(pipe, materialIcon, 1, 0.13F, translateCenter, translateCenter);
		}

		for (IGateExpansion expansion : pipe.pipe.gate.expansions.keySet()) {
			renderGate(pipe, expansion.getOverlayBlock(), 2, 0.13F, translateCenter, translateCenter);
		}

		RenderHelper.enableStandardItemLighting();

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private void renderGate(LogisticsTileGenericPipe tile, IIcon icon, int layer, float trim, float translateCenter, float extraDepth) {
		PipeRenderState state = tile.renderState;

		RenderInfo renderBox = new RenderInfo();
		renderBox.texture = icon;

		float[][] zeroState = new float[3][2];
		float min = LPConstants.PIPE_MIN_POS + trim / 2F;
		float max = LPConstants.PIPE_MAX_POS - trim / 2F;

		// X START - END
		zeroState[0][0] = min;
		zeroState[0][1] = max;
		// Y START - END
		zeroState[1][0] = LPConstants.PIPE_MIN_POS - 0.10F - 0.001F * layer;
		zeroState[1][1] = LPConstants.PIPE_MIN_POS + 0.001F + 0.01F * layer + extraDepth;
		// Z START - END
		zeroState[2][0] = min;
		zeroState[2][1] = max;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (shouldRenderNormalPipeSide(state, direction)) {
				GL11.glPushMatrix();

				float xt = direction.offsetX * translateCenter,
						yt = direction.offsetY * translateCenter,
						zt = direction.offsetZ * translateCenter;

				GL11.glTranslatef(xt, yt, zt);

				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				if (layer != 0) {
					renderBox.setRenderSingleSide(direction.ordinal());
				}
				renderBox.setBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				RenderEntityBlock.INSTANCE.renderBlock(renderBox, tile.getWorldObj(), 0, 0, 0, tile.xCoord, tile.yCoord, tile.zCoord, true, true);
				GL11.glPopMatrix();
			}
		}
	}

	private void renderSolids(CoreUnroutedPipe pipe, double x, double y, double z, float f) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		
		float light = pipe.container.getWorldObj().getLightBrightness(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		
		int count = 0;
		for(LPTravelingItem item: pipe.transport.items) {
			if(count >= MAX_ITEMS_TO_RENDER) {
				break;
			}
			
			LPPosition pos = new LPPosition(0.5D, 0.5D, 0.5D);
			
			if(item.getPosition() > 1 || item.getPosition() < 0) {
				continue;
			}
			
			float fPos = item.getPosition() + item.getSpeed() * f;
			
			if(fPos < 0.5) {
				if(item.input == ForgeDirection.UNKNOWN) continue;
				if(!pipe.container.renderState.pipeConnectionMatrix.isConnected(item.input.getOpposite())) continue;
				pos.moveForward(item.input.getOpposite(), 0.5F - fPos);
			} else {
				if(item.output == ForgeDirection.UNKNOWN) continue;
				if(!pipe.container.renderState.pipeConnectionMatrix.isConnected(item.output)) continue;
				pos.moveForward(item.output, fPos - 0.5F);
			}
			
			if(item == null || item.getItemIdentifierStack() == null) continue;
			if(item.getContainer().xCoord != pipe.container.xCoord || item.getContainer().yCoord != pipe.container.yCoord || item.getContainer().zCoord != pipe.container.zCoord) continue;
			ItemStack itemstack = item.getItemIdentifierStack().makeNormalStack();
			doRenderItem(itemstack, x + pos.getXD(), y + pos.getYD(), z + pos.getZD(), light, item.getAge(), item.getHoverStart());
			count++;
		}
		
		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}
	
	public void doRenderItem(ItemStack itemstack, double x, double y, double z, float light, int age, float hoverStart) {
		float renderScale = 0.7f;
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(itemstack);
		dummyEntityItem.age = age;
		dummyEntityItem.hoverStart = hoverStart;
		customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
		dummyEntityItem.age = 0;
		dummyEntityItem.hoverStart = 0;
		GL11.glPopMatrix();
	}
	
	private boolean needDistance(List<Pair<ForgeDirection, IPipeSign>> list) {
		List<Pair<ForgeDirection, IPipeSign>> copy = new ArrayList<Pair<ForgeDirection, IPipeSign>>(list);
		Iterator<Pair<ForgeDirection, IPipeSign>> iter = copy.iterator();
		boolean north = false, south = false, east = false, west = false;
		while(iter.hasNext()) {
			Pair<ForgeDirection, IPipeSign> pair = iter.next();
			if(pair.getValue1() == ForgeDirection.UP || pair.getValue1() == ForgeDirection.DOWN || pair.getValue1() == ForgeDirection.UNKNOWN) {
				iter.remove();
			}
			if(pair.getValue1() == ForgeDirection.NORTH) north = true;
			if(pair.getValue1() == ForgeDirection.SOUTH) south = true;
			if(pair.getValue1() == ForgeDirection.EAST) east = true;
			if(pair.getValue1() == ForgeDirection.WEST) west = true;
		}
		boolean result = copy.size() > 1;
		if(copy.size() == 2) {
			if(north && south) {
				result = false;
			}
			if(east && west) {
				result = false;
			}
		}
		return result;
	}
	
	private void renderPipePipe(CoreRoutedPipe pipe, double x, double y, double z) {
		if(!pipe.getPipeSigns().isEmpty()) {
			List<Pair<ForgeDirection, IPipeSign>> list = pipe.getPipeSigns();
			for(Pair<ForgeDirection, IPipeSign> pair: list) {
				if(pipe.container.renderState.pipeConnectionMatrix.isConnected(pair.getValue1())) {
					continue;
				}
				GL11.glPushMatrix();
				GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
				switch(pair.getValue1()) {
					case UP:
						GL11.glRotatef(90, 1.0F, 0.0F, 0.0F);
						break;
					case DOWN:
						GL11.glRotatef(-90, 1.0F, 0.0F, 0.0F);
						break;
					case NORTH:
						GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
						if(needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case SOUTH:
						GL11.glRotatef(-180, 0.0F, 1.0F, 0.0F);
						if(needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case EAST:
						GL11.glRotatef(-90, 0.0F, 1.0F, 0.0F);
						if(needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					case WEST:
						GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
						if(needDistance(list)) {
							GL11.glTranslatef(0.0F, 0.0F, -0.15F);
						}
						break;
					default:;
				}
				renderSign(pipe, pair.getValue2());
				GL11.glPopMatrix();
			}
		}
	}
	
	private static final ResourceLocation	SIGN	= new ResourceLocation("textures/entity/sign.png");
	private static final ResourceLocation	BLOCKS	= new ResourceLocation("textures/atlas/blocks.png");
	private static final ResourceLocation	ITEMS	= new ResourceLocation("textures/atlas/items.png");
	
	private void renderSign(CoreRoutedPipe pipe, IPipeSign type) {
		float var10 = 0.6666667F;
		
		GL11.glTranslatef(0.0F, -0.3125F, -0.31F);
		GL11.glRotatef(180, 0.0f, 1.0f, 0.0f);
		SimpleServiceLocator.betterSignProxy.hideSignSticks(this.modelSign);
		Minecraft.getMinecraft().renderEngine.bindTexture(SIGN);
		
		GL11.glPushMatrix();
		GL11.glScalef(var10, -var10, -var10);
		this.modelSign.renderSign();
		GL11.glPopMatrix();
		GL11.glTranslatef(-0.32F, 0.5F * var10 + 0.08F, 0.07F * var10);
		
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		renderSignLabel(pipe, type);
		GL11.glPopAttrib();
	}
	
	private void renderSignLabel(CoreRoutedPipe pipe, IPipeSign type) {
		type.render(pipe, this);
	}
	
	public void renderItemStackOnSign(ItemStack itemstack) {
		if(itemstack == null || itemstack.getItem() == null) return; // Only happens on false configuration
			
		Item item = itemstack.getItem();
		
		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.INVENTORY);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(itemstack.getItemSpriteNumber() == 0 ? BLOCKS : ITEMS);
		
		GL11.glPushMatrix();
		
		if(customRenderer != null) {
			if(customRenderer.shouldUseRenderHelper(ItemRenderType.INVENTORY, itemstack, ItemRendererHelper.INVENTORY_BLOCK)) {
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
		} else if(item instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType())) {
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
			
			if(item.requiresMultipleRenderPasses()) {
				for(int var14 = 0; var14 < item.getRenderPasses(itemstack.getItemDamage()); ++var14) {
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
		if(par3Icon == null) return;
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
		if(renderer.getStringWidth(name) < 90) { return name; }
		StringBuilder sum = new StringBuilder();
		for(int i = 0; i < name.length(); i++) {
			if(renderer.getStringWidth(sum.toString() + name.charAt(i) + "...") < 90) {
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
		PipeFluidTransportLogistics trans = (PipeFluidTransportLogistics)(pipe.transport);
		
		boolean needsRender = false;
		for(int i = 0; i < 7; ++i) {
			FluidStack fluidStack = trans.renderCache[i];
			if(fluidStack != null && fluidStack.amount > 0) {
				needsRender = true;
				break;
			}
		}
		
		if(!needsRender) return;
		
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glTranslatef((float)x, (float)y, (float)z);
		
		// sides
		
		boolean sides = false, above = false;
		
		for(int i = 0; i < 6; ++i) {
			FluidStack fluidStack = trans.renderCache[i];
			
			if(fluidStack != null && fluidStack.amount > 0) {
				DisplayFluidList d = getListFromBuffer(fluidStack, pipe.container.getWorldObj());
				
				if(d == null) {
					continue;
				}
				
				// XXX int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));
				int stage = (int)((float)fluidStack.amount / (float)(trans.getSideCapacity()) * (LIQUID_STAGES - 1));
				
				GL11.glPushMatrix();
				int list = 0;
				
				switch(ForgeDirection.VALID_DIRECTIONS[i]) {
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
		
		if(fluidStack != null && fluidStack.amount > 0) {
			DisplayFluidList d = getListFromBuffer(fluidStack, pipe.container.getWorldObj());
			
			if(d != null) {
				// XXX int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));
				int stage = (int)((float)fluidStack.amount / (float)(trans.getInnerCapacity()) * (LIQUID_STAGES - 1));
				
				bindTexture(TextureMap.locationBlocksTexture);
				FluidRenderer.setColorForFluidStack(fluidStack);
				
				if(above) {
					GL11.glCallList(d.centerVertical[stage]);
				}
				
				if(!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);
				}
			}
			
		}
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
	
	// BC copy
	private DisplayFluidList getListFromBuffer(FluidStack stack, World world) {
		
		int liquidId = stack.fluidID;
		
		if(liquidId == 0) return null;
		
		return getDisplayFluidLists(liquidId, world);
	}
	
	// BC copy
	private DisplayFluidList getDisplayFluidLists(int liquidId, World world) {
		if(displayFluidLists.containsKey(liquidId)) { return displayFluidLists.get(liquidId); }
		
		DisplayFluidList d = new DisplayFluidList();
		displayFluidLists.put(liquidId, d);
		
		RenderInfo block = new RenderInfo();
		
		Fluid fluid = FluidRegistry.getFluid(liquidId);
		block.baseBlock = fluid.getBlock();
		block.texture = fluid.getStillIcon();
		
		float size = LPConstants.PIPE_MAX_POS - LPConstants.PIPE_MIN_POS;
		
		// render size
		
		for(int s = 0; s < LIQUID_STAGES; ++s) {
			float ratio = (float)s / (float)LIQUID_STAGES;
			
			// SIDE HORIZONTAL
			
			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], 4864 /* GL_COMPILE */);
			
			block.minX = 0.0F;
			block.minZ = LPConstants.PIPE_MIN_POS + 0.01F;
			
			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;
			
			block.minY = LPConstants.PIPE_MIN_POS + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;
			
			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
			
			// SIDE VERTICAL
			
			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], 4864 /* GL_COMPILE */);
			
			block.minY = LPConstants.PIPE_MAX_POS - 0.01;
			block.maxY = 1;
			
			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;
			
			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;
			
			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
			
			// CENTER HORIZONTAL
			
			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], 4864 /* GL_COMPILE */);
			
			block.minX = LPConstants.PIPE_MIN_POS + 0.01;
			block.minZ = LPConstants.PIPE_MIN_POS + 0.01;
			
			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;
			
			block.minY = LPConstants.PIPE_MIN_POS + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;
			
			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
			
			// CENTER VERTICAL
			
			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], 4864 /* GL_COMPILE */);
			
			block.minY = LPConstants.PIPE_MIN_POS + 0.01;
			block.maxY = LPConstants.PIPE_MAX_POS - 0.01;
			
			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;
			
			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;
			
			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
			
		}
		
		return d;
	}
}
