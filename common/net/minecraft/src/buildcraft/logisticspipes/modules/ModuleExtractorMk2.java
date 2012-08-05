package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleExtractorMk2 extends ModuleExtractor{
	
	public ModuleExtractorMk2() {
		super();
	}
	
	@Override
	protected int ticksToAction() {
		return 20;
	}

}
