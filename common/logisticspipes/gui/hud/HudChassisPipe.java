package logisticspipes.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.client.FMLClientHandler;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

public class HudChassisPipe extends BasicHUDGui {

	private final PipeLogisticsChassi pipe;
	private final ItemIdentifierInventory moduleInventory;

	private int selected = -1;
	private int modulePage = 0;

	private int xCursor;
	private int yCursor;

	public HudChassisPipe(PipeLogisticsChassi pipeLogisticsChassi, ItemIdentifierInventory _moduleInventory) {
		pipe = pipeLogisticsChassi;
		moduleInventory = _moduleInventory;
		for (int i = 0; i < pipe.getChassiSize(); i++) {
			addButton(new ItemButton(moduleInventory, i, -45, -35 + ((i % 3) * 27), 20, 25));
		}

		addButton(new BasicHUDButton("<", -45, -45, 8, 8) {

			@Override
			public boolean shouldRenderButton() {
				return !isSlotSelected();
			}

			@Override
			public void clicked() {
				modulePage--;
			}

			@Override
			public boolean buttonEnabled() {
				return modulePage > 0;
			}
		});
		addButton(new BasicHUDButton(">", -33, -45, 8, 8) {

			@Override
			public boolean shouldRenderButton() {
				return !isSlotSelected();
			}

			@Override
			public void clicked() {
				modulePage++;
			}

			@Override
			public boolean buttonEnabled() {
				return modulePage < ((pipe.getChassiSize() - 1) / 3);
			}
		});
		addButton(new BasicHUDButton("x", 37, -45, 8, 8) {

			@Override
			public boolean shouldRenderButton() {
				return isSlotSelected();
			}

			@Override
			public void clicked() {
				resetSelection();
			}

			@Override
			public boolean buttonEnabled() {
				return true;
			}
		});
	}

	@Override
	public void renderHeadUpDisplay(double distance, boolean day, boolean shifted, Minecraft mc, IHUDConfig config) {
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 64);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 64);
		}
		GuiGraphics.drawGuiBackGround(mc, -50, -50, 50, 50, 0, false);
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 127);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);
		}
		GL11.glTranslatef(0.0F, 0.0F, (float) (-0.00005F * distance));
		super.renderHeadUpDisplay(distance, day, shifted, mc, config);
		if (selected != -1) {
			LogisticsModule selectedmodule = pipe.getSubModule(selected);
			if (selectedmodule == null) {
				return;
			}

			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 64);
			GuiGraphics.drawGuiBackGround(mc, -23, -35, 45, 45, 0, false);
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);

			if (selectedmodule instanceof IHUDModuleHandler && ((IHUDModuleHandler) selectedmodule).getHUDRenderer() != null) {
				GL11.glTranslatef(11.0F, 5.0F, (float) (-0.00005F * distance));
				((IHUDModuleHandler) selectedmodule).getHUDRenderer().renderContent(shifted);
				if (((IHUDModuleHandler) selectedmodule).getHUDRenderer().getButtons() != null) {
					for (IHUDButton button : ((IHUDModuleHandler) selectedmodule).getHUDRenderer().getButtons()) {
						button.renderAlways(shifted);
						if (button.shouldRenderButton()) {
							button.renderButton(button.isFocused(), button.isblockFocused(), shifted);
						}
						if (!button.buttonEnabled() || !button.shouldRenderButton()) {
							continue;
						}
						if ((button.getX() - 1 < (xCursor - 11) && (xCursor - 11) < (button.getX() + button.sizeX() + 1)) && (button.getY() - 1 < (yCursor - 5) && (yCursor - 5) < (button.getY() + button.sizeY() + 1))) {
							if (!button.isFocused() && !button.isblockFocused()) {
								button.setFocused();
							} else if (button.focusedTime() > 400 && !button.isblockFocused()) {
								button.clicked();
								button.blockFocused();
							}
						} else if (button.isFocused() || button.isblockFocused()) {
							button.clearFocused();
						}
					}
				}
				GL11.glTranslatef(-11.0F, -5.0F, (float) (0.00005F * distance));
			} else {
				GL11.glTranslatef(0.0F, 0.0F, (float) (-0.00005F * distance));
				mc.fontRenderer.drawString("Nothing", -5, -15, 0);
				mc.fontRenderer.drawString("to", 9, -5, 0);
				mc.fontRenderer.drawString("display", -5, 5, 0);
				GL11.glTranslatef(0.0F, 0.0F, (float) (0.00005F * distance));
			}
		} else {
			GL11.glTranslatef(0.0F, 0.0F, (float) (-0.005F * distance));
			GL11.glScalef(1.5F, 1.5F, 0.0001F);
			GL11.glScalef(0.8F, 0.8F, -1F);
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, -15, -35, 3, 12, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
		}
		GL11.glTranslatef(0.0F, 0.0F, (float) (0.00005F * distance));
	}

	@Override
	public boolean display(IHUDConfig config) {
		if (!config.isHUDChassie()) {
			return false;
		}
		for (int i = 0; i < moduleInventory.getSizeInventory(); i++) {
			ItemStack stack = moduleInventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return -50 < x && x < 50 && -50 < y && y < 50;
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x, y);
		xCursor = x;
		yCursor = y;
	}

	private void moduleClicked(int number) {
		selected = number;
		if (selected != -1) {
			LogisticsModule selectedmodule = pipe.getSubModule(selected);
			if (selectedmodule instanceof IHUDModuleHandler) {
				((IHUDModuleHandler) selectedmodule).startHUDWatching();
			}
		}
	}

	private void resetSelection() {
		if (selected != -1) {
			LogisticsModule selectedmodule = pipe.getSubModule(selected);
			if (selectedmodule instanceof IHUDModuleHandler) {
				((IHUDModuleHandler) selectedmodule).stopHUDWatching();
			}
		}
		selected = -1;
	}

	private boolean isSlotSelected() {
		return selected != -1;
	}

	private boolean isSlotSelected(int number) {
		return selected == number;
	}

	private boolean shouldDisplayButton(int number) {
		return modulePage * 3 <= number && number < (modulePage + 1) * 3;
	}

	public void stopWatching() {
		resetSelection();
	}

	private class ItemButton extends BasicHUDButton {

		private ItemIdentifierInventory inv;
		private int position;

		public ItemButton(ItemIdentifierInventory inv, int position, int x, int y, int width, int heigth) {
			super("item." + position, x, y, width, heigth);
			this.inv = inv;
			this.position = position;
		}

		@Override
		public void clicked() {
			moduleClicked(position);
		}

		@Override
		public void renderButton(boolean hover, boolean clicked, boolean shifted) {
			Minecraft mc = FMLClientHandler.instance().getClient();
			GL11.glEnable(GL11.GL_BLEND);

			if (shifted || hover || isSlotSelected(position)) {
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
			} else {
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			}

			GL11.glTranslatef(0.0F, 0.0F, -0.001F);
			GL11.glScaled(0.5D, 0.5D, 1.0D);
			if (isSlotSelected(position)) {
				GuiGraphics.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2 + 19, (posY + sizeY) * 2, 0, false, true, true, true, false);
			} else {
				GuiGraphics.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2, (posY + sizeY) * 2, 0, false);
			}
			GL11.glScaled(2.0D, 2.0D, 1.0D);
			GL11.glTranslatef(0.0F, 0.0F, 0.001F);

			ItemStack module = inv.getStackInSlot(position);

			if (module != null) {
				boolean renderInColor = buttonEnabled() || isSlotSelected(position);
				ItemStackRenderer itemStackRenderer = new ItemStackRenderer(posX + ((sizeX - 16) / 2), posY + ((sizeY - 16) / 2), -0.002F, shifted, renderInColor);
				itemStackRenderer.setItemstack(module).setDisplayAmount(DisplayAmount.NEVER);

				itemStackRenderer.renderInGui();
			}
		}

		@Override
		public void renderAlways(boolean shifted) {
			if (inv.getStackInSlot(position) == null && shouldDisplayButton(position)) {
				GL11.glEnable(GL11.GL_BLEND);
				if (shifted) {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.3F);
				}
				GL11.glScaled(0.5D, 0.5D, 1.0D);
				Minecraft mc = FMLClientHandler.instance().getClient();
				GuiGraphics.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2, (posY + sizeY) * 2, 0, false);
			}
		}

		@Override
		public boolean shouldRenderButton() {
			return inv.getStackInSlot(position) != null && shouldDisplayButton(position);
		}

		@Override
		public boolean buttonEnabled() {
			return !isSlotSelected();
		}
	}
}
