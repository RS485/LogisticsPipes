package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.krapht.gui.GuiChassiPipe;

public class ChassiPipeProxy {
	
	public static void refreshGui() {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiChassiPipe){
			ModLoader.getMinecraftInstance().currentScreen.initGui();
		}
	}
	
}
