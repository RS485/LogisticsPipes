package logisticspipes.proxy;

import logisticspipes.proxy.specialinventoryhandler.BarrelInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.CrateInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.QuantumChestHandler;
import cpw.mods.fml.common.Loader;

public class SpecialInventoryHandlerManager {
	
	public static void load() {
		if(Loader.isModLoaded("factorization")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelInventoryHandler());
		}
		
		if(Loader.isModLoaded("GregTech_Addon")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new QuantumChestHandler());
		}

		if(Loader.isModLoaded("BetterStorage")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CrateInventoryHandler());
		}
	}
}
