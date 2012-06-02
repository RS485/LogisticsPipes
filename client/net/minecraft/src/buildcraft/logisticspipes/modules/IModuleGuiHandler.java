package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;

public interface IModuleGuiHandler {
	public boolean displayGui(EntityPlayer entityplayer,ILogisticsModule module, GuiScreen previousGui);
}
