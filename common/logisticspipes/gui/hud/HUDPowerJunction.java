package logisticspipes.gui.hud;

import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class HUDPowerJunction extends BasicHUDGui implements IHeadUpDisplayRenderer {
	
	private final LogisticsPowerJuntionTileEntity_BuildCraft junction;
	
	public HUDPowerJunction(LogisticsPowerJuntionTileEntity_BuildCraft junction) {
		this.junction = junction;
	}
	
	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc, HUDConfig config) {
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)32);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)32);	
        }
		BasicGuiHelper.drawGuiBackGround(mc, -60, -40, 60, 40, 0, false);
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
        }
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		super.renderHeadUpDisplay(distance, day, mc, config);
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/power_junction.png");
		mc.renderEngine.bindTexture(i);
		drawTexturedModalRect(-50, -30, 9, 10, 7, 61);
		GL11.glTranslatef(0.0F, 0.0F, -0.0005F);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(-49, -29 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString("Stored Energy:", -30, -15, 0x404040);
		mc.fontRenderer.drawString(""+junction.getPowerLevel() + " LP", -30, -5, 0x404040);
		mc.fontRenderer.drawString("/ "+junction.MAX_STORAGE + " LP", -30, 5, 0x404040);
		GL11.glTranslatef(0.0F, 0.0F, 0.0015F);
	}

	public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), (double)0, (double)((float)(par3 + 0) * var7), (double)((float)(par4 + par6) * var8));
        var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)0, (double)((float)(par3 + par5) * var7), (double)((float)(par4 + par6) * var8));
        var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), (double)0, (double)((float)(par3 + par5) * var7), (double)((float)(par4 + 0) * var8));
        var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), (double)0, (double)((float)(par3 + 0) * var7), (double)((float)(par4 + 0) * var8));
        var9.draw();
    }

	@Override
	public boolean display(HUDConfig config) {
		return !junction.isInvalid() && config.isHUDPowerJunction();
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return -50 < x && x < 50 && -50 < y && y < 50;
	}

}
