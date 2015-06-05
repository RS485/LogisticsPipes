package logisticspipes.network.guis.module.inpipe;

import logisticspipes.gui.modules.GuiSimpleFilter;
import logisticspipes.modules.abstractmodules.LogisticsSimpleFilterModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class SimpleFilterInventorySlot extends ModuleCoordinatesGuiProvider {

	public SimpleFilterInventorySlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsSimpleFilterModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsSimpleFilterModule.class);
		if (module == null) {
			return null;
		}
		return new GuiSimpleFilter(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsSimpleFilterModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsSimpleFilterModule.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SimpleFilterInventorySlot(getId());
	}
}
