
package logisticspipes.proxy;

import logisticspipes.proxy.specialinventoryhandler.AEInterfaceInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.BarrelInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.CrateInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.DSUInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.DigitalChestHandler;
import logisticspipes.proxy.specialinventoryhandler.JABBAInventoryHandler;
import cpw.mods.fml.common.Loader;

public class SpecialInventoryHandlerManager {
	
	public static void load() {
		if(Loader.isModLoaded("factorization")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelInventoryHandler());
		}
		
		if(Loader.isModLoaded("GregTech_Addon")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new DigitalChestHandler());
		}

		if(Loader.isModLoaded("betterstorage")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CrateInventoryHandler());
		}

		if(Loader.isModLoaded("AppliedEnergistics2-Core")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new AEInterfaceInventoryHandler());
		}

		if(Loader.isModLoaded("JABBA")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new JABBAInventoryHandler());
		}

		try {
			Class.forName("powercrystals.minefactoryreloaded.api.IDeepStorageUnit");
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new DSUInventoryHandler());
		} catch(ClassNotFoundException e) {
		}
	}
}
