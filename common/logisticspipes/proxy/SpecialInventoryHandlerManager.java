package logisticspipes.proxy;

import logisticspipes.proxy.specialinventoryhandler.AEInterfaceInventoryHandler;
import net.minecraftforge.fml.common.Loader;
import network.rs485.logisticspipes.compat.CharsetImplementation;
import network.rs485.logisticspipes.proxy.StorageDrawersProxy;

import static logisticspipes.LPConstants.*;

public class SpecialInventoryHandlerManager {

	public static void load() {

		if (Loader.isModLoaded(appliedenergisticsModID)) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new AEInterfaceInventoryHandler());
		}

		SimpleServiceLocator.buildCraftProxy.registerInventoryHandler();

		StorageDrawersProxy.INSTANCE.registerInventoryHandler();
	}

}
