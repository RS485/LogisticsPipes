package logisticspipes.modules;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;


public class ModuleExtractorMk3 extends ModuleExtractorMk2 {

	public ModuleExtractorMk3() {
		super();
	}

	@Override
	protected int ticksToAction(){
		return 1;
	}

	@Override
	protected int itemsToExtract(){
		return 64;
	}

	@Override
	protected int neededEnergy() {
		return 10;
	}

	@Override
	protected ItemSendMode itemSendMode() {
		return ItemSendMode.Fast;
	}
}
