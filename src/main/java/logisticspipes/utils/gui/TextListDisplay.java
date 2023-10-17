package logisticspipes.utils.gui;

import java.util.Collections;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.TextFormatting;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.utils.Color;
import network.rs485.logisticspipes.util.TextUtil;

public class TextListDisplay {

	public interface List {

		int getSize();

		String getTextAt(int index);

		int getTextColor(int index);
	}

	private final List list;
	private final IGuiAccess gui;

	private final int borderTop;
	private final int borderRight;
	private final int borderBottom;
	private final int borderLeft;
	private final int elementPerPage;

	private int mouseClickX = 0;
	private int mouseClickY = 0;
	private int mousePosX = 0;
	private int mousePosY = 0;
	private int scroll = 0;
	@Getter
	@Setter
	private int selected = -1;
	private int hover = -1;

	public TextListDisplay(IGuiAccess gui, int borderLeft, int borderTop, int borderRight, int borderBottom, int elementPerPage, List list) {
		this.list = list;
		this.gui = gui;
		this.borderTop = borderTop;
		this.borderRight = borderRight;
		this.borderBottom = borderBottom;
		this.borderLeft = borderLeft;
		this.elementPerPage = elementPerPage;
	}

	public void mouseClicked(int i, int j, int k) {
		mouseClickX = i;
		mouseClickY = j;
	}

	public void renderGuiBackground(int mouseX, int mouseY) {
		mousePosX = mouseX;
		mousePosY = mouseY;

		Gui.drawRect(gui.getGuiLeft() + borderLeft, gui.getGuiTop() + borderTop, gui.getRight() - borderRight, gui.getBottom() - borderBottom, Color.getValue(Color.GREY));

		if (scroll + elementPerPage > list.getSize()) {
			scroll = list.getSize() - elementPerPage;
		}
		if (scroll < 0) {
			scroll = 0;
		}

		boolean flag = false;

		hover = -1;
		if (gui.getGuiLeft() + borderLeft + 2 < this.mousePosX
				&& this.mousePosX < gui.getRight() - borderRight - 2 && gui.getGuiTop() + borderTop + 2 < this.mousePosY
				&& this.mousePosY < gui.getGuiTop() + borderTop + 3 + (elementPerPage * 10)) {
			hover = scroll + (this.mousePosY - gui.getGuiTop() - borderTop - 3) / 10;
		}
		if (list.getSize() == 0 || hover >= list.getSize()) {
			hover = -1;
		}

		if (gui.getGuiLeft() + borderLeft + 2 < this.mouseClickX
				&& this.mouseClickX < gui.getRight() - borderRight - 2 && gui.getGuiTop() + borderTop + 2 < this.mouseClickY
				&& this.mouseClickY < gui.getGuiTop() + borderTop + 3 + (elementPerPage * 10)) {
			selected = scroll + (this.mouseClickY - gui.getGuiTop() - borderTop - 3) / 10;
			mouseClickX = -1;
			mouseClickY = -1;
		}

		for (int i = scroll; i < list.getSize() && (i - scroll) < elementPerPage; i++) {
			if (i == selected) {
				Gui.drawRect(gui.getGuiLeft() + borderLeft + 2, gui.getGuiTop() + borderTop + 2 + ((i - scroll) * 10), gui.getRight() - borderRight - 2, gui.getGuiTop() + borderTop + 13 + ((i - scroll) * 10), Color.getValue(Color.DARKER_GREY));
				flag = true;
			}
			String name = list.getTextAt(i);
			name = TextUtil.getTrimmedString(name, gui.getXSize() - borderRight - borderLeft - 6, gui.getMC().fontRenderer, "...");
			gui.getMC().fontRenderer.drawString(name, gui.getGuiLeft() + borderLeft + 4, gui.getGuiTop() + borderTop + 4 + ((i - scroll) * 10), list.getTextColor(i));
		}

		if (!flag) {
			selected = -1;
		}
	}

	public void renderGuiForeground() {
		if (hover != -1) {
			GuiGraphics.drawToolTip(mousePosX - gui.getGuiLeft(), mousePosY - gui.getGuiTop(), Collections.singletonList(list.getTextAt(hover)), TextFormatting.WHITE);
		}
	}

	public void scrollUp() {
		scroll++;
	}

	public void scrollDown() {
		if (scroll > 0) {
			scroll--;
		}
	}

	public void mouseScrollUp() {
		if (gui.getGuiLeft() + borderLeft < mousePosX
				&& mousePosX < gui.getRight() - borderRight && gui.getGuiTop() + borderTop < mousePosY && mousePosY < gui.getBottom() + borderBottom) {
			scrollUp();
		}
	}

	public void mouseScrollDown() {
		if (gui.getGuiLeft() + borderLeft < mousePosX
				&& mousePosX < gui.getRight() - borderRight && gui.getGuiTop() + borderTop < mousePosY && mousePosY < gui.getBottom() + borderBottom) {
			scrollDown();
		}
	}
}
