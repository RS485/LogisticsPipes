package logisticspipes.modules;


public class ModuleExtractorMk3 extends ModuleExtractorMk2 {

	public ModuleExtractorMk3() {
		super();
	}

	protected int ticksToAction(){
		return 0;
	}

	protected int itemsToExtract(){
		return 64;
	}
}
