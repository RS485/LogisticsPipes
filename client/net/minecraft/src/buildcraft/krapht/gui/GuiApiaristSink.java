package net.minecraft.src.buildcraft.krapht.gui;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsApiaristSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristSink.SinkSetting;
import net.minecraft.src.krapht.gui.BasicGuiHelper;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;

public class GuiApiaristSink extends KraphtBaseGuiScreen {

	private final ModuleApiaristSink module;
	private final EntityPlayer player;
	
	public GuiApiaristSink(ModuleApiaristSink module, EntityPlayer player) {
		super(120, 150, 0, 0);
		this.module = module;
		this.player = player;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel);
		for(int i=0; i < 6; i++) {
			SinkSetting filter = module.filter[i];
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 20, guiTop + 20 + (i*18));
			BasicGuiHelper.renderForestryIconAt(mc, guiLeft + 21, guiTop + 21 + (i*18), zLevel, filter.filterType.icon);
			if(filter.filterType.secondSlots > 0) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 60, guiTop + 20 + (i*18));
				BasicGuiHelper.renderForestryIconAt(mc, guiLeft + 61, guiTop + 21 + (i*18), zLevel, filter.filterType.icon);
				if(filter.filterType.secondSlots > 1) {
					BasicGuiHelper.drawSlotBackground(mc, guiLeft + 78, guiTop + 20 + (i*18));
					BasicGuiHelper.renderForestryIconAt(mc, guiLeft + 79, guiTop + 21 + (i*18), zLevel, filter.filterType.icon);
				}
			}
		}
	}
	
	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Apiarist_Sink_ID;
	}

}
