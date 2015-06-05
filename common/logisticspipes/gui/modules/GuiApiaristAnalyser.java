package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.GuiStringHandlerButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;

public class GuiApiaristAnalyser extends ModuleBaseGui {

	private final ModuleApiaristAnalyser module;

	public GuiApiaristAnalyser(ModuleApiaristAnalyser module, IInventory playerInventory) {
		super(new DummyContainer(playerInventory, null), module);
		this.module = module;
		xSize = 120;
		ySize = 60;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, guiLeft + 12, guiTop + 20, 96, 20, new GuiStringHandlerButton.StringHandler() {

			@Override
			public String getContent() {
				return module.getExtractMode() == 1 ? "Extract Mode: on" : "Extract Mode: off";
			}
		}));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 0) {
			int mode = module.getExtractMode();
			if (mode == 1) {
				module.setExtractMode(0);
			} else if (mode == 0) {
				module.setExtractMode(1);
			}
		} else {
			super.actionPerformed(par1GuiButton);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}
}
