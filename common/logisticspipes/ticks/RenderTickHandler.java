package logisticspipes.ticks;

import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.debug.ClientViewController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;

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
}
