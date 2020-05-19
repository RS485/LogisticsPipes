package logisticspipes.modules.abstractmodules;

import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;

public abstract class LogisticsGuiModule extends LogisticsModule {

	public abstract ModuleCoordinatesGuiProvider getPipeGuiProvider();

	public abstract ModuleInHandGuiProvider getInHandGuiProvider();

	public final ModuleCoordinatesGuiProvider getPipeGuiProviderForModule() {
		return getPipeGuiProvider().setSlot(slot).setPositionInt(positionInt);
	}

	public final ModuleInHandGuiProvider getInHandGuiProviderForModule() {
		return getInHandGuiProvider().setInvSlot(positionInt);
	}

	@Override
	public boolean hasGui() {
		return true;
	}

}
