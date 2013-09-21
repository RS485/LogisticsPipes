package logisticspipes.nei;

import logisticspipes.Configs;
import codechicken.nei.forge.GuiContainerManager;
import cpw.mods.fml.client.FMLClientHandler;

public class LoadingHelper {

	public static void LoadNeiNBTDebugHelper() {
		if(Configs.TOOLTIP_INFO && !NEILogisticsPipesConfig.added) {
			GuiContainerManager.addTooltipHandler(new DebugHelper());
			NEILogisticsPipesConfig.added = true;
			if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Enabled.");
			}
		} else if(NEILogisticsPipesConfig.added) {
			if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Already enabled.");
			}
		}
	}

}
