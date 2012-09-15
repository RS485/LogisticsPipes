package logisticspipes.ticks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import logisticspipes.renderer.LogisticsHUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ActiveRenderInfo;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		
	}
	
	private long renderTicks=0;
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		renderTicks++;
		if(type.contains(TickType.RENDER)) {
			if(LogisticsHUDRenderer.instance().displayRenderer()) {
				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				Minecraft mc = FMLClientHandler.instance().getClient();
				//Orientation
				try {
					Method camera = mc.entityRenderer.getClass().getDeclaredMethod("setupCameraTransform", new Class[]{float.class, int.class});
					camera.setAccessible(true);
					camera.invoke(mc.entityRenderer, new Object[]{tickData[0],1});
					ActiveRenderInfo.updateRenderInfo(mc.thePlayer, mc.gameSettings.thirdPersonView == 2);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				LogisticsHUDRenderer.instance().renderWorldRelative(renderTicks);
		        GL11.glPopMatrix();
			}
		}
	}
	
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes";
	}
}
