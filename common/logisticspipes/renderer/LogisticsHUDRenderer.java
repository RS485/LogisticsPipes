package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.MathVector;
import logisticspipes.utils.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class LogisticsHUDRenderer {

	private LinkedList<IHeadUpDisplayRendererProvider> list = new LinkedList<IHeadUpDisplayRendererProvider>();
	private double lastXPos = 0;
	private double lastYPos = 0;
	private double lastZPos = 0;
	
	private ArrayList<IHeadUpDisplayBlockRendererProvider> providers = new ArrayList<IHeadUpDisplayBlockRendererProvider>();
	
	private static LogisticsHUDRenderer renderer = null;

	public void add(IHeadUpDisplayBlockRendererProvider provider) {
		IHeadUpDisplayBlockRendererProvider toRemove = null;
		for(IHeadUpDisplayBlockRendererProvider listedProvider:providers) {
			if(listedProvider.getX() == provider.getX() && listedProvider.getY() == provider.getY() && listedProvider.getZ() == provider.getZ()) {
				toRemove = listedProvider;
				break;
			}
		}
		if(toRemove != null) {
			providers.remove(toRemove);
		}
		providers.add(provider);
	}
	
	public void remove(IHeadUpDisplayBlockRendererProvider provider) {
		providers.remove(provider);
	}
	
	public void clear() {
		providers.clear();
		instance().clearList(false);
	}
	
	private void clearList(boolean flag) {
		if(flag) {
			for(IHeadUpDisplayRendererProvider renderer:list) {
				renderer.stopWaitching();
			}
		}
		list.clear();
	}
	
	private void refreshList(double x,double y,double z) {
		ArrayList<Pair<Double,IHeadUpDisplayRendererProvider>> newList = new ArrayList<Pair<Double,IHeadUpDisplayRendererProvider>>();
		for(IRouter router:SimpleServiceLocator.routerManager.getRouters()) {
			if(router == null)
				continue;
			CoreRoutedPipe pipe = router.getPipe();
			if(!(pipe instanceof IHeadUpDisplayRendererProvider)) continue;
			if(MainProxy.getDimensionForWorld(pipe.worldObj) == MainProxy.getDimensionForWorld(FMLClientHandler.instance().getClient().theWorld)) {
				double dis = Math.hypot(pipe.xCoord - x + 0.5,Math.hypot(pipe.yCoord - y + 0.5, pipe.zCoord - z + 0.5));
				if(dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75) {
					newList.add(new Pair<Double,IHeadUpDisplayRendererProvider>(dis,(IHeadUpDisplayRendererProvider)pipe));
					if(!list.contains(pipe)) {
						((IHeadUpDisplayRendererProvider)pipe).startWaitching();
					}
				}
			}
		}
		
		List<IHeadUpDisplayBlockRendererProvider> remove = new ArrayList<IHeadUpDisplayBlockRendererProvider>();
		for(IHeadUpDisplayBlockRendererProvider provider:providers) {
			if(MainProxy.getDimensionForWorld(provider.getWorld()) == MainProxy.getDimensionForWorld(FMLClientHandler.instance().getClient().theWorld)) {
				double dis = Math.hypot(provider.getX() - x + 0.5,Math.hypot(provider.getY() - y + 0.5, provider.getZ() - z + 0.5));
				if(dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75 && !provider.isInvalid() && provider.isExistend()) {
					newList.add(new Pair<Double,IHeadUpDisplayRendererProvider>(dis,provider));
					if(!list.contains(provider)) {
						provider.startWaitching();
					}
				} else if(provider.isInvalid() || !provider.isExistend()) {
					remove.add(provider);
				}
			}
		}
		for(IHeadUpDisplayBlockRendererProvider provider:remove) {
			providers.remove(provider);
		}
		
		if(newList.size() < 1) { 
			clearList(false);
			return;
		}
		for(IHeadUpDisplayRendererProvider part:list) {
		Collections.sort(newList,
				new Comparator<Pair<Double, IHeadUpDisplayRendererProvider>>() {
					@Override
					public int compare(
							Pair<Double, IHeadUpDisplayRendererProvider> o1,
							Pair<Double, IHeadUpDisplayRendererProvider> o2) {
						if (o1.getValue1() < o2.getValue1()) {
							return -1;
						} else if (o1.getValue1() > o2.getValue1()) {
							return 1;
						} else {
							return 0;
						}
					}
				});
			boolean contains = false;
			for(Pair<Double,IHeadUpDisplayRendererProvider> inpart:newList) {
				if(inpart.getValue2().equals(part)) {
					contains = true;
					break;
				}
			}
			if(!contains) {
				part.stopWaitching();
			}
		}
		clearList(false);
		for (Pair<Double, IHeadUpDisplayRendererProvider> part : newList) {
			list.addLast(part.getValue2());
		}
	}
	
	private boolean playerWearsHUD() {
		return FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.inventory != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3] != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3].itemID == LogisticsPipes.LogisticsHUDArmor.itemID;
	}
	
	public void renderPlayerDisplay(long renderTicks) {}
	
	public void renderWorldRelative(long renderTicks, float partialTick) {
		if(!displayRenderer()) return;
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;
		if(list.size() == 0 || Math.hypot(lastXPos - player.posX,Math.hypot(lastYPos - player.posY, lastZPos - player.posZ)) > 0.5 || (renderTicks % 10 == 0 && (lastXPos != player.posX || lastYPos != player.posY + player.getEyeHeight() || lastZPos != player.posZ)) || renderTicks % 600 == 0) {
			refreshList(player.posX,player.posY,player.posZ);
			lastXPos = player.posX;
			lastYPos = player.posY + player.getEyeHeight();
			lastZPos = player.posZ;
		}
		boolean cursorHandled = false;
		HUDConfig config = new HUDConfig(FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3]);
		for(IHeadUpDisplayRendererProvider renderer:list) {
			if(renderer.getRenderer() == null) continue;
			if(renderer.getRenderer().display(config)) {
				GL11.glPushMatrix();
				if(!cursorHandled) {
					double x = renderer.getX() + 0.5 - player.posX;
					double y = renderer.getY() + 0.5 - player.posY;
					double z = renderer.getZ() + 0.5 - player.posZ;
					if(Math.hypot(x,Math.hypot(y, z)) < 0.75 || (renderer instanceof IHeadUpDisplayBlockRendererProvider && (((IHeadUpDisplayBlockRendererProvider)renderer).isInvalid() || !((IHeadUpDisplayBlockRendererProvider)renderer).isExistend()))) {
						refreshList(player.posX,player.posY,player.posZ);
				        GL11.glPopMatrix();
						break;
					}
					cursorHandled = handleCursor(renderer);
				}
				//GL11.glPopMatrix();
				//GL11.glPushMatrix();
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				displayOneView(renderer, config, partialTick);
		        GL11.glPopMatrix();
			}
		}
	}

	
	private void displayOneView(IHeadUpDisplayRendererProvider renderer, HUDConfig config, float partialTick) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;
		double x = renderer.getX() + 0.5 - player.prevPosX - ((player.posX - player.prevPosX) * partialTick);
		double y = renderer.getY() + 0.5 - player.prevPosY - ((player.posY - player.prevPosY) * partialTick);
		double z = renderer.getZ() + 0.5 - player.prevPosZ - ((player.posZ - player.prevPosZ) * partialTick);
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(getAngle(z,x) + 90, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef((-1)*getAngle(Math.hypot(x,z),y) + 180, 1.0F, 0.0F, 0.0F);

		GL11.glTranslatef(0.0F, 0.0F, -0.4F);
		
		GL11.glScalef(0.01F, 0.01F, 1F);
		
		float light = mc.theWorld.getBlockLightValue(renderer.getX(), renderer.getY(), renderer.getZ());
		boolean dark = light < 11;
		renderer.getRenderer().renderHeadUpDisplay(Math.hypot(x,Math.hypot(y, z)),dark, mc, config);
	}
	
	private float getAngle(double x, double y) {
		return (float) (Math.atan2(x,y) * 360 / (2 * Math.PI));
	}
	
	public double up(double input) {
		input %= 360.0D;
		while(input < 0 && !Double.isNaN(input) && !Double.isInfinite(input)) {
			input += 360;
		}
		return input;
	}
	
	private boolean handleCursor(IHeadUpDisplayRendererProvider renderer) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;
		
		MathVector playerView = MathVector.getFromAngles((270 - player.rotationYaw) / 360 * -2 * Math.PI, (player.rotationPitch) / 360 * -2 * Math.PI);
		MathVector playerPos = new MathVector();
		playerPos.X = player.posX;
		playerPos.Y = player.posY;
		playerPos.Z = player.posZ;

		MathVector panelPos = new MathVector();
		panelPos.X = renderer.getX() + 0.5;
		panelPos.Y = renderer.getY() + 0.5;
		panelPos.Z = renderer.getZ() + 0.5;
		
		MathVector panelView = new MathVector();
		panelView.X = playerPos.X - panelPos.X;
		panelView.Y = playerPos.Y - panelPos.Y;
		panelView.Z = playerPos.Z - panelPos.Z;

		panelPos.add(panelView, 0.44D);

		double d = panelPos.X * panelView.X + panelPos.Y * panelView.Y + panelPos.Z * panelView.Z;
		double c = panelView.X * playerPos.X + panelView.Y * playerPos.Y + panelView.Z * playerPos.Z;
		double b = panelView.X * playerView.X + panelView.Y * playerView.Y + panelView.Z * playerView.Z;
		double a = (d - c) / b;
		
		MathVector viewPos = new MathVector();
		viewPos.X = playerPos.X + a * playerView.X - panelPos.X;
		viewPos.Y = playerPos.Y + a * playerView.Y - panelPos.Y;
		viewPos.Z = playerPos.Z + a * playerView.Z - panelPos.Z;
		
		MathVector panelScalVector1 = new MathVector();
		
		if(panelView.Y == 0) {
			panelScalVector1.X = 0;
			panelScalVector1.Y = 1;
			panelScalVector1.Z = 0;
		} else {
			panelScalVector1 = panelView.getOrtogonal(-panelView.X, null, -panelView.Z).makeVectorLength(1.0D);
		}
		
		MathVector panelScalVector2 = new MathVector();
		
		if(panelView.Z == 0) {
			panelScalVector2.X = 0;
			panelScalVector2.Y = 0;
			panelScalVector2.Z = 1;
		} else {
			panelScalVector2 = panelView.getOrtogonal(1.0D, 0.0D, null).makeVectorLength(1.0D);
		}
		
		if(panelScalVector1.Y == 0) {
			return false;
		}
		
		double cursorY = -viewPos.Y / panelScalVector1.Y;
		
		MathVector restViewPos = viewPos.clone();
		restViewPos.X += cursorY*panelScalVector1.X;
		restViewPos.Y = 0;
		restViewPos.Z += cursorY*panelScalVector1.Z;
		
		double cursorX;
		
		if(panelScalVector2.X == 0) {
			cursorX = restViewPos.Z / panelScalVector2.Z;
		} else {
			cursorX = restViewPos.X / panelScalVector2.X;
		}
		
		cursorX *= 50 / 0.47D;
		cursorY *= 50 / 0.47D;
		if(panelView.Z < 0) {
			cursorX *= -1;
		}
		if(panelView.Y < 0) {
			cursorY *= -1;
		}

		if(renderer.getRenderer().cursorOnWindow((int) cursorX, (int)cursorY)) {
			renderer.getRenderer().handleCursor((int) cursorX, (int)cursorY);
			return true;
		}
		return false;
	}
	
	public boolean displayRenderer() {
		if(!displayHUD()) {
			if(list.size() != 0) {
				clearList(true);
			}
		}
		return displayHUD();
	}
	
	private boolean displayHUD() {
		return playerWearsHUD() && FMLClientHandler.instance().getClient().currentScreen == null && FMLClientHandler.instance().getClient().gameSettings.thirdPersonView == 0 && !FMLClientHandler.instance().getClient().gameSettings.hideGUI;
	}
	
	public static LogisticsHUDRenderer instance() {
		if(renderer == null) {
			renderer = new LogisticsHUDRenderer();
		}
		return renderer;
	}
}
