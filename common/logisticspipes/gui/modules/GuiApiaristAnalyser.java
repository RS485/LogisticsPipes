package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import buildcraft.transport.Pipe;

public class GuiApiaristAnalyser extends GuiWithPreviousGuiContainer {

	private final ModuleApiaristAnalyser module;	
	
	public GuiApiaristAnalyser(ModuleApiaristAnalyser module, CoreRoutedPipe pipe, GuiScreen previousGui, IInventory playerInventory) {
		super(new DummyContainer(playerInventory,null), pipe, previousGui);
		this.module = module;
		xSize = 120;
		ySize = 60;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiStringHandlerButton(0, guiLeft + 12, guiTop + 20, 96, 20, new GuiStringHandlerButton.StringHandler() {
			@Override
			public String getContent() {
				return module.getExtractMode()==1 ? "Extract Mode: on" : "Extract Mode: off";
			}
		}));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			int mode = module.getExtractMode();
			if (mode == 1) module.setExtractMode(0); 
			else if (mode == 0) module.setExtractMode(1);
		} else {
			super.actionPerformed(par1GuiButton);		
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}
	
	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Apiarist_Analyzer;
	}
}
