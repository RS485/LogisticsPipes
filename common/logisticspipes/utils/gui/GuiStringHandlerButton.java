package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiStringHandlerButton extends GuiButton {

	private final StringHandler handler;

	public GuiStringHandlerButton(int par1, int par2, int par3, StringHandler handler) {
		super(par1, par2, par3, "");
		this.handler = handler;
	}

	public GuiStringHandlerButton(int par1, int par2, int par3, int par4, int par5, StringHandler handler) {
		super(par1, par2, par3, par4, par5, "");
		this.handler = handler;
	}

	@Override
	public void drawButton(@Nonnull Minecraft par1Minecraft, int par2, int par3, float partial) {
		displayString = handler.getContent();
		super.drawButton(par1Minecraft, par2, par3, partial);
	}

	public interface StringHandler {

		String getContent();
	}

}
