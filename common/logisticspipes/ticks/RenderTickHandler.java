package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.GuiReopenPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.utils.ObfuscationHelper;
import logisticspipes.utils.ObfuscationHelper.NAMES;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

	@AllArgsConstructor
	private static class GuiEntry {
		@Getter
		private final int xCoord;
		@Getter
		private final int yCoord;
		@Getter
		private final int zCoord;
		@Getter
		private final int guiID;
		@Getter @Setter
		private boolean isActive;
	}
	
	private long renderTicks=0;
	private static Queue<GuiEntry> guiPos = new LinkedList<GuiEntry>();
	private int emptyCounter = 0;
	private int fullCounter = 0;
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
			if(LogisticsHUDRenderer.instance().displayRenderer()) {
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
		}
	}
	
	private Method getSetupCameraTransformMethod() throws NoSuchMethodException {
		Minecraft mc = FMLClientHandler.instance().getClient();
		Class<?> start = mc.entityRenderer.getClass();
		while(!start.equals(Object.class)) {
			try {
				return ObfuscationHelper.getDeclaredMethod(NAMES.setupCameraTransform, start, float.class, int.class);
			} catch(Exception e) {}
			start = start.getSuperclass();
		}
		throw new NoSuchMethodException("Can't find setupCameraTransform or a to display HUD");
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.RENDER)) {
			renderTicks++;
			if(LogisticsHUDRenderer.instance().displayRenderer()) {
				GL11.glPushMatrix();
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
				LogisticsHUDRenderer.instance().renderWorldRelative(renderTicks, (Float) tickData[0]);
				mc.entityRenderer.setupOverlayRendering();
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				LogisticsHUDRenderer.instance().renderPlayerDisplay(renderTicks);
				GL11.glPopMatrix();
			}
			//Handle GuiRepoen
			if(!guiPos.isEmpty()) {
				if(FMLClientHandler.instance().getClient().currentScreen == null) {
					fullCounter = 0;
					emptyCounter++;
					if(emptyCounter > 5) {
						GuiEntry part = guiPos.peek();
						if(part.isActive()) {
							part = guiPos.poll();
							MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiReopenPacket.class).setGuiID(part.getGuiID()).setPosX(part.getXCoord()).setPosY(part.getYCoord()).setPosZ(part.getZCoord()));
						}
						emptyCounter = 0;
					}
				} else {
					emptyCounter = 0;
					fullCounter++;
					if(fullCounter > 5) {
						GuiEntry part = guiPos.peek();
						part.setActive(true);
						fullCounter = 0;
					}
				}
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
	
	public static void addGuiToReopen(int xCoord, int yCoord, int zCoord, int guiID) {
		guiPos.add(new GuiEntry(xCoord, yCoord, zCoord, guiID, false));
	}
}
