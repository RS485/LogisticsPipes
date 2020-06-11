package logisticspipes.ticks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import logisticspipes.LPBlocks;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.LPPositionSet;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;

public class RenderTickHandler {

	private long renderTicks = 0;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void renderTick(RenderTickEvent event) {
		if (event.phase == Phase.START) {
			if (LogisticsGuiOverrenderer.getInstance().isCompatibleGui()) {
				LogisticsGuiOverrenderer.getInstance().preRender();
			}
			ClientViewController.instance().tick();
		} else {
			renderTicks++;
			if (LogisticsHUDRenderer.instance().displayRenderer()) {
				GL11.glPushMatrix();
				Minecraft mc = FMLClientHandler.instance().getClient();
				//Orientation
				mc.entityRenderer.setupCameraTransform(event.renderTickTime, 1);
				ActiveRenderInfo.updateRenderInfo(mc.player, mc.gameSettings.thirdPersonView == 2);
				LogisticsHUDRenderer.instance().renderWorldRelative(renderTicks, event.renderTickTime);
				mc.entityRenderer.setupOverlayRendering();
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				GL11.glPopMatrix();
			} else if (LogisticsGuiOverrenderer.getInstance().isCompatibleGui()) {
				LogisticsGuiOverrenderer.getInstance().renderOverGui();
			}
		}
	}

	//private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/blocks/pipes/White.png");

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderWorldLast(RenderWorldLastEvent worldEvent) {
		//if (LogisticsRenderPipe.config.isUseNewRenderer()) {
		if (displayPipeGhost()) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			RayTraceResult box = mc.objectMouseOver;
			if (box != null && box.typeOfHit == RayTraceResult.Type.BLOCK) {
				ItemStack stack = FMLClientHandler.instance().getClient().player.inventory.mainInventory.get(FMLClientHandler.instance().getClient().player.inventory.currentItem);
				CoreUnroutedPipe pipe = ((ItemLogisticsPipe) stack.getItem()).getDummyPipe();

				World world = player.getEntityWorld();
				EnumFacing side = box.sideHit;
				BlockPos bPos = box.getBlockPos();

				Block block = world.getBlockState(bPos).getBlock();

				if (block == Blocks.SNOW_LAYER && block.isReplaceable(world, bPos)) {
					side = EnumFacing.UP;
				} else if (!block.isReplaceable(world, bPos)) {
					bPos = bPos.offset(side);
				}

				boolean isFreeSpace = true;
				ITubeOrientation orientation = null;

				if (pipe instanceof CoreMultiBlockPipe) {
					CoreMultiBlockPipe multipipe = (CoreMultiBlockPipe) pipe;
					DoubleCoordinates placeAt = new DoubleCoordinates(bPos);
					LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> globalPos = new LPPositionSet<>(DoubleCoordinatesType.class);
					globalPos.add(new DoubleCoordinatesType<>(placeAt, CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
					LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> positions = multipipe.getSubBlocks();
					orientation = multipipe.getTubeOrientation(player, bPos.getX(), bPos.getZ());
					if (orientation != null) {
						orientation.rotatePositions(positions);
						positions.stream().map(pos -> pos.add(placeAt)).forEach(globalPos::add);
						globalPos.addToAll(orientation.getOffset());

						for (DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare> pos : globalPos) {
							if (!player.getEntityWorld().mayPlace(LPBlocks.pipe, pos.getBlockPos(), false, side, player)) {
								TileEntity tile = player.getEntityWorld().getTileEntity(pos.getBlockPos());
								boolean canPlace = false;
								if (tile instanceof LogisticsTileGenericSubMultiBlock) {
									if (CoreMultiBlockPipe.canShare(((LogisticsTileGenericSubMultiBlock) tile).getSubTypes(), pos.getType())) {
										canPlace = true;
									}
								}
								if (!canPlace) {
									isFreeSpace = false;
									break;
								}
							}
						}
					} else {
						return;
					}
				} else {
					if (!player.getEntityWorld().mayPlace(LPBlocks.pipe, bPos, false, side, player)) {
						isFreeSpace = false;
					}
				}
				if (isFreeSpace) {
					GlStateManager.pushMatrix();
					double x;
					double y;
					double z;
					if (orientation != null) {
						x = bPos.getX() + orientation.getOffset().getXInt() - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
						y = bPos.getY() + orientation.getOffset().getYInt() - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
						z = bPos.getZ() + orientation.getOffset().getZInt() - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
					} else {
						x = bPos.getX() - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
						y = bPos.getY() - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
						z = bPos.getZ() - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
					}
					GL11.glTranslated(x + 0.001, y + 0.001, z + 0.001);

					GlStateManager.enableBlend();
					//GL11.glDepthMask(false);
					GlStateManager.disableTexture2D();
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					mc.renderEngine.bindTexture(new ResourceLocation("logisticspipes", "textures/blocks/pipes/white.png"));

					SimpleServiceLocator.cclProxy.getRenderState().reset();
					SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

					GlStateManager.enableTexture2D();

					SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0x50);
					SimpleServiceLocator.cclProxy.getRenderState().startDrawing(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

					pipe.getHighlightRenderer().renderHighlight(orientation);

					SimpleServiceLocator.cclProxy.getRenderState().draw();

					SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);
					GlStateManager.disableBlend();
					GlStateManager.depthMask(true);
					GlStateManager.popMatrix();
				}
			}
		}
		//}
	}

	private boolean displayPipeGhost() {
		return FMLClientHandler.instance().getClient().player != null && FMLClientHandler.instance().getClient().player.inventory != null && FMLClientHandler.instance().getClient().player.inventory.mainInventory != null
				&& FMLClientHandler.instance().getClient().player.inventory.mainInventory.size() > FMLClientHandler.instance().getClient().player.inventory.currentItem
				&& !FMLClientHandler.instance().getClient().player.inventory.mainInventory.get(FMLClientHandler.instance().getClient().player.inventory.currentItem).isEmpty()
				&& checkItemStackForPipeGhost(FMLClientHandler.instance().getClient().player.inventory.mainInventory.get(FMLClientHandler.instance().getClient().player.inventory.currentItem));
	}

	private boolean checkItemStackForPipeGhost(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ItemLogisticsPipe;
	}

}
