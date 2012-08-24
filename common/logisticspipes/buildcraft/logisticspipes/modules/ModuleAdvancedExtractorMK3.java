package logisticspipes.buildcraft.logisticspipes.modules;

import logisticspipes.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleAdvancedExtractorMK3 extends ModuleAdvancedExtractorMK2 {

	public ModuleAdvancedExtractorMK3() {
		super();
	}

	protected int ticksToAction(){
		return 0;
	}

	protected int itemsToExtract(){
		return 64;
	}
}
