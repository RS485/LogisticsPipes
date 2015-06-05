package logisticspipes.modules.abstractmodules;

import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;

public abstract class LogisticsGuiModule extends LogisticsModule {

	protected abstract ModuleCoordinatesGuiProvider getPipeGuiProvider();

	protected abstract ModuleInHandGuiProvider getInHandGuiProvider();

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

	@Override
	public final int getX() {
		if (slot.isInWorld()) {
			return _service.getX();
		} else {
			return 0;
		}
	}

	@Override
	public final int getY() {
		if (slot.isInWorld()) {
			return _service.getY();
		} else {
			return -1;
		}
	}

	@Override
	public final int getZ() {
		if (slot.isInWorld()) {
			return _service.getZ();
		} else {
			return -1 - positionInt;
		}
	}
}
