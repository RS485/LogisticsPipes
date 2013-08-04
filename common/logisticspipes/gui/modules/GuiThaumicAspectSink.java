package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import buildcraft.transport.Pipe;

public class GuiThaumicAspectSink extends GuiWithPreviousGuiContainer {
	
	private final ModuleThaumicAspectSink _module;
	private final SimpleInventory tmpInv;
	
	public GuiThaumicAspectSink(IInventory playerInventory, CoreRoutedPipe pipe, ModuleThaumicAspectSink itemSink, GuiScreen previousGui, int slot) {
		super(null, pipe, previousGui);
		
		_module = itemSink;
		
		tmpInv = new SimpleInventory(1, "Aspect Reader", 1);
		
		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 79, 8);
		
		dummy.addNormalSlotsForPlayerInventory(7, 100);
	    
	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 182;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, guiLeft + 8, guiTop + 53, 50, 20, "Inject"));
		this.buttonList.add(new GuiButton(1, guiLeft + 116, guiTop + 53, 50, 20, "Clear"));
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Thaumic_AspectSink_ID;
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0 && tmpInv.getStackInSlot(0) != null) {
			_module.handleItem(tmpInv.getStackInSlot(0));
			tmpInv.clearInventorySlotContents(0);
		} else if(par1GuiButton.id == 1) {
			_module.clearAspectList();
		} else {
			super.actionPerformed(par1GuiButton);		
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, false);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 100);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 78, guiTop + 7);
		if(tmpInv.getStackInSlot(0) != null) {
			((GuiButton)buttonList.get(0)).displayString = "Inject";
		} else {
			((GuiButton)buttonList.get(0)).displayString = "...";
		}
		if(_module.aspectList.size() == 0) {
			((GuiButton)buttonList.get(1)).displayString = "...";
		} else {
			((GuiButton)buttonList.get(1)).displayString = "Clear";
		}
		
		/** back **/
		BasicGuiHelper.drawRect(guiLeft + 5, guiTop + 28, guiLeft + 169, guiTop + 97, 0xff808080);
		BasicGuiHelper.drawRect(guiLeft + 58, guiTop + 34, guiLeft + 116, guiTop + 92, 0xff807080);	
		
		/** Top Left L shape **/
		BasicGuiHelper.drawRect(guiLeft + 58, guiTop + 34, guiLeft + 61, guiTop + 54, 0xff803080);	
		BasicGuiHelper.drawRect(guiLeft + 58, guiTop + 34, guiLeft + 78, guiTop + 37, 0xff803080);		
		
		/** Top Right L shape **/
		BasicGuiHelper.drawRect(guiLeft + 116, guiTop + 34, guiLeft + 113, guiTop + 54, 0xff803080);	
		BasicGuiHelper.drawRect(guiLeft + 116, guiTop + 34, guiLeft + 96, guiTop + 37, 0xff803080);		
		
		/** Bottom Right L shape **/
		BasicGuiHelper.drawRect(guiLeft + 116, guiTop + 72, guiLeft + 113, guiTop + 92, 0xff803080);	
		BasicGuiHelper.drawRect(guiLeft + 116, guiTop + 89, guiLeft + 96, guiTop + 92, 0xff803080);		
		
		/** Bottom Left L shape **/
		BasicGuiHelper.drawRect(guiLeft + 58, guiTop + 72, guiLeft + 61, guiTop + 92, 0xff803080);	
		BasicGuiHelper.drawRect(guiLeft + 58, guiTop + 89, guiLeft + 78, guiTop + 92, 0xff803080);		

		
		if (tmpInv.getStackInSlot(0) != null) {
			SimpleServiceLocator.thaumCraftProxy.renderAspectsDown(tmpInv.getStackInSlot(0), guiLeft + 175, guiTop + 8, this);
		}
		
		SimpleServiceLocator.thaumCraftProxy.renderAspectsInGrid(_module.aspectList, guiLeft + 61, guiTop + 37, 3, 3, this);
	}
}
