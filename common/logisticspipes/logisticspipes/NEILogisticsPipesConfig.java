package logisticspipes.logisticspipes;

import static codechicken.nei.api.API.addSetRange;
import logisticspipes.LogisticsPipes;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.MultiItemRange;

public class NEILogisticsPipesConfig implements IConfigureNEI
{

	@Override
	public void loadConfig()
	{
		MultiItemRange main = new MultiItemRange();
		main.add(LogisticsPipes.LogisticsNetworkMonitior);
		main.add(LogisticsPipes.LogisticsRemoteOrderer);
		main.add(LogisticsPipes.LogisticsCraftingSignCreator);
		
		MultiItemRange pipes = new MultiItemRange();
		pipes.add(LogisticsPipes.LogisticsBasicPipe);
		pipes.add(LogisticsPipes.LogisticsRequestPipe);
		pipes.add(LogisticsPipes.LogisticsProviderPipe);
		pipes.add(LogisticsPipes.LogisticsCraftingPipe);
		pipes.add(LogisticsPipes.LogisticsSatellitePipe);
		pipes.add(LogisticsPipes.LogisticsSupplierPipe);
		pipes.add(LogisticsPipes.LogisticsBuilderSupplierPipe);
		pipes.add(LogisticsPipes.LogisticsLiquidSupplierPipe);
		pipes.add(LogisticsPipes.LogisticsCraftingPipeMK2);
		pipes.add(LogisticsPipes.LogisticsRequestPipeMK2);
		pipes.add(LogisticsPipes.LogisticsProviderPipeMK2);
		pipes.add(LogisticsPipes.LogisticsRemoteOrdererPipe);
		pipes.add(LogisticsPipes.LogisticsApiaristAnalyserPipe);
		
		MultiItemRange pipesChassi = new MultiItemRange();
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe1);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe2);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe3);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe4);
		pipesChassi.add(LogisticsPipes.LogisticsChassiPipe5);
		
		MultiItemRange modules = new MultiItemRange();
		modules.add(LogisticsPipes.ModuleItem, 0, 500);
		
		
		addSetRange("LogisticsPipes", main);
		addSetRange("LogisticsPipes.Modules", modules);
		addSetRange("LogisticsPipes.Pipes", pipes);
		addSetRange("LogisticsPipes.Pipes.Chassi", pipesChassi);
		
	}
	
}
