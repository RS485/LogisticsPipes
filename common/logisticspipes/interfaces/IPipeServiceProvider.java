package logisticspipes.interfaces;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.logisticspipes.IInventoryProvider;

//methods needed by modules that any CRP can offer
public interface IPipeServiceProvider extends IRoutedPowerProvider, IInventoryProvider, ISpawnParticles {
	public boolean isNthTick(int n);
}
