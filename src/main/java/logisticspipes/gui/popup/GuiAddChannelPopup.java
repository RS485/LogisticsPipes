package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.input.Keyboard;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.AddNewChannelPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiAddChannelPopup extends SubGuiScreen {

	private static String GUI_LANG_KEY = "gui.popup.addchannel.";
	protected InputBar textInput = null;
	protected final UUID responsibleSecurityID;

	public GuiAddChannelPopup(UUID responsibleSecurityID) {
		super(118, 140, 0, 0);
		this.responsibleSecurityID = responsibleSecurityID;
	}

	protected GuiAddChannelPopup(UUID responsibleSecurityID, int ySize) {
		super(118, ySize, 0, 0);
		this.responsibleSecurityID = responsibleSecurityID;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();

		buttonList.clear();
		buttonList.add(new GuiCheckBox(0, guiLeft + 94, guiTop + 66, 16, 16, true));
		buttonList.add(new GuiCheckBox(1, guiLeft + 94, guiTop + 81, 16, 16, false));
		buttonList.add(new GuiCheckBox(2, guiLeft + 94, guiTop + 96, 16, 16, false));

		buttonList.add(new SmallGuiButton(4, guiLeft + 58, guiTop + 120, 50, 10, TextUtil.translate(GUI_LANG_KEY + "save")));

		if (this.textInput == null) {
			this.textInput = new InputBar(Minecraft.getMinecraft().fontRenderer, this.getBaseScreen(), guiLeft + 30, guiTop + 32, right - guiLeft - 20, 15);
		}
		this.textInput.reposition(guiLeft + 10, guiTop + 34, right - guiLeft - 20, 15);

		((GuiCheckBox) buttonList.get(1)).enabled = responsibleSecurityID != null;
	}

	@Override
	public void exitGui() {
		super.exitGui();
		Keyboard.enableRepeatEvents(false);
		getBaseScreen().initGui();
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		drawTitle();
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "name"), guiLeft + 10, guiTop + 20, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "access") + ":", guiLeft + 10, guiTop + 55, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "public"), guiLeft + 10, guiTop + 70, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "security"), guiLeft + 10, guiTop + 85, responsibleSecurityID != null ? 0x404040 : 0x808080);
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "private"), guiLeft + 10, guiTop + 100, 0x404040);
	}

	protected void drawTitle() {
		mc.fontRenderer.drawStringWithShadow(
				TextUtil.translate(GUI_LANG_KEY + "title"), xCenter - (mc.fontRenderer.getStringWidth(TextUtil.translate(GUI_LANG_KEY + "title")) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		textInput.drawTextBox();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (!this.textInput.handleKey(par1, par2)) {
			super.keyTyped(par1, par2);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!this.textInput.handleClick(mouseX, mouseY, mouseButton)) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
			case 0:
				((GuiCheckBox) buttonList.get(0)).setState(true);
				((GuiCheckBox) buttonList.get(1)).setState(false);
				((GuiCheckBox) buttonList.get(2)).setState(false);
				break;
			case 1:
				((GuiCheckBox) buttonList.get(0)).setState(false);
				((GuiCheckBox) buttonList.get(1)).setState(true);
				((GuiCheckBox) buttonList.get(2)).setState(false);
				break;
			case 2:
				((GuiCheckBox) buttonList.get(0)).setState(false);
				((GuiCheckBox) buttonList.get(1)).setState(false);
				((GuiCheckBox) buttonList.get(2)).setState(true);
				break;
			case 4:
				ChannelInformation.AccessRights rights = null;
				UUID security = null;
				if (((GuiCheckBox) buttonList.get(0)).getState()) {
					rights = ChannelInformation.AccessRights.PUBLIC;
				} else if (((GuiCheckBox) buttonList.get(1)).getState()) {
					rights = ChannelInformation.AccessRights.SECURED;
					security = responsibleSecurityID;
				} else if (((GuiCheckBox) buttonList.get(2)).getState()) {
					rights = ChannelInformation.AccessRights.PRIVATE;
				}
				MainProxy.sendPacketToServer(PacketHandler.getPacket(AddNewChannelPacket.class).setName(this.textInput.getText()).setRights(rights).setSecurityStationID(security));
				exitGui();
				break;
		}
	}
}
