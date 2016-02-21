package logisticspipes.modules.abstractmodules;

import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import net.minecraft.util.BlockPos;

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
	public abstract BlockPos getblockpos();
}

