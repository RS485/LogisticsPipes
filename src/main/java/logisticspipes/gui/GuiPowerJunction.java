package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerJunctionCheatPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiPowerJunction extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.powerjunction.";

	private final LogisticsPowerJunctionTileEntity junction;

	public GuiPowerJunction(EntityPlayer player, LogisticsPowerJunctionTileEntity junction) {
		super(176, 166, 0, 0);
		DummyContainer dummy = new DummyContainer(player, null, junction);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		inventorySlots = dummy;
		this.junction = junction;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/power_junction.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiPowerJunction.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(j + 10, k + 11 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString(TextUtil.translate(GuiPowerJunction.PREFIX + "LogisticsPowerJunction"), guiLeft + 30, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiPowerJunction.PREFIX + "StoredEnergy") + ":", guiLeft + 40, guiTop + 23, 0x404040);
		mc.fontRenderer.drawString(TextUtil.formatNumberWithCommas(junction.getPowerLevel()) + " LP", guiLeft + 40, guiTop + 33, 0x404040);
		mc.fontRenderer.drawString("/ " + TextUtil.formatNumberWithCommas(LogisticsPowerJunctionTileEntity.MAX_STORAGE) + " LP", guiLeft + 40, guiTop + 43, 0x404040);
		mc.fontRenderer.drawString("1 MJ = 5 LP", guiLeft + 30, guiTop + 58, 0x404040);
		mc.fontRenderer.drawString("1 EU = 2 LP", guiLeft + 100, guiTop + 58, 0x404040);
		mc.fontRenderer.drawString("10 RF = 5 LP", guiLeft + 24, guiTop + 68, 0x404040);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		if (LogisticsPipes.isDEBUG()) {
			buttonList.add(new GuiButton(0, guiLeft + 140, guiTop + 20, 20, 20, "+"));
		}
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) throws IOException {
		if (par1GuiButton.id == 0) {
			junction.addEnergy(100000);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(PowerJunctionCheatPacket.class).setPosX(junction.getX()).setPosY(junction.getY()).setPosZ(junction.getZ()));
		} else {
			super.actionPerformed(par1GuiButton);
		}
	}

}
