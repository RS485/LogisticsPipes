package logisticspipes.modules;

import logisticspipes.logisticspipes.IInventoryProvider;

public class ModuleAdvancedExtractorMK2 extends ModuleAdvancedExtractor {

	public ModuleAdvancedExtractorMK2() {
		super();
	}

	@Override
	protected int ticksToAction() {
		return 20;
	}
}
