package logisticspipes.interfaces;

import logisticspipes.api.IRoutedPowerProvider;

//methods needed by modules that any CRP can offer
public interface IPipeServiceProvider extends IRoutedPowerProvider, ISpawnParticles {
	public boolean isNthTick(int n);
}
