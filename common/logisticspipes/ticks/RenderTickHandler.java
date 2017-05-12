package logisticspipes.ticks;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsRenderPipe;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.LPPositionSet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
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

import network.rs485.logisticspipes.world.DoubleCoordinatesType;
import org.lwjgl.opengl.GL11;

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
				ActiveRenderInfo.updateRenderInfo(mc.thePlayer, mc.gameSettings.thirdPersonView == 2);
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
				EntityPlayer player = mc.thePlayer;
				RayTraceResult box = mc.objectMouseOver;
				if (box != null && box.typeOfHit == RayTraceResult.Type.BLOCK) {
					ItemStack stack = FMLClientHandler.instance().getClient().thePlayer.inventory.mainInventory[FMLClientHandler.instance().getClient().thePlayer.inventory.currentItem];
					CoreUnroutedPipe pipe = ((ItemLogisticsPipe) stack.getItem()).getDummyPipe();

					int i = box.blockX;
					int j = box.blockY;
					int k = box.blockZ;
					World world = player.getEntityWorld();
					int side = box.sideHit;

					Block worldBlock = world.getBlock(i, j, k);

					if (worldBlock == Blocks.snow) {
						side = 1;
					} else if (worldBlock != Blocks.vine && worldBlock != Blocks.tallgrass && worldBlock != Blocks.deadbush && (worldBlock == null || !worldBlock.isReplaceable(world, i, j, k))) {
						if (side == 0) {
							j--;
						}
						if (side == 1) {
							j++;
						}
						if (side == 2) {
							k--;
						}
						if (side == 3) {
							k++;
						}
						if (side == 4) {
							i--;
						}
						if (side == 5) {
							i++;
						}
					}

					double xCoord = i;
					double yCoord = j;
					double zCoord = k;

					boolean isFreeSpace = true;
					ITubeOrientation orientation = null;

					if (pipe instanceof CoreMultiBlockPipe) {
						CoreMultiBlockPipe multipipe = (CoreMultiBlockPipe) pipe;
						DoubleCoordinates placeAt = new DoubleCoordinates(xCoord, yCoord, zCoord);
						LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> globalPos = new LPPositionSet<>(DoubleCoordinatesType.class);
						globalPos.add(new DoubleCoordinatesType<>(placeAt, CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
						LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> positions = multipipe.getSubBlocks();
						orientation = multipipe.getTubeOrientation(player, (int) xCoord, (int) zCoord);
						if (orientation != null) {
							orientation.rotatePositions(positions);
							positions.stream().map(pos -> pos.add(placeAt)).forEach(globalPos::add);
							globalPos.addToAll(orientation.getOffset());

							for (DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare> pos : globalPos) {
								if (!player.getEntityWorld().canPlaceEntityOnSide(LogisticsPipes.LogisticsPipeBlock, pos.getXInt(), pos.getYInt(), pos.getZInt(), false, side, player, stack)) {
									TileEntity tile = player.getEntityWorld().getTileEntity(pos.getXInt(), pos.getYInt(), pos.getZInt());
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
						if (!player.getEntityWorld().canPlaceEntityOnSide(LogisticsPipes.LogisticsPipeBlock, i, j, k, false, side, player, stack)) {
							isFreeSpace = false;
						}
					}
					if (isFreeSpace) {
						GL11.glPushMatrix();
						double x;
						double y;
						double z;
						if (orientation != null) {
							x = xCoord + orientation.getOffset().getXInt() - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
							y = yCoord + orientation.getOffset().getYInt() - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
							z = zCoord + orientation.getOffset().getZInt() - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
						} else {
							x = xCoord - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
							y = yCoord - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
							z = zCoord - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
						}
						GL11.glTranslated(x + 0.001, y + 0.001, z + 0.001);

						GL11.glEnable(GL11.GL_BLEND);
						//GL11.glDepthMask(false);
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						mc.renderEngine.bindTexture(new ResourceLocation("logisticspipes", "textures/blocks/pipes/White.png"));

						SimpleServiceLocator.cclProxy.getRenderState().reset();
						SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

						GL11.glEnable(GL11.GL_TEXTURE_2D);

						SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0x50);
						CCRenderState.hasBrightness = false;
						SimpleServiceLocator.cclProxy.getRenderState().startDrawing(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

						pipe.getHighlightRenderer().renderHighlight(orientation);

						SimpleServiceLocator.cclProxy.getRenderState().draw();

						SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);
						GL11.glDisable(GL11.GL_BLEND);
						GL11.glDepthMask(true);
						GL11.glPopMatrix();
					}
				}
			}
		//}
	}

	private boolean displayPipeGhost() {
		return FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.inventory != null && FMLClientHandler.instance().getClient().thePlayer.inventory.mainInventory != null
				&& FMLClientHandler.instance().getClient().thePlayer.inventory.mainInventory.length > FMLClientHandler.instance().getClient().thePlayer.inventory.currentItem
				&& FMLClientHandler.instance().getClient().thePlayer.inventory.mainInventory[FMLClientHandler.instance().getClient().thePlayer.inventory.currentItem] != null
				&& checkItemStackForPipeGhost(FMLClientHandler.instance().getClient().thePlayer.inventory.mainInventory[FMLClientHandler.instance().getClient().thePlayer.inventory.currentItem]);
	}

	private boolean checkItemStackForPipeGhost(ItemStack stack) {
		if (stack.getItem() instanceof ItemLogisticsPipe) {
			return true;
		}
		return false;
	}

}
