package logisticspipes.modules;

public abstract class LogisticsGuiModule extends LogisticsModule {

	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public abstract int getGuiHandlerID();
	
	@Override
	public boolean hasGui() {
		return true;
	}
}
