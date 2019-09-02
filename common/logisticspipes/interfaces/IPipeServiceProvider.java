package logisticspipes.interfaces;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.utils.CacheHolder;

//methods needed by modules that any CRP can offer
public interface IPipeServiceProvider extends IRoutedPowerProvider, IInventoryProvider, ISpawnParticles {

	boolean isNthTick(int n);

	DebugLogController getDebug();

	CacheHolder getCacheHolder();
}
