package logisticspipes.logistics;

import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.Pair;
import net.minecraftforge.liquids.LiquidStack;

public class LogisticsLiquidManager implements ILogisticsLiquidManager {
	
	public Pair<UUID, Integer> getBestReply(LiquidStack stack, IRouter sourceRouter, List<UUID> jamList) {
		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
			if(candidateRouter.getId().equals(sourceRouter.getId())) continue;
			if(jamList.contains(candidateRouter.getId())) continue;
			
			if (candidateRouter.getPipe() == null || !candidateRouter.getPipe().isEnabled()) continue;
			CoreRoutedPipe pipe = candidateRouter.getPipe();
			
			if(!(pipe instanceof ILiquidSink)) continue;
			
			int amount = ((ILiquidSink)pipe).sinkAmount(stack);
			if(amount > 0) {
				Pair<UUID, Integer> result = new Pair<UUID, Integer>(candidateRouter.getId(), amount);
				return result;
			}
		}
		Pair<UUID, Integer> result = new Pair<UUID, Integer>(null, 0);
		return result;
	}
}
