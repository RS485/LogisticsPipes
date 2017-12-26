package logisticspipes.nei;

import logisticspipes.config.Configs;

import net.minecraftforge.fml.client.FMLClientHandler;

import codechicken.nei.handler.NEIClientEventHandler;

public class LoadingHelper {

	public static void LoadNeiNBTDebugHelper() {
		if (Configs.TOOLTIP_INFO && !NEILogisticsPipesConfig.added) {
			NEIClientEventHandler.INSTANCE.addTooltipHandler(new DebugHelper());
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
