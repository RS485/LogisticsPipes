package logisticspipes.proxy;

import logisticspipes.proxy.specialinventoryhandler.BarrelInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.CrateInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.DSUInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.StorageDrawersInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.AEInterfaceInventoryHandler;

import net.minecraftforge.fml.common.Loader;

public class SpecialInventoryHandlerManager {

	public static void load() {
		if (Loader.isModLoaded("factorization")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelInventoryHandler());
		}

		if (Loader.isModLoaded("betterstorage")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CrateInventoryHandler());
		}

		if (Loader.isModLoaded("AppliedEnergistics2-Core") || Loader.isModLoaded("appliedenergistics2-core")) {
			System.out.println("Loaded AE2!");		//TODO remove this
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new AEInterfaceInventoryHandler());
		}

		if (Loader.isModLoaded("storagedrawers")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new StorageDrawersInventoryHandler());
		}

		SimpleServiceLocator.buildCraftProxy.registerInventoryHandler();

		try {
			Class.forName("powercrystals.minefactoryreloaded.api.IDeepStorageUnit");
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new DSUInventoryHandler());
		} catch (ClassNotFoundException e) {}
	}
}
