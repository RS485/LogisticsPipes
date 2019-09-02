package logisticspipes.modplugins.nei;

import net.minecraftforge.fml.common.Mod;

import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.api.NEIPlugin;
import codechicken.nei.handler.NEIClientEventHandler;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;

@NEIPlugin
public class NEILogisticsPipesConfig implements IConfigureNEI {

	public static boolean added = false;

	@Override
	public void loadConfig() {

		if (Configs.TOOLTIP_INFO && !NEILogisticsPipesConfig.added) {
			NEIClientEventHandler.addTooltipHandler(new DebugHelper());
			NEILogisticsPipesConfig.added = true;
		}

		NEIClientEventHandler.addDrawHandler(new DrawHandler());

		/*
		MultiItemRange main = new MultiItemRange();
		main.add(LogisticsPipes.LogisticsNetworkMonitior);
		main.add(LogisticsPipes.LogisticsRemoteOrderer);
		main.add(LogisticsPipes.LogisticsCraftingSignCreator);

		MultiItemRange pipesChassi = new MultiItemRange();
		pipesChassi.add(LogisticsPipes.LogisticsChassisPipeMk1);
		pipesChassi.add(LogisticsPipes.LogisticsChassisPipeMk2);
		pipesChassi.add(LogisticsPipes.LogisticsChassisPipeMk3);
		pipesChassi.add(LogisticsPipes.LogisticsChassisPipeMk4);
		pipesChassi.add(LogisticsPipes.LogisticsChassisPipeMk5);

		MultiItemRange modules = new MultiItemRange();
		modules.add(LogisticsPipes.ModuleItem, 0, 1000);

		addSetRange("LogisticsPipes", main);
		addSetRange("LogisticsPipes.Modules", modules);
		//addSetRange("LogisticsPipes.Pipes", pipes);
		addSetRange("LogisticsPipes.Pipes.Chassi", pipesChassi);
		 */

		//TODO: Uncomment
/*		API.registerRecipeHandler(new NEISolderingStationRecipeManager());
		API.registerUsageHandler(new NEISolderingStationRecipeManager());
		API.registerGuiOverlay(GuiSolderingStation.class, "solderingstation");
		API.registerGuiOverlayHandler(GuiLogisticsCraftingTable.class, new LogisticsCraftingOverlayHandler(), "crafting");
		API.registerGuiOverlayHandler(GuiRequestTable.class, new LogisticsCraftingOverlayHandler(), "crafting");
		*/
	}

	@Override
	public String getName() {
		return LogisticsPipes.class.getAnnotation(Mod.class).name();
	}

	@Override
	public String getVersion() {
		return LogisticsPipes.class.getAnnotation(Mod.class).version();
	}

}
