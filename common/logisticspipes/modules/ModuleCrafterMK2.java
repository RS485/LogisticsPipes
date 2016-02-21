package logisticspipes.modules;

import logisticspipes.pipes.PipeItemsCraftingLogistics;

public class ModuleCrafterMK2 extends ModuleCrafter {

	public ModuleCrafterMK2() {}

	public ModuleCrafterMK2(PipeItemsCraftingLogistics parentPipe) {
		super(parentPipe);
	}

	@Override
	protected int neededEnergy() {
		return 15;
	}

	@Override
	protected int itemsToExtract() {
		return 64;
	}

	@Override
	protected int stacksToExtract() {
		return 1;
	}

}
