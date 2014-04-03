package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.debug.ClientViewController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.SingleIntervalHandler;
import cpw.mods.fml.common.TickType;

public class RenderTickHandler implements ITickHandler {

	private long renderTicks=0;
	private Field ticks;
	private Field wrapper;

	public RenderTickHandler() {
		try {
			ticks = FMLCommonHandler.class.getDeclaredField("scheduledClientTicks");
			ticks.setAccessible(true);
			wrapper = SingleIntervalHandler.class.getDeclaredField("wrapped");
			wrapper.setAccessible(true);
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.RENDER)) {
			if(LogisticsHUDRenderer.instance().displayRenderer() || LogisticsGuiOverrenderer.getInstance().isCompatibleGui()) {
				try {
					@SuppressWarnings("unchecked")
					List<IScheduledTickHandler> old = (List<IScheduledTickHandler>) ticks.get(FMLCommonHandler.instance());
					List<IScheduledTickHandler> newList = new ArrayList<IScheduledTickHandler>(old.size());
					BitSet handled = new BitSet(old.size());
					for(int i = 0;i < old.size();i++) {
						IScheduledTickHandler handler = old.get(i);
						if(handler instanceof SingleIntervalHandler) {
							ITickHandler tick = (ITickHandler) wrapper.get(handler);
							if(tick == this) {
								newList.add(old.get(i));
								handled.set(i);
								break;
							}
						}
						
					}
					for(int i = 0;i < old.size();i++) {
						if(handled.get(i)) continue;
						newList.add(old.get(i));
					}
					ticks.set(FMLCommonHandler.instance(), newList);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(LogisticsGuiOverrenderer.getInstance().isCompatibleGui()) {
				LogisticsGuiOverrenderer.getInstance().preRender();
			}
			ClientViewController.instance().tick();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.RENDER)) {
			renderTicks++;
			if(LogisticsHUDRenderer.instance().displayRenderer()) {
				GL11.glPushMatrix();
				Minecraft mc = FMLClientHandler.instance().getClient();
				//Orientation
				mc.entityRenderer.setupCameraTransform((Float)tickData[0], 1);
				ActiveRenderInfo.updateRenderInfo(mc.thePlayer, mc.gameSettings.thirdPersonView == 2);
				LogisticsHUDRenderer.instance().renderWorldRelative(renderTicks, (Float) tickData[0]);
				mc.entityRenderer.setupOverlayRendering();
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				GL11.glPopMatrix();
			} else if(LogisticsGuiOverrenderer.getInstance().isCompatibleGui()) {
				LogisticsGuiOverrenderer.getInstance().renderOverGui();
			}
		}
	}
	
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes Renderer";
	}
}
