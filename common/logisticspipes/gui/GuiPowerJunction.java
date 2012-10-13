package logisticspipes.gui;

import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.src.EntityPlayer;

import org.lwjgl.opengl.GL11;

public class GuiPowerJunction extends KraphtBaseGuiScreen {

	private final LogisticsPowerJuntionTileEntity_BuildCraft junction;
	
	public GuiPowerJunction(EntityPlayer player, LogisticsPowerJuntionTileEntity_BuildCraft junction) {
		super(176, 166, 0, 0);
		this.inventorySlots = junction.createContainer(player);
		this.junction = junction;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Power_Junction_ID;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/power_junction.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(j + 10, k + 11 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString("Logistics Power Junction", guiLeft + 30, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString("Stored Energy:", guiLeft + 40, guiTop + 25, 0x404040);
		mc.fontRenderer.drawString(""+junction.getPowerLevel() + " LP", guiLeft + 40, guiTop + 35, 0x404040);
		mc.fontRenderer.drawString("/ "+junction.MAX_STORAGE + " LP", guiLeft + 40, guiTop + 45, 0x404040);
		mc.fontRenderer.drawString("1 MJ = 5 LP", guiLeft + 30, guiTop + 65, 0x404040);
		mc.fontRenderer.drawString("1 EU = 2 LP", guiLeft + 100, guiTop + 65, 0x404040);
	}
}
