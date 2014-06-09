package logisticspipes.modules;

import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;

public abstract class LogisticsGuiModule extends LogisticsModule {

	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public int getGuiHandlerID() {
		return -1;
	}
	
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return null;
	}
	
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return null;
	}
	
	@Override
	public boolean hasGui() {
		return true;
	}
}
