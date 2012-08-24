package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.mod_LogisticsPipes;

public class NEILogisticsPipesConfig implements IConfigureNEI
{

	@Override
	public void loadConfig()
	{
		MultiItemRange main = new MultiItemRange();
		main.add(mod_LogisticsPipes.LogisticsNetworkMonitior);
		main.add(mod_LogisticsPipes.LogisticsRemoteOrderer);
		main.add(mod_LogisticsPipes.LogisticsCraftingSignCreator);
		
		MultiItemRange pipes = new MultiItemRange();
		pipes.add(mod_LogisticsPipes.LogisticsBasicPipe);
		pipes.add(mod_LogisticsPipes.LogisticsRequestPipe);
		pipes.add(mod_LogisticsPipes.LogisticsProviderPipe);
		pipes.add(mod_LogisticsPipes.LogisticsCraftingPipe);
		pipes.add(mod_LogisticsPipes.LogisticsSatellitePipe);
		pipes.add(mod_LogisticsPipes.LogisticsSupplierPipe);
		pipes.add(mod_LogisticsPipes.LogisticsBuilderSupplierPipe);
		pipes.add(mod_LogisticsPipes.LogisticsLiquidSupplierPipe);
		pipes.add(mod_LogisticsPipes.LogisticsCraftingPipeMK2);
		pipes.add(mod_LogisticsPipes.LogisticsRequestPipeMK2);
		pipes.add(mod_LogisticsPipes.LogisticsProviderPipeMK2);
		pipes.add(mod_LogisticsPipes.LogisticsRemoteOrdererPipe);
		pipes.add(core_LogisticsPipes.LogisticsApiaristAnalyserPipe);
		
		MultiItemRange pipesChassi = new MultiItemRange();
		pipesChassi.add(mod_LogisticsPipes.LogisticsChassiPipe1);
		pipesChassi.add(mod_LogisticsPipes.LogisticsChassiPipe2);
		pipesChassi.add(mod_LogisticsPipes.LogisticsChassiPipe3);
		pipesChassi.add(mod_LogisticsPipes.LogisticsChassiPipe4);
		pipesChassi.add(mod_LogisticsPipes.LogisticsChassiPipe5);
		
		MultiItemRange modules = new MultiItemRange();
		modules.add(mod_LogisticsPipes.ModuleItem, 0, 500);
		
		
		addSetRange("LogisticsPipes", main);
		addSetRange("LogisticsPipes.Modules", modules);
		addSetRange("LogisticsPipes.Pipes", pipes);
		addSetRange("LogisticsPipes.Pipes.Chassi", pipesChassi);
		
	}
	
}
