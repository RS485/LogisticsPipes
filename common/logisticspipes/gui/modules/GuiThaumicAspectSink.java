package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import thaumcraft.api.EnumTag;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.transport.Pipe;

public class GuiThaumicAspectSink extends GuiWithPreviousGuiContainer {
	
	private final ModuleThaumicAspectSink _module;
	private final SimpleInventory tmpInv;
	
	public GuiThaumicAspectSink(IInventory playerInventory, Pipe pipe, ModuleThaumicAspectSink itemSink, GuiScreen previousGui, int slot) {
		super(null, pipe, previousGui);
		
		_module = itemSink;
		
		tmpInv = new SimpleInventory(1, "Aspect Reader", 1);
		
		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 150, 8);
		
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
		this.controlList.add(new GuiButton(0, guiLeft + 8, guiTop + 5, 50, 20, "Inject"));
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_ModBased_ItemSink_ID;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0 && tmpInv.getStackInSlot(0) != null) {
			_module.handleItem(tmpInv.getStackInSlot(0));
			tmpInv.setInventorySlotContents(0, null);
			_module.aspectListChanged();
		} else {
			super.actionPerformed(par1GuiButton);			
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, false);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 100);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 149, guiTop + 7);
		((GuiButton)controlList.get(0)).displayString = "...";
		if(tmpInv.getStackInSlot(0) != null) {
			((GuiButton)controlList.get(0)).displayString = "Inject";
		}
		/** 0xc0c0d7 **/
		BasicGuiHelper.drawRect(guiLeft + 5, guiTop + 28, guiLeft + 169, guiTop + 97, 0xff808080);		
		//BasicGuiHelper.drawSlotBackground(mc, guiLeft+60, guiTop+28);
		int tmpX = 60;
		int tmpY = 28;
		for(int i = 0; i < _module.aspectList.size(); i++) {
			if (i <= 3) {
				SimpleServiceLocator.thaumCraftProxy.renderAspectAt(EnumTag.get(_module.aspectList.get(i)), tmpX + (18*(i%3)), tmpY, this);
				continue;
			}
			if (i <= 6) {
				SimpleServiceLocator.thaumCraftProxy.renderAspectAt(EnumTag.get(_module.aspectList.get(i)), tmpX + (18*(i%3)), tmpY + (18*1), this);
				continue;
			}
			if (i <= 9) {
				SimpleServiceLocator.thaumCraftProxy.renderAspectAt(EnumTag.get(_module.aspectList.get(i)), tmpX + (18*(i%3)), tmpY + (18*2), this);
				continue;
			}
		}
	}
}
