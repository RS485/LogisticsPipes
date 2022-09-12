package logisticspipes.proxy;

import net.minecraftforge.fml.common.Loader;

import static logisticspipes.LPConstants.appliedenergisticsModID;

import logisticspipes.proxy.specialinventoryhandler.AEInterfaceInventoryHandler;
import network.rs485.logisticspipes.compat.CharsetImplementationFactory;
import network.rs485.logisticspipes.proxy.StorageDrawersProxy;

public class SpecialInventoryHandlerManager {

	public static void load() {

		if (Loader.isModLoaded(appliedenergisticsModID)) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new AEInterfaceInventoryHandler());
		}

		SimpleServiceLocator.buildCraftProxy.registerInventoryHandler();

		StorageDrawersProxy.INSTANCE.registerInventoryHandler();

		SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CharsetImplementationFactory());
	}

}
