package logisticspipes.utils.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Locale;

import logisticspipes.utils.Color;

import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.input.Keyboard;

public class SearchBar {

	public String searchinput1 = "";
	public String searchinput2 = "";
	private boolean isActive = false;
	private boolean displaycursor = true;
	private long oldSystemTime = 0;
	private int searchWidth = 150;
	private boolean numberOnly = false;
	private boolean alignRight = false;

	private final FontRenderer fontRenderer;
	private final LogisticsBaseGuiScreen screen;
	private int left, top, heigth, width;

	public SearchBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth) {
		this(fontRenderer, screen, left, top, width, heigth, true);
	}

	public SearchBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive) {
		this(fontRenderer, screen, left, top, width, heigth, isActive, false);
	}

	public SearchBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive, boolean numberOnly) {
		this(fontRenderer, screen, left, top, width, heigth, isActive, numberOnly, false);
	}

	public SearchBar(FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, int left, int top, int width, int heigth, boolean isActive, boolean numberOnly, boolean alignRight) {
		this.fontRenderer = fontRenderer;
		this.screen = screen;
		this.left = left;
		this.top = top;
		this.width = width;
		this.heigth = heigth;
		searchWidth = width - 10;
		this.isActive = isActive;
		this.numberOnly = numberOnly;
		this.alignRight = alignRight;
	}

	public void reposition(int left, int top, int width, int heigth) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.heigth = heigth;
		searchWidth = width - 10;
	}

	public void renderSearchBar() {
		if (isFocused()) {
			screen.drawRect(left + 0, top - 2, left + width - 0, top + heigth - 0, Color.BLACK);
			screen.drawRect(left + 1, top - 1, left + width - 1, top + heigth - 1, Color.WHITE);
		} else {
			screen.drawRect(left + 1, top - 1, left + width - 1, top + heigth - 1, Color.BLACK);
		}
		screen.drawRect(left + 2, top - 0, left + width - 2, top + heigth - 2, Color.DARKER_GREY);
		if (alignRight) {
			fontRenderer.drawString(searchinput1 + searchinput2, left + 5 + searchWidth - fontRenderer.getStringWidth(searchinput1 + searchinput2), top + 3, 0xFFFFFF);
		} else {
			fontRenderer.drawString(searchinput1 + searchinput2, left + 5, top + 3, 0xFFFFFF);
		}
		if (isFocused()) {
			int linex = 0;
			if (alignRight) {
				linex = left + 5 + searchWidth - fontRenderer.getStringWidth(searchinput2);
			} else {
				linex = left + 5 + fontRenderer.getStringWidth(searchinput1);
			}
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				screen.drawRect(linex, top + 1, linex + 1, top + heigth - 3, Color.WHITE);
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
				searchinput1 = "";
				searchinput2 = "";
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
			searchinput1 += searchinput2;
			searchinput2 = "";
			try {
				int value = Integer.valueOf(searchinput1);
				searchinput1 = Integer.toString(value);
			} catch (Exception e) {
				searchinput1 = "";
			}
			if (searchinput1.isEmpty() && searchinput2.isEmpty()) {
				searchinput1 = "0";
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
			if (searchinput1.length() > 0) {
				searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
			}
		} else if (i == 203) { //Left
			if (searchinput1.length() > 0) {
				searchinput2 = searchinput1.substring(searchinput1.length() - 1) + searchinput2;
				searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
			}
		} else if (i == 205) { //Right
			if (searchinput2.length() > 0) {
				searchinput1 += searchinput2.substring(0, 1);
				searchinput2 = searchinput2.substring(1);
			}
		} else if (i == 199) { //Home
			searchinput2 = searchinput1 + searchinput2;
			searchinput1 = "";
		} else if (i == 207) { //End
			searchinput1 = searchinput1 + searchinput2;
			searchinput2 = "";
		} else if (i == 211) { //Del
			if (searchinput2.length() > 0) {
				searchinput2 = searchinput2.substring(1);
			}
		} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-v
			boolean isFine = true;
			if (numberOnly) {
				try {
					Integer.valueOf(SearchBar.getClipboardString());
				} catch (Exception e) {
					isFine = false;
				}
			}
			if (isFine) {
				String toAdd = SearchBar.getClipboardString();
				while (fontRenderer.getStringWidth(searchinput1 + toAdd + searchinput2) > searchWidth) {
					toAdd = toAdd.substring(0, toAdd.length() - 1);
				}
				searchinput1 = searchinput1 + toAdd;
			}
		} else if ((!numberOnly && !Character.isISOControl(c)) || (numberOnly && Character.isDigit(c))) {
			if (fontRenderer.getStringWidth(searchinput1 + c + searchinput2) <= searchWidth) {
				searchinput1 += c;
			}
		} else {
			//ignore this key/character
		}
		return true;
	}

	public String getContent() {
		return searchinput1 + searchinput2;
	}

	public boolean isEmpty() {
		return searchinput1.isEmpty() && searchinput2.isEmpty();
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
