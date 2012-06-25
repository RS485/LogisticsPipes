package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleAdvancedExtractorMK3 extends ModuleAdvancedExtractorMK2 {

	public ModuleAdvancedExtractorMK3(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		super(invProvider, itemSender);
	}

	protected int ticksToAction(){
		return 0;
	}

	protected int itemsToExtract(){
		return 64;
	}
}
