package logisticspipes.interfaces;


public interface IHUDModuleHandler {
	public void startWatching();
	public void stopWatching();
	public IHUDModuleRenderer getRenderer();
}
