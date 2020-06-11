/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ItemSinkDefaultPacket;
import logisticspipes.network.packets.module.ItemSinkImportPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtils;

public class GuiItemSink extends ModuleBaseGui {

	private static final String PREFIX = "gui.itemsink.";
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");

	private final ModuleItemSink _itemSink;
	private final boolean isFuzzy;
	private int fuzzyPanelSelection = -1;

	public GuiItemSink(IInventory playerInventory, ModuleItemSink itemSink, boolean hasFuzzyUpgrade) {
		super(null, itemSink);
		isFuzzy = hasFuzzyUpgrade;
		_itemSink = itemSink;
		DummyContainer dummy = new DummyContainer(playerInventory, _itemSink.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		// Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}

	@Override
	public void initGui() {
		super.initGui();
		// Default item toggle:
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 50, height / 2 - 34, 30, 20, () -> StringUtils.translate(GuiItemSink.PREFIX + (_itemSink.isDefaultRoute() ? "Yes" : "No"))));
		buttonList.add(new SmallGuiButton(1, guiLeft + 10, guiTop + 37, 40, 10, StringUtils.translate(GuiItemSink.PREFIX + "import")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 0:
				_itemSink.setDefaultRoute(!_itemSink.isDefaultRoute());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkDefaultPacket.class).setDefault(_itemSink.isDefaultRoute()).setModulePos(_itemSink));
				break;
			case 1:
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkImportPacket.class).setModulePos(_itemSink));
				break;
		}

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(_itemSink.getFilterInventory().getName(), 8, 6, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiItemSink.PREFIX + "Inventory"), 8, ySize - 92, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiItemSink.PREFIX + "Defaultroute") + ":", 65, 45, 0x404040);

		if (isFuzzy) {
			int mx = par1 - guiLeft;
			int my = par2 - guiTop;
			if (!isMouseInFuzzyPanel(mx, my)) {
				fuzzyPanelSelection = -1;
			}
			int hovered_slot = -1;
			if (my >= 18 && my <= 18 + 16) {
				if ((mx - 8) % 18 <= 16 && (mx - 8) % 18 >= 0) {
					hovered_slot = (mx - 8) / 18;
				}
			}
			if (hovered_slot < 0 || hovered_slot >= 9) {
				hovered_slot = -1;
			}
			if (hovered_slot != -1) {
				fuzzyPanelSelection = hovered_slot;
			}
		}

		if (fuzzyPanelSelection != -1) {
			int posX = 8 + fuzzyPanelSelection * 18;
			int posY = 18 + 16;
			GuiGraphics.drawGuiBackGround(mc, posX, posY, posX + 70, posY + 27, zLevel, true, true, true, true, true);

			mc.fontRenderer.drawString(StringUtils.translate(GuiItemSink.PREFIX + "IgnoreData"), posX + 4, posY + 4, !_itemSink.isIgnoreData(fuzzyPanelSelection) ? 0x404040 : 0xFF4040);
			mc.fontRenderer.drawString(StringUtils.translate(GuiItemSink.PREFIX + "IgnoreNBT"), posX + 4, posY + 14, !_itemSink.isIgnoreNBT(fuzzyPanelSelection) ? 0x404040 : 0x40FF40);
		}

	}

	private boolean isMouseInFuzzyPanel(int mx, int my) {
		if (fuzzyPanelSelection == -1) {
			return false;
		}
		int posX = 8 + fuzzyPanelSelection * 18;
		int posY = 18 + 16;
		return mx >= posX && my >= posY && mx <= posX + 70 && my <= posY + 27;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int which) {
		if (isMouseInFuzzyPanel(mouseX - guiLeft, mouseY - guiTop)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, which);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int par3) throws IOException {
		if (isMouseInFuzzyPanel(mouseX - guiLeft, mouseY - guiTop)) {
			int posX = 8 + fuzzyPanelSelection * 18;
			int posY = 18 + 16;
			int sel = -1;
			if (mouseX - guiLeft >= posX + 4 && mouseX - guiLeft <= posX + 70 - 4) {
				if (mouseY - guiTop >= posY + 4 && mouseY - guiTop <= posY + 27 - 4) {
					sel = (mouseY - guiTop - posY - 4) / 11;
				}
			}
			if (sel == 0) {
				_itemSink.setIgnoreData(fuzzyPanelSelection, null);
			} else if (sel == 1) {
				_itemSink.setIgnoreNBT(fuzzyPanelSelection, null);
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, par3);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiItemSink.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
