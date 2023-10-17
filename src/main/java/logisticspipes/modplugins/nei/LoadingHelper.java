package logisticspipes.modplugins.nei;

import net.minecraftforge.fml.client.FMLClientHandler;

import codechicken.nei.handler.NEIClientEventHandler;

import logisticspipes.config.Configs;

public class LoadingHelper {

	public static void LoadNeiNBTDebugHelper() {
		if (Configs.TOOLTIP_INFO && !NEILogisticsPipesConfig.added) {
			NEIClientEventHandler.addTooltipHandler(new DebugHelper());
			NEILogisticsPipesConfig.added = true;
			if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().player != null) {
				FMLClientHandler.instance().getClient().player.sendChatMessage("Enabled.");
			}
		} else if (NEILogisticsPipesConfig.added) {
			if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().player != null) {
				FMLClientHandler.instance().getClient().player.sendChatMessage("Already enabled.");
			}
		}
	}

}
