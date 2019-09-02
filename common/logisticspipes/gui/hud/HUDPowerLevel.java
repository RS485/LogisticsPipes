package logisticspipes.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IPowerLevelDisplay;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.string.StringUtils;

public class HUDPowerLevel extends BasicHUDGui implements IHeadUpDisplayRenderer {

	private final IPowerLevelDisplay junction;
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/power_junction.png");

	public HUDPowerLevel(IPowerLevelDisplay junction) {
		this.junction = junction;
	}

	@Override
	public void renderHeadUpDisplay(double distance, boolean day, boolean shifted, Minecraft mc, IHUDConfig config) {
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 32);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 32);
		}
		GuiGraphics.drawGuiBackGround(mc, -60, -40, 60, 40, 0, false);
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 127);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);
		}
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		super.renderHeadUpDisplay(distance, day, shifted, mc, config);
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		mc.renderEngine.bindTexture(HUDPowerLevel.TEXTURE);
		drawTexturedModalRect(-50, -30, 9, 10, 7, 61);
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(-49, -29 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString("Stored Energy:", -30, -15, 0x404040);
		mc.fontRenderer.drawString(StringUtils.getStringWithSpacesFromInteger(junction.getDisplayPowerLevel()) + " " + junction.getBrand(), -30, -5, 0x404040);
		mc.fontRenderer.drawString("/ " + StringUtils.getStringWithSpacesFromInteger(junction.getMaxStorage()) + " " + junction.getBrand(), -30, 5, 0x404040);
		GL11.glTranslatef(0.0F, 0.0F, 0.0015F);
	}

	public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.getInstance();
		BufferBuilder buffer = var9.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(par1 + 0, par2 + par6, 0).tex((par3 + 0) * var7, (par4 + par6) * var8).endVertex();
		buffer.pos(par1 + par5, par2 + par6, 0).tex((par3 + par5) * var7, (par4 + par6) * var8).endVertex();
		buffer.pos(par1 + par5, par2 + 0, 0).tex((par3 + par5) * var7, (par4 + 0) * var8).endVertex();
		buffer.pos(par1 + 0, par2 + 0, 0).tex((par3 + 0) * var7, (par4 + 0) * var8).endVertex();
		var9.draw();
	}

	@Override
	public boolean display(IHUDConfig config) {
		return !junction.isHUDInvalid() && config.isHUDPowerLevel();
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return -60 < x && x < 60 && -40 < y && y < 40;
	}

}
