package logisticspipes.gui.popup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SubGuiScreen;

public class ActionChoisePopup extends SubGuiScreen {

	private final String message;
	private final String leftButton;
	private final Runnable leftAction;
	private final String rightButton;
	private final Runnable rightAction;
	private final boolean buttonMin;

	public ActionChoisePopup(String message, String leftButton, Runnable leftAction, String rightButton, Runnable rightAction) {
		super(100, 100, 0, 0);
		this.message = message;
		this.leftButton = leftButton;
		this.leftAction = leftAction;
		this.rightButton = rightButton;
		this.rightAction = rightAction;
		int sizeX = Minecraft.getMinecraft().fontRenderer.getStringWidth(message);
		int leftX = Minecraft.getMinecraft().fontRenderer.getStringWidth(leftButton);
		int rightX = Minecraft.getMinecraft().fontRenderer.getStringWidth(rightButton);
		this.xSize = Math.max(sizeX + 20, leftX + rightX + 70);
		this.ySize = 55;
		this.buttonMin = xSize == leftX + rightX + 70;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();

		if (buttonMin) {
			buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 25, Minecraft.getMinecraft().fontRenderer.getStringWidth(leftButton) + 20, 20, leftButton));
			buttonList.add(new GuiButton(1, guiLeft + Minecraft.getMinecraft().fontRenderer.getStringWidth(leftButton) + 40, guiTop + 25,
					Minecraft.getMinecraft().fontRenderer.getStringWidth(rightButton) + 20, 20, rightButton));
		} else {
			buttonList.add(new GuiButton(0, guiLeft + (this.xSize / 4) - ((Minecraft.getMinecraft().fontRenderer.getStringWidth(leftButton) + 20) / 2),
					guiTop + 25, Minecraft.getMinecraft().fontRenderer.getStringWidth(leftButton) + 20, 20, leftButton));
			buttonList.add(new GuiButton(1, guiLeft + (this.xSize * 3 / 4) - ((Minecraft.getMinecraft().fontRenderer.getStringWidth(rightButton) + 20) / 2),
					guiTop + 25, Minecraft.getMinecraft().fontRenderer.getStringWidth(rightButton) + 20, 20, rightButton));
		}
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		mc.fontRenderer.drawStringWithShadow(message, xCenter - (mc.fontRenderer.getStringWidth(message) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0:
				leftAction.run();
				exitGui();
				break;
			case 1:
				rightAction.run();
				exitGui();
				break;
		}
	}
}
