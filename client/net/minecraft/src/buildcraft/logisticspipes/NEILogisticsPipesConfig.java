package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.core_LogisticsPipes;
import codechicken.nei.IConfigureNEI;
import codechicken.nei.MultiItemRange;

import static codechicken.nei.API.*;

public class NEILogisticsPipesConfig implements IConfigureNEI
{

	@Override
	public void loadConfig()
	{
		MultiItemRange main = new MultiItemRange();
		main.add(core_LogisticsPipes.LogisticsNetworkMonitior);
		main.add(core_LogisticsPipes.LogisticsRemoteOrderer);
		main.add(core_LogisticsPipes.LogisticsCraftingSignCreator);
		
		MultiItemRange pipes = new MultiItemRange();
		pipes.add(core_LogisticsPipes.LogisticsBasicPipe);
		pipes.add(core_LogisticsPipes.LogisticsRequestPipe);
		pipes.add(core_LogisticsPipes.LogisticsProviderPipe);
		pipes.add(core_LogisticsPipes.LogisticsCraftingPipe);
		pipes.add(core_LogisticsPipes.LogisticsSatellitePipe);
		pipes.add(core_LogisticsPipes.LogisticsSupplierPipe);
		pipes.add(core_LogisticsPipes.LogisticsBuilderSupplierPipe);
		pipes.add(core_LogisticsPipes.LogisticsLiquidSupplierPipe);
		pipes.add(core_LogisticsPipes.LogisticsCraftingPipeMK2);
		pipes.add(core_LogisticsPipes.LogisticsRequestPipeMK2);
		pipes.add(core_LogisticsPipes.LogisticsProviderPipeMK2);
		pipes.add(core_LogisticsPipes.LogisticsRemoteOrdererPipe);
		pipes.add(core_LogisticsPipes.LogisticsApiaristAnalyserPipe);
		
		MultiItemRange pipesChassi = new MultiItemRange();
		pipesChassi.add(core_LogisticsPipes.LogisticsChassiPipe1);
		pipesChassi.add(core_LogisticsPipes.LogisticsChassiPipe2);
		pipesChassi.add(core_LogisticsPipes.LogisticsChassiPipe3);
		pipesChassi.add(core_LogisticsPipes.LogisticsChassiPipe4);
		pipesChassi.add(core_LogisticsPipes.LogisticsChassiPipe5);
		
		MultiItemRange modules = new MultiItemRange();
		modules.add(core_LogisticsPipes.ModuleItem, 0, 500);
		
		
		addSetRange("LogisticsPipes", main);
		addSetRange("LogisticsPipes.Modules", modules);
		addSetRange("LogisticsPipes.Pipes", pipes);
		addSetRange("LogisticsPipes.Pipes.Chassi", pipesChassi);
		
	}
	
}
