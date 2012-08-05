package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleAdvancedExtractorMK2 extends ModuleAdvancedExtractor {

	public ModuleAdvancedExtractorMK2() {
		super();
	}

	@Override
	protected int ticksToAction() {
		return 20;
	}
}
