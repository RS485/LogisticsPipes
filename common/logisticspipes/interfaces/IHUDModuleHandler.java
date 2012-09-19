package logisticspipes.interfaces;

import logisticspipes.pipes.PipeLogisticsChassi;

public interface IHUDModuleHandler {
	public void startWatching();
	public void stopWatching();
	public IHUDModuleRenderer getRenderer();
}
