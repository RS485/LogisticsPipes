package logisticspipes.proxy;

import logisticspipes.proxy.specialtankhandler.AETankHandler;
import logisticspipes.proxy.specialtankhandler.BuildCraftTankHandler;

import cpw.mods.fml.common.Loader;

public class SpecialTankHandlerManager {

	public static void load() {
		if (Loader.isModLoaded("BuildCraft|Factory")) {
			SimpleServiceLocator.specialTankHandler.registerHandler(new BuildCraftTankHandler());
		}
		if (Loader.isModLoaded("AppliedEnergistics2-Core") || Loader.isModLoaded("appliedenergistics2-core")) {
			SimpleServiceLocator.specialTankHandler.registerHandler(new AETankHandler());
		}
	}
}
