package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleAdvancedExtractorMK2 extends ModuleAdvancedExtractor {

	public ModuleAdvancedExtractorMK2(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		super(invProvider, itemSender);
	}

	@Override
	protected int ticksToAction() {
		return 20;
	}
}
