package logisticspipes.modules;


public class ModuleExtractorMk2 extends ModuleExtractor{
	
	public ModuleExtractorMk2() {
		super();
	}
	
	@Override
	protected int ticksToAction() {
		return 20;
	}

	@Override
	protected int neededEnergy() {
		return 7;
	}

}
