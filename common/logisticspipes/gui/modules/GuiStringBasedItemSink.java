package logisticspipes.gui.modules;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;

import logisticspipes.interfaces.IStringBasedModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierInventory;

public class GuiStringBasedItemSink extends ModuleBaseGui {

	private final IStringBasedModule _itemSink;
	private final ItemIdentifierInventory tmpInv;
	private String name = "";

	private int mouseX = 0;
	private int mouseY = 0;

	public GuiStringBasedItemSink(IInventory playerInventory, LogisticsModule module) {
		super(null, module);
		if (!(module instanceof IStringBasedModule)) throw new IllegalArgumentException("Module must be string based");
		_itemSink = (IStringBasedModule) module;

		tmpInv = new ItemIdentifierInventory(1, "Analyse Slot", 1);

		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 7, 8);

		dummy.addNormalSlotsForPlayerInventory(7, 126);

		inventorySlots = dummy;
		xSize = 175;
		ySize = 208;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 38, guiTop + 18, 50, 10, "Add"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 107, guiTop + 18, 50, 10, "Remove"));
		buttonList.get(0).enabled = false;
		buttonList.get(1).enabled = false;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) throws IOException {
		if (par1GuiButton.id == 0) {
			if (!_itemSink.getStringList().contains(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()))) {
				_itemSink.getStringList().add(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()));
				_itemSink.listChanged();
			}
		} else if (par1GuiButton.id == 1) {
			if (tmpInv.getIDStackInSlot(0) != null && _itemSink.getStringList().contains(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()))) {
				_itemSink.getStringList().remove(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()));
				_itemSink.listChanged();
			} else if (!name.equals("") && _itemSink.getStringList().contains(name)) {
				_itemSink.getStringList().remove(name);
				_itemSink.listChanged();
			}
		} else {
			super.actionPerformed(par1GuiButton);
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		int x = i - guiLeft;
		int y = j - guiTop;
		if (0 < x && x < 175 && 0 < y && y < 208) {
			mouseX = x;
			mouseY = y;
		}
		super.mouseClicked(i, j, k);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 126);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 6, guiTop + 7);
		SimpleGraphics.drawRectNoBlend(guiLeft + 26, guiTop + 5, guiLeft + 169, guiTop + 17, Color.DARK_GREY, 0.0);
		if (tmpInv.getIDStackInSlot(0) != null) {
			name = "";
			mc.fontRenderer.drawString(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()), guiLeft + 28, guiTop + 7, 0x404040);
			if (_itemSink.getStringList().contains(_itemSink.getStringForItem(tmpInv.getIDStackInSlot(0).getItem()))) {
				buttonList.get(0).enabled = false;
				buttonList.get(1).enabled = true;
			} else if (_itemSink.getStringList().size() < 9) {
				buttonList.get(0).enabled = true;
				buttonList.get(1).enabled = false;
			} else {
				buttonList.get(0).enabled = false;
				buttonList.get(1).enabled = false;
			}
		} else if (name.equals("")) {
			buttonList.get(0).enabled = false;
			buttonList.get(1).enabled = false;
		} else {
			if (_itemSink.getStringList().contains(name)) {
				mc.fontRenderer.drawString(name, guiLeft + 28, guiTop + 7, 0x404040);
				buttonList.get(0).enabled = false;
				buttonList.get(1).enabled = true;
			} else {
				name = "";
				buttonList.get(0).enabled = false;
				buttonList.get(1).enabled = false;
			}
		}
		Gui.drawRect(guiLeft + 5, guiTop + 30, guiLeft + 169, guiTop + 122, Color.DARK_GREY.getValue());
		for (int i = 0; i < _itemSink.getStringList().size() && i < 9; i++) {
			int pointerX = var2 - guiLeft;
			int pointerY = var3 - guiTop;
			if (6 <= pointerX && pointerX < 168 && 31 + (10 * i) <= pointerY && pointerY < 31 + (10 * (i + 1))) {
				Gui.drawRect(guiLeft + 6, guiTop + 31 + (10 * i), guiLeft + 168, guiTop + 31 + (10 * (i + 1)), Color.LIGHT_GREY.getValue());
			}
			mc.fontRenderer.drawString(_itemSink.getStringList().get(i), guiLeft + 7, guiTop + 32 + (10 * i), 0x404040);
			if (6 <= mouseX && mouseX < 168 && 31 + (10 * i) <= mouseY && mouseY < 31 + (10 * (i + 1))) {
				name = _itemSink.getStringList().get(i);
				mouseX = 0;
				mouseY = 0;
				tmpInv.clearInventorySlotContents(0);
			}
		}
	}
}
