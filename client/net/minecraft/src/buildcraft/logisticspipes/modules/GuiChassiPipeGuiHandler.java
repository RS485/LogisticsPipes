package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.krapht.gui.GuiChassiPipe;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;

public class GuiChassiPipeGuiHandler implements IModuleGuiHandler {
	;
	PipeLogisticsChassi _parentPipe;
	public GuiChassiPipeGuiHandler(PipeLogisticsChassi parentPipe) {
		_parentPipe = parentPipe;
	}

	@Override
	public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
		ModLoader.openGUI(entityplayer, new GuiChassiPipe(entityplayer, _parentPipe, previousGui));
		return true;
	}

}
