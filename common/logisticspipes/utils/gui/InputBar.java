package logisticspipes.utils.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Locale;

import logisticspipes.utils.Color;

import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.input.Keyboard;

public class InputBar {

	public enum Align {
		LEFT,
		CENTER,
		RIGHT;
	}

	public int minNumber = 0;

	public String input1 = "";
	public String input2 = "";
	private boolean isActive = false;
	private boolean displaycursor = true;
	private long oldSystemTime = 0;
	private int searchWidth = 150;
	private boolean numberOnly = false;
	private Align align = Align.LEFT;

	private final FontRenderer fontRenderer;
	private final LogisticsBaseGuiScreen screen;
	private int left, top, heigth, width;

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth) {
		this(fontRenderer, screen, left, top, width, heigth, true);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive) {
		this(fontRenderer, screen, left, top, width, heigth, isActive, false);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive, boolean numberOnly) {
		this(fontRenderer, screen, left, top, width, heigth, isActive, numberOnly, Align.LEFT);
	}

	public InputBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive, boolean numberOnly, Align align) {
		this.fontRenderer = fontRenderer;
		this.screen = screen;
		this.left = left;
		this.top = top;
		this.width = width;
		this.heigth = heigth;
		searchWidth = width - (int) (4.5f + (this.heigth - 9) / 2f);
		this.isActive = isActive;
		this.numberOnly = numberOnly;
		this.align = align;
	}

	public void reposition(int left, int top, int width, int heigth) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.heigth = heigth;
		searchWidth = width - (int) (4.5f + (this.heigth - 9) / 2f);
	}

	public void renderSearchBar() {
		if (isFocused()) {
			screen.drawRect(left + 0, top - 2, left + width - 0, top + heigth - 0, Color.BLACK);
			screen.drawRect(left + 1, top - 1, left + width - 1, top + heigth - 1, Color.WHITE);
		} else {
			screen.drawRect(left + 1, top - 1, left + width - 1, top + heigth - 1, Color.BLACK);
		}
		screen.drawRect(left + 2, top - 0, left + width - 2, top + heigth - 2, Color.DARKER_GREY);
		if (align == Align.RIGHT) {
			fontRenderer.drawString(input1 + input2, left + 2 + (this.heigth - 9) / 2f + searchWidth - fontRenderer.getStringWidth(input1 + input2), top + (this.heigth - 9) / 2f, 0xFFFFFF, false);
		} else if (align == Align.CENTER) {
			fontRenderer.drawString(input1 + input2, left + 2 + (this.heigth - 9) / 2f + (searchWidth - fontRenderer.getStringWidth(input1 + input2)) / 2f, top + (this.heigth - 9) / 2f, 0xFFFFFF, false);
		} else {
			fontRenderer.drawString(input1 + input2, left + 2 + (this.heigth - 9) / 2f, top + (this.heigth - 9) / 2f, 0xFFFFFF, false);
		}
		if (isFocused()) {
			float linex = 0;
			if (align == Align.RIGHT) {
				linex = left + 2 + (this.heigth - 9) / 2f + searchWidth - fontRenderer.getStringWidth(input2);
			} else if (align == Align.CENTER) {
				linex = left + 2 + (this.heigth - 9) / 2f + (searchWidth - fontRenderer.getStringWidth(input2)) / 2f + (fontRenderer.getStringWidth(input1)) / 2f;
			} else {
				linex = left + 2 + (this.heigth - 9) / 2f + fontRenderer.getStringWidth(input1);
			}
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				screen.drawRect((int) (linex), top + 1, (int) (linex + 1), top + heigth - 3, Color.WHITE);
			}
		}
	}

	/**
	 * @return Boolean, true if click was handled.
	 */
	public boolean handleClick(int x, int y, int k) {
		if (x >= left + 2 && x < left + width - 2 && y >= top && y < top + heigth) {
			focus();
			if (k == 1) {
				input1 = "";
				input2 = "";
			}
			return true;
		} else if (isFocused()) {
			unFocus();
			return true;
		}
		return false;
	}

	private void unFocus() {
		isActive = false;
		if (numberOnly) {
			input1 += input2;
			input2 = "";
			try {
				int value = Integer.valueOf(input1);
				value = Math.max(value, minNumber);
				input1 = Integer.toString(value);
			} catch (Exception e) {
				input1 = "";
			}
			if (input1.isEmpty() && input2.isEmpty()) {
				input1 = Integer.toString(minNumber);
			}
		}
	}

	private void focus() {
		isActive = true;
	}

	public boolean isFocused() {
		return isActive;
	}

	/**
	 * @return Boolean, true if key was handled.
	 */
	public boolean handleKey(char c, int i) {
		if (!isFocused()) {
			return false;
		}
		if (i == 1) {
			return false;
		}
		if (c == 13 || i == 28) { //Enter
			unFocus();
		} else if (c == 8 || (i == 14 && System.getProperty("os.name").toLowerCase(Locale.US).contains("mac"))) { //Backspace
			if (input1.length() > 0) {
				input1 = input1.substring(0, input1.length() - 1);
			}
		} else if (i == 203) { //Left
			if (input1.length() > 0) {
				input2 = input1.substring(input1.length() - 1) + input2;
				input1 = input1.substring(0, input1.length() - 1);
			}
		} else if (i == 205) { //Right
			if (input2.length() > 0) {
				input1 += input2.substring(0, 1);
				input2 = input2.substring(1);
			}
		} else if (i == 199) { //Home
			input2 = input1 + input2;
			input1 = "";
		} else if (i == 207) { //End
			input1 = input1 + input2;
			input2 = "";
		} else if (i == 211) { //Del
			if (input2.length() > 0) {
				input2 = input2.substring(1);
			}
		} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-v
			boolean isFine = true;
			if (numberOnly) {
				try {
					Integer.valueOf(InputBar.getClipboardString());
				} catch (Exception e) {
					isFine = false;
				}
			}
			if (isFine) {
				String toAdd = InputBar.getClipboardString();
				while (fontRenderer.getStringWidth(input1 + toAdd + input2) > searchWidth) {
					toAdd = toAdd.substring(0, toAdd.length() - 1);
				}
				input1 = input1 + toAdd;
			}
		} else if ((!numberOnly && !Character.isISOControl(c)) || (numberOnly && Character.isDigit(c))) {
			if (fontRenderer.getStringWidth(input1 + c + input2) <= searchWidth) {
				input1 += c;
			}
		} else {
			//ignore this key/character
		}
		return true;
	}

	public String getContent() {
		return input1 + input2;
	}

	public boolean isEmpty() {
		return input1.isEmpty() && input2.isEmpty();
	}

	private static String getClipboardString() {
		try {
			Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object) null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (Exception exception) {}
		return "";
	}
}
