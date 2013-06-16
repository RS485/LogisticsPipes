package logisticspipes.waila;

import logisticspipes.config.Configs;
import logisticspipes.waila.providers.SolidBlockProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegister {
	public static void register(IWailaRegistrar registrar) {
		registrar.registerBodyProvider(new SolidBlockProvider(),
				Configs.LOGISTICS_SOLID_BLOCK_ID);
		registrar.addConfig("LogisticsPipes", "lp.power", "Power Junction");
	}
}