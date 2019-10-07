package logisticspipes.utils.gui;

import net.minecraft.client.gui.FontRenderer;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;

public class InputBar extends GuiTextField implements LogisticsBaseGuiScreen.EventListener {

	public enum Align {
		LEFT,
		CENTER,
		RIGHT
	}

	public int minNumber = 0;

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int height) {
		this(fontRenderer, screen, left, top, width, height, true);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int height, boolean isActive) {
		this(fontRenderer, screen, left, top, width, height, isActive, false);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int height, boolean isActive, boolean numberOnly) {
		this(fontRenderer, screen, left, top, width, height, isActive, numberOnly, Align.LEFT);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int height, boolean isActive, boolean numberOnly, Align align) {
		super(0, fontRenderer, left+2, top, width-4, height-2);
		screen.onGuiEvents.add(this);
		if (numberOnly) {
			setValidator((String s) -> {
				try {
					return Integer.parseInt(s) >= minNumber;
				} catch (NumberFormatException ignored) {
					return false;
				}
			});
			setMaxStringLength(5);
		} else
			setMaxStringLength(128);
	}

	public void reposition(int left, int top, int width, int height) {
		x = left+2;
		y = top;
		this.width = width-4;
		this.height = height-2;
	}

	@Override
	public void onUpdateScreen() {
		updateCursorCounter();
	}

	@Override
	public boolean onKeyboardInput() {
		return (isFocused() || GuiScreen.isAltKeyDown()) && ChatAllowedCharacters.isAllowedCharacter(Keyboard.getEventCharacter());
	}

	/**
	 * @return Boolean, true if click was handled.
	 */
	public boolean handleClick(int x, int y, int k) {
		if (k == 1 && x >= this.x && x < this.x + width && y >= this.y && y < y + height)
			setText("");
		return mouseClicked(x, y, k);
	}

	/**
	 * @return Boolean, true if key was handled.
	 */
	public boolean handleKey(char c, int i) {
		if (GuiScreen.isKeyComboCtrlC(i) && (getSelectedText().isEmpty()))
			GuiScreen.setClipboardString(this.getText());
		return textboxKeyTyped(c, i);
	}

	public void setInteger(int newValue) {
		setText(Integer.toString(Math.max(minNumber, newValue)));
	}

	public int getInteger() {
		try {
			return Math.max(minNumber, Integer.parseInt(getText()));
		} catch (NumberFormatException ignored) {
			return minNumber;
		}
	}

	public boolean isEmpty() {
		return getText().isEmpty();
	}

}
