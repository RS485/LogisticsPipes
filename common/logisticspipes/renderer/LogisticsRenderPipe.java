package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.newpipe.LogisticsNewPipeItemBoxRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsRenderPipe extends TileEntitySpecialRenderer<LogisticsTileGenericPipe> {

	private static final int LIQUID_STAGES = 40;
	private static final int MAX_ITEMS_TO_RENDER = 10;
	private static final ResourceLocation SIGN = new ResourceLocation("textures/entity/sign.png");
	public static LogisticsNewRenderPipe secondRenderer = new LogisticsNewRenderPipe();
	public static LogisticsNewPipeItemBoxRenderer boxRenderer = new LogisticsNewPipeItemBoxRenderer();
	public static ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
	private static ItemStackRenderer itemRenderer = new ItemStackRenderer(0, 0, 0, false, false);
	private ModelSign modelSign;

	public LogisticsRenderPipe() {
		super();
		modelSign = new ModelSign();
		modelSign.signStick.showModel = false;
	}

	@Override
	public void render(LogisticsTileGenericPipe tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		boolean inHand = false;
		if (tileentity == null && x == 0 && y == 0 && z == 0) {
			inHand = true;
		} else if (tileentity.pipe == null) {
			return;
		}

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);

		if (destroyStage >= 0) {
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4.0F, 4.0F, 1.0F);
			//GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if (destroyStage < 0) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		}

		if (!inHand) {
			if (tileentity.pipe instanceof CoreRoutedPipe) {
				renderPipeSigns((CoreRoutedPipe) tileentity.pipe, x, y, z, partialTicks);
			}
		}

		double distance = !inHand ? new DoubleCoordinates((TileEntity) tileentity).distanceTo(new DoubleCoordinates(Minecraft.getMinecraft().player)) : 0;

		LogisticsRenderPipe.secondRenderer.renderTileEntityAt(tileentity, x, y, z, partialTicks, distance);

		if (!inHand && !tileentity.isOpaque()) {
			if (tileentity.pipe.transport instanceof PipeFluidTransportLogistics) {
				//renderFluids(pipe.pipe, x, y, z);
			}
			if (tileentity.pipe.transport != null) {
				renderSolids(tileentity.pipe, x, y, z, partialTicks);
			}
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (destroyStage >= 0) {
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}

		if (!inHand) {
			SimpleServiceLocator.mcmpProxy.renderTileEntitySpecialRenderer(tileentity, x, y, z, partialTicks, destroyStage, alpha);
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

			ItemStack stack = item.getItemIdentifierStack().makeNormalStack();
			doRenderItem(stack, pipe.container.getWorld(), lX + pos.getXCoord(), lY + pos.getYCoord(), lZ + pos.getZCoord(), light, 0.75F, boxScale, itemYaw, itemPitch, itemYawForPitch, partialTickTime);
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
			ItemStack stack = item.getValue1().makeNormalStack();
			doRenderItem(stack, pipe.container.getWorld(), x + pos.getXCoord(), y + pos.getYCoord(), z + pos.getZCoord(), light, 0.25F, 0, 0, 0, 0, partialTickTime);
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

	public void doRenderItem(@Nonnull ItemStack itemstack, World world, double x, double y, double z, float light, float renderScale, double boxScale, double yaw, double pitch, double yawForPitch, float partialTickTime) {
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

		type.render(pipe, this);
	}

	public void renderItemStackOnSign(@Nonnull ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			return; // Only happens on false configuration
		}

		Minecraft mc = Minecraft.getMinecraft();
		RenderItem itemRender = mc.getRenderItem();

		GlStateManager.disableLighting();
		GlStateManager.color(1F, 1F, 1F); //Forge: Reset color in case Items change it.
		GlStateManager.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
		itemRender.zLevel = 100.0F;
		GlStateManager.enableRescaleNormal();

		// itemRender.renderItemAndEffectIntoGUI(itemstack, 0, 0);
		// item render code
		GlStateManager.pushMatrix();
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// mezz.jei.render.ItemStackFastRenderer#getBakedModel
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		IBakedModel bakedModel = itemModelMesher.getItemModel(itemstack);
		bakedModel = bakedModel.getOverrides().handleItemState(bakedModel, itemstack, null, null);

		// make item/block flat and position it
		GlStateManager.translate(0.05F, 0F, 0F);
		GlStateManager.scale(0.8F, 0.8F, 0.001F);

		// model rotation
		bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);

		// model scaling to fit on sign
		GlStateManager.scale(0.4F, 0.4F, 0.4F);

		itemRender.renderItem(itemstack, bakedModel);

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		// item render code end

		// not needed?
		//itemRender.renderItemOverlays(mc.fontRenderer, itemstack, 0, 0);
		itemRender.zLevel = 0.0F;
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
}
