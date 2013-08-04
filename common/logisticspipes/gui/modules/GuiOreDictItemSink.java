package logisticspipes.gui.modules;

import java.util.ArrayList;

import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.transport.Pipe;

public class GuiOreDictItemSink extends GuiWithPreviousGuiContainer {

	private final ModuleOreDictItemSink _itemSink;
	private final SimpleInventory tmpInv;

	private int mouseX = 0;
	private int mouseY = 0;

	public GuiOreDictItemSink(IInventory playerInventory, CoreRoutedPipe pipe, ModuleOreDictItemSink itemSink, GuiScreen previousGui, int slot) {
		super(null, pipe, previousGui);

		_itemSink = itemSink;

		tmpInv = new SimpleInventory(1, "Analyse Slot", 1);

		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 7, 8);

		dummy.addNormalSlotsForPlayerInventory(7, 126);

	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 208;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new SmallGuiButton(0, guiLeft + 38, guiTop + 18, 50, 10, "Add"));
		this.buttonList.add(new SmallGuiButton(1, guiLeft + 107, guiTop + 18, 50, 10, "Remove"));
		((GuiButton)buttonList.get(0)).enabled = false;
		((GuiButton)buttonList.get(1)).enabled = false;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_OreDict_ItemSink_ID;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			if(tmpInv.getStackInSlot(0) != null) {
				String oreName = getOreName(tmpInv.getStackInSlot(0));
				if(!oreName.equals("") && !_itemSink.oreList.contains(oreName)) {
					_itemSink.oreList.add(oreName);
					_itemSink.OreListChanged();
				}
			}
		} else if(par1GuiButton.id == 1) {
			if(tmpInv.getStackInSlot(0) != null && _itemSink.oreList.contains(getOreName(tmpInv.getStackInSlot(0)))) {
				_itemSink.oreList.remove(getOreName(tmpInv.getStackInSlot(0)));
				_itemSink.OreListChanged();
			}
		} else {
			super.actionPerformed(par1GuiButton);
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		int x = i - guiLeft;
		int y = j - guiTop;
		if(0 < x && x < 175 && 0 < y && y < 208) {
			mouseX = x;
			mouseY = y;
		}
		super.mouseClicked(i, j, k);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 126);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 6, guiTop + 7);
		BasicGuiHelper.drawRect(guiLeft + 26, guiTop + 5, guiLeft + 169, guiTop + 17, 0xff808080);
		if(tmpInv.getStackInSlot(0) != null) {
			String oreName = getOreName(tmpInv.getStackInSlot(0));
			mc.fontRenderer.drawString(oreName, guiLeft + 28, guiTop + 7, 0x404040);
			if(_itemSink.oreList.contains(oreName)) {
				((GuiButton)buttonList.get(0)).enabled = false;
				((GuiButton)buttonList.get(1)).enabled = true;
			} else if (oreName != "" && _itemSink.oreList.size() < 9) {
				((GuiButton)buttonList.get(0)).enabled = true;
				((GuiButton)buttonList.get(1)).enabled = false;
			} else {
				((GuiButton)buttonList.get(0)).enabled = false;
				((GuiButton)buttonList.get(1)).enabled = false;
			}
		} else {
			((GuiButton)buttonList.get(0)).enabled = false;
			((GuiButton)buttonList.get(1)).enabled = false;
		}
		BasicGuiHelper.drawRect(guiLeft + 5, guiTop + 30, guiLeft + 169, guiTop + 122, 0xff808080);
		for(int i=0; i < _itemSink.oreList.size() && i < 9; i++) {
			mc.fontRenderer.drawString(_itemSink.oreList.get(i), guiLeft + 7, guiTop + 32 + (10 * i), 0x404040);
			if(7 < mouseX && mouseX < 168 && 32 + (10 * i) < mouseY && mouseY < 32 + (10 * (i + 1))) {
				mouseX = 0;
				mouseY = 0;

				String oreName = _itemSink.oreList.get(i);
				if(!oreName.equals("")) {
					ArrayList<ItemStack> items = OreDictionary.getOres(oreName);
					if(items.size() > 0 && items.get(0) != null) {
						ItemStack t = items.get(0).copy();
						if(t.getItemDamage() == OreDictionary.WILDCARD_VALUE)
							t.setItemDamage(0);
						tmpInv.setInventorySlotContents(0, t);
					} else {
						tmpInv.clearInventorySlotContents(0);
					}
				}
			}
		}
	}

	private String getOreName(ItemStack s) {
		int oreid = OreDictionary.getOreID(s);
		if(oreid == -1)
			return "";
		return OreDictionary.getOreName(oreid);
	}
}
