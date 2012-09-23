package logisticspipes.ticks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.renderer.LogisticsHUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ActiveRenderInfo;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class RenderTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		
	}
	
	private long renderTicks=0;
	
	private Method getSetupCameraTransformMethod() throws NoSuchMethodException {
		Minecraft mc = FMLClientHandler.instance().getClient();
		Class start = mc.entityRenderer.getClass();
		do {
			try {
				return start.getDeclaredMethod("a", new Class[]{float.class, int.class});
			} catch(Exception e) {
				try {
				return start.getDeclaredMethod("setupCameraTransform", new Class[]{float.class, int.class});
				} catch(Exception e1) {}
			}
		} while(!start.getSuperclass().equals(Object.class));
		throw new NoSuchMethodException("Can't find setupCameraTransform or a to display HUD");
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.RENDER)) {
			renderTicks++;
			if(LogisticsHUDRenderer.instance().displayRenderer()) {
				//Saveguard List
				List<UnlockThreadSecure> suspendedThread = new ArrayList<UnlockThreadSecure>();
				//Suspend Rei Mini Map
				for(Thread thread:Thread.getAllStackTraces().keySet()) {
					if(thread.getClass().getName().equals("reifnsk.minimap.ReiMinimap")) {
						//Start saveguard
						suspendedThread.add(new UnlockThreadSecure(1000, thread));
						thread.suspend();
					}
				}
				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				Minecraft mc = FMLClientHandler.instance().getClient();
				//Orientation
				try {
					Method camera = getSetupCameraTransformMethod();
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
				mc.entityRenderer.setupOverlayRendering();
				//Stop saveguard
				for(UnlockThreadSecure thread:suspendedThread) {
					thread.running = false;
				}
				//Restart Rei Mini Map
				for(Thread thread:Thread.getAllStackTraces().keySet()) {
					if(thread.getClass().getName().equals("reifnsk.minimap.ReiMinimap")) {
						thread.resume();
					}
				}
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
		return "LogisticsPipes Renderer";
	}
}
