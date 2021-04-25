package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;

import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.gui.SmallGuiButton;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiSelectChannelPopup extends GuiManageChannelPopup {

	private static final String GUI_LANG_KEY = "gui.popup.selectchannel.";

	private final Consumer<ChannelInformation> handleResult;

	public GuiSelectChannelPopup(List<ChannelInformation> channelList, BlockPos pos, Consumer<ChannelInformation> handleResult) {
		super(channelList, pos);
		this.handleResult = handleResult;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.remove(0);
		buttonList.add(new SmallGuiButton(0, xCenter + 16, bottom - 27, 50, 10, "Select"));
	}

	protected void drawTitle() {
		mc.fontRenderer.drawStringWithShadow(
				TextUtil.translate(GUI_LANG_KEY + "title"), xCenter - (mc.fontRenderer.getStringWidth(TextUtil.translate(GUI_LANG_KEY + "title")) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) { // Select
			int selected = textList.getSelected();
			if (selected >= 0) {
				ChannelInformation info = channelList.get(selected);
				if (info != null) {
					handleResult.accept(info);
				}
				exitGui();
			}
		} else {
			super.actionPerformed(guibutton);
		}
	}
}
