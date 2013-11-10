package net.machinemuse.api;

import net.minecraft.item.ItemStack;


public class ModuleManager {
	
	public static void addModule(IPowerModule module) {}
	
	public static boolean itemHasActiveModule(ItemStack itemStack, String moduleName) {return false;}

	public static boolean itemHasModule(ItemStack stack, String moduleName) {return false;}
}

