package logisticspipes.interfaces;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipes.basic.debug.DebugLogController;

//methods needed by modules that any CRP can offer
public interface IPipeServiceProvider extends IRoutedPowerProvider, IInventoryProvider, ISpawnParticles {
	public boolean isNthTick(int n);
	public DebugLogController getDebug();
}
