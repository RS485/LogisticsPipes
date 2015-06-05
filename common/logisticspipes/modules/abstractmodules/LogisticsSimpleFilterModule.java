package logisticspipes.modules.abstractmodules;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.SimpleFilterInventoryInHand;
import logisticspipes.network.guis.module.inpipe.SimpleFilterInventorySlot;

import net.minecraft.inventory.IInventory;

public abstract class LogisticsSimpleFilterModule extends LogisticsGuiModule {

	public abstract IInventory getFilterInventory();

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(SimpleFilterInventorySlot.class);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(SimpleFilterInventoryInHand.class);
	}
}
