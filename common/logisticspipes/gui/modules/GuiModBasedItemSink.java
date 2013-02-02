package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.transport.Pipe;

public class GuiModBasedItemSink extends GuiWithPreviousGuiContainer {
	
	private final ModuleModBasedItemSink _itemSink;
	private final SimpleInventory tmpInv;
	private int page = 0;
	
	public GuiModBasedItemSink(IInventory playerInventory, Pipe pipe, ModuleModBasedItemSink itemSink, GuiScreen previousGui, int slot) {
		super(null, pipe, previousGui);
		
		_itemSink = itemSink;
		
		tmpInv = new SimpleInventory(1, "Analyse Slot", 1);
		
		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 8, 8);
		
		dummy.addNormalSlotsForPlayerInventory(7, 100);
	    
	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 182;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, guiLeft + 120, guiTop + 5, 50, 20, "Add"));
		this.controlList.add(new SmallGuiButton(1, guiLeft + 135, guiTop + 30, 10, 10, "<"));
		this.controlList.add(new SmallGuiButton(2, guiLeft + 155, guiTop + 30, 10, 10, ">"));
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_ModBased_ItemSink_ID;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			if(_itemSink.modList.contains(ItemIdentifier.get(tmpInv.getStackInSlot(0)).getModName())) {
				_itemSink.modList.remove(ItemIdentifier.get(tmpInv.getStackInSlot(0)).getModName());
			} else {
				_itemSink.modList.add(ItemIdentifier.get(tmpInv.getStackInSlot(0)).getModName());
			}
			_itemSink.ModListChanged();
			page = 0;
		} else if(par1GuiButton.id == 1) {
			page = Math.max(0, page - 1);
		} else if(par1GuiButton.id == 2) {
			page++;
			if(page * 6 >= _itemSink.modList.size()) {
				page--;
			}
		} else {
			super.actionPerformed(par1GuiButton);			
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 100);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 7, guiTop + 7);
		if(tmpInv.getStackInSlot(0) != null) {
			mc.fontRenderer.drawString(ItemIdentifier.get(tmpInv.getStackInSlot(0)).getModName(), guiLeft + 28, guiTop + 13, 0x404040);
			if(_itemSink.modList.contains(ItemIdentifier.get(tmpInv.getStackInSlot(0)).getModName())) {
				((GuiButton)controlList.get(0)).displayString = "Remove";
			} else {
				((GuiButton)controlList.get(0)).displayString = "Add";
			}
			((GuiButton)controlList.get(0)).drawButton = true;
		} else {
			((GuiButton)controlList.get(0)).drawButton = false;
		}
		BasicGuiHelper.drawRect(guiLeft + 5, guiTop + 28, guiLeft + 169, guiTop + 97, 0xff808080);
		for(int i=(page * 6); i < _itemSink.modList.size() && i < 6 + (page * 6);i++) {
			mc.fontRenderer.drawString(_itemSink.modList.get(i), guiLeft + 8, guiTop + 32 + (11 * (i - (page * 6))), 0x404040);
		}
	}
}
