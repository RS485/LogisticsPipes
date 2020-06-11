package logisticspipes.utils.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class LogisticsBaseTabGuiScreen extends LogisticsBaseGuiScreen {

	private int current_Tab;

	private int buttonNextFreeId = 0;

	private final List<TabSubGui> tabList = new ArrayList<>();
	private final List<Slot> hiddenSlots = new ArrayList<>();

	public LogisticsBaseTabGuiScreen(int xSize, int ySize) {
		super(xSize, ySize, 0, 0);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		tabList.forEach(TabSubGui::initTab);
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
		initGui();
	}

	protected int getFreeButtonId() {
		return buttonNextFreeId++;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		tabList.stream().filter(aTabList -> aTabList.isButtonFromGui(button)).forEach(aTabList -> aTabList.buttonClicked(button));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouse_x, int mouse_y) {
		GL11.glColor4d(1.0D, 1.0D, 1.0D, 1.0D);
		for (int i = 0; i < tabList.size(); i++) {
			GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * i) + 2, guiTop - 2, guiLeft + 27 + (25 * i), guiTop + 35, zLevel, false, true, true, false, true);
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop + 20, right, bottom, zLevel, true);
		GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * current_Tab) + 2, guiTop - 2, guiLeft + 27 + (25 * current_Tab), guiTop + 38, zLevel, true, true, true, false, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 135);

		int x = 6;
		for (TabSubGui aTabList : tabList) {
			aTabList.renderIcon(guiLeft + x, guiTop + 3);
			x += 25;
		}

		for (int i = 0; i < tabList.size(); i++) {
			if (current_Tab == i) {
				tabList.get(i).renderBackgroundContent();
			}
		}

		super.drawGuiContainerBackgroundLayer(f, mouse_x, mouse_y);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException {
		if (par3 == 0 && par1 > guiLeft && par1 < guiLeft + 220 && par2 > guiTop && par2 < guiTop + 20) {
			par1 -= guiLeft + 3;
			int select = Math.max(0, Math.min(par1 / 25, tabList.size() - 1));
			if (current_Tab != select) {
				tabList.get(current_Tab).leavingTab();
				tabList.get(select).enteringTab();
			}
			current_Tab = select;
		} else {
			for (int i = 0; i < tabList.size(); i++) {
				if (current_Tab == i) {
					if (tabList.get(i).handleClick(par1, par2, par3)) {
						return;
					}
				}
			}
			super.mouseClicked(par1, par2, par3);
		}
	}

	@Override
	protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException {
		for (int i = 0; i < tabList.size(); i++) {
			if (current_Tab == i) {
				if (tabList.get(i).handleKey(p_73869_2_, p_73869_1_)) {
					return;
				}
			}
		}
		if (p_73869_2_ == 1 || p_73869_2_ == mc.gameSettings.keyBindInventory.getKeyCode()) {
			tabList.forEach(TabSubGui::guiClose);
		}
		super.keyTyped(p_73869_1_, p_73869_2_);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		for (int i = 0; i < tabList.size(); i++) {
			if (current_Tab == i) {
				tabList.get(i).renderForgroundContent();
			}
		}
	}

	@Override
	protected void drawSlot(Slot slot) {
		if (hiddenSlots.contains(slot)) return;
		for (int i = 0; i < tabList.size(); i++) {
			if (tabList.get(i).isSlotForTab(slot)) {
				if (current_Tab != i || !tabList.get(i).showSlot(slot)) {
					return;
				}
			}
		}
		super.drawSlot(slot);
	}

	@Override
	protected boolean isMouseOverSlot(Slot slot, int par2, int par3) {
		if (!super.isMouseOverSlot(slot, par2, par3)) {
			return false;
		}
		if (hiddenSlots.contains(slot)) return false;
		for (int i = 0; i < tabList.size(); i++) {
			if (tabList.get(i).isSlotForTab(slot)) {
				if (current_Tab != i || !tabList.get(i).showSlot(slot)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void checkButtons() {
		super.checkButtons();
		for (GuiButton button : buttonList) {
			for (int i = 0; i < tabList.size(); i++) {
				if (tabList.get(i).isButtonFromGui(button)) {
					tabList.get(i).checkButton(button, current_Tab == i);
				}
			}
		}
	}

	protected void addTab(TabSubGui gui) {
		tabList.add(gui);
	}

	protected Slot addHiddenSlot(Slot slot) {
		hiddenSlots.add(slot);
		return slot;
	}

	protected abstract class TabSubGui {

		private final List<Slot> TAB_SLOTS = new ArrayList<>();
		private final List<GuiButton> TAB_BUTTONS = new ArrayList<>();

		public abstract void renderIcon(int x, int y);

		public abstract void renderBackgroundContent();

		public abstract void renderForgroundContent();

		public boolean isSlotForTab(Slot slot) {
			return TAB_SLOTS.contains(slot);
		}

		public Slot addSlot(Slot slot) {
			TAB_SLOTS.add(slot);
			return slot;
		}

		public GuiButton addButton(GuiButton button) {
			TAB_BUTTONS.add(LogisticsBaseTabGuiScreen.this.addButton(button));
			return button;
		}

		public boolean isButtonFromGui(GuiButton button) {
			return TAB_BUTTONS.contains(button);
		}

		public void initTab() {}

		public void checkButton(GuiButton button, boolean isTabActive) {
			if (TAB_BUTTONS.contains(button)) {
				button.visible = isTabActive;
			}
		}

		public void buttonClicked(GuiButton button) {}

		public boolean handleClick(int x, int y, int type) {
			return false;
		}

		public boolean handleKey(int code, char c) {
			return false;
		}

		public void guiClose() {}

		public boolean showSlot(Slot slot) {
			return true;
		}

		public void leavingTab() {}

		public void enteringTab() {}
	}
}
