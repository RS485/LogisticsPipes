package logisticspipes.ticks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
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
import logisticspipes.renderer.GuiOverlay;
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
			if (GuiOverlay.getInstance().isCompatibleGui()) {
				GuiOverlay.getInstance().preRender();
			}
			ClientViewController.instance().tick();
		} else {
			renderTicks++;
			if (LogisticsHUDRenderer.instance().displayRenderer()) {
				GL11.glPushMatrix();
				Minecraft mc = FMLClientHandler.instance().getClient();
				mc.entityRenderer.setupCameraTransform(event.renderTickTime, 1);
				ActiveRenderInfo.updateRenderInfo(mc.player, mc.gameSettings.thirdPersonView == 2);
				LogisticsHUDRenderer.instance().renderWorldRelative(renderTicks, event.renderTickTime);
				mc.entityRenderer.setupOverlayRendering();
				GL11.glPopMatrix();

				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				GL11.glPopMatrix();
			} else if (GuiOverlay.getInstance().isCompatibleGui()) {
				GuiOverlay.getInstance().renderOverGui();
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderWorldLast(RenderWorldLastEvent worldEvent) {
		// We are not holding an Item that needs to render a ghost pipe!
		if (!displayPipeGhost()) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		RayTraceResult box = mc.objectMouseOver;

		// The box is null or we are targeting something else than a block!
		if (box == null || box.typeOfHit != RayTraceResult.Type.BLOCK) return;

		InventoryPlayer inventory = FMLClientHandler.instance().getClient().player.inventory;
		ItemStack stack = inventory.mainInventory.get(inventory.currentItem);
		CoreUnroutedPipe pipe = ((ItemLogisticsPipe) stack.getItem()).getDummyPipe();
		World world = player.getEntityWorld();
		EnumFacing side = box.sideHit;
		BlockPos pos = box.getBlockPos();
		Block block = world.getBlockState(pos).getBlock();

		if (block == Blocks.SNOW_LAYER && block.isReplaceable(world, pos)) {
			side = EnumFacing.UP;
		} else if (!block.isReplaceable(world, pos)) {
			pos = pos.offset(side);
		}

		boolean isFreeSpace = true;
		ITubeOrientation orientation = null;

		if (pipe instanceof CoreMultiBlockPipe) {
			CoreMultiBlockPipe multiPipe = (CoreMultiBlockPipe) pipe;
			DoubleCoordinates placeAt = new DoubleCoordinates(pos);
			LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> globalPos = new LPPositionSet<>(DoubleCoordinatesType.class);
			globalPos.add(new DoubleCoordinatesType<>(placeAt, CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
			LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> positions = multiPipe.getSubBlocks();
			orientation = multiPipe.getTubeOrientation(player, pos.getX(), pos.getZ());

			if (orientation == null) return;

			orientation.rotatePositions(positions);
			positions.stream().map(p -> p.add(placeAt)).forEach(globalPos::add);
			globalPos.addToAll(orientation.getOffset());

			for (DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare> posType : globalPos) {
				if (!world.mayPlace(LPBlocks.pipe, posType.getBlockPos(), false, side, player)) {
					TileEntity tile = world.getTileEntity(posType.getBlockPos());
					boolean canPlace = false;
					if (tile instanceof LogisticsTileGenericSubMultiBlock) {
						if (CoreMultiBlockPipe.canShare(((LogisticsTileGenericSubMultiBlock) tile).getSubTypes(), posType.getType())) {
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
			if (!world.mayPlace(LPBlocks.pipe, pos, false, side, player)) {
				isFreeSpace = false;
			}
		}

		// No free space to render anything!
		if (!isFreeSpace) return;

		GlStateManager.pushMatrix();
		double x;
		double y;
		double z;
		if (orientation != null) {
			x = pos.getX() + orientation.getOffset().getXInt() - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
			y = pos.getY() + orientation.getOffset().getYInt() - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
			z = pos.getZ() + orientation.getOffset().getZInt() - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
		} else {
			x = pos.getX() - player.prevPosX - ((player.posX - player.prevPosX) * worldEvent.getPartialTicks());
			y = pos.getY() - player.prevPosY - ((player.posY - player.prevPosY) * worldEvent.getPartialTicks());
			z = pos.getZ() - player.prevPosZ - ((player.posZ - player.prevPosZ) * worldEvent.getPartialTicks());
		}
		GL11.glTranslated(x + 0.001, y + 0.001, z + 0.001);

		GlStateManager.enableBlend();
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

	private boolean displayPipeGhost() {
		EntityPlayer player = FMLClientHandler.instance().getClient().player;
		if (player == null) return false;

		InventoryPlayer pInventory = player.inventory;
		if (pInventory == null) return false;

		NonNullList<ItemStack> inv = pInventory.mainInventory;
		if (inv == null) return false;

		return inv.size() > pInventory.currentItem
				&& inv.get(pInventory.currentItem).getItem() instanceof ItemLogisticsPipe;
	}
}
