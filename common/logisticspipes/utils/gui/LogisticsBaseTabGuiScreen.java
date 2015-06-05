package logisticspipes.utils.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

public class LogisticsBaseTabGuiScreen extends LogisticsBaseGuiScreen {

	private int current_Tab;

	private final List<TabSubGui> tabList = new ArrayList<TabSubGui>();

	public LogisticsBaseTabGuiScreen(int xSize, int ySize) {
		super(xSize, ySize, 0, 0);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		for (int i = 0; i < tabList.size(); i++) {
			tabList.get(i).initTab();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		for (int i = 0; i < tabList.size(); i++) {
			if (tabList.get(i).isButtonFromGui(button)) {
				tabList.get(i).buttonClicked(button);
			}
		}
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
		for (int i = 0; i < tabList.size(); i++) {
			tabList.get(i).renderIcon(guiLeft + x, guiTop + 3);
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
	protected void mouseClicked(int par1, int par2, int par3) {
		if (par3 == 0 && par1 > guiLeft && par1 < guiLeft + 220 && par2 > guiTop && par2 < guiTop + 20) {
			par1 -= guiLeft + 3;
			int select = Math.max(0, Math.min(par1 / 25, tabList.size() - 1));
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
	protected void keyTyped(char p_73869_1_, int p_73869_2_) {
		for (int i = 0; i < tabList.size(); i++) {
			if (current_Tab == i) {
				if (tabList.get(i).handleKey(p_73869_2_, p_73869_1_)) {
					return;
				}
			}
		}
		if (p_73869_2_ == 1 || p_73869_2_ == mc.gameSettings.keyBindInventory.getKeyCode()) {
			for (int i = 0; i < tabList.size(); i++) {
				tabList.get(i).guiClose();
			}
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
	protected void func_146977_a(Slot slot) {
		for (int i = 0; i < tabList.size(); i++) {
			if (tabList.get(i).isSlotForTab(slot)) {
				if (current_Tab != i) {
					return;
				}
			}
		}
		super.func_146977_a(slot);
	}

	@Override
	protected boolean isMouseOverSlot(Slot slot, int par2, int par3) {
		if (!super.isMouseOverSlot(slot, par2, par3)) {
			return false;
		}
		for (int i = 0; i < tabList.size(); i++) {
			if (tabList.get(i).isSlotForTab(slot)) {
				if (current_Tab != i) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void checkButtons() {
		super.checkButtons();
		for (GuiButton button : (List<GuiButton>) buttonList) {
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

	protected abstract class TabSubGui {

		private final List<Slot> TAB_SLOTS = new ArrayList<Slot>();
		private final List<GuiButton> TAB_BUTTONS = new ArrayList<GuiButton>();

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
	}
}
