package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.api.IHUDArmor;
import logisticspipes.config.Configs;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.MathVector;
import logisticspipes.utils.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class LogisticsHUDRenderer {

	private LinkedList<IHeadUpDisplayRendererProvider> list = new LinkedList<IHeadUpDisplayRendererProvider>();
	private double lastXPos = 0;
	private double lastYPos = 0;
	private double lastZPos = 0;
	
	private ArrayList<IHeadUpDisplayBlockRendererProvider> providers = new ArrayList<IHeadUpDisplayBlockRendererProvider>();
	
	private List<LaserData> lasers = new ArrayList<LaserData>();
	
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
				renderer.stopWatching();
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
			if(MainProxy.getDimensionForWorld(pipe.getWorld()) == MainProxy.getDimensionForWorld(FMLClientHandler.instance().getClient().theWorld)) {
				double dis = Math.hypot(pipe.getX() - x + 0.5,Math.hypot(pipe.getY() - y + 0.5, pipe.getZ() - z + 0.5));
				if(dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75) {
					newList.add(new Pair<Double,IHeadUpDisplayRendererProvider>(dis,(IHeadUpDisplayRendererProvider)pipe));
					if(!list.contains(pipe)) {
						((IHeadUpDisplayRendererProvider)pipe).startWatching();
					}
				}
			}
		}
		
		List<IHeadUpDisplayBlockRendererProvider> remove = new ArrayList<IHeadUpDisplayBlockRendererProvider>();
		for(IHeadUpDisplayBlockRendererProvider provider:providers) {
			if(MainProxy.getDimensionForWorld(provider.getWorld()) == MainProxy.getDimensionForWorld(FMLClientHandler.instance().getClient().theWorld)) {
				double dis = Math.hypot(provider.getX() - x + 0.5,Math.hypot(provider.getY() - y + 0.5, provider.getZ() - z + 0.5));
				if(dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75 && !provider.isInvalid() && provider.isExistent()) {
					newList.add(new Pair<Double,IHeadUpDisplayRendererProvider>(dis,provider));
					if(!list.contains(provider)) {
						provider.startWatching();
					}
				} else if(provider.isInvalid() || !provider.isExistent()) {
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
		Collections.sort(newList, new Comparator<Pair<Double, IHeadUpDisplayRendererProvider>>() {
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
		for(IHeadUpDisplayRendererProvider part:list) {
			boolean contains = false;
			for(Pair<Double,IHeadUpDisplayRendererProvider> inpart:newList) {
				if(inpart.getValue2().equals(part)) {
					contains = true;
					break;
				}
			}
			if(!contains) {
				part.stopWatching();
			}
		}
		clearList(false);
		for (Pair<Double, IHeadUpDisplayRendererProvider> part : newList) {
			list.addLast(part.getValue2());
		}
	}
	
	private boolean playerWearsHUD() {
		return FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.inventory != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3] != null && FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3].getItem() instanceof IHUDArmor && ((IHUDArmor)FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3].getItem()).isEnabled(FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3]);
	}
	
	private boolean displayCross = false;
	
	//TODO: only load this once, rather than twice
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/icons.png");
	
	public void renderPlayerDisplay(long renderTicks) {
		if(!displayRenderer()) return;
		Minecraft mc = FMLClientHandler.instance().getClient();
		if(displayCross) {
			ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
	        int width = res.getScaledWidth();
	        int height = res.getScaledHeight();
	        if (GuiIngameForge.renderCrosshairs && mc.ingameGUI != null) {
		        mc.renderEngine.func_110577_a(TEXTURE);
		        GL11.glColor4d(0.0D, 0.0D, 0.0D, 1.0D);
		        GL11.glDisable(GL11.GL_BLEND);
		        mc.ingameGUI.drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
		    }
		}
	}
	
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
		displayCross = false;
		HUDConfig config = new HUDConfig(FMLClientHandler.instance().getClient().thePlayer.inventory.armorInventory[3]);
		IHeadUpDisplayRendererProvider thisIsLast = null;
		for(IHeadUpDisplayRendererProvider renderer:list) {
			if(renderer.getRenderer() == null) continue;
			if(renderer.getRenderer().display(config)) {
				GL11.glPushMatrix();
				if(!cursorHandled) {
					double x = renderer.getX() + 0.5 - player.posX;
					double y = renderer.getY() + 0.5 - player.posY;
					double z = renderer.getZ() + 0.5 - player.posZ;
					if(Math.hypot(x,Math.hypot(y, z)) < 0.75 || (renderer instanceof IHeadUpDisplayBlockRendererProvider && (((IHeadUpDisplayBlockRendererProvider)renderer).isInvalid() || !((IHeadUpDisplayBlockRendererProvider)renderer).isExistent()))) {
						refreshList(player.posX,player.posY,player.posZ);
				        GL11.glPopMatrix();
						break;
					}
					int[] pos = getCursor(renderer);
					if(pos.length == 2) {
						if(renderer.getRenderer().cursorOnWindow(pos[0], pos[1])) {
							renderer.getRenderer().handleCursor(pos[0], pos[1]);
							if(FMLClientHandler.instance().getClient().thePlayer.isSneaking()) {
								thisIsLast = renderer;
								displayCross = true;
							}
							cursorHandled = true;
						}
					}
				}
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				if(thisIsLast != renderer) {
					displayOneView(renderer, config, partialTick);
				}
				GL11.glPopMatrix();
			}
		}
		if(thisIsLast != null) {
			GL11.glPushMatrix();
	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        displayOneView(thisIsLast, config, partialTick);
	        GL11.glPopMatrix();
		}
		
		//Render Laser
        GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glEnable(GL11.GL_LIGHTING);
		for(LaserData data: lasers) {
			GL11.glPushMatrix();
			
			double x = data.getPosX() + 0.5 - player.prevPosX - ((player.posX - player.prevPosX) * partialTick);
			double y = data.getPosY() + 0.5 - player.prevPosY - ((player.posY - player.prevPosY) * partialTick);
			double z = data.getPosZ() + 0.5 - player.prevPosZ - ((player.posZ - player.prevPosZ) * partialTick);
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			switch(data.getDir()) {
				case NORTH:
					GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
					break;
				case SOUTH:
					GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
					break;
				case EAST:
					break;
				case WEST:
					GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
					break;
				case UP:
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
					break;
				case DOWN:
					GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
					break;
				default:
					break;
			}

			GL11.glScalef(0.01F, 0.01F, 0.01F);

			Tessellator tessellator = Tessellator.instance;

			for(float i = 0; i < 6 * data.getLength(); i++) {
				setColor(i, data.getConnectionType());
				
				float shift = 100f * i / 6f;
				float start = 0.0f;
				if(data.isStartPipe() && i == 0) {
					start = -6.0f;
				}

				tessellator.startDrawingQuads();
				tessellator.addVertex(19.7f + shift        , 3.0f, -3.0f);
				tessellator.addVertex( 3.0f + shift + start, 3.0f, -3.0f);
				tessellator.addVertex( 3.0f + shift + start, 3.0f,  3.0f);
				tessellator.addVertex(19.7f + shift        , 3.0f,  3.0f);
				tessellator.draw();

				tessellator.startDrawingQuads();
				tessellator.addVertex(19.7f + shift        , -3.0f,  3.0f);
				tessellator.addVertex( 3.0f + shift + start, -3.0f,  3.0f);
				tessellator.addVertex( 3.0f + shift + start, -3.0f, -3.0f);
				tessellator.addVertex(19.7f + shift        , -3.0f, -3.0f);
				tessellator.draw();

				tessellator.startDrawingQuads();
				tessellator.addVertex(19.7f + shift        ,  3.0f, 3.0f);
				tessellator.addVertex( 3.0f + shift + start,  3.0f, 3.0f);
				tessellator.addVertex( 3.0f + shift + start, -3.0f, 3.0f);
				tessellator.addVertex(19.7f + shift        , -3.0f, 3.0f);
				tessellator.draw();

				tessellator.startDrawingQuads();
				tessellator.addVertex(19.7f + shift        , -3.0f, -3.0f);
				tessellator.addVertex( 3.0f + shift + start, -3.0f, -3.0f);
				tessellator.addVertex( 3.0f + shift + start,  3.0f, -3.0f);
				tessellator.addVertex(19.7f + shift        ,  3.0f, -3.0f);
				tessellator.draw();
			}

			if(data.isStartPipe()) {
				setColor(0, data.getConnectionType());
				tessellator.startDrawingQuads();
				tessellator.addVertex(-3.0f,  3.0f,  3.0f);
				tessellator.addVertex(-3.0f,  3.0f, -3.0f);
				tessellator.addVertex(-3.0f, -3.0f, -3.0f);
				tessellator.addVertex(-3.0f, -3.0f,  3.0f);
				tessellator.draw();
			}

			if(data.isFinalPipe()) {
				setColor(6 * data.getLength() - 1, data.getConnectionType());
				tessellator.startDrawingQuads();
				tessellator.addVertex(100.0f * data.getLength() + 3f,  3.0f, -3.0f);
				tessellator.addVertex(100.0f * data.getLength() + 3f,  3.0f,  3.0f);
				tessellator.addVertex(100.0f * data.getLength() + 3f, -3.0f,  3.0f);
				tessellator.addVertex(100.0f * data.getLength() + 3f, -3.0f, -3.0f);
				tessellator.draw();
			}

			GL11.glPopMatrix();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	private void setColor(float i, EnumSet<PipeRoutingConnectionType> flags) {
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		if(!flags.isEmpty()) {
			int k=0;
			for(int j=0;j<PipeRoutingConnectionType.values.length;j++) {
				PipeRoutingConnectionType type = PipeRoutingConnectionType.values[j];
				if(flags.contains(type)) {
					k++;
				}
				if(k - 1 == (int) i % flags.size()) {
					setColor(type);
					break;
				}
			}
		}
	}
	
	private void setColor(PipeRoutingConnectionType type) {
		switch (type) {
			case canRouteTo:
				GL11.glColor4f(1.0f, 1.0f, 0.0f, 0.5f);
				break;
			case canRequestFrom:
				GL11.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
				break;
			case canPowerFrom:
				GL11.glColor4f(0.0f, 0.0f, 1.0f, 0.5f);
				break;
			default:
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
		
		renderer.getRenderer().renderHeadUpDisplay(Math.hypot(x,Math.hypot(y, z)), false, mc, config);
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
	
	private int[] getCursor(IHeadUpDisplayRendererProvider renderer) {
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
			return new int[]{};
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

		return new int[]{(int) cursorX, (int)cursorY};
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
	
	public void resetLasers() {
		lasers.clear();
	}
	
	public void setLasers(List<LaserData> newLasers) {
		lasers.clear();
		lasers.addAll(newLasers);
	}
	
	public boolean hasLasers() {
		return !lasers.isEmpty();
	}
	
	public static LogisticsHUDRenderer instance() {
		if(renderer == null) {
			renderer = new LogisticsHUDRenderer();
		}
		return renderer;
	}
}
