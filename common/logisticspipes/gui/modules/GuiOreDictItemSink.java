package logisticspipes.gui.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierInventory;

public class GuiOreDictItemSink extends ModuleBaseGui {

	private final ItemIdentifierInventory tmpInv;

	// FIXME please…
	private final ArrayList<String> guiLocalOreList;
	private final List<String> unsunkNames = new ArrayList<>();
	private int mouseX = 0;
	private int mouseY = 0;
	private int currentOffset = 0;

	public GuiOreDictItemSink(IInventory playerInventory, ModuleOreDictItemSink moduleIn) {
		super(null, moduleIn);
		guiLocalOreList = new ArrayList<>(moduleIn.oreList);

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
		buttonList.add(new SmallGuiButton(0, guiLeft + 159, guiTop + 5, 10, 10, ""));
		buttonList.add(new SmallGuiButton(1, guiLeft + 159, guiTop + 17, 10, 10, ""));
		buttonList.get(0).enabled = true;
		buttonList.get(1).enabled = true;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) throws IOException {
		if (par1GuiButton.id == 0) {
			currentOffset -= 1;
		} else if (par1GuiButton.id == 1) {
			currentOffset += 1;
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
		int pointerX = var2 - guiLeft;
		int pointerY = var3 - guiTop;
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 126);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 6, guiTop + 7);

		if (tmpInv.getIDStackInSlot(0) != null) {
			List<String> oreNames = getOreNames(tmpInv.getStackInSlot(0));
			oreNames.stream().filter(name -> !unsunkNames.contains(name)).forEach(unsunkNames::add);
			tmpInv.clearInventorySlotContents(0);
		}

		if (currentOffset > unsunkNames.size() - 2) {
			currentOffset = unsunkNames.size() - 2;
		}
		if (currentOffset < 0) {
			currentOffset = 0;
		}

		//draw unsunk list and highlight bar, handle clicks
		Gui.drawRect(guiLeft + 26, guiTop + 5, guiLeft + 159, guiTop + 27, Color.DARK_GREY.getValue());
		for (int i = 0; i + currentOffset < unsunkNames.size() && i < 2; i++) {
			if (27 <= pointerX && pointerX < 158 && 6 + (10 * i) <= pointerY && pointerY < 6 + (10 * (i + 1))) {
				Gui.drawRect(guiLeft + 27, guiTop + 6 + (10 * i), guiLeft + 158, guiTop + 6 + (10 * (i + 1)),
						Color.LIGHT_GREY.getValue());
			}
			mc.fontRenderer
					.drawString(unsunkNames.get(currentOffset + i), guiLeft + 28, guiTop + 7 + (10 * i), 0x404040);
			if (27 <= mouseX && mouseX < 158 && 6 + (10 * i) <= mouseY && mouseY < 6 + (10 * (i + 1))) {
				mouseX = 0;
				mouseY = 0;
				if (guiLocalOreList.size() < 9) {
					String oreName = unsunkNames.get(currentOffset + i);
					if (!guiLocalOreList.contains(oreName)) {
						guiLocalOreList.add(oreName);
					}
					unsunkNames.remove(oreName);
				}
			}
		}

		//draw main list and highlight bar, handle clicks
		Gui.drawRect(guiLeft + 5, guiTop + 30, guiLeft + 169, guiTop + 122, Color.DARK_GREY.getValue());
		for (int i = 0; i < guiLocalOreList.size() && i < 9; i++) {
			if (6 <= pointerX && pointerX < 168 && 31 + (10 * i) <= pointerY && pointerY < 31 + (10 * (i + 1))) {
				Gui.drawRect(guiLeft + 6, guiTop + 31 + (10 * i), guiLeft + 168, guiTop + 31 + (10 * (i + 1)),
						Color.LIGHT_GREY.getValue());
			}
			mc.fontRenderer.drawString(guiLocalOreList.get(i), guiLeft + 7, guiTop + 32 + (10 * i), 0x404040);
			if (6 <= mouseX && mouseX < 168 && 31 + (10 * i) <= mouseY && mouseY < 31 + (10 * (i + 1))) {
				mouseX = 0;
				mouseY = 0;
				String oreName = guiLocalOreList.get(i);
				if (!unsunkNames.contains(oreName)) {
					unsunkNames.add(oreName);
				}
				guiLocalOreList.remove(oreName);
			}
		}
	}

	private List<String> getOreNames(@Nonnull ItemStack stack) {
		int[] oreids = OreDictionary.getOreIDs(stack);
		List<String> oreNames = new ArrayList<>(oreids.length);
		for (int oreid : oreids) {
			String oreName = OreDictionary.getOreName(oreid);
			if (oreName != null && !oreName.equals("Unknown") && !oreNames.contains(oreName)) {
				oreNames.add(oreName);
			}
		}
		return oreNames;
	}
}
