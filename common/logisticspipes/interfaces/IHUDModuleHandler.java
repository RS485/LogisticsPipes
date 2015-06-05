package logisticspipes.interfaces;

public interface IHUDModuleHandler {

	public void startHUDWatching();

	public void stopHUDWatching();

	public IHUDModuleRenderer getHUDRenderer();
}
