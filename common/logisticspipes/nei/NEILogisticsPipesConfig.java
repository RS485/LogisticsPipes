package logisticspipes.nei;

import static codechicken.nei.api.API.addSetRange;

import java.util.Arrays;
import java.util.Comparator;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import net.minecraft.item.Item;
import codechicken.nei.MultiItemRange;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.forge.GuiContainerManager;
import cpw.mods.fml.common.Mod;

public class NEILogisticsPipesConfig implements IConfigureNEI {
	
	public static boolean added = false;
	
	@Override
	public void loadConfig() {
		
		if(Configs.TOOLTIP_INFO && !added) {
			GuiContainerManager.addTooltipHandler(new DebugHelper());
			added = true;
		}
		
		MultiItemRange main = new MultiItemRange();
		main.add(LogisticsPipes.LogisticsNetworkMonitior);
		main.add(LogisticsPipes.LogisticsRemoteOrderer);
		main.add(LogisticsPipes.LogisticsCraftingSignCreator);
		
		Item[] pipeArray = BuildCraftProxy.pipelist.toArray(new Item[]{});
		Arrays.sort(pipeArray, new Comparator<Item>() {
			@Override
			public int compare(Item arg0, Item arg1) {
				if(arg0.itemID < arg1.itemID) {
					return -1;
				} else if(arg0.itemID > arg1.itemID) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		MultiItemRange pipes = new MultiItemRange();
		for(Item pipe: pipeArray) {
			if(pipe != LogisticsPipes.LogisticsChassiPipe1 && pipe != LogisticsPipes.LogisticsChassiPipe2 && pipe != LogisticsPipes.LogisticsChassiPipe3 && pipe != LogisticsPipes.LogisticsChassiPipe4 && pipe != LogisticsPipes.LogisticsChassiPipe5) {
				pipes.add(pipe);
			}
		}

		MultiItemRange pipesChassi = new MultiItemRange();
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe1);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe2);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe3);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe4);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe5);
		
		MultiItemRange modules = new MultiItemRange();
		modules.add(LogisticsPipes.ModuleItem, 0, 1000);
		
		addSetRange("LogisticsPipes", main);
		addSetRange("LogisticsPipes.Modules", modules);
		addSetRange("LogisticsPipes.Pipes", pipes);
		addSetRange("LogisticsPipes.Pipes.Chassi", pipesChassi);

		API.registerRecipeHandler(new NEISolderingStationRecipeManager());
		API.registerUsageHandler(new NEISolderingStationRecipeManager());
		API.registerGuiOverlayHandler(GuiLogisticsCraftingTable.class, new LogisticsCraftingOverlayHandler(), "crafting");
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
