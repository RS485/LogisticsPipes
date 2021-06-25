package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import logisticspipes.api.IHUDArmor;
import logisticspipes.config.Configs;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IDebugHUDProvider;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import logisticspipes.utils.math.Vector3d;
import logisticspipes.utils.tuples.Pair;

public class LogisticsHUDRenderer {

	public IDebugHUDProvider debugHUD = null;

	private LinkedList<IHeadUpDisplayRendererProvider> list = new LinkedList<>();
	private double lastXPos = 0;
	private double lastYPos = 0;
	private double lastZPos = 0;

	private int progress = 0;
	private long last = 0;

	private ArrayList<IHeadUpDisplayBlockRendererProvider> providers = new ArrayList<>();

	private List<LaserData> lasers = new ArrayList<>();

	private static LogisticsHUDRenderer renderer = null;

	public void add(IHeadUpDisplayBlockRendererProvider provider) {
		IHeadUpDisplayBlockRendererProvider toRemove = null;
		for (IHeadUpDisplayBlockRendererProvider listedProvider : providers) {
			if (listedProvider.getX() == provider.getX() && listedProvider.getY() == provider.getY() && listedProvider.getZ() == provider.getZ()) {
				toRemove = listedProvider;
				break;
			}
		}
		if (toRemove != null) {
			providers.remove(toRemove);
		}
		providers.add(provider);
	}

	public void remove(IHeadUpDisplayBlockRendererProvider provider) {
		providers.remove(provider);
	}

	public void clear() {
		providers.clear();
		LogisticsHUDRenderer.instance().clearList(false);
	}

	private void clearList(boolean flag) {
		if (flag) {
			list.forEach(IHeadUpDisplayRendererProvider::stopWatching);
		}
		list.clear();
	}

	private void refreshList(double x, double y, double z) {
		ArrayList<Pair<Double, IHeadUpDisplayRendererProvider>> newList = new ArrayList<>();
		for (IRouter router : SimpleServiceLocator.routerManager.getRouters()) {
			if (router == null) {
				continue;
			}
			CoreRoutedPipe pipe = router.getPipe();
			if (!(pipe instanceof IHeadUpDisplayRendererProvider)) {
				continue;
			}
			if (pipe.getWorld().provider.getDimension() == FMLClientHandler.instance().getClient().world.provider.getDimension()) {
				double dis = Math.hypot(pipe.getX() - x + 0.5, Math.hypot(pipe.getY() - y + 0.5, pipe.getZ() - z + 0.5));
				if (dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75) {
					newList.add(new Pair<>(dis, (IHeadUpDisplayRendererProvider) pipe));
					if (!list.contains(pipe)) {
						((IHeadUpDisplayRendererProvider) pipe).startWatching();
					}
				}
			}
		}

		List<IHeadUpDisplayBlockRendererProvider> remove = new ArrayList<>();
		providers.stream().filter(provider -> provider.getWorldForHUD().provider.getDimension() == FMLClientHandler.instance().getClient().world.provider.getDimension())
				.forEach(provider -> {
					double dis = Math.hypot(provider.getX() - x + 0.5, Math.hypot(provider.getY() - y + 0.5, provider.getZ() - z + 0.5));
					if (dis < Configs.LOGISTICS_HUD_RENDER_DISTANCE && dis > 0.75 && !provider.isHUDInvalid() && provider.isHUDExistent()) {
						newList.add(new Pair<>(dis, provider));
						if (!list.contains(provider)) {
							provider.startWatching();
						}
					} else if (provider.isHUDInvalid() || !provider.isHUDExistent()) {
						remove.add(provider);
					}
				});
		for (IHeadUpDisplayBlockRendererProvider provider : remove) {
			providers.remove(provider);
		}

		if (newList.size() < 1) {
			clearList(true);
			return;
		}
		newList.sort(Comparator.comparing(Pair::getValue1));
		for (IHeadUpDisplayRendererProvider part : list) {
			boolean contains = false;
			for (Pair<Double, IHeadUpDisplayRendererProvider> inpart : newList) {
				if (inpart.getValue2().equals(part)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				part.stopWatching();
			}
		}
		clearList(false);
		for (Pair<Double, IHeadUpDisplayRendererProvider> part : newList) {
			list.addLast(part.getValue2());
		}
	}

	private boolean playerWearsHUD() {
		EntityPlayerSP player = FMLClientHandler.instance().getClient().player;
		if (player == null) return false;

		InventoryPlayer inv = player.inventory;
        if (inv == null) return false;

        return inv.armorInventory != null && checkItemStackForHUD(inv.armorInventory.get(3));
	}

	private boolean checkItemStackForHUD(@Nonnull ItemStack stack) {
		if (stack.getItem() instanceof IHUDArmor) {
			return ((IHUDArmor) stack.getItem()).isEnabled(stack);
		}
		return false;
	}

	private boolean displayCross = false;

	//TODO: only load this once, rather than twice
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/icons.png");

	public void renderPlayerDisplay(long renderTicks) {
		if (!displayRenderer()) {
			return;
		}
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (displayHUD() && displayCross) {
			ScaledResolution res = new ScaledResolution(mc);
			int width = res.getScaledWidth();
			int height = res.getScaledHeight();
			if (GuiIngameForge.renderCrosshairs && mc.ingameGUI != null) {
				mc.renderEngine.bindTexture(LogisticsHUDRenderer.TEXTURE);
				GL11.glColor4d(0.0D, 0.0D, 0.0D, 1.0D);
				GL11.glDisable(GL11.GL_BLEND);
				mc.ingameGUI.drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void renderWorldRelative(long renderTicks, float partialTick) {
		if (!displayRenderer()) {
			return;
		}
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.player;
		if (list.size() == 0 || Math.hypot(lastXPos - player.posX, Math.hypot(lastYPos - player.posY, lastZPos - player.posZ)) > 0.5 || (renderTicks % 10 == 0 && (lastXPos != player.posX || lastYPos != player.posY || lastZPos != player.posZ)) || renderTicks % 600 == 0) {
			refreshList(player.posX, player.posY, player.posZ);
			lastXPos = player.posX;
			lastYPos = player.posY;
			lastZPos = player.posZ;
		}
		boolean cursorHandled = false;
		displayCross = false;
		IHUDConfig config;
		if (debugHUD == null) {
			config = new HUDConfig(mc.player.inventory.armorInventory.get(3));
		} else {
			config = new IHUDConfig() {

				@Override
				public boolean isHUDSatellite() {
					return false;
				}

				@Override
				public boolean isHUDProvider() {
					return false;
				}

				@Override
				public boolean isHUDPowerLevel() {
					return false;
				}

				@Override
				public boolean isHUDInvSysCon() {
					return false;
				}

				@Override
				public boolean isHUDCrafting() {
					return false;
				}

				@Override
				public boolean isChassisHUD() {
					return false;
				}

				@Override
				public void setChassisHUD(boolean state) {}

				@Override
				public void setHUDCrafting(boolean state) {}

				@Override
				public void setHUDInvSysCon(boolean state) {}

				@Override
				public void setHUDPowerJunction(boolean state) {}

				@Override
				public void setHUDProvider(boolean state) {}

				@Override
				public void setHUDSatellite(boolean state) {}
			};
		}
		IHeadUpDisplayRendererProvider thisIsLast = null;
		List<IHeadUpDisplayRendererProvider> toUse = list;
		if (debugHUD != null) {
			toUse = debugHUD.getHUDs();
		}

		for (IHeadUpDisplayRendererProvider renderer : toUse) {
			if (renderer.getRenderer() == null) {
				continue;
			}
			if (renderer.getRenderer().display(config)) {
				GL11.glPushMatrix();
				if (!cursorHandled) {
					double x = renderer.getX() + 0.5 - player.posX;
					double y = renderer.getY() + 0.5 - player.posY;
					double z = renderer.getZ() + 0.5 - player.posZ;
					if (Math.hypot(x, Math.hypot(y, z)) < 0.75 || (renderer instanceof IHeadUpDisplayBlockRendererProvider && (((IHeadUpDisplayBlockRendererProvider) renderer).isHUDInvalid() || !((IHeadUpDisplayBlockRendererProvider) renderer).isHUDExistent()))) {
						refreshList(player.posX, player.posY, player.posZ);
						GL11.glPopMatrix();
						break;
					}
					int[] pos = getCursor(renderer);
					if (pos.length == 2) {
						if (renderer.getRenderer().cursorOnWindow(pos[0], pos[1])) {
							renderer.getRenderer().handleCursor(pos[0], pos[1]);
							if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) { //if(FMLClientHandler.instance().getClient().player.isSneaking()) {
								thisIsLast = renderer;
								displayCross = true;
							}
							cursorHandled = true;
						}
					}
				}
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				if (thisIsLast != renderer) {
					displayOneView(renderer, config, partialTick, false);
				}
				GL11.glPopMatrix();
			}
		}
		if (thisIsLast != null) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			displayOneView(thisIsLast, config, partialTick, true);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}

		GL11.glPushMatrix();
		RayTraceResult box = mc.objectMouseOver;
		if (box != null && box.typeOfHit == RayTraceResult.Type.BLOCK) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				progress = Math.min(progress + (2 * Math.max(1, (int) Math.floor((System.currentTimeMillis() - last) / 50.0D))), 100);
			} else {
				progress = Math.max(progress - (2 * Math.max(1, (int) Math.floor((System.currentTimeMillis() - last) / 50.0D))), 0);
			}
			if (progress != 0) {
				List<String> textData = SimpleServiceLocator.neiProxy.getInfoForPosition(player.world, player, box);

				//TileEntity tile = new DoubleCoordinates(box.blockX, box.blockY, box.blockZ).getTileEntity(DimensionManager.getWorld(0));
				//Insert debug code here

				if (!textData.isEmpty()) {
					double xCoord = box.getBlockPos().getX() + 0.5D;
					double yCoord = box.getBlockPos().getY() + 0.5D;
					double zCoord = box.getBlockPos().getZ() + 0.5D;

					double x = xCoord - player.prevPosX - ((player.posX - player.prevPosX) * partialTick);
					double y = yCoord - player.prevPosY - ((player.posY - player.prevPosY) * partialTick);
					double z = zCoord - player.prevPosZ - ((player.posZ - player.prevPosZ) * partialTick);

					GL11.glDisable(GL11.GL_DEPTH_TEST);

					GL11.glTranslatef((float) x, (float) y, (float) z);
					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(getAngle(z, x) + 110F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef((-1) * getAngle(Math.hypot(x + 0.8, z + 0.8), y + 0.5) + 180, 1.0F, 0.0F, 0.0F);

					double dProgress = progress / 100D;

					GL11.glTranslated(0.4D * dProgress + 0.6D, -0.2D * dProgress - 0.6D, -0.0D);

					GL11.glScalef(0.01F, 0.01F, 1F);

					int heigth = Math.max(32, 10 * textData.size() + 15);
					int width = 0;
					for (String s : textData) {
						width = Math.max(width, mc.fontRenderer.getStringWidth(s) + 22);
					}
					width = Math.max(32, width + 15);

					GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 96);
					GuiGraphics.drawGuiBackGround(mc, (int) ((-0.5 * (width - 32)) * dProgress) - 16, (int) ((-0.5 * (heigth - 32)) * dProgress) - 16, (int) ((0.5 * (width - 32)) * dProgress) + 16, (int) ((0.5 * (heigth - 32)) * dProgress) + 16, 0, false);
					GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);

					if (progress == 100) {
						GL11.glTranslated((int) ((-0.5 * (width - 32)) * dProgress) - 16, (int) ((-0.5 * (heigth - 32)) * dProgress) - 16, -0.0001D);
						for (int i = 0; i < textData.size(); i++) {
							mc.fontRenderer.drawString(textData.get(i), 28, 8 + i * 10, 0x000000);
						}

						ItemStack stack = SimpleServiceLocator.neiProxy.getItemForPosition(player.world, player, box);

						if (!stack.isEmpty()) {
							float scaleX = 1.5F * 0.8F;
							float scaleY = 1.5F * 0.8F;
							float scaleZ = -0.0001F;

							GL11.glScalef(scaleX, scaleY, scaleZ);

							ItemStackRenderer itemStackRenderer = new ItemStackRenderer(5, 6, 0.0F, true, true);
							itemStackRenderer.setItemstack(stack).setDisplayAmount(DisplayAmount.NEVER);
							itemStackRenderer.setScaleX(scaleX).setScaleY(scaleY).setScaleZ(scaleZ);

							itemStackRenderer.renderInGui();
						}
					}

					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
		} else if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			progress = 0;
		}
		GL11.glPopMatrix();

		//Render Laser
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glEnable(GL11.GL_LIGHTING);
		for (LaserData data : lasers) {
			GL11.glPushMatrix();

			double x = data.getPosX() + 0.5 - player.prevPosX - ((player.posX - player.prevPosX) * partialTick);
			double y = data.getPosY() + 0.5 - player.prevPosY - ((player.posY - player.prevPosY) * partialTick);
			double z = data.getPosZ() + 0.5 - player.prevPosZ - ((player.posZ - player.prevPosZ) * partialTick);
			GL11.glTranslatef((float) x, (float) y, (float) z);

			switch (data.getDir()) {
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

			Tessellator tessellator = Tessellator.getInstance();

			for (float i = 0; i < 6 * data.getLength(); i++) {
				setColor(i, data.getConnectionType());

				float shift = 100f * i / 6f;
				float start = 0.0f;
				if (data.isStartPipe() && i == 0) {
					start = -6.0f;
				}

				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(19.7f + shift, 3.0f, -3.0f);
				buffer.pos(3.0f + shift + start, 3.0f, -3.0f);
				buffer.pos(3.0f + shift + start, 3.0f, 3.0f);
				buffer.pos(19.7f + shift, 3.0f, 3.0f);
				tessellator.draw();

				buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(19.7f + shift, -3.0f, 3.0f);
				buffer.pos(3.0f + shift + start, -3.0f, 3.0f);
				buffer.pos(3.0f + shift + start, -3.0f, -3.0f);
				buffer.pos(19.7f + shift, -3.0f, -3.0f);
				tessellator.draw();

				buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(19.7f + shift, 3.0f, 3.0f);
				buffer.pos(3.0f + shift + start, 3.0f, 3.0f);
				buffer.pos(3.0f + shift + start, -3.0f, 3.0f);
				buffer.pos(19.7f + shift, -3.0f, 3.0f);
				tessellator.draw();

				buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(19.7f + shift, -3.0f, -3.0f);
				buffer.pos(3.0f + shift + start, -3.0f, -3.0f);
				buffer.pos(3.0f + shift + start, 3.0f, -3.0f);
				buffer.pos(19.7f + shift, 3.0f, -3.0f);
				tessellator.draw();
			}

			if (data.isStartPipe()) {
				setColor(0, data.getConnectionType());
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(-3.0f, 3.0f, 3.0f);
				buffer.pos(-3.0f, 3.0f, -3.0f);
				buffer.pos(-3.0f, -3.0f, -3.0f);
				buffer.pos(-3.0f, -3.0f, 3.0f);
				tessellator.draw();
			}

			if (data.isFinalPipe()) {
				setColor(6 * data.getLength() - 1, data.getConnectionType());
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(100.0f * data.getLength() + 3f, 3.0f, -3.0f);
				buffer.pos(100.0f * data.getLength() + 3f, 3.0f, 3.0f);
				buffer.pos(100.0f * data.getLength() + 3f, -3.0f, 3.0f);
				buffer.pos(100.0f * data.getLength() + 3f, -3.0f, -3.0f);
				tessellator.draw();
			}

			GL11.glPopMatrix();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		last = System.currentTimeMillis();
	}

	private void setColor(float i, EnumSet<PipeRoutingConnectionType> flags) {
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		if (!flags.isEmpty()) {
			int k = 0;
			for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values) {
				if (flags.contains(type)) {
					k++;
				}
				if (k - 1 == (int) i % flags.size()) {
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

	private void displayOneView(IHeadUpDisplayRendererProvider renderer, IHUDConfig config, float partialTick, boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.player;
		double x = renderer.getX() + 0.5 - player.prevPosX - ((player.posX - player.prevPosX) * partialTick);
		double y = renderer.getY() + 0.5 - player.prevPosY - ((player.posY - player.prevPosY) * partialTick);
		double z = renderer.getZ() + 0.5 - player.prevPosZ - ((player.posZ - player.prevPosZ) * partialTick);
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(getAngle(z, x) + 90, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef((-1) * getAngle(Math.hypot(x, z), y - player.getEyeHeight()) + 180, 1.0F, 0.0F, 0.0F);

		GL11.glTranslatef(0.0F, 0.0F, -0.4F);

		GL11.glScalef(0.01F, 0.01F, 1F);

		renderer.getRenderer().renderHeadUpDisplay(Math.hypot(x, Math.hypot(y, z)), false, shifted, mc, config);
	}

	private float getAngle(double x, double y) {
		return (float) (Math.atan2(x, y) * 360 / (2 * Math.PI));
	}

	public double up(double input) {
		input %= 360.0D;
		while (input < 0 && !Double.isNaN(input) && !Double.isInfinite(input)) {
			input += 360;
		}
		return input;
	}

	private int[] getCursor(IHeadUpDisplayRendererProvider renderer) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.player;

		Vector3d playerView = Vector3d.getFromAngles((270 - player.rotationYaw) / 360 * -2 * Math.PI, (player.rotationPitch) / 360 * -2 * Math.PI);
		Vector3d playerPos = new Vector3d();
		playerPos.x = player.posX;
		playerPos.y = player.posY + player.getEyeHeight();
		playerPos.z = player.posZ;

		Vector3d panelPos = new Vector3d();
		panelPos.x = renderer.getX() + 0.5;
		panelPos.y = renderer.getY() + 0.5;
		panelPos.z = renderer.getZ() + 0.5;

		Vector3d panelView = new Vector3d();
		panelView.x = playerPos.x - panelPos.x;
		panelView.y = playerPos.y - panelPos.y;
		panelView.z = playerPos.z - panelPos.z;

		panelPos.add(panelView, 0.44D);

		double d = panelPos.x * panelView.x + panelPos.y * panelView.y + panelPos.z * panelView.z;
		double c = panelView.x * playerPos.x + panelView.y * playerPos.y + panelView.z * playerPos.z;
		double b = panelView.x * playerView.x + panelView.y * playerView.y + panelView.z * playerView.z;
		double a = (d - c) / b;

		Vector3d viewPos = new Vector3d();
		viewPos.x = playerPos.x + a * playerView.x - panelPos.x;
		viewPos.y = playerPos.y + a * playerView.y - panelPos.y;
		viewPos.z = playerPos.z + a * playerView.z - panelPos.z;

		Vector3d panelScalVector1 = new Vector3d();

		if (panelView.y == 0) {
			panelScalVector1.x = 0;
			panelScalVector1.y = 1;
			panelScalVector1.z = 0;
		} else {
			panelScalVector1 = panelView.getOrtogonal(-panelView.x, null, -panelView.z).makeVectorLength(1.0D);
		}

		Vector3d panelScalVector2 = new Vector3d();

		if (panelView.z == 0) {
			panelScalVector2.x = 0;
			panelScalVector2.y = 0;
			panelScalVector2.z = 1;
		} else {
			panelScalVector2 = panelView.getOrtogonal(1.0D, 0.0D, null).makeVectorLength(1.0D);
		}

		if (panelScalVector1.y == 0) {
			return new int[] {};
		}

		double cursorY = -viewPos.y / panelScalVector1.y;

		Vector3d restViewPos = viewPos.clone();
		restViewPos.x += cursorY * panelScalVector1.x;
		restViewPos.y = 0;
		restViewPos.z += cursorY * panelScalVector1.z;

		double cursorX;

		if (panelScalVector2.x == 0) {
			cursorX = restViewPos.z / panelScalVector2.z;
		} else {
			cursorX = restViewPos.x / panelScalVector2.x;
		}

		cursorX *= 50 / 0.47D;
		cursorY *= 50 / 0.47D;
		if (panelView.z < 0) {
			cursorX *= -1;
		}
		if (panelView.y < 0) {
			cursorY *= -1;
		}

		return new int[] { (int) cursorX, (int) cursorY };
	}

	public boolean displayRenderer() {
		if (!displayHUD()) {
			if (list.size() != 0) {
				clearList(true);
			}
		}
		return displayHUD();
	}

	private boolean displayHUD() {
		return (playerWearsHUD() || debugHUD != null) && FMLClientHandler.instance().getClient().currentScreen == null && FMLClientHandler.instance().getClient().gameSettings.thirdPersonView == 0 && !FMLClientHandler.instance().getClient().gameSettings.hideGUI;
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
		if (LogisticsHUDRenderer.renderer == null) {
			LogisticsHUDRenderer.renderer = new LogisticsHUDRenderer();
		}
		return LogisticsHUDRenderer.renderer;
	}
}
