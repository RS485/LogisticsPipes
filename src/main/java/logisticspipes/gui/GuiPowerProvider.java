package logisticspipes.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiPowerProvider extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.powerprovider.";

	private final LogisticsPowerProviderTileEntity junction;

	public GuiPowerProvider(EntityPlayer player, LogisticsPowerProviderTileEntity junction) {
		super(176, 166, 0, 0);
		DummyContainer dummy = new DummyContainer(player, null, junction);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		inventorySlots = dummy;
		this.junction = junction;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/power_junction.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiPowerProvider.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(j + 10, k + 11 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString(TextUtil.translate(GuiPowerProvider.PREFIX + "Logistics" + junction.getBrand() + "PowerProvider"), guiLeft + 25, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiPowerProvider.PREFIX + "StoredEnergy") + ":", guiLeft + 40, guiTop + 25, 0x404040);
		mc.fontRenderer.drawString(StringUtils.getStringWithSpacesFromInteger(junction.getDisplayPowerLevel()) + " " + junction.getBrand(), guiLeft + 40, guiTop + 35, 0x404040);
		mc.fontRenderer.drawString("/ " + StringUtils.getStringWithSpacesFromInteger(junction.getMaxStorage()) + " " + junction.getBrand(), guiLeft + 40, guiTop + 45, 0x404040);
	}
}
