package logisticspipes.proxy;

import logisticspipes.proxy.specialtankhandler.BuildCraftTankHandler;
import logisticspipes.proxy.specialtankhandler.ECInterfaceHandler;
import cpw.mods.fml.common.Loader;

public class SpecialTankHandlerManager {

	public static void load() {
		SimpleServiceLocator.specialTankHandler.registerHandler(new BuildCraftTankHandler());
		if(Loader.isModLoaded("extracells")) {
			SimpleServiceLocator.specialTankHandler.registerHandler(new ECInterfaceHandler());
		}
	}
}
