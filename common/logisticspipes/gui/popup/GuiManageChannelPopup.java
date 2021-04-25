package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;

import logisticspipes.interfaces.IGUIChannelInformationReceiver;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.DeleteChannelPacket;
import logisticspipes.network.packets.gui.OpenAddChannelGUIPacket;
import logisticspipes.network.packets.gui.OpenEditChannelGUIPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.gui.TextListDisplay;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiManageChannelPopup extends SubGuiScreen implements IGUIChannelInformationReceiver {

	private static String GUI_LANG_KEY = "gui.popup.managechannel.";

	protected final List<ChannelInformation> channelList;
	protected final TextListDisplay textList;
	private BlockPos position;

	public GuiManageChannelPopup(List<ChannelInformation> channelList, BlockPos pos) {
		super(150, 170, 0, 0);
		this.channelList = channelList;
		this.position = pos;
		this.textList = new TextListDisplay(this, 6, 16, 6, 30, 12, new TextListDisplay.List() {

			@Override
			public int getSize() {
				return channelList.size();
			}

			@Override
			public String getTextAt(int index) {
				return channelList.get(index).getName();
			}

			@Override
			public int getTextColor(int index) {
				return 0xFFFFFF;
			}
		});
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(10, xCenter + 16, bottom - 27, 50, 10, "Delete"));
		buttonList.add(new SmallGuiButton(1, xCenter + 16, bottom - 15, 50, 10, "Exit"));
		buttonList.add(new SmallGuiButton(2, xCenter - 66, bottom - 27, 50, 10, "Add"));
		buttonList.add(new SmallGuiButton(3, xCenter - 66, bottom - 15, 50, 10, "Edit"));
		buttonList.add(new SmallGuiButton(4, xCenter - 12, bottom - 27, 25, 10, "/\\"));
		buttonList.add(new SmallGuiButton(5, xCenter - 12, bottom - 15, 25, 10, "\\/"));
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		drawTitle();

		textList.renderGuiBackground(mouseX, mouseY);
	}

	protected void drawTitle() {
		mc.fontRenderer.drawStringWithShadow(
				TextUtil.translate(GUI_LANG_KEY + "title"), xCenter - (mc.fontRenderer.getStringWidth(TextUtil.translate(GUI_LANG_KEY + "title")) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		textList.mouseClicked(i, j, k);
		super.mouseClicked(i, j, k);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
		}
		if (wheel < 0) {
			textList.scrollUp();
		} else if (wheel > 0) {
			textList.scrollDown();
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 1) { // Exit
			exitGui();
		} else if (guibutton.id == 2) { // Add
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OpenAddChannelGUIPacket.class).setBlockPos(position));
		} else if (guibutton.id == 3) { // Edit
			int selected = textList.getSelected();
			if (selected >= 0) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(OpenEditChannelGUIPacket.class).setIdentifier(channelList.get(selected).getChannelIdentifier().toString()).setBlockPos(position));
			}
		} else if (guibutton.id == 4) {
			textList.scrollDown();
		} else if (guibutton.id == 5) {
			textList.scrollUp();
		} else if (guibutton.id == 10) {
			int selected = textList.getSelected();
			if (selected >= 0) {
				this.setSubGui(new ActionChoisePopup(TextUtil.translate(GUI_LANG_KEY + "deletedialog.title"), TextUtil.translate(GUI_LANG_KEY + "deletedialog.yes"), () ->
						MainProxy.sendPacketToServer(PacketHandler.getPacket(DeleteChannelPacket.class).setChannelIdentifier(channelList.get(selected).getChannelIdentifier())),
						TextUtil.translate(GUI_LANG_KEY + "deletedialog.no"), () -> {}));
			}
		} else {
			super.actionPerformed(guibutton);
		}
	}

	@Override
	public void handleChannelInformation(ChannelInformation channel, boolean flag) {
		if (!flag) {
			if (channel.getName() == null) {
				channelList.removeIf(chan -> chan.getChannelIdentifier().equals(channel.getChannelIdentifier()));
			} else {
				if (channelList.stream().anyMatch(chan -> chan.getChannelIdentifier().equals(channel.getChannelIdentifier()))) {
					channelList.stream().filter(chan -> chan.getChannelIdentifier().equals(channel.getChannelIdentifier())).forEach(chan -> {
						chan.setName(channel.getName());
						chan.setRights(channel.getRights());
						chan.setResponsibleSecurityID(channel.getResponsibleSecurityID());
					});
				} else {
					channelList.add(channel);
				}
			}
		}
	}
}
