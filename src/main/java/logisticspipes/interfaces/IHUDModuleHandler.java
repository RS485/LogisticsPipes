package logisticspipes.interfaces;

public interface IHUDModuleHandler {

	void startHUDWatching();

	void stopHUDWatching();

	IHUDModuleRenderer getHUDRenderer();
}
