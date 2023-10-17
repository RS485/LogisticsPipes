package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.client.gui.GuiButton;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.EditChannelPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.SmallGuiButton;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiEditChannelPopup extends GuiAddChannelPopup {

	private static String GUI_LANG_KEY = "gui.popup.editchannel.";
	private final UUID channelIdentifier;
	private ChannelInformation toInit;

	public GuiEditChannelPopup(UUID correspondingSecurityStationID, ChannelInformation toEdit) {
		super(correspondingSecurityStationID, 160);
		this.channelIdentifier = toEdit.getChannelIdentifier();
		toInit = toEdit;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.remove(buttonList.size() - 1);
		buttonList.add(new SmallGuiButton(5, guiLeft + 58, guiTop + 140, 50, 10, TextUtil.translate(GUI_LANG_KEY + "save")));
		if (toInit != null) {
			this.textInput.setText(toInit.getName());
			((GuiCheckBox) buttonList.get(0)).setState(toInit.getRights() == ChannelInformation.AccessRights.PUBLIC);
			((GuiCheckBox) buttonList.get(1)).setState(toInit.getRights() == ChannelInformation.AccessRights.SECURED);
			((GuiCheckBox) buttonList.get(2)).setState(toInit.getRights() == ChannelInformation.AccessRights.PRIVATE);
		}
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		super.renderGuiBackground(mouseX, mouseY);
		mc.fontRenderer.drawString(TextUtil.translate(GUI_LANG_KEY + "owner") + ": ", guiLeft + 10, guiTop + 115, 0x404040);
		mc.fontRenderer.drawString(toInit.getOwner().getUsername(), guiLeft + 10, guiTop + 127, 0x404040);
	}

	@Override
	protected void drawTitle() {
		mc.fontRenderer.drawStringWithShadow(TextUtil.translate(GUI_LANG_KEY + "title"), xCenter - (mc.fontRenderer.getStringWidth(TextUtil.translate(GUI_LANG_KEY + "title")) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
			case 5:
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
				MainProxy.sendPacketToServer(
						PacketHandler.getPacket(EditChannelPacket.class).setChannelIdentifier(channelIdentifier).setName(this.textInput.getText()).setRights(rights).setSecurityStationID(security));
				exitGui();
				break;
		}
	}
}
