package logisticspipes.gui.popup;

import java.awt.Rectangle;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.upgrade.ToogleDisconnectionUpgradeSidePacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.gui.UpgradeSlot;
import logisticspipes.utils.gui.sideconfig.SideConfigDisplay;
import network.rs485.logisticspipes.util.TextUtil;

public class DisconnectionConfigurationPopup extends SubGuiScreen {

	private static final String PREFIX = "gui.pipecontroller.popup.";

	private SideConfigDisplay configDisplay;
	private CoreRoutedPipe pipe;
	private Rectangle bounds;
	private UpgradeSlot pos;

	public DisconnectionConfigurationPopup(CoreRoutedPipe pipe, UpgradeSlot pos) {
		super(250, 250, 0, 0);
		this.pipe = pipe;
		this.pos = pos;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		configDisplay = new SideConfigDisplay(pipe) {

			@Override
			public void handleSelection(SelectedFace selection) {
				DisconnectionConfigurationPopup.this.handleSelection(selection);
			}
		};
		configDisplay.init();
		configDisplay.renderNeighbours = true;

		buttonList.add(new GuiButton(0, right - 106, bottom - 26, 100, 20, "OK"));

		bounds = new Rectangle(guiLeft + 5, guiTop + 20, this.xSize - 10, this.ySize - 50);
	}

	public void handleSelection(SideConfigDisplay.SelectedFace selection) {
		//ItemStack stack = pipe.getOriginalUpgradeManager().getInv().getStackInSlot(pos);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(ToogleDisconnectionUpgradeSidePacket.class).setSide(selection.face).setSlot(pos));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float partialTick) {
		drawRect(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, 0xff000000);

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaledresolution = new ScaledResolution(mc);

		int vpx = bounds.x * scaledresolution.getScaleFactor();
		int vpy = (bounds.y + 10) * scaledresolution.getScaleFactor();
		int w = bounds.width * scaledresolution.getScaleFactor();
		int h = (bounds.height - 1) * scaledresolution.getScaleFactor();

		mc.fontRenderer.drawString(TextUtil
				.translate(PREFIX + "disconnectTitle"), guiLeft + 8, guiTop + 8, logisticspipes.utils.Color
				.getValue(logisticspipes.utils.Color.DARKER_GREY), false);

		configDisplay.drawScreen(mouseX, mouseY, partialTick, new Rectangle(vpx, vpy, w, h), bounds);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		super.handleMouseInputSub();
		configDisplay.handleMouseInput();
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0:
				this.exitGui();
			default:
				break;
		}
	}
}
