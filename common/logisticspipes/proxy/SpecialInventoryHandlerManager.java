package logisticspipes.proxy;

import logisticspipes.proxy.specialinventoryhandler.AEInterfaceInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.BarrelInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.BarrelModInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.CrateInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.DSUInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.DigitalChestHandler;
import cpw.mods.fml.common.Loader;

public class SpecialInventoryHandlerManager {
	
	public static void load() {
		if(Loader.isModLoaded("factorization")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelInventoryHandler());
		}
		
		if(Loader.isModLoaded("GregTech_Addon")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new DigitalChestHandler());
		}

		if(Loader.isModLoaded("BetterStorage")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CrateInventoryHandler());
		}

		if(Loader.isModLoaded("AppliedEnergistics")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new AEInterfaceInventoryHandler());
		}

		if(Loader.isModLoaded("MineFactoryReloaded")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new DSUInventoryHandler());
		}

		if(Loader.isModLoaded("barrels")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelModInventoryHandler());
		}
	}
}
